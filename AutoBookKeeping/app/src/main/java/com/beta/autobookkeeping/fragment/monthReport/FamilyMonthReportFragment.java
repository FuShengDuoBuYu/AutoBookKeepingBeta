package com.beta.autobookkeeping.fragment.monthReport;

import static Util.ConstVariable.IP;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.activity.monthReport.MonthReportActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.google.gson.JsonObject;
import com.hss01248.dialog.StyledDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Util.ProjectUtil;
import Util.SpUtils;
import Util.StringUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class FamilyMonthReportFragment extends Fragment {

    private BarChart monthMoneyBarChart;
    private PieChart monthMoneyPieChart;
    private LinearLayout costRankingProcessBar;
    MonthReportActivity activity;
    com.beta.autobookkeeping.activity.monthReport.charts.PieChart pieChart;
    private ArrayList<Float> everyMonthMoney = new ArrayList<>();

    public FamilyMonthReportFragment() {
        // Required empty public constructor
    }

    public static FamilyMonthReportFragment newInstance() {
        FamilyMonthReportFragment fragment = new FamilyMonthReportFragment();
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
        View view = inflater.inflate(R.layout.fragment_family_month_report, container, false);
        findViewsByIdAndInit(view);
        //请求后端获取12月份的收支数据
        getEveryMonthMoney();
        getSomeMonthMoney();
        return view;
    }

    @Override
    public void onResume() {
        if(everyMonthMoney.size() != 0){
            refreshMonthCost(activity.recordYear,activity.recordMonth);
        }
        super.onResume();
    }

    private void findViewsByIdAndInit(View v){
        this.activity = (MonthReportActivity)getActivity();
        monthMoneyBarChart = v.findViewById(R.id.bar_chart_month_money);
        monthMoneyPieChart = v.findViewById(R.id.pie_chart_month_money);
        costRankingProcessBar = v.findViewById(R.id.ll_cost_ranking_process_bar);
    }

    //查询某个月的支出情况
    private void getSomeMonthMoney(){
        StyledDialog.buildLoading().show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/findFamilySomeMonthCosts/"+ SpUtils.get(getContext(),"familyId","")+"/"+activity.recordYear+"/"+activity.recordMonth;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).get().build();
                try{
                    Response response = client.newCall(request).execute();
                    if(response.code()==200){
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        if(jsonResponse.getBoolean("success")){
                            //将Map转换为Map
                            Map<String,Object> map = StringUtil.Json2Map(jsonResponse.getJSONObject("data").toString());
                            Map<String,ArrayList<OrderInfo>> map1 = new HashMap<>();
                            //遍历Map
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                JSONArray jsonArray = new JSONArray(entry.getValue().toString());
                                ArrayList<OrderInfo> orderInfos = new ArrayList<>();
                                for(int i = 0;i < jsonArray.length();i++){
                                    orderInfos.add(new OrderInfo(
                                            jsonArray.getJSONObject(i).getInt("id"),
                                            jsonArray.getJSONObject(i).getInt("year"),
                                            jsonArray.getJSONObject(i).getInt("month"),
                                            jsonArray.getJSONObject(i).getInt("day"),
                                            jsonArray.getJSONObject(i).getString("clock"),
                                            jsonArray.getJSONObject(i).getDouble("money"),
                                            jsonArray.getJSONObject(i).getString("bankName"),
                                            jsonArray.getJSONObject(i).getString("orderRemark"),
                                            jsonArray.getJSONObject(i).getString("costType"),
                                            jsonArray.getJSONObject(i).getString("userId")

                                    ));
                                }
                                map1.put(entry.getKey(),orderInfos);
                            }
                            afterGetSomeMonthMoney(map1);
                        }
                        else{
                            Looper.prepare();
                            ProjectUtil.toastMsg(getContext(),jsonResponse.getString("message"));
                            Looper.loop();
                        }
                    }
                    else{
                        Looper.prepare();
                        Log.d("test",String.valueOf(response.code()));
                        Log.d("test",String.valueOf(response.toString()));
                        ProjectUtil.toastMsg(getContext(),"服务器出错");
                        Looper.loop();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //查询12个月的收支情况
    private void getEveryMonthMoney(){
        StyledDialog.buildLoading().show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/findFamilyAllMonthCosts/"+ SpUtils.get(getContext(),"familyId","");
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).get().build();
                try{
                    Response response = client.newCall(request).execute();
                    if(response.code()==200){
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        if(jsonResponse.getBoolean("success")){
                            JSONArray familyAllMonthCosts = jsonResponse.getJSONArray("data");
                            //将jsonArray转换为ArrayList
                            for(int i=0;i<familyAllMonthCosts.length();i++){
                                everyMonthMoney.add((float)familyAllMonthCosts.getDouble(i));
                            }
                            afterGetEveryMonthMoney(everyMonthMoney);
                        }
                        else{
                            Looper.prepare();
                            ProjectUtil.toastMsg(getContext(),jsonResponse.getString("message"));
                            Looper.loop();
                        }
                    }
                    else{
                        Looper.prepare();
                        Log.d("test",String.valueOf(response.code()));
                        Log.d("test",String.valueOf(response.toString()));
                        ProjectUtil.toastMsg(getContext(),"服务器出错");
                        Looper.loop();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void afterGetSomeMonthMoney(Map<String,ArrayList<OrderInfo>> map){
        StyledDialog.dismissLoading(activity);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //将所有的OrderInfo转换为ArrayList
                ArrayList<OrderInfo> orderInfos = new ArrayList<>();
                for (Map.Entry<String, ArrayList<OrderInfo>> entry : map.entrySet()) {
                    orderInfos.addAll(entry.getValue());
                }

                if(pieChart == null){
                        pieChart = new com.beta.autobookkeeping.activity.monthReport.charts.PieChart(
                        monthMoneyPieChart,activity,activity.recordMonth,activity.recordYear, orderInfos
                        ,costRankingProcessBar
                    );
                    pieChart.showPieChart();
                    pieChart.showMonthlyCostRanking();
                }
                else{
                    pieChart.refreshPieChartAndRanking(activity.recordYear,activity.recordMonth,orderInfos);//                pieChart = new com.beta.autobookkeeping.activity.monthReport.charts.PieChart(

                }
            }
        });
    }

    //获取12个月的收支后
    private void afterGetEveryMonthMoney(ArrayList<Float> everyMonthCosts){
        StyledDialog.dismissLoading(activity);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshMonthCost(activity.recordYear,activity.recordMonth);
                com.beta.autobookkeeping.activity.monthReport.charts.BarChart barChart = new com.beta.autobookkeeping.activity.monthReport.charts.BarChart(
                        getContext(),monthMoneyBarChart,everyMonthCosts
                );
                barChart.showBarChart();
            }
        });
    }

    //更新数据
    public void refreshMonthCost(int year,int month){
        activity.refreshMonthMoney((double) everyMonthMoney.get(getMonthDiff(year,month)),year,month);
        getSomeMonthMoney();
        StyledDialog.dismissLoading(activity);
    }



    //判断两个年月之间差了几个月
    private int getMonthDiff(int year,int month){
        return (ProjectUtil.getCurrentYear()-year)*12+(ProjectUtil.getCurrentMonth()-month);
    }
}