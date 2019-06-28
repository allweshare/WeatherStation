package com.wtx;

import com.wtx.ResourceFactory.FXMLResource;
import com.wtx.controller.MainFrame;
import com.wtx.dao.DataBaseDao;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Main extends Application{

	private double initX;
    private double initY;
    
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void init() throws Exception {
		super.init();
		FactoryPreset.getPreset();
		DataBaseDao.getInstance();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		final FXMLResource<MainFrame> mainFrame = ResourceFactory.getFactory().getMainFrame();
		if(mainFrame.getContainer() != null){
			mainFrame.getContainer().getStylesheets().add("/com/wtx/view/custom.css");
			mainFrame.getContainer().setOnMouseDragged((MouseEvent me) -> {
				primaryStage.setX(me.getScreenX() - initX);
				primaryStage.setY(me.getScreenY() - initY);
			});
			mainFrame.getContainer().setOnMousePressed((MouseEvent me) -> {
				initX = me.getScreenX() - primaryStage.getX();
                initY = me.getScreenY() - primaryStage.getY();
			});
			
			Scene scene = new Scene(mainFrame.getContainer(),1024,768);
			primaryStage.initStyle(StageStyle.UNDECORATED);
			primaryStage.setTitle("西昌气象站观测软件");
			primaryStage.setScene(scene);
			primaryStage.centerOnScreen();
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					//关闭串口之前关闭串口
					if(mainFrame.getController().getComService() != null) {
						mainFrame.getController().getComService().close();
					}
				}
			});
			primaryStage.setOnShown(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					if(mainFrame != null && mainFrame.getContainer() != null) {
						mainFrame.getController().bindData();
					}
				}
			});
			
			primaryStage.show();
		}else{
			System.err.println("加载主界面资源错误!");
		}
	}

}
