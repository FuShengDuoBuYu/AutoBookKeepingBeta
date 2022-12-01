package com.beta.autobookkeeping.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.widget.Toast;

import com.beta.autobookkeeping.activity.orderDetail.OrderDetailActivity;

import Util.ProjectUtil;

public class NotificationReceiver extends NotificationListenerService {
    @Override
    public void onNotificationPosted(android.service.notification.StatusBarNotification sbn) {
       //打印通知的相关信息
        if(sbn.getPackageName().equals("com.tencent.mm")){
            if("微信支付".equals(sbn.getNotification().extras.getString("android.title"))){
                ProjectUtil.toastMsg(this,"读取到银行账单!");
                Intent orderDetail = new Intent();
                orderDetail.setClassName(this, OrderDetailActivity.class.getName());
                orderDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                orderDetail.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Bundle bundle = getWechatOrderInfo(sbn.getNotification().extras.getString("android.text"));
                orderDetail.putExtras(bundle);
                this.startActivity(orderDetail);
            }
        }
        if(sbn.getPackageName().equals("com.eg.android.AlipayGphone")){
            if("交易提醒".equals(sbn.getNotification().extras.getString("android.title"))){
                ProjectUtil.toastMsg(this,"读取到银行账单!");
                Intent orderDetail = new Intent();
                orderDetail.setClassName(this, OrderDetailActivity.class.getName());
                orderDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                orderDetail.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Bundle bundle = getAlipayOrderInfo(sbn.getNotification().extras.getString("android.text"));
                orderDetail.putExtras(bundle);
                this.startActivity(orderDetail);
            }
        }
        super.onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(android.service.notification.StatusBarNotification sbn) {
        Log.d("NotificationReceiver", "onNotificationRemoved: ");
        super.onNotificationRemoved(sbn);
    }

    //获取支付的相关信息
    private Bundle getWechatOrderInfo(String msg){
        Bundle bundle = new Bundle();
        bundle.putString("orderType",msg.contains("支付")?"支出":"收入");
        bundle.putString("payWay","微信");
        //获取字符串中¥后的浮点数
        Double money = Double.valueOf(msg.substring(msg.indexOf("¥")+1));
        bundle.putDouble("money",money);
        return bundle;
    }

    //获取支付的相关信息
    private Bundle getAlipayOrderInfo(String msg){
        Bundle bundle = new Bundle();
        bundle.putString("orderType",msg.contains("支出")?"支出":"收入");
        bundle.putString("payWay","支付宝");
        //用正则表达式获取字符串中的浮点数
        Double money = Double.valueOf(msg.replaceAll("[^0-9.]", ""));
        bundle.putDouble("money",money);
//        Double money = Double.valueOf(msg.substring(0,msg.indexOf("元")));
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
