package com.wtx.model;

import com.wtx.ResourceFactory;
import com.wtx.ResourceFactory.FXMLResource;
import com.wtx.controller.IDataBind;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class CustomDialog extends Stage {
	//对话框加载时,执行数据绑定的方式
		public enum DialogLoad {
			FIRST_LOAD,
			EVERY_LOAD
		}
		
		public CustomDialog(Modality mode,FXMLResource<?> resource) {
			super();
			
			Window parent = ResourceFactory.getFactory().getMainFrame().getContainer().getScene().getWindow();
			this.initModality(mode);
			if(parent != null) {
				this.initOwner(parent);
			}
			this.initStyle(StageStyle.UNDECORATED);
			this.setResizable(false);
			this.setFullScreen(false);
			this.centerOnScreen();
			Scene scene = null;
			if(resource != null && resource.getContainer() != null) {
				scene = new Scene(resource.getContainer());
			}else {
				Pane panel = new Pane();
				scene = new Scene(panel,300,200);
			}
			
			if(resource.getController() != null && resource.getController() instanceof IDataBind) {
				IDataBind databind = (IDataBind)resource.getController();
				databind.bindData();
			}
			
			scene.getStylesheets().add("/com/wtx/view/custom.css");
			this.setScene(scene);
		}
		
}
