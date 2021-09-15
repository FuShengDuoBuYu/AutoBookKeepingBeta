package com.beta.autobookkeeping;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import Util.Util;

public class MonthReportActivity extends AppCompatActivity {
    Button btn_right_choose,btn_left_choose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_report);
        //获取各个组件
        getViews();
    }

    public void getViews(){
        btn_left_choose = findViewById(R.id.btn_left_choose);
        btn_right_choose = findViewById(R.id.btn_right_choose);
        btn_left_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.toastMsg(MonthReportActivity.this,"左点击成功");
            }
        });
        btn_right_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.toastMsg(MonthReportActivity.this,"右点击成功");
            }
        });
    }
}