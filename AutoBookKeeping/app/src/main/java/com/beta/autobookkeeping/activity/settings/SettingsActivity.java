package com.beta.autobookkeeping.activity.settings;

import static Util.ImageUtil.base642bitmap;
import static Util.ProjectUtil.BLUE;
import static Util.ProjectUtil.getLocalOrderInfo;
import static Util.ProjectUtil.toastMsg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.orderItemSearch.OrderItemSearchActivity;
import com.beta.autobookkeeping.activity.presonalInfo.PersonlInfoActivity;
import com.beta.autobookkeeping.smsTools.SMSDataBase;
import com.beta.autobookkeeping.smsTools.SMSService;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Util.ProjectUtil;
import Util.SpUtils;

public class SettingsActivity extends AppCompatActivity {

    TextView tvUserPhoneNum;
    LinearLayout personalCenter;
    ImageView userPortrait;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_settings);
        super.onCreate(savedInstanceState);
        findViewByIdAndInit();
    }

    private void findViewByIdAndInit(){
        //个人中心
        tvUserPhoneNum = findViewById(R.id.tv_user_phone_num);
        tvUserPhoneNum.setText((String) SpUtils.get(this,"phoneNum",""));

        personalCenter = findViewById(R.id.ll_personal_center);
        personalCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this,PersonlInfoActivity.class);
                startActivity(intent);
            }
        });
        //获取头像
        userPortrait = findViewById(R.id.iv_portrait);
        if(SpUtils.get(this,"portrait","")==null||"".equals(SpUtils.get(this,"portrait",""))){
            userPortrait.setBackground(this.getDrawable(R.drawable.ic_portrait));
        }
        else{
            userPortrait.setBackground(new BitmapDrawable(base642bitmap((String) SpUtils.get(this,"portrait",""))));
        }
    }
}