package com.wtx.service;

import java.util.Arrays;
import java.util.Observable;
import java.util.zip.CRC32;

import com.wtx.model.RealTimeData;

public class DataProtocol extends Observable {
	
	//定义每条数据的最大长度为76字节
	private final int MAX_RECORD_LEN = 76;
	private static DataProtocol instance = null;
	private final byte FLAG_START = 'S';		//数据的帧头
	private final byte FLAG_END = '\n';			//数据的帧尾
	
	private byte[] dataRecord = new byte[MAX_RECORD_LEN];
	private int position = 0;
	
	/**
	 * 解析接收完整数据帧的数据
	 * @param buff
	 * @param len
	 */
	private void analysis(byte[] buff,int len) {
		/**
		 * 先检查CRC校验是否匹配
		 */
		try {
			String crc32_curr = new String(buff,66,8);
			Long crcVal = Long.parseLong(crc32_curr,16);
			CRC32 crc32 = new CRC32();
			if(buff != null) {
				crc32.update(buff, 0, 66);
			}
			if(crc32.getValue() == crcVal.longValue()) {
			//if(crc32 != null) {
				//CRC32匹配
				System.out.println("CRC32 Matched !");
				RealTimeData rtData = new RealTimeData();
				
				//台站编号
				//String station_id = new String(buff,1,4);
				//System.out.println("台站编号: " + station_id);
				{
					//温度
					String str_val = new String(buff,5,4);
					Double val = Double.valueOf(str_val);
					rtData.setTempreature(val.doubleValue() / 10.0);	//一位小数
					//System.out.println("温度: " + rtData.getTempreature());
				}
				{
					//湿度
					String str_val = new String(buff,9,3);
					Double val = Double.valueOf(str_val);
					rtData.setHumidity(val.doubleValue() / 10.0);			//一位小数
					//System.out.println("湿度: " +rtData.getHumidity());	
				}
				{
					//气压
					String str_val = new String(buff,12,5);
					Double val = Double.valueOf(str_val);
					rtData.setPressure(val.doubleValue() / 10.0);				//一位小数
					//System.out.println("气压: " + rtData.getPressure());
				}
				{
					//瞬时风
					String str_dir = new String(buff, 17, 3);
					Short val_dir = Short.valueOf(str_dir);
					rtData.setWind_dir_curr(val_dir.shortValue());
					String str_speed = new String(buff, 20, 3);
					Double val_speed = Double.valueOf(str_speed);
					rtData.setWind_speed_curr(val_speed.doubleValue() / 10.0);	//一位小数
					//System.out.println("瞬时风: " + rtData.getWind_dir_curr() +"°\t" + rtData.getWind_speed_curr() + "m/s");
				}
				{
					//一分钟风
					String str_dir = new String(buff, 23, 3);
					Short val_dir = Short.valueOf(str_dir);
					rtData.setWind_dir_1min(val_dir.shortValue());
					String str_speed = new String(buff, 26, 3);
					Double val_speed = Double.valueOf(str_speed);
					rtData.setWind_speed_1min(val_speed.doubleValue() / 10.0);	//一位小数
					//System.out.println("一分钟风: " + rtData.getWind_dir_1min() +"°\t" + rtData.getWind_speed_1min() + "m/s");
				}
				{
					//十分钟风
					String str_dir = new String(buff, 29, 3);
					Short val_dir = Short.valueOf(str_dir);
					rtData.setWind_dir_10min(val_dir.shortValue());
					String str_speed = new String(buff, 32, 3);
					Double val_speed = Double.valueOf(str_speed);
					rtData.setWind_speed_10min(val_speed.doubleValue() / 10.0);	//一位小数
					//System.out.println("十分钟风: " + rtData.getWind_dir_10min() +"°\t" + rtData.getWind_speed_10min() + "m/s");
				}
				{
					//小时雨量
					String str_val = new String(buff,35,3);
					Double val = Double.valueOf(str_val);
					rtData.setRain_hour(val.doubleValue() / 10.0);				//一位小数
					//System.out.println("雨量: " + rtData.getRain_hour());
				}
				{
					//设备电压
					String str_val = new String(buff,38,3);
					Double val = Double.valueOf(str_val);
					rtData.setVoltage(val.doubleValue() / 10.0);				//一位小数
					//System.out.println("电压: " + rtData.getVoltage());
				}
//				{
//					//维度
//					String str_val = new String(buff,41,10);
//					rtData.setLatitude(str_val);
//					System.out.println("维度: " + rtData.getLatitude()); 
//				}
//				{
//					//经度
//					String str_val = new String(buff,51,10);
//					rtData.setLongitude(str_val);
//					System.out.println("经度: " + rtData.getLongitude());
//				}
				{
					//信号强度
					String str_val = new String(buff,62,2);
					Short val = Short.valueOf(str_val);
					rtData.setRssi(val.shortValue());
					//System.out.println("RSSI: " + rtData.getRssi());
				}
				
				//发送事件通知
				setChanged();
				notifyObservers(rtData);
				
			}else{
				//CRC32校验不通过
				System.err.println("数据帧CRC32校验错误 !");
			}
		} catch (NumberFormatException e) {
			System.err.println("数据帧中含有不可解析字符!");
		} catch (Exception e) {
			System.err.println("数据帧无效 !");
		}
		
	}
	
	/**
	 * 拼接并解析数据
	 */
	public boolean parse(byte[] buff,int len) {
		try {
			if(position == 0 && (buff[0] == (byte)0x00 || buff[0] == (byte)0x00FF)) {
				//排除乱码
				position = 0;
				Arrays.fill(dataRecord,(byte)0);		//清空解析缓冲区
				return false;
			}
			
			if(buff[0] == FLAG_START && len <= MAX_RECORD_LEN && position == 0) {
				System.arraycopy(buff, 0, dataRecord, 0, len);
				position = len;
			}else if(position > 0 && position <= MAX_RECORD_LEN){
				if((position + len) > MAX_RECORD_LEN) {
					System.err.println("数据长度无效!");
					Arrays.fill(dataRecord,(byte)0);		//清空解析缓冲区
					position = 0;
				}else{
					System.arraycopy(buff, 0, dataRecord, position, len);
					position = position + len;
				}
			}
			
			if(dataRecord[0] == FLAG_START && dataRecord[MAX_RECORD_LEN-1] == FLAG_END) {
				System.out.println("数据完整!");
				/**
				 * 在接收到完整的数据开始解析数据
				 */
				analysis(dataRecord,position);
				
				position = 0;
				Arrays.fill(dataRecord,(byte)0);		//清空解析缓冲区
				System.out.println("--------------------------------------------");
				for(int i = 0;i<position;i ++) {
					if(Integer.toHexString((int)dataRecord[i] & 0x000000FF).length() == 1) {
						System.out.print("0"+Integer.toHexString((int)dataRecord[i] & 0x000000FF) + " ");
					}else {
						System.out.print(Integer.toHexString((int)dataRecord[i] & 0x000000FF) + " ");
					}
				}
				System.out.println();
			}
			return true;
		} catch (Exception e) {
			System.err.println("串口缓冲区异常!");
		}
		return false;
	}
	
	private DataProtocol() {
		
	}
	
	public static DataProtocol getParser() {
		if(instance == null){
			instance = new DataProtocol();
		}
		return instance;
	}
}
