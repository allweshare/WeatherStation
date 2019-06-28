package com.wtx.dao;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.wtx.controller.DataExport;
import com.wtx.model.MinuteData;

import javafx.concurrent.Task;

public class DataExportTask extends Task<Integer> {
	
	private DataExport exporter;
	
	private SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public DataExportTask(DataExport exporter) {
		this.exporter = exporter;
	}
	
	@Override
	protected Integer call() throws Exception {
		
		String str_time_start = exporter.getStartDate().toString() + " 00:00:00";
		String str_time_end = exporter.getEndDate().plusDays(1).toString() + " 00:00:00";		//����ʱ������Ƴ�һ��
		
		try {
			Date time_start = timeFmt.parse(str_time_start);
			Date time_end = timeFmt.parse(str_time_end);
			DataBaseDao dao = DataBaseDao.getInstance();
			//Ԥ�����յ����ݼ�,ʹȱ�������Ҳ�ܱ�����
			TreeMap<String, MinuteData> dataMap = new TreeMap<>();
			Calendar cal_curr = Calendar.getInstance();
			cal_curr.setTime(time_start);
			while(cal_curr.getTime().before(time_end)) {
				dataMap.put(timeFmt.format(cal_curr.getTime()), null);
				//System.out.println("Export: " + timeFmt.format(cal_curr.getTime()));
				cal_curr.add(Calendar.MINUTE, 1);
			}
			//-------------------------------------------------
			Set<MinuteData> dataSet = dao.load_mdata(time_start, time_end);
			Iterator<MinuteData> dataItert = dataSet.iterator();
			while(dataItert.hasNext()) {
				MinuteData mdata = dataItert.next();
				dataMap.put(timeFmt.format(mdata.getTime()), mdata);
			}
			//-------------------------------------------------
			DecimalFormat fmt_float = new DecimalFormat("#0.0");
			DecimalFormat fmt_int = new DecimalFormat("###");
			Iterator<Entry<String, MinuteData>> mdataItert = dataMap.entrySet().iterator();
			while(mdataItert.hasNext()) {
				Entry<String, MinuteData> entry = mdataItert.next();
				String filePath = exporter.getExportPath() + entry.getKey().substring(0,10) +exporter.getExtension(); 
				
				FileOutputStream outStream = new FileOutputStream(filePath, true);
				OutputStreamWriter outWriter = new OutputStreamWriter(outStream);
				BufferedWriter buffWriter = new BufferedWriter(outWriter);
				
				StringBuffer line = new StringBuffer();
				if(entry.getValue() != null) {
					//ʱ��
					line.append(entry.getKey().substring(11,19)+"\t");
					//�¶�
					line.append(fmt_float.format(entry.getValue().getTempreature()) + "\t");
					//ʪ��
					line.append(fmt_float.format(entry.getValue().getHumidity()) + "\t");
					//��ѹ
					line.append(fmt_float.format(entry.getValue().getPressure()) + "\t");
					//˲ʱ��
					line.append(fmt_float.format(entry.getValue().getWind_speed_curr()) + "\t");
					line.append(fmt_int.format(entry.getValue().getWind_dir_curr()) + "\t");
					//һ���ӷ�
					line.append(fmt_float.format(entry.getValue().getWind_speed_1min()) + "\t");
					line.append(fmt_int.format(entry.getValue().getWind_dir_1min()) + "\t");
					//ʮ���ӷ�
					line.append(fmt_float.format(entry.getValue().getWind_speed_10min()) + "\t");
					line.append(fmt_int.format(entry.getValue().getWind_dir_10min()) + "\t");
					//��ǿ
					line.append(fmt_float.format(entry.getValue().getRain_hour()));
				}else {
					//ʱ��
					line.append(entry.getKey().substring(11,19)+"\t");
					//�¶�
					line.append("----\t");
					//ʪ��
					line.append("----\t");
					//��ѹ
					line.append("----\t");
					//˲ʱ��
					line.append("----\t");
					line.append("----\t");
					//һ���ӷ�
					line.append("----\t");
					line.append("----\t");
					//ʮ���ӷ�
					line.append("----\t");
					line.append("----\t");
					//��ǿ
					line.append("----");
				}
				
				buffWriter.write(line.toString());
				buffWriter.newLine();
				
				buffWriter.close();
				outWriter.close();
				outStream.close();
				//-----------------------------------------------------
				//���µ�������
				long diff_milsec = timeFmt.parse(entry.getKey()).getTime() - time_start.getTime();
				long total_milsec = time_end.getTime() - time_start.getTime();
				updateProgress(diff_milsec, total_milsec);
			}
		} catch (ParseException e) {
			System.err.println("�������ݵ����ڴ���!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
//		for(int i = 0;i<10;i++) {
//			
//			updateProgress(i, 9);
//			
//			Thread.sleep(200);
//		}
		
		return null;
	}
	
	@Override
	protected void succeeded() {
		super.succeeded();
		if(exporter != null) {
			exporter.exportFinished();
		}
		System.out.println("�����������!");
		System.gc();
	}
	
	@Override
	protected void updateProgress(double workDone, double max) {
		super.updateProgress(workDone, max);
	}
	
}
