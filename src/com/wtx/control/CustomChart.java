package com.wtx.control;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import com.wtx.dao.DataBaseDao;
import com.wtx.model.MinuteData;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class CustomChart extends TabPane {
	//private int cache_hour = 90;
	private LineChart<Number,Number> chart_thp = null;		//温湿压曲线图
	private LineChart<Number,Number> chart_rain = null;	//降水强度柱状图	
	//显示方式一中要显示多少小时以内的数据
	private final int dispHours;
	//显示方式二中要显示的时间区间
	private Date dispStart;
	private Date dispEnd;
	//定义温度范围
	private final double BOUND_TEMP_UP = 60.0;
	private final double BOUND_TEMP_DOWN = -40.0;
	//定义气压温度范围
	private final double BOUND_PRES_UP = 675.0;
	private final double BOUND_PRES_DOWN = 1050.0;
	
	//定义显示曲线比例范围(温度、湿度、气压数值缩放至0-100的数字)
	private final double BOUND_DISP_RANGE = 100.0;
	//private final double FACT_DIV_DOWN = 0.0;
	
	private double initX;
    private double initY;
	
	private SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public Date getDispStart() {
		return dispStart;
	}
	public Date getDispEnd() {
		return dispEnd;
	}
	public int getDispHours() {
		return dispHours;
	}

	/**
	 * 创建温湿压显示图谱
	 */
	private void create_chart_thp() {
		Tab tab_thp = new Tab("温湿压曲线");
		NumberAxis xAxis_thp = new NumberAxis();			//时间轴
		Calendar cal_end = Calendar.getInstance();
		cal_end.setTime(this.dispEnd);
		xAxis_thp.setUpperBound(cal_end.get(Calendar.HOUR_OF_DAY));
		//计算相差的小时数
		long diff_hours = (this.dispEnd.getTime() - this.dispStart.getTime()) / (60 * 60 * 1000);
		xAxis_thp.setLowerBound(cal_end.get(Calendar.HOUR_OF_DAY) - diff_hours);
		xAxis_thp.setTickUnit(1);
		xAxis_thp.setAutoRanging(false);
		xAxis_thp.setTickLabelFill(Color.WHITE);
		xAxis_thp.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number num) {
				if(num.intValue() < 0) {
					return String.valueOf(diff_hours + num.intValue());
				}else if(num.intValue() >=0 && num.intValue() < 10) {
					return "0"+String.valueOf(num.intValue());
				}else if(num.intValue() >= 10){
					return String.valueOf(num.intValue());
				}else {
					return "Nan";
				}
			}
			
			@Override
			public Number fromString(String string) {
				return Integer.valueOf(string);
			}
		});
		
		NumberAxis yAxis = new NumberAxis();							//数据轴
		yAxis.setTickLabelsVisible(false);
		yAxis.setTickMarkVisible(false);
		yAxis.setMinorTickVisible(false);
		chart_thp = new LineChart<Number,Number>(xAxis_thp, yAxis);
		chart_thp.setPrefWidth(300);
		tab_thp.setClosable(false);
		tab_thp.setContent(new BorderPane(chart_thp));
		this.getTabs().add(tab_thp);
	}
	
	private void create_timeline() {
		Timeline timeLine = new Timeline();
		timeLine.setCycleCount(Timeline.INDEFINITE);
		timeLine.setAutoReverse(true);
		//设置一个刷新数据的频率
		//KeyFrame key1 = new KeyFrame(Duration.seconds(30),new EventHandler<ActionEvent>() {
		KeyFrame key1 = new KeyFrame(Duration.minutes(15),new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Calendar calc_div = Calendar.getInstance();
				calc_div.setTime(new Date());
				calc_div.set(Calendar.MINUTE, 0);
				calc_div.set(Calendar.SECOND, 0);
				calc_div.add(Calendar.HOUR_OF_DAY, 1);
				dispEnd = calc_div.getTime();
				calc_div.setTime(dispEnd);
				calc_div.set(Calendar.MINUTE, 0);
				calc_div.set(Calendar.SECOND, 0);
				calc_div.add(Calendar.HOUR_OF_DAY, 0-dispHours);
				dispStart = calc_div.getTime();
				//-------------------------------------------------------------
				//更新温湿压显示区间
				NumberAxis xAxis_thp = (NumberAxis)chart_thp.getXAxis();
				xAxis_thp.setUpperBound(calc_div.get(Calendar.HOUR_OF_DAY));
				//计算相差的小时数
				final long offset_hours = (dispEnd.getTime() - dispStart.getTime()) / (60 * 60 * 1000);
				xAxis_thp.setLowerBound(calc_div.get(Calendar.HOUR_OF_DAY) - offset_hours);
				//-------------------------------------------------------------
				//更新雨量显示区间
				NumberAxis xAxis_rain = (NumberAxis)chart_rain.getXAxis();
				xAxis_rain.setUpperBound(calc_div.get(Calendar.HOUR_OF_DAY));
				xAxis_rain.setLowerBound(calc_div.get(Calendar.HOUR_OF_DAY) - offset_hours);
				//-------------------------------------------------------------
				System.out.println("加载曲线数据: ");
				System.out.println(timeFmt.format(dispStart));
				System.out.println(timeFmt.format(dispEnd));
				
				load_chart_data();
			}
		});
		timeLine.getKeyFrames().add(key1);
		timeLine.play();
	}
	
	/**
	 * 构造方式一
	 * 默认显示指定多少小时以内的数据
	 */
	public CustomChart(int hours) throws Exception {
		//显示的小时数
		this.dispHours = hours;
		this.dispEnd = new Date();		//结尾是当前的时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dispEnd);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.add(Calendar.HOUR_OF_DAY, 1);	//显示区间向后推一个小时
		this.dispEnd = calendar.getTime();
		calendar.add(Calendar.HOUR_OF_DAY, 0-this.dispHours);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		this.dispStart = calendar.getTime();
		
//		System.out.println("Start: " + timeFmt.format(dispStart));
//		System.out.println("End: " + timeFmt.format(dispEnd));
		
		//验证显示图表的时间区间
		calendar.setTime(dispEnd);
		if(calendar.get(Calendar.MINUTE) != 0) {
			throw new Exception("结束时间必须是0分0秒 !");
		}
		if(calendar.get(Calendar.SECOND) != 0) {
			throw new Exception("结束时间必须是0分0秒 !");
		}
		calendar.setTime(dispStart);
		if(calendar.get(Calendar.MINUTE) != 0) {
			throw new Exception("起始时间必须是0分0秒 !");
		}
		if(calendar.get(Calendar.SECOND) != 0) {
			throw new Exception("起始时间必须是0分0秒 !");
		}
		
		this.setSide(Side.BOTTOM);
		//创建温湿压显示图谱
		create_chart_thp();
		//创建降水量柱状图
		create_chart_rain();
		//创建周期任务
		create_timeline();
	}
	
	/**
	 * 绑定鼠标拖拽事件,使窗口位置可以被移动
	 */
	public void bindDragEvent() {
		Window wnd_parent = this.getScene().getWindow();
		if(wnd_parent != null && wnd_parent instanceof Stage) {
			Stage primaryStage = (Stage)wnd_parent;
			this.setOnMouseDragged((MouseEvent me) -> {
				primaryStage.setX(me.getScreenX() - initX);
				primaryStage.setY(me.getScreenY() - initY);
			});
			this.setOnMousePressed((MouseEvent me) -> {
				initX = me.getScreenX() - primaryStage.getX();
	            initY = me.getScreenY() - primaryStage.getY();
			});
		}
	}
	
	/**
	 * 构造方式二
	 * 显示指定时间范围内的数据
	 */
	public CustomChart(Date start,Date end) throws Exception {
		//显示的时间范围
		this.dispHours = 0;
		this.dispStart = start;
		this.dispEnd = end;
		
		//验证显示图表的时间区间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dispEnd);
		if(calendar.get(Calendar.MINUTE) != 0) {
			throw new Exception("结束时间必须是0分0秒 !");
		}
		if(calendar.get(Calendar.SECOND) != 0) {
			throw new Exception("结束时间必须是0分0秒 !");
		}
		calendar.setTime(dispStart);
		if(calendar.get(Calendar.MINUTE) != 0) {
			throw new Exception("起始时间必须是0分0秒 !");
		}
		if(calendar.get(Calendar.SECOND) != 0) {
			throw new Exception("起始时间必须是0分0秒 !");
		}
		
		this.setSide(Side.BOTTOM);
		
		//创建温湿压显示图谱
		create_chart_thp();
		//创建降水量柱状图
		create_chart_rain();
		//方式二,创建后直接加载数据
		load_chart_data();
	}
	
	/**
	 * 创建后直接加载历史数据
	 */
	private void load_chart_data() {
		//清空原有的系列
		try {
			chart_thp.getData().clear();
			chart_rain.getData().clear();
			//计算相差的小时数
			//-------------------------------------------
			XYChart.Series<Number, Number> series_temp = new XYChart.Series<Number, Number>();
			series_temp.setName("温度");
			chart_thp.getData().add(series_temp);
			//-------------------------------------------
			XYChart.Series<Number, Number> series_humd = new XYChart.Series<Number, Number>();
			series_humd.setName("湿度");
			chart_thp.getData().add(series_humd);
			//-------------------------------------------
			XYChart.Series<Number, Number> series_press = new XYChart.Series<Number, Number>();
			series_press.setName("气压");
			chart_thp.getData().add(series_press);
			//-------------------------------------------
			XYChart.Series<Number, Number> serials_rain = new XYChart.Series<>();
			serials_rain.setName("雨量");
			chart_rain.getData().add(serials_rain);
			//-------------------------------------------
			DataBaseDao dao = DataBaseDao.getInstance();
			Set<MinuteData> dataset = dao.load_mdata(dispStart, dispEnd);
			Iterator<MinuteData> dataItert = dataset.iterator();
			MinuteData cacheLast = null;
			int lastHour = 90;
			Calendar cal_last = Calendar.getInstance();
			while(dataItert.hasNext()) {
				MinuteData mdata = dataItert.next();
				Calendar currCal = Calendar.getInstance();
				currCal.setTime(mdata.getTime());
				if(lastHour != currCal.get(Calendar.HOUR_OF_DAY) && cacheLast != null) {
					//System.out.println("Point: " + mdata.getTime());
					//以小时作为纵坐标点的索引
					//long diff_hours = (dispEnd.getTime() - cacheLast.getTime().getTime()) / (60 * 60 * 1000);
					long diff_hours = (dispEnd.getTime() - mdata.getTime().getTime()) / (60 * 60 * 1000);
					cal_last.setTime(dispEnd);
					//计算横坐标
					double x = cal_last.get(Calendar.HOUR_OF_DAY) - diff_hours;
					{
						/**
						 * 温度显示范围:-40 ~ 60 
						 * 纵坐标要转换为0-100的刻度
						 */
						double y = (cacheLast.getTempreature() + (0-BOUND_TEMP_DOWN)) 
								/ (BOUND_TEMP_UP - BOUND_TEMP_DOWN) * BOUND_DISP_RANGE;
						series_temp.getData().add(new Data<Number, Number>(x,y));
					}
					
					{
						/**
						 * 湿度显示范围0-100
						 */
						double y = cacheLast.getHumidity();
						series_humd.getData().add(new Data<Number, Number>(x,y));
					}
					
					{
						/**
						 * 气压显示范围:500.0 ~ 1100.0 
						 * 纵坐标要转换为0-100的刻度
						 */
						double y = (cacheLast.getPressure() - BOUND_PRES_DOWN) / (BOUND_PRES_UP - BOUND_PRES_DOWN) * 100;
						series_press.getData().add(new Data<Number, Number>(x, y));
					}
					
					{
						/**
						 * 雨量取每小时雨量最大的那组
						 */
						Calendar cal_rain_time = Calendar.getInstance();
						cal_rain_time.setTime(cacheLast.getTime());
						XYChart.Data<Number, Number> dataCurr = new XYChart.Data<>(x,cacheLast.getRain_hour());
						serials_rain.getData().add(dataCurr);
						
					}
				}
				lastHour = currCal.get(Calendar.HOUR_OF_DAY);
				cacheLast = mdata;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//创建降水量柱状图
	private void create_chart_rain() {
		try {
			Tab tab_rain = new Tab("降水趋势图");
			NumberAxis xAxis_rain = new NumberAxis();
			Calendar cal_end = Calendar.getInstance();
			cal_end.setTime(this.dispEnd);
			xAxis_rain.setUpperBound(cal_end.get(Calendar.HOUR_OF_DAY));
			//计算相差的小时数
			long diff_hours = (this.dispEnd.getTime() - this.dispStart.getTime()) / (60 * 60 * 1000);
			xAxis_rain.setLowerBound(cal_end.get(Calendar.HOUR_OF_DAY) - diff_hours);
			xAxis_rain.setTickUnit(1);
			xAxis_rain.setAutoRanging(false);
			xAxis_rain.setTickLabelFill(Color.WHITE);
			xAxis_rain.setTickLabelFormatter(new StringConverter<Number>() {
				@Override
				public String toString(Number num) {
					if(num.intValue() < 0) {
						return String.valueOf(diff_hours + num.intValue());
					}else if(num.intValue() >=0 && num.intValue() < 10) {
						return "0"+String.valueOf(num.intValue());
					}else if(num.intValue() >= 10){
						return String.valueOf(num.intValue());
					}else {
						return "Nan";
					}
				}
				@Override
				public Number fromString(String string) {
					return Integer.valueOf(string);
				}
			});
			//计算相差的小时数
			NumberAxis yAxis = new NumberAxis();							//数据轴
			yAxis.setTickLabelFill(Color.WHITE);
			chart_rain = new LineChart<Number,Number>(xAxis_rain, yAxis);
			//---------------------------------------------------------
			chart_rain.setPrefWidth(300);
			chart_rain.setLegendVisible(false);
			tab_rain.setClosable(false);
			tab_rain.setContent(chart_rain);
			this.getTabs().add(tab_rain);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
