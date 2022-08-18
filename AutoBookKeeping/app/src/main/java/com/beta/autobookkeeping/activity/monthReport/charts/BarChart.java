package com.beta.autobookkeeping.activity.monthReport.charts;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.beta.autobookkeeping.R;
import com.github.mikephil.charting.animation.Easing;
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

import Util.ProjectUtil;

public class BarChart {
    private BarDataSet moneyDataSet;
    private final List<BarEntry> moneyEntry = new ArrayList<BarEntry>();
    public ArrayList<String> monthLabels = new ArrayList<String>();
    public ArrayList<Float> monthCosts;

    private com.github.mikephil.charting.charts.BarChart monthMoneyBarChart;

    private Context context;
    private Activity activity;

    public BarChart(Context context,com.github.mikephil.charting.charts.BarChart monthMoneyBarChart,ArrayList<Float> monthCosts){
        this.context = context;
        this.monthMoneyBarChart = monthMoneyBarChart;
        this.activity = (Activity) context;
        this.monthCosts = monthCosts;
    }

    //显示柱状图的方法
    public void showBarChart(){
        //将moneyEntry的值先去到
        setBarDataSetDataMoney(monthCosts);
        //创建第一个变量总收支的图
        moneyDataSet = new BarDataSet(moneyEntry,"总收支");
        //设置这个变量柱子的基本参数
        int[] colors = new int[moneyEntry.size()];
        for (int i = 0; i < moneyEntry.size(); i++) {
            colors[i] = (moneyEntry.get(i).getY()>0? Color.GREEN:Color.RED);
        }
        moneyDataSet.setColors(colors,80);
        //创建变量组
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(moneyDataSet);
        BarData data = new BarData(dataSets);
        data.setBarWidth(0.3f);
        //将变量组交给图
        monthMoneyBarChart.setData(data);
        //显示柱状图
        setBarChart(monthMoneyBarChart);
    }

    //柱状图格式的设置
    public void setBarChart(com.github.mikephil.charting.charts.BarChart barChart){
        XAxis xAxis = barChart.getXAxis();
        YAxis leftAxis = barChart.getAxisLeft();
        YAxis rightAxis = barChart.getAxisRight();
        //取消左右y轴显示
        leftAxis.setEnabled(false);
        rightAxis.setEnabled(false);
        //背景颜色
        barChart.setBackgroundColor(context.getColor(R.color.reverse_primary_font));
        //不显示图表网格
        barChart.setDrawGridBackground(true);
        //背景阴影
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        //显示边框
        barChart.setDrawBorders(false);
        barChart.setTouchEnabled(false);
        barChart.notifyDataSetChanged();
        barChart.setFitBars(true);
        //设置动画效果
        barChart.animateY(500, Easing.Linear);
        barChart.animateX(500, Easing.Linear);
        //不显示右下角描述内容
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);
        //不显示X轴网格线
        xAxis.setDrawGridLines(false);
        //X轴设置显示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //X轴横坐标显示的数量
        xAxis.setLabelCount(12,true);

        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value+0.5 >= monthLabels.size() || value+0.5 < 0? "" : monthLabels.get((int)(value+0.5));
            }
        });
    }

    //设置柱状图的数据
    public void setBarDataSetDataMoney(ArrayList<Float> monthCosts){
        int recordMonth = ProjectUtil.getCurrentMonth(),recordYear = ProjectUtil.getCurrentYear();
        //获取数据
        for(int i = 0;i < monthCosts.size();i++){
            moneyEntry.add(new BarEntry(i,monthCosts.get(i)));
            monthLabels.add(String.valueOf(recordMonth));
            //更新要查询的月份
            if(recordMonth==1){
                recordMonth = 12;
                recordYear-=1;
            }else{
                recordMonth-=1;
            }
        }
    }
}
