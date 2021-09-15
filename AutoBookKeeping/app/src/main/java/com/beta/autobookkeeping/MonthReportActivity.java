package com.beta.autobookkeeping;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

import Util.Util;

public class MonthReportActivity extends AppCompatActivity {
    private Button btn_right_choose,btn_left_choose;
    private BarChart monthMoneyBarChart;
    //保存数据的实体
    private ArrayList<BarEntry> moneyEntry = new ArrayList<>();
    //数据的集合
    private BarDataSet moneyDataSet;
    //表格下方的月份文字
    public ArrayList<String> monthLabels = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_report);
        //获取各个组件
        getViews();
        showBarChart();
    }

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

    public void showBarChart(){
        moneyEntry.add(0,new BarEntry(4,10));
        moneyEntry.add(1,new BarEntry(3,10));
        moneyDataSet = new BarDataSet(moneyEntry,"收支情况");
        monthLabels.add("测试一");
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(moneyDataSet);
        BarData data = new BarData(dataSets);
        monthMoneyBarChart.setData(data);
        setBarChart(monthMoneyBarChart);
    }

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
        ValueFormatter valueFormatter = new ValueFormatter() {
            private final String[] xLableList = new String[]{"110\n报警", "120\n报警", "119\n报警", "110\n报警", "120\n报警",
                    "119\n报警", "110\n报警", "120\n报警", "119\n报警"};

            @Override
            public String getFormattedValue(float value) {
                if (value >= 0) {
                    return "fsfa";
                } else {
                    return "";
                }
            }
        };
        xAxis.setValueFormatter(valueFormatter);
    }

    public void setBarDataSet(BarDataSet barDataSet, int color){
        barDataSet.setColor(color);
        barDataSet.setFormLineWidth(1f);
        barDataSet.setFormSize(15.f);
        //显示柱状图顶部值
        barDataSet.setDrawValues(true);
    }
}