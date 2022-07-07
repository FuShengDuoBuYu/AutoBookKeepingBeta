package com.rainlife;

import Util.Util;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.qmuiteam.qmui.alpha.QMUIAlphaTextView;
import com.rainlife.autobookkeeping.R;
import com.rainlife.autobookkeeping.R.id;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.Nullable;

public final class MainActivity extends AppCompatActivity {
    TextView date,time_and_week;
    FrameLayout autoBookKeepingPanel;
    Button Test;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        findViews();
    }

    private void findViews(){
        date = findViewById(id.date);
        time_and_week = findViewById(id.time_and_week);
        date.setText(Util.getCurrentMonth()+"/"+Util.getCurrentDay());
        time_and_week.setText(Util.getCurrentHour()+":"+Util.getCurrentMinute()+" / "+Util.getWeek(new Date(Util.getCurrentYear(),Util.getCurrentMonth(),Util.getCurrentDay())));
        setScorller();
    }

    private void setScorller(){
        autoBookKeepingPanel = findViewById(R.id.auto_book_keeping_panel);
        Test = findViewById(R.id.test);
        Test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}

