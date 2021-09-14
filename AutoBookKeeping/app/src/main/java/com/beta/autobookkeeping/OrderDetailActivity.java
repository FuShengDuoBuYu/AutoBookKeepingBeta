package com.beta.autobookkeeping;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.beta.autobookkeeping.SMStools.SMSApplication;
import com.beta.autobookkeeping.SMStools.SMSDataBase;
import com.beta.autobookkeeping.SMStools.SMSReader;
import com.beta.autobookkeeping.SMStools.SMSService;

import Util.Util;

public class OrderDetailActivity extends AppCompatActivity {

    private Button btnSaveChanges,btnCostType,btnGetCurrentTime,btnPayWay,btnOrderType;
    private EditText etOrderNumber,etOrderRemark;
    int costType,payWayType,orderTypeIndex;
    String[] msgContent;
    final String[] costTypes = {"消费","饮食","交通","娱乐","购物","通讯","红包","医疗"};
    final String[] payWays = {"银行卡","支付宝","微信","现金"};
    final String[] orderType = {"支出","收入"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        //页面进来的时候就查看是否有短信信息在这里,若有,则处理信息自动匹配
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            handleMsg();
        }
        //开启读取短信线程
        startService(new Intent(OrderDetailActivity.this, SMSService.class));
        //页面一进来就执行获取当前时间的操作,不能放在最前面
        btnGetCurrentTime = findViewById(R.id.btnGetCurrentTime);
        String currentTime = Util.getCurrentTime();
        btnGetCurrentTime.setText(currentTime);
        //保存账单信息的按钮
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.toastMsg(OrderDetailActivity.this,"保存成功");
                setDataBaseData();
                finish();
            }
        });

        //选择消费类型的按钮
        btnCostType = findViewById(R.id.btnCostType);
        btnCostType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //弹出选择消费类型的dialog
                showCostType();
            }
        });
        //选择账单是支出还是收入的按钮
        btnOrderType = findViewById(R.id.btnOrderType);
        btnOrderType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrderType();
            }
        });
        //获取当前时间的按钮
        btnGetCurrentTime = findViewById(R.id.btnGetCurrentTime);
        btnGetCurrentTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取当前时间并显示
                String currentTime = Util.getCurrentTime();
                btnGetCurrentTime.setText(currentTime);
            }
        });
        //金额
        etOrderNumber = findViewById(R.id.etOrderNumber);
        //订单备注
        etOrderRemark = findViewById(R.id.etOrderRemark);
        //选择消费方式的方法
        btnPayWay = findViewById(R.id.btnPayWay);
        btnPayWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPayWay();
            }
        });
    }

    @Override
    protected void onDestroy() {
        SMSApplication smsApplication = new SMSApplication();
        smsApplication = (SMSApplication)getApplication();
        smsApplication.setSMSMsg(null);
        super.onDestroy();
    }

    //选择消费类型并显示的方法
    private void showCostType(){
        costType = -1;
        AlertDialog.Builder costTypeDialog = new AlertDialog.Builder(OrderDetailActivity.this);
        costTypeDialog.setTitle("选择支出类型");
        //第二个参数是默认选项,此处设置为0.即数组的第一个元素
        costTypeDialog.setSingleChoiceItems(costTypes, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //将选择的数据赋值(下标)
                costType = i;
            }
        });
        //设置确定按钮并将结果返回给界面
        costTypeDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(costType!=-1){
                    btnCostType.setText(costTypes[costType]);
                }
            }
        });
        //调用dialog的show方法
        costTypeDialog.show();
    }

    //选择支付方式类型并显示的方法
    private void showPayWay(){
        payWayType = -1;
        AlertDialog.Builder payWayDialog = new AlertDialog.Builder(OrderDetailActivity.this);
        payWayDialog.setTitle("选择支付方式");
        //第二个参数是默认选项,此处设置为0.即数组的第一个元素
        payWayDialog.setSingleChoiceItems(payWays, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //将选择的数据赋值(下标)
                payWayType = i;
            }
        });
        //设置确定按钮并将结果返回给界面
        payWayDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(payWayType!=-1){
                    btnPayWay.setText(payWays[payWayType]);
                }
            }
        });
        //调用dialog的show方法
        payWayDialog.show();
    }

    //选择账单是收入还是支出并显示的方法
    private void showOrderType(){
        orderTypeIndex = -1;
        AlertDialog.Builder orderTypeDialog = new AlertDialog.Builder(OrderDetailActivity.this);
        orderTypeDialog.setTitle("选择账单类型");
        //第二个参数是默认选项,此处设置为0.即数组的第一个元素
        orderTypeDialog.setSingleChoiceItems(orderType, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //将选择的数据赋值(下标)
                orderTypeIndex = i;
            }
        });
        //设置确定按钮并将结果返回给界面
        orderTypeDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(orderTypeIndex!=-1){
                    btnOrderType.setText(orderType[orderTypeIndex]);
                }
            }
        });
        //调用dialog的show方法
        orderTypeDialog.show();
    }

    //获取短信内容并处理的方法
    public String[] handleMsg(){
        SMSApplication smsApplication = new SMSApplication();;
        smsApplication = (SMSApplication) getApplication();
        String msg = smsApplication.getSMSMsg();
        smsApplication.setSMSMsg(null);
        return (msg==null)?null:Util.getBankOrderInfo(msg);
    }

    @Override
    protected void onPause() {
        //在这里将Application中的数据设置为空,这样就不会跳转两次,这个bug和activity的生命周期
        //息息相关
        SMSApplication smsApplication = new SMSApplication();;
        smsApplication = (SMSApplication) getApplication();
        String msg = smsApplication.getSMSMsg();
        smsApplication.setSMSMsg(null);
        super.onPause();
    }

    @Override
    protected void onStart() {
        //先尝试查找Application中是否有信息
        msgContent = handleMsg();
        if(msgContent != null){
            etOrderNumber.setText(msgContent[2]);
            btnOrderType.setText(msgContent[1]);
            btnPayWay.setText(msgContent[0]);
        }
        super.onStart();
    }

    //写入数据库数据
    public void setDataBaseData(){
        //设置数据库数据
        SMSDataBase smsDb = new SMSDataBase(OrderDetailActivity.this,"orderInfo",null,1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("year",Util.getCurrentYear());
        values.put("month",Util.getCurrentMonth());
        values.put("day",Util.getCurrentDay());
        values.put("clock",btnGetCurrentTime.getText().toString());
        //根据内容确定写入数组的金额还是用户的金额,根据支出还是收入记录正负号
        //根据内容确定写入数组还是用户默认
        //用户手动添加的账单信息
        if(msgContent == null){
            if(btnOrderType.getText().toString().equals("收入")){
                //收入记正数
                values.put("money",Double.valueOf(etOrderNumber.getText().toString()));
            }
            else{
                //支出记负数
                values.put("money",0.0-(Double.parseDouble(etOrderNumber.getText().toString())));
            }
            values.put("bankName",btnPayWay.getText().toString());
            values.put("costType",btnCostType.getText().toString());
        }
        //短信自动读取的账单信息
        else{
            if(msgContent[1].equals("收入")){
                //收入记正数
                values.put("money",Double.parseDouble(msgContent[2]));
            }
            else{
                //支出记负数
                values.put("money",0.0-Double.parseDouble(msgContent[2]));
            }
            if(!msgContent[0].equals("")){
                values.put("bankName",msgContent[0]);
            }
            values.put("costType",msgContent[1]);
        }
        //写入账单备注
        values.put("orderRemark",etOrderRemark.getText().toString());

        db.insert("orderInfo",null,values);
    }
}

