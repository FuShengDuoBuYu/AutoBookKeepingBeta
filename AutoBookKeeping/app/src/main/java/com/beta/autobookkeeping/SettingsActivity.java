package com.beta.autobookkeeping;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.beta.autobookkeeping.SMStools.SMSReader;
import com.beta.autobookkeeping.SMStools.SMSService;

import Util.Util;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout llDeleteAllOrders;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //开启读取短信线程
        startService(new Intent(SettingsActivity.this, SMSService.class));
        OnClick onClick = new OnClick();
        //找到各个设置的按钮
        llDeleteAllOrders = findViewById(R.id.llDeleteAllOrders);
        //设置点击事件
        llDeleteAllOrders.setOnClickListener(onClick);

//        //设置自动读取短信弹窗
//        SMSReader smsReader = new SMSReader(SettingsActivity.this);
//        smsReader.readSMS();
    }
    //自己写的一个实现的OnClick类
    class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.llDeleteAllOrders:
                    //Todo:弹出dialog对话框后,确实清除数据
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    //设置对话框的内容
                    builder.setTitle("清空账单记录").setMessage("您的所有账单记录将被清除,是否确认删除?");
                    //设置对话框的两个选项
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Util.toastMsg(SettingsActivity.this,"确定清除数据");
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Util.toastMsg(SettingsActivity.this,"取消清除数据");
                        }
                        //不要忘记最后要show()
                    }).show();
                    break;
            }
        }
    }
}



