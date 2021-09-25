package com.beta.autobookkeeping;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.beta.autobookkeeping.SMStools.SMSDataBase;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.List;

import Util.Util;

public class MonthReportActivity extends AppCompatActivity {
    private Button btn_right_choose,btn_left_choose;
    private BarChart monthMoneyBarChart;
    //当前页面查看的月份
    int recordYear = Util.getCurrentYear();
    int recordMonth = Util.getCurrentMonth();
    SMSDataBase smsDb = new SMSDataBase(MonthReportActivity.this,"orderInfo",null,1);
    SQLiteDatabase db;
    //保存数据的实体
    private final List<BarEntry> moneyEntry = new ArrayList<BarEntry>();
    //数据的集合
    private BarDataSet moneyDataSet;
    //表格下方的月份文字
    public ArrayList<String> monthLabels = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = smsDb.getWritableDatabase();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_report);
        //获取各个组件
        getViews();
        showBarChart();
    }

    //获取views的控件
    public void getViews(){
        btn_left_choose = findViewById(R.id.btn_left_choose);
        btn_right_choose = findViewById(R.id.btn_right_choose);
        monthMoneyBarChart = findViewById(R.id.bar_chart_month_money);
        btn_left_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.toastMsg(MonthReportActivity.this,"左点击成功");
            }
        });
        btn_right_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.toastMsg(MonthReportActivity.this,"右点击成功");
            }
        });
    }

    //显示柱状图的方法
    public void showBarChart(){
        setBarDataSet();
        moneyDataSet = new BarDataSet(moneyEntry,"总收支");
        moneyEntry.add(new BarEntry(1,10));
        moneyDataSet.setColor(Color.rgb(187,255,255));
        moneyDataSet.setFormLineWidth(1f);
        moneyDataSet.setDrawValues(true);
        monthLabels.add("test");
        monthLabels.add("test2");
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(moneyDataSet);
        BarData data = new BarData(dataSets);
        monthMoneyBarChart.setData(data);
        setBarChart(monthMoneyBarChart);
        Util.toastMsg(MonthReportActivity.this,String.valueOf(moneyEntry.size()));
    }

    //柱状图格式的设置
    public void setBarChart(BarChart barChart){
        XAxis xAxis = barChart.getXAxis();
        YAxis leftAxis = barChart.getAxisLeft();
        YAxis rightAxis = barChart.getAxisRight();
        //取消左右y轴显示
        leftAxis.setEnabled(false);
        rightAxis.setEnabled(true);
        leftAxis.setCenterAxisLabels(true);
        leftAxis.setUseAutoScaleMinRestriction(true);
        leftAxis.setStartAtZero(false);
        //背景颜色
        barChart.setBackgroundColor(Color.WHITE);
        //不显示图表网格
        barChart.setDrawGridBackground(false);
        //背景阴影
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        //显示边框
        barChart.setDrawBorders(false);
        //设置动画效果
        barChart.animateY(1000, Easing.Linear);
        barChart.animateX(1000, Easing.Linear);
        //不显示右下角描述内容
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);
        //不显示X轴网格线
        xAxis.setDrawGridLines(false);
        //X轴设置显示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // X轴显示Value值的精度，与自定义X轴返回的Value值精度一致
//        xAxis.setGranularity(1f);
        //X轴横坐标显示的数量
        xAxis.setLabelCount(9,true);
//        xAxis.setLabelCount(5);
        //X轴最大坐标
        xAxis.setAxisMaximum(8);
//        xAxis.setAxisMaximum(4);
//        xAxis.set
        //X轴最小坐标
        xAxis.setAxisMinimum(0);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
//                Log.d("value",String.valueOf(value));
                return value >= monthLabels.size() || value < 0? "" : monthLabels.get((int)value);
            }
        });
    }

    //设置柱状图的数据
    public void setBarDataSet(){

        //依次查取各个月的收支数据
        double monthCost=0.0;
        //x轴柱的下标
        int index = 0;
        while (true){
            //查出某个月的收支总和
            monthCost=Util.getMonthMoney(recordYear,recordMonth,MonthReportActivity.this);
            //如果某个月收支为0,说明为起始月份
            if(monthCost==0.0){
                break;
            }
            //否则将收支总和写入记录的list
            else{
                moneyEntry.add(new BarEntry(index++,(float)monthCost));
                monthLabels.add(String.valueOf(recordYear)+"/"+String.valueOf(recordMonth)+"");
            }
            //更新要查询的月份
            if(recordMonth==1){
                recordMonth = 9;
                recordYear-=1;
            }else{
                recordMonth-=1;
            }
        }
    }
}