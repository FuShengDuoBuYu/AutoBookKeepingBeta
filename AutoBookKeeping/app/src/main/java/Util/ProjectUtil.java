package Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.Log;

import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.smsTools.*;

import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beta.autobookkeeping.R;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectUtil {
    public final static int[] colors = new int[]{
            Color.rgb(92, 172, 238),
            Color.rgb(112, 128, 144),
            Color.rgb(60, 179, 113),
            Color.rgb(123, 104, 238),
            Color.rgb(210, 105, 30),
            Color.rgb(218, 112, 214),
            Color.rgb(237, 189, 189),
            Color.rgb(172, 217, 243),
            Color.rgb(34,139,34),
            Color.rgb(95,158,160),
            Color.rgb(138,43,226),
            Color.rgb(233,150,122)};
    public final static int BLUE = Color.parseColor("#5091F3");
    public final static int Gray = Color.rgb(235, 235, 235);
    //匹配银行账单信息的正则表达式
    //获取【】之内的内容
    private final static String regExBank = "【(.*?)】";
//    private final static String regExBank = "[农业银行|建设银行|郑州银行|工商银行|招商银行|中国银行]";
    private final static String regExMoneyType = "[-|出|入|代|取]";
    private final static String regExMoney = "\\d*\\.\\d*";

    //弹出Toast的方法
    public static void toastMsg(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }

    //获取当前时间的方法
    public static String getCurrentTime() {
        SimpleDateFormat s_format = new SimpleDateFormat("MM月dd日 HH:mm");
        return s_format.format(new Date());
    }

    public static int getCurrentYear() {
        Calendar cal = Calendar.getInstance();

        return cal.get(Calendar.YEAR);
    }

    public static int getCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getCurrentDay() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DATE);
    }

    public static int getCurrentHour() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int getCurrentMinute() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MINUTE);
    }

    //获取字符串中的银行账单数据的方法
    public static String[] getBankOrderInfo(String bankOrder) {
        //将[]替换为【】
        bankOrder = bankOrder.replace("[", "【");
        bankOrder = bankOrder.replace("]", "】");
        String[] result = new String[3];
        //获取银行名称
        Matcher matchBank = Pattern.compile(regExBank).matcher(bankOrder);
        String bankName = getString(matchBank, false);
        result[0] = bankName.substring(1, bankName.length() - 1);
        Log.d("bank", result[0]);
        //对工商银行进行单独适配
        if ("工商银行".equals(result[0])) {
            result[1] = getICBCInfo(bankOrder)[0];
            result[2] = getICBCInfo(bankOrder)[1];
            return result;
        }
        //获取支付类型
        Matcher matchMoneyType = Pattern.compile(regExMoneyType).matcher(bankOrder);
        result[1] = getString(matchMoneyType, false);
        //获取收入或支出金额
        Matcher matchMoney = Pattern.compile(regExMoney).matcher(bankOrder);
        result[2] = getString(matchMoney, true);
        return result;
    }

    //获取账单单个数据的方法,第二个参数为是否匹配到第一个符合条件的就结束
    public static String getString(Matcher matcher, boolean stopAtFirstResult) {
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            sb.append(matcher.group());
            if (stopAtFirstResult && (!sb.toString().equals("")))
                break;

        }
        String result = sb.toString();
        if (result.equals("出") || result.equals("-")|| result.equals("取")) {
            return "支出";
        } else if (result.equals("入") || result.equals("代")) {
            return "收入";
        }
        return result;
    }

    //获取工商银行短信中的数据的方法
    public static String[] getICBCInfo(String msg) {
        String[] result = new String[2];
        //判断支出还是收入
        if (msg.contains("支出")) {
            result[0] = "支出";
        } else {
            result[0] = "收入";
        }
        //获取收支金额
        int startIndex, endIndex;
        startIndex = msg.indexOf(")");
        endIndex = msg.indexOf("元");
        result[1] = msg.substring(startIndex + 1, endIndex);
        return result;
    }

    //获取当日收支金额的方法
    public static double getTodayMoney(Context context) {
        double allTodayOrder = 0.0;
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            if (cursor.getInt(1) == ProjectUtil.getCurrentYear() && cursor.getInt(2) == ProjectUtil.getCurrentMonth() && cursor.getInt(3) == ProjectUtil.getCurrentDay()) {
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
            if (cursor.getInt(1) == ProjectUtil.getCurrentYear() && cursor.getInt(2) == ProjectUtil.getCurrentMonth()) {
                allMonthOrder += cursor.getDouble(5);
            }
        }
        cursor.close();
        return allMonthOrder;
    }

    //获取指定月份收支金额的方法
    public static double getMonthMoney(int year, int month, Context context) {
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
    public static double getDayMoney(int year, int month, int day, Context context) {
        double appointDayMoney = 0.0;
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            if (cursor.getInt(1) == year && cursor.getInt(2) == month && cursor.getInt(3) == day) {
                appointDayMoney += cursor.getDouble(5);
            }
        }
        cursor.close();
        return appointDayMoney;
    }

    //获取指定月份的所有账单信息
    public static ArrayList<OrderInfo> getMonthOrders(int year,int month,Context context){
        ArrayList<OrderInfo> orderInfos = new ArrayList<>();
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            //当是查询月的时候且是支出时
            if (cursor.getInt(1) == year && cursor.getInt(2) == month && cursor.getDouble(5) < 0) {
                orderInfos.add(new OrderInfo(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getDouble(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getString(9)
                ));
            }
        }
        cursor.close();
        return orderInfos;
    }

    //获取指定月份的支出类型和金额
    public static ArrayList<ArrayList> getCostTypeAndMoney(ArrayList<OrderInfo> monthOrders) {
        //包含金额和支付类型两个量的ArrayList
        ArrayList<ArrayList> result = new ArrayList<>();
        //支付类型
        ArrayList<String> costlabels = new ArrayList<>();
        //金额
        ArrayList<Float> costMoney = new ArrayList<Float>();
        result.add(costlabels);
        result.add(costMoney);

        for(int i = 0;i < monthOrders.size();i++){
            OrderInfo orderInfo = monthOrders.get(i);
            //支出
            if(orderInfo.getMoney()<0){
                //如果这个标签在labels中,就不插入labels而将数据合并
                if(costlabels.contains(orderInfo.getCostType())){
                    //当前这个标签的index
                    int index = costlabels.indexOf(orderInfo.getCostType());
                    //将这个数值加进去
                    costMoney.set(index,costMoney.get(index)+(float) orderInfo.getMoney());
                }
                //不存在的话就添加这个label,并进行加入数据
                else {
                    costlabels.add(orderInfo.getCostType());
                    costMoney.add((float) orderInfo.getMoney());
                }
            }
        }
        //将两个list按照金额大小排序,手动写一个冒泡排序
        for(int i = 0;i < costMoney.size();i++){
            for(int j = i+1;j < costMoney.size();j++){
                if(costMoney.get(i)<=costMoney.get(j)){
                    float tempMoney = costMoney.get(i);
                    costMoney.set(i,costMoney.get(j));
                    costMoney.set(j,tempMoney);
                    String tempLabel = costlabels.get(i);
                    costlabels.set(i,costlabels.get(j));
                    costlabels.set(j,tempLabel);
                }
            }
        }
        Collections.reverse(costlabels);
        Collections.reverse(costMoney);

//        //去掉小于2%项目的显示
//        for(int i = 0;i < costMoney.size();i++){
//            if((costMoney.get(i)/(float)getMonthCost(year,month,context))-0.02f<0){
//                costMoney.remove(i);
//                costlabels.remove(i);
//            }
//        }
        //将金额只保留两位小数
        for(int i = 0;i < costMoney.size();i++){
            BigDecimal b = new BigDecimal(costMoney.get(i));
            costMoney.set(i,b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue());
        }
        return result;
    }

    //获取查询月所有支出总和,返回值为负
    public static double getMonthCost(int year, int month, Context context) {
        double appointMonthCost = 0.0;
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            if (cursor.getInt(1) == year && cursor.getInt(2) == month && cursor.getDouble(5) < 0) {
                appointMonthCost += cursor.getDouble(5);
            }
        }
        cursor.close();
        return appointMonthCost;
    }

    //获取查询某个月某一项支出总和,返回值为负
    public static double getMonthSomeItemCost(int year, int month,String itemName, Context context) {
        double appointMonthSomeItemCost = 0.0;
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            Log.d("1",String.valueOf(month));
            if (cursor.getInt(1) == year && cursor.getInt(2) == month && cursor.getString(8).equals(itemName) ) {
                Log.d("2",String.valueOf(cursor.getDouble(5)));
                appointMonthSomeItemCost += cursor.getDouble(5);
            }
        }
        cursor.close();
        Log.d("tag",String.valueOf(appointMonthSomeItemCost));
        return appointMonthSomeItemCost;
    }

    //获取查询日所有支出总和,返回值为负
    public static double getDayCost(int year, int month, int day, Context context) {
        double appointDayCost = 0.0;
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            if (cursor.getInt(1) == year && cursor.getInt(2) == month && cursor.getInt(3) == day && cursor.getDouble(5) < 0) {
                appointDayCost += cursor.getDouble(5);
            }
        }
        cursor.close();
        return appointDayCost;
    }

    //获取某个月都有那几天有数据
    public static ArrayList<Integer> getHasOrderDays(int year,int month, Context context) {
        ArrayList<Integer> hasOrderDays = new ArrayList<>();
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        while (cursor.moveToNext()) {
            //先找到当月
            if (cursor.getInt(2) == month&&cursor.getInt(1)==year) {
                //如果没由记录这个日期,就记录进去
                if (!hasOrderDays.contains(cursor.getInt(3))) {
                    hasOrderDays.add(cursor.getInt(3));
                }
            }
        }
        cursor.close();
        //将日期倒置排序
        Collections.reverse(hasOrderDays);
        return hasOrderDays;
    }

    //获取某日是周几
    public static String getWeek(Date date) {
        String[] weeks = {"周四", "周五", "周六", "周日", "周一", "周二", "周三"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week_index < 0) {
            week_index = 0;
        }
        return weeks[week_index];
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getIconByCategory(String category, Context context){
        String categoryName = null;
        if(category.contains("-")){
            categoryName = category.substring(0,category.indexOf("-"));
        }
        else{
            categoryName = category;
        }
        switch (categoryName){
            case("收入"):
                return context.getDrawable(R.drawable.ic_part_time_job);
            case("消费"):
                return context.getDrawable(R.drawable.ic_normal);
            case("饮食"):
                return context.getDrawable(R.drawable.ic_food);
            case("交通"):
                return context.getDrawable(R.drawable.ic_traffic);
            case("体育"):
                return context.getDrawable(R.drawable.ic_sport);
            case("聚会"):
                return context.getDrawable(R.drawable.ic_party);
            case("娱乐"):
                return context.getDrawable(R.drawable.ic_entertain);
            case("购物"):
                return context.getDrawable(R.drawable.ic_shopping);
            case("通讯"):
                return context.getDrawable(R.drawable.ic_communication);
            case("红包"):
                return context.getDrawable(R.drawable.ic_red_money);
            case("医疗"):
                return context.getDrawable(R.drawable.ic_hospital);
            case("一卡通"):
                return context.getDrawable(R.drawable.ic_ecard);
            case("学习"):
                return context.getDrawable(R.drawable.ic_study);
            case("其他"):
                return context.getDrawable(R.drawable.ic_others);
        }
        return null;
    }
    //添加一个数据账单项
    @SuppressLint("UseCompatLoadingForDrawables")
    public static LinearLayout setDayOrderItem(String category, String payWay, String money, String time, Context context) {
        //最外层的总LinearLayout
        LinearLayout linearLayoutItem = new LinearLayout(context);
        linearLayoutItem.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutItem.setPadding(0, 20, 0, 20);
        //再加三个子layout
        LinearLayout linearLayoutLeftPart = new LinearLayout(context);
        LinearLayout linearLayoutImagePart = new LinearLayout(context);
        LinearLayout linearLayoutRightPart = new LinearLayout(context);
        linearLayoutLeftPart.setOrientation(LinearLayout.VERTICAL);
        linearLayoutImagePart.setOrientation(LinearLayout.VERTICAL);
        linearLayoutRightPart.setOrientation(LinearLayout.VERTICAL);
        //再设置一个图片
        ImageView categoryImage = new ImageView(context);
        Drawable image = getIconByCategory(category, context);
        categoryImage.setImageDrawable(image);
        //设置子布局格式
        linearLayoutLeftPart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        linearLayoutRightPart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        linearLayoutImagePart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayoutLeftPart.setPadding(60, 7, 0, 7);
        linearLayoutRightPart.setPadding(60, 7, 0, 7);
        linearLayoutImagePart.setPadding(60, 16, 0, 7);
        linearLayoutLeftPart.setGravity(Gravity.START);
        linearLayoutImagePart.setHorizontalGravity(1);
        linearLayoutImagePart.setVerticalGravity(16);
        linearLayoutRightPart.setGravity(Gravity.END);
        //每个字layout里加两个textview
        TextView tvCategory = new TextView(context);
        tvCategory.setMaxEms(10);
        TextView tvPayWay = new TextView(context);
        TextView tvMoney = new TextView(context);
        TextView tvTime = new TextView(context);
        //设置每个textview
        tvCategory.setText(category);
        tvPayWay.setText(payWay);
        tvMoney.setText(money);
        tvTime.setText(time);
        //设置textView格式
        tvCategory.setTextColor(context.getResources().getColor(R.color.primary_font));
        tvCategory.setTextSize(18);
        tvMoney.setGravity(Gravity.END);
        tvTime.setGravity(Gravity.END);
        tvMoney.setPadding(0, 0, 60, 0);
        tvTime.setPadding(0, 0, 60, 0);
        //将textView加入子布局
        linearLayoutLeftPart.addView(tvCategory);
        linearLayoutLeftPart.addView(tvPayWay);
        linearLayoutRightPart.addView(tvMoney);
        linearLayoutRightPart.addView(tvTime);
        linearLayoutImagePart.addView(categoryImage);
        //将子布局加到总布局里
        linearLayoutItem.addView(linearLayoutImagePart);
        linearLayoutItem.addView(linearLayoutLeftPart);
        linearLayoutItem.addView(linearLayoutRightPart);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true);
        linearLayoutItem.setForeground(context.getDrawable(outValue.resourceId));
        return linearLayoutItem;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static LinearLayout setDayOrderItem(String category, String payWay, String money, String time, Context context, ImageView imageView) {
        //最外层的总LinearLayout
        LinearLayout linearLayoutItem = new LinearLayout(context);
        linearLayoutItem.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutItem.setPadding(0, 20, 0, 20);
        //再加四个子layout
        LinearLayout linearLayoutLeftPart = new LinearLayout(context);
        LinearLayout linearLayoutLeftImagePart = new LinearLayout(context);
        LinearLayout linearLayoutRightPart = new LinearLayout(context);
        LinearLayout linearLayoutRightImagePart = new LinearLayout(context);
        linearLayoutLeftPart.setOrientation(LinearLayout.VERTICAL);
        linearLayoutLeftImagePart.setOrientation(LinearLayout.VERTICAL);
        linearLayoutRightPart.setOrientation(LinearLayout.VERTICAL);
        linearLayoutRightImagePart.setOrientation(LinearLayout.VERTICAL);
        //再设置图片
        ImageView categoryImage = new ImageView(context);
        categoryImage.setId(R.id.order_item_category_image_id);
        categoryImage.setTransitionName("categoryImage");
        Drawable image = getIconByCategory(category, context);
        categoryImage.setImageDrawable(image);
        //头像
        imageView.setTransitionName("portrait");
        imageView.setId(R.id.order_item_portrait_id);
        linearLayoutRightImagePart.addView(imageView);
        //设置子布局格式
        linearLayoutLeftPart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        linearLayoutRightPart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        linearLayoutLeftImagePart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayoutLeftPart.setPadding(60, 7, 0, 7);
        linearLayoutRightPart.setPadding(60, 7, 0, 7);
        linearLayoutLeftImagePart.setPadding(60, 16, 0, 7);
        linearLayoutLeftPart.setGravity(Gravity.START);
        linearLayoutLeftImagePart.setHorizontalGravity(1);
        linearLayoutLeftImagePart.setVerticalGravity(16);
        linearLayoutRightPart.setGravity(Gravity.END);
        //每个字layout里加两个textview
        TextView tvCategory = new TextView(context);
        tvCategory.setMaxEms(10);
        TextView tvPayWay = new TextView(context);
        TextView tvMoney = new TextView(context);
        TextView tvTime = new TextView(context);
        //设置每个textview
        tvCategory.setText(category);
        tvCategory.setId(R.id.order_item_category_id);
        tvCategory.setTransitionName("category");
        tvPayWay.setText(payWay);
        tvPayWay.setId(R.id.order_item_payway_id);
        tvPayWay.setTransitionName("payway");
        tvMoney.setText(money);
        tvMoney.setId(R.id.order_item_price_id);
        tvMoney.setTransitionName("money");
        tvTime.setText(time);
        tvTime.setId(R.id.order_item_time_id);
        tvTime.setTransitionName("time");
        //设置textView格式
        tvCategory.setTextColor(context.getResources().getColor(R.color.primary_font));
        tvCategory.setTextSize(18);
        tvMoney.setGravity(Gravity.END);
        tvTime.setGravity(Gravity.END);
        tvMoney.setPadding(0, 0, 10, 0);
        tvTime.setPadding(0, 0, 10, 0);
        //将textView加入子布局
        linearLayoutLeftPart.addView(tvCategory);
        linearLayoutLeftPart.addView(tvPayWay);
        linearLayoutRightPart.addView(tvMoney);
        linearLayoutRightPart.addView(tvTime);
        linearLayoutLeftImagePart.addView(categoryImage);
        //将子布局加到总布局里
        linearLayoutItem.addView(linearLayoutLeftImagePart);
        linearLayoutItem.addView(linearLayoutLeftPart);
        linearLayoutItem.addView(linearLayoutRightPart);
        linearLayoutItem.addView(linearLayoutRightImagePart);
        //设置水滴按压效果
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true);
        linearLayoutItem.setForeground(context.getDrawable(outValue.resourceId));
        linearLayoutItem.setOnClickListener(null);
        return linearLayoutItem;
    }




    //动态设置一个xmlTitle
    public static LinearLayout setDayOrderTitle(String date, String money, Context context) {
        LinearLayout linearLayoutTitle = new LinearLayout(context);
        linearLayoutTitle.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutTitle.setBackgroundColor(context.getResources().getColor(R.color.item_background));
        //创建两个textview并赋值
        TextView tvDate, tvMoney;
        tvDate = new TextView(context);
        tvMoney = new TextView(context);
        tvDate.setText(date);
        tvMoney.setText(money);
        //设置两个textView的格式
        tvDate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        tvDate.setGravity(Gravity.LEFT);
        tvDate.setPadding(40, 0, 40, 0);
        tvMoney.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        tvMoney.setGravity(Gravity.RIGHT);
        tvMoney.setPadding(40, 0, 40, 0);
        //将两个textview放进去
        linearLayoutTitle.addView(tvDate);
        linearLayoutTitle.addView(tvMoney);
        return linearLayoutTitle;
    }

    //获取某个日期是今天还是昨天,否则返回该日
    public static String getDayRelation(int targetDay) {
        if (targetDay == getCurrentDay())
            return "今日";
        else if (targetDay + 1 == getCurrentDay())
            return "昨日";
        else
            return "本日";
    }


    //获取当前sqlite中的所有数据
    public static Cursor getLocalOrderInfo(Context context) {
        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
        SQLiteDatabase db = smsDb.getWritableDatabase();
        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        return cursor;
    }
}

