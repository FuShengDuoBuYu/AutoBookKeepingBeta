package Util;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.beta.autobookkeeping.SMStools.SMSDataBase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    //匹配银行账单信息的正则表达式
    private final static String regExBank = "[农业银行|建设银行|郑州银行|工商银行|招商银行]";
    private final static String regExMoneyType = "[-|出|入|代]";
    private final static String regExMoney = "\\d*\\.\\d*";
    //弹出Toast的方法
    public static void toastMsg(Context context,String s){
        Toast.makeText(context,s,Toast.LENGTH_LONG).show();
    }
    //获取当前时间的方法
    public static String getCurrentTime(){
        SimpleDateFormat s_format = new SimpleDateFormat("MM月dd日 HH:mm");
        return s_format.format(new Date());
    }
    public static int getCurrentYear(){
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR);
    }
    public static int getCurrentMonth(){
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH) + 1;
    }
    public static int getCurrentDay(){
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DATE);
    }
    //获取字符串中的银行账单数据的方法
    public static String[] getBankOrderInfo(String bankOrder){
        String[] result = new String[3];
        //获取银行名称
        Matcher matchBank = Pattern.compile(regExBank).matcher(bankOrder);
        result[0] = getString(matchBank,false);
        //获取支付类型
        Matcher matchMoneyType = Pattern.compile(regExMoneyType).matcher(bankOrder);
        result[1] = getString(matchMoneyType,false);
        //获取收入或支出金额
        Matcher matchMoney = Pattern.compile(regExMoney).matcher(bankOrder);
        result[2] = getString(matchMoney,true);
        return result;
    }
    //获取账单单个数据的方法,第二个参数为是否匹配到第一个符合条件的就结束
    public static String getString(Matcher matcher,boolean stopAtFirstResult){
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            sb.append(matcher.group());
            if(stopAtFirstResult && (!sb.toString().equals("")))
                break;

        }
        String result = sb.toString();
        if(result.equals("出")||result.equals("-")||result.equals("代")){
            return "支出";
        }else if(result.equals("入")){
            return "收入";
        }
        return result;
    }
    //获取当日收支金额的方法
    public static double getTodayMoney(Context context) {
        double allTodayOrder = 0.0;
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            if (cursor.getInt(1) == Util.getCurrentYear() && cursor.getInt(2) == Util.getCurrentMonth() && cursor.getInt(3) == Util.getCurrentDay()) {
                allTodayOrder += cursor.getDouble(5);
            }
        }
        cursor.close();
        return allTodayOrder;
    }

    //获得本月收支金额的方法
    //获取当日收支金额的方法
    public static double getMonthMoney(Context context) {
        double allMonthOrder = 0.0;
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            if (cursor.getInt(1) == Util.getCurrentYear() && cursor.getInt(2) == Util.getCurrentMonth()) {
                allMonthOrder += cursor.getDouble(5);
            }
        }
        cursor.close();
        return allMonthOrder;
    }

    //获取指定月份收支金额的方法
    public static double getMonthMoney(int year,int month,Context context){
        double appointMonthMoney = 0.0;
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            if (cursor.getInt(1) == year && cursor.getInt(2) == month) {
                appointMonthMoney += cursor.getDouble(5);
            }
        }
        cursor.close();
        return appointMonthMoney;
    }

    //获取指定日收支金额的方法
    public static double getMonthMoney(int year,int month,int day,Context context){
        double appointDayMoney = 0.0;
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            if (cursor.getInt(1) == year && cursor.getInt(2) == month && cursor.getInt(3) == month){
                appointDayMoney += cursor.getDouble(5);
            }
        }
        cursor.close();
        return appointDayMoney;
    }

    //获取指定月份的支出类型和金额
    public static ArrayList<ArrayList> getCostTypeAndMoney(int month,int year,Context context){
        //包含金额和支付类型两个量的ArrayList
        ArrayList<ArrayList> result = new ArrayList<>();
        //支付类型
        ArrayList<String> costlabels = new ArrayList<>();
        //金额
        ArrayList<Float> costMoney = new ArrayList<Float>();
        result.add(costlabels);
        result.add(costMoney);

        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            //当是查询月的时候且是支出时
            if (cursor.getInt(1) == year && cursor.getInt(2) == month && cursor.getDouble(5)<0) {
                //如果这个标签在labels中,就不插入labels而将数据合并
                if(costlabels.contains(cursor.getString(8))){
                    //当前这个标签的index
                    int index = costlabels.indexOf(cursor.getString(8));
                    //将这个数值加进去
                    costMoney.set(index,costMoney.get(index)+(float)cursor.getDouble(5));
                }
                //不存在的话就添加这个label,并进行加入数据
                else{
                    costlabels.add(cursor.getString(8));
                    costMoney.add((float)cursor.getDouble(5));
                }
            }
        }
        cursor.close();
        return result;
    }

    //获取查询月所有支出总和,返回值为负
    public static double getMonthCost(int year,int month,Context context){
        double appointMonthCost = 0.0;
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            if (cursor.getInt(1) == year && cursor.getInt(2) == month && cursor.getDouble(5)<0) {
                appointMonthCost += cursor.getDouble(5);
            }
        }
        cursor.close();
        return appointMonthCost;
    }
}


