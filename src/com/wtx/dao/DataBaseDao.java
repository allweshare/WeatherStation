package com.wtx.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import com.wtx.model.MinuteData;

public class DataBaseDao {
	
	private static DataBaseDao instance = null;
	//private final String DB_URL = "jdbc:derby:D:\\\\WTX_DATA;create=true";
	private final String DB_URL;
	private final SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private final String sql_create_table = "create table mdata(" + 
			"time TIMESTAMP primary key," + 
			"temp DOUBLE," + 
			"humd DOUBLE," + 
			"pres DOUBLE," + 
			"wdc SMALLINT," + 
			"wsc DOUBLE," + 
			"wd1m SMALLINT," + 
			"ws1m DOUBLE," + 
			"wdtm SMALLINT," + 
			"wstm DOUBLE," + 
			"rain DOUBLE," + 
			"rssi SMALLINT)";
	private final String sql_search_rain = "select TIME,RAIN from mdata where time between "
			+ "? and ? order by time asc";
	
	private final String sql_insert_data = "insert into mdata(time,temp,humd,pres,wdc,wsc,wd1m,ws1m,wdtm,wstm,rain,rssi" + 
			") values (?,?,?,?,?,?,?,?,?,?,?,?)";
	
	private final String sql_search_data = "select * from mdata where time between ? and ? order by time asc";
	
	public Set<MinuteData> load_mdata(Date start,Date end) throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(start);
		if(calendar.get(Calendar.MINUTE) != 0 || calendar.get(Calendar.SECOND) != 0) {
			throw new Exception("查询起始时间必须是整点(00分00秒)!");
		}
		calendar.setTime(end);
		if(calendar.get(Calendar.MINUTE) != 0 || calendar.get(Calendar.SECOND) != 0) {
			throw new Exception("查询截至时间必须是整点(00分00秒)!");
		}
		//-------------------------------------------------------------------------
		Set<MinuteData> result = new TreeSet<MinuteData>() ;
		//检查并创建数据库
		ResultSet rst = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql_search_data);
			pstmt.setTimestamp(1, new Timestamp(start.getTime()));
			pstmt.setTimestamp(2, new Timestamp(end.getTime()));
			rst = pstmt.executeQuery();
			while((!rst.isClosed()) && rst.next()) {
				MinuteData data = new MinuteData();
				data.setTime(rst.getTimestamp(1));		//时间
				data.setTempreature(rst.getDouble(2));		//温度
				data.setHumidity(rst.getDouble(3));				//湿度
				data.setPressure(rst.getDouble(4));				//气压
				data.setWind_dir_curr(rst.getShort(5));			//瞬时风向
				data.setWind_speed_curr(rst.getDouble(6));	 //瞬时风速
				data.setWind_dir_1min(rst.getShort(7));  					//一分钟风向
				data.setWind_speed_1min(rst.getDouble(8));			//一分钟风速
				data.setWind_dir_10min(rst.getShort(9));  				//十分钟风向
				data.setWind_speed_10min(rst.getDouble(10));		//十分钟风速
				data.setRain_hour(rst.getDouble(11)); 			//雨量
				data.setRssi(rst.getShort(12));	 					//RSSI
				result.add(data);
			}
			return result;
		}catch (SQLSyntaxErrorException e) {
			e.printStackTrace();
		}catch (SQLException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(rst != null) {
				rst.close();
			}
			if(pstmt != null) {
				pstmt.close();
			}
			if(conn != null) {
				conn.close();
			}
		}
		return null;
	}
	
	/**
	 * 查询指定时间24小时以内的累计雨量
	 */
	public double rain_sum_day(Date end) {
		Calendar cal_start = Calendar.getInstance();
		cal_start.setTime(end);
		cal_start.add(Calendar.HOUR_OF_DAY, -24);
		
		System.out.println("Rain Sum: " + timeFmt.format(cal_start.getTime()) + "\t" + timeFmt.format(end));
		ResultSet rst = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		
		int cache_hour = 90;
		double rainSum = 0.0;				//总的累计雨量
		double hourRainMax = 0.0;		//每小时最大的雨量
		
		Calendar cal_chk = Calendar.getInstance();
		
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql_search_rain);
			pstmt.setTimestamp(1, new Timestamp(cal_start.getTime().getTime()));
			pstmt.setTimestamp(2, new Timestamp(end.getTime()));
			rst = pstmt.executeQuery();
			while(rst.next()) {
				cal_chk.setTime(new Date(rst.getTimestamp(1).getTime()));
				if(cache_hour != cal_chk.get(Calendar.HOUR_OF_DAY)) {
					//System.out.println("New Hour ! : " + hourRainMax);
					rainSum += hourRainMax;
					hourRainMax = 0.0;
				}
				if(hourRainMax < rst.getDouble(2)) {
					hourRainMax = rst.getDouble(2);
				}
				cache_hour = cal_chk.get(Calendar.HOUR_OF_DAY);
			}
			//System.out.println("Last Hour ! : " + hourRainMax);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(rst != null) {
				try {
					rst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return rainSum;
	}
	
	/**
	 * 添加分钟数据记录
	 */
	public void insert_mdata(MinuteData mdata) {
		//检查并创建数据库
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql_insert_data);
			//Timestamp time = Timestamp.valueOf(timeFmt.format(mdata.getTime()));
			pstmt.setString(1, timeFmt.format(mdata.getTime()) + ":00");		//时间
			pstmt.setDouble(2, mdata.getTempreature());				//温度
			pstmt.setDouble(3, mdata.getHumidity());				//湿度
			pstmt.setDouble(4, mdata.getPressure());			//气压
			pstmt.setInt(5, mdata.getWind_dir_curr());						//瞬时风向
			pstmt.setDouble(6, mdata.getWind_speed_curr());					//瞬时风速
			pstmt.setInt(7, mdata.getWind_dir_1min());						//一分钟风向
			pstmt.setDouble(8, mdata.getWind_speed_1min());					//一分钟风速
			pstmt.setInt(9, mdata.getWind_dir_10min());						//十分钟风向
			pstmt.setDouble(10, mdata.getWind_speed_10min());				//十分钟风速
			pstmt.setDouble(11, mdata.getRain_hour());				//雨量
			pstmt.setInt(12, mdata.getRssi());						//RSSI
			
			pstmt.executeUpdate();
			System.out.println("已插入数据: " + timeFmt.format(mdata.getTime()));
		} catch (IllegalArgumentException e) {
			System.err.println("插入分钟数据时间错误!");
		} catch (SQLIntegrityConstraintViolationException e) {
			System.err.println("主键重复");
		} catch (SQLSyntaxErrorException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(pstmt != null) {
					pstmt.close();
				}
				if(conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private DataBaseDao() {
		Properties osProp = System.getProperties();
		String osName = osProp.getProperty("os.name");
		String osDBURL = null;
		if(osName != null) {
			if(osName .toLowerCase().indexOf("win") >-1) {
				osDBURL = "jdbc:derby:D:\\\\WTX_DATA;create=true";
			}else if(osName.toLowerCase().indexOf("linux") > - 1) {
				osDBURL = "jdbc:derby:"+osProp.getProperty("user.home", "")+"/WTX_DATA;create=true";
			}else {
				System.err.println("不支持的操作系统 : " + osName);
			}
		}
		DB_URL = osDBURL;
		
		try {
			//加载驱动
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			Connection conn = getConnection();
			if(conn != null) {
				//检查并创建数据库
				PreparedStatement pstmt = conn.prepareStatement(sql_create_table);
				pstmt.executeUpdate();
				System.out.println("数据表已创建!");
				conn.close();
			}
		} catch (SQLSyntaxErrorException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			if(e.getErrorCode() == 40000) {
				System.err.println("数据库被别的应用程序占用！");
			}else if(e.getErrorCode() == 30000) {
				System.err.println("数据表已存在！");
			}else {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Connection getConnection() {
		try {
			Connection conn = DriverManager.getConnection(DB_URL);
			return conn;
		} catch (SQLException e) {
			if(e.getErrorCode() == 40000) {
				System.err.println("数据库文件被别的应用程序占用！");
			}else {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static DataBaseDao getInstance() {
		if(instance == null) {
			synchronized (DataBaseDao.class) {
				if(instance == null) {
					instance = new DataBaseDao();
				}
			}
		}
		return instance;
	}
}
