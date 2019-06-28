package com.wtx.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Set;
import java.util.SortedSet;
import java.util.TooManyListenersException;
import java.util.TreeSet;
import com.wtx.FactoryPreset;
import com.wtx.ResourceFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class SerialComService extends ComServiceBase implements SerialPortEventListener {
	
	private SerialPort serialPort = null;
	private InputStream inputStream;
	private OutputStream outputStream;	
	private final int RECV_BUFF_LEN = 1024;
	private byte[] dataBuffer = new byte[RECV_BUFF_LEN];
	
	public SerialComService() {
		System.out.println("SerialComService ...");
	}
	
	public static Set<String> getPortList() {
		SortedSet<String> sortedList = new TreeSet<String>();
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portidentifiers = CommPortIdentifier.getPortIdentifiers();
		while(portidentifiers.hasMoreElements()){
			sortedList.add(portidentifiers.nextElement().getName());
		}
		return sortedList;
	}
	
	@Override
	public boolean open() {
		String comPort = FactoryPreset.getPreset().readValueByKey(FactoryPreset.COMMPORT);
		try {
			CommPortIdentifier portdendifier = CommPortIdentifier.getPortIdentifier(comPort);
			if(portdendifier.isCurrentlyOwned()){
				System.err.println("Port " + comPort + " already in used !");
			}else{
				CommPort commPort = portdendifier.open("App", 2000);
				int baudRate = Integer.parseInt(FactoryPreset.getPreset().readValueByKey(FactoryPreset.BAUDRATE));
				int dataBits = Integer.parseInt(FactoryPreset.getPreset().readValueByKey(FactoryPreset.DATABITS));
				int parityBit = SerialPort.PARITY_NONE;
				if(FactoryPreset.getPreset().readValueByKey(FactoryPreset.PARITY).equals("None")){
					parityBit = SerialPort.PARITY_NONE;
				}
				if(FactoryPreset.getPreset().readValueByKey(FactoryPreset.PARITY).equals("Odd")){
					parityBit = SerialPort.PARITY_ODD;
				}
				if(FactoryPreset.getPreset().readValueByKey(FactoryPreset.PARITY).equals("Even")){
					parityBit = SerialPort.PARITY_EVEN;
				}
				int stopBits = SerialPort.STOPBITS_1;
				if(FactoryPreset.getPreset().readValueByKey(FactoryPreset.STOPBITS).equals("1")){
					stopBits = SerialPort.STOPBITS_1;
				}
				if(FactoryPreset.getPreset().readValueByKey(FactoryPreset.STOPBITS).equals("1.5")){
					stopBits = SerialPort.STOPBITS_1_5;
				}
				if(FactoryPreset.getPreset().readValueByKey(FactoryPreset.STOPBITS).equals("2")){
					stopBits = SerialPort.STOPBITS_2;
				}
				
				if(commPort instanceof SerialPort){
					serialPort = (SerialPort)commPort;
					serialPort.removeEventListener();
					serialPort.addEventListener(this);
					serialPort.notifyOnDataAvailable(true);
					serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parityBit);
					serialPort.setInputBufferSize(1024);		//适当的设置输入输出缓冲区大小
					serialPort.setOutputBufferSize(1024);
					inputStream = serialPort.getInputStream();
					outputStream = serialPort.getOutputStream();
					
					System.out.println("串口: " + comPort + " 已开启!");
					
				}
			}
			//标记为串口已经打开
			isopen = true;
		} catch (PortInUseException e) {
			System.err.println("端口"+comPort+"被占用: "+e.currentOwner+"!");
		} catch (NoSuchPortException e) {
			System.err.println("没有找到指定的端口: " + comPort + " !");
			e.printStackTrace();
		} catch (TooManyListenersException e) {
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isopen;
	}
	
	@Override
	public void send(byte[] buffer) {
		if(serialPort != null && isopen){
			if(outputStream != null){
				try {
					outputStream.write(buffer);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub
		isopen = false;
		if(inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(serialPort != null){
			try {
				//serialPort.notifyOnDataAvailable(false);
				serialPort.removeEventListener();
				serialPort.close();
				System.out.println("通信串口已关闭!");
				serialPort = null;
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void serialEvent(SerialPortEvent serialEvent) {
		try {
			switch (serialEvent.getEventType()) {
			case SerialPortEvent.BI:
			case SerialPortEvent.OE:
			case SerialPortEvent.FE:
			case SerialPortEvent.PE:
			case SerialPortEvent.CD:
			case SerialPortEvent.CTS:
			case SerialPortEvent.DSR:
			case SerialPortEvent.RI:
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				break;
			case SerialPortEvent.DATA_AVAILABLE:		// 获取到串口返回信息
				if(inputStream != null && inputStream.available() > 0){
					int currRead = inputStream.read(dataBuffer);
					System.out.println("Read Len: " + currRead);
					for(int i = 0;i<currRead;i ++) {
						if(Integer.toHexString((int)dataBuffer[i] & 0x000000FF).length() == 1) {
							System.out.print("0"+Integer.toHexString((int)dataBuffer[i] & 0x000000FF) + " ");
						}else {
							System.out.print(Integer.toHexString((int)dataBuffer[i] & 0x000000FF) + " ");
						}
					}
					System.out.println();
					if(currRead > 1) {
						//-------------------------------------------------
						/**
						 * 将接收到的内容添加到串口调试面板
						 */
						ResourceFactory.getFactory().getSerialPanel().getController().appendRecv(new String(dataBuffer, 0, currRead));
						//-------------------------------------------------
						/**
						 * 开始拼接并解析数据
						 */
						DataProtocol.getParser().parse(dataBuffer, currRead);
					}
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
