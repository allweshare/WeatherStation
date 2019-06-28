package com.wtx.model;

import java.util.Date;

public class MinuteData extends RealTimeData implements Comparable<MinuteData> {
	
	private static final long serialVersionUID = 5L;
	private Date time;
	
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public MinuteData() {
	
	}
	public MinuteData(RealTimeData data) {
		super(data);
	}
	public MinuteData(Date time,RealTimeData data) {
		super(data);
		this.time = time;
	}
	
	@Override
	public int compareTo(MinuteData o) {
		return this.time.compareTo(o.time);
	}
}
