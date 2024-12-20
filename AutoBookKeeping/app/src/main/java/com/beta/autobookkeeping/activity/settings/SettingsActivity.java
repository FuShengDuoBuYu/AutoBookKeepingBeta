package com.beta.autobookkeeping.activity.settings;

import static Util.ConstVariable.IP;
import static Util.ImageUtil.base642bitmap;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.activity.orderItemSearch.OrderItemSearchActivity;
import com.beta.autobookkeeping.activity.presonalInfo.PersonlInfoActivity;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Util.ProjectUtil;
import Util.SpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity {

    TextView tvUserPhoneNum;
    LinearLayout personalCenter,llSearchOrders,llDownloadOrders,llAlipayXiaohebao;
    ImageView userPortrait;
    Switch useXiaohebao;
    QMUIRoundButton btnAddBankNumber;
    Fragment fragmentTargetCostWater;
    SQLiteDatabase db;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViewById();
        db = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString() + "/orderInfo.db", null);
        initViews();
    }

    private void findViewById(){
        tvUserPhoneNum = findViewById(R.id.tv_user_phone_num);
        personalCenter = findViewById(R.id.ll_personal_center);
        userPortrait = findViewById(R.id.iv_portrait);
        llSearchOrders = findViewById(R.id.ll_search_orders);
        llDownloadOrders = findViewById(R.id.ll_download_orders);
        llAlipayXiaohebao = findViewById(R.id.ll_alipay_xiaohebao);
        useXiaohebao = findViewById(R.id.switch_use_xiaohebao);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTargetCostWater = fragmentManager.findFragmentById(R.id.fragment_target_cost_water);
    }

    private void initViews(){
        //个人中心
        tvUserPhoneNum.setText((String) SpUtils.get(this,"phoneNum",""));
        personalCenter.setOnClickListener(v-> startActivity(new Intent(this,PersonlInfoActivity.class)));
        //获取头像
        if(SpUtils.get(this,"portrait","")==null||"".equals(SpUtils.get(this,"portrait",""))){
            userPortrait.setBackground(this.getDrawable(R.drawable.ic_portrait));
        }
        else{
            userPortrait.setBackground(new BitmapDrawable(base642bitmap((String) SpUtils.get(this,"portrait",""))));
        }
        //跳转到订单查询
        llSearchOrders.setOnClickListener(v-> startActivity(new Intent(this, OrderItemSearchActivity.class)));
        //从云端拉取个人账单信息
        llDownloadOrders.setOnClickListener(v->{
            StyledDialog.buildIosAlert("下载个人账单", "将会清空本地存储账单后从云端覆盖", new MyDialogListener() {
                @Override
                public void onFirst() {
                    downloadOrders();
                }
                @Override
                public void onSecond() {}
            }).show();
        });
        useXiaohebao.setChecked(SpUtils.contains(this,"is_alipay_xiaohebao"));
        //支付宝小荷包
        useXiaohebao.setOnClickListener(v->{
            if(useXiaohebao.isChecked()){
                String input1;
                input1 = (String) SpUtils.get(this,"is_alipay_xiaohebao","");
                if(input1==null||"".equals(input1)){
                    StyledDialog.buildNormalInput("选择使用支付宝小荷包记账", "请输入小荷包昵称", null, "确定","取消",new MyDialogListener() {
                        @Override
                        public void onFirst() {}
                        @Override
                        public void onSecond() {}
                        @Override
                        public void onGetInput(CharSequence input1, CharSequence input2) {
                            SpUtils.put(SettingsActivity.this,"is_alipay_xiaohebao",input1);
                            ProjectUtil.toastMsg(SettingsActivity.this,"设置成功");
                        }
                    }).show();
                }
            }
            else{
                SpUtils.remove(this,"is_alipay_xiaohebao");
                ProjectUtil.toastMsg(this,"取消使用支付宝小荷包记账");
                System.out.println(SpUtils.getAll(SettingsActivity.this));
            }
        });
    }
    private void downloadOrders(){
        StyledDialog.buildLoading().show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //清空表中数据
                db.execSQL("delete from orderInfo");
                String url = IP+"/getOrderByUserId/"+SpUtils.get(SettingsActivity.this,"phoneNum","");
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).get().build();
                try{
                    Response response = client.newCall(request).execute();
                    if(response.code()==200){
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        if(jsonResponse.getBoolean("success")){
                            List<OrderInfo> orderInfos = new ArrayList<>();
                            for(int i=0;i<jsonResponse.getJSONArray("data").length();i++){
                                JSONObject orderInfo = jsonResponse.getJSONArray("data").getJSONObject(i);
                                orderInfos.add(new OrderInfo(orderInfo.getInt("id"),
                                        orderInfo.getInt("year"),
                                        orderInfo.getInt("month"),
                                        orderInfo.getInt("day"),
                                        orderInfo.getString("clock"),
                                        orderInfo.getDouble("money"),
                                        orderInfo.getString("bankName"),
                                        orderInfo.getString("orderRemark"),
                                        orderInfo.getString("costType"),
                                        orderInfo.getString("userId")));
                            }
                            //将数据存入数据库
                            for(int i=0;i<orderInfos.size();i++){
                                OrderInfo orderInfo = orderInfos.get(i);
                                db.execSQL("insert into orderInfo values(?,?,?,?,?,?,?,?,?,?)",
                                        new Object[]{orderInfo.getId(),orderInfo.getYear(),orderInfo.getMonth(),orderInfo.getDay(),orderInfo.getClock(),orderInfo.getMoney(),orderInfo.getBankName(),orderInfo.getOrderRemark(),orderInfo.getCostType(),(String) SpUtils.get(SettingsActivity.this,"phoneNum","")});
                            }
                            //提示
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    StyledDialog.dismissLoading(SettingsActivity.this);
                                    ProjectUtil.toastMsg(SettingsActivity.this,"下载成功");
                                }
                            });
                        }

                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}