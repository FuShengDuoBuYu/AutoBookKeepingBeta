package com.beta.autobookkeeping.activity.orderDetail;

import static Util.ConstVariable.COST_TYPE;
import static Util.ConstVariable.IP;
import static Util.ConstVariable.ORDER_TYPE;
import static Util.ConstVariable.PAY_WAY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.BaseApplication;
import com.beta.autobookkeeping.smsTools.SMSDataBase;
import com.beta.autobookkeeping.smsTools.SMSService;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.config.ConfigBean;
import com.hss01248.dialog.interfaces.MyItemDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import Util.ProjectUtil;
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
    final String[] costTypes = COST_TYPE;
    final String[] payWays = PAY_WAY;
    final String[] orderType = ORDER_TYPE;
    private int orderYear = ProjectUtil.getCurrentYear(),orderMonth = ProjectUtil.getCurrentMonth(),orderDay = ProjectUtil.getCurrentDay(),orderHour = ProjectUtil.getCurrentHour(),orderMin = ProjectUtil.getCurrentMinute();
    private String orderTime = orderMonth+"月"+orderDay+"日"+" "+orderHour+ ":"+orderMin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        findViews();
        initViews();
        //页面进来的时候就查看是否有短信信息或者数据信息在这里,若有,则处理信息自动匹配
        bundle = getIntent().getExtras();
        handleMsg(bundle);
    }

    //找到各个组件
    private void findViews(){
        btnGetCurrentTime = findViewById(R.id.btnGetCurrentTime);
        btnPayWay = findViewById(R.id.btnPayWay);
        etOrderRemark = findViewById(R.id.etOrderRemark);
        etOrderNumber = findViewById(R.id.etOrderNumber);
        btnGetCurrentTime = findViewById(R.id.btnGetCurrentTime);
        btnOrderType = findViewById(R.id.btnOrderType);
        btnCostType = findViewById(R.id.btnCostType);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
    }

    private void initViews(){
        //页面一进来就执行获取当前时间的操作,不能放在最前面
        String currentTime = ProjectUtil.getCurrentTime();
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
    }

    //选择消费类型并显示的方法
    private void showCostType(){
        costType = -1;
        StyledDialog.buildIosSingleChoose(new ArrayList<>(Arrays.asList(costTypes)), new MyItemDialogListener() {
            @Override
            public void onItemClick(CharSequence text, int position) {
                costType = position;
                btnCostType.setText(text.toString());
            }
        }).setTitle("请选择消费类别").setTitleSize(40).setTitleColor(R.color.primary_font).show();
    }

    //选择支付方式类型并显示的方法
    private void showPayWay(){
        payWayType = -1;
        StyledDialog.buildIosSingleChoose(new ArrayList<>(Arrays.asList(payWays)), new MyItemDialogListener() {
            @Override
            public void onItemClick(CharSequence text, int position) {
                payWayType = position;
                btnPayWay.setText(text.toString());
            }
        }).setTitle("请选择支出方式").setTitleSize(40).setTitleColor(R.color.primary_font).show();
    }

    //选择账单是收入还是支出并显示的方法
    private void showOrderType(){
        orderTypeIndex = -1;
        StyledDialog.buildIosSingleChoose(new ArrayList<>(Arrays.asList(orderType)), new MyItemDialogListener() {
            @Override
            public void onItemClick(CharSequence text, int position) {
                orderTypeIndex = position;
                btnOrderType.setText(text.toString());
            }
        }).setTitle("请选择账单类型").setTitleSize(40).setTitleColor(R.color.primary_font).show();
    }

    //获取短信内容并处理的方法
    private void handleMsg(Bundle bundle){
        //不是手动添加
        if(bundle != null){
            //修改账单
            if(bundle.getInt("id")!=0){
                isChangeOrderInfo = true;
                changeOrderInfo(bundle);
            }
            else{
                etOrderNumber.setText(String.valueOf(bundle.getDouble("money")));
                btnOrderType.setText(bundle.getString("orderType"));
                btnPayWay.setText(bundle.getString("payWay"));
            }
        }
    }

    //写入数据库数据
    private void setDataBaseData(){
        StyledDialog.buildLoading().show();
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
                        }
                        else{
                            Looper.prepare();
                            ProjectUtil.toastMsg(OrderDetailActivity.this,"服务器出错");
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
                                uploadLBSData(values);
                                refreshLocalSql(db,values);
                            }
                        }
                        else{
                            Looper.prepare();
                            ProjectUtil.toastMsg(OrderDetailActivity.this,"服务器出错");
                            Looper.loop();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * 上传LBS数据
     * 删除v1.3
     * @param values
     */
    private void uploadLBSData(ContentValues values) {
        final double[] latitude = new double[1];
        final double[] longitude = new double[1];
        //上传后端
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/addLBSOrder";
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("latitude",latitude[0]);
                    jsonObject.put("longitude",longitude[0]);
                    jsonObject.put("money",values.get("money"));
                    jsonObject.put("userId",values.get("userId"));
                    jsonObject.put("orderId",values.get("id"));
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
                            Looper.prepare();
                            StyledDialog.dismiss();
                            ProjectUtil.toastMsg(OrderDetailActivity.this,"保存成功");
                            finish();
                            Looper.loop();
                        }
                    }
                    else{
                        Looper.prepare();
                        ProjectUtil.toastMsg(OrderDetailActivity.this,"服务器出错");
                        Looper.loop();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        //利用高德获取经纬度
        AMapLocationClient mLocationClient = null;
        try {
            mLocationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        //定位成功回调信息，设置相关消息
                        aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                        latitude[0] = aMapLocation.getLatitude();//获取纬度
                        longitude[0] = aMapLocation.getLongitude();//获取经度
                        Log.d("经纬度", latitude[0] +","+ longitude[0]);
                        t.start();
                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError", "location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo());
                    }
                }
            }
        });
        //初始化定位参数
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);
        //获取最近3s内精度最高的一次定位结果：
        mLocationOption.setOnceLocationLatest(true);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();


    }

    //后端返回成功后更新本地数据库
    private void refreshLocalSql(SQLiteDatabase db,ContentValues values){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isChangeOrderInfo){
                    String sql = "update orderInfo set day="+orderDay+",month="+orderMonth+",clock='"+orderTime+
                            "',money="+(btnOrderType.getText().toString().equals("收入")?"":"-")+Double.valueOf(etOrderNumber.getText().toString())+",bankName='"+btnPayWay.getText()+
                            "',orderRemark='"+etOrderRemark.getText()+"',costType='"+(btnOrderType.getText().toString().equals("收入")?"收入":btnCostType.getText().toString())+"' where id="+
                            bundle.getInt("id");
                    db.execSQL(sql);
                    finish();
                }
                else{
                    db.insert("orderInfo",null,values);
                }
            }
        });
    }

    //修改数据库中的数据
    private void changeOrderInfo(Bundle bundle){
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
    private void selectOrderTime(){
        //显示日期选择器
        DatePickerDialog datePicker = new DatePickerDialog(OrderDetailActivity.this,DatePickerDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                //获取用户输入的日期
                orderYear = year;
                orderMonth = monthOfYear+1;
                orderDay = dayOfMonth;
            }

        }, ProjectUtil.getCurrentYear(), ProjectUtil.getCurrentMonth()-1, ProjectUtil.getCurrentDay()){
            //不允许选择今天以后的时间
            @Override
            public void onDateChanged(@NonNull DatePicker view, int year, int month, int dayOfMonth) {
                if (year > ProjectUtil.getCurrentYear())
                    view.updateDate(ProjectUtil.getCurrentYear(), ProjectUtil.getCurrentMonth(), ProjectUtil.getCurrentDay());

                if (month > ProjectUtil.getCurrentMonth() && year == ProjectUtil.getCurrentYear())
                    view.updateDate(ProjectUtil.getCurrentYear(), ProjectUtil.getCurrentMonth(),  ProjectUtil.getCurrentDay());

                if (dayOfMonth > ProjectUtil.getCurrentDay() && year == ProjectUtil.getCurrentYear() && month == ProjectUtil.getCurrentMonth())
                    view.updateDate(ProjectUtil.getCurrentYear(), ProjectUtil.getCurrentMonth(),  ProjectUtil.getCurrentDay());
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
        }, ProjectUtil.getCurrentHour(), ProjectUtil.getCurrentMinute(),true);
        timePicker.show();
        //选择好以后将ordertime修改
        orderTime = ((orderMonth>0&&orderMonth<10)?("0"+orderMonth):orderMonth)+"月"+orderDay+"日"+" "+orderHour+ ":"+((orderMin>0&&orderMin<10)?("0"+orderMin):orderMin);
    }
}
