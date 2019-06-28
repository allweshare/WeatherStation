package com.wtx.model;

import java.io.Serializable;

public class RealTimeData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private double tempreature;
	private double humidity;
	private double pressure;
	private short wind_dir_curr;
	private double wind_speed_curr;
	private short wind_dir_1min;
	private double wind_speed_1min;
	private short wind_dir_10min;
	private double wind_speed_10min;
	private double rain_hour;
	private double voltage;
	private String longitude;	//¾­¶È
	private String latitude;	//Î¬¶È
	private short rssi;
	
	public double getTempreature() {
		return tempreature;
	}

	public void setTempreature(double tempreature) {
		this.tempreature = tempreature;
	}

	public double getHumidity() {
		return humidity;
	}

	public void setHumidity(double humidity) {
		this.humidity = humidity;
	}

	public double getPressure() {
		return pressure;
	}

	public void setPressure(double pressure) {
		this.pressure = pressure;
	}

	public short getWind_dir_curr() {
		return wind_dir_curr;
	}

	public void setWind_dir_curr(short wind_dir_curr) {
		this.wind_dir_curr = wind_dir_curr;
	}

	public double getWind_speed_curr() {
		return wind_speed_curr;
	}

	public void setWind_speed_curr(double wind_speed_curr) {
		this.wind_speed_curr = wind_speed_curr;
	}

	public short getWind_dir_1min() {
		return wind_dir_1min;
	}

	public void setWind_dir_1min(short wind_dir_1min) {
		this.wind_dir_1min = wind_dir_1min;
	}

	public double getWind_speed_1min() {
		return wind_speed_1min;
	}

	public void setWind_speed_1min(double wind_speed_1min) {
		this.wind_speed_1min = wind_speed_1min;
	}

	public short getWind_dir_10min() {
		return wind_dir_10min;
	}

	public void setWind_dir_10min(short wind_dir_10min) {
		this.wind_dir_10min = wind_dir_10min;
	}

	public double getWind_speed_10min() {
		return wind_speed_10min;
	}

	public void setWind_speed_10min(double wind_speed_10min) {
		this.wind_speed_10min = wind_speed_10min;
	}

	public double getRain_hour() {
		return rain_hour;
	}

	public void setRain_hour(double rain_hour) {
		this.rain_hour = rain_hour;
	}

	public double getVoltage() {
		return voltage;
	}

	public void setVoltage(double voltage) {
		this.voltage = voltage;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public short getRssi() {
		return rssi;
	}

	public void setRssi(short rssi) {
		this.rssi = rssi;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public RealTimeData() {
	}
	
	public RealTimeData(RealTimeData data) {
		this.tempreature = data.tempreature;
		this.humidity = data.humidity;
		this.pressure = data.pressure;
		this.wind_dir_curr = data.wind_dir_curr;;
		this.wind_speed_curr = data.wind_speed_curr;;
		this.wind_dir_1min = data.wind_dir_1min;;
		this.wind_speed_1min = data.wind_speed_1min;
		this.wind_dir_10min = data.wind_dir_10min;
		this.wind_speed_10min = data.wind_speed_10min;
		this.rain_hour = data.rain_hour;
		this.voltage = data.voltage;
		this.longitude = data.longitude;
		this.latitude = data.latitude;
		this.rssi = data.rssi;
	}
	
	
}
