package com.wtx.service;

public abstract class ComServiceBase {
	
	//标记通信链路是否已经打开
	protected boolean isopen = false;
	
	public ComServiceBase() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 打开通信链路
	 * @param args: 通信参数
	 */
	public abstract boolean open();
	
	/**
	 * 发送数据
	 */
	public abstract void send(byte[] buffer);
	
	/**
	 * 关闭通信链路
	 * @param args
	 */
	public abstract void close();
	
	/**
	 * 标记串口是否已经打开
	 * @return
	 */
	public boolean isOpen() {
		return isopen;
	}
}
