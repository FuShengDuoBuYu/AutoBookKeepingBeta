package com.beta.autobookkeeping.activity.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class DialogPermisson {
    //查看有没有获取到权限,没有就弹窗获取权限
    public static void ifGetPermission(Context context, Activity activity){
        List<String> permissions = new ArrayList<>();
        //获取短信权限
        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS)){
            permissions.add(Manifest.permission.RECEIVE_SMS);
        }
        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)){
            permissions.add(Manifest.permission.READ_SMS);
        }
        //获取后台弹出权限
        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.SYSTEM_ALERT_WINDOW)){
            permissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
        }
        //一次性获取权限
        if (permissions.size() != 0) {
            //这里谷歌原生可以直接来实现,但是小米等不可以
            ActivityCompat.requestPermissions(activity,(String[]) permissions.toArray(new String[0]),667);
        }
    }
}
