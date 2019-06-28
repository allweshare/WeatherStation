package com.wtx.control;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class Thermograph extends Pane {
	
	//温度计中间的空圆柱部分占据总高度的比例
	private double HEIGHT_FACT = 4.0/6.0;
	//温度计中间的空圆柱部分占据总宽度的比例
	private double WIDTH_FACT = 1.0/12.0;
	//温度计下方的椭圆横向半径
	private double XRADIUS_FACT = 1.0/10.0;
	//温度计下方的椭圆纵向半径
	private double YRADIUS_FACT = 1.0/15.0;
	//温度刻度显示
	private double DIV_FACT = 3.0/6.0;
	//温度刻度显示范围
	private double DIV_START = 50;
	private double DIV_END = -40;
	//当前温度刻度显示
	private Rectangle rect_curr;
	//当前的温度数值
	private double curr_temp;
	
	
	/**
	 * 更新温度图示
	 */
	public void update(double val) {
		curr_temp = val;
		redraw();
	}
	
	/**
	 * 获取当前温度值
	 */
	public double getTempVal() {
		return curr_temp;
	}
	
	/**
	 * 绘制轮廓
	 */
	private void redraw() {
		//清空所有节点
		this.getChildren().clear();
		
		{
			//绘制温度计空圆柱部分
			double width = getWidth() * WIDTH_FACT;
			double height = getHeight() * HEIGHT_FACT;
			
			double x = getWidth()/2.0 - width/2.0;
			double y = getHeight()/2.0 - height/2.0;
			Rectangle rect_cylind = new Rectangle(x, y, width, height);
			rect_cylind.setArcWidth(10);
			rect_cylind.setArcHeight(20);
			
			Stop[] stops = new Stop[] { 
					new Stop(0.0, Color.valueOf("#d0d0d0")), 
					new Stop(0.3, Color.WHITE),
					new Stop(1.0, Color.valueOf("#807e7e"))
					};
			LinearGradient lineGrad = new LinearGradient(0.0, 1.0, 1.0, 1.0, true, CycleMethod.NO_CYCLE, stops);
			
			
			rect_cylind.setFill(lineGrad);
			
			this.getChildren().add(rect_cylind);
		}
		
		{
			//绘制温度计下方的椭圆阴影
			double radiusX = getWidth() * XRADIUS_FACT * 1.5;
			double radiusY = getHeight() * YRADIUS_FACT / 2.0 * 0.5;
			double x = getWidth()/2.0 + radiusX * 0.08;
			double y = getHeight()/2.0 + getHeight()*HEIGHT_FACT/2.0 + radiusY*6.0;
			Ellipse ellps_bottom = new Ellipse(x, y, radiusX, radiusY);
			Stop[] stops = new Stop[] { 
					new Stop(0.0, Color.valueOf("#a4a4a4")), 
					new Stop(0.58, Color.WHITE),
					new Stop(1.0, Color.valueOf("#a49f9f"))
					};
			LinearGradient lineGrad = new LinearGradient(0.0, 1.0, 1.0, 1.0, true, CycleMethod.NO_CYCLE, stops);
			ellps_bottom.setFill(lineGrad);
			GaussianBlur gaussEffect = new GaussianBlur(10.73);
			ellps_bottom.setEffect(gaussEffect);
			this.getChildren().add(ellps_bottom);
		}
		
		{
			//绘制温度数值显示
			double x = 0;
			double y = (1.0-HEIGHT_FACT)/3.0 * getHeight();
			String strDisp;
			if(curr_temp >= -40.0 && curr_temp <= 50.0) {
				strDisp = "温度："+String.valueOf(curr_temp)+"℃";		//温度：-13.5℃
			}else {
				strDisp = "温度：----℃";		//温度：-13.5℃
			}

			Text txt_temp = new Text(x, y, strDisp);
			txt_temp.setWrappingWidth(getWidth());
			
			InnerShadow innerShadow = new InnerShadow();
			innerShadow.setRadius(1d);
			innerShadow.setOffsetX(0.3);
			innerShadow.setOffsetY(0.3);
			txt_temp.setFill(Color.web("#BBBBBB"));
			txt_temp.setEffect(innerShadow);
			txt_temp.setStroke(Color.WHITE);
			
			double fontSize = getWidth()/strDisp.length();
			Font txtFont = Font.font("SimHei", FontWeight.BOLD, fontSize);
			txt_temp.setFont(txtFont);
			txt_temp.setTextAlignment(TextAlignment.CENTER);
			
			this.getChildren().add(txt_temp);
		}
		
		{
			//绘制温度刻度
			double left_x = getWidth()/2.0 - getWidth()*WIDTH_FACT*1.5;
			double right_x = getWidth()/2.0 + getWidth()*WIDTH_FACT;
			double y = getHeight()/2.0 - getHeight()*DIV_FACT/2.0;
			double div_width = getWidth() * WIDTH_FACT * 0.5;
			
 			double perFix = (getHeight()*DIV_FACT)/(DIV_START-DIV_END)*5.0;
 			
 			int div_val_curr = (int)DIV_START;
 			int div_sub_curr = (int)DIV_START - 5;
 			for(int i=0;i<((DIV_START-DIV_END)/10.0*2.0 + 1);i++) {
 				//大刻度
 				if(i%2 == 0) {
 					Font fnt_div = Font.font("SimHei", FontWeight.BOLD, div_width * 1.5);
 					Text txt_div = new Text(0, y+i*perFix+(div_width * 1.5)*0.35, String.valueOf(div_val_curr));
 					txt_div.setWrappingWidth(left_x - div_width*2);
 					txt_div.setTextAlignment(TextAlignment.RIGHT);
 					txt_div.setFont(fnt_div);
 					txt_div.setStroke(Color.WHITE);
 					this.getChildren().add(txt_div);
 					Line left = new Line(left_x-div_width, y+i*perFix, left_x+div_width, y+i*perFix);
 					left.setStroke(Color.WHITE);
 	 				this.getChildren().add(left);
 	 				
 	 				Line right = new Line(right_x, y+i*perFix, right_x+div_width * 2, y+i*perFix);
 	 				right.setStroke(Color.WHITE);
 	 				this.getChildren().add(right);
 	 				
 	 				div_val_curr -= 10;
 				}
 				if(i%2 == 1) {
 					Font fnt_div = Font.font("SimHei", FontWeight.BOLD, div_width);
 					Line left = new Line(left_x, y+i*perFix, left_x+div_width, y+i*perFix);
 					left.setStroke(Color.WHITE);
 	 				this.getChildren().add(left);
 	 				Line right = new Line(right_x, y+i*perFix, right_x+div_width, y+i*perFix);
 	 				right.setStroke(Color.WHITE);
 	 				this.getChildren().add(right);
 	 				Text txt_div = new Text(right_x+div_width*2,y+i*perFix+div_width*0.35,String.valueOf(div_sub_curr));
 	 				txt_div.setFont(fnt_div);
 	 				txt_div.setStroke(Color.WHITE);
 	 				this.getChildren().add(txt_div);
 	 				
 	 				div_sub_curr -= 10;
 				}
 			}
		}
		
		{
			//计算并绘制当前温度所在的刻度
			if(curr_temp >= -40.0 && curr_temp <= 50.0) {
				double perFix = (getHeight()*DIV_FACT)/(DIV_START-DIV_END);
				//double perFix = (getHeight()*DIV_FACT)/(DIV_START-DIV_END)*5.0
				double width = getWidth() * WIDTH_FACT;
				double x = getWidth()/2.0 - width/2.0;
	 			double start_y = getHeight()/2.0 - getHeight()*DIV_FACT/2.0;
	 			double end_y = getHeight()/2.0 + getHeight()*HEIGHT_FACT/2.0;
	 			double y = start_y + (perFix * (DIV_START-curr_temp));
				
	 			//double height = getHeight() * HEIGHT_FACT;
				Stop[] stops = new Stop[] { 
						new Stop(0.0, Color.valueOf("#ff261f")), 
						new Stop(0.2734, Color.valueOf("#f2caca")),
						new Stop(1.0, Color.valueOf("#ab0606"))
						};
				LinearGradient lineGrad = new LinearGradient(0.0, 1.0, 1.0, 1.0, true, CycleMethod.NO_CYCLE, stops);
				
				rect_curr = new Rectangle(x, y, width, end_y-y);
				rect_curr.setFill(lineGrad);
				rect_curr.setArcWidth(10);
				rect_curr.setArcHeight(20);
				this.getChildren().add(rect_curr);
			}
		}
		
		{
			//绘制温度计下面的椭圆
			double x = getWidth()/2.0;
			double y = getHeight()/2.0 + getHeight()*HEIGHT_FACT/2.0;
			double radiusX = getWidth() * XRADIUS_FACT;
			double radiusY = getHeight() * YRADIUS_FACT;
			Ellipse ellps_bottom = new Ellipse(x, y, radiusX, radiusY);
			
			Stop[] stops = new Stop[] { 
					new Stop(0.0, Color.valueOf("#fcc7c7")), 
					new Stop(0.5, Color.valueOf("#fc7777")),
					new Stop(1.0, Color.valueOf("#a80000"))
					};
			RadialGradient radiaGrad = new RadialGradient(0.0, 0.0, 0.34, 0.36, 0.5, true, CycleMethod.NO_CYCLE, stops);
			ellps_bottom.setFill(radiaGrad);
			
			this.getChildren().add(ellps_bottom);
		}
	}
	
	public Thermograph() {
		//默认设置一个无效的温度值
		curr_temp = -99.9;
		//-fx-border-style:dotted;
		String borderStyle = "-fx-border-color:white;-fx-border-radius:30;-fx-border-width:0.5;";
		this.setStyle(borderStyle);
		DropShadow borderEffect = new DropShadow(8, 0.0, 0.0, Color.GRAY);
		borderEffect.setWidth(10);
		borderEffect.setHeight(12);
		this.setEffect(borderEffect);
		
		this.widthProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				redraw();
			}
		});
		this.heightProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				redraw();
			}
		});
	}
	
}
