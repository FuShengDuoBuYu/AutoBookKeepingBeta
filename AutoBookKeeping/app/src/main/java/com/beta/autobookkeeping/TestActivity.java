package com.beta.autobookkeeping;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.beta.autobookkeeping.SMStools.SMSDataBase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Button btn = findViewById(R.id.start);
        SMSDataBase smsDb = new SMSDataBase(TestActivity.this,"orderInfo",null,1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", new String[]{"year"}, null, null, null, null, null);
        //利用游标遍历所有数据对象
        //为了显示全部，把所有对象连接起来，放到TextView中
        String textview_data = "";
        while(cursor.moveToNext()){
            String number = cursor.getString(cursor.getColumnIndex("year"));
            textview_data = textview_data + "\n" + number;
        }
        while(cursor.moveToNext()){
            String number = cursor.getString(cursor.getColumnIndex("day"));
            textview_data = textview_data + "\n" + number;
        }
        while(cursor.moveToNext()){
            String number = cursor.getString(cursor.getColumnIndex("clock"));
            textview_data = textview_data + "\n" + number;
        }
        while(cursor.moveToNext()){
            String number = cursor.getString(cursor.getColumnIndex("money"));
            textview_data = textview_data + "\n" + number;
        }
        while(cursor.moveToNext()){
            String number = cursor.getString(cursor.getColumnIndex("bankName"));
            textview_data = textview_data + "\n" + number;
        }
        btn.setText(textview_data);
    }
}

