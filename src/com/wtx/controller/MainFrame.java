package com.wtx.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import com.wtx.ResourceFactory;
import com.wtx.ResourceFactory.FXMLResource;
import com.wtx.control.Barometer;
import com.wtx.control.CustomChart;
import com.wtx.control.Hygrometer;
import com.wtx.control.RainIntensity;
import com.wtx.control.RainSumPanel;
import com.wtx.control.Thermograph;
import com.wtx.control.WindPanel;
import com.wtx.dao.DataBaseDao;
import com.wtx.model.CustomDialog;
import com.wtx.model.MinuteData;
import com.wtx.model.RealTimeData;
import com.wtx.model.RealTimeRow;
import com.wtx.service.ComServiceBase;
import com.wtx.service.DataProtocol;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class MainFrame implements IDataBind,Observer{
	
	private final int MAX_TABLE_ROWS = 300;
	private boolean isFullScreen = false;
	
	@FXML
	private GridPane grid_parent;
	
	@FXML
	private TabPane tab_panel;
	
	@FXML
	private TableView<RealTimeRow> table_data;
	
	@FXML
	private Button btn_history;
	
	@FXML
	private Button btn_export;
	
	//通信服务
	private ComServiceBase comService;
	
	//温度图表
	private Thermograph tempGraph;
	
	//湿度图表
	private Hygrometer humdGraph;
	
	//气压图表
	private Barometer pressGraph;
	
	//瞬时风
	private WindPanel wind_curr;
	
	//瞬时风
	private WindPanel wind_1min;
	
	//瞬时风
	private WindPanel wind_10min;
	
	//降水强度
	private RainIntensity rainIntsPane;
	
	//累计雨量
	private RainSumPanel rainSumPane;
	
	private int cache_minute = 90;		//缓存上一分钟
	private Date cache_time = new Date();
	//private SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public MainFrame() {
		//System.out.println("MainFrame Init ......");
		DataProtocol.getParser().addObserver(this);
	}
	
	/**
	 * 接收到实时数据
	 */
	public void update(Observable o, Object data) {
		if(o != null && data != null && data instanceof RealTimeData) {
			System.out.println("Recv Notify !");
			RealTimeData rtData = (RealTimeData)data;
			//存储分钟数据
			Calendar calendar = Calendar.getInstance();
			if(cache_minute != calendar.get(Calendar.MINUTE)) {
				//保存为分钟数据
				MinuteData mdata = new MinuteData(cache_time,rtData);
				DataBaseDao dao = DataBaseDao.getInstance();
				dao.insert_mdata(mdata);
				//System.out.println("insert mdata: " + timeFmt.format(cache_time));
			}
			cache_minute = calendar.get(Calendar.MINUTE);
			cache_time = new Date();
			
			//必须在JavaFX线程内部修改控件的属性
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if(tempGraph != null) {
						tempGraph.update(rtData.getTempreature());
					}
					if(humdGraph != null) {
						humdGraph.update(rtData.getHumidity());
					}
					if(pressGraph != null) {
						pressGraph.update(rtData.getPressure());
					}
					if(wind_curr != null) {
						wind_curr.update(rtData.getWind_speed_curr(), rtData.getWind_dir_curr());
					}
					if(wind_1min != null) {
						wind_1min.update(rtData.getWind_speed_1min(), rtData.getWind_dir_1min());
					}
					if(wind_10min != null) {
						wind_10min.update(rtData.getWind_speed_10min(), rtData.getWind_dir_10min());
					}
					if(rainIntsPane != null) {
						rainIntsPane.update(rtData.getRain_hour());
					}
					
					//添加当前数据到实时数据列表
					RealTimeRow row = RealTimeRow.createFrom(rtData);
					if(row != null) {
						if(table_data.getItems().size() > MAX_TABLE_ROWS) {
							table_data.getItems().remove(table_data.getItems().size()-1);
						}
						table_data.getItems().add(0, row);
					}
					//使主窗口获得焦点
					ResourceFactory.getFactory().getMainFrame().getContainer().requestFocus();
				}
			});
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void bindData() {
		Tab tab_serial = tab_panel.getTabs().get(0);
		FXMLResource<SerialPanel> panel_serial = ResourceFactory.getFactory().getSerialPanel();
		//添加串口调试面板
		if(panel_serial != null && panel_serial.getContainer() != null) {
			tab_serial.setContent(panel_serial.getContainer());
			tab_serial.setOnSelectionChanged(new EventHandler<Event>() {
				public void handle(Event event) {}; {
					if(panel_serial.getController() != null) {
						panel_serial.getController().bindData();
					}
				};
			});
		}
		
		Timeline timeLine = new Timeline();
		timeLine.setCycleCount(Timeline.INDEFINITE);
		timeLine.setAutoReverse(true);
		//设置一个刷新数据的频率
		KeyFrame key1 = new KeyFrame(Duration.seconds(30),new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				DataBaseDao dao = DataBaseDao.getInstance();
				double rainSumVal = dao.rain_sum_day(new Date());
				rainSumPane.update(rainSumVal);
			}
		});
		timeLine.getKeyFrames().add(key1);
		timeLine.play();
		
		//添加温度图表
		if(grid_parent != null) {
			//----------------------------------------------------
			//添加温度图表
			tempGraph = new Thermograph();
			GridPane.setMargin(tempGraph, new Insets(5, 5, 5, 5));
			grid_parent.add(tempGraph, 0, 0);
			//----------------------------------------------------
			//添加湿度图表
			humdGraph = new Hygrometer();
			GridPane.setMargin(humdGraph, new Insets(5, 5, 5, 5));
			grid_parent.add(humdGraph, 1, 0);
			//----------------------------------------------------
			//添加气压图表
			pressGraph = new Barometer();
			GridPane.setMargin(pressGraph, new Insets(5, 5, 5, 5));
			grid_parent.add(pressGraph, 2, 0);
			//----------------------------------------------------
			//添加瞬时风图表
			wind_curr = new WindPanel("瞬时风");
			GridPane.setMargin(wind_curr, new Insets(5, 5, 5, 5));
			grid_parent.add(wind_curr, 0, 1);
			//----------------------------------------------------
			//添加一分钟风图表
			wind_1min = new WindPanel("一分钟风");
			GridPane.setMargin(wind_1min, new Insets(5, 5, 5, 5));
			grid_parent.add(wind_1min, 1, 1);
			//----------------------------------------------------
			//添加十分钟风图表
			wind_10min = new WindPanel("十分钟风");
			GridPane.setMargin(wind_10min, new Insets(5, 5, 5, 5));
			grid_parent.add(wind_10min, 2, 1);
			//----------------------------------------------------
			//添加小时雨量(雨强)
			rainIntsPane = new RainIntensity();
			GridPane.setMargin(rainIntsPane, new Insets(5, 5, 5, 5));
			grid_parent.add(rainIntsPane, 4, 1);
			//----------------------------------------------------
			//添加日累计雨量
			rainSumPane = new RainSumPanel();
			GridPane.setMargin(rainSumPane, new Insets(5, 5, 5, 5));
			grid_parent.add(rainSumPane, 5, 1);
			//----------------------------------------------------
			//添加自定义曲线图和降水柱状图
			CustomChart customChart = null;
			try {
				customChart = new CustomChart(24);
			} catch (Exception e) {
				e.printStackTrace();
			}
			GridPane.setColumnSpan(customChart, 3);
			GridPane.setMargin(customChart, new Insets(5, 5, 5, 5));
			grid_parent.add(customChart, 4, 0);
			customChart.bindDragEvent();
			//----------------------------------------------------
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
		}
		
		//----------------------------------------------------------
		//绑定快捷键
		Scene scene = ResourceFactory.getFactory().getMainFrame().getContainer().getScene();
		if(scene != null) {
			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if(event.getCode() == KeyCode.F5) {		//浏览数据
						Event.fireEvent(btn_history, new ActionEvent());
					}
					if(event.getCode() == KeyCode.F6) {		//导出数据
						Event.fireEvent(btn_export, new ActionEvent());
					}
					if(event.getCode() == KeyCode.F7) {		//生成报表
						
					}
					if(event.getCode() == KeyCode.F8) {		//退出
						Window parent = ResourceFactory.getFactory().getMainFrame().getContainer().getScene().getWindow();
						if(parent != null && parent instanceof Stage) {
							Stage primaryStage = (Stage)parent;
							Event.fireEvent(primaryStage, new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
						}
					}
					if(event.getCode() == KeyCode.F11) {	//全屏
						Window parent = ResourceFactory.getFactory().getMainFrame().getContainer().getScene().getWindow();
						if(parent != null && parent instanceof Stage) {
							Stage primaryStage = (Stage)parent;
							if(isFullScreen == false) {
								primaryStage.setFullScreen(true);
								isFullScreen = true;
							}else {
								primaryStage.setFullScreen(false);
								isFullScreen = false;
							}
						}
					}
				}
			});
		}
	}
	
	@FXML
	public void on_btn_exit() {
		Window parent = ResourceFactory.getFactory().getMainFrame().getContainer().getScene().getWindow();
		if(parent != null && parent instanceof Stage) {
			Stage primaryStage = (Stage)parent;
			Event.fireEvent(primaryStage, new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
		}
	}
	
	/**
	 * 生成报表
	 */
	@FXML
	public void on_btn_report() {
		
	}
	
	/**
	 * 导出数据
	 */
	@FXML
	public void on_btn_export() {
		FXMLResource<DataExport> dlgDataExport = ResourceFactory.getFactory().getDataExport();
		CustomDialog dialog = new CustomDialog(Modality.APPLICATION_MODAL, dlgDataExport);
		dialog.show();
	}
	
	/**
	 * 历史数据
	 */
	@FXML
	public void on_btn_history() {
		FXMLResource<DataExplorer> dlgDataExplorer = ResourceFactory.getFactory().getDataExplorer(); 
		CustomDialog dialog = new CustomDialog(Modality.APPLICATION_MODAL, dlgDataExplorer);
		dialog.show();
	}
	
	public ComServiceBase getComService() {
		return comService;
	}

	public void setComService(ComServiceBase comService) {
		this.comService = comService;
	}
}
