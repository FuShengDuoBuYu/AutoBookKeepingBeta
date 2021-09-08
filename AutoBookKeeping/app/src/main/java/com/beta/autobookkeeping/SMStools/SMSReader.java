package com.beta.autobookkeeping.SMStools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import Util.Util;

public class SMSReader extends AppCompatActivity {
    SMSApplication smsApplication;
    private Context context;
    //短信内容
    private String body;
    //短信观察者
    private SMSContent smsObsever;
    private Handler handler =new Handler(){
        public void handleMessage(android.os.Message msg) {
            Bundle bundle=msg.getData();
            body=bundle.getString("body");
            if(isBankOrderMsg(body)){
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                intent.putExtras(bundle);
                smsApplication  = (SMSApplication) context.getApplicationContext();
                smsApplication.setSMSMsg(body);
                context.startActivity(intent);
            }
        };
    };


    public SMSReader(Context c){
        context = c;
    }

    public void readSMS(){
        smsObsever = new SMSContent(handler, context);//实例化短信观察者
        //注册短信观察者
        context.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsObsever);
    }

    //判断短信是否是银行发的账单短信
    public boolean isBankOrderMsg(String msg){
        String[] msgInfo = Util.getBankOrderInfo(msg);
        //如果三个银行信息中有一个为空,就说明不是银行信息
        if(msgInfo[0].equals("")||msgInfo[1].equals("")||msgInfo[2].equals("")){
            return false;
        }else{
            return true;
        }
    }
}
