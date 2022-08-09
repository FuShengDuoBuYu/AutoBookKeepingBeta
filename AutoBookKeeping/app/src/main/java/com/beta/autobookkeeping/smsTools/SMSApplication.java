package com.beta.autobookkeeping.smsTools;

import android.app.Application;

public class SMSApplication extends Application {
    private String SMSMsg;
    //获取Application中的字符数据
    public String getSMSMsg() {
        return SMSMsg;
    }
    //设置Application中的字符数据
    public void setSMSMsg(String SMSMsg) {
        this.SMSMsg = SMSMsg;
    }

    @Override
    //先将数据设置为空
    public void onCreate() {
        setSMSMsg(null);
        super.onCreate();
    }
}
