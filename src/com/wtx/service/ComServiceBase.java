package com.wtx.service;

public abstract class ComServiceBase {
	
	//���ͨ����·�Ƿ��Ѿ���
	protected boolean isopen = false;
	
	public ComServiceBase() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * ��ͨ����·
	 * @param args: ͨ�Ų���
	 */
	public abstract boolean open();
	
	/**
	 * ��������
	 */
	public abstract void send(byte[] buffer);
	
	/**
	 * �ر�ͨ����·
	 * @param args
	 */
	public abstract void close();
	
	/**
	 * ��Ǵ����Ƿ��Ѿ���
	 * @return
	 */
	public boolean isOpen() {
		return isopen;
	}
}
