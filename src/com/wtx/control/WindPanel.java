package com.wtx.control;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.effect.Bloom;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

public class WindPanel extends Pane {
	
	//设置一个固定的外部边框
	private final double FIXED_BORDER_WIDTH = 10.0;
	//设置中心原点的半径相对于外圆半径的比例
	private final double FACT_CIRC_CENTER = 0.03;
	//设置风向标纵向偏移量
	private final double OFFSET_Y_ARROW = 0.3;
	
	private Double wind_speed = null;
	private Short wind_dir = null;
	private final String wind_title;
	
	private Queue<Short> dir_fifo = new LinkedList<Short>();
	
	public WindPanel(String title) {
		this.setStyle("-fx-background-image: url(/img/bkg.jpg)");
		this.wind_title = title;
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
	
	/**
	 * 绘制风向标
	 * degree: 指定角度
	 * opacity: 指定透明度(0.0 ~ 1.0)
	 */
	public void draw_arrow(double degree,double opacity) {
		//绘制外部轮廓
		final double width = (getWidth()>getHeight())?getHeight():getWidth();
		//绘制风向标
		double rect_width = FACT_CIRC_CENTER * width * 0.5;
		double rect_height = width/2.0-FIXED_BORDER_WIDTH;
		double startX = getWidth()/2.0 - rect_width/2.0;
		double startY = getHeight()/2.0 - rect_height * OFFSET_Y_ARROW;
		Rectangle rect_arrow = new Rectangle(startX, startY, rect_width, rect_height);
		Stop[] stops_arrow = new Stop[] {
				new Stop(0.0, Color.valueOf("#2c2c2c")),
				new Stop(0.176, Color.valueOf("#fffbfb")),
				new Stop(1.0, Color.valueOf("#2c2c2c"))
				};
		LinearGradient fill_arrow = new LinearGradient(0.0, 1.0, 1.0, 1.0, true, CycleMethod.NO_CYCLE, stops_arrow);
		rect_arrow.setFill(fill_arrow);
		rect_arrow.setStrokeWidth(0);
		//------------------------------------------------
		//旋转到指定的位置
		Rotate rotat_curr = new Rotate(degree,getWidth()/2.0,getHeight()/2.0);
		rect_arrow.getTransforms().clear();
		rect_arrow.getTransforms().add(rotat_curr);
		rect_arrow.setOpacity(opacity);
		this.getChildren().add(rect_arrow);
	}
	
	public void redraw() {
		//清空画布
		this.getChildren().clear();
		//绘制外部轮廓
		final double width = (getWidth()>getHeight())?getHeight():getWidth();
		{
			Circle circ_bound = new Circle(getWidth()/2.0, getHeight()/2.0, width/2.0-FIXED_BORDER_WIDTH);
			circ_bound.setStrokeWidth(0);
			Stop[] stops_bound = new Stop[] {
					new Stop(0.0, Color.TRANSPARENT),
					new Stop(0.9, Color.TRANSPARENT),
					new Stop(1.0, Color.valueOf("#b5b5b5"))
					};
			RadialGradient bound_fill = new RadialGradient(0.0, 0.0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, stops_bound);
			circ_bound.setFill(bound_fill);
			this.getChildren().add(circ_bound);
		}
		
		{
			//绘制内环线
			double radius_inner = (width/2.0-FIXED_BORDER_WIDTH) * 0.7;
			Circle circ_inner = new Circle(getWidth()/2.0, getHeight()/2.0, radius_inner);
			circ_inner.setFill(null);
			circ_inner.setStroke(Color.WHITE);
			circ_inner.setStrokeWidth(1);
			circ_inner.setStyle("-fx-stroke-dash-array: 1 5 1 5;");
			this.getChildren().add(circ_inner);
		}
		
		{
			double radius_inner = (width/2.0-FIXED_BORDER_WIDTH) * 0.7;
			double fnt_size = radius_inner * 0.3;
			Font fnt_lab = Font.font("Microsoft YaHei", FontWeight.BOLD, fnt_size);
			
			//绘制东西南北方位
			Text txt_north = new Text(getWidth()/2.0 - fnt_size/2.0,getHeight()/2.0 - radius_inner - fnt_size/3.0,"北");
			txt_north.setFill(Color.WHITE);
			txt_north.setStrokeWidth(0);
			txt_north.setFont(fnt_lab);
			txt_north.setEffect(new Bloom());
			this.getChildren().add(txt_north);
			
			Text txt_south = new Text(getWidth()/2.0 - fnt_size/2.0,getHeight()/2.0 + radius_inner + fnt_size,"南");
			txt_south.setFill(Color.WHITE);
			txt_south.setStrokeWidth(0);
			txt_south.setFont(fnt_lab);
			txt_south.setEffect(new Bloom());
			this.getChildren().add(txt_south);
			
			Text txt_west = new Text(getWidth()/2.0 - radius_inner - fnt_size,getHeight()/2.0 + fnt_size/3.0 ,"西");
			txt_west.setFill(Color.WHITE);
			txt_west.setStrokeWidth(0);
			txt_west.setFont(fnt_lab);
			txt_west.setEffect(new Bloom());
			this.getChildren().add(txt_west);
			
			Text txt_east = new Text(getWidth()/2.0 + radius_inner,getHeight()/2.0 + fnt_size/3.0, "东");
			txt_east.setFill(Color.WHITE);
			txt_east.setStrokeWidth(0);
			txt_east.setFont(fnt_lab);
			txt_east.setEffect(new Bloom());
			this.getChildren().add(txt_east);
		}
		
		{
			//开始绘制风向标和轨迹
			if(dir_fifo.size() >= 3) {
				Iterator<Short> dir_itert = dir_fifo.iterator();
				draw_arrow(180 + dir_itert.next(), 0.10);
				draw_arrow(180 + dir_itert.next(), 0.25);
				draw_arrow(180 + dir_itert.next(), 1.0);
			}
		}
		
		{
			//绘制中心原点
			double circ_radius = FACT_CIRC_CENTER * width;
			Circle circ_center = new Circle(getWidth()/2.0,getHeight()/2.0,circ_radius);
			Stop[] stops_center = new Stop[] {
					new Stop(0.0, Color.WHITE),
					new Stop(0.65, Color.valueOf("#6867679e")),
					new Stop(1.0, Color.TRANSPARENT)
					};
			RadialGradient center_fill = new RadialGradient(0.0, 0.0, 0.46, 0.5, 0.5, true, CycleMethod.NO_CYCLE, stops_center); 
			circ_center.setFill(center_fill);
			circ_center.setStrokeWidth(0);
			this.getChildren().add(circ_center);
		}
		
		{
			//显示当前的内容和数值
			double fnt_size = width * 0.1;
			double label_y = getHeight()/2.0 - (width/2.0-FIXED_BORDER_WIDTH) - fnt_size/3.5;
			Text wind_type = new Text(0, label_y, wind_title);
			wind_type.setWrappingWidth(getWidth());
			wind_type.setTextAlignment(TextAlignment.CENTER);
			wind_type.setFont(Font.font("SimHei",FontWeight.BOLD,fnt_size));
			wind_type.setFill(Color.WHITE);
			wind_type.setStrokeWidth(0);
			//wind_type.setEffect(new Bloom());
			this.getChildren().add(wind_type);
			
			double value_y = getHeight()/2.0 + (width/2.0-FIXED_BORDER_WIDTH) + fnt_size/1.5;
			value_y += FIXED_BORDER_WIDTH*0.5;
			DecimalFormat numFmt = new DecimalFormat("0.0");
			Text wind_value = new Text(0,value_y,"风速：----  风向：----");
			if(this.wind_speed != null && this.wind_dir != null) {
				if(this.wind_speed > 0.0) {
					wind_value.setText("风速："+numFmt.format(this.wind_speed)+"m/s  风向：" + this.wind_dir + "°");
				}else {
					wind_value.setText("风速："+numFmt.format(this.wind_speed)+"m/s  风向：----");
				}
			}
			wind_value.setWrappingWidth(getWidth());
			wind_value.setTextAlignment(TextAlignment.CENTER);
			wind_value.setFont(Font.font("SimHei",FontWeight.BOLD,fnt_size/1.3));
			wind_value.setFill(Color.WHITE);
			wind_value.setStrokeWidth(0);
			//wind_value.setEffect(new Bloom());
			this.getChildren().add(wind_value);
			
		}
	}
	
	/**
	 * 显示或切换风向
	 * @param speed
	 * @param dir
	 */
	public void update(double speed,short dir) {
		
		this.wind_speed = speed;
		this.wind_dir = dir;
		
		dir_fifo.offer(dir);
		if(dir_fifo.size() > 3) {
			dir_fifo.poll();	
			//开始更新风向标
			redraw();
		}
		//-----------------------------------
		//输出最近的三次风向
//		Iterator<Short> dir_itert = dir_fifo.iterator();
//		System.out.println("Dir: ");
//		while(dir_itert.hasNext()) {
//			System.out.print(dir_itert.next() + "\t");
//		}
//		System.out.println();
	}
	
}
