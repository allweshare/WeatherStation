package com.wtx.model;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class MinuteRow extends RealTimeRow {
	private static SimpleDateFormat timeDispFmt = new SimpleDateFormat("HH:mm");
	private static DecimalFormat numFmt = new DecimalFormat("0.0");
	
	public MinuteRow() {
	}
	
	public static MinuteRow createFrom(MinuteData data,String defaultTime) {
		try {
			if(data != null) {
				MinuteRow row = new MinuteRow();
				row.col_Time.setValue(timeDispFmt.format(data.getTime()));
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
			}else {
				//如果当前数据缺测
				MinuteRow row = new MinuteRow();
				row.col_Time.setValue(defaultTime.substring(11, 16));
				row.col_Temp.setValue("----");
				row.col_Humd.setValue("----");
				row.col_Press.setValue("----");
				row.col_Wcurr.setValue("----");
				row.col_W1min.setValue("----");
				row.col_W10min.setValue("----");
				row.col_Rain.setValue("----");
				return row;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
