package com.beta.autobookkeeping;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.icu.text.Transliterator;
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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

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
    private  ArrayList<PieEntry> costEntry = new ArrayList<>();
    //数据的集合
    private BarDataSet moneyDataSet;
    private PieDataSet costDataSet;
    //表格下方的月份文字
    public ArrayList<String> monthLabels = new ArrayList<String>();
    public ArrayList<String> costLabels = new ArrayList<>();
    public ArrayList<Float> costMoney = new ArrayList<>();
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
                    refreshPieChart();
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
                refreshPieChart();
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
        moneyDataSet.setColor(Color.parseColor("#5091F3"));
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

    //饼状图格式的设置和点击事件
    public void setPieChart(PieChart pieChart){
        //设置图表描述
        Description description = new Description();
        description.setText("单位:元");
        pieChart.setDescription(description);
        //设置使用百分比
        pieChart.setUsePercentValues(true);
        setPieDataSetDataMoney(recordMonth,recordYear);
        //设置中心有空圆
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(60f);
        //中心可以加字
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("总支出:\n"+String.format("%.1f",Util.getMonthCost(recordYear,recordMonth,MonthReportActivity.this)));
        pieChart.setCenterTextSize(25f);
        //设置饼状图的颜色
        costDataSet.setColors(new int[]{Color.rgb(181, 194, 202), Color.rgb(129, 216, 200), Color.rgb(241, 214, 145),
                Color.rgb(108, 176, 223), Color.rgb(195, 221, 155), Color.rgb(251, 215, 191),
                Color.rgb(237, 189, 189), Color.rgb(172, 217, 243)});
        //图例设置
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);//是否显示图例
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);//图例相对于图表横向的位置
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);//图例相对于图表纵向的位置
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);//图例显示的方向
        legend.setDrawInside(false);
        legend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        //设置数据
        PieData pieData = new PieData(costDataSet);
        //可以旋转
        pieChart.setRotationEnabled(true);
        //设置饼块的点击事件
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                //点击时显示此时的具体金额
                PieEntry pe= (PieEntry) e;
                if(!pe.getLabel().contains(String.valueOf(e.getY())))
                    pe.setLabel(pe.getLabel()+String.valueOf(e.getY()));
            }

            @Override
            public void onNothingSelected() {
                //取消点击时去除此时的具体金额
                refreshPieChart();
            }
        });
        //启动pieChart
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
        //X轴横坐标显示的数量
        xAxis.setLabelCount(9,true);
        //X轴最大坐标
        xAxis.setAxisMaximum(7.5f);

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
        //获取本月中所有消费的具体值
        costMoney = Util.getCostTypeAndMoney(recordMonth,recordYear,MonthReportActivity.this).get(1);
        //创建Entry
        for(int i = 0;i < costLabels.size();i++){
            costEntry.add(new PieEntry(0.0f-costMoney.get(i),costLabels.get(i)));
        }
        //将获取到的数据写入DataSet
        costDataSet = new PieDataSet(costEntry,"");
        //设置将百分比显示在外面
        costDataSet.setValueLinePart1OffsetPercentage(80f);
        costDataSet.setValueLinePart1Length(0.3f);
        costDataSet.setValueLinePart2Length(0.5f);
        costDataSet.setValueLineColor(Color.BLACK);//设置连接线的颜色
        costDataSet.setValueTextSize(12);
        costDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        costDataSet.setValueFormatter(new PercentFormatter(monthMoneyPieChart));
        //设置各个饼块的间隔
        costDataSet.setSliceSpace(1f);
    }

    ///刷新显示饼状图
    public void refreshPieChart(){
        costDataSet = null;
        costLabels = null;
        costEntry.clear();
        costMoney = null;
        showPieChart();
    }
}