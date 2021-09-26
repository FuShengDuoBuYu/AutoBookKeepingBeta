package com.beta.autobookkeeping;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.beta.autobookkeeping.SMStools.SMSDataBase;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.List;

import Util.Util;

public class MonthReportActivity extends AppCompatActivity {
    private Button btn_right_choose,btn_left_choose;
    private BarChart monthMoneyBarChart;
    private PieChart monthMoneyPieChart;
    private TextView tv_month_report_money,tv_month_report_time;
    //当前页面查看的月份
    int recordYear = Util.getCurrentYear();
    int recordMonth = Util.getCurrentMonth();
    SMSDataBase smsDb = new SMSDataBase(MonthReportActivity.this,"orderInfo",null,1);
    SQLiteDatabase db;
    //保存数据的实体
    private final List<BarEntry> moneyEntry = new ArrayList<BarEntry>();
    private final ArrayList<PieEntry> costEntry = new ArrayList<>();
    //数据的集合
    private BarDataSet moneyDataSet;
    private PieDataSet costDataSet;
    //表格下方的月份文字
    public ArrayList<String> monthLabels = new ArrayList<String>();
    public ArrayList<String> costLabels = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = smsDb.getWritableDatabase();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_report);
        //获取各个组件
        getViews();
        //先更新总收支数据
        tv_month_report_money.setText(String.format("%.1f",Util.getMonthMoney(recordYear,recordMonth,MonthReportActivity.this)));
        showBarChart();
        showPieChart();
    }

    //获取views的控件
    public void getViews(){
        btn_left_choose = findViewById(R.id.btn_left_choose);
        btn_right_choose = findViewById(R.id.btn_right_choose);
        monthMoneyBarChart = findViewById(R.id.bar_chart_month_money);
        monthMoneyPieChart = findViewById(R.id.pie_chart_month_money);
        tv_month_report_money = findViewById(R.id.tv_month_report_money);
        tv_month_report_time = findViewById(R.id.tv_month_report_time);
        btn_right_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recordMonth+1>Util.getCurrentMonth()&&recordYear==Util.getCurrentYear()){
                    Util.toastMsg(MonthReportActivity.this,"不能选择未发生的时间");
                }else {
                    //更改要查询年和月
                    if (recordMonth == 12) {
                        recordMonth = 1;
                        recordYear += 1;
                    } else {
                        recordMonth++;
                    }
                    //更新文字
                    tv_month_report_time.setText(String.valueOf(recordYear) + "年" + String.valueOf(recordMonth) + "月");
                    //更新月份的总收支
                    tv_month_report_money.setText(String.format("%.1f", Util.getMonthMoney(recordYear, recordMonth, MonthReportActivity.this)));
                }
            }
        });
        btn_left_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //更改要查询年和月
                if(recordMonth==1){
                    recordMonth = 12;
                    recordYear-=1;
                }else{
                    recordMonth--;
                }
                //更新文字
                tv_month_report_time.setText(String.valueOf(recordYear)+"年"+String.valueOf(recordMonth)+"月");
                //更新月份的总收支
                tv_month_report_money.setText(String.format("%.1f",Util.getMonthMoney(recordYear,recordMonth,MonthReportActivity.this)));
            }
        });
    }

    //显示柱状图的方法
    public void showBarChart(){
        //将moneyEntry的值先去到
        setBarDataSetDataMoney(recordMonth,recordYear);
        moneyEntry.add(new BarEntry(-0.5f,0));
        //创建第一个变量总收支的图
        moneyDataSet = new BarDataSet(moneyEntry,"总收支");
        //设置这个变量柱子的基本参数
        moneyDataSet.setColor(Color.rgb(187,255,255));
        moneyDataSet.setDrawValues(true);

        //创建变量组
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(moneyDataSet);
        BarData data = new BarData(dataSets);
        //将变量组交给图
        monthMoneyBarChart.setData(data);
        //显示柱状图
        setBarChart(monthMoneyBarChart);
    }

    //显示饼状图的方法
    public void showPieChart(){

        setPieChart(monthMoneyPieChart);
    }

    //饼状图格式的设置
    public void setPieChart(PieChart pieChart){
        //设置图表描述
        Description description = new Description();
        description.setText("支出占比");
        pieChart.setDescription(description);
        //设置使用百分比
        pieChart.setUsePercentValues(true);
        setPieDataSetDataMoney(recordMonth,recordYear);
        //设置中心无空圆
        pieChart.setHoleRadius(0);
        //设置饼状图的颜色
        costDataSet.setColors(new int[]{Color.rgb(181, 194, 202), Color.rgb(129, 216, 200), Color.rgb(241, 214, 145),

                Color.rgb(108, 176, 223), Color.rgb(195, 221, 155), Color.rgb(251, 215, 191),

                Color.rgb(237, 189, 189), Color.rgb(172, 217, 243)});
        //设置
        PieData pieData = new PieData(costDataSet);
        pieChart.setData(pieData);
        monthMoneyPieChart.setData(pieData);
    }

    //柱状图格式的设置
    public void setBarChart(BarChart barChart){
        XAxis xAxis = barChart.getXAxis();
        YAxis leftAxis = barChart.getAxisLeft();
        YAxis rightAxis = barChart.getAxisRight();
        //取消左右y轴显示
        leftAxis.setEnabled(false);
        rightAxis.setEnabled(false);
        //背景颜色
        barChart.setBackgroundColor(Color.WHITE);
        //不显示图表网格
        barChart.setDrawGridBackground(true);
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
        //X轴最大坐标
        xAxis.setAxisMaximum(7.5f);
        //X轴最小坐标
//        xAxis.setAxisMinimum(-0.5f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value+0.5 >= monthLabels.size() || value+0.5 < 0? "" : monthLabels.get((int)(value+0.5));
            }
        });
    }

    //设置柱状图的数据
    public void setBarDataSetDataMoney(int recordMonth,int recordYear){
        int recordMonth2 = recordMonth,recordYear2 = recordYear;
        //依次查取各个月的收支数据
        double monthCost=0.0;
        //x轴柱的下标
        int index = 0;
        while (true){
            //查出某个月的收支总和
            monthCost=Util.getMonthMoney(recordYear2,recordMonth2,MonthReportActivity.this);
            //如果某个月收支为0,说明为起始月份
            if(monthCost==0.0){
                break;
            }
            //否则将收支总和写入记录的list
            else{
                moneyEntry.add(new BarEntry(index++,(float)monthCost));

                monthLabels.add(String.valueOf(recordYear2)+"/"+String.valueOf(recordMonth2)+"");
            }
            //更新要查询的月份
            if(recordMonth2==1){
                recordMonth2 = 9;
                recordYear2-=1;
            }else{
                recordMonth2-=1;
            }
        }
    }

    //设置柱状图的数据
    public void setPieDataSetDataMoney(int recordMonth,int recordYear){


        //获取本月中有消费的类型
        costLabels = Util.getCostTypeAndMoney(recordMonth,recordYear,MonthReportActivity.this).get(0);
        costEntry.add(new PieEntry(10,"测试1"));
        costEntry.add(new PieEntry(20,"测试2"));
        costDataSet = new PieDataSet(costEntry,"");
    }
}