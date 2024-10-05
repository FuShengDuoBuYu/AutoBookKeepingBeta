package com.beta.autobookkeeping.fragment.monthReport;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.monthReport.MonthReportActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;

import java.time.Month;
import java.util.ArrayList;

import Util.ProjectUtil;

public class PersonalMonthReportFragment extends Fragment {

    private BarChart monthMoneyBarChart;
    private PieChart monthMoneyPieChart;
    private LinearLayout costRankingProcessBar;
    MonthReportActivity activity;
    com.beta.autobookkeeping.activity.monthReport.charts.PieChart pieChart;
    public PersonalMonthReportFragment() {
    }

    public static PersonalMonthReportFragment newInstance() {
        PersonalMonthReportFragment fragment = new PersonalMonthReportFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_month_report, container, false);
        findViewsByIdAndInit(view);
        return view;
    }

    @Override
    public void onResume() {
        refreshMonthCost(activity.recordYear,activity.recordMonth);
        super.onResume();
    }

    private void findViewsByIdAndInit(View v){
        this.activity = (MonthReportActivity)getActivity();
        monthMoneyBarChart = v.findViewById(R.id.bar_chart_month_money);
        com.beta.autobookkeeping.activity.monthReport.charts.BarChart barChart = new com.beta.autobookkeeping.activity.monthReport.charts.BarChart(
                getContext(),monthMoneyBarChart,getEveryMonthMoney()
        );
        barChart.showBarChart();

        monthMoneyPieChart = v.findViewById(R.id.pie_chart_month_money);
        costRankingProcessBar = v.findViewById(R.id.ll_cost_ranking_process_bar);
        pieChart = new com.beta.autobookkeeping.activity.monthReport.charts.PieChart(
                monthMoneyPieChart,getContext(),activity.recordMonth,activity.recordYear,ProjectUtil.getMonthOrders(activity.recordYear,activity.recordMonth,getContext())
                ,costRankingProcessBar
        );
        pieChart.showPieChart();
        pieChart.showMonthlyCostRanking();
    }

    //查询各个月的收支情况
    private ArrayList<Float> getEveryMonthMoney(){
        ArrayList<Float> res = new ArrayList<>();
        int recordMonth = ProjectUtil.getCurrentMonth(),recordYear = ProjectUtil.getCurrentYear();
        //依次查取各个月的收支数据
        double monthCost=0.0;
        while (true){
            //查出某个月的收支总和
            monthCost= ProjectUtil.getMonthMoney(recordYear,recordMonth, getContext());
            //如果某个月收支为0,说明为起始月份
            if(monthCost==0.0 || res.size() ==12){
                break;
            }
            //否则将收支总和写入记录的list
            else{
                res.add((float) monthCost);
            }
            //更新要查询的月份
            if(recordMonth==1){
                recordMonth = 12;
                recordYear-=1;
            }else{
                recordMonth-=1;
            }
        }
        return res;
    }

    //更新显示月份
    public void refreshMonthCost(int recordYear,int recordMonth){
        activity.refreshMonthMoney(ProjectUtil.getMonthMoney(recordYear,recordMonth,getContext()),recordYear,recordMonth);
        pieChart.refreshPieChartAndRanking(activity.recordYear,activity.recordMonth,ProjectUtil.getMonthOrders(activity.recordYear,activity.recordMonth,getContext()));
    }
}