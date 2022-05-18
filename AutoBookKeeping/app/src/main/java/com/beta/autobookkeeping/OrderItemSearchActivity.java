package com.beta.autobookkeeping;

import static Util.Util.getCurrentMonth;
import static Util.Util.getCurrentYear;
import static Util.Util.setDayOrderItem;
import static Util.Util.setDayOrderTitle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beta.autobookkeeping.SMStools.SMSDataBase;

import java.util.ArrayList;
import java.util.Date;

import Util.Util;

public class OrderItemSearchActivity extends AppCompatActivity {
    SQLiteDatabase db;
    //如果限制条件为3个,日查找,2个,就是月查找,如果是4个,就是月度报告中点击查找
    private int limitSize;
    private TextView tv_searchLimit,tv_allOrderMoney,tv_allCost;
    private LinearLayout ll_allOrderItemLimit;
    private Bundle searchLimitBundle = null;
    private int searchYear=0,searchMonth=0,searchDay = 0;
    private String itemName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_item_search);
        SMSDataBase smsDb = new SMSDataBase(OrderItemSearchActivity.this, "orderInfo", null, 1);
        db = smsDb.getWritableDatabase();
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
        limitSize = searchLimitBundle.size();
        searchYear = searchLimitBundle.getInt("year");
        searchMonth =1+ searchLimitBundle.getInt("month");
        //日查找
        if(limitSize==3){
            searchDay =searchLimitBundle.getInt("day");
        }
        //报告中查找
        else if(limitSize==4){
            itemName = searchLimitBundle.getString("itemName");
            searchMonth-=1;
        }
    }

    //设置这个界面的信息并显示
    public void setAllOrderInfo(){
        tv_searchLimit.setText(setTvSearchLimit());
        //如果是按照月来查询
        if(limitSize==2){
            tv_allOrderMoney.setText("总收支\n"+String.format("%.1f",Util.getMonthMoney(searchYear,searchMonth,OrderItemSearchActivity.this)));
            tv_allCost.setText("总支出\n"+String.format("%.1f",Util.getMonthCost(searchYear,searchMonth,OrderItemSearchActivity.this)));
        }
        else if(limitSize==3){
            tv_allOrderMoney.setText("总收支\n"+String.format("%.1f",Util.getDayMoney(searchYear,searchMonth,searchDay,OrderItemSearchActivity.this)));
            tv_allCost.setText("总支出\n"+String.format("%.1f",Util.getDayCost(searchYear,searchMonth,searchDay,OrderItemSearchActivity.this)));
        }
        else if(limitSize==4){
            tv_allOrderMoney.setText(itemName);
            tv_allCost.setText("总支出\n"+String.format("%.1f",Util.getMonthSomeItemCost(searchYear,searchMonth,itemName,OrderItemSearchActivity.this)));
        }
        showFindOrderItems();
    }

    //显示tv_searchLimit
    public String setTvSearchLimit(){
        String tv_searchLimit;
        if(limitSize==2){
            tv_searchLimit = searchYear+"/"+searchMonth;
        }else if(limitSize == 3){
            tv_searchLimit = searchYear+"/"+searchMonth+"/"+searchDay;
        }else{
            tv_searchLimit = searchYear+"/"+searchMonth;
        }
        return tv_searchLimit;
    }

    //显示查询的所有账单信息
    public void showFindOrderItems(){
        if(limitSize==3)
            showFindDayOrderItems();
        if(limitSize==2)
            showFindMonthOrderItems();
        if(limitSize==4)
            showFindItemOrderItems();
    }

    //显示月的账单信息
    public void showFindMonthOrderItems(){
        //先获取本月都有哪些天有数据
        ArrayList<Integer> hasOrderDays = Util.getHasOrderDays(searchMonth,OrderItemSearchActivity.this);
        //依次查询这些天,并进行view的添加
        for(int i = 0;i < hasOrderDays.size();i++){
            //每天先加一个title
            String date = searchMonth+"月"+String.valueOf(hasOrderDays.get(i))+"日";
            String money ="本日总计: "+ String.format("%.1f",Util.getDayMoney(searchYear, searchMonth,hasOrderDays.get(i),OrderItemSearchActivity.this))+"元";
            ll_allOrderItemLimit.addView(setDayOrderTitle(date,money,OrderItemSearchActivity.this));
            //再加入每天的账单
            String sql = "select * from orderInfo where year = " + searchYear +
                    " and month = " + searchMonth + " and day= " + String.valueOf(hasOrderDays.get(i));
            Cursor cursor = db.rawQuery(sql,null);
            while (cursor.moveToNext()) {
                int itemIdInDatabase = cursor.getInt(0);
                String category = cursor.getString(8)  + (cursor.getString(7).equals("")?"":"-"+cursor.getString(7));
                String payWay = cursor.getString(6);
                String dayMoney = String.format("%.1f",cursor.getDouble(5))+"元";
                String time = Util.getWeek(new Date(searchYear,searchMonth,hasOrderDays.get(i))) + " " +cursor.getString(4).substring(cursor.getString(4).length()-5,cursor.getString(4).length());
                LinearLayout dayOrderItem = setDayOrderItem(category,payWay,dayMoney,time,OrderItemSearchActivity.this);
                ll_allOrderItemLimit.addView(dayOrderItem);
            }
        }
    }
    //显示日的账单信息
    public void showFindDayOrderItems(){
        //找到本日所有账单信息
        String sql = "select * from orderInfo where year="+searchYear+" and month="+searchMonth+" and day="+searchDay;
        Cursor cursor = db.rawQuery(sql,null);
        //将这些信息显示出来
        while(cursor.moveToNext()){
            int itemIdInDatabase = cursor.getInt(0);
            String category = cursor.getString(8)  + (cursor.getString(7).equals("")?"":"-"+cursor.getString(7));
            String payWay = cursor.getString(6);
            String dayMoney = String.format("%.1f",cursor.getDouble(5))+"元";
            String time = cursor.getString(4).substring(cursor.getString(4).length()-5,cursor.getString(4).length());
            LinearLayout dayOrderItem = setDayOrderItem(category,payWay,dayMoney,time,OrderItemSearchActivity.this);
            ll_allOrderItemLimit.addView(dayOrderItem);
        }
    }
    //显示某一项的账单信息
    public void showFindItemOrderItems(){
        //找到本日所有账单信息
        String sql = "select * from orderInfo where year="+searchYear+" and month="+searchMonth+" and costType='"+itemName+"'";
        Cursor cursor = db.rawQuery(sql,null);
        //将这些信息显示出来
        while(cursor.moveToNext()){
            int itemIdInDatabase = cursor.getInt(0);
            String category = cursor.getString(8)  + (cursor.getString(7).equals("")?"":"-"+cursor.getString(7));
            String payWay = cursor.getString(6);
            String dayMoney = String.format("%.1f",cursor.getDouble(5))+"元";
            String time = searchMonth + "月" + cursor.getString(3) + "日" + cursor.getString(4).substring(cursor.getString(4).length()-5,cursor.getString(4).length());
            LinearLayout someItemOrderItem = setDayOrderItem(category,payWay,dayMoney,time,OrderItemSearchActivity.this);
            ll_allOrderItemLimit.addView(someItemOrderItem);
        }
    }
}