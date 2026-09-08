package com.beta.autobookkeeping.fragment.monthReport;

import static Util.ConstVariable.IP;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.activity.monthReport.MonthReportActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;
import Util.ProjectUtil;
import Util.SpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FamilyMonthReportFragment extends Fragment {
    private final Handler main = new Handler(Looper.getMainLooper());
    private final CopyOnWriteArrayList<Call> calls = new CopyOnWriteArrayList<>();
    private final OkHttpClient client = new OkHttpClient.Builder().callTimeout(30, TimeUnit.SECONDS).build();
    private BarChart bar;
    private PieChart pie;
    private LinearLayout ranking;
    private MonthReportActivity host;
    private String familyId;
    private int generation, monthRequest;
    private boolean viewAlive;
    private ArrayList<Float> monthly = new ArrayList<>();

    public static FamilyMonthReportFragment newInstance() { return new FamilyMonthReportFragment(); }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_family_month_report, container, false);
        host = (MonthReportActivity) requireActivity();
        bar = view.findViewById(R.id.bar_chart_month_money);
        pie = view.findViewById(R.id.pie_chart_month_money);
        ranking = view.findViewById(R.id.ll_cost_ranking_process_bar);
        familyId = (String) SpUtils.get(requireContext(), "familyId", "");
        viewAlive = true;
        generation++;
        monthly.clear();
        if (familyId.isEmpty()) ProjectUtil.toastMsg(host, "您还没有加入家庭");
        else fetch("/findFamilyAllMonthCosts/" + familyId, json -> {
            JSONArray data = json.getJSONArray("data");
            ArrayList<Float> values = new ArrayList<>();
            for (int i=0;i<data.length();i++) values.add((float)data.getDouble(i));
            monthly = values;
            new com.beta.autobookkeeping.activity.monthReport.charts.BarChart(host, bar, values).showBarChart();
            updateTotal(host.recordYear, host.recordMonth);
        });
        return view;
    }

    @Override public void onResume() {
        super.onResume();
        if (ready()) refreshMonthCost(host.recordYear, host.recordMonth);
    }

    private boolean ready() {
        return viewAlive && isAdded() && host != null && !host.isFinishing() && !host.isDestroyed();
    }
    private interface Result { void accept(JSONObject json) throws Exception; }
    private void fetch(String path, Result result) {
        final int viewGeneration = generation;
        Call call = client.newCall(new Request.Builder().url(IP + path).build());
        calls.add(call);
        call.enqueue(new Callback() {
            @Override public void onFailure(Call c, IOException error) {
                calls.remove(c);
                if (!c.isCanceled()) reportFailure(viewGeneration);
            }
            @Override public void onResponse(Call c, Response response) {
                calls.remove(c);
                try (Response closed = response) {
                    if (!closed.isSuccessful() || closed.body() == null) throw new IOException();
                    JSONObject json = new JSONObject(closed.body().string());
                    if (!json.optBoolean("success")) throw new IOException();
                    main.post(() -> {
                        if (viewGeneration != generation || !ready()) return;
                        try { result.accept(json); }
                        catch (Exception error) { reportFailure(viewGeneration); }
                    });
                } catch (Exception error) { reportFailure(viewGeneration); }
            }
        });
    }
    private void reportFailure(int viewGeneration) {
        main.post(() -> {
            if (viewGeneration == generation && ready())
                ProjectUtil.toastMsg(host, "家庭月报加载失败，请检查网络后重新进入");
        });
    }
    private void updateTotal(int year, int month) {
        int index = (ProjectUtil.getCurrentYear()-year)*12+ProjectUtil.getCurrentMonth()-month;
        if (index>=0 && index<monthly.size()) host.refreshMonthMoney((double)monthly.get(index),year,month);
    }
    public void refreshMonthCost(int year, int month) {
        if (!ready() || familyId == null || familyId.isEmpty()) return;
        updateTotal(year, month);
        final int request = ++monthRequest;
        fetch("/findFamilySomeMonthCosts/"+familyId+"/"+year+"/"+month, json -> {
            if (request != monthRequest || host.recordYear != year || host.recordMonth != month) return;
            JSONObject grouped = json.getJSONObject("data");
            ArrayList<OrderInfo> orders = new ArrayList<>();
            for (Iterator<String> keys=grouped.keys();keys.hasNext();) {
                JSONArray list = grouped.getJSONArray(keys.next());
                for (int i=0;i<list.length();i++) {
                    JSONObject r=list.getJSONObject(i);
                    orders.add(new OrderInfo(r.getInt("id"),r.getInt("year"),r.getInt("month"),r.getInt("day"),
                            r.getString("clock"),r.getDouble("money"),r.getString("bankName"),
                            r.getString("orderRemark"),r.getString("costType"),r.getString("userId")));
                }
            }
            com.beta.autobookkeeping.activity.monthReport.charts.PieChart chart =
                    new com.beta.autobookkeeping.activity.monthReport.charts.PieChart(pie,host,month,year,orders,ranking);
            chart.showPieChart();
            chart.showMonthlyCostRanking();
        });
    }
    @Override public void onDestroyView() {
        viewAlive=false;
        generation++;
        for (Call call:calls) call.cancel();
        calls.clear();
        bar=null;pie=null;ranking=null;host=null;
        super.onDestroyView();
    }
}
