package Util;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.text.SimpleDateFormat;
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
}

