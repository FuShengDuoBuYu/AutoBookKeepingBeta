package com.beta.autobookkeeping;

import static Util.Util.getLocalOrderInfo;
import static Util.Util.toastMsg;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ScrollerCompat;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.beta.autobookkeeping.SMStools.SMSDataBase;
import com.beta.autobookkeeping.SMStools.SMSReader;
import com.beta.autobookkeeping.SMStools.SMSService;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
                    //输入要操作的账本的信息
                    final String[] downLoadTableName = {null};
                    final EditText downloadInputServer = new EditText(SettingsActivity.this);
                    AlertDialog.Builder downloadInputBuilder = new AlertDialog.Builder(SettingsActivity.this);
                    downloadInputBuilder.setTitle("请输入您账单本的名称").setIcon(android.R.drawable.ic_dialog_info).setView(downloadInputServer)
                            .setNegativeButton("取消", null);
                    downloadInputBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            downLoadTableName[0] =  downloadInputServer.getText().toString();
                            //执行下载操作
                            downloadAllOrdersFromCloud(downLoadTableName[0]);
                        }
                    });
                    downloadInputBuilder.show();
                    break;
                case R.id.ll_uploadAllOrders:
                    //输入要操作的账本的信息
                    final String[] uploadTableName = {null};
                    final EditText uploadInputServer = new EditText(SettingsActivity.this);
                    AlertDialog.Builder uploadInputBuilder = new AlertDialog.Builder(SettingsActivity.this);
                    uploadInputBuilder.setTitle("请输入您账单本的名称").setIcon(android.R.drawable.ic_dialog_info).setView(uploadInputServer)
                            .setNegativeButton("取消", null);
                    uploadInputBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            uploadTableName[0] =  uploadInputServer.getText().toString();
                            //执行上传操作
                            uploadAllOrdersToCloud(uploadTableName[0]);
                        }
                    });
                    uploadInputBuilder.show();
                    break;
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
    private void downloadAllOrdersFromCloud(String tableName){
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
                            + "/" + dbName+"?useUnicode=true&characterEncoding=UTF-8"; // 构建连接mysql的字符串
                    String user = "root";                //自己的用户名
                    String password = "20011024Yangshuo!";           //自己的密码
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++=
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                    // 3.连接JDBC
                    try {
                        connection[0] = DriverManager.getConnection(url, user, password);
                        /*先找到表名是否存在*/
                        DatabaseMetaData dbMetaData = connection[0].getMetaData();
                        ResultSet rs = dbMetaData.getTables(null, null, null,new String[] { "TABLE" });
                        List<String> tableNames = new ArrayList<>();
                        Statement statement = connection[0].createStatement();
                        while(rs.next()) {
                            tableNames.add(rs.getString("TABLE_NAME"));
                        }
                        //如果有这个表,就进行表的下载
                        if(tableNames.contains(tableName)){
                            //获取本地的总数据
                            Cursor localData = getLocalOrderInfo(SettingsActivity.this);
                            //获取数据库里的数据
                            String getCloudData = "select * from "+tableName;
                            ResultSet cloudDataResult  = statement.executeQuery(getCloudData);
//                            ArrayList<Integer> idArray = new ArrayList<>();
//                            while(idResult.next()){
//                                idArray.add(idResult.getInt("id"));
//                            }
                            SMSDataBase smsDb = new SMSDataBase(SettingsActivity.this, "orderInfo", null, 1);
                            SQLiteDatabase db = smsDb.getWritableDatabase();
                            db.execSQL("delete from orderInfo");
//
                            db.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name= 'orderInfo'");
//                            Looper.prepare();
//                            toastMsg(SettingsActivity.this,"下载成功");
//                            Looper.loop();
                            //如果云没有本地的某条记录,就将其传上去
                            while (cloudDataResult.next()) {
                                    String sql = "insert into orderInfo" +"(id,year,month,day,clock,money,bankName,orderRemark,costType) "
                                            + "values (" + cloudDataResult.getInt("id") + "," + cloudDataResult.getInt("year") + "," + cloudDataResult.getInt("month") + "," + cloudDataResult.getInt("day") + ","
                                            + "'" + cloudDataResult.getString("clock") + "'" + "," + cloudDataResult.getDouble("money") + "," + "'" + cloudDataResult.getString("bankName") + "'" + "," + "'" +
                                            cloudDataResult.getString("orderRemark") + "'" + "," + "'" + cloudDataResult.getString("costType") + "'" + ");";
                                    Log.d("sql", sql);
                                    db.execSQL(sql);
                            }
                            Looper.prepare();
                            toastMsg(SettingsActivity.this,"下载成功");
                            Looper.loop();
                        }
                        //如果没有,就先提示用户是否创建这个表
                        else{
                            Looper.prepare();
                            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                            builder.setTitle("目前没有该账单本,确定要创建吗?").setNegativeButton("取消",null);
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String sql = "create table "+tableName + "(id integer(10),year int(4),month int(2),day int(2),clock varchar(20),money numeric(10,2),bankName varchar(255),orderRemark varchar(255),costType varchar(255));";
                                    try {
                                        java.sql.Statement statement = connection[0].createStatement();
                                        statement.executeUpdate(sql);
                                        toastMsg(SettingsActivity.this,"创建成功");
                                    } catch (SQLException e) {}
                                }
                            }).show();
                            Looper.loop();
                        }
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++=
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        //关闭数据库
                        try {
                            connection[0].close();
                        } catch (SQLException e) {
                            toastMsg(SettingsActivity.this,"关闭云数据库失败");
                        }
                        return;
                    } catch (SQLException e) {}
                }
            }
        });
        thread.start();
    }

    //推送数据到云
    private void uploadAllOrdersToCloud(String tableName){
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
                    } catch (InterruptedException e) {}
                    // 2.设置好IP/端口/数据库名/用户名/密码等必要的连接信息
                    String ip = "sh-cynosdbmysql-grp-h8u7vdmk.sql.tencentcdb.com";                 //本机IP
                    int port = 26934;                              //mysql默认端口
                    String dbName = "book_data";             //自己的数据库名
                    String url = "jdbc:mysql://" + ip + ":" + port
                            + "/" + dbName+"?useUnicode=true&characterEncoding=UTF-8"; // 构建连接mysql的字符串
                    String user = "root";                //自己的用户名
                    String password = "20011024Yangshuo!";           //自己的密码

                    // 3.连接JDBC
                    try {
                        connection[0] = DriverManager.getConnection(url, user, password);
//+++++++++++++++++++++++++++++以下为sql内容,并执行++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        /*先找到表名是否存在*/
                        DatabaseMetaData dbMetaData = connection[0].getMetaData();
                        ResultSet rs = dbMetaData.getTables(null, null, null,new String[] { "TABLE" });
                        List<String> tableNames = new ArrayList<>();
                        while(rs.next()) {
                            tableNames.add(rs.getString("TABLE_NAME"));
                        }
                        //如果有这个表,就进行上传
                        java.sql.Statement statement = connection[0].createStatement();
                        if(tableNames.contains(tableName)){
                            //获取本地的总数据
                            Cursor localData = getLocalOrderInfo(SettingsActivity.this);
                            //获取数据库里的数据id
                            String getCloudDataId = "select id from "+tableName;
                            ResultSet idResult  = statement.executeQuery(getCloudDataId);
                            ArrayList<Integer> idArray = new ArrayList<>();
                            while(idResult.next()){
                                idArray.add(idResult.getInt("id"));
                            }
                            //如果云没有本地的某条记录,就将其传上去
                            while (localData.moveToNext()) {
                                if(!idArray.contains(localData.getInt(0))) {
                                    String sql = "insert into " + tableName + " (id,year,month,day,clock,money,bankName,orderRemark,costType) "
                                            + "values (" + localData.getInt(0) + "," + localData.getInt(1) + "," + localData.getInt(2) + "," + localData.getInt(3) + ","
                                            + "'" + localData.getString(4) + "'" + "," + localData.getDouble(5) + "," + "'" + localData.getString(6) + "'" + "," + "'" + localData.getString(7) + "'" + "," + "'" + localData.getString(8) + "'" + ");";
                                    Log.d("sql", sql);
                                    statement.execute(sql);
                                }
                            }
                            Looper.prepare();
                            toastMsg(SettingsActivity.this,"推送成功");
                            Looper.loop();
                        }
                        //如果没有这个表,就提示用户是否创建
                        else{
                            Looper.prepare();
                            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                            builder.setTitle("目前没有该账单本,确定要创建吗?").setNegativeButton("取消",null);
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String sql = "create table "+tableName + "(id integer(10),year int(4),month int(2),day int(2),clock varchar(20),money numeric(10,2),bankName varchar(255),orderRemark varchar(255),costType varchar(255));";
                                    try {
                                        statement.executeUpdate(sql);
                                        toastMsg(SettingsActivity.this,"创建成功");
                                    } catch (SQLException e) {}
                                }
                            }).show();
                            Looper.loop();
                        }
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        //关闭数据库
                        try {
                            connection[0].close();
                        } catch (SQLException e) {
                            toastMsg(SettingsActivity.this,"关闭云数据库失败");
                        }
                        return;
                    } catch (SQLException e) {}
                }
            }
        });
        thread.start();
    }
}




