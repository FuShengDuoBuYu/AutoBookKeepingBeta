package com.beta.autobookkeeping.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import Util.ProjectUtil;

public class DebugNotificationTestReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getStringExtra("packageName");
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        String subText = intent.getStringExtra("subText");
        String bigText = intent.getStringExtra("bigText");
        String linesText = intent.getStringExtra("lines");

        ArrayList<String> lines = new ArrayList<>();
        if (linesText != null && !linesText.trim().equals("")) {
            String[] splitLines = linesText.split("\\n");
            for (String line : splitLines) {
                lines.add(line);
            }
        }

        ProjectUtil.handleNotificationBillWithRegexForDebug(
                context,
                packageName == null ? "" : packageName,
                title == null ? "" : title,
                text == null ? "" : text,
                subText == null ? "" : subText,
                bigText == null ? "" : bigText,
                lines);
    }
}
