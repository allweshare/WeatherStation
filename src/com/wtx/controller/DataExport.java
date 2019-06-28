package com.wtx.controller;

import java.io.File;
import java.time.LocalDate;
import java.util.Properties;
import com.wtx.dao.DataExportTask;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class DataExport implements IDataBind {
	
	@FXML
	private VBox dlg_export;
	
	@FXML
	private DatePicker picker_start;
	
	@FXML
	private DatePicker picker_end;
	
	@FXML
	private RadioButton rdb_excel;
	
	@FXML
	private RadioButton rdb_txt;
	
	@FXML
	private TextField txt_path;
	
	@FXML
	private Label lab_curr_record;
	
	@FXML
	private ProgressBar progress_export;
	
	@FXML
	private Label lab_err_msg;
	
	@FXML
	private Button btn_export;
	
	@FXML
	private Button btn_cancel;
	
	private double initX;
    private double initY;
	
	@FXML
	private void on_btn_cancel() {
		if(dlg_export.getScene().getWindow() instanceof Stage) {
			Stage dlgStage = (Stage)dlg_export.getScene().getWindow();
			Event.fireEvent(dlgStage, new WindowEvent(dlgStage, WindowEvent.WINDOW_CLOSE_REQUEST));
		}
	}
	
	@Override
	public void bindData() {
		picker_start.setValue(LocalDate.now());
		picker_end.setValue(LocalDate.now());
		this.lab_err_msg.setVisible(false);
		
		Properties osProp = System.getProperties();
		String osName = osProp.getProperty("os.name");
		if(osName != null) {
			if(osName .toLowerCase().indexOf("win") >-1) {
				this.txt_path.setText("D:\\WTX_DATA\\");
			}else if(osName.toLowerCase().indexOf("linux") > - 1) {
				this.txt_path.setText(osProp.getProperty("user.home", "")+"/WTX_DATA/");
			}else {
				this.lab_err_msg.setText("不支持的操作系统 : " + osName);
				this.lab_err_msg.setVisible(true);
				System.err.println("不支持的操作系统 : " + osName);
			}
		}
		
	}
	
	@FXML
	private void on_btn_export() {
		boolean checkFilter = true;			//检查各种参数是否有效
		
		this.lab_err_msg.setVisible(false);
		
		LocalDate date_start = picker_start.getValue();
		LocalDate date_end = picker_end.getValue();
		
		//如果起始日期在截至日期之后
		if(date_start.isAfter(date_end)) {
			this.lab_err_msg.setText("起始日期必须早于截至日期!");
			this.lab_err_msg.setVisible(true);
			checkFilter = false;
		}
		//检查导出目录是否存在
		File checkDir = new File(this.txt_path.getText());
		if(checkDir.exists() && checkDir.isDirectory()) {
		}else {
			this.lab_err_msg.setText("导出目录不存在!");
			this.lab_err_msg.setVisible(true);
			checkFilter = false;
		}
		
		//如果参数有效,开始导出
		if(checkFilter) {
			btn_cancel.setDisable(true);
			btn_export.setDisable(true);
			//创建数据导出任务
			DataExportTask task = new DataExportTask(this);
			progress_export.progressProperty().bind(task.progressProperty());
			new Thread(task).start();
		}
		
	}
	
	/**
	 * 导出数据完毕后执行的操作
	 */
	public void exportFinished() {
		this.btn_export.setDisable(false);
		this.btn_cancel.setDisable(false);
	}
	
	/**
	 * 获取选择的起始时间
	 * @return
	 */
	public LocalDate getStartDate() {
		return picker_start.getValue();
	}
	
	/**
	 * 获取选择的结束时间
	 */
	public LocalDate getEndDate() {
		return picker_end.getValue();
	}
	
	/**
	 * 读取导出数据的目录
	 */
	public String getExportPath() {
		return txt_path.getText();
	}
	
	/**
	 * 获取选择的导出数据的格式
	 * @param me
	 */
	public String getExtension() {
		if(rdb_excel.isSelected()) {
			return ".xls";
		}else if(rdb_txt.isSelected()) {
			return ".txt";
		}else {
			return "";
		}
	}
	
	public void onMouseDragged(MouseEvent me) {
		Window parent = dlg_export.getScene().getWindow();
		if(parent != null && parent instanceof Stage) {
			Stage dialogStage = (Stage)parent;
			dialogStage.setX(me.getScreenX() - initX);
			dialogStage.setY(me.getScreenY() - initY);
		}
	}
	
	public void onMousePressed(MouseEvent me) {
		Window parent = dlg_export.getScene().getWindow();
		if(parent != null && parent instanceof Stage) {
			Stage dialogStage = (Stage)parent;
			initX = me.getScreenX() - dialogStage.getX();
	        initY = me.getScreenY() - dialogStage.getY();
		}
	}
	
	public DataExport() {
	}
}
