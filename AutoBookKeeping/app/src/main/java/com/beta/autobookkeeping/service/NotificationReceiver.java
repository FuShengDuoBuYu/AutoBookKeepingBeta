package com.beta.autobookkeeping.service;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.widget.Toast;

import Util.ProjectUtil;

public class NotificationReceiver extends NotificationListenerService {
    @Override
    public void onNotificationPosted(android.service.notification.StatusBarNotification sbn) {
        ProjectUtil.handleNotificationBillWithLlm(this, sbn);
        super.onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(android.service.notification.StatusBarNotification sbn) {
        Log.d("NotificationReceiver", "onNotificationRemoved: ");
        super.onNotificationRemoved(sbn);
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
