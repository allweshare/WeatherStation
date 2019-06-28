package com.wtx.controller;

import java.util.Iterator;
import java.util.Set;

import com.wtx.FactoryPreset;
import com.wtx.ResourceFactory;
import com.wtx.service.SerialComService;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class SerialPanel implements IDataBind {
	
	/**
	 * 超过1024个字节自动清空
	 */
	private final int DISP_MAX_LENGTH	=	1024;
	
	@FXML
	private ComboBox<String> cmb_portlist;
	
	@FXML
	private ComboBox<String> cmb_baudrates;
	
	@FXML
	private ComboBox<String> cmb_datas;
	
	@FXML
	private ComboBox<String> cmb_parity;
	
	@FXML
	private ComboBox<String> cmb_stops;
	
	@FXML
	private Button btn_switch;
	
	@FXML
	private TextArea area_recv;
	
	@FXML
	private TextField txt_input;
	
	@FXML
	private CheckBox chk_newline;
	
	@FXML
	private Button btn_send;
	
	public SerialPanel() {
		//System.out.println("Serial Panel Init ...");
	}
	
	/**
	 * 追加接收内容
	 */
	public void appendRecv(String buff) {
		if(area_recv != null && buff != null) {
			if(area_recv.getLength() > DISP_MAX_LENGTH) {
				area_recv.clear();
			}
			area_recv.appendText(buff);
		}
	}
	
	@Override
	public void bindData() {
		//System.out.println("SerialPanel::bindData() ...");
		//加载串口列表，并选中之前缓存的结果
		if(cmb_portlist != null) {
			cmb_portlist.getItems().clear();
			Set<String> portList = SerialComService.getPortList();
			Iterator<String> itert = portList.iterator();
			while(itert.hasNext()) {
				cmb_portlist.getItems().add(itert.next());
			}
			String currPort = FactoryPreset.getPreset().readValueByKey(FactoryPreset.COMMPORT);
			cmb_portlist.getSelectionModel().select(currPort);
		}
		
		//波特率
		if(cmb_baudrates != null) {
			cmb_baudrates.getItems().clear();
			cmb_baudrates.getItems().addAll("1200","2400","4800","9600","14400","19200","38400","57600","115200");
			String currBaud = FactoryPreset.getPreset().readValueByKey(FactoryPreset.BAUDRATE);
			cmb_baudrates.getSelectionModel().select(currBaud);
		}
		
		//数据位
		if(cmb_datas != null) {
			cmb_datas.getItems().clear();
			cmb_datas.getItems().addAll("5","6","7","8");
			String currDatas = FactoryPreset.getPreset().readValueByKey(FactoryPreset.DATABITS);
			cmb_datas.getSelectionModel().select(currDatas);
		}
		
		//校验位
		if(cmb_parity != null) {
			cmb_parity.getItems().clear();
			cmb_parity.getItems().addAll("NONE","EVEN","ODD");
			String currParity = FactoryPreset.getPreset().readValueByKey(FactoryPreset.PARITY);
			cmb_parity.getSelectionModel().select(currParity);
		}
		
		//停止位
		if(cmb_stops != null) {
			cmb_stops.getItems().clear();
			cmb_stops.getItems().addAll("1","1.5","2");
			String currStops = FactoryPreset.getPreset().readValueByKey(FactoryPreset.STOPBITS);
			cmb_stops.getSelectionModel().select(currStops);
		}
		
		//接收内容显示
		if(area_recv != null) {
			area_recv.setEditable(false);
		}
		
		//串口打开与关闭
		if(btn_switch != null) {
			btn_switch.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					MainFrame mainFrame = (MainFrame)ResourceFactory.getFactory().getMainFrame().getController();
					//将当前通信服务设置为串口通信
					if(mainFrame.getComService() == null) {
						mainFrame.setComService(new SerialComService());
					}
					
					//保存当前选定的通信参数
					FactoryPreset.getPreset().writeKeyValue(FactoryPreset.COMMPORT, cmb_portlist.getSelectionModel().getSelectedItem());
					FactoryPreset.getPreset().writeKeyValue(FactoryPreset.BAUDRATE, cmb_baudrates.getSelectionModel().getSelectedItem());
					FactoryPreset.getPreset().writeKeyValue(FactoryPreset.DATABITS, cmb_datas.getSelectionModel().getSelectedItem());
					FactoryPreset.getPreset().writeKeyValue(FactoryPreset.PARITY, cmb_parity.getSelectionModel().getSelectedItem());
					FactoryPreset.getPreset().writeKeyValue(FactoryPreset.STOPBITS, cmb_stops.getSelectionModel().getSelectedItem());
					
					//检查串口是否已经打开
					if(mainFrame.getComService().isOpen()) {
						mainFrame.getComService().close();
						btn_switch.setText("打开");
					}else {
						//打开串口
						if(mainFrame.getComService().open()) {
							btn_switch.setText("关闭");
						}
						
					}
				}
			});
		}
		
		//串口发送
		if(btn_send != null && txt_input != null && chk_newline != null) {
			btn_send.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					String strSend;
					if(chk_newline.isSelected()) {
						strSend = txt_input.getText() + "\r\n";
					}else {
						strSend = txt_input.getText();
					}
					MainFrame mainFrame = (MainFrame)ResourceFactory.getFactory().getMainFrame().getController();
					if(mainFrame.getComService() instanceof SerialComService) {
						SerialComService comService = (SerialComService)mainFrame.getComService();
						comService.send(strSend.getBytes());
					}
				}
			});
		}
		
	}
	
	
}
