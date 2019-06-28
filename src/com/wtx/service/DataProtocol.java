package com.wtx.service;

import java.util.Arrays;
import java.util.Observable;
import java.util.zip.CRC32;

import com.wtx.model.RealTimeData;

public class DataProtocol extends Observable {
	
	//����ÿ�����ݵ���󳤶�Ϊ76�ֽ�
	private final int MAX_RECORD_LEN = 76;
	private static DataProtocol instance = null;
	private final byte FLAG_START = 'S';		//���ݵ�֡ͷ
	private final byte FLAG_END = '\n';			//���ݵ�֡β
	
	private byte[] dataRecord = new byte[MAX_RECORD_LEN];
	private int position = 0;
	
	/**
	 * ����������������֡������
	 * @param buff
	 * @param len
	 */
	private void analysis(byte[] buff,int len) {
		/**
		 * �ȼ��CRCУ���Ƿ�ƥ��
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
				//CRC32ƥ��
				System.out.println("CRC32 Matched !");
				RealTimeData rtData = new RealTimeData();
				
				//̨վ���
				//String station_id = new String(buff,1,4);
				//System.out.println("̨վ���: " + station_id);
				{
					//�¶�
					String str_val = new String(buff,5,4);
					Double val = Double.valueOf(str_val);
					rtData.setTempreature(val.doubleValue() / 10.0);	//һλС��
					//System.out.println("�¶�: " + rtData.getTempreature());
				}
				{
					//ʪ��
					String str_val = new String(buff,9,3);
					Double val = Double.valueOf(str_val);
					rtData.setHumidity(val.doubleValue() / 10.0);			//һλС��
					//System.out.println("ʪ��: " +rtData.getHumidity());	
				}
				{
					//��ѹ
					String str_val = new String(buff,12,5);
					Double val = Double.valueOf(str_val);
					rtData.setPressure(val.doubleValue() / 10.0);				//һλС��
					//System.out.println("��ѹ: " + rtData.getPressure());
				}
				{
					//˲ʱ��
					String str_dir = new String(buff, 17, 3);
					Short val_dir = Short.valueOf(str_dir);
					rtData.setWind_dir_curr(val_dir.shortValue());
					String str_speed = new String(buff, 20, 3);
					Double val_speed = Double.valueOf(str_speed);
					rtData.setWind_speed_curr(val_speed.doubleValue() / 10.0);	//һλС��
					//System.out.println("˲ʱ��: " + rtData.getWind_dir_curr() +"��\t" + rtData.getWind_speed_curr() + "m/s");
				}
				{
					//һ���ӷ�
					String str_dir = new String(buff, 23, 3);
					Short val_dir = Short.valueOf(str_dir);
					rtData.setWind_dir_1min(val_dir.shortValue());
					String str_speed = new String(buff, 26, 3);
					Double val_speed = Double.valueOf(str_speed);
					rtData.setWind_speed_1min(val_speed.doubleValue() / 10.0);	//һλС��
					//System.out.println("һ���ӷ�: " + rtData.getWind_dir_1min() +"��\t" + rtData.getWind_speed_1min() + "m/s");
				}
				{
					//ʮ���ӷ�
					String str_dir = new String(buff, 29, 3);
					Short val_dir = Short.valueOf(str_dir);
					rtData.setWind_dir_10min(val_dir.shortValue());
					String str_speed = new String(buff, 32, 3);
					Double val_speed = Double.valueOf(str_speed);
					rtData.setWind_speed_10min(val_speed.doubleValue() / 10.0);	//һλС��
					//System.out.println("ʮ���ӷ�: " + rtData.getWind_dir_10min() +"��\t" + rtData.getWind_speed_10min() + "m/s");
				}
				{
					//Сʱ����
					String str_val = new String(buff,35,3);
					Double val = Double.valueOf(str_val);
					rtData.setRain_hour(val.doubleValue() / 10.0);				//һλС��
					//System.out.println("����: " + rtData.getRain_hour());
				}
				{
					//�豸��ѹ
					String str_val = new String(buff,38,3);
					Double val = Double.valueOf(str_val);
					rtData.setVoltage(val.doubleValue() / 10.0);				//һλС��
					//System.out.println("��ѹ: " + rtData.getVoltage());
				}
//				{
//					//ά��
//					String str_val = new String(buff,41,10);
//					rtData.setLatitude(str_val);
//					System.out.println("ά��: " + rtData.getLatitude()); 
//				}
//				{
//					//����
//					String str_val = new String(buff,51,10);
//					rtData.setLongitude(str_val);
//					System.out.println("����: " + rtData.getLongitude());
//				}
				{
					//�ź�ǿ��
					String str_val = new String(buff,62,2);
					Short val = Short.valueOf(str_val);
					rtData.setRssi(val.shortValue());
					//System.out.println("RSSI: " + rtData.getRssi());
				}
				
				//�����¼�֪ͨ
				setChanged();
				notifyObservers(rtData);
				
			}else{
				//CRC32У�鲻ͨ��
				System.err.println("����֡CRC32У����� !");
			}
		} catch (NumberFormatException e) {
			System.err.println("����֡�к��в��ɽ����ַ�!");
		} catch (Exception e) {
			System.err.println("����֡��Ч !");
		}
		
	}
	
	/**
	 * ƴ�Ӳ���������
	 */
	public boolean parse(byte[] buff,int len) {
		try {
			if(position == 0 && (buff[0] == (byte)0x00 || buff[0] == (byte)0x00FF)) {
				//�ų�����
				position = 0;
				Arrays.fill(dataRecord,(byte)0);		//��ս���������
				return false;
			}
			
			if(buff[0] == FLAG_START && len <= MAX_RECORD_LEN && position == 0) {
				System.arraycopy(buff, 0, dataRecord, 0, len);
				position = len;
			}else if(position > 0 && position <= MAX_RECORD_LEN){
				if((position + len) > MAX_RECORD_LEN) {
					System.err.println("���ݳ�����Ч!");
					Arrays.fill(dataRecord,(byte)0);		//��ս���������
					position = 0;
				}else{
					System.arraycopy(buff, 0, dataRecord, position, len);
					position = position + len;
				}
			}
			
			if(dataRecord[0] == FLAG_START && dataRecord[MAX_RECORD_LEN-1] == FLAG_END) {
				System.out.println("��������!");
				/**
				 * �ڽ��յ����������ݿ�ʼ��������
				 */
				analysis(dataRecord,position);
				
				position = 0;
				Arrays.fill(dataRecord,(byte)0);		//��ս���������
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
			System.err.println("���ڻ������쳣!");
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
