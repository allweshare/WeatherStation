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
	
	//ͨ�ŷ���
	private ComServiceBase comService;
	
	//�¶�ͼ��
	private Thermograph tempGraph;
	
	//ʪ��ͼ��
	private Hygrometer humdGraph;
	
	//��ѹͼ��
	private Barometer pressGraph;
	
	//˲ʱ��
	private WindPanel wind_curr;
	
	//˲ʱ��
	private WindPanel wind_1min;
	
	//˲ʱ��
	private WindPanel wind_10min;
	
	//��ˮǿ��
	private RainIntensity rainIntsPane;
	
	//�ۼ�����
	private RainSumPanel rainSumPane;
	
	private int cache_minute = 90;		//������һ����
	private Date cache_time = new Date();
	//private SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public MainFrame() {
		//System.out.println("MainFrame Init ......");
		DataProtocol.getParser().addObserver(this);
	}
	
	/**
	 * ���յ�ʵʱ����
	 */
	public void update(Observable o, Object data) {
		if(o != null && data != null && data instanceof RealTimeData) {
			System.out.println("Recv Notify !");
			RealTimeData rtData = (RealTimeData)data;
			//�洢��������
			Calendar calendar = Calendar.getInstance();
			if(cache_minute != calendar.get(Calendar.MINUTE)) {
				//����Ϊ��������
				MinuteData mdata = new MinuteData(cache_time,rtData);
				DataBaseDao dao = DataBaseDao.getInstance();
				dao.insert_mdata(mdata);
				//System.out.println("insert mdata: " + timeFmt.format(cache_time));
			}
			cache_minute = calendar.get(Calendar.MINUTE);
			cache_time = new Date();
			
			//������JavaFX�߳��ڲ��޸Ŀؼ�������
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
					
					//��ӵ�ǰ���ݵ�ʵʱ�����б�
					RealTimeRow row = RealTimeRow.createFrom(rtData);
					if(row != null) {
						if(table_data.getItems().size() > MAX_TABLE_ROWS) {
							table_data.getItems().remove(table_data.getItems().size()-1);
						}
						table_data.getItems().add(0, row);
					}
					//ʹ�����ڻ�ý���
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
		//��Ӵ��ڵ������
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
		//����һ��ˢ�����ݵ�Ƶ��
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
		
		//����¶�ͼ��
		if(grid_parent != null) {
			//----------------------------------------------------
			//����¶�ͼ��
			tempGraph = new Thermograph();
			GridPane.setMargin(tempGraph, new Insets(5, 5, 5, 5));
			grid_parent.add(tempGraph, 0, 0);
			//----------------------------------------------------
			//���ʪ��ͼ��
			humdGraph = new Hygrometer();
			GridPane.setMargin(humdGraph, new Insets(5, 5, 5, 5));
			grid_parent.add(humdGraph, 1, 0);
			//----------------------------------------------------
			//�����ѹͼ��
			pressGraph = new Barometer();
			GridPane.setMargin(pressGraph, new Insets(5, 5, 5, 5));
			grid_parent.add(pressGraph, 2, 0);
			//----------------------------------------------------
			//���˲ʱ��ͼ��
			wind_curr = new WindPanel("˲ʱ��");
			GridPane.setMargin(wind_curr, new Insets(5, 5, 5, 5));
			grid_parent.add(wind_curr, 0, 1);
			//----------------------------------------------------
			//���һ���ӷ�ͼ��
			wind_1min = new WindPanel("һ���ӷ�");
			GridPane.setMargin(wind_1min, new Insets(5, 5, 5, 5));
			grid_parent.add(wind_1min, 1, 1);
			//----------------------------------------------------
			//���ʮ���ӷ�ͼ��
			wind_10min = new WindPanel("ʮ���ӷ�");
			GridPane.setMargin(wind_10min, new Insets(5, 5, 5, 5));
			grid_parent.add(wind_10min, 2, 1);
			//----------------------------------------------------
			//���Сʱ����(��ǿ)
			rainIntsPane = new RainIntensity();
			GridPane.setMargin(rainIntsPane, new Insets(5, 5, 5, 5));
			grid_parent.add(rainIntsPane, 4, 1);
			//----------------------------------------------------
			//������ۼ�����
			rainSumPane = new RainSumPanel();
			GridPane.setMargin(rainSumPane, new Insets(5, 5, 5, 5));
			grid_parent.add(rainSumPane, 5, 1);
			//----------------------------------------------------
			//����Զ�������ͼ�ͽ�ˮ��״ͼ
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
		//�󶨿�ݼ�
		Scene scene = ResourceFactory.getFactory().getMainFrame().getContainer().getScene();
		if(scene != null) {
			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if(event.getCode() == KeyCode.F5) {		//�������
						Event.fireEvent(btn_history, new ActionEvent());
					}
					if(event.getCode() == KeyCode.F6) {		//��������
						Event.fireEvent(btn_export, new ActionEvent());
					}
					if(event.getCode() == KeyCode.F7) {		//���ɱ���
						
					}
					if(event.getCode() == KeyCode.F8) {		//�˳�
						Window parent = ResourceFactory.getFactory().getMainFrame().getContainer().getScene().getWindow();
						if(parent != null && parent instanceof Stage) {
							Stage primaryStage = (Stage)parent;
							Event.fireEvent(primaryStage, new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
						}
					}
					if(event.getCode() == KeyCode.F11) {	//ȫ��
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
	 * ���ɱ���
	 */
	@FXML
	public void on_btn_report() {
		
	}
	
	/**
	 * ��������
	 */
	@FXML
	public void on_btn_export() {
		FXMLResource<DataExport> dlgDataExport = ResourceFactory.getFactory().getDataExport();
		CustomDialog dialog = new CustomDialog(Modality.APPLICATION_MODAL, dlgDataExport);
		dialog.show();
	}
	
	/**
	 * ��ʷ����
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
