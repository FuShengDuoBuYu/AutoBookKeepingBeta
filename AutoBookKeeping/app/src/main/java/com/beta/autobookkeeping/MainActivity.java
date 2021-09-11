package com.beta.autobookkeeping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.beta.autobookkeeping.OrderListView.OrderInfo;
import com.beta.autobookkeeping.OrderListView.OrderInfoAdapter;
import com.beta.autobookkeeping.SMStools.SMSApplication;
import com.beta.autobookkeeping.SMStools.SMSDataBase;
import com.beta.autobookkeeping.SMStools.SMSReader;
import com.beta.autobookkeeping.SMStools.SMSService;

import java.util.ArrayList;
import java.util.List;

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
        tvAllMonthOrder.setText(String.valueOf(allMonthOrder));
        tvAllTodayOrder.setText(String.valueOf(allTodayOrder));
        cursor.close();
        //获取并显示所有账单详情
        showOrderDetailList();
        super.onStart();
    }

    //获取并显示所有账单详情的方法
    public void showOrderDetailList(){
        Cursor cursor = db.query ("orderInfo",null,null,null,null,null,"id");
        while(cursor.moveToNext()){
            //初始化orderInfoList
            OrderInfo orderInfo = new OrderInfo(cursor.getString(4),cursor.getString(6),cursor.getString(7),cursor.getDouble(5));
            orderInfos.add(orderInfo);
        }
        //获取适配器
        OrderInfoAdapter orderInfoAdapter = new OrderInfoAdapter(MainActivity.this,R.layout.lv_order_detail_item,orderInfos);
        //将适配器的数据传给ListView
        ListView listView = findViewById(R.id.lvOrderDetail);
        listView.setAdapter(orderInfoAdapter);
        // 为ListView注册一个监听器，当用户点击了ListView中的任何一个子项时，就会回调onItemClick()方法
        // 在这个方法中可以通过position参数判断出用户点击的是那一个子项
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Util.toastMsg(MainActivity.this,String.valueOf(position));
            }
        });
    }
}