package com.beta.autobookkeeping.activity.main.checking;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

public final class PermissonChecking {
    private static final int REQUEST_NOTIFICATIONS = 667;
    private static final String POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";
    private static boolean listenerDialogShowing;

    private PermissonChecking() {
    }

    public static void ifGetPermission(Activity activity) {
        requestNotificationDisplayPermission(activity);
        requestNotificationListenerAccess(activity);
    }

    private static void requestNotificationDisplayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 33
                && ActivityCompat.checkSelfPermission(activity, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
        }
    }

    private static void requestNotificationListenerAccess(Activity activity) {
        if (NotificationManagerCompat.getEnabledListenerPackages(activity).contains(activity.getPackageName())
                || listenerDialogShowing
                || activity.isFinishing()) {
            return;
        }
        listenerDialogShowing = true;
        new AlertDialog.Builder(activity)
                .setTitle("开启自动记账")
                .setMessage("自动记账需要读取支付宝、微信和银行的交易通知。请在系统页面中允许本应用访问通知；内容只会按本地规则解析。")
                .setPositiveButton("去开启", (dialog, which) -> {
                    listenerDialogShowing = false;
                    activity.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                })
                .setNegativeButton("暂不开启", (dialog, which) -> listenerDialogShowing = false)
                .setOnCancelListener(dialog -> listenerDialogShowing = false)
                .show();
    }
}
