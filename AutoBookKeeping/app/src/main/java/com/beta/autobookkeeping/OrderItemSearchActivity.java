package com.beta.autobookkeeping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import Util.Util;

public class OrderItemSearchActivity extends AppCompatActivity {
    //如果限制条件为3个,日查找,2个,就是月查找
    private int limitSize;
    private TextView tv_searchLimit,tv_allOrderMoney,tv_allCost;
    private LinearLayout ll_allOrderItemLimit;
    private Bundle searchLimitBundle = null;
    private int searchYear=0,searchMonth=0,searchDay = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_item_search);
        findViewsAndInit();
        setAllOrderInfo();
    }

    //找到各个控件并进行数据的初始化
    public void findViewsAndInit(){
        //找到控件
        tv_searchLimit = findViewById(R.id.tv_searchLimit);
        tv_allOrderMoney = findViewById(R.id.tv_allOrderMoney);
        tv_allCost = findViewById(R.id.tv_allCost);
        ll_allOrderItemLimit = findViewById(R.id.ll_allOrderItemLimit);
        //找到查询的日期并赋值
        Intent intent = this.getIntent();
        searchLimitBundle = intent.getExtras();
        Util.toastMsg(OrderItemSearchActivity.this,String.valueOf(searchLimitBundle.size()));
        limitSize = searchLimitBundle.size();
        searchYear = searchLimitBundle.getInt("year");
        searchMonth = searchLimitBundle.getInt("month");
        if(limitSize==3){
            searchDay =searchLimitBundle.getInt("day");
        }
    }

    //设置这个界面的信息并显示
    public void setAllOrderInfo(){
        tv_searchLimit.setText(set_tv_SearchLimit());
    }

    //显示tv_searchLimit
    public String set_tv_SearchLimit(){
        String tv_searchLimit;
        if(limitSize==2){
            tv_searchLimit = searchYear+"年"+searchMonth+"月";
        }else{
            tv_searchLimit = searchYear+"年"+searchMonth+"月"+searchDay+"日";
        }
        return tv_searchLimit;
    }
}