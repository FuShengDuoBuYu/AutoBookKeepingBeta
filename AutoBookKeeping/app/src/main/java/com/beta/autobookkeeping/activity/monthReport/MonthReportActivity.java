package com.beta.autobookkeeping.activity.monthReport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

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
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.activity.orderItemSearch.OrderItemSearchActivity;
import com.beta.autobookkeeping.fragment.monthReport.FamilyMonthReportFragment;
import com.beta.autobookkeeping.fragment.monthReport.PersonalMonthReportFragment;
import com.beta.autobookkeeping.fragment.orderDetail.FamilyOrderDetailFragment;
import com.beta.autobookkeeping.fragment.orderDetail.PersonalOrderDetailFragment;
import com.beta.autobookkeeping.fragment.orderDetail.TabOrderDetailFragmentPagerAdapter;
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
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import Util.ProjectUtil;

public class MonthReportActivity extends AppCompatActivity {
    private ViewPager2 tabViewPager;
    private List<Fragment> fragments;
    private TabLayout tabLayout;
    private TabOrderDetailFragmentPagerAdapter tabOrderDetailFragmentPagerAdapter;
    private Button btn_right_choose,btn_left_choose;

    private TextView tv_month_report_money,tv_month_report_time;

    private final int[] dataColor = ProjectUtil.colors;
    //当前页面查看的月份
    public int recordYear = ProjectUtil.getCurrentYear();
    public int recordMonth = ProjectUtil.getCurrentMonth();
    SMSDataBase smsDb = new SMSDataBase(MonthReportActivity.this,"orderInfo",null,1);
    SQLiteDatabase db;
    //保存数据的实体
    private final List<BarEntry> moneyEntry = new ArrayList<BarEntry>();
    private  ArrayList<PieEntry> costEntry = new ArrayList<>();
    //数据的集合

    private PieDataSet costDataSet;
    //表格下方的月份文字
    public ArrayList<String> monthLabels = new ArrayList<String>();
    public ArrayList<String> costLabels = new ArrayList<>();
    public ArrayList<Float> costMoney = new ArrayList<>();

    private int currentMode = 0;
    private static final int FAMILY_MODE = 0;
    private static final int PERSONAL_MODE = 1;
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
        tv_month_report_money.setText("0.00");
        tv_month_report_time.setText("计算中...");
    }

    //获取views的控件
    public void getViews(){
        //顶部导航栏
        tabLayout = findViewById(R.id.month_report_tab);
        //viewpage
        tabViewPager = (ViewPager2) findViewById(R.id.month_report_view_page);
        btn_left_choose = findViewById(R.id.btn_left_choose);
        btn_right_choose = findViewById(R.id.btn_right_choose);

        tv_month_report_money = findViewById(R.id.tv_month_report_money);
        tv_month_report_time = findViewById(R.id.tv_month_report_time);
//        costRankingProcessBar = findViewById(R.id.costRankingProcessBar);
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
                    //更新数据
                    if(currentMode==FAMILY_MODE){
                        FamilyMonthReportFragment familyMonthReportFragment = (FamilyMonthReportFragment) fragments.get(0);
                        familyMonthReportFragment.refreshMonthCost(recordYear,recordMonth);
                    }
                    else{
                        PersonalMonthReportFragment personalMonthReportFragment = (PersonalMonthReportFragment) fragments.get(1);
                        personalMonthReportFragment.refreshMonthCost(recordYear,recordMonth);
                    }
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
                if(currentMode==FAMILY_MODE){
                    FamilyMonthReportFragment familyMonthReportFragment = (FamilyMonthReportFragment) fragments.get(0);
                    familyMonthReportFragment.refreshMonthCost(recordYear,recordMonth);
                }
                else{
                    PersonalMonthReportFragment personalMonthReportFragment = (PersonalMonthReportFragment) fragments.get(1);
                    personalMonthReportFragment.refreshMonthCost(recordYear,recordMonth);
                }
            }
        });

        fragments = new LinkedList<>();
        fragments.add(FamilyMonthReportFragment.newInstance());
        fragments.add(PersonalMonthReportFragment.newInstance());
        tabOrderDetailFragmentPagerAdapter = new TabOrderDetailFragmentPagerAdapter(getSupportFragmentManager(),getLifecycle(), fragments);
        tabViewPager.setAdapter(tabOrderDetailFragmentPagerAdapter);
        //初始化显示第一个页面(个人)
        tabViewPager.setCurrentItem(0);
        //设置监听
        tabViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
            @Override
            public void onPageSelected(int position) {
                //更新头部信息
                currentMode = position;
                super.onPageSelected(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, tabViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if(position==0){
                    tab.setText("家庭报告");
                }
                else{
                    tab.setText("个人报告");
                }
            }
        });
        mediator.attach();

    }

    public void refreshMonthMoney(Double money, int year, int month) {
        tv_month_report_money.setText(String.format("%.1f", money));
        tv_month_report_time.setText(String.valueOf(year)+"年"+String.valueOf(month)+"月");
    }
}