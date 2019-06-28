package com.wtx.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import com.wtx.control.CustomChart;
import com.wtx.dao.DataBaseDao;
import com.wtx.model.MinuteData;
import com.wtx.model.MinuteRow;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class DataExplorer implements IDataBind{
	
	@FXML
	private DatePicker datePick;
	
	@FXML
	private Slider slidTime;
	
	private double initX;
    private double initY;
    
    //设置底部自定义图表
    private CustomChart chart = null;
    
    //当前查询到的结果集
    private Map<String,MinuteData> dataMap = new TreeMap<>();
    
    private SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
	@FXML
	private BorderPane dlg_explorer;
	
	@FXML
	private TableView<MinuteRow> table_data;
	
	public DataExplorer() {
		
	}
	
	@FXML
	public void onDateChange() {
		DataBaseDao dao = DataBaseDao.getInstance();
		Calendar cal_start = Calendar.getInstance();
		cal_start.set(Calendar.YEAR, datePick.getValue().getYear());
		cal_start.set(Calendar.MONTH,datePick.getValue().getMonthValue()-1);
		cal_start.set(Calendar.DAY_OF_MONTH, datePick.getValue().getDayOfMonth());
		cal_start.set(Calendar.HOUR_OF_DAY, 0);
		cal_start.set(Calendar.MINUTE, 0);
		cal_start.set(Calendar.SECOND, 0);
		Calendar cal_end = Calendar.getInstance();
		cal_end.setTime(cal_start.getTime());
		cal_end.add(Calendar.HOUR_OF_DAY, 24);
		//加载指定时间段的数据
		try {
			dataMap.clear();
			//固定缓存24小时 x 60分钟的数据
			Calendar cal_curr = Calendar.getInstance();
			cal_curr.setTime(cal_start.getTime());
			for(int i = 0;i<24 * 60;i++) {
				dataMap.put(timeFmt.format(cal_curr.getTime()), null);
				cal_curr.add(Calendar.MINUTE, 1);
			}
			
//			System.out.println("Load Data: ");
//			System.out.println(cal_start.getTime());
//			System.out.println(cal_end.getTime());
			
			Set<MinuteData> dataset = dao.load_mdata(cal_start.getTime(), cal_end.getTime());
			Iterator<MinuteData> dataItert = dataset.iterator();
			while(dataItert.hasNext()) {
				MinuteData mdata = dataItert.next();
				dataMap.put(timeFmt.format(mdata.getTime()), mdata);
			}
			//-----------------------------------------------------------------
			dlg_explorer.setBottom(null);
			cal_start.set(Calendar.YEAR, datePick.getValue().getYear());
			cal_start.set(Calendar.MONTH, datePick.getValue().getMonthValue()-1);
			cal_start.set(Calendar.DAY_OF_MONTH, datePick.getValue().getDayOfMonth());
			cal_start.set(Calendar.HOUR_OF_DAY, 0);
			cal_start.set(Calendar.MINUTE,0);
			cal_start.set(Calendar.SECOND,0);
			cal_end.setTime(cal_start.getTime());
			cal_end.add(Calendar.HOUR_OF_DAY, 24);
			try {
				chart = new CustomChart(cal_start.getTime(),cal_end.getTime());
				table_data.setPrefHeight(400);
				chart.setPrefHeight(250);
				chart.setStyle("-fx-border-color:white;-fx-border-style:dotted;");
				BorderPane.setMargin(chart, new Insets(5, 5, 5, 5));
				dlg_explorer.setBottom(chart);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			/**
			 * 显示指定小时内的数据
			 */
			dispHourData(0);
			
			slidTime.setValue(0.0);
			
			//-----------------------------------------------------------------
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *	显示指定小时的数据 
	 * @param hour
	 */
	private void dispHourData(int hour) {
		Calendar cal_start = Calendar.getInstance();
		cal_start.set(Calendar.YEAR, datePick.getValue().getYear());
		cal_start.set(Calendar.MONTH, datePick.getValue().getMonthValue()-1);
		cal_start.set(Calendar.DAY_OF_MONTH, datePick.getValue().getDayOfMonth());
		cal_start.set(Calendar.HOUR_OF_DAY, 0);
		cal_start.set(Calendar.MINUTE, 0);
		cal_start.set(Calendar.SECOND, 0);
		Calendar cal_end = Calendar.getInstance();
		cal_end.setTime(cal_start.getTime());
		cal_end.add(Calendar.HOUR_OF_DAY, 24);
		
		Calendar cal_curr = Calendar.getInstance();
		cal_curr.setTime(cal_start.getTime());
		cal_curr.add(Calendar.MINUTE, hour * 60);
		table_data.getItems().clear();
		for(int i=0;i<60;i++) {
			MinuteData mdata = dataMap.get(timeFmt.format(cal_curr.getTime()));
			MinuteRow mrow = MinuteRow.createFrom(mdata,timeFmt.format(cal_curr.getTime()));
			table_data.getItems().add(mrow);
			cal_curr.add(Calendar.MINUTE, 1);
		}
		
	}
	
	@FXML
	public void onMouseDragged(MouseEvent me) {
		Window parent = dlg_explorer.getScene().getWindow();
		if(parent != null && parent instanceof Stage) {
			Stage dialogStage = (Stage)parent;
			dialogStage.setX(me.getScreenX() - initX);
			dialogStage.setY(me.getScreenY() - initY);
		}
	}
	
	@FXML
	public void onMousePressed(MouseEvent me) {
		Window parent = dlg_explorer.getScene().getWindow();
		if(parent != null && parent instanceof Stage) {
			Stage dialogStage = (Stage)parent;
			initX = me.getScreenX() - dialogStage.getX();
	        initY = me.getScreenY() - dialogStage.getY();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void bindData() {
		System.out.println("DataExplorer databind ...");
		Calendar cal_start = Calendar.getInstance();
		cal_start.setTime(new Date());
		cal_start.set(Calendar.HOUR_OF_DAY, 0);
		cal_start.set(Calendar.MINUTE,0);
		cal_start.set(Calendar.SECOND,0);
		Calendar cal_end = Calendar.getInstance();
		cal_end.setTime(cal_start.getTime());
		cal_end.add(Calendar.HOUR_OF_DAY, 24);
		try {
			chart = new CustomChart(cal_start.getTime(),cal_end.getTime());
			table_data.setPrefHeight(400);
			chart.setPrefHeight(250);
			chart.setStyle("-fx-border-color:white;-fx-border-style:dotted;");
			BorderPane.setMargin(chart, new Insets(5, 5, 5, 5));
			dlg_explorer.setBottom(chart);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(table_data != null) {
			table_data.getColumns().get(0).setCellValueFactory(new PropertyValueFactory("col_Time"));
			table_data.getColumns().get(1).setCellValueFactory(new PropertyValueFactory("col_Temp"));
			table_data.getColumns().get(2).setCellValueFactory(new PropertyValueFactory("col_Humd"));
			table_data.getColumns().get(3).setCellValueFactory(new PropertyValueFactory("col_Press"));
			table_data.getColumns().get(4).setCellValueFactory(new PropertyValueFactory("col_Wcurr"));
			table_data.getColumns().get(5).setCellValueFactory(new PropertyValueFactory("col_W1min"));
			table_data.getColumns().get(6).setCellValueFactory(new PropertyValueFactory("col_W10min"));
			table_data.getColumns().get(7).setCellValueFactory(new PropertyValueFactory("col_Rain"));
		}
		
		//滑动动态切换显示页
		slidTime.valueProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				if(datePick.getValue() != null) {
					dispHourData((int)(slidTime.getValue()));
				}
			}
		});
		
	}
	
	@FXML
	public void on_btn_cancel() {
		Window parent = dlg_explorer.getScene().getWindow();
		if(parent != null && parent instanceof Stage) {
			Stage dialogStage = (Stage)parent;
			Event.fireEvent(dialogStage, new WindowEvent(dialogStage, WindowEvent.WINDOW_CLOSE_REQUEST));
		}
	}
}
