package com.beta.autobookkeeping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.beta.autobookkeeping.SMStools.SMSApplication;
import com.beta.autobookkeeping.SMStools.SMSDataBase;
import com.beta.autobookkeeping.SMStools.SMSReader;
import com.beta.autobookkeeping.SMStools.SMSService;

import Util.Util;

public class MainActivity extends AppCompatActivity {

    private Button btnPlusNewOrder,btnSettings,btnSearchMonthlyReport;
    Bundle bundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //开启读取短信线程
        startService(new Intent(MainActivity.this, SMSService.class));
        //找到新增和设置两个按钮
        btnPlusNewOrder = findViewById(R.id.btnPlusNewOrder);
        btnSettings = findViewById(R.id.btnSettings);
        //设置两个新增和设置按钮的两个监听事件
        btnPlusNewOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到新增界面
                Intent intent = new Intent(MainActivity.this,OrderDetailActivity.class);
                startActivity(intent);
            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到设置界面
                Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
        });
        //找到<查找月度报告>的按钮
        btnSearchMonthlyReport = findViewById(R.id.btnSearchMonthlyReport);
        //设置该按钮的监听事件
        btnSearchMonthlyReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TOdo:跳转到月度报告页面
                Util.toastMsg(MainActivity.this,"进入月度报告成功");
                Intent intent = new Intent(MainActivity.this,TestActivity.class);
                startActivity(intent);
            }
        });
        //设置数据库
        SMSDataBase smsDb = new SMSDataBase(this,"orderInfo",null,1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
    }

    @Override
    protected void onStart() {
        //检测Application中是否有短信数据
        SMSApplication smsApplication = new SMSApplication();
        smsApplication = (SMSApplication)getApplication();
        if(smsApplication.getSMSMsg()!=null){
            //跳转到新增界面
            Intent intent = new Intent(MainActivity.this,OrderDetailActivity.class);
            startActivity(intent);
        }
        super.onStart();
    }
}