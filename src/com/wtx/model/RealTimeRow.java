package com.wtx.model;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RealTimeRow {
	private static SimpleDateFormat timeDispFmt = new SimpleDateFormat("HH:mm:ss");
	private static DecimalFormat numFmt = new DecimalFormat("0.0");
	protected StringProperty col_Time;					//显示分钟数据的时间
	protected StringProperty col_Temp;					//温度
	protected StringProperty col_Humd;					//湿度
	protected StringProperty col_Press;					//气压
	protected StringProperty col_Wcurr;					//瞬时风
	protected StringProperty col_W1min;				//一分钟风
	protected StringProperty col_W10min;				//十分钟时风
	protected StringProperty col_Rain;						//小时雨量
	
	public static RealTimeRow createFrom(RealTimeData data) {
		try {
			RealTimeRow row = new RealTimeRow();
			row.col_Time.setValue(timeDispFmt.format(new Date()));
			row.col_Temp.setValue(numFmt.format(data.getTempreature()));
			row.col_Humd.setValue(numFmt.format(data.getHumidity()));
			row.col_Press.setValue(numFmt.format(data.getPressure()));
			//瞬时风
			if(data.getWind_speed_curr() > 0.0) {
				row.col_Wcurr.setValue(""+data.getWind_speed_curr() + "m/s | " + data.getWind_dir_curr() + "°");
			}else {
				row.col_Wcurr.setValue("----");
			}
			//一分钟风
			if(data.getWind_speed_1min() > 0.0) {
				row.col_W1min.setValue(""+data.getWind_speed_1min() + "m/s | " + data.getWind_dir_1min() + "°");
			}else{
				row.col_W1min.setValue("----");
			}
			//十分钟风
			if(data.getWind_speed_10min() > 0.0) {
				row.col_W10min.setValue(""+data.getWind_speed_10min() + "m/s | " + data.getWind_dir_10min() + "°");
			}else {
				row.col_W10min.setValue("----");
			}
			//小时雨量
			row.col_Rain.setValue(numFmt.format(data.getRain_hour()));
			return row;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public StringProperty col_TimeProperty() {
		return col_Time;
	}
	public StringProperty col_TempProperty() {
		return col_Temp;
	}
	public StringProperty col_HumdProperty() {
		return col_Humd;
	}
	public StringProperty col_PressProperty() {
		return col_Press;
	}
	public StringProperty col_WcurrProperty() {
		return col_Wcurr;
	}
	public StringProperty col_W1minProperty() {
		return col_W1min;
	}
	public StringProperty col_W10minProperty() {
		return col_W10min;
	}
	public StringProperty col_RainProperty() {
		return col_Rain;
	}
	
	public RealTimeRow() {
		col_Time = new SimpleStringProperty();
		col_Temp = new SimpleStringProperty();
		col_Humd = new SimpleStringProperty();
		col_Press = new SimpleStringProperty();
		col_Wcurr = new SimpleStringProperty();
		col_W1min = new SimpleStringProperty();
		col_W10min = new SimpleStringProperty();
		col_Rain = new SimpleStringProperty();
	}
}
