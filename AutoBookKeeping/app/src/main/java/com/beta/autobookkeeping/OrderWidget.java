package com.beta.autobookkeeping;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import Util.Util;

public class OrderWidget extends AppWidgetProvider {
    /**
     * 接收窗口小部件点击时发送的广播
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.order_widget);
//        remoteViews.setTextViewText(R.id.wtvAllTodayOrder, String.valueOf(Util.getTodayMoney(context)));
//        remoteViews.setTextViewText(R.id.wtvAllMonthOrder, String.valueOf(Util.getMonthMoney(context)));
    }
    /**
     * 每次窗口小部件被更新都调用一次该方法
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d("tag", String.valueOf(Util.getMonthMoney(context)));
//        Log.i("AppWidget", "开始了更新");
//        RemoteViews rv = new RemoteViews(AppUMS.mContent.getPackageName(), R.layout.order_widget);
////这里获得当前的包名，并且用AppWidgetManager来向NewAppWidget.class发送广播。
//        AppWidgetManager manager = AppWidgetManager.getInstance(AppUMS.mContent);
//        ComponentName cn = new ComponentName(AppUMS.mContent, NewAppWidget.class);
//        manager.updateAppWidget(cn, rv);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.order_widget);
        remoteViews.setTextViewText(R.id.wtvAllTodayOrder, String.valueOf(Util.getTodayMoney(context)));
        remoteViews.setTextViewText(R.id.wtvAllMonthOrder, String.valueOf(Util.getMonthMoney(context)));
        appWidgetManager.updateAppWidget(appWidgetIds,remoteViews);
    }
    /**
     * 每删除一次窗口小部件就调用一次
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        //context.stopService(new Intent(context, WidgetService.class));
//        Log.i("AppWidget", "删除成功！");
    }
    /**
     * 当该窗口小部件第一次添加到桌面时调用该方法
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.order_widget);
//        remoteViews.setTextViewText(R.id.wtvAllTodayOrder, String.valueOf(Util.getTodayMoney(context)));
//        remoteViews.setTextViewText(R.id.wtvAllMonthOrder, String.valueOf(Util.getMonthMoney(context)));
//            Log.d("tag", String.valueOf(Util.getMonthMoney(context)));
        // Intent mTimerIntent = new Intent(context, WidgetService.class);
        // context.startService(mTimerIntent);
//        Log.i("AppWidget", "创建成功！");
    }
    /**
     * 当最后一个该窗口小部件删除时调用该方法
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        //  Intent mTimerIntent = new Intent(context, WidgetService.class);
        // context.stopService(mTimerIntent);
//        Log.i("AppWidget", "删除成功！");
    }
    /**
     * 当小部件大小改变时
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }
    /**
     * 当小部件从备份恢复时调用该方法
     */
    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }
}
