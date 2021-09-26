package com.beta.autobookkeeping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beta.autobookkeeping.OrderListView.OrderInfo;
import com.beta.autobookkeeping.OrderListView.OrderInfoAdapter;
import com.beta.autobookkeeping.SMStools.SMSApplication;
import com.beta.autobookkeeping.SMStools.SMSDataBase;
import com.beta.autobookkeeping.SMStools.SMSReader;
import com.beta.autobookkeeping.SMStools.SMSService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import Util.Util;

public class MainActivity extends AppCompatActivity {

    private Button btnPlusNewOrder,btnSettings,btnSearchMonthlyReport;
    private TextView tvAllTodayOrder,tvAllMonthOrder;
    Bundle bundle;
    //数据库实例
    SQLiteDatabase db;
    //所有账单信息的list
    private List<OrderInfo> orderInfos = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //先检查短信等权限是否获取
        ifGetPermission();
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
                Intent intent = new Intent(MainActivity.this,MonthReportActivity.class);
                startActivity(intent);
            }
        });
        //设置数据库
        SMSDataBase smsDb = new SMSDataBase(this,"orderInfo",null,1);
        db = smsDb.getWritableDatabase();
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
        //获取本日和本月累计收支
        showDayAndMonthMoney();
        //获取并显示所有账单详情
        showOrderDetailList();
        super.onStart();
    }

    //获取并显示所有账单详情的方法
    public void showOrderDetailList(){
        Cursor cursor = db.query ("orderInfo",null,null,null,null,null,"id desc");
        //先清空list中的数据
        orderInfos.clear();
        while(cursor.moveToNext()){
            //初始化orderInfoList
            OrderInfo orderInfo = new OrderInfo(cursor.getString(4),cursor.getString(6),cursor.getString(7),cursor.getDouble(5),cursor.getString(8));
            orderInfos.add(orderInfo);
        }

        //获取适配器
        OrderInfoAdapter orderInfoAdapter = new OrderInfoAdapter(MainActivity.this,R.layout.lv_order_detail_item,orderInfos);
        //将适配器的数据传给ListView
        ListView listView = findViewById(R.id.lvOrderDetail);
        listView.setAdapter(orderInfoAdapter);
        // 为ListView注册一个监听器，当用户点击了ListView中的任何一个子项时，就会回调onItemClick()方法
        // 在这个方法中可以通过position参数判断出用户点击的是那一个子项
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                //弹出提示是否删除该记录的对话框,并进行对应操作
                confirmDeleteOrderInfo(cursor,i);
                return true;
            }
        });
    }

    //长按进行是否删除订单的选择
    public void confirmDeleteOrderInfo(Cursor cursor,int itemIndex){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("是否删除该记录?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //通过itemIndex来获取后台这条数据的id
                cursor.moveToPosition(itemIndex);
                String sql = "delete from orderInfo where id =" + String.valueOf(cursor.getInt(0));
                db.execSQL(sql);
                //重新获取数据库的数据来展示信息
                showDayAndMonthMoney();
                showOrderDetailList();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Util.toastMsg(MainActivity.this,"取消删除");
            }
        });
        builder.show();
    }

    public void showDayAndMonthMoney(){
        //获取本日和本月收支数据
        double allTodayOrder = 0.0,allMonthOrder = 0.0;
        tvAllTodayOrder = findViewById(R.id.tvAllTodayOrder);
        tvAllMonthOrder = findViewById(R.id.tvAllMonthOrder);
        Cursor cursor = db.query ("orderInfo",null,null,null,null,null,"id");
        while(cursor.moveToNext()){
            //本月数值
            if(cursor.getInt(1)==Util.getCurrentYear() && cursor.getInt(2)==Util.getCurrentMonth()){
                allMonthOrder +=cursor.getDouble(5);
                //本日数值
                if (cursor.getInt(3)==Util.getCurrentDay()){
                    allTodayOrder+=cursor.getDouble(5);
                }
            }
        }
        //重新给月和日开销赋值
        tvAllMonthOrder.setText(String .format("%.2f",allMonthOrder));
        tvAllTodayOrder.setText(String .format("%.2f",allTodayOrder));
        cursor.close();
    }

    public void ifGetPermission(){
        List<String> permissions = new ArrayList<>();
        //获取短信权限
        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS)){
            permissions.add(Manifest.permission.RECEIVE_SMS);
        }
        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS)){
            permissions.add(Manifest.permission.READ_SMS);
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 1);

        }
        //获取后台弹出权限
        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SYSTEM_ALERT_WINDOW)){
            permissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
        }
        //一次性获取权限
        if (permissions.size() != 0) {
            //这里谷歌原生可以直接来实现,但是小米等不可以
            ActivityCompat.requestPermissions(MainActivity.this,(String[]) permissions.toArray(new String[0]),667);
        }
    }

    //获取权限是否成功的回调函数
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 667:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] ==PackageManager.PERMISSION_GRANTED) {
                } else {
                    // Permission Denied 权限被拒绝
//                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                    //设置对话框的内容
//                    builder.setTitle("权限申请").setMessage("由于国内定制UI的限制,无法自动弹窗获取权限\n请您前往设置-应用设置-授权管理\n(或在多任务栏里点击本应用的设置信息) 给予本应用读取短信以及后台弹窗的权限,否则将" +
//                            "无法实现自动读取短信记账的功能").setCancelable(false);
//                    //设置对话框的两个选项
//                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                        }
//                    }).show();
                }
                break;
            default:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}