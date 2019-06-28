
/**
 * 显示气压刻度从:	650 到 1050
 * 分16个区间，每个区间25 hPa
 */
package com.wtx.control;

import java.text.DecimalFormat;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.VPos;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

public class Barometer extends Pane {
	
	private double curr_press;
	
	//设置一个固定的外部边框
	private final double FIXED_BORDER_WIDTH = 3.0;
	//设置主刻度相对于轮廓圆半径的比例
	private final double  FACT_DIV_RADIUS = 0.1;
	//设置刻度线相对于半径的位置
	private final double FACT_DIV_DIST = 0.45;
	//设置刻度数值相对于半径的比例
	private final double FACT_DIV_TXT = 0.1;
	//设置圆心相对于半径的比例
	private final double FACT_CENTER = 0.02;
	
	/**
	 * 更新湿度图示
	 */
	public void update(double val) {
		curr_press = val;
		redraw();
	}
	
	/**
	 * 获取当前湿度值
	 */
	public double getPressVal() {
		return curr_press;
	}
	
	private void redraw() {
		//清空画布
		this.getChildren().clear();
		{
			//绘制外部轮廓
			final double width = (getWidth()>getHeight())?getHeight():getWidth();
			Circle circ_bound = new Circle(getWidth()/2.0, getHeight()/2.0,width/2.0-FIXED_BORDER_WIDTH);
			Stop[] stops_bound = new Stop[] {
					new Stop(0.0, Color.WHITE),
					new Stop(0.9, Color.WHITE),
					new Stop(1.0, Color.valueOf("#909090"))
					};
			RadialGradient fill_out = new RadialGradient(0.0, 0.0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, stops_bound);
			circ_bound.setFill(fill_out);
			this.getChildren().add(circ_bound);
			//绘制内部轮廓
			Circle circ_inner = new Circle(getWidth()/2.0, getHeight()/2.0,(width/2.0-FIXED_BORDER_WIDTH) * 0.9);
			Stop[] stops_inner = new Stop[] { 
					new Stop(0.0, Color.WHITE), 
					new Stop(0.76, Color.WHITE), 
					new Stop(0.93, Color.valueOf("#a8a8a8")),
					new Stop(0.98, Color.valueOf("#fcfcfc")), 
					new Stop(1.0, Color.valueOf("#fcfcfc"))
					};
			RadialGradient fill_in = new RadialGradient(0.0, 0.0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, stops_inner);
			circ_inner.setFill(fill_in);
			this.getChildren().add(circ_inner);
		}
		
		{
			double width = (getWidth()>getHeight())?getHeight():getWidth();
			//绘制刻度
			double startY = getHeight()/2.0 + width/2.0*FACT_DIV_DIST;
			double endY = startY + width/2.0*FACT_DIV_RADIUS;
			//-----------------------------------------------------------------
			Line line_start = new Line(getWidth()/2.0,startY,getWidth()/2.0,endY);
			line_start.getTransforms().clear();
			Rotate rotat_start = new Rotate(30,getWidth()/2.0,getHeight()/2.0);
			line_start.getTransforms().add(rotat_start);
			this.getChildren().add(line_start);
			//-----------------------------------------------------------------
			Line line_end = new Line(getWidth()/2.0,startY,getWidth()/2.0,endY);
			line_end.getTransforms().clear();
			Rotate rotat_end = new Rotate(-30,getWidth()/2.0,getHeight()/2.0);
			line_end.getTransforms().add(rotat_end);
			this.getChildren().add(line_end);
			//-----------------------------------------------------------------
			//绘制刻度线
			final double fact_angle = 300/15;
			for(int i = 0;i < 15;i ++) {
				Line newline = new Line(getWidth()/2.0,startY,getWidth()/2.0,endY);
				Rotate rotat_curr = new Rotate(fact_angle*i + 30,getWidth()/2.0,getHeight()/2.0);
				newline.getTransforms().add(rotat_curr);
				this.getChildren().add(newline);
			}
			//绘制刻度数值
			int press_curr = 1050;
			double radius_inner = (width/2.0-FIXED_BORDER_WIDTH) * (1-FACT_DIV_DIST+FACT_DIV_TXT);
			for(int i = 0;i < 16;i ++) {
				double size_fnt = width/2.0*FACT_DIV_TXT;
				Text txtDiv = new Text(String.valueOf(press_curr));
				txtDiv.setFont(Font.font("SimHei", FontWeight.THIN, size_fnt));
				txtDiv.setTextOrigin(VPos.CENTER);
				txtDiv.setTextAlignment(TextAlignment.CENTER);
				double angle = fact_angle*i + 30;
				double x = Math.sin(Math.PI*angle/180) *  radius_inner + getWidth()/2.0;
				double y = Math.cos(Math.PI*angle/180) * radius_inner + getHeight()/2.0;
				if(press_curr <= 850) {
					x -= size_fnt * 1.2;
				}else {
					x -= size_fnt * 0.3;
				}
				if(press_curr == 1000) {
					txtDiv.setText("1K");
				}
				txtDiv.setX(x);
				txtDiv.setY(y);
				this.getChildren().add(txtDiv);
				press_curr -= 25;
			}
		}
		
		{
			//绘制箭头
			if(curr_press >= 675.0 && curr_press <= 1050.0) {
				final double width = (getWidth()>getHeight())?getHeight():getWidth();
				double radius_inner = (width/2.0-FIXED_BORDER_WIDTH) * (1-FACT_DIV_DIST+FACT_DIV_TXT);
				double arraw_width = width * FACT_CENTER;
				double arraw_height = radius_inner;
				double rect_x = getWidth()/2.0 - arraw_width/2.0;
				double rect_y = getHeight()/2.0;
				Rectangle rect_arrow = new Rectangle(rect_x, rect_y-arraw_height*0.3, arraw_width, arraw_height);
				Stop[] stops_arrow = new Stop[] { 
						new Stop(0.0, Color.valueOf("#b2b2b2")), 
						new Stop(0.2, Color.WHITE), 
						new Stop(1.0, Color.valueOf("#6f6c6c"))
						};
				LinearGradient fill_arrow = new LinearGradient(0.08, 1.0, 0.9, 1.0, true, CycleMethod.NO_CYCLE, stops_arrow);
				rect_arrow.setFill(fill_arrow);
				rect_arrow.setStrokeWidth(0.0);
				double curr_angle = curr_press / (1050-675) * 300.0;
				Rotate rotat_curr = new Rotate(curr_angle + 30 - 180,getWidth()/2.0,getHeight()/2.0);
				rect_arrow.getTransforms().add(rotat_curr);
				this.getChildren().add(rect_arrow);
			}
		}
		
		{
			//绘制圆心
			final double width = (getWidth()>getHeight())?getHeight():getWidth();
			double radius_center = width * FACT_CENTER;
			Circle cir_center = new Circle(getWidth()/2.0, getHeight()/2.0, radius_center);
			Stop[] stops_cir = new Stop[] { 
					new Stop(0.0, Color.valueOf("#d7d4d4")), 
					new Stop(0.3, Color.valueOf("#d7d4d4")),
					new Stop(1.0, Color.valueOf("#656565"))
					};
			RadialGradient fill = new RadialGradient(0.0, 0.0, 0.34, 0.34, 0.5, true, CycleMethod.NO_CYCLE, stops_cir);
			cir_center.setFill(fill);		//空心
			cir_center.setStrokeWidth(0);
			this.getChildren().add(cir_center);
		}
		
		{
			//绘制底部文字
			final double width = (getWidth()>getHeight())?getHeight():getWidth();
			double startY = getHeight()/2.0 + width/2.0 - FIXED_BORDER_WIDTH;
			double fnt_size = width * 0.05;
			Text txt_bottom = new Text(0,startY - fnt_size * 3.0,"barometer");
			txt_bottom.setTextAlignment(TextAlignment.CENTER);
			txt_bottom.setWrappingWidth(getWidth());
			txt_bottom.setFont(Font.font("SimHei", FontWeight.BOLD, fnt_size));
			this.getChildren().add(txt_bottom);
			Text txt_unit = new Text(0,startY - fnt_size * 4.0,"(hPa)");
			txt_unit.setTextAlignment(TextAlignment.CENTER);
			txt_unit.setWrappingWidth(getWidth());
			txt_unit.setFont(Font.font("SimHei", FontWeight.BOLD, fnt_size));
			this.getChildren().add(txt_unit);
		}
		
		{
			//绘制显示气压
			final double width = (getWidth()>getHeight())?getHeight():getWidth();
			double size_fnt = width * 0.12;
			double startY = getHeight()/2.0 - (width/2.0 - FIXED_BORDER_WIDTH) - size_fnt * 0.5;
			if(startY - size_fnt < 0) {
				
			}else {
				DecimalFormat numFmt = new DecimalFormat("0.0");
				Text txt_label;
				if(curr_press >= 500.0 && curr_press <= 1100.0) {
					txt_label = new Text(0, startY, "气压: "+numFmt.format(curr_press)+" hPa");
				}else {
					txt_label = new Text(0, startY, "气压: ---- hPa");
				}
				txt_label.setFont(Font.font("SimHei", FontWeight.BOLD, size_fnt));
				txt_label.setTextAlignment(TextAlignment.CENTER);
				txt_label.setWrappingWidth(getWidth());
				txt_label.setStroke(Color.WHITE);
				InnerShadow innerShadow = new InnerShadow();
				innerShadow.setRadius(1d);
				innerShadow.setOffsetX(0.3);
				innerShadow.setOffsetY(0.3);
				txt_label.setFill(Color.web("#BBBBBB"));
				txt_label.setEffect(innerShadow);
				
				this.getChildren().add(txt_label);
			}
		}
	}
	
	public Barometer() {
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
