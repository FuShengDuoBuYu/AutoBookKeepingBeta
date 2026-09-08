package com.beta.autobookkeeping.widget;

import static Util.ConstVariable.*;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.main.MainActivity;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;
import org.json.JSONObject;
import Util.ProjectUtil;
import Util.SpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OrderWidget extends AppWidgetProvider {
    public static final String REFRESH="refresh", SWAP_VERSION="swapVersion";
    private static final AtomicInteger generation=new AtomicInteger();
    private static final OkHttpClient CLIENT=new OkHttpClient.Builder().callTimeout(8,TimeUnit.SECONDS).build();

    public static void refreshAll(Context context) {
        if (AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context,OrderWidget.class)).length>0)
            context.sendBroadcast(new Intent(context,OrderWidget.class).setAction(REFRESH));
    }
    private static String mode(Context context) {
        return FAMILY_MODE.equals(SpUtils.get(context,"widgetVersion",PERSONAL_MODE)) ? FAMILY_MODE : PERSONAL_MODE;
    }
    @Override public void onReceive(Context context,Intent intent) {
        String action=intent.getAction();
        if (REFRESH.equals(action) || SWAP_VERSION.equals(action) || AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            if (SWAP_VERSION.equals(action)) SpUtils.put(context,"widgetVersion",PERSONAL_MODE.equals(mode(context))?FAMILY_MODE:PERSONAL_MODE);
            final PendingResult pending=goAsync();
            final Context app=context.getApplicationContext();
            final int request=generation.incrementAndGet();
            new Thread(() -> {
                try { refresh(app,request); }
                finally { pending.finish(); }
            },"widget-refresh").start();
            return;
        }
        super.onReceive(context,intent);
    }
    private static RemoteViews views(Context context) {
        RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.order_widget);
        PendingIntent open=PendingIntent.getActivity(context,0,new Intent(context,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.ll_month,open);
        views.setOnClickPendingIntent(R.id.ll_today,open);
        views.setOnClickPendingIntent(R.id.iv_refresh,PendingIntent.getBroadcast(context,1,
                new Intent(context,OrderWidget.class).setAction(REFRESH),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
        views.setOnClickPendingIntent(R.id.iv_swap_version,PendingIntent.getBroadcast(context,2,
                new Intent(context,OrderWidget.class).setAction(SWAP_VERSION),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
        return views;
    }
    private static void refresh(Context context,int request) {
        String version=mode(context);
        String phone=(String)SpUtils.get(context,"phoneNum","");
        String family=(String)SpUtils.get(context,"familyId","");
        int year=ProjectUtil.getCurrentYear(),month=ProjectUtil.getCurrentMonth(),day=ProjectUtil.getCurrentDay();
        String key=phone+":"+family+":"+version+":"+year+":"+month+":"+day;
        SharedPreferences cache=context.getSharedPreferences("widget_cache",Context.MODE_PRIVATE);
        String today=cache.getString(key+":today","--"),total=cache.getString(key+":month","--");
        String label=version;
        try {
            if (phone.isEmpty()) { today="--";total="--";label="请先登录"; }
            else if (PERSONAL_MODE.equals(version)) {
                today=String.format(Locale.getDefault(),"%.1f",ProjectUtil.getTodayMoney(context));
                total=String.format(Locale.getDefault(),"%.1f",ProjectUtil.getMonthMoney(context));
            } else if (family.isEmpty()) { today="--";total="--";label="未加入家庭"; }
            else {
                try(Response response=CLIENT.newCall(new Request.Builder().url(IP+"/findMonthFamilyOrders/"+family+"/"+month).build()).execute()) {
                    if (!response.isSuccessful() || response.body()==null) throw new Exception();
                    JSONObject json=new JSONObject(response.body().string());
                    if (!json.optBoolean("success")) throw new Exception();
                    JSONArray orders=json.getJSONArray("data").getJSONArray(0);
                    double d=0,m=0;
                    for(int i=0;i<orders.length();i++) {
                        JSONObject bill=orders.getJSONObject(i);
                        if(bill.getInt("year")!=year || bill.getInt("month")!=month) continue;
                        double amount=bill.getDouble("money");m+=amount;
                        if(bill.getInt("day")==day)d+=amount;
                    }
                    today=String.format(Locale.getDefault(),"%.1f",d);total=String.format(Locale.getDefault(),"%.1f",m);
                }
            }
            cache.edit().putString(key+":today",today).putString(key+":month",total).apply();
        } catch(Exception error) { label=version+" · 刷新失败"; }
        if(request!=generation.get() || !phone.equals(SpUtils.get(context,"phoneNum",""))
                || !family.equals(SpUtils.get(context,"familyId","")) || !version.equals(mode(context))) return;
        RemoteViews view=views(context);
        view.setTextViewText(R.id.wtvAllTodayOrder,today);
        view.setTextViewText(R.id.wtvAllMonthOrder,total);
        view.setTextViewText(R.id.tv_version,label);
        AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context,OrderWidget.class),view);
    }
}
