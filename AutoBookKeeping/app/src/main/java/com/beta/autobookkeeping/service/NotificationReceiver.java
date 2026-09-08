package com.beta.autobookkeeping.service;

import android.service.notification.NotificationListenerService;
import android.util.Log;

import Util.ProjectUtil;

public class NotificationReceiver extends NotificationListenerService {
    @Override
    public void onNotificationPosted(android.service.notification.StatusBarNotification sbn) {
        ProjectUtil.handleNotificationBillWithRegex(this, sbn);
        super.onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(android.service.notification.StatusBarNotification sbn) {
        Log.d("NotificationReceiver", "onNotificationRemoved: ");
        super.onNotificationRemoved(sbn);
    }

}
