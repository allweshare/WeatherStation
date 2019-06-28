package com.wtx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class FactoryPreset {
	
	public static final String COMMPORT = "COMMPORT";
	public static final String BAUDRATE = "BAUDRATE";
	public static final String DATABITS = "DATABITS";
	public static final String PARITY = "PARITY";
	public static final String STOPBITS = "STOPBITS";
	
	//默认将配置文件存放在运行程序同级目录下
	private static FactoryPreset preset = null;
	private final String CONFIG_FILE_PATH = "config.properties";
	private File configFile = null;
	private Properties appCfgProps = null;
	
	private FactoryPreset() {
		System.out.println("FactoryPreset init ......");
		
		configFile = new File(CONFIG_FILE_PATH);
		appCfgProps = new Properties();
		
		if(!configFile.exists()){
			try {
				configFile.createNewFile();
				writeKeyValue(COMMPORT, "COM1");
				writeKeyValue(BAUDRATE, "9600");
				writeKeyValue(DATABITS, "8");
				writeKeyValue(PARITY, "NONE");
				writeKeyValue(STOPBITS, "1");
			} catch (IOException e) {
				System.err.println("不能创建配置文件,检查目录......");
				e.printStackTrace();
			}
		}
		
		try {
			if(configFile.isFile()){
				appCfgProps.load(new FileInputStream(configFile));
				System.out.println("已经加载配置文件!");
			}else{
				System.err.println("无法加载配置文件: config.properties !");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String readValueByKey(String key){
		return appCfgProps.getProperty(key);
	}
	
	public void writeKeyValue(String key,String value){
		try {
			OutputStream fos = new FileOutputStream(configFile);
			appCfgProps.setProperty(key, value);
			appCfgProps.store(fos, "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static FactoryPreset getPreset(){
		if(preset == null){
			synchronized (FactoryPreset.class) {
				if(preset == null) {
					preset = new FactoryPreset();
				}
			}
		}
		return preset;
	}
	
	
}
