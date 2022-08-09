package com.beta.autobookkeeping;

import static Util.Const.IP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.beta.autobookkeeping.SMStools.SMSApplication;
import com.beta.autobookkeeping.SMStools.SMSDataBase;
import com.beta.autobookkeeping.SMStools.SMSService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

import Util.Util;
import Util.SpUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderDetailActivity extends AppCompatActivity {
    //点击item修改的标志
    boolean isChangeOrderInfo = false;
    private Button btnSaveChanges,btnCostType,btnGetCurrentTime,btnPayWay,btnOrderType;
    private EditText etOrderNumber,etOrderRemark;
    private TextView tv_order_status;
    int costType,payWayType,orderTypeIndex;
    //如果读取短信内容,短信的实质信息
    String[] msgContent;
    //如果是点击修改,则bundle不为null
    Bundle bundle;
    final String[] costTypes = {"消费","饮食","交通","体育","聚会","娱乐","购物","通讯","红包","医疗","一卡通","学习","其他"};
    final String[] payWays = {"银行卡","支付宝","微信","现金"};
    final String[] orderType = {"支出","收入"};
    private int orderYear = Util.getCurrentYear(),orderMonth = Util.getCurrentMonth(),orderDay = Util.getCurrentDay(),orderHour = Util.getCurrentHour(),orderMin = Util.getCurrentMinute();
    private String orderTime = orderMonth+"月"+orderDay+"日"+" "+orderHour+ ":"+orderMin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        findViews();
        //页面进来的时候就查看是否有短信信息或者数据信息在这里,若有,则处理信息自动匹配
        bundle = getIntent().getExtras();
        handleMsg(bundle);
        //开启读取短信线程
        startService(new Intent(OrderDetailActivity.this, SMSService.class));
        //页面一进来就执行获取当前时间的操作,不能放在最前面
        String currentTime = Util.getCurrentTime();
        btnGetCurrentTime.setText(currentTime);
        //保存账单信息的按钮
        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDataBaseData();
            }
        });

        //选择消费类型的按钮
        btnCostType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //弹出选择消费类型的dialog
                showCostType();
            }
        });
        //选择账单是支出还是收入的按钮
        btnOrderType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrderType();
            }
        });
        //获取当前时间的按钮
        btnGetCurrentTime.setOnClickListener(new View.OnClickListener() {
            //点击后用户进行时间的选择
            @Override
            public void onClick(View view) {
                selectOrderTime();
            }
        });
        //选择消费方式的方法
        btnPayWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPayWay();
            }
        });
        //显示当前的账单状态
        tv_order_status.setText("当前版本:"+SpUtils.get(OrderDetailActivity.this,"OrderStatus","").toString());
    }

    //找到各个组件
    public void findViews(){
        btnGetCurrentTime = findViewById(R.id.btnGetCurrentTime);
        btnPayWay = findViewById(R.id.btnPayWay);
        etOrderRemark = findViewById(R.id.etOrderRemark);
        etOrderNumber = findViewById(R.id.etOrderNumber);
        btnGetCurrentTime = findViewById(R.id.btnGetCurrentTime);
        btnOrderType = findViewById(R.id.btnOrderType);
        btnCostType = findViewById(R.id.btnCostType);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        tv_order_status = findViewById(R.id.tv_order_status);
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
    public void handleMsg(Bundle bundle){
        //如果bundle是空,代表用户不是点击修改的
        if(bundle!=null)
            isChangeOrderInfo = true;
        //代表用户点击item进来修改
        if(isChangeOrderInfo){
            changeOrderInfo(bundle);
        }
        //代表用户读取短信获取
        else{
            SMSApplication smsApplication = new SMSApplication();;
            smsApplication = (SMSApplication) getApplication();
            String msg = smsApplication.getSMSMsg();
            smsApplication.setSMSMsg(null);
            if(msg!=null)
            msgContent = Util.getBankOrderInfo(msg);
        }
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
        if(msgContent != null){
            etOrderNumber.setText(msgContent[2]);
            btnOrderType.setText(msgContent[1]);
            btnPayWay.setText(msgContent[0]);
        }
        super.onStart();
    }

    //写入数据库数据
    public void setDataBaseData(){
        //家庭版下不允许修改账单信息
        if(SpUtils.get(OrderDetailActivity.this,"OrderStatus","").toString().equals("家庭版")){
            Util.toastMsg(OrderDetailActivity.this,"家庭版下不允许修改账单信息,请前往设置切换个人版");
            return;
        }
        SMSDataBase smsDb = new SMSDataBase(OrderDetailActivity.this, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        //执行更新数据库操作
        if(isChangeOrderInfo){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String url = IP+"/modifyOrder/"+bundle.getInt("id");
                    OkHttpClient client = new OkHttpClient();
                    JSONObject jsonObject = new JSONObject();
                    String sql = "update orderInfo set day="+orderDay+",month="+orderMonth+",clock='"+orderTime+
                            "',money="+(btnOrderType.getText().toString().equals("收入")?"":"-")+Double.valueOf(etOrderNumber.getText().toString())+",bankName='"+btnPayWay.getText()+
                            "',orderRemark='"+etOrderRemark.getText()+"',costType='"+(btnOrderType.getText().toString().equals("收入")?"收入":btnCostType.getText().toString())+"' where id="+
                            bundle.getInt("id");
                    try {
                        jsonObject.put("day",orderDay);
                        jsonObject.put("month",orderMonth);
                        jsonObject.put("clock",orderTime);
                        jsonObject.put("money",(btnOrderType.getText().toString().equals("收入")?"":"-")+Double.valueOf(etOrderNumber.getText().toString()));
                        jsonObject.put("bankName",btnPayWay.getText());
                        jsonObject.put("orderRemark",etOrderRemark.getText());
                        jsonObject.put("costType",(btnOrderType.getText().toString().equals("收入")?"收入":btnCostType.getText().toString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));
                    Request request = new Request.Builder()
                            .url(url)
                            .put(body)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        if(response.code()==200){
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            if(jsonResponse.getBoolean("success")){
                                refreshLocalSql(db,null);
                            }
                            else{
                                Looper.prepare();
                                Util.toastMsg(OrderDetailActivity.this,jsonResponse.getString("message"));
                                Looper.loop();
                            }
                        }
                        else{
                            Looper.prepare();
                            Util.toastMsg(OrderDetailActivity.this,"服务器出错");
                            Looper.loop();
                        }
                        // str为json字符串
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        //新增账单数据
        else{
            //设置数据库数据
            ContentValues values = new ContentValues();
            values.put("year",orderYear);
            values.put("month",orderMonth);
            values.put("day",orderDay);
            values.put("clock",btnGetCurrentTime.getText().toString());
            //根据内容确定写入数组的金额还是用户的金额,根据支出还是收入记录正负号
            //根据内容确定写入数组还是用户默认
            //用户手动添加的账单信息
            if(msgContent == null){
                if(btnOrderType.getText().toString().equals("收入")){
                    //收入记正数
                    values.put("money",Double.valueOf(etOrderNumber.getText().toString()));
                    //costType记收入
                    values.put("costType","收入");
                }
                else{
                    //支出记负数
                    values.put("money",0.0-(Double.parseDouble(etOrderNumber.getText().toString())));
                    //获取支出的类型
                    values.put("costType",btnCostType.getText().toString());
                }
                values.put("bankName",btnPayWay.getText().toString());

            }
            //短信自动读取的账单信息
            else{
                if(msgContent[1].equals("收入")){
                    //收入记正数
                    values.put("money",Double.parseDouble(msgContent[2]));
                    //costType记收入
                    values.put("costType","收入");
                }
                else{
                    //支出记负数
                    values.put("money",0.0-Double.parseDouble(msgContent[2]));
                    //costType获取用户支出类型
                    values.put("costType",btnCostType.getText().toString());
                }
                if(!msgContent[0].equals("")){
                    values.put("bankName",msgContent[0]);
                }
            }
            //写入账单备注
            values.put("orderRemark",etOrderRemark.getText().toString());
            values.put("userId",(String) SpUtils.get(OrderDetailActivity.this,"phoneNum",""));
            //传递给后端
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String url = IP+"/addOrder";
                    OkHttpClient client = new OkHttpClient();
                    JSONObject jsonObject = new JSONObject();
                    try {
                        Set<String> keys = values.keySet();
                        for(String key:keys){
                            jsonObject.put(key,values.get(key));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));
                    Request requst = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    try {
                        Response response = client.newCall(requst).execute();
                        if(response.code()==200){
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            if(jsonResponse.getBoolean("success")){
                                values.put("id",Integer.valueOf(jsonResponse.getString("data")));
                                refreshLocalSql(db,values);
                            }
                            else{
                                Looper.prepare();
                                Util.toastMsg(OrderDetailActivity.this,jsonResponse.getString("message"));
                                Looper.loop();
                            }
                        }
                        else{
                            Looper.prepare();
                            Util.toastMsg(OrderDetailActivity.this,"服务器出错");
                            Looper.loop();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    //后端返回成功后更新本地数据库
    public void refreshLocalSql(SQLiteDatabase db,ContentValues values){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isChangeOrderInfo){
                    String sql = "update orderInfo set day="+orderDay+",month="+orderMonth+",clock='"+orderTime+
                            "',money="+(btnOrderType.getText().toString().equals("收入")?"":"-")+Double.valueOf(etOrderNumber.getText().toString())+",bankName='"+btnPayWay.getText()+
                            "',orderRemark='"+etOrderRemark.getText()+"',costType='"+(btnOrderType.getText().toString().equals("收入")?"收入":btnCostType.getText().toString())+"' where id="+
                            bundle.getInt("id");
                    db.execSQL(sql);
                    Util.toastMsg(OrderDetailActivity.this,"保存成功");
                }
                else{
                    db.insert("orderInfo",null,values);
                    Util.toastMsg(OrderDetailActivity.this,"保存成功");
                }
                finish();
            }
        });
    }

    //修改数据库中的数据
    public void changeOrderInfo(Bundle bundle){
        if(isChangeOrderInfo){
            //将数据传过来
            etOrderNumber.setText(String.valueOf((Math.abs(bundle.getFloat("money")))));
            orderYear = bundle.getInt("year");
            orderMonth = bundle.getInt("month");
            orderDay = bundle.getInt("day");
            orderTime = bundle.getString("clock");
            btnGetCurrentTime.setText(orderTime);
            etOrderRemark.setText(bundle.getString("orderRemark"));
            btnOrderType.setText(bundle.getFloat("money")>0?"收入":"支出");
            btnPayWay.setText(bundle.getString("bankName"));
            btnCostType.setText(bundle.getFloat("money")>0?"其他":bundle.getString("costType"));
            isChangeOrderInfo = true;
        }
    }


    //点击时间按钮后进行时间的选择
    public void selectOrderTime(){
        //显示日期选择器
        DatePickerDialog datePicker = new DatePickerDialog(OrderDetailActivity.this,DatePickerDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                //获取用户输入的日期
                orderYear = year;
                orderMonth = monthOfYear+1;
                orderDay = dayOfMonth;
            }

        }, Util.getCurrentYear(), Util.getCurrentMonth()-1, Util.getCurrentDay()){
            //不允许选择今天以后的时间
            @Override
            public void onDateChanged(@NonNull DatePicker view, int year, int month, int dayOfMonth) {
                if (year > Util.getCurrentYear())
                    view.updateDate(Util.getCurrentYear(),Util.getCurrentMonth(), Util.getCurrentDay());

                if (month > Util.getCurrentMonth() && year == Util.getCurrentYear())
                    view.updateDate(Util.getCurrentYear(), Util.getCurrentMonth(),  Util.getCurrentDay());

                if (dayOfMonth > Util.getCurrentDay() && year == Util.getCurrentYear() && month == Util.getCurrentMonth())
                    view.updateDate(Util.getCurrentYear(), Util.getCurrentMonth(),  Util.getCurrentDay());
            }

            //当该dialog被删除的时候,就把值赋值过去,这里要注意要查到dialog的生命周期十分重要
            @Override
            public void dismiss() {
                orderTime = ((orderMonth>0&&orderMonth<10)?("0"+orderMonth):orderMonth)+"月"+orderDay+"日"+" "+orderHour+ ":"+((orderMin>0&&orderMin<10)?("0"+orderMin):orderMin);
                btnGetCurrentTime.setText(orderTime);
                super.dismiss();
            }
        };
        datePicker.show();
        //显示时间选择器
        TimePickerDialog timePicker = new TimePickerDialog(OrderDetailActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                orderHour = i;
                orderMin = i1;
            }
        },Util.getCurrentHour(),Util.getCurrentMinute(),true);
        timePicker.show();
        //选择好以后将ordertime修改
        orderTime = ((orderMonth>0&&orderMonth<10)?("0"+orderMonth):orderMonth)+"月"+orderDay+"日"+" "+orderHour+ ":"+((orderMin>0&&orderMin<10)?("0"+orderMin):orderMin);
    }
}
