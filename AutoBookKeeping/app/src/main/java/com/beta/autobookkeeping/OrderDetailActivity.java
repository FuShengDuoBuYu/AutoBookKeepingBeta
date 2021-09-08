package com.beta.autobookkeeping;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.beta.autobookkeeping.SMStools.SMSApplication;
import com.beta.autobookkeeping.SMStools.SMSReader;
import com.beta.autobookkeeping.SMStools.SMSService;

import Util.Util;

public class OrderDetailActivity extends AppCompatActivity {

    private Button btnSaveChanges,btnCostType,btnGetCurrentTime,btnPayWay,btnOrderType;
    private EditText etOrderNumber;
    int costType,payWayType,orderTypeIndex;
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
        //选择消费方式的方法
        btnPayWay = findViewById(R.id.btnPayWay);
        btnPayWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPayWay();
            }
        });
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
        SMSApplication smsApplication;
        smsApplication = (SMSApplication) getApplication();
        String msg = smsApplication.getSMSMsg();
        smsApplication.setSMSMsg(null);
        return (msg==null)?null:Util.getBankOrderInfo(msg);
    }

    @Override
    protected void onStart() {
        String[] msgContent = handleMsg();
        if(msgContent != null){
            etOrderNumber.setText(msgContent[2]);
            btnOrderType.setText(msgContent[1]);
            btnPayWay.setText(msgContent[0]);
        }
        super.onStart();
    }
}