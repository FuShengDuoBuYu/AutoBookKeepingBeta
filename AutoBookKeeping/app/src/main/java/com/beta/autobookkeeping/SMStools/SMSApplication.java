package com.beta.autobookkeeping.SMStools;

import android.app.Application;

public class SMSApplication extends Application {
    private String SMSMsg;

    public String getSMSMsg() {
        return SMSMsg;
    }

    public void setSMSMsg(String SMSMsg) {
        this.SMSMsg = SMSMsg;
    }

    @Override
    public void onCreate() {
        setSMSMsg(null);
        super.onCreate();
    }
}
