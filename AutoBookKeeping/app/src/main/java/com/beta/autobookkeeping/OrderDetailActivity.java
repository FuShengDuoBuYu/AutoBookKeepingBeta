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

    private Button btnSaveChanges,btnCostType,btnGetCurrentTime,btnPayWay;
    private EditText etOrderNumber;
    int costType,payWayType;
    final String[] costTypes = {"消费","饮食","交通","娱乐","购物","通讯","红包","医疗"};
    final String[] payWays = {"银行卡","支付宝","微信","现金"};
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
            }
        });

        //选择消费类型的按钮
        btnCostType = findViewById(R.id.btnCostType);
        btnCostType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //弹出选择消费类型的dialog
                showCostType();
//                Util.showDialog(OrderDetailActivity.this,"选择支出类型",costTypes,"确定",btnCostType);
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

    //获取短信内容并处理的方法
    public String[] handleMsg(){
        SMSApplication smsApplication;
        smsApplication = (SMSApplication) getApplication();
        String msg = smsApplication.getSMSMsg();
        smsApplication.setSMSMsg(null);
        return Util.getBankOrderInfo(msg);
    }

    @Override
    protected void onStart() {
        String[] msgContent = handleMsg();
        etOrderNumber.setText(msgContent[2]);
        super.onStart();
    }
}