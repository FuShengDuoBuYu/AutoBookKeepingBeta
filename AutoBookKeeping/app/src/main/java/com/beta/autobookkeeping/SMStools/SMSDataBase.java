package com.beta.autobookkeeping.SMStools;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SMSDataBase extends SQLiteOpenHelper {
    public SMSDataBase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建一个表
        String sql = "create table orderInfo(id integer primary key autoincrement,year int(4),month int(2),day int(2),clock varchar(20),money numeric(10,2),bankName varchar(255),orderRemark varchar(255))";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
