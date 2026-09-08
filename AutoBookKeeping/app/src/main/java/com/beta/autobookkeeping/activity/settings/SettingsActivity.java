package com.beta.autobookkeeping.activity.settings;

import static Util.ConstVariable.IP;
import static Util.ImageUtil.base642bitmap;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
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
import com.beta.autobookkeeping.activity.main.MainActivity;
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

    TextView tvUserPhoneNum, tvXiaohebaoSummary;
    LinearLayout personalCenter,llSearchOrders,llDownloadOrders,llAlipayXiaohebao,llSwitchAccount;
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
        llSwitchAccount = findViewById(R.id.ll_switch_account);
        useXiaohebao = findViewById(R.id.switch_use_xiaohebao);
        tvXiaohebaoSummary = findViewById(R.id.tv_xiaohebao_summary);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTargetCostWater = fragmentManager.findFragmentById(R.id.fragment_target_cost_water);
    }

    private void initViews(){
        //个人中心
        refreshProfileSummary();
        personalCenter.setOnClickListener(v-> startActivity(new Intent(this,PersonlInfoActivity.class)));
        //跳转到订单查询
        llSearchOrders.setOnClickListener(v-> startActivity(new Intent(this, OrderItemSearchActivity.class)));
        //从云端拉取个人账单信息
        llDownloadOrders.setOnClickListener(v->{
            StyledDialog.buildIosAlert("下载个人账单", "将以云端数据替换本机账单；下载或解析失败时会保留当前本机数据。", new MyDialogListener() {
                @Override
                public void onFirst() {
                    downloadOrders();
                }
                @Override
                public void onSecond() {}
            }).show();
        });
        refreshXiaohebaoSummary();
        //支付宝小荷包
        findViewById(R.id.ll_xiaohebao_text).setOnClickListener(view -> {
            if (!useXiaohebao.isEnabled()) return;
            final boolean enabled = useXiaohebao.isChecked();
            StyledDialog.buildNormalInput("修改小荷包昵称", "请输入小荷包中的成员昵称", null,
                    "确定", "取消", new MyDialogListener() {
                        @Override public void onFirst() { }
                        @Override public void onSecond() { }
                        @Override public void onGetInput(CharSequence input1, CharSequence input2) {
                            String nickname = input1 == null ? "" : input1.toString().trim();
                            if (nickname.isEmpty() || nickname.length() > 30) {
                                ProjectUtil.toastMsg(SettingsActivity.this, "昵称需为1至30个字符");
                                return;
                            }
                            saveXiaohebaoSettings(enabled, nickname);
                        }
                    }).show();
        });
        useXiaohebao.setOnClickListener(v->{
            if(useXiaohebao.isChecked()){
                String input1;
                input1 = (String) SpUtils.get(this,"xiaohebao_nickname",
                        SpUtils.get(this,"is_alipay_xiaohebao",""));
                if(input1==null||"".equals(input1)){
                    StyledDialog.buildNormalInput("选择使用支付宝小荷包记账", "请输入小荷包昵称", null, "确定","取消",new MyDialogListener() {
                        @Override
                        public void onFirst() {}
                        @Override
                        public void onSecond() {
                            useXiaohebao.setChecked(false);
                        }
                        @Override
                        public void onGetInput(CharSequence input1, CharSequence input2) {
                            String nickname = input1 == null ? "" : input1.toString().trim();
                            if(nickname.isEmpty()){
                                useXiaohebao.setChecked(false);
                                ProjectUtil.toastMsg(SettingsActivity.this,"请输入小荷包中的成员昵称");
                                return;
                            }
                            saveXiaohebaoSettings(true, nickname);
                        }
                    }).show();
                } else {
                    saveXiaohebaoSettings(true, input1);
                }
            }
            else{
                saveXiaohebaoSettings(false, (String) SpUtils.get(this,"xiaohebao_nickname",
                        SpUtils.get(this,"is_alipay_xiaohebao","")));
            }
        });
        llSwitchAccount.setOnClickListener(view -> StyledDialog.buildIosAlert(
                "切换账号",
                "切换后需要重新登录并从云端同步账单。原账号的云端账单不会被删除。",
                new MyDialogListener() {
                    @Override
                    public void onFirst() {
                        SpUtils.remove(SettingsActivity.this,"phoneNum");
                        SpUtils.remove(SettingsActivity.this,"nickName");
                        SpUtils.remove(SettingsActivity.this,"familyId");
                        SpUtils.remove(SettingsActivity.this,"familyIdentity");
                        SpUtils.remove(SettingsActivity.this,"portrait");
                        SpUtils.remove(SettingsActivity.this,"is_alipay_xiaohebao");
                        SpUtils.remove(SettingsActivity.this,"xiaohebao_nickname");
                        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onSecond() {
                    }
                }).setBtnText("重新登录", "取消").show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(tvUserPhoneNum != null && userPortrait != null){
            refreshProfileSummary();
            refreshXiaohebaoSummary();
        }
    }

    private void refreshXiaohebaoSummary() {
        String active = (String) SpUtils.get(this, "is_alipay_xiaohebao", "");
        String nickname = (String) SpUtils.get(this, "xiaohebao_nickname", active);
        boolean enabled = active != null && !active.trim().isEmpty();
        useXiaohebao.setChecked(enabled);
        tvXiaohebaoSummary.setText(nickname != null && !nickname.trim().isEmpty()
                ? "昵称：" + nickname.trim()
                : "未设置昵称 · 点击设置");
    }

    private void saveXiaohebaoSettings(boolean enabled, String nickname) {
        String phone = (String) SpUtils.get(this, "phoneNum", "");
        refreshXiaohebaoSummary();
        useXiaohebao.setEnabled(false);
        new Thread(() -> {
            boolean success = false;
            try {
                JSONObject payload = new JSONObject();
                payload.put("phoneNum", phone);
                payload.put("enabled", enabled);
                payload.put("nickname", nickname);
                Request request = new Request.Builder().url(IP + "/user/settings/xiaohebao")
                        .put(okhttp3.RequestBody.create(payload.toString(),
                                okhttp3.MediaType.parse("application/json;charset=utf-8"))).build();
                OkHttpClient client = new OkHttpClient.Builder()
                        .callTimeout(30, java.util.concurrent.TimeUnit.SECONDS).build();
                try (Response response = client.newCall(request).execute()) {
                    success = response.isSuccessful() && response.body() != null
                            && new JSONObject(response.body().string()).optBoolean("success", false);
                }
            } catch (Exception exception) {
                // Keep the last confirmed local settings on failure.
            }
            final boolean saved = success;
            runOnUiThread(() -> {
                if (!phone.equals(SpUtils.get(this, "phoneNum", ""))) return;
                if (saved) {
                    SpUtils.put(this, "xiaohebao_nickname", nickname);
                    SpUtils.put(this, "is_alipay_xiaohebao", enabled ? nickname : "");
                }
                useXiaohebao.setEnabled(true);
                refreshXiaohebaoSummary();
                ProjectUtil.toastMsg(this, saved ? "小荷包设置已保存到云端"
                        : "云端保存失败，已保留原设置，请检查网络或后端版本后重试");
            });
        }, "xiaohebao-settings-save").start();
    }

    private void refreshProfileSummary(){
        tvUserPhoneNum.setText((String) SpUtils.get(this,"phoneNum",""));
        String portrait = (String) SpUtils.get(this,"portrait","");
        Bitmap portraitBitmap = portrait == null || portrait.isEmpty() ? null : base642bitmap(portrait);
        userPortrait.setBackground(null);
        userPortrait.setImageDrawable(portraitBitmap == null
                ? getDrawable(R.drawable.ic_portrait)
                : new BitmapDrawable(getResources(), portraitBitmap));
    }
    private void downloadOrders(){
        StyledDialog.buildLoading().show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/getOrderByPhoneNum/"+SpUtils.get(SettingsActivity.this,"phoneNum","");
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).get().build();
                try(Response response = client.newCall(request).execute()){
                    if(response.code()==200 && response.body() != null){
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
                                        orderInfo.optString("userId", orderInfo.optString("phoneNum", ""))));
                            }
                            db.beginTransaction();
                            try {
                                // Only replace local data after successful remote fetch and parse.
                                db.execSQL("delete from orderInfo");
                                for(int i=0;i<orderInfos.size();i++){
                                    OrderInfo orderInfo = orderInfos.get(i);
                                    db.execSQL("insert into orderInfo values(?,?,?,?,?,?,?,?,?,?)",
                                            new Object[]{orderInfo.getId(),orderInfo.getYear(),orderInfo.getMonth(),orderInfo.getDay(),orderInfo.getClock(),orderInfo.getMoney(),orderInfo.getBankName(),orderInfo.getOrderRemark(),orderInfo.getCostType(),orderInfo.getUser()});
                                }
                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    StyledDialog.dismissLoading(SettingsActivity.this);
                                    ProjectUtil.toastMsg(SettingsActivity.this,"下载成功");
                                    com.beta.autobookkeeping.widget.OrderWidget.refreshAll(getApplicationContext());
                                }
                            });
                            return;
                        }

                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StyledDialog.dismissLoading(SettingsActivity.this);
                        ProjectUtil.toastMsg(SettingsActivity.this,"下载失败,请检查网络后重试");
                    }
                });
            }
        }).start();
    }
}
