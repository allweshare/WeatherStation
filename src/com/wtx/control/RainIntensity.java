package com.wtx.control;

import java.text.DecimalFormat;
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

public class RainIntensity extends Pane{
	
	private double rainHour = 0.0;
	private final double[] RAIN_RANGE = {2.5,8.0,16.0,50.0};
	private final String[] RAIN_TYPE = {"小雨","中雨","大雨","暴雨"};
	private final Color[] RAIN_FILL = {Color.valueOf("#01bcb9"),
			Color.valueOf("#e1d249"),Color.valueOf("#e17516"),Color.RED};
	private final DecimalFormat numFmt_div = new DecimalFormat("0.0");
	
	//累计雨量表中间的空圆柱部分占据总高度的比例
	private double HEIGHT_FACT = 4.0/6.0;
	//累计雨量表中间的空圆柱部分占据总宽度的比例
	private double WIDTH_FACT = 1.0/8.0;

	private void redraw() {
		//清空所有节点
		this.getChildren().clear();
			
		final double width = getWidth() * WIDTH_FACT;
		final double height_total =getHeight() * HEIGHT_FACT;
		final double x = getWidth()/2.0 - width;
		final double y = getHeight()/2.0 - height_total/2.0;
		final double fnt_size = getHeight() *  0.05;
		{
			//分为几个不同的等分
			for(int i=0;i<4;i++) {
				double curr_y = y + i * (height_total/4);
				Rectangle rect_range = new Rectangle(x,curr_y,width,height_total/4);
				rect_range.setStroke(Color.WHITE);
				rect_range.setStrokeWidth(1);
				Stop[] stops_range = new Stop[] {
						new Stop(0.0, RAIN_FILL[3-i]),
						new Stop(1.0, Color.WHITE)
						};
				LinearGradient fill_range = new LinearGradient(0.0, 1.0, 1.0, 1.0, true, CycleMethod.NO_CYCLE, stops_range);
				rect_range.setFill(fill_range);
				this.getChildren().add(rect_range);
				//------------------------------------------------------
				double txt_y = curr_y + fnt_size * 2.0;
				Text txt_level = new Text(0,txt_y,RAIN_TYPE[3-i]);
				txt_level.setWrappingWidth(x-10);
				txt_level.setTextAlignment(TextAlignment.RIGHT);
				txt_level.setFont(Font.font("SimHei", FontWeight.BOLD, fnt_size));
				txt_level.setFill(RAIN_FILL[3-i]);
				this.getChildren().add(txt_level);
				//------------------------------------------------------
				//显示刻度范围
				double fnt_div_size = width * 0.5;
				Text txt_div = new Text(x+width+10,curr_y + fnt_div_size * 0.3,numFmt_div.format(RAIN_RANGE[3-i]));
				if(txt_div.getText().equals("50.0")) {
					txt_div.setText("......");
				}
				txt_div.setStroke(Color.LIGHTGRAY);
				txt_div.setFill(Color.LIGHTGRAY);
				txt_div.setFont(Font.font("SimHei", FontWeight.BOLD, fnt_div_size));
				txt_div.setStrokeWidth(0);
				this.getChildren().add(txt_div);
			}
			
			{
				//追加最后的刻度
				Text txt_div_last = new Text(x+width+10,y+height_total + fnt_size * 0.3,"0.0");
				txt_div_last.setStroke(Color.LIGHTGRAY);
				txt_div_last.setFill(Color.LIGHTGRAY);
				double fnt_div_size = width * 0.5;
				txt_div_last.setFont(Font.font("SimHei", FontWeight.BOLD, fnt_div_size));
				txt_div_last.setStrokeWidth(0);
				this.getChildren().add(txt_div_last);
			}
		}
		
		{
			//在上面绘制降水强度
			Text txt_title = new Text(0, y - fnt_size*1.5, "降水强度");
			txt_title.setStroke(Color.WHITE);
			txt_title.setFill(Color.WHITE);
			txt_title.setFont(Font.font("SimHei", FontWeight.BOLD, getWidth() * 0.1));
			txt_title.setStrokeWidth(1);
			txt_title.setWrappingWidth(getWidth());
			txt_title.setTextAlignment(TextAlignment.CENTER);
			this.getChildren().add(txt_title);
		}
		
		{
			//在下面绘制雨量的统计单位
			Text txt_unit = new Text(0, y+height_total+fnt_size*2, "单位：mm  ");
			txt_unit.setStroke(Color.WHITE);
			txt_unit.setFill(Color.WHITE);
			txt_unit.setFont(Font.font("SimHei", FontWeight.BOLD, getWidth() * 0.1));
			txt_unit.setStrokeWidth(1);
			txt_unit.setWrappingWidth(getWidth());
			txt_unit.setTextAlignment(TextAlignment.CENTER);
			this.getChildren().add(txt_unit);
		}
		
		double currStart = 0.0;
		for(int i=0;i<4;i++) {
			//System.out.println("Start: "+currStart+"; End: = " + RAIN_RANGE[i]);
			if(this.rainHour >= currStart && this.rainHour <= RAIN_RANGE[i]) {
				double radius = getWidth() * 0.1;
				double endX = (getWidth() - (x+width))/2.0 + x + width - radius;
				double offset_y = 0;
				if(this.rainHour >= 0.0 && this.rainHour <= 50.0) {
					offset_y = (height_total / 4.0) - (this.rainHour - currStart) / (RAIN_RANGE[i] - currStart) * (height_total / 4.0);
				}else if(this.rainHour > 50.0) {
					offset_y = y;
				}
				double div_y = y + (height_total / 4.0) * (3-i) + offset_y;
				Line line_curr = new Line(x+width, div_y, endX, div_y);
				line_curr.setStroke(Color.WHITE);
				line_curr.setStrokeWidth(3);
				line_curr.setFill(Color.WHITE);
				this.getChildren().add(line_curr);
				Circle circ_curr = new Circle(endX+radius, div_y, radius);
				circ_curr.setStroke(Color.WHITE);
				circ_curr.setStrokeWidth(3);
				circ_curr.setFill(null);
				this.getChildren().add(circ_curr);
				Text txt_val = new Text(endX+radius*0.25, div_y + radius*0.3, numFmt_div.format(rainHour));
				txt_val.setFill(Color.WHITE);
				txt_val.setStrokeWidth(0);
				txt_val.setFont(Font.font("SimHei", FontWeight.BOLD, radius * 0.8));
				this.getChildren().add(txt_val);
			}
			currStart = RAIN_RANGE[i];
		}
		
		
	}
	
	public void update(double val) {
		if(val >= 0.0 && val <= 250.0) {
			this.rainHour = val;
			redraw();
		}
	}
	
	public double getRainHour() {
		return rainHour;
	}

	public void setRainHour(double rainHour) {
		this.rainHour = rainHour;
	}
	
	public RainIntensity() {
		//-fx-border-style:dotted;
		String borderStyle = "-fx-background-color:transparent;-fx-border-color:white;-fx-border-radius:30;-fx-border-width:0.5;";
		this.setStyle(borderStyle);
		DropShadow borderEffect = new DropShadow(8, 0.0, 0.0, Color.GREY);
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
