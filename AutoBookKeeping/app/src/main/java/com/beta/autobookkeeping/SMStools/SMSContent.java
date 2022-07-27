package com.beta.autobookkeeping.SMStools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SMSContent extends ContentObserver {
    private Handler mHandler;
    private Context context;
    public SMSContent(Handler handler, Context context) {
        super(handler);
        mHandler=handler;
        this.context = context;
    }

    @SuppressLint("Range")
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Cursor cursor = null;
        String body=null;
        try {
            cursor = context.getContentResolver().query(
                    Uri.parse("content://sms/inbox"), null, null, null,
                    "date desc");
            if(cursor!=null){
                if(cursor.moveToNext()){//不遍历只拿当前最新的一条短信
                    //获取当前的短信内容
                    body=cursor.getString(cursor.getColumnIndex("body"));
                    Message msg=Message.obtain();
                    Bundle bundle=new Bundle();
                    bundle.putString("body", body);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(cursor!=null){
                cursor.close();
            }
        }
    }
}
