package Util;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.beta.autobookkeeping.BuildConfig;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.activity.orderDetail.OrderDetailActivity;


import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beta.autobookkeeping.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
            Color.rgb(233,150,122),
            Color.rgb(255, 99, 71),    // Tomato
            Color.rgb(64, 224, 208),   // Turquoise
            Color.rgb(255, 215, 0),    // Gold
            Color.rgb(199, 21, 133),   // MediumVioletRed
            Color.rgb(46, 139, 87),    // SeaGreen
            Color.rgb(255, 105, 180),  // HotPink
            Color.rgb(255, 69, 0),     // OrangeRed
            Color.rgb(255, 182, 193),  // LightPink
            Color.rgb(70, 130, 180),   // SteelBlue
            Color.rgb(72, 61, 139),    // DarkSlateBlue
            Color.rgb(128, 0, 0),      // Maroon
            Color.rgb(135, 206, 250),  // LightSkyBlue
            Color.rgb(0, 255, 127),    // SpringGreen
            Color.rgb(165, 42, 42),    // Brown
            Color.rgb(255, 140, 0),    // DarkOrange
            Color.rgb(75, 0, 130),     // Indigo
            Color.rgb(240, 128, 128)   // LightCoral
    };

    public final static int BLUE = Color.parseColor("#5091F3");
    public final static int Gray = Color.rgb(235, 235, 235);
    private static final String TAG = "ProjectUtil";
    private static final String ORDER_NOTIFICATION_CHANNEL = "order_notification";
    private static final long NOTIFICATION_DEDUP_WINDOW_MS = 2 * 60 * 1000;
    private static String lastNotificationDedupKey = "";
    private static long lastNotificationDedupTime = 0L;

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

    public static void handleNotificationBillWithLlm(Context context, StatusBarNotification sbn) {
        RawNotification rawNotification = RawNotification.from(sbn);
        if (rawNotification == null || isEmpty(rawNotification.readableText())) {
            return;
        }
        if (context.getPackageName().equals(rawNotification.packageName)) {
            return;
        }
        if (isDuplicateNotification(rawNotification)) {
            return;
        }

        Context appContext = context.getApplicationContext();
        new Thread(() -> {
            try {
                BillParseResult bill = parseNotificationBillWithLlm(appContext, rawNotification);
                if (bill == null || !bill.isBill) {
                    return;
                }
                ContentValues values = buildOrderValues(appContext, bill);
                int orderId = addOrderToRemoteAndLocal(appContext, values);
                if (orderId > 0) {
                    bill.id = orderId;
                    showAutoBillNotification(appContext, bill);
                }
            } catch (Exception e) {
                Log.e(TAG, "handle notification bill failed", e);
            }
        }).start();
    }

    private static BillParseResult parseNotificationBillWithLlm(Context context, RawNotification rawNotification) throws IOException, JSONException {
        JSONObject requestJson = new JSONObject();
        requestJson.put("model", "qwen");
        requestJson.put("json", true);
        requestJson.put("think", false);
        requestJson.put("raw", false);
        requestJson.put("system", "你是一个账单通知解析器。你只能返回合法 JSON,不要解释。");
        requestJson.put("prompt", buildBillPrompt(rawNotification).toString());
        requestJson.put("expect_schema", buildBillExpectSchema());

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(requestJson.toString(), MediaType.parse("application/json;charset=utf-8"));
        Request request = new Request.Builder()
                .url(joinUrl(BuildConfig.LLM_BASE_URL, "chat"))
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200 || response.body() == null) {
                return null;
            }
            JSONObject llmJson = normalizeLlmJson(response.body().string());
            return BillParseResult.fromJson(context, rawNotification, llmJson);
        }
    }

    private static JSONObject buildBillPrompt(RawNotification rawNotification) throws JSONException {
        JSONObject prompt = new JSONObject();
        JSONObject notification = new JSONObject();
        notification.put("packageName", rawNotification.packageName);
        notification.put("title", safeString(rawNotification.title));
        notification.put("text", safeString(rawNotification.text));
        notification.put("subText", safeString(rawNotification.subText));
        notification.put("bigText", safeString(rawNotification.bigText));
        notification.put("lines", new JSONArray(rawNotification.lines));
        notification.put("postTime", rawNotification.postTime);
        notification.put("key", safeString(rawNotification.key));

        prompt.put("task", "parse_android_notification_bill");
        prompt.put("currentDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        prompt.put("notification", notification);
        prompt.put("allowedCostTypes", new JSONArray(ConstVariable.COST_TYPE));
        prompt.put("allowedPayWays", new JSONArray(ConstVariable.PAY_WAY));
        prompt.put("rules", new JSONArray()
                .put("如果不是账单通知,isBill=false,其余字段给安全默认值。")
                .put("支出 money 必须为负数,收入 money 必须为正数。")
                .put("bankName 必须优先从 allowedPayWays 中选择。")
                .put("costType 必须优先从 allowedCostTypes 中选择;收入统一返回 收入。")
                .put("orderRemark 填商户、对方、场景或简短备注;没有则为空字符串。")
                .put("clock 使用 App 当前格式,例如 5月15日 10:32。"));
        return prompt;
    }

    private static JSONObject buildBillExpectSchema() throws JSONException {
        JSONObject properties = new JSONObject();
        properties.put("isBill", new JSONObject().put("type", "boolean"));
        properties.put("year", new JSONObject().put("type", "integer"));
        properties.put("month", new JSONObject().put("type", "integer"));
        properties.put("day", new JSONObject().put("type", "integer"));
        properties.put("clock", new JSONObject().put("type", "string"));
        properties.put("money", new JSONObject().put("type", "number"));
        properties.put("bankName", new JSONObject().put("type", "string"));
        properties.put("orderRemark", new JSONObject().put("type", "string"));
        properties.put("costType", new JSONObject().put("type", "string"));
        properties.put("confidence", new JSONObject().put("type", "number"));

        return new JSONObject()
                .put("type", "object")
                .put("required", new JSONArray()
                        .put("isBill")
                        .put("year")
                        .put("month")
                        .put("day")
                        .put("clock")
                        .put("money")
                        .put("bankName")
                        .put("orderRemark")
                        .put("costType")
                        .put("confidence"))
                .put("properties", properties);
    }

    private static ContentValues buildOrderValues(Context context, BillParseResult bill) {
        ContentValues values = new ContentValues();
        values.put("year", bill.year);
        values.put("month", bill.month);
        values.put("day", bill.day);
        values.put("clock", bill.clock);
        values.put("money", bill.money);
        values.put("bankName", bill.bankName);
        values.put("orderRemark", bill.orderRemark);
        values.put("costType", bill.costType);
        values.put("userId", (String) SpUtils.get(context, "phoneNum", ""));
        return values;
    }

    private static int addOrderToRemoteAndLocal(Context context, ContentValues values) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        for (String key : values.keySet()) {
            jsonObject.put(key, values.get(key));
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));
        Request request = new Request.Builder()
                .url(ConstVariable.IP + "/addOrder")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200 || response.body() == null) {
                return -1;
            }
            JSONObject jsonResponse = new JSONObject(response.body().string());
            if (!jsonResponse.optBoolean("success", false)) {
                return -1;
            }
            int orderId = Integer.parseInt(jsonResponse.optString("data", "-1"));
            if (orderId <= 0) {
                return -1;
            }
            values.put("id", orderId);
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
            ensureOrderTable(db);
            db.insert("orderInfo", null, values);
            db.close();
            return orderId;
        }
    }

    private static void showAutoBillNotification(Context context, BillParseResult bill) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", bill.id);
        bundle.putInt("year", bill.year);
        bundle.putInt("month", bill.month);
        bundle.putInt("day", bill.day);
        bundle.putString("clock", bill.clock);
        bundle.putFloat("money", (float) bill.money);
        bundle.putString("bankName", bill.bankName);
        bundle.putString("orderRemark", bill.orderRemark);
        bundle.putString("costType", bill.costType);

        Intent orderDetail = new Intent(context, OrderDetailActivity.class);
        orderDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        orderDetail.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                bill.id,
                orderDetail,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String orderType = bill.money >= 0 ? "收入" : "支出";
        String amount = String.format("%.2f", Math.abs(bill.money));
        String summary = bill.bankName + " · " + safeString(bill.orderRemark) + " · " + bill.costType;
        String confidence = bill.confidence > 0 ? "置信度 " + Math.round(bill.confidence * 100) + "% · 点击可编辑账单" : "点击可编辑账单";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ORDER_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("自动记账")
                .setContentText("已记录:" + orderType + " ¥" + amount)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle("已自动记录一笔" + orderType)
                        .bigText("金额: ¥" + amount + "\n来源: " + bill.bankName + "\n备注: " + safeString(bill.orderRemark) + "\n类型: " + bill.costType + "\n时间: " + bill.clock + "\n状态: 已记录,可编辑"))
                .setSubText(summary)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_launcher_foreground, "撤销记录", pendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "改分类", pendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "加备注", pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(10000 + bill.id, builder.build());
        Log.d(TAG, "auto bill notification shown: " + orderType + " " + amount + " " + summary + " " + confidence);
    }

    private static void ensureOrderTable(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' and name='orderInfo'", null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        if (!exists) {
            db.execSQL("create table orderInfo(id int(8),year int(4),month int(2),day int(2),clock varchar(20),money numeric(10,2),bankName varchar(255),orderRemark varchar(255),costType varchar(255),userId varchar(255))");
        }
    }

    private static JSONObject normalizeLlmJson(String responseText) throws JSONException {
        JSONObject root = new JSONObject(extractFirstJsonObject(responseText));
        if (root.has("isBill")) {
            return root;
        }

        String[] keys = new String[]{"data", "result", "response", "message", "content"};
        for (String key : keys) {
            Object value = root.opt(key);
            if (value instanceof JSONObject && ((JSONObject) value).has("isBill")) {
                return (JSONObject) value;
            }
            if (value instanceof String && !isEmpty((String) value)) {
                JSONObject nested = new JSONObject(extractFirstJsonObject((String) value));
                if (nested.has("isBill")) {
                    return nested;
                }
            }
        }
        return root;
    }

    private static String extractFirstJsonObject(String text) throws JSONException {
        if (isEmpty(text)) {
            throw new JSONException("empty json");
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new JSONException("json object not found");
        }
        return text.substring(start, end + 1);
    }

    private static String joinUrl(String baseUrl, String path) {
        if (baseUrl.endsWith("/")) {
            return baseUrl + path;
        }
        return baseUrl + "/" + path;
    }

    private static synchronized boolean isDuplicateNotification(RawNotification rawNotification) {
        String key = rawNotification.dedupKey();
        long now = System.currentTimeMillis();
        if (key.equals(lastNotificationDedupKey) && now - lastNotificationDedupTime < NOTIFICATION_DEDUP_WINDOW_MS) {
            return true;
        }
        lastNotificationDedupKey = key;
        lastNotificationDedupTime = now;
        return false;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().equals("");
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }

    private static boolean contains(String[] values, String target) {
        if (target == null) {
            return false;
        }
        for (String value : values) {
            if (target.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private static class RawNotification {
        final String packageName;
        final String title;
        final String text;
        final String subText;
        final String bigText;
        final ArrayList<String> lines;
        final long postTime;
        final String key;

        RawNotification(String packageName, String title, String text, String subText, String bigText, ArrayList<String> lines, long postTime, String key) {
            this.packageName = safeString(packageName);
            this.title = safeString(title);
            this.text = safeString(text);
            this.subText = safeString(subText);
            this.bigText = safeString(bigText);
            this.lines = lines == null ? new ArrayList<>() : lines;
            this.postTime = postTime;
            this.key = safeString(key);
        }

        static RawNotification from(StatusBarNotification sbn) {
            if (sbn == null || sbn.getNotification() == null || sbn.getNotification().extras == null) {
                return null;
            }
            Bundle extras = sbn.getNotification().extras;
            ArrayList<String> lines = new ArrayList<>();
            CharSequence[] textLines = extras.getCharSequenceArray("android.textLines");
            if (textLines != null) {
                for (CharSequence line : textLines) {
                    if (line != null) {
                        lines.add(line.toString());
                    }
                }
            }
            return new RawNotification(
                    sbn.getPackageName(),
                    charSequenceToString(extras.getCharSequence("android.title")),
                    charSequenceToString(extras.getCharSequence("android.text")),
                    charSequenceToString(extras.getCharSequence("android.subText")),
                    charSequenceToString(extras.getCharSequence("android.bigText")),
                    lines,
                    sbn.getPostTime(),
                    sbn.getKey());
        }

        String readableText() {
            return title + "\n" + text + "\n" + subText + "\n" + bigText + "\n" + lines.toString();
        }

        String dedupKey() {
            if (!isEmpty(key)) {
                return key;
            }
            return packageName + "|" + title + "|" + text + "|" + subText + "|" + bigText;
        }

        private static String charSequenceToString(CharSequence value) {
            return value == null ? "" : value.toString();
        }
    }

    private static class BillParseResult {
        int id;
        final boolean isBill;
        final int year;
        final int month;
        final int day;
        final String clock;
        final double money;
        final String bankName;
        final String orderRemark;
        final String costType;
        final double confidence;

        BillParseResult(boolean isBill, int year, int month, int day, String clock, double money, String bankName, String orderRemark, String costType, double confidence) {
            this.isBill = isBill;
            this.year = year;
            this.month = month;
            this.day = day;
            this.clock = clock;
            this.money = money;
            this.bankName = bankName;
            this.orderRemark = orderRemark;
            this.costType = costType;
            this.confidence = confidence;
        }

        static BillParseResult fromJson(Context context, RawNotification rawNotification, JSONObject jsonObject) {
            boolean isBill = jsonObject.optBoolean("isBill", false);
            if (!isBill) {
                return new BillParseResult(false, getCurrentYear(), getCurrentMonth(), getCurrentDay(), getCurrentTime(), 0, "", "", "其他", 0);
            }

            double money = jsonObject.optDouble("money", 0);
            if (money == 0) {
                return null;
            }

            String bankName = jsonObject.optString("bankName", "");
            if (!contains(ConstVariable.PAY_WAY, bankName)) {
                bankName = inferPayWay(rawNotification.packageName, bankName);
            }

            String costType = jsonObject.optString("costType", "");
            if (money > 0) {
                costType = "收入";
            } else if (!contains(ConstVariable.COST_TYPE, costType)) {
                costType = "其他";
            }

            return new BillParseResult(
                    true,
                    jsonObject.optInt("year", getCurrentYear()),
                    jsonObject.optInt("month", getCurrentMonth()),
                    jsonObject.optInt("day", getCurrentDay()),
                    jsonObject.optString("clock", getCurrentTime()),
                    money,
                    bankName,
                    jsonObject.optString("orderRemark", ""),
                    costType,
                    jsonObject.optDouble("confidence", 0));
        }

        private static String inferPayWay(String packageName, String fallback) {
            if ("com.tencent.mm".equals(packageName)) {
                return "微信";
            }
            if ("com.eg.android.AlipayGphone".equals(packageName)) {
                return "支付宝";
            }
            if (!isEmpty(fallback)) {
                return fallback;
            }
            return "银行卡";
        }
    }

    //旧短信正则解析入口已废弃,账单解析统一走 handleNotificationBillWithLlm。
    @Deprecated
    public static String[] getBankOrderInfo(String bankOrder) {
        Log.d(TAG, "getBankOrderInfo is deprecated. Use handleNotificationBillWithLlm instead.");
        return new String[]{"", "", "0"};
    }

    //获取当日收支金额的方法
    public static double getTodayMoney(Context context) {
        double allTodayOrder = 0.0;
//        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
//        SQLiteDatabase db = smsDb.getWritableDatabase();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
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
//        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
//        SQLiteDatabase db = smsDb.getWritableDatabase();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(context.getFilesDir().toString() + "/orderInfo.db", null, SQLiteDatabase.OPEN_READWRITE);
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
//        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
//        SQLiteDatabase db = smsDb.getWritableDatabase();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
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
//        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
//        SQLiteDatabase db = smsDb.getWritableDatabase();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
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
//        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
//        SQLiteDatabase db = smsDb.getWritableDatabase();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
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
//        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
//        SQLiteDatabase db = smsDb.getWritableDatabase();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
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
//        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
//        SQLiteDatabase db = smsDb.getWritableDatabase();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
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
//        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
//        SQLiteDatabase db = smsDb.getWritableDatabase();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
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
//        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
//        SQLiteDatabase db = smsDb.getWritableDatabase();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
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
        String[] weeks = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
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
            case("学习"):
                return context.getDrawable(R.drawable.ic_study);
            case("其他"):
                return context.getDrawable(R.drawable.ic_others);
            case("房租与水电"):
                return context.getDrawable(R.drawable.ic_rent);
            default:
                return context.getDrawable(R.drawable.ic_self_design);
        }
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
//        SMSDataBase smsDb = new SMSDataBase(context, "orderInfo", null, 1);
//        SQLiteDatabase db = smsDb.getWritableDatabase();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);

        Cursor cursor = db.query("orderInfo", null, null, null, null, null, "id");
        return cursor;
    }
}
