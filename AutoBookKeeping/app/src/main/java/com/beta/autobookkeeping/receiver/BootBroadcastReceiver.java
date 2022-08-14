package com.beta.autobookkeeping.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.beta.autobookkeeping.activity.main.MainActivity;
import com.beta.autobookkeeping.smsTools.SMSService;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            //自启动APP，参数为需要自动启动的应用包名
            Intent newIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            //下面这句话必须加上才能开机自动运行app的界面
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //自启动读取短信内容的服务
            context.startService(new Intent(context, SMSService.class));
            Toast.makeText(context, "已启动自动记账", Toast.LENGTH_LONG).show();
        }
    }
}
