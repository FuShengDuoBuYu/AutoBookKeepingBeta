package com.beta.autobookkeeping.activity.main;

import static Util.ConstVariable.IP;
import static Util.ProjectUtil.BLUE;
import static Util.ProjectUtil.getCurrentMonth;
import static Util.ProjectUtil.getCurrentYear;
import static Util.ProjectUtil.getDayMoney;
import static Util.ProjectUtil.getDayRelation;
import static Util.ProjectUtil.setDayOrderItem;
import static Util.ProjectUtil.setDayOrderTitle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AlertDialog;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.orderDetail.OrderDetailActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import Util.ProjectUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OrderDetail {
    public static void addViewByData(Context context, LinearLayout lvOrderDetail, SQLiteDatabase db,ScrollView scrollView,Activity activity){
        //先清除所有view,防止重复显示
        lvOrderDetail.removeAllViews();
        //先获取本月都有哪些天有数据
        ArrayList<Integer> hasOrderDays = ProjectUtil.getHasOrderDays(getCurrentMonth(),context);
        //依次查询这些天,并进行view的添加
        for(int i = 0;i < hasOrderDays.size();i++){
            //每天先加一个title
            String date = String.valueOf(getCurrentMonth())+"月"+String.valueOf(hasOrderDays.get(i))+"日";
            String money =getDayRelation(hasOrderDays.get(i))+"总计: "+ String.format("%.1f",getDayMoney(getCurrentYear(), getCurrentMonth(),hasOrderDays.get(i),context))+"元";
            lvOrderDetail.addView(setDayOrderTitle(date,money,context));
            //再加入每天的账单
            String sql = "select * from orderInfo where year = " + String.valueOf(getCurrentYear()) +
                    " and month = " + String.valueOf(getCurrentMonth()) + " and day= " + String.valueOf(hasOrderDays.get(i));
            Cursor cursor = db.rawQuery(sql,null);
            while (cursor.moveToNext()) {
                int itemIdInDatabase = cursor.getInt(0);
                String category = cursor.getString(8)  + (cursor.getString(7).equals("")?"":"-"+cursor.getString(7));
                String payWay = cursor.getString(6);
                String dayMoney = String.format("%.1f",cursor.getDouble(5))+"元";
                String time = ProjectUtil.getWeek(new Date(getCurrentYear(),getCurrentMonth(),hasOrderDays.get(i))) + " " +cursor.getString(4).substring(cursor.getString(4).length()-5,cursor.getString(4).length());
                LinearLayout dayOrderItem = setDayOrderItem(category,payWay,dayMoney,time,context);
                //为每个item设置长按选择删除事件
                dayOrderItem.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        confirmDeleteOrderInfo(cursor,itemIdInDatabase,context,db,activity);
                        return true;
                    }
                });
                //为每个item设置点击进行账单修改事件
                dayOrderItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        modifyOrderInfo(itemIdInDatabase,context,db);
                    }
                });
                lvOrderDetail.addView(dayOrderItem);
            }
            cursor.close();
        }
        //最后每天最后加一个分割线
        View line = new View(context);
        line.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,2));
        line.setPadding(0,10,0,0);
        line.setBackgroundColor(Color.BLACK);
        lvOrderDetail.addView(line);
        //设置左右监听
        listenUserTouch(context,scrollView);
    }

    //长按进行是否删除订单的选择
    @SuppressLint("UseCompatLoadingForDrawables")
    public static void confirmDeleteOrderInfo(Cursor cursor, int itemId,Context context,SQLiteDatabase db,Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("是否删除该记录?").setIcon(context.getDrawable(R.drawable.warning));
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String url = IP+"/deleteOrder/"+itemId;
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(url).delete().build();
                        try{
                            Response response = client.newCall(request).execute();
                            if(response.code()==200){
                                JSONObject jsonResponse = new JSONObject(response.body().string());
                                if(jsonResponse.getBoolean("success")){
                                    handleAfterDelete(String.valueOf(itemId),activity,db,context);
                                }
                                else{
                                    Looper.prepare();
                                    ProjectUtil.toastMsg(context,jsonResponse.getString("message"));
                                    Looper.loop();
                                }
                            }
                            else{
                                Looper.prepare();
                                ProjectUtil.toastMsg(context,"服务器出错");
                                Looper.loop();
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(BLUE);
    }

    public static void handleAfterDelete(String itemId, Activity activity, SQLiteDatabase db, Context context){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String sql = "delete from orderInfo where id =" + itemId;
                db.execSQL(sql);
                ProjectUtil.toastMsg(context,"删除成功");
                //重新获取数据库的数据来展示信息
//                showDayAndMonthMoney();
//                showOrderDetailList();
            }
        });
    }

    //设置点击后修改账单信息的事件
    public static void modifyOrderInfo(int itemIdInDatabase, Context context, SQLiteDatabase db) {
        //跳转到新增界面
        Intent intent = new Intent(context, OrderDetailActivity.class);
        //在数据库里找到这个数据
        Cursor cursor = db.query("orderInfo",null,"id="+itemIdInDatabase,null,null,null,null);
        //将已有的数据传过去
        cursor.moveToNext();
        Bundle bundle = new Bundle();
        bundle.putInt("id",cursor.getInt(0));
        bundle.putInt("year",cursor.getInt(1));
        bundle.putInt("month",cursor.getInt(2));
        bundle.putInt("day",cursor.getInt(3));
        bundle.putString("clock",cursor.getString(4));
        bundle.putFloat("money",cursor.getFloat(5));
        bundle.putString("bankName",cursor.getString(6));
        bundle.putString("orderRemark",cursor.getString(7));
        bundle.putString("costType",cursor.getString(8));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    //检测左右滑动的事件
    @SuppressLint("ClickableViewAccessibility")
    public static void listenUserTouch(Context context, ScrollView svOrderDetail){
        //检测用户的左右滑动
        svOrderDetail.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY, offsetX, offsetY;
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        Log.d("下",String.valueOf(startX));
                        break;
                    case MotionEvent.ACTION_UP:
                        offsetX = event.getX() - startX;
                        offsetY = event.getY() - startY;
                        if (Math.abs(offsetX) > Math.abs(offsetY)) {
                            if (offsetX < -5) { // left
                                ProjectUtil.toastMsg(context,"左滑动");
                            } else if (offsetX > 5) { // right
                                ProjectUtil.toastMsg(context,"右滑动");
                            }
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }
}
