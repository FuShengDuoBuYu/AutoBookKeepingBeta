package com.beta.autobookkeeping;

import static Util.Util.BLUE;
import static Util.Util.getLocalOrderInfo;
import static Util.Util.toastMsg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ScrollerCompat;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;

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
import java.util.logging.LogRecord;

import Util.Util;

public class SettingsActivity extends AppCompatActivity {
    private static final int pbDownloadFamilyOrderCOMPLETED = 1;
    private static final int pbDownloadPersonalOrderCOMPLETED = 2;
    private static final int pbUploadFamilyOrderCOMPLETED = 3;
    private static final int pbUploadPersonalOrderCOMPLETED = 4;
    private static final int pbDownloadFamilyOrderNOTCOMPLETED = 5;
    private static final int pbDownloadPersonalOrderNOTCOMPLETED = 6;
    private static final int pbUploadFamilyOrderNOTCOMPLETED = 7;
    private static final int pbUploadPersonalOrderNOTCOMPLETED = 8;
    private LinearLayout llDeleteAllOrders,ll_searchLimitTimeOrders,ll_downloadAllOrders,ll_uploadAllOrders,ll_uploadFamilyOrder,ll_downloadFamilyOrder;
    private ProgressBar pbUploadPersonalOrder,pbDownloadPersonalOrder,pbUploadFamilyOrder,pbDownloadFamilyOrder;
    //用来上传下载完成后取消显示进度条的handler
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case(pbDownloadFamilyOrderNOTCOMPLETED):
                    pbDownloadFamilyOrder.setVisibility(View.VISIBLE);
                    break;
                case(pbDownloadPersonalOrderNOTCOMPLETED):
                    pbDownloadPersonalOrder.setVisibility(View.VISIBLE);
                    break;
                case(pbUploadFamilyOrderNOTCOMPLETED):
                    pbUploadFamilyOrder.setVisibility(View.VISIBLE);
                    break;
                case(pbUploadPersonalOrderNOTCOMPLETED):
                    pbUploadPersonalOrder.setVisibility(View.VISIBLE);
                    break;
                case(pbDownloadFamilyOrderCOMPLETED):
                    pbDownloadFamilyOrder.setVisibility(View.INVISIBLE);
                    break;
                case(pbDownloadPersonalOrderCOMPLETED):
                    pbDownloadPersonalOrder.setVisibility(View.INVISIBLE);
                    break;
                case(pbUploadFamilyOrderCOMPLETED):
                    pbUploadFamilyOrder.setVisibility(View.INVISIBLE);
                    break;
                case(pbUploadPersonalOrderCOMPLETED):
                    pbUploadPersonalOrder.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };
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
        pbDownloadFamilyOrder = findViewById(R.id.pbDownloadFamilyOrder);
        pbDownloadPersonalOrder = findViewById(R.id.pbDownloadPersonalOrder);
        pbUploadFamilyOrder = findViewById(R.id.pbUploadFamilyOrder);
        pbUploadPersonalOrder = findViewById(R.id.pbUploadPersonalOrder);
        pbDownloadFamilyOrder.setVisibility(View.INVISIBLE);
        pbDownloadPersonalOrder.setVisibility(View.INVISIBLE);
        pbUploadFamilyOrder.setVisibility(View.INVISIBLE);
        pbUploadPersonalOrder.setVisibility(View.INVISIBLE);
        ll_uploadFamilyOrder = findViewById(R.id.ll_uploadFamilyOrder);
        ll_downloadFamilyOrder = findViewById(R.id.ll_downloadFamilyOrder);
        //设置点击事件
        llDeleteAllOrders.setOnClickListener(onClick);
        ll_searchLimitTimeOrders.setOnClickListener(onClick);
        ll_downloadAllOrders.setOnClickListener(onClick);
        ll_uploadAllOrders.setOnClickListener(onClick);
        ll_uploadFamilyOrder.setOnClickListener(onClick);
        ll_downloadFamilyOrder.setOnClickListener(onClick);
    }

    //自己写的一个实现的OnClick类
    class OnClick implements View.OnClickListener {
        @SuppressLint({"UseCompatLoadingForDrawables", "NonConstantResourceId"})
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.llDeleteAllOrders:
                    //弹出dialog对话框后,确实清除数据
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    //设置对话框的内容
                    builder.setTitle("清空账单记录").setMessage("您的所有本地账单记录将被清除,是否确认删除?").setIcon(SettingsActivity.this.getDrawable(R.drawable.warning));
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
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(BLUE);
                    break;
                case R.id.ll_searchLimitTimeOrders:
                    //获取查询的限制条件
                    getSearchDate();
                    break;
                case R.id.ll_downloadAllOrders:
                    //输入要操作的账本的信息
                    AlertDialog.Builder warning = new AlertDialog.Builder(SettingsActivity.this);
                    warning.setTitle("可能丢失数据!").setIcon(SettingsActivity.this.getDrawable(R.drawable.warning)).setMessage("请务必保证已经上传个人账单到个人云").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final String[] downLoadTableName = {null};
                            final EditText downloadInputServer = new EditText(SettingsActivity.this);
                            AlertDialog.Builder downloadInputBuilder = new AlertDialog.Builder(SettingsActivity.this);
                            downloadInputBuilder.setTitle("请输入您账单本的名称").setMessage("注:账单本名称需由小写字母开头,且由小写字母和数字构成\n如:test123order").setIcon(SettingsActivity.this.getDrawable(R.drawable.download)).setView(downloadInputServer)
                                    .setNegativeButton("取消", null);
                            downloadInputBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    downLoadTableName[0] =  downloadInputServer.getText().toString();
                                    //执行下载操作
                                    downloadAllOrdersFromCloud(downLoadTableName[0]);
                                }
                            });
                            AlertDialog dialog = downloadInputBuilder.create();
                            dialog.show();
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(BLUE);
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                        }
                    }).setNegativeButton("取消",null);
                    AlertDialog downloadDialog = warning.create();
                    downloadDialog.show();
                    downloadDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(BLUE);
                    downloadDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                    break;
                    

                case R.id.ll_uploadAllOrders:
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SettingsActivity.this);
                    builder1.setIcon(R.drawable.warning).setTitle("确认您当前账单").setMessage("请确保您当前账单为个人账单信息而非家庭账单信息\n若提交家庭账单信息,则会丢失个人账单数据");
                    builder1.setNegativeButton("取消",null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //输入要操作的账本的信息
                            final String[] uploadTableName = {null};
                            final EditText uploadInputServer = new EditText(SettingsActivity.this);
                            AlertDialog.Builder uploadInputBuilder = new AlertDialog.Builder(SettingsActivity.this);
                            uploadInputBuilder.setTitle("请输入您账单本的名称").setMessage("注:账单本名称需由小写字母开头,且由小写字母和数字构成\n如:test123order").setIcon(SettingsActivity.this.getDrawable(R.drawable.upload))
                                    .setView(uploadInputServer).setNegativeButton("取消", null);
                            uploadInputBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    uploadTableName[0] =  uploadInputServer.getText().toString();
                                    //执行上传操作
                                    uploadAllOrdersToCloud(uploadTableName[0]);
                                }
                            });
                            AlertDialog uploadDialog = uploadInputBuilder.create();
                            uploadDialog.show();
                            uploadDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(BLUE);
                            uploadDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                        }
                    }).show();
                    break;

                case R.id.ll_uploadFamilyOrder:
                    //输入要操作的家庭账本的信息
                    final String[] uploadFamilyTableName = {null,null,null,null,null};
                    final EditText uploadFamilyInputServer = new EditText(SettingsActivity.this);
                    uploadFamilyInputServer.setHint("家庭账单名");
                    final EditText uploadFamilyInputPerson1 = new EditText(SettingsActivity.this);
                    uploadFamilyInputPerson1.setHint("本人账单名");
                    final EditText uploadFamilyInputPerson2 = new EditText(SettingsActivity.this);
                    uploadFamilyInputPerson2.setHint("家庭成员账单名");
                    LinearLayout linearLayout = new LinearLayout(SettingsActivity.this);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.addView(uploadFamilyInputServer);
                    linearLayout.addView(uploadFamilyInputPerson1);
                    linearLayout.addView(uploadFamilyInputPerson2);
                    AlertDialog.Builder uploadFamilyInputBuilder = new AlertDialog.Builder(SettingsActivity.this);
                    uploadFamilyInputBuilder.setTitle("请输入您家庭与个人账单本的名称").setMessage("注:账单本名称需由小写字母开头,且由小写字母和数字构成\n如:test123order").setIcon(SettingsActivity.this.getDrawable(R.drawable.upload))
                            .setView(linearLayout).setNegativeButton("取消", null);
                    uploadFamilyInputBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            uploadFamilyTableName[0] =  uploadFamilyInputServer.getText().toString();
                            uploadFamilyTableName[1] =  uploadFamilyInputPerson1.getText().toString();
                            uploadFamilyTableName[2] =  uploadFamilyInputPerson2.getText().toString();
                            //执行上传操作
                            uploadFamilyOrdersToCloud(uploadFamilyTableName[0],uploadFamilyTableName[1],uploadFamilyTableName[2]);
                        }
                    });
                    AlertDialog uploadFamilyDialog = uploadFamilyInputBuilder.create();
                    uploadFamilyDialog.show();
                    uploadFamilyDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(BLUE);
                    uploadFamilyDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                    break;


                case R.id.ll_downloadFamilyOrder:
                    //输入要操作的账本的信息
                    AlertDialog.Builder familyWarning = new AlertDialog.Builder(SettingsActivity.this);
                    familyWarning.setTitle("可能丢失数据!").setIcon(SettingsActivity.this.getDrawable(R.drawable.warning)).setMessage("请务必保证已经上传个人账单到个人云\n同时请确认已经上传个人账单到家庭云").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final String[] downLoadFamilyTableName = {null};
                            final EditText downloadFamilyInputServer = new EditText(SettingsActivity.this);
                            AlertDialog.Builder downloadFamilyInputBuilder = new AlertDialog.Builder(SettingsActivity.this);
                            downloadFamilyInputBuilder.setTitle("请输入您家庭账单本的名称").setMessage("注:账单本名称需由小写字母开头,且由小写字母和数字构成\n如:test123order").setIcon(SettingsActivity.this.getDrawable(R.drawable.download)).setView(downloadFamilyInputServer)
                                    .setNegativeButton("取消", null);
                            downloadFamilyInputBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    downLoadFamilyTableName[0] =  downloadFamilyInputServer.getText().toString();
                                    //执行下载操作
                                    downloadFamilyOrdersFromCloud(downLoadFamilyTableName[0]);
                                }
                            });
                            AlertDialog dialog = downloadFamilyInputBuilder.create();
                            dialog.show();
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(BLUE);
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                        }
                    }).setNegativeButton("取消",null);
                    AlertDialog downloadFamilyDialog = familyWarning.create();
                    downloadFamilyDialog.show();
                    downloadFamilyDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(BLUE);
                    downloadFamilyDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                    break;
            }
        }
    }

    //获取用户要读取的指定日期
    public void getSearchDate(){
        Bundle bundle = new Bundle();
        //设置一个弹窗让用户选择是按日还是按月查找
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("请选择查询方式").setCancelable(true).setIcon(SettingsActivity.this.getDrawable(R.drawable.search)).setPositiveButton("按月查找", new DialogInterface.OnClickListener() {
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
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(BLUE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(BLUE);
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
                            //设置进度条显示
                            Message msgNotComplete = new Message();
                            msgNotComplete.what = pbDownloadPersonalOrderNOTCOMPLETED;
                            handler.sendMessage(msgNotComplete);
                            //获取本地的总数据
                            Cursor localData = getLocalOrderInfo(SettingsActivity.this);
                            //获取数据库里的数据
                            String getCloudData = "select * from "+tableName;
                            ResultSet cloudDataResult  = statement.executeQuery(getCloudData);
                            SMSDataBase smsDb = new SMSDataBase(SettingsActivity.this, "orderInfo", null, 1);
                            SQLiteDatabase db = smsDb.getWritableDatabase();
                            db.execSQL("delete from orderInfo");
                            db.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name= 'orderInfo'");
                            //如果云没有本地的某条记录,就将其传上去
                            while (cloudDataResult.next()) {
                                    String sql = "insert into orderInfo" +"(id,year,month,day,clock,money,bankName,orderRemark,costType) "
                                            + "values (" + cloudDataResult.getInt("id") + "," + cloudDataResult.getInt("year") + "," + cloudDataResult.getInt("month") + "," + cloudDataResult.getInt("day") + ","
                                            + "'" + cloudDataResult.getString("clock") + "'" + "," + cloudDataResult.getDouble("money") + "," + "'" + cloudDataResult.getString("bankName") + "'" + "," + "'" +
                                            cloudDataResult.getString("orderRemark") + "'" + "," + "'" + cloudDataResult.getString("costType") + "'" + ");";
                                    Log.d("sql", sql);
                                    db.execSQL(sql);
                            }
                            //设置进度条不显示
                            Message msgComplete = new Message();
                            msgComplete.what = pbDownloadPersonalOrderCOMPLETED;
                            handler.sendMessage(msgComplete);
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
                            //显示进度条
                            Message msgNotComplete = new Message();
                            msgNotComplete.what = pbUploadPersonalOrderNOTCOMPLETED;
                            handler.sendMessage(msgNotComplete);
                            //获取本地的总数据
                            Cursor localData = getLocalOrderInfo(SettingsActivity.this);
                            //删除云端的数据
                            String deleteCloudData = "delete from "+tableName + ";";
                            statement.executeUpdate(deleteCloudData);
                            //将其传上去
                            while (localData.moveToNext()) {
                                String sql = "insert into " + tableName + " (id,year,month,day,clock,money,bankName,orderRemark,costType) "
                                        + "values (" + localData.getInt(0) + "," + localData.getInt(1) + "," + localData.getInt(2) + "," + localData.getInt(3) + ","
                                        + "'" + localData.getString(4) + "'" + "," + localData.getDouble(5) + "," + "'" + localData.getString(6) + "'" + "," + "'" + localData.getString(7) + "'" + "," + "'" + localData.getString(8) + "'" + ");";
                                statement.execute(sql);
                            }
                            //不显示进度条
                            Message msgComplete = new Message();
                            msgComplete.what = pbUploadPersonalOrderCOMPLETED;
                            handler.sendMessage(msgComplete);
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

    //为家庭云上传
    private void uploadFamilyOrdersToCloud(String familyTableName,String personalTableName1,String personalTableName2){
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
                        //如果没有个人账单表,就提示用户创建个人账单本并结束这个方法
                        if(!tableNames.contains(personalTableName1)||!tableNames.contains(personalTableName2)){
                            Looper.prepare();
                            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                            builder.setTitle("无该个人账单本,请先创建个人账本").setPositiveButton("确定", null).show();
                            Looper.loop();
                            return;
                        }
                        //如果有家庭表,就进行上传
                        java.sql.Statement statement = connection[0].createStatement();
                        if(tableNames.contains(familyTableName)){
                            //显示进度条
                            Message msgNotComplete = new Message();
                            msgNotComplete.what = pbUploadFamilyOrderNOTCOMPLETED;
                            handler.sendMessage(msgNotComplete);
                            //将个人的数据先同步到个人云
                            //获取本地的总数据
                            Cursor localData = getLocalOrderInfo(SettingsActivity.this);
                            //删除云端的数据
                            String deleteCloudData = "delete from "+personalTableName1 + ";";
                            statement.executeUpdate(deleteCloudData);
                            //将其传上去
                            while (localData.moveToNext()) {
                                String sql = "insert into " + personalTableName1 + " (id,year,month,day,clock,money,bankName,orderRemark,costType) "
                                        + "values (" + localData.getInt(0) + "," + localData.getInt(1) + "," + localData.getInt(2) + "," + localData.getInt(3) + ","
                                        + "'" + localData.getString(4) + "'" + "," + localData.getDouble(5) + "," + "'" + localData.getString(6) + "'" + "," + "'" + localData.getString(7) + "'" + "," + "'" + localData.getString(8) + "'" + ");";
                                statement.execute(sql);
                            }
                            //先清空家庭表
                            String deleteCloudData2 = "delete from "+familyTableName + ";";
                            statement.executeUpdate(deleteCloudData2);
                            //将用户云端的数据写入家庭表中
                            String insertPerson1 = "insert into "+familyTableName +" select * from "+personalTableName1+";";
                            String insertPerson2 = "insert into "+familyTableName +" select * from "+personalTableName2+";";
                            statement.execute(insertPerson1);
                            statement.execute(insertPerson2);
                            //不显示进度条
                            Message msgComplete = new Message();
                            msgComplete.what = pbUploadFamilyOrderCOMPLETED;
                            handler.sendMessage(msgComplete);
                            Looper.prepare();
                            toastMsg(SettingsActivity.this,"家庭表更新成功");
                            Looper.loop();
                        }
                        //如果没有家庭表,就提示用户是否创建
                        else{
                            Looper.prepare();
                            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                            builder.setTitle("目前没有该家庭账单本,确定要创建吗?").setNegativeButton("取消",null);
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String sql = "create table "+ familyTableName + "(id integer(10),year int(4),month int(2),day int(2),clock varchar(20),money numeric(10,2),bankName varchar(255),orderRemark varchar(255),costType varchar(255));";
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
//        toastMsg(SettingsActivity.this,familyTableName+personalTableName1+personalTableName2);
    }

    //为家庭云下载
    private void downloadFamilyOrdersFromCloud(String familyTableName){
        final Connection[] connection = new Connection[1];
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
//            toastMsg(SettingsActivity.this,"加载JDBC驱动失败");
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
                        if(tableNames.contains(familyTableName)){
                            //设置进度条显示
                            Message msgNotComplete = new Message();
                            msgNotComplete.what = pbDownloadFamilyOrderNOTCOMPLETED;
                            handler.sendMessage(msgNotComplete);
                            //获取本地的总数据
                            Cursor localData = getLocalOrderInfo(SettingsActivity.this);
                            //获取数据库里的数据
                            String getCloudData = "select * from "+familyTableName+" order by year, month, day;";
                            ResultSet cloudDataResult  = statement.executeQuery(getCloudData);
                            SMSDataBase smsDb = new SMSDataBase(SettingsActivity.this, "orderInfo", null, 1);
                            SQLiteDatabase db = smsDb.getWritableDatabase();
                            db.execSQL("delete from orderInfo");
                            db.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name= 'orderInfo'");
                            //如果云没有本地的某条记录,就将其传上去
                            while (cloudDataResult.next()) {
                                String sql = "insert into orderInfo" +"(year,month,day,clock,money,bankName,orderRemark,costType) "
                                        + "values ("+cloudDataResult.getInt("year") + "," + cloudDataResult.getInt("month") + "," + cloudDataResult.getInt("day") + ","
                                        + "'" + cloudDataResult.getString("clock") + "'" + "," + cloudDataResult.getDouble("money") + "," + "'" + cloudDataResult.getString("bankName") + "'" + "," + "'" +
                                        cloudDataResult.getString("orderRemark") + "'" + "," + "'" + cloudDataResult.getString("costType") + "'" + ");";
                                Log.d("sql", sql);
                                db.execSQL(sql);
                            }
                            //设置进度条不显示
                            Message msgComplete = new Message();
                            msgComplete.what = pbDownloadFamilyOrderCOMPLETED;
                            handler.sendMessage(msgComplete);
                            Looper.prepare();
                            toastMsg(SettingsActivity.this,"下载家庭账单成功");
                            Looper.loop();
                        }
                        //如果没有,就先提示用户创建这个表
                        else{
                            Looper.prepare();
                            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                            builder.setTitle("目前没有该家庭账单本,请先上传创建").setPositiveButton("确定",null).show();
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
}




