package com.wtx;

import java.io.InputStream;

import com.wtx.controller.DataExplorer;
import com.wtx.controller.DataExport;
import com.wtx.controller.MainFrame;
import com.wtx.controller.SerialPanel;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;

public class ResourceFactory {
	private static ResourceFactory factory = null;
	
	/**
	 * 自定义资源项
	 * @param <资源对应的控制器类>
	 */
	public class FXMLResource<T> {
		private Parent container;
		private T controller;
		
		public FXMLResource(Parent container,T controller){
			this.container = container;
			this.controller = controller;
		}
		
		public Parent getContainer(){
			return this.container;
		}
		
		public T getController(){
			return this.controller;
		}
	}
	
	//---------------------------------------------------------------
	//主界面资源
	private FXMLResource<MainFrame> mainFrame = null;
	public FXMLResource<MainFrame> getMainFrame(){
		return this.mainFrame;
	}
	
	private FXMLResource<SerialPanel> serialPanel = null;
	public FXMLResource<SerialPanel> getSerialPanel(){
		return this.serialPanel;
	}
	
	/**
	 * 加载数据浏览面板,每次都创建新的实例
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public FXMLResource<DataExplorer> getDataExplorer() {
		return (FXMLResource<DataExplorer>) loadFXMLResource("/com/wtx/view/explorer.fxml");
	}
	
	/**
	 * 加载数据导出面板,每次都创建新的实例
	 */
	@SuppressWarnings("unchecked")
	public FXMLResource<DataExport> getDataExport() {
		return (FXMLResource<DataExport>)loadFXMLResource("/com/wtx/view/export.fxml");
	}
	
	@SuppressWarnings("unchecked")
	private ResourceFactory(){
		System.out.println("ResourceFactory init ......");
		//这里列出只加载一次的资源
		//---------------------------------------
		mainFrame = (FXMLResource<MainFrame>) loadFXMLResource("/com/wtx/view/main.fxml");
		serialPanel = (FXMLResource<SerialPanel>) loadFXMLResource("/com/wtx/view/serialport.fxml");
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private FXMLResource<?> loadFXMLResource(String fxmlPath){
		FXMLResource<?> temp = null;
		try {
			FXMLLoader loader = new FXMLLoader();
			InputStream in = getClass().getResourceAsStream(fxmlPath);
			loader.setBuilderFactory(new JavaFXBuilderFactory());
			try {
				temp = new FXMLResource(loader.load(in), loader.getController());
				return temp;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ResourceFactory getFactory(){
		if(factory == null){
			synchronized (ResourceFactory.class) {
				if(factory == null){
					factory = new ResourceFactory();
				}
			}
		}
		return factory;
	}
}
