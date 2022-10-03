package com.beta.autobookkeeping.smsTools;

import static Util.ProjectUtil.getBankOrderInfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.beta.autobookkeeping.BaseApplication;
import com.beta.autobookkeeping.activity.orderDetail.OrderDetailActivity;

import java.util.List;

import Util.ProjectUtil;
import Util.SpUtils;
import Util.StringUtil;

public class SMSReader extends AppCompatActivity {
    BaseApplication baseApplication;
    private Context context;
    //短信内容
    private String body,sender;
    //短信观察者
    private SMSContent smsObsever;
    private Handler handler =new Handler(){
        public void handleMessage(android.os.Message msg) {

            Bundle msgBundle=msg.getData();
            body=msgBundle.getString("body");
            sender=msgBundle.getString("sender");
            //如果短信是银行账单短信且和本地新信息不一致,就调开app并进行下一步操作
            if(isBankOrderMsg(body)&&isSavedMsg(body)&&isUserInputBankNumber(sender)){
                ProjectUtil.toastMsg(context,"读取到银行账单!");
                Intent orderDetail = new Intent();
                orderDetail.setClassName(context, OrderDetailActivity.class.getName());
                orderDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                orderDetail.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String[] orderDetailMsg = getBankOrderInfo(body);
                Bundle bundle = new Bundle();
                bundle.putString("payWay",orderDetailMsg[0]);
                bundle.putString("orderType",orderDetailMsg[1]);
                bundle.putDouble("money",Double.parseDouble(orderDetailMsg[2]));
                orderDetail.putExtras(bundle);
            }
        };
    };


    public SMSReader(Context c){
        context = c;
    }

    public void readSMS(){
        smsObsever = new SMSContent(handler, context);//实例化短信观察者
        //注册短信观察者
        context.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsObsever);
    }

    //判断短信是否是银行发的账单短信
    public boolean isBankOrderMsg(String msg){
        String[] msgInfo = getBankOrderInfo(msg);
        //如果三个银行信息中有一个为空,就说明不是银行信息
        if(msgInfo[0].equals("")||msgInfo[1].equals("")||msgInfo[2].equals("")){
            return false;
        }else{
            return true;
        }
    }
    //判断是否是已经存储过的短信格式
    public boolean isSavedMsg(String msg){
        SharedPreferences sharedPreferences = context.getSharedPreferences("msgTempSave", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //如果和本地存储的一样
        if(msg.equals(sharedPreferences.getString("msgSave",""))){
            return false;
        }
        //如果和本地存储的不一样,则更新本地最新的
        else{
            editor.putString("msgSave",msg);
            editor.apply();
            return true;
        }
    }
    //判断是否是精确识别号码的账单
    private boolean isUserInputBankNumber(String sender){
        //用户未设置
        if(SpUtils.get(context,"bankNumbers","")==null||"".equals(SpUtils.get(context,"bankNumbers",""))){
            return true;
        }
        //用户已设置
        else{
            List<String> bankNumbers = StringUtil.string2List((String) SpUtils.get(context,"bankNumbers",""));
            return bankNumbers.contains(sender) || bankNumbers.contains(sender.substring(3));
        }
    }
}
