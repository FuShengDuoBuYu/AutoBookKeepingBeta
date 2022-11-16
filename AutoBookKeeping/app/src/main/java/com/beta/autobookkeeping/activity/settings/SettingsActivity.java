package com.beta.autobookkeeping.activity.settings;

import static Util.ConstVariable.IP;
import static Util.ImageUtil.base642bitmap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.familyTodo.FamilyTodoActivity;
import com.beta.autobookkeeping.activity.orderItemSearch.OrderItemSearchActivity;
import com.beta.autobookkeeping.activity.orderMap.OrderMapActivity;
import com.beta.autobookkeeping.activity.presonalInfo.PersonlInfoActivity;
import com.beta.autobookkeeping.activity.settings.items.BankNumbers;
import com.gelitenight.waveview.library.WaveView;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Util.ProjectUtil;
import Util.SpUtils;
import Util.StringUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity {

    TextView tvUserPhoneNum;
    LinearLayout personalCenter,llSearchOrders,llOrderMap,llFamilyTodo;
    ImageView userPortrait;
    GridLayout glBankNum;
    BankNumbers bankNumbers = null;
    QMUIRoundButton btnAddBankNumber;
    Fragment fragmentTargetCostWater;

    private int mBorderColor = Color.parseColor("#44FFFFFF");

    private int mBorderWidth = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViewById();
        initViews();
        final WaveView waveView = (WaveView) findViewById(R.id.wave);
        waveView.setBorder(mBorderWidth, mBorderColor);
//        waveView.setBackgroundColor(Color.GREEN);
        waveView.setShapeType(WaveView.ShapeType.SQUARE);
//        ObjectAnimator waterLevelAnim = ObjectAnimator.ofFloat(
//                waveView, "waterLevelRatio", 0f, 0.5f);
//        waterLevelAnim.setDuration(10000);
//        waterLevelAnim.setInterpolator(new DecelerateInterpolator());
//        waterLevelAnim.start();
//        ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(
//                waveView, "waveShiftRatio", 0f, 1f);
////        waveShiftAnim.setRepeatCount(ValueAnimator.);
//        waveShiftAnim.setDuration(1000);
//        waveShiftAnim.setInterpolator(new LinearInterpolator());
//        waveShiftAnim.start();
//        ObjectAnimator amplitudeAnim = ObjectAnimator.ofFloat(
//                waveView, "amplitudeRatio", 0f, 0.05f);
////        amplitudeAnim.setRepeatCount(ValueAnimator.INFINITE);
//        amplitudeAnim.setRepeatMode(ValueAnimator.REVERSE);
//        amplitudeAnim.setDuration(5000);
//        amplitudeAnim.start();
//        amplitudeAnim.setInterpolator(new LinearInterpolator());
        waveView.setShowWave(true);
//        waveView.setWaterLevelRatio(60);
        List<Animator> animators = new ArrayList<>();
        ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(
                waveView, "waveShiftRatio", 0.0f, 1.0f);
        waveShiftAnim.setRepeatCount(10);
        waveShiftAnim.setDuration(250);
        waveShiftAnim.setInterpolator(new LinearInterpolator());

// vertical animation.
// water level increases from 0 to center of WaveView
        ObjectAnimator waterLevelAnim = ObjectAnimator.ofFloat(
                waveView, "waterLevelRatio", 0f, 0.5f);
        waterLevelAnim.setDuration(2500);
        waterLevelAnim.setInterpolator(new DecelerateInterpolator());
        animators.add(waterLevelAnim);

// amplitude animation.
// wave grows big then grows small, repeatedly
        ObjectAnimator amplitudeAnim = ObjectAnimator.ofFloat(
                waveView, "amplitudeRatio", 0f, 0.05f);
        amplitudeAnim.setRepeatCount(2);
//        amplitudeAnim.setRepeatMode(ValueAnimator.REVERSE);
        amplitudeAnim.setDuration(1250);
        amplitudeAnim.setInterpolator(new LinearInterpolator());
        animators.add(amplitudeAnim);
        waveView.setWaveColor(Color.RED, Color.BLUE);
//        AnimatorSet waveAnimation = new AnimatorSet();
//        waveAnimation.playTogether(animators);
//        waveAnimation.start();
        waveShiftAnim.start();
        waterLevelAnim.start();
        amplitudeAnim.start();
//        animators.add(waterLevelAnim);
    }

    private void findViewById(){
        tvUserPhoneNum = findViewById(R.id.tv_user_phone_num);
        personalCenter = findViewById(R.id.ll_personal_center);
        userPortrait = findViewById(R.id.iv_portrait);
        btnAddBankNumber = findViewById(R.id.btn_add_bank_number);
        glBankNum = findViewById(R.id.gl_bank_num);
        llSearchOrders = findViewById(R.id.ll_search_orders);
        llOrderMap = findViewById(R.id.ll_order_map);
        llFamilyTodo = findViewById(R.id.ll_family_todo);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTargetCostWater = fragmentManager.findFragmentById(R.id.fragment_target_cost_water);
    }

    private void initViews(){
        //个人中心
        tvUserPhoneNum.setText((String) SpUtils.get(this,"phoneNum",""));
        personalCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this,PersonlInfoActivity.class);
                startActivity(intent);
            }
        });
        //获取头像
        if(SpUtils.get(this,"portrait","")==null||"".equals(SpUtils.get(this,"portrait",""))){
            userPortrait.setBackground(this.getDrawable(R.drawable.ic_portrait));
        }
        else{
            userPortrait.setBackground(new BitmapDrawable(base642bitmap((String) SpUtils.get(this,"portrait",""))));
        }
        //添加银行号码
        btnAddBankNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StyledDialog.buildNormalInput("添加银行号码", "请输入要监听短信的银行号码", null, "确定", "取消", new MyDialogListener() {
                    @Override public void onFirst() {}  @Override public void onSecond() {}
                    @Override
                    public boolean onInputValid(CharSequence input1, CharSequence input2, EditText editText1, EditText editText2) {
                        if(input1.toString().matches("^[0-9]*$")&&!input1.toString().equals("")){
                            addBankNumber(input1.toString());
                        }
                        else{
                            ProjectUtil.toastMsg(SettingsActivity.this,"短信号格式不合法!");
                        }
                        return super.onInputValid(input1, input2, editText1, editText2);
                    }
                }).show();
            }
        });
        //银行短信
        bankNumbers = new BankNumbers(SpUtils.get(this,"bankNumbers","")==null?"": (String) SpUtils.get(this,"bankNumbers",""),SettingsActivity.this);
        for(int i = 0;i < bankNumbers.getBankNumbersViews().size();i++){
            glBankNum.addView(bankNumbers.getBankNumbersViews().get(i));
        }
        //跳转到订单查询
        llSearchOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, OrderItemSearchActivity.class);
                startActivity(intent);
            }
        });

        llOrderMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, OrderMapActivity.class);
                startActivity(intent);
            }
        });

        llFamilyTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, FamilyTodoActivity.class);
                startActivity(intent);
            }
        });
    }

    //添加一个银行号码
    private void addBankNumber(String newBankNumber){
        List<String> bankNumbers = StringUtil.string2List((String) SpUtils.get(SettingsActivity.this,"bankNumbers",""));
        bankNumbers.add(newBankNumber);
        //传递给后端
        StyledDialog.buildLoading().show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/user/modifyBankNumber";
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("phoneNum",SpUtils.get(SettingsActivity.this,"phoneNum",""));
                    jsonObject.put("bankNumbers",StringUtil.list2String(bankNumbers));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));
                Request request = new Request.Builder().url(url).put(body).build();
                try{
                    Response response = client.newCall(request).execute();
                    if(response.code()==200){
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        if(jsonResponse.getBoolean("success")){
                            afterModifyBankNumber(StringUtil.list2String(bankNumbers));
                        }
                        else{
                            Looper.prepare();
                            StyledDialog.dismissLoading(SettingsActivity.this);
                            ProjectUtil.toastMsg(SettingsActivity.this,jsonResponse.getString("message"));
                            Looper.loop();
                        }
                    }
                    else{
                        Looper.prepare();
                        StyledDialog.dismissLoading(SettingsActivity.this);
                        ProjectUtil.toastMsg(SettingsActivity.this,"服务器出错");
                        Looper.loop();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void afterModifyBankNumber(String newBankNumbers){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpUtils.put(SettingsActivity.this,"bankNumbers",newBankNumbers);
                bankNumbers.setBankNumbers(newBankNumbers);
                glBankNum.removeAllViews();
                for(int i = 0;i < bankNumbers.getBankNumbersViews().size();i++){
                    glBankNum.addView(bankNumbers.getBankNumbersViews().get(i));
                }
                StyledDialog.dismissLoading(SettingsActivity.this);
                ProjectUtil.toastMsg(SettingsActivity.this,"修改成功");
            }
        });
    }
}