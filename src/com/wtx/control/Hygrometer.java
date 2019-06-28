package com.wtx.control;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class Hygrometer extends Pane {
	
	private double curr_humdity;
	//湿度表中间的空圆柱部分占据总高度的比例
	private double HEIGHT_FACT = 4.0/6.0;
	//湿度表中间的空圆柱部分占据总宽度的比例
	private double WIDTH_FACT = 1.0/8.0;
	
	/**
	 * 控件重绘
	 */
	private void redraw() {
		//清空所有节点
		this.getChildren().clear();
		
		{
			Font txtFont = Font.font("SimHei", FontWeight.BOLD, getWidth() * WIDTH_FACT * 0.6);
			
			//绘制刻度栏
			double total_height = getHeight()*HEIGHT_FACT;
			double width = getWidth() * WIDTH_FACT;
			double x = getWidth()/2.0 - width/2.0 - width;
			double y = getHeight()/2.0 - total_height/2.0;
			
			//----------------------------------------------------
			Rectangle range1 = new Rectangle(x, y, width, total_height/4.0);
			Stop[] stops1 = new Stop[] { 
					new Stop(0.0, Color.valueOf("#eaeeee")), 
					new Stop(0.3, Color.valueOf("#65b4d0")),
					new Stop(1.0, Color.valueOf("#809394"))
					};
			LinearGradient grad1 = new LinearGradient(0.0, 1.0, 1.0, 1.0, true, CycleMethod.NO_CYCLE, stops1);
			range1.setFill(grad1);
			this.getChildren().add(range1);

			Text txt1 = new Text(0,y+total_height/8.0+txtFont.getSize()*0.5,"防潮");
			txt1.setFont(txtFont);
			txt1.setStrokeWidth(0.0);
			txt1.setFill(Color.valueOf("#2fc7f2"));
			txt1.setWrappingWidth(x - 10);
			txt1.setTextAlignment(TextAlignment.RIGHT);
			this.getChildren().add(txt1);
			//----------------------------------------------------
			Rectangle range2 = new Rectangle(x, y+total_height/4.0, width, total_height/4.0);
			Stop[] stops2 = new Stop[] { 
					new Stop(0.0, Color.valueOf("#f5f5f5")), 
					new Stop(0.3, Color.valueOf("#5aff48")),
					new Stop(1.0, Color.valueOf("#868282"))
					};
			LinearGradient grad2 = new LinearGradient(0.0, 1.0, 1.0, 1.0, true, CycleMethod.NO_CYCLE, stops2);
			range2.setFill(grad2);
			this.getChildren().add(range2);
			
			double txt_y2 = y+total_height/4.0 + total_height/8.0+txtFont.getSize()*0.5;
			Text txt2 = new Text(0,txt_y2,"舒适");
			txt2.setFont(txtFont);
			txt2.setStrokeWidth(0.0);
			txt2.setFill(Color.valueOf("#1fc321"));
			txt2.setWrappingWidth(x - 10);
			txt2.setTextAlignment(TextAlignment.RIGHT);
			this.getChildren().add(txt2);
			//----------------------------------------------------
			Rectangle range3 = new Rectangle(x, y+total_height/2.0, width, total_height/4.0);
			Stop[] stops3 = new Stop[] { 
					new Stop(0.0, Color.valueOf("#fffddd")), 
					new Stop(0.3, Color.valueOf("#f5d800")),
					new Stop(1.0, Color.valueOf("#727171"))
					};
			LinearGradient grad3 = new LinearGradient(0.0, 1.0, 1.0, 1.0, true, CycleMethod.NO_CYCLE, stops3);
			range3.setFill(grad3);
			this.getChildren().add(range3);
			
			double txt_y3 = y+total_height/2.0 + total_height/8.0+txtFont.getSize()*0.5;
			Text txt3 = new Text(0,txt_y3,"干燥");
			txt3.setFont(txtFont);
			txt3.setStrokeWidth(0.0);
			txt3.setFill(Color.valueOf("#e1ac00"));
			txt3.setWrappingWidth(x - 10);
			txt3.setTextAlignment(TextAlignment.RIGHT);
			this.getChildren().add(txt3);
			//----------------------------------------------------
			Rectangle range4 = new Rectangle(x, y+total_height*0.75, width, total_height/4.0);
			Stop[] stops4 = new Stop[] { 
					new Stop(0.0, Color.valueOf("#fffbfb")), 
					new Stop(0.3, Color.valueOf("#f20000")),
					new Stop(1.0, Color.valueOf("#795755"))
					};
			LinearGradient grad4 = new LinearGradient(0.0, 1.0, 1.0, 1.0, true, CycleMethod.NO_CYCLE, stops4);
			range4.setFill(grad4);
			this.getChildren().add(range4);
			
			double txt_y4 = y+total_height*0.75 + total_height/8.0+txtFont.getSize()*0.5;
			Text txt4 = new Text(0,txt_y4,"防火");
			txt4.setFont(txtFont);
			txt4.setStrokeWidth(0.0);
			txt4.setFill(Color.valueOf("#c54545"));
			txt4.setWrappingWidth(x - 10);
			txt4.setTextAlignment(TextAlignment.RIGHT);
			this.getChildren().add(txt4);
		}
		
		{
			//添加显示刻度
			double width = getWidth() * WIDTH_FACT;
			double total_height = getHeight()*HEIGHT_FACT;
			double x = getWidth()/2.0;
			double y = getHeight()/2.0 - total_height/2.0;
			Font fnt_div = Font.font("SimHei", FontWeight.BOLD, width * 0.5);
			int DIV_START = 100;
			for(int i=0;i<5;i++)
			{
				Text txt_div = new Text(x, y + i * total_height/4.0 + fnt_div.getSize() * 0.5, String.valueOf(DIV_START));
				txt_div.setFont(fnt_div);
				txt_div.setFill(Color.valueOf("#00000050"));
				txt_div.setStroke(Color.LIGHTGRAY);
				this.getChildren().add(txt_div);
				DIV_START -= 25;
			}
		}
		
		{
			//绘制刻度显示部分
			double width = getWidth() * WIDTH_FACT;
			double total_height = getHeight()*HEIGHT_FACT;
			double startX = getWidth()/2.0 - width/2.0;
			double startY = getHeight()/2.0 - total_height/2.0;
			double perPix = total_height / 100.0;
 			double currY = (100.0 - curr_humdity) * perPix + startY ;
			
			//绘制空心圆
 			//DropShadow divEffect = new DropShadow(19, 3.0, 3.0, Color.BLACK);
			double circX = startX + width * 2.5;
			Circle circ_curr = new Circle(circX, currY, width * 0.8);
			circ_curr.setStroke(Color.WHITE);
			circ_curr.setStrokeWidth(total_height * 0.015);
			circ_curr.setFill(null);
			//circ_curr.setEffect(divEffect);
			this.getChildren().add(circ_curr);
			
			//在空心圆里写入数据
			Text txt_val = new Text(circX - width/2.0, currY + width/2.5, String.valueOf(Math.round(curr_humdity)));
			txt_val.setFont(Font.font("SimHei", FontWeight.BOLD, width));
			txt_val.setStrokeWidth(0);
			txt_val.setFill(Color.WHITE);
			this.getChildren().add(txt_val);
			
			//计算起始纵坐标
			Line line_div = new Line(startX, currY, startX + width * 2.5 - width * 0.8, currY);
			DropShadow lineEffect = new DropShadow(0.0,Color.GREY);
			lineEffect.setOffsetY(total_height * 0.015);
			line_div.setEffect(lineEffect);
			line_div.setStroke(Color.WHITE);
			line_div.setStrokeWidth(total_height * 0.015);
			this.getChildren().add(line_div);
			
			//添加显示内容“相对湿度”
			//Text txt_disp = new Text(0, startY-width*0.6, "相对湿度");
			Text txt_disp = new Text(0, startY-width * 0.5, "相对湿度(%RH)");
			txt_disp.setFont(Font.font("SimHei",FontWeight.BOLD,width));
			txt_disp.setWrappingWidth(getWidth());
			txt_disp.setTextAlignment(TextAlignment.CENTER);
			txt_disp.setFill(Color.web("#BBBBBB"));
			txt_disp.setStroke(Color.WHITE);
			this.getChildren().add(txt_disp);
		}
	}
	
	
	/**
	 * 更新湿度图示
	 */
	public void update(double val) {
		curr_humdity = val;
		redraw();
	}
	
	/**
	 * 获取当前湿度值
	 */
	public double getHumdVal() {
		return curr_humdity;
	}
	
	public Hygrometer() {
		//预先设置一个无效的湿度
		curr_humdity = 999.0;
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
