package com.beta.autobookkeeping.SMStools;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.beta.autobookkeeping.OrderDetailActivity;

import Util.Util;

public class SMSService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //调用读取短信的功能类
        SMSReader smsReader = new SMSReader(SMSService.this);
        smsReader.readSMS();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
    }
}
