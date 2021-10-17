package com.beta.autobookkeeping;

import static Util.Util.toastMsg;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ScrollerCompat;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.beta.autobookkeeping.SMStools.SMSDataBase;
import com.beta.autobookkeeping.SMStools.SMSReader;
import com.beta.autobookkeeping.SMStools.SMSService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import Util.Util;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout llDeleteAllOrders,ll_searchLimitTimeOrders,ll_downloadAllOrders,ll_uploadAllOrders;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //开启读取短信线程
        startService(new Intent(SettingsActivity.this, SMSService.class));
        findViewsAndSetClick();
    }

    //找到各个按钮,并设置自己的点击事件
    public void findViewsAndSetClick(){
        OnClick onClick = new OnClick();
        //找到各个设置的按钮
        llDeleteAllOrders = findViewById(R.id.llDeleteAllOrders);
        ll_searchLimitTimeOrders = findViewById(R.id.ll_searchLimitTimeOrders);
        ll_downloadAllOrders = findViewById(R.id.ll_downloadAllOrders);
        ll_uploadAllOrders = findViewById(R.id.ll_uploadAllOrders);
        //设置点击事件
        llDeleteAllOrders.setOnClickListener(onClick);
        ll_searchLimitTimeOrders.setOnClickListener(onClick);
        ll_downloadAllOrders.setOnClickListener(onClick);
        ll_uploadAllOrders.setOnClickListener(onClick);
    }

    //自己写的一个实现的OnClick类
    class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.llDeleteAllOrders:
                    //弹出dialog对话框后,确实清除数据
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    //设置对话框的内容
                    builder.setTitle("清空账单记录").setMessage("您的所有账单记录将被清除,是否确认删除?");
                    //设置对话框的两个选项
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SMSDataBase smsDb = new SMSDataBase(SettingsActivity.this,"orderInfo",null,1);
                            SQLiteDatabase db = smsDb.getWritableDatabase();
                            String sql = "delete from orderInfo";
                            db.execSQL(sql);
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                        //不要忘记最后要show()
                    }).show();
                    break;
                case R.id.ll_searchLimitTimeOrders:
                    //获取查询的限制条件
                    getSearchDate();
                    break;
                case R.id.ll_downloadAllOrders:
                    downloadAllOrdersFromCloud();
                case R.id.ll_uploadAllOrders:
                    toastMsg(SettingsActivity.this,"待开发");
            }
        }
    }

    //获取用户要读取的指定日期
    public void getSearchDate(){
        Bundle bundle = new Bundle();
        //设置一个弹窗让用户选择是按日还是按月查找
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("请选择查询方式").setCancelable(true).setPositiveButton("按月查找", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //按月查找只显示月份的选择
                DatePickerDialog datePickerDialog = new DatePickerDialog(new ContextThemeWrapper(SettingsActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        bundle.putInt("year",i);
                        bundle.putInt("month",i1);
                        //跳转到查询账单详情页
                        Intent intent = new Intent(SettingsActivity.this,OrderItemSearchActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }, Util.getCurrentYear(), Util.getCurrentMonth() - 1, Util.getCurrentDay()){
                    @Override
                    //只显示年和月,不显示日
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        LinearLayout mSpinners = (LinearLayout) findViewById(getContext().getResources().getIdentifier("android:id/pickers", null, null));
                        if (mSpinners != null) {
                            NumberPicker mMonthSpinner = (NumberPicker) findViewById(getContext().getResources().getIdentifier("android:id/month", null, null));
                            NumberPicker mYearSpinner = (NumberPicker) findViewById(getContext().getResources().getIdentifier("android:id/year", null, null));
                            mSpinners.removeAllViews();
                            if (mMonthSpinner != null) {
                                mSpinners.addView(mMonthSpinner);
                            }
                            if (mYearSpinner != null) {
                                mSpinners.addView(mYearSpinner);
                            }
                        }
                        View dayPickerView = findViewById(getContext().getResources().getIdentifier("android:id/day", null, null));
                        if(dayPickerView != null){
                            dayPickerView.setVisibility(View.GONE);
                        }
                    }
                };
                datePickerDialog.show();
            }
        }).setNegativeButton("按日查找", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //按日查找显示日的选择
                DatePickerDialog datePicker = new DatePickerDialog(SettingsActivity.this,DatePickerDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        //获取用户输入的日期
                        bundle.putInt("year",year);
                        bundle.putInt("month",monthOfYear);
                        bundle.putInt("day",dayOfMonth);
                        //跳转到查询账单详情页
                        Intent intent = new Intent(SettingsActivity.this,OrderItemSearchActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }

                }, Util.getCurrentYear(), Util.getCurrentMonth()-1, Util.getCurrentDay()){};
                datePicker.show();
            }
        }).show();
    }

    //从云下载数据
    private void downloadAllOrdersFromCloud(){
        final Connection[] connection = new Connection[1];
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            toastMsg(SettingsActivity.this,"加载JDBC驱动失败");
            return;
        }
        //连接数据库（开辟一个新线程）
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 反复尝试连接，直到连接成功后退出循环
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(100);  // 每隔0.1秒尝试连接
                    } catch (InterruptedException e) {
                        Log.e("MainActivity", e.toString());
                    }
                    // 2.设置好IP/端口/数据库名/用户名/密码等必要的连接信息
                    String ip = "sh-cynosdbmysql-grp-h8u7vdmk.sql.tencentcdb.com";                 //本机IP
                    int port = 26934;                              //mysql默认端口
                    String dbName = "book_data";             //自己的数据库名
                    String url = "jdbc:mysql://" + ip + ":" + port
                            + "/" + dbName; // 构建连接mysql的字符串
                    String user = "root";                //自己的用户名
                    String password = "20011024Yangshuo!";           //自己的密码

                    // 3.连接JDBC
                    try {
                        connection[0] = DriverManager.getConnection(url, user, password);
//                        Log.d("MainActivity", "连接数据库成功!");
//
//                        //查询学生表
                        String sql = "insert into test(test)values(1)";
                        try {
//                            // 创建用来执行sql语句的对象
                            java.sql.Statement statement = connection[0].createStatement();
//                            // 执行sql查询语句并获取查询信息
                            statement.execute(sql);
//                            // 迭代打印出查询信息
//                            Log.d("MainActivity", "学生列表");
//                            Log.d("MainActivity", "id\t\t\tname\tage\tsex\t");
//                            while (rSet.next()) {
//                                Log.d("MainActivity", rSet.getString("id") + "\t" + rSet.getString("name")+"\t"
//                                        + rSet.getString("age") + "\t" + rSet.getString("sex") + "\t");
//                            }
                        } catch (SQLException e) {
                            Log.d("MainActivity", e.toString());
                        }
                        //关闭数据库
                        try {
//                            statement.close();
                            connection[0].close();
                        } catch (SQLException e) {
                            toastMsg(SettingsActivity.this,"关闭云数据库失败");                        }
                        return;
                    } catch (SQLException e) {
//                        toastMsg(SettingsActivity.this,"连接云数据库失败");
                    }
                }
            }
        });
        thread.start();
    }
}



