package com.beta.autobookkeeping.activity.settings;

import static Util.ConstVariable.IP;
import static Util.ImageUtil.base642bitmap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.presonalInfo.PersonlInfoActivity;
import com.beta.autobookkeeping.activity.settings.items.BankNumbers;
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
    LinearLayout personalCenter;
    ImageView userPortrait;
    GridLayout glBankNum;
    BankNumbers bankNumbers = null;
    QMUIRoundButton btnAddBankNumber;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_settings);
        super.onCreate(savedInstanceState);
        findViewById();
        initViews();
    }

    private void findViewById(){
        tvUserPhoneNum = findViewById(R.id.tv_user_phone_num);
        personalCenter = findViewById(R.id.ll_personal_center);
        userPortrait = findViewById(R.id.iv_portrait);
        btnAddBankNumber = findViewById(R.id.btn_add_bank_number);
        glBankNum = findViewById(R.id.gl_bank_num);
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