package com.beta.autobookkeeping;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import Util.*;
import android.widget.RemoteViews;


public class OrderWidget extends AppWidgetProvider {
    /**
     * 接收窗口小部件点击时发送的广播
     */
    public static String ONCLICK = "onClick";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ONCLICK)) {
            //打开应用
            Intent intent2 = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            context.startActivity(intent2);
        }
        super.onReceive(context, intent);


    }
    /**
     * 每次窗口小部件被更新都调用一次该方法
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.order_widget);
        remoteViews.setTextViewText(R.id.wtvAllTodayOrder, String .format("%.1f",Util.getTodayMoney(context)));
        remoteViews.setTextViewText(R.id.wtvAllMonthOrder, String .format("%.1f",Util.getMonthMoney(context)));

        //设置小组件被点击时的事件
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(ONCLICK);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity
                    (context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            pendingIntent = PendingIntent.getActivity
                    (context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        }
        remoteViews.setOnClickPendingIntent(R.id.wll, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds,remoteViews);
    }
    /**
     * 每删除一次窗口小部件就调用一次
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }
    /**
     * 当该窗口小部件第一次添加到桌面时调用该方法
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }
    /**
     * 当最后一个该窗口小部件删除时调用该方法
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
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
