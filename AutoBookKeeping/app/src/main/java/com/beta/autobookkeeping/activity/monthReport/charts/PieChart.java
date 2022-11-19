package com.beta.autobookkeeping.activity.monthReport.charts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.activity.monthReport.MonthReportActivity;
import com.beta.autobookkeeping.activity.orderItemSearch.OrderItemSearchActivity;
import com.beta.autobookkeeping.activity.orderItemSearch.items.SearchConditionEntity;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

import Util.ProjectUtil;

public class PieChart {
    private com.github.mikephil.charting.charts.PieChart monthMoneyPieChart;
    private Context context;
    private Activity activity;
    private int recordMonth,recordYear;
    private ArrayList<PieEntry> costEntry = new ArrayList<>();
    private final int[] dataColor = ProjectUtil.colors;
    public ArrayList<String> costLabels = new ArrayList<>();
    public ArrayList<Float> costMoney = new ArrayList<>();
    private PieDataSet costDataSet;
    private LinearLayout costRankingProcessBar;
    private ArrayList<OrderInfo> monthOrders;

    public PieChart(com.github.mikephil.charting.charts.PieChart monthMoneyPieChart,Context context,int recordMonth,int recordYear,ArrayList<OrderInfo> monthOrders,LinearLayout costRankingProcessBar){
        this.context = context;
        this.monthMoneyPieChart = monthMoneyPieChart;
        this.activity = (Activity) context;
        this.recordMonth = recordMonth;
        this.recordYear = recordYear;
        this.monthOrders = monthOrders;
        this.costRankingProcessBar = costRankingProcessBar;
    }
    //显示饼状图的方法
    public void showPieChart(){
        setPieChart(monthMoneyPieChart);
    }

    //饼状图格式的设置和点击事件
    public void setPieChart(com.github.mikephil.charting.charts.PieChart pieChart){
        //设置图表描述
        Description description = new Description();
        description.setTextColor(context.getColor(R.color.primary_font));
        description.setText("单位:元");
        pieChart.setDescription(description);
        //设置使用百分比
        pieChart.setUsePercentValues(true);
        setPieDataSetDataMoney();
        //设置动画
        pieChart.animateXY(500,500);
        //设置中心有空圆
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(60f);
        //中心可以加字
        pieChart.setDrawCenterText(true);
//        设置中心的文字
        double totalMoney = 0.0;
        //通过monthOrders获取总金额
        for (OrderInfo orderInfo:monthOrders){
            totalMoney += orderInfo.getMoney();
        }
        if(totalMoney==0.0){
            pieChart.setCenterText("暂无数据");
        }else{
            pieChart.setCenterText("总支出:\n"+String.format("%.1f", totalMoney)+"元");
        }
        pieChart.setCenterTextSize(23f);
        pieChart.setCenterTextColor(context.getColor(R.color.primary_font));
        //设置中心背景颜色为透明
        pieChart.setHoleColor(0);
        //设置饼状图的颜色
        costDataSet.setColors(dataColor,150);
        //图例设置
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);//是否显示图例
        legend.setTextColor(context.getColor(R.color.primary_font));
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
                refreshPieChartAndRanking(recordYear,recordMonth,monthOrders);
            }
        });
        //启动pieChart
        pieChart.setData(pieData);
        monthMoneyPieChart.setData(pieData);
    }

    //设置柱状图的数据
    public void setPieDataSetDataMoney(){
        //获取本月中有消费的类型
        costLabels = ProjectUtil.getCostTypeAndMoney(monthOrders).get(0);
        //获取本月中所有消费的具体值
        costMoney = ProjectUtil.getCostTypeAndMoney(monthOrders).get(1);
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
        costDataSet.setValueLineColor(context.getColor(R.color.primary_font));//设置连接线的颜色
        costDataSet.setValueTextSize(12);
        costDataSet.setValueTextColor(context.getColor(R.color.primary_font));
        costDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        costDataSet.setValueFormatter(new PercentFormatter(monthMoneyPieChart));
        //设置各个饼块的间隔
        costDataSet.setSliceSpace(1f);
    }

    ///刷新显示饼状图
    public void refreshPieChartAndRanking(int recordYear,int recordMonth,ArrayList<OrderInfo> monthOrders){
        this.monthOrders = monthOrders;
        this.recordYear = recordYear;
        this.recordMonth = recordMonth;
        costDataSet = null;
        costLabels = null;
        costEntry.clear();
        costMoney = null;
        monthMoneyPieChart.removeAllViews();
        costRankingProcessBar.removeAllViews();

        showPieChart();
        showMonthlyCostRanking(monthOrders);
    }

    //动态显示月度消费排行榜
    public void showMonthlyCostRanking(ArrayList<OrderInfo> monthOrders){
        double totalCostMoney = 0.0;
        //通过monthOrders获取总支出金额
        for (OrderInfo orderInfo:monthOrders){
            totalCostMoney += orderInfo.getMoney();
        }
        //支出
        for(int i = 0;i < costLabels.size();i++){
            //要加入的进度条
            LinearLayout costProcessBar = setCostProcessBar(costLabels.get(i),costMoney.get(i), totalCostMoney,i);
            //点击进度条进入具体支出项目查询
            //由于内部类,故使用final的i
            int finalI = i;
            costProcessBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showCostItems(costLabels.get(finalI),recordYear,recordMonth);
                }
            });
            //加入显示
            costRankingProcessBar.addView(costProcessBar);
        }
        //收入
        LinearLayout incomeProcessBar = setCostProcessBar("点击查看收入明细",1f,1f,costLabels.size());
        incomeProcessBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCostItems("收入",recordYear,recordMonth);
            }
        });
        costRankingProcessBar.addView(incomeProcessBar);
    }

    //创建一个processBar对象,显示某一个类别的消费支出
    public LinearLayout setCostProcessBar(String category,float cost,double sumCost,int colorIndex){
        LinearLayout rankingItem = new LinearLayout(context);
        rankingItem.setOrientation(LinearLayout.VERTICAL);
        //先把该item的信息加上的子布局
        LinearLayout itemInfo = new LinearLayout(context);
        //排行进度条
        ProgressBar progressBar = new ProgressBar(context,null,android.R.attr.progressBarStyleHorizontal);
        //item信息子布局的两个小布局
        TextView tvCategory = new TextView(context);
        TextView tvCost = new TextView(context);
        //设置两个小布局
        tvCategory.setText(category);
        tvCost.setText(cost+"元");
        tvCategory.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1));
        tvCost.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1));
        tvCategory.setPadding(40,10,10,30);
        tvCost.setPadding(10,10,40,30);
        tvCategory.setGravity(Gravity.START);
        tvCost.setGravity(Gravity.END);
        //将小布局加入子布局
        itemInfo.addView(tvCategory);
        itemInfo.addView(tvCost);
        //设置进度条
        progressBar.setProgress((int)(((0-cost)*100)/(0-sumCost)));
        progressBar.setPadding(40,10,40,50);
        //准备progressBar带圆角的背景Drawable
        GradientDrawable progressBg = new GradientDrawable();
        //设置圆角弧度
        progressBg.setCornerRadius(30);
        //设置绘制颜色
        progressBg.setColor(Color.rgb(217,208,208));
        //准备progressBar带圆角的进度条Drawable
        GradientDrawable progressContent = new GradientDrawable();
        progressContent.setCornerRadius(30);
        //设置绘制颜色，此处可以自己获取不同的颜色
        progressContent.setColor(dataColor[colorIndex]);

        //ClipDrawable是对一个Drawable进行剪切操作，可以控制这个drawable的剪切区域，以及相相对于容器的对齐方式
        ClipDrawable progressClip = new ClipDrawable(progressContent, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        //Setup LayerDrawable and assign to progressBar
        //待设置的Drawable数组
        Drawable[] progressDrawables = {progressBg, progressClip};
        LayerDrawable progressLayerDrawable = new LayerDrawable(progressDrawables);
        //根据ID设置progressBar对应内容的Drawable
        progressLayerDrawable.setId(0, android.R.id.background);
        progressLayerDrawable.setId(1, android.R.id.progress);
        //设置progressBarDrawable
        progressBar.setProgressDrawable(progressLayerDrawable);

        //将各个组件加入总布局
        rankingItem.addView(itemInfo);
        rankingItem.addView(progressBar);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true);
        rankingItem.setForeground(context.getDrawable(outValue.resourceId));
        return rankingItem;
    }

    //点击进度条进入页面
    public void showCostItems(String itemName,int recordYear,int recordMonth){
        //配置搜索条件
        SearchConditionEntity searchConditionEntity = SearchConditionEntity.getINSTANCE();
        searchConditionEntity.setMode(((MonthReportActivity)activity).getCurrentMode());
        searchConditionEntity.setYear(recordYear);
        searchConditionEntity.setMonth(recordMonth);
        searchConditionEntity.setDay(0);
        searchConditionEntity.setIfIgnoreDay(true);
        searchConditionEntity.setIfIgnoreMonth(false);
        searchConditionEntity.setIfIgnoreYear(false);
        searchConditionEntity.setSearchCostType(new String[]{itemName});
        searchConditionEntity.setSearchOrderRemark("");
        Intent intent = new Intent(context, OrderItemSearchActivity.class);
        context.startActivity(intent);
    }
}
