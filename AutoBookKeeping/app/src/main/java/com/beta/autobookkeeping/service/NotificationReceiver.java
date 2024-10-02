package com.beta.autobookkeeping.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.orderDetail.OrderDetailActivity;

import Util.ProjectUtil;
import Util.SpUtils;

public class NotificationReceiver extends NotificationListenerService {
    @Override
    public void onNotificationPosted(android.service.notification.StatusBarNotification sbn) {
       //打印通知的相关信息
        if(sbn.getPackageName().equals("com.tencent.mm")){
            if("微信支付".equals(sbn.getNotification().extras.getString("android.title"))){
                ProjectUtil.toastMsg(this,"读取到微信账单!");
                Bundle bundle = getWechatOrderInfo(sbn.getNotification().extras.getString("android.text"));
                Intent orderDetail = new Intent(this, OrderDetailActivity.class);
                orderDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                orderDetail.putExtras(bundle);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, orderDetail, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "order_notification")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("自动记账")
                        .setContentText(bundle.getString("payWay") + "-" + bundle.getString("orderType") + "-" + bundle.getDouble("money") + "元")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, builder.build());
            }
        }
        if(sbn.getPackageName().equals("com.eg.android.AlipayGphone")){
            String title = sbn.getNotification().extras.getString("android.title");
            String text = sbn.getNotification().extras.getString("android.text");

            // 判断是否为小荷包模式
            boolean isXiaohebaoMode = SpUtils.contains(this, "is_alipay_xiaohebao");

            // 条件处理
            if (!isXiaohebaoMode && "交易提醒".equals(title)) {
                handleAlipayNotification("读取到支付宝账单!", text);
            } else if (isXiaohebaoMode && "小荷包资金变动提醒".equals(title) &&
                    text.startsWith((String) SpUtils.get(this, "is_alipay_xiaohebao", ""))) {
                handleAlipayNotification("读取到支付宝小荷包账单!", text);
            }
        }
        super.onNotificationPosted(sbn);
    }

    private void handleAlipayNotification(String toastMsg, String notificationText) {
        // 显示 toast
        ProjectUtil.toastMsg(this, toastMsg);

        // 创建 Intent 和 Bundle
        Intent orderDetail = new Intent(this, OrderDetailActivity.class);
        orderDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = getAlipayOrderInfo(notificationText);
        orderDetail.putExtras(bundle);

        // 创建 PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, orderDetail, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "order_notification")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("自动记账")
                .setContentText(bundle.getString("payWay") + "-" + bundle.getString("orderType") + "-" + bundle.getDouble("money") + "元")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH); // 确保通知的优先级较高

        // 发送通知
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onNotificationRemoved(android.service.notification.StatusBarNotification sbn) {
        Log.d("NotificationReceiver", "onNotificationRemoved: ");
        super.onNotificationRemoved(sbn);
    }

    //获取支付的相关信息
    private Bundle getWechatOrderInfo(String msg){
        Bundle bundle = new Bundle();
        bundle.putString("orderType",(msg.contains("支出")||msg.contains("支付"))?"支出":"收入");
        bundle.putString("payWay","微信");
        //获取字符串中¥后的浮点数
        Double money = Double.valueOf(msg.substring(msg.indexOf("¥")+1));
        bundle.putDouble("money",money);
        return bundle;
    }

    //获取支付的相关信息
    private Bundle getAlipayOrderInfo(String msg){
        Bundle bundle = new Bundle();
        bundle.putString("orderType",(msg.contains("支出")||msg.contains("支付"))?"支出":"收入");
        bundle.putString("payWay","支付宝");
        //用正则表达式获取字符串中的浮点数
        Double money = Double.valueOf(msg.replaceAll("[^0-9.]", ""));
        bundle.putDouble("money",money);
        return bundle;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "NotificationReceiver Service Started", Toast.LENGTH_LONG).show();
        super.onStart(intent, startId);
    }
}
