package com.wtx.control;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import com.wtx.dao.DataBaseDao;
import com.wtx.model.MinuteData;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class CustomChart extends TabPane {
	//private int cache_hour = 90;
	private LineChart<Number,Number> chart_thp = null;		//��ʪѹ����ͼ
	private LineChart<Number,Number> chart_rain = null;	//��ˮǿ����״ͼ	
	//��ʾ��ʽһ��Ҫ��ʾ����Сʱ���ڵ�����
	private final int dispHours;
	//��ʾ��ʽ����Ҫ��ʾ��ʱ������
	private Date dispStart;
	private Date dispEnd;
	//�����¶ȷ�Χ
	private final double BOUND_TEMP_UP = 60.0;
	private final double BOUND_TEMP_DOWN = -40.0;
	//������ѹ�¶ȷ�Χ
	private final double BOUND_PRES_UP = 675.0;
	private final double BOUND_PRES_DOWN = 1050.0;
	
	//������ʾ���߱�����Χ(�¶ȡ�ʪ�ȡ���ѹ��ֵ������0-100������)
	private final double BOUND_DISP_RANGE = 100.0;
	//private final double FACT_DIV_DOWN = 0.0;
	
	private double initX;
    private double initY;
	
	private SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public Date getDispStart() {
		return dispStart;
	}
	public Date getDispEnd() {
		return dispEnd;
	}
	public int getDispHours() {
		return dispHours;
	}

	/**
	 * ������ʪѹ��ʾͼ��
	 */
	private void create_chart_thp() {
		Tab tab_thp = new Tab("��ʪѹ����");
		NumberAxis xAxis_thp = new NumberAxis();			//ʱ����
		Calendar cal_end = Calendar.getInstance();
		cal_end.setTime(this.dispEnd);
		xAxis_thp.setUpperBound(cal_end.get(Calendar.HOUR_OF_DAY));
		//��������Сʱ��
		long diff_hours = (this.dispEnd.getTime() - this.dispStart.getTime()) / (60 * 60 * 1000);
		xAxis_thp.setLowerBound(cal_end.get(Calendar.HOUR_OF_DAY) - diff_hours);
		xAxis_thp.setTickUnit(1);
		xAxis_thp.setAutoRanging(false);
		xAxis_thp.setTickLabelFill(Color.WHITE);
		xAxis_thp.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number num) {
				if(num.intValue() < 0) {
					return String.valueOf(diff_hours + num.intValue());
				}else if(num.intValue() >=0 && num.intValue() < 10) {
					return "0"+String.valueOf(num.intValue());
				}else if(num.intValue() >= 10){
					return String.valueOf(num.intValue());
				}else {
					return "Nan";
				}
			}
			
			@Override
			public Number fromString(String string) {
				return Integer.valueOf(string);
			}
		});
		
		NumberAxis yAxis = new NumberAxis();							//������
		yAxis.setTickLabelsVisible(false);
		yAxis.setTickMarkVisible(false);
		yAxis.setMinorTickVisible(false);
		chart_thp = new LineChart<Number,Number>(xAxis_thp, yAxis);
		chart_thp.setPrefWidth(300);
		tab_thp.setClosable(false);
		tab_thp.setContent(new BorderPane(chart_thp));
		this.getTabs().add(tab_thp);
	}
	
	private void create_timeline() {
		Timeline timeLine = new Timeline();
		timeLine.setCycleCount(Timeline.INDEFINITE);
		timeLine.setAutoReverse(true);
		//����һ��ˢ�����ݵ�Ƶ��
		//KeyFrame key1 = new KeyFrame(Duration.seconds(30),new EventHandler<ActionEvent>() {
		KeyFrame key1 = new KeyFrame(Duration.minutes(15),new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Calendar calc_div = Calendar.getInstance();
				calc_div.setTime(new Date());
				calc_div.set(Calendar.MINUTE, 0);
				calc_div.set(Calendar.SECOND, 0);
				calc_div.add(Calendar.HOUR_OF_DAY, 1);
				dispEnd = calc_div.getTime();
				calc_div.setTime(dispEnd);
				calc_div.set(Calendar.MINUTE, 0);
				calc_div.set(Calendar.SECOND, 0);
				calc_div.add(Calendar.HOUR_OF_DAY, 0-dispHours);
				dispStart = calc_div.getTime();
				//-------------------------------------------------------------
				//������ʪѹ��ʾ����
				NumberAxis xAxis_thp = (NumberAxis)chart_thp.getXAxis();
				xAxis_thp.setUpperBound(calc_div.get(Calendar.HOUR_OF_DAY));
				//��������Сʱ��
				final long offset_hours = (dispEnd.getTime() - dispStart.getTime()) / (60 * 60 * 1000);
				xAxis_thp.setLowerBound(calc_div.get(Calendar.HOUR_OF_DAY) - offset_hours);
				//-------------------------------------------------------------
				//����������ʾ����
				NumberAxis xAxis_rain = (NumberAxis)chart_rain.getXAxis();
				xAxis_rain.setUpperBound(calc_div.get(Calendar.HOUR_OF_DAY));
				xAxis_rain.setLowerBound(calc_div.get(Calendar.HOUR_OF_DAY) - offset_hours);
				//-------------------------------------------------------------
				System.out.println("������������: ");
				System.out.println(timeFmt.format(dispStart));
				System.out.println(timeFmt.format(dispEnd));
				
				load_chart_data();
			}
		});
		timeLine.getKeyFrames().add(key1);
		timeLine.play();
	}
	
	/**
	 * ���췽ʽһ
	 * Ĭ����ʾָ������Сʱ���ڵ�����
	 */
	public CustomChart(int hours) throws Exception {
		//��ʾ��Сʱ��
		this.dispHours = hours;
		this.dispEnd = new Date();		//��β�ǵ�ǰ��ʱ��
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dispEnd);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.add(Calendar.HOUR_OF_DAY, 1);	//��ʾ���������һ��Сʱ
		this.dispEnd = calendar.getTime();
		calendar.add(Calendar.HOUR_OF_DAY, 0-this.dispHours);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		this.dispStart = calendar.getTime();
		
//		System.out.println("Start: " + timeFmt.format(dispStart));
//		System.out.println("End: " + timeFmt.format(dispEnd));
		
		//��֤��ʾͼ���ʱ������
		calendar.setTime(dispEnd);
		if(calendar.get(Calendar.MINUTE) != 0) {
			throw new Exception("����ʱ�������0��0�� !");
		}
		if(calendar.get(Calendar.SECOND) != 0) {
			throw new Exception("����ʱ�������0��0�� !");
		}
		calendar.setTime(dispStart);
		if(calendar.get(Calendar.MINUTE) != 0) {
			throw new Exception("��ʼʱ�������0��0�� !");
		}
		if(calendar.get(Calendar.SECOND) != 0) {
			throw new Exception("��ʼʱ�������0��0�� !");
		}
		
		this.setSide(Side.BOTTOM);
		//������ʪѹ��ʾͼ��
		create_chart_thp();
		//������ˮ����״ͼ
		create_chart_rain();
		//������������
		create_timeline();
	}
	
	/**
	 * �������ק�¼�,ʹ����λ�ÿ��Ա��ƶ�
	 */
	public void bindDragEvent() {
		Window wnd_parent = this.getScene().getWindow();
		if(wnd_parent != null && wnd_parent instanceof Stage) {
			Stage primaryStage = (Stage)wnd_parent;
			this.setOnMouseDragged((MouseEvent me) -> {
				primaryStage.setX(me.getScreenX() - initX);
				primaryStage.setY(me.getScreenY() - initY);
			});
			this.setOnMousePressed((MouseEvent me) -> {
				initX = me.getScreenX() - primaryStage.getX();
	            initY = me.getScreenY() - primaryStage.getY();
			});
		}
	}
	
	/**
	 * ���췽ʽ��
	 * ��ʾָ��ʱ�䷶Χ�ڵ�����
	 */
	public CustomChart(Date start,Date end) throws Exception {
		//��ʾ��ʱ�䷶Χ
		this.dispHours = 0;
		this.dispStart = start;
		this.dispEnd = end;
		
		//��֤��ʾͼ���ʱ������
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dispEnd);
		if(calendar.get(Calendar.MINUTE) != 0) {
			throw new Exception("����ʱ�������0��0�� !");
		}
		if(calendar.get(Calendar.SECOND) != 0) {
			throw new Exception("����ʱ�������0��0�� !");
		}
		calendar.setTime(dispStart);
		if(calendar.get(Calendar.MINUTE) != 0) {
			throw new Exception("��ʼʱ�������0��0�� !");
		}
		if(calendar.get(Calendar.SECOND) != 0) {
			throw new Exception("��ʼʱ�������0��0�� !");
		}
		
		this.setSide(Side.BOTTOM);
		
		//������ʪѹ��ʾͼ��
		create_chart_thp();
		//������ˮ����״ͼ
		create_chart_rain();
		//��ʽ��,������ֱ�Ӽ�������
		load_chart_data();
	}
	
	/**
	 * ������ֱ�Ӽ�����ʷ����
	 */
	private void load_chart_data() {
		//���ԭ�е�ϵ��
		try {
			chart_thp.getData().clear();
			chart_rain.getData().clear();
			//��������Сʱ��
			//-------------------------------------------
			XYChart.Series<Number, Number> series_temp = new XYChart.Series<Number, Number>();
			series_temp.setName("�¶�");
			chart_thp.getData().add(series_temp);
			//-------------------------------------------
			XYChart.Series<Number, Number> series_humd = new XYChart.Series<Number, Number>();
			series_humd.setName("ʪ��");
			chart_thp.getData().add(series_humd);
			//-------------------------------------------
			XYChart.Series<Number, Number> series_press = new XYChart.Series<Number, Number>();
			series_press.setName("��ѹ");
			chart_thp.getData().add(series_press);
			//-------------------------------------------
			XYChart.Series<Number, Number> serials_rain = new XYChart.Series<>();
			serials_rain.setName("����");
			chart_rain.getData().add(serials_rain);
			//-------------------------------------------
			DataBaseDao dao = DataBaseDao.getInstance();
			Set<MinuteData> dataset = dao.load_mdata(dispStart, dispEnd);
			Iterator<MinuteData> dataItert = dataset.iterator();
			MinuteData cacheLast = null;
			int lastHour = 90;
			Calendar cal_last = Calendar.getInstance();
			while(dataItert.hasNext()) {
				MinuteData mdata = dataItert.next();
				Calendar currCal = Calendar.getInstance();
				currCal.setTime(mdata.getTime());
				if(lastHour != currCal.get(Calendar.HOUR_OF_DAY) && cacheLast != null) {
					//System.out.println("Point: " + mdata.getTime());
					//��Сʱ��Ϊ������������
					//long diff_hours = (dispEnd.getTime() - cacheLast.getTime().getTime()) / (60 * 60 * 1000);
					long diff_hours = (dispEnd.getTime() - mdata.getTime().getTime()) / (60 * 60 * 1000);
					cal_last.setTime(dispEnd);
					//���������
					double x = cal_last.get(Calendar.HOUR_OF_DAY) - diff_hours;
					{
						/**
						 * �¶���ʾ��Χ:-40 ~ 60 
						 * ������Ҫת��Ϊ0-100�Ŀ̶�
						 */
						double y = (cacheLast.getTempreature() + (0-BOUND_TEMP_DOWN)) 
								/ (BOUND_TEMP_UP - BOUND_TEMP_DOWN) * BOUND_DISP_RANGE;
						series_temp.getData().add(new Data<Number, Number>(x,y));
					}
					
					{
						/**
						 * ʪ����ʾ��Χ0-100
						 */
						double y = cacheLast.getHumidity();
						series_humd.getData().add(new Data<Number, Number>(x,y));
					}
					
					{
						/**
						 * ��ѹ��ʾ��Χ:500.0 ~ 1100.0 
						 * ������Ҫת��Ϊ0-100�Ŀ̶�
						 */
						double y = (cacheLast.getPressure() - BOUND_PRES_DOWN) / (BOUND_PRES_UP - BOUND_PRES_DOWN) * 100;
						series_press.getData().add(new Data<Number, Number>(x, y));
					}
					
					{
						/**
						 * ����ȡÿСʱ������������
						 */
						Calendar cal_rain_time = Calendar.getInstance();
						cal_rain_time.setTime(cacheLast.getTime());
						XYChart.Data<Number, Number> dataCurr = new XYChart.Data<>(x,cacheLast.getRain_hour());
						serials_rain.getData().add(dataCurr);
						
					}
				}
				lastHour = currCal.get(Calendar.HOUR_OF_DAY);
				cacheLast = mdata;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//������ˮ����״ͼ
	private void create_chart_rain() {
		try {
			Tab tab_rain = new Tab("��ˮ����ͼ");
			NumberAxis xAxis_rain = new NumberAxis();
			Calendar cal_end = Calendar.getInstance();
			cal_end.setTime(this.dispEnd);
			xAxis_rain.setUpperBound(cal_end.get(Calendar.HOUR_OF_DAY));
			//��������Сʱ��
			long diff_hours = (this.dispEnd.getTime() - this.dispStart.getTime()) / (60 * 60 * 1000);
			xAxis_rain.setLowerBound(cal_end.get(Calendar.HOUR_OF_DAY) - diff_hours);
			xAxis_rain.setTickUnit(1);
			xAxis_rain.setAutoRanging(false);
			xAxis_rain.setTickLabelFill(Color.WHITE);
			xAxis_rain.setTickLabelFormatter(new StringConverter<Number>() {
				@Override
				public String toString(Number num) {
					if(num.intValue() < 0) {
						return String.valueOf(diff_hours + num.intValue());
					}else if(num.intValue() >=0 && num.intValue() < 10) {
						return "0"+String.valueOf(num.intValue());
					}else if(num.intValue() >= 10){
						return String.valueOf(num.intValue());
					}else {
						return "Nan";
					}
				}
				@Override
				public Number fromString(String string) {
					return Integer.valueOf(string);
				}
			});
			//��������Сʱ��
			NumberAxis yAxis = new NumberAxis();							//������
			yAxis.setTickLabelFill(Color.WHITE);
			chart_rain = new LineChart<Number,Number>(xAxis_rain, yAxis);
			//---------------------------------------------------------
			chart_rain.setPrefWidth(300);
			chart_rain.setLegendVisible(false);
			tab_rain.setClosable(false);
			tab_rain.setContent(chart_rain);
			this.getTabs().add(tab_rain);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
