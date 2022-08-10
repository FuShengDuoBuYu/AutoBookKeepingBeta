package com.beta.autobookkeeping.activity.monthReport;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.orderItemSearch.OrderItemSearchActivity;
import com.beta.autobookkeeping.smsTools.SMSDataBase;
import com.beta.autobookkeeping.smsTools.SMSService;
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

import Util.ProjectUtil;

public class MonthReportActivity extends AppCompatActivity {
    private Button btn_right_choose,btn_left_choose;
    private BarChart monthMoneyBarChart;
    private PieChart monthMoneyPieChart;
    private TextView tv_month_report_money,tv_month_report_time;
    private LinearLayout costRankingProcessBar;
    private final int[] dataColor = ProjectUtil.colors;
    //当前页面查看的月份
    int recordYear = ProjectUtil.getCurrentYear();
    int recordMonth = ProjectUtil.getCurrentMonth();
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
        //开启读取短信线程
        startService(new Intent(MonthReportActivity.this, SMSService.class));
        db = smsDb.getWritableDatabase();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_report);
        //获取各个组件
        getViews();
        //先更新总收支数据
        tv_month_report_money.setText(String.format("%.1f", ProjectUtil.getMonthMoney(recordYear,recordMonth,MonthReportActivity.this)));
        tv_month_report_time.setText(ProjectUtil.getCurrentYear()+"年"+ ProjectUtil.getCurrentMonth()+"月");
        showBarChart();
        showPieChart();
        showMonthlyCostRanking();
    }

    //获取views的控件
    public void getViews(){
        btn_left_choose = findViewById(R.id.btn_left_choose);
        btn_right_choose = findViewById(R.id.btn_right_choose);
        monthMoneyBarChart = findViewById(R.id.bar_chart_month_money);
        monthMoneyPieChart = findViewById(R.id.pie_chart_month_money);
        tv_month_report_money = findViewById(R.id.tv_month_report_money);
        tv_month_report_time = findViewById(R.id.tv_month_report_time);
        costRankingProcessBar = findViewById(R.id.costRankingProcessBar);
        btn_right_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recordMonth+1> ProjectUtil.getCurrentMonth()&&recordYear== ProjectUtil.getCurrentYear()){
                    ProjectUtil.toastMsg(MonthReportActivity.this,"不能选择未发生的时间");
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
                    tv_month_report_money.setText(String.format("%.1f", ProjectUtil.getMonthMoney(recordYear, recordMonth, MonthReportActivity.this)));
                    refreshPieChartAndRanking();
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
                tv_month_report_money.setText(String.format("%.1f", ProjectUtil.getMonthMoney(recordYear,recordMonth,MonthReportActivity.this)));
                refreshPieChartAndRanking();
            }
        });
    }

    //显示柱状图的方法
    public void showBarChart(){
        //将moneyEntry的值先去到
        setBarDataSetDataMoney(recordMonth,recordYear);
//        moneyEntry.add(new BarEntry(-0.5f,0));
        //创建第一个变量总收支的图
        moneyDataSet = new BarDataSet(moneyEntry,"总收支");
        //设置这个变量柱子的基本参数
        int[] colors = new int[moneyEntry.size()];
        for (int i = 0; i < moneyEntry.size(); i++) {
            Log.d("money",String.valueOf(moneyEntry.get(i).getY()));
            colors[i] = (moneyEntry.get(i).getY()>0?Color.GREEN:Color.RED);
        }
        moneyDataSet.setColors(colors,80);
//        moneyDataSet.setDrawValues(true);
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
        //设置中心的文字
        if(ProjectUtil.getMonthCost(recordYear,recordMonth,MonthReportActivity.this)==0.0){
            pieChart.setCenterText("暂无数据");
        }else{
            pieChart.setCenterText("总支出:\n"+String.format("%.1f", ProjectUtil.getMonthCost(recordYear,recordMonth,MonthReportActivity.this)));
        }
        pieChart.setCenterTextSize(23f);
        //设置中心背景颜色为透明
        pieChart.setHoleColor(0);
        //设置饼状图的颜色
        costDataSet.setColors(dataColor,150);
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
                refreshPieChartAndRanking();
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
    public void setBarDataSetDataMoney(int recordMonth,int recordYear){
        int recordMonth2 = recordMonth,recordYear2 = recordYear;
        //依次查取各个月的收支数据
        double monthCost=0.0;
        //x轴柱的下标
        int index = 0;
        while (true){
            //查出某个月的收支总和
            monthCost= ProjectUtil.getMonthMoney(recordYear2,recordMonth2,MonthReportActivity.this);
            //如果某个月收支为0,说明为起始月份
            if(monthCost==0.0 || moneyEntry.size() ==12){
                break;
            }
            //否则将收支总和写入记录的list
            else{
//                Log.d("index",String.valueOf(index));
//                Log.d("monthCost",String.valueOf(monthCost));
//                Log.d("month",String.valueOf(recordMonth2));
                moneyEntry.add(new BarEntry(index++,(float)monthCost));
                monthLabels.add(String.valueOf(recordMonth2));
            }
            //更新要查询的月份
            if(recordMonth2==1){
                recordMonth2 = 12;
                recordYear2-=1;
            }else{
                recordMonth2-=1;
            }
        }
    }

    //设置柱状图的数据
    public void setPieDataSetDataMoney(int recordMonth,int recordYear){
        //获取本月中有消费的类型
        costLabels = ProjectUtil.getCostTypeAndMoney(recordMonth,recordYear,MonthReportActivity.this).get(0);
        //获取本月中所有消费的具体值
        costMoney = ProjectUtil.getCostTypeAndMoney(recordMonth,recordYear,MonthReportActivity.this).get(1);
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
    public void refreshPieChartAndRanking(){
        costDataSet = null;
        costLabels = null;
        costEntry.clear();
        costMoney = null;
        monthMoneyPieChart.removeAllViews();
        costRankingProcessBar.removeAllViews();
        showPieChart();
        showMonthlyCostRanking();
    }

    //动态显示月度消费排行榜
    public void showMonthlyCostRanking(){
        for(int i = 0;i < costLabels.size();i++){
            //要加入的进度条
            LinearLayout costProcessBar = setCostProcessBar(costLabels.get(i),costMoney.get(i), ProjectUtil.getMonthCost(recordYear,recordMonth,MonthReportActivity.this),i);
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
    }

    //创建一个processBar对象,显示某一个类别的消费支出
    public LinearLayout setCostProcessBar(String category,float cost,double sumCost,int colorIndex){
        LinearLayout rankingItem = new LinearLayout(this);
        rankingItem.setOrientation(LinearLayout.VERTICAL);
        //先把该item的信息加上的子布局
        LinearLayout itemInfo = new LinearLayout(this);
        //排行进度条
        ProgressBar progressBar = new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        //item信息子布局的两个小布局
        TextView tvCategory = new TextView(this);
        TextView tvCost = new TextView(this);
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
        return rankingItem;
    }

    //点击进度条进入页面
    public void showCostItems(String itemName,int recordYear,int recordMonth){
        //放入数据
        Bundle bundle = new Bundle();
        bundle.putInt("year",recordYear);
        bundle.putInt("month",recordMonth);
        bundle.putString("itemName",itemName);
        //此处架构不好,但是懒得优化了,这个项是不必要的
        bundle.putString("noMeaning","noMeaning");
        Intent intent = new Intent(MonthReportActivity.this, OrderItemSearchActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}