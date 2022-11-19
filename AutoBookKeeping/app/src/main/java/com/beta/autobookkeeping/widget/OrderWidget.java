package com.beta.autobookkeeping.widget;

import static Util.ConstVariable.FAMILY_MODE;
import static Util.ConstVariable.IP;
import static Util.ConstVariable.PERSONAL_MODE;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import Util.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.beta.autobookkeeping.activity.main.MainActivity;
import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.orderItemSearch.items.SearchConditionEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class OrderWidget extends AppWidgetProvider {
    /**
     * 接收窗口小部件点击时发送的广播
     */
    public static String ONCLICK = "onClick";
    public static String REFRESH = "refresh";
    public static String SWAP_VERSION = "swapVersion";
    @Override
    public void onReceive(Context context, Intent intent) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.order_widget);
        if (intent.getAction().equals(ONCLICK)) {
            //打开应用
            Intent intent2 = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            context.startActivity(intent2);
        }
        if(intent.getAction().equals(REFRESH)){
            String version = (String) SpUtils.get(context,"widgetVersion","");
            if(version.equals(PERSONAL_MODE)){
                String[] personalMoney = getPersonalMoneyInfo(context);
                updateWidgetInfo(context,remoteViews,personalMoney[0],personalMoney[1],version);
            }
            if(version.equals(FAMILY_MODE)){
                String[] familyMoney = getFamilyMoneyInfo(context);
                updateWidgetInfo(context,remoteViews,familyMoney[0],familyMoney[1],version);
            }
        }
        if(intent.getAction().equals(SWAP_VERSION)){
            String version = getXMLVersionRecord(context);
            Toast.makeText(context, "切换"+version, Toast.LENGTH_SHORT).show();
            if(version.equals(PERSONAL_MODE)){
                String[] personalMoney = getPersonalMoneyInfo(context);
                updateWidgetInfo(context,remoteViews,personalMoney[0],personalMoney[1],version);
            }
            if (version.equals(FAMILY_MODE)){
                String[] familyMoney = getFamilyMoneyInfo(context);
                updateWidgetInfo(context,remoteViews,familyMoney[0],familyMoney[1],version);
            }
        }
        super.onReceive(context, intent);
    }
    /**
     * 每次窗口小部件被更新都调用一次该方法
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.order_widget);
        //设置小组件被点击时的事件
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(ONCLICK);
        PendingIntent pendingIntent = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S?
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE):
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        remoteViews.setOnClickPendingIntent(R.id.ll_month, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.ll_today, pendingIntent);

        //刷新按钮点击后刷新小组件
        Intent intent2 = new Intent(context, OrderWidget.class);
        intent2.setAction("refresh");
        intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent pendingIntent2 = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S?
                PendingIntent.getBroadcast(context, 0, intent2, PendingIntent.FLAG_IMMUTABLE):
                PendingIntent.getBroadcast(context, 0, intent2, PendingIntent.FLAG_ONE_SHOT);
        remoteViews.setOnClickPendingIntent(R.id.iv_refresh, pendingIntent2);

        //切换版本
        Intent intent3 = new Intent(context, OrderWidget.class);
        intent3.setAction("swapVersion");
        intent3.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent pendingIntent3 = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S?
                PendingIntent.getBroadcast(context, 0, intent3, PendingIntent.FLAG_IMMUTABLE):
                PendingIntent.getBroadcast(context, 0, intent3, PendingIntent.FLAG_ONE_SHOT);
        remoteViews.setOnClickPendingIntent(R.id.iv_swap_version, pendingIntent3);

        //更新小组件
        String version = (String) SpUtils.get(context,"widgetVersion","");
        if(PERSONAL_MODE.equals(version)){
            String[] personalMoney = getPersonalMoneyInfo(context);
            updateWidgetInfo(context,remoteViews,personalMoney[0],personalMoney[1],version);
        }
        else{
            String[] familyMoney = getFamilyMoneyInfo(context);
            updateWidgetInfo(context,remoteViews,familyMoney[0],familyMoney[1],version);
        }
    }

    private void updateWidgetInfo(Context context,RemoteViews remoteViews,String todayMoney,String monthMoney,String version){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        remoteViews.setTextViewText(R.id.wtvAllTodayOrder, todayMoney);
        remoteViews.setTextViewText(R.id.wtvAllMonthOrder, monthMoney);
        remoteViews.setTextViewText(R.id.tv_version, version);
        appWidgetManager.updateAppWidget(new ComponentName(context,OrderWidget.class),remoteViews);
    }

    public String getXMLVersionRecord(Context context){
        if(SpUtils.get(context,"widgetVersion","").equals("")||SpUtils.get(context,"widgetVersion","")==null){
            SpUtils.put(context,"widgetVersion",PERSONAL_MODE);
            return PERSONAL_MODE;
        }
        else{
            String currentVersion = String.valueOf(SpUtils.get(context,"widgetVersion",""));
            String afterVersion = currentVersion.equals(PERSONAL_MODE)?FAMILY_MODE:PERSONAL_MODE;
            SpUtils.put(context,"widgetVersion",afterVersion);
            return afterVersion;
        }
    }

    private String[] getPersonalMoneyInfo(Context context){
        String[] res = new String[2];
        res[0] = String .format("%.1f", ProjectUtil.getTodayMoney(context));
        res[1] = String .format("%.1f", ProjectUtil.getMonthMoney(context));
        return res;
    }

    private String[] getFamilyMoneyInfo(Context context){
        String[] res = new String[2];
        //向后端取数据
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/findMonthFamilyOrders/"+ SpUtils.get(context,"familyId","")+"/"+String.valueOf(ProjectUtil.getCurrentMonth());
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).get().build();
                try{
                    Response response = client.newCall(request).execute();
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    JSONArray familyOrdersAndFamilyUsers = jsonResponse.getJSONArray("data");
                    JSONArray familyOrders = familyOrdersAndFamilyUsers.getJSONArray(0);
                    Double monthMoney = 0.0,dayMoney = 0.0;
                    for(int i=0;i<familyOrders.length();i++){
                        JSONObject familyOrder = familyOrders.getJSONObject(i);
                        if(familyOrder.getInt("day")==ProjectUtil.getCurrentDay()){
                            dayMoney += familyOrder.getDouble("money");
                        }
                        monthMoney += familyOrder.getDouble("money");
                    }
                    res[0] = String .format("%.1f", dayMoney);
                    res[1] = String .format("%.1f", monthMoney);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        //等待t执行完再继续
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

}
