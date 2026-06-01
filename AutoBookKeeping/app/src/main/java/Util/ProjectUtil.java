package Util;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.beta.autobookkeeping.BuildConfig;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.activity.orderDetail.OrderDetailActivity;
import com.beta.autobookkeeping.service.AutoBillNotificationActionReceiver;


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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String DEFAULT_LLM_MODEL = "qwen3.5:9b";
    private static final int LLM_CONTEXT_TOKENS = 93696;
    private static final int LLM_MAX_COMPLETION_TOKENS = 512;
    private static final double LLM_MIN_CONFIDENCE = 0.60;
    private static final int LOG_CHUNK_SIZE = 3000;
    private static final int CATEGORY_ACTIONS_PER_PAGE = 2;
    private static final long BILL_DEDUP_WINDOW_MS = 2 * 60 * 1000;
    private static final long NOTIFICATION_DEDUP_WINDOW_MS = 5 * 60 * 1000;
    private static final HashMap<String, Long> recentNotificationDedupTimes = new HashMap<>();
    private static final String[] BILL_APP_PACKAGE_NAMES = new String[]{
            "com.eg.android.AlipayGphone",
            "com.tencent.mm",
            "com.unionpay",
            "com.unionpay.tsmservice",
            "com.sankuai.meituan",
            "com.sankuai.meituan.takeoutnew",
            "com.dianping.v1",
            "com.jingdong.app.mall",
            "com.jd.jrapp",
            "com.icbc",
            "com.chinamworld.main",
            "com.cmbchina.ccd.pluto.cmbActivity",
            "com.ccb.longjiLife",
            "com.bankcomm.Bankcomm",
            "cn.com.cmbc.newmbank",
            "com.cib.cibmb",
            "com.cebbank.mobile.cemb",
            "com.spdbccc.app",
            "com.pingan.paces.ccms",
            "com.citicbank.mobile",
            "com.hxb.mobile.client",
            "com.psbc.mobilebank",
            "com.boc.bocsoft.mobile"
    };
    private static final String[] BILL_APP_PACKAGE_PREFIXES = new String[]{
            "com.alipay.",
            "com.tencent.mm",
            "com.unionpay",
            "com.jingdong.",
            "com.jd.",
            "com.sankuai.",
            "com.dianping.",
            "com.icbc",
            "com.cmbchina.",
            "com.ccb.",
            "com.bankcomm.",
            "cn.com.cmbc.",
            "com.cib.",
            "com.cebbank.",
            "com.spdb",
            "com.pingan.",
            "com.citicbank.",
            "com.hxb.",
            "com.psbc.",
            "com.boc."
    };
    private static final String[] BILL_APP_LABEL_KEYWORDS = new String[]{
            "银行",
            "信用卡",
            "支付宝",
            "微信",
            "云闪付",
            "美团",
            "大众点评",
            "京东",
            "京东金融",
            "翼支付",
            "数字人民币"
    };

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
        handleRawNotificationBillWithLlm(context, rawNotification);
    }

    public static void handleNotificationBillWithLlmForDebug(Context context, String packageName, String title, String text, String subText, String bigText, ArrayList<String> lines) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        RawNotification rawNotification = new RawNotification(
                packageName,
                title,
                text,
                subText,
                bigText,
                lines,
                System.currentTimeMillis(),
                "debug-" + packageName + "-" + System.currentTimeMillis());
        handleRawNotificationBillWithLlm(context, rawNotification);
    }

    private static void handleRawNotificationBillWithLlm(Context context, RawNotification rawNotification) {
        if (rawNotification == null || isEmpty(rawNotification.readableText())) {
            debugLog("notification.ignore", "empty notification");
            return;
        }
        debugLog("notification.raw", rawNotification.toDebugJson().toString());
        debugLog("notification.readableText", rawNotification.readableText());
        if (context.getPackageName().equals(rawNotification.packageName)) {
            debugLog("notification.ignore", "self package notification");
            return;
        }
        if (!isPotentialBillApp(context, rawNotification)) {
            debugLog("notification.ignore", "not a bill candidate app: " + rawNotification.packageName);
            return;
        }
        if (isDuplicateNotification(rawNotification)) {
            debugLog("notification.ignore", "duplicate notification: " + rawNotification.contentDedupKey());
            return;
        }

        Context appContext = context.getApplicationContext();
        new Thread(() -> {
            try {
                BillParseResult bill = parseNotificationBillWithLlm(appContext, rawNotification);
                if (bill == null || !bill.isBill) {
                    debugLog("bill.ignore", "LLM result is not a bill or invalid");
                    return;
                }
                debugLog("bill.parsed", bill.toDebugJson().toString());
                if (isDuplicateBillByTransactionTime(appContext, bill)) {
                    debugLog("bill.ignore", "duplicate bill by amount/payWay/time window");
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
        requestJson.put("model", DEFAULT_LLM_MODEL);
        requestJson.put("messages", new JSONArray()
                .put(new JSONObject()
                        .put("role", "system")
                        .put("content", "你是一个账单通知解析器。最终只能返回合法 JSON 对象，不要 Markdown、代码块或解释。"))
                .put(new JSONObject()
                        .put("role", "user")
                        .put("content", buildBillPrompt(context, rawNotification).toString())));
        requestJson.put("stream", false);
        requestJson.put("temperature", 0);
        requestJson.put("top_p", 0.95);
        requestJson.put("max_completion_tokens", LLM_MAX_COMPLETION_TOKENS);
        requestJson.put("num_ctx", LLM_CONTEXT_TOKENS);
        requestJson.put("think", false);
        requestJson.put("response_format", new JSONObject().put("type", "json_object"));

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();
        String requestText = requestJson.toString();
        String llmUrl = joinUrl(BuildConfig.LLM_BASE_URL, "v1/chat/completions");
        debugLog("llm.url", llmUrl);
        debugLog("llm.request", requestText);
        RequestBody body = RequestBody.create(requestText, MediaType.parse("application/json;charset=utf-8"));
        Request request = new Request.Builder()
                .url(llmUrl)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseText = response.body() == null ? "" : response.body().string();
            debugLog("llm.response.status", response.code() + " " + response.message());
            debugLog("llm.response.body", responseText);
            if (!response.isSuccessful() || isEmpty(responseText)) {
                return null;
            }
            JSONObject llmJson = normalizeLlmJson(responseText);
            debugLog("llm.response.normalized", llmJson.toString());
            return BillParseResult.fromJson(context, rawNotification, llmJson);
        }
    }

    private static JSONObject buildBillPrompt(Context context, RawNotification rawNotification) throws JSONException {
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
        prompt.put("alipayXiaohebao", buildAlipayXiaohebaoPrompt(context));
        prompt.put("outputSchema", buildBillExpectSchema());
        prompt.put("emptyResultExample", new JSONObject()
                .put("isBill", false)
                .put("year", getCurrentYear())
                .put("month", getCurrentMonth())
                .put("day", getCurrentDay())
                .put("clock", "")
                .put("money", 0)
                .put("bankName", "")
                .put("orderRemark", "")
                .put("costType", "其他")
                .put("confidence", 0));
        prompt.put("rules", new JSONArray()
                .put("只能返回一个 JSON 对象,不能返回 Markdown、代码块或解释。")
                .put("返回 JSON 必须包含 outputSchema.required 中所有字段。")
                .put("如果不是账单通知,isBill=false,其余字段给安全默认值。")
                .put("alipayXiaohebao 只用于支付宝小荷包多人消费通知的成员匹配,不要因此拒绝微信、银行或其他来源的真实账单。")
                .put("alipayXiaohebao.enabled=true 不代表只解析小荷包;如果支付宝通知标题和正文没有出现“小荷包”,必须按普通支付宝交易提醒判断。")
                .put("如果是支付宝小荷包通知,且通知内容出现“某某消费了、支付了、付款了、支出了”,只有“某某”与 alipayXiaohebao.nickname 完全对应时才算自己的账单;不对应或无法确认时 isBill=false。")
                .put("如果 alipayXiaohebao.enabled=false,不要因为小荷包多人消费通知自动记账。")
                .put("普通支付宝、微信、银行卡交易提醒中,如果出现“你有一笔X元的支出/收入”、“支出X元”、“收入X元”、“支付成功”、“扣款成功”、“收款成功”、“入账”等明确交易语义且金额明确,必须返回 isBill=true。")
                .put("例如支付宝普通通知“你有一笔0.10元的支出”是账单,应返回 isBill=true、money=-0.10、bankName=支付宝;不要因为金额小、出现积分、或小荷包功能开启而返回 false。")
                .put("支出 money 必须为负数,收入 money 必须为正数。")
                .put("bankName 必须优先从 allowedPayWays 中选择。")
                .put("costType 必须优先从 allowedCostTypes 中选择;收入统一返回 收入;支出消费类型无法确定时返回 消费。")
                .put("orderRemark 必须返回空字符串,不要根据商户、对方或场景主动填写备注,备注由用户自己编辑。")
                .put("money 必须来自 notification.title、text、subText、bigText 或 lines 原文中的明确金额;如果原文没有金额,必须返回 isBill=false。")
                .put("禁止使用示例中的金额、时间、支付方式作为真实结果;不能根据常识或上下文补金额。")
                .put("出现待支付、未支付、还未支付、自动取消、配置更新、登录、验证码等内容时必须返回 isBill=false。")
                .put("clock 使用 App 当前格式,例如 5月15日 10:32。")
                .put("返回前逐项自检: 金额必须匹配原文明确金额, 收支正负号必须符合语义, bankName 和 costType 必须在候选中尽量选择最准确项。")
                .put("如果金额、收支方向、支付方式或是否账单存在不确定,优先返回 isBill=false 或降低 confidence;不要为了自动记账而猜测。"));
        return prompt;
    }

    private static boolean isPotentialBillApp(Context context, RawNotification rawNotification) {
        String packageName = rawNotification.packageName;
        if (contains(BILL_APP_PACKAGE_NAMES, packageName)) {
            return true;
        }
        for (String prefix : BILL_APP_PACKAGE_PREFIXES) {
            if (packageName.startsWith(prefix)) {
                return true;
            }
        }

        String appLabel = getApplicationLabel(context, packageName);
        for (String keyword : BILL_APP_LABEL_KEYWORDS) {
            if (appLabel.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String getApplicationLabel(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            CharSequence label = packageManager.getApplicationLabel(applicationInfo);
            return label == null ? "" : label.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean amountMatchesNotification(RawNotification rawNotification, double money) {
        double expected = Math.abs(money);
        for (double amount : extractAmounts(rawNotification.readableText())) {
            if (Math.abs(amount - expected) < 0.01) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<Double> extractAmounts(String text) {
        ArrayList<Double> amounts = new ArrayList<>();
        if (isEmpty(text)) {
            return amounts;
        }
        addMatchedAmounts(amounts, text, Pattern.compile("(?:(?:￥|¥|人民币|RMB|CNY)\\s*([0-9]+(?:,[0-9]{3})*(?:\\.\\d{1,2})?|[0-9]+(?:\\.\\d{1,2})?)|([0-9]+(?:,[0-9]{3})*(?:\\.\\d{1,2})?|[0-9]+(?:\\.\\d{1,2})?)\\s*(?:元|块|圆))"));
        addMatchedAmounts(amounts, text, Pattern.compile("(?:金额|消费|支付|支出|收入|转入|转出|扣款|付款|收款|入账|提现|退款)[^0-9￥¥]{0,8}(?:￥|¥)?\\s*([0-9]+(?:,[0-9]{3})*(?:\\.\\d{1,2})?|[0-9]+(?:\\.\\d{1,2})?)"));
        return amounts;
    }

    private static void addMatchedAmounts(ArrayList<Double> amounts, String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String value = firstNonEmptyGroup(matcher);
            double amount = safeParseDouble(value.replace(",", ""), 0);
            if (amount > 0 && !containsAmount(amounts, amount)) {
                amounts.add(amount);
            }
        }
    }

    private static String firstNonEmptyGroup(Matcher matcher) {
        for (int i = 1; i <= matcher.groupCount(); i++) {
            String value = matcher.group(i);
            if (!isEmpty(value)) {
                return value;
            }
        }
        return "";
    }

    private static boolean containsAmount(ArrayList<Double> amounts, double target) {
        for (double amount : amounts) {
            if (Math.abs(amount - target) < 0.01) {
                return true;
            }
        }
        return false;
    }

    private static String firstAmountOrEmpty(String text) {
        ArrayList<Double> amounts = extractAmounts(text);
        if (amounts.isEmpty()) {
            return "";
        }
        return String.format(Locale.US, "%.2f", amounts.get(0));
    }

    private static double safeParseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static JSONObject buildAlipayXiaohebaoPrompt(Context context) throws JSONException {
        String nickname = (String) SpUtils.get(context, "is_alipay_xiaohebao", "");
        return new JSONObject()
                .put("enabled", !isEmpty(nickname))
                .put("nickname", safeString(nickname))
                .put("matchingRule", "小荷包通知中如果出现“xxx消费了、支付了、付款了、支出了”,xxx 必须与 nickname 对应才记录;其他成员消费返回 isBill=false");
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

    private static boolean isDuplicateBillByTransactionTime(Context context, BillParseResult bill) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
        Cursor cursor = null;
        try {
            ensureOrderTable(db);
            cursor = db.query(
                    "orderInfo",
                    null,
                    "year=? and month=? and day=?",
                    new String[]{String.valueOf(bill.year), String.valueOf(bill.month), String.valueOf(bill.day)},
                    null,
                    null,
                    "id desc");
            long billTimeMs = parseBillTimeMs(bill.year, bill.month, bill.day, bill.clock);
            while (cursor.moveToNext()) {
                double existingMoney = cursor.getDouble(5);
                String existingBankName = cursor.getString(6);
                String existingClock = cursor.getString(4);
                if (!safeString(existingBankName).equals(safeString(bill.bankName))) {
                    continue;
                }
                if (Math.abs(existingMoney - bill.money) >= 0.01) {
                    continue;
                }
                long existingTimeMs = parseBillTimeMs(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), existingClock);
                long diffMs = Math.abs(existingTimeMs - billTimeMs);
                debugLog("bill.dedup.compare", "existingId=" + cursor.getInt(0) + ", money=" + existingMoney + ", bankName=" + existingBankName + ", existingClock=" + existingClock + ", newClock=" + bill.clock + ", diffMs=" + diffMs);
                if (diffMs <= BILL_DEDUP_WINDOW_MS) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "check duplicate bill failed", e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    private static long parseBillTimeMs(int year, int month, int day, String clock) {
        int hour = 0;
        int minute = 0;
        Matcher matcher = Pattern.compile("(\\d{1,2})\\s*:\\s*(\\d{1,2})").matcher(safeString(clock));
        if (matcher.find()) {
            hour = safeParseInt(matcher.group(1), 0);
            minute = safeParseInt(matcher.group(2), 0);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static int safeParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static int addOrderToRemoteAndLocal(Context context, ContentValues values) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        for (String key : values.keySet()) {
            jsonObject.put(key, values.get(key));
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        String requestText = jsonObject.toString();
        String addOrderUrl = ConstVariable.IP + "/addOrder";
        debugLog("addOrder.url", addOrderUrl);
        debugLog("addOrder.request", requestText);
        RequestBody body = RequestBody.create(requestText, MediaType.parse("application/json;charset=utf-8"));
        Request request = new Request.Builder()
                .url(addOrderUrl)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseText = response.body() == null ? "" : response.body().string();
            debugLog("addOrder.response.status", response.code() + " " + response.message());
            debugLog("addOrder.response.body", responseText);
            if (response.code() != 200 || isEmpty(responseText)) {
                return -1;
            }
            JSONObject jsonResponse = new JSONObject(responseText);
            if (!jsonResponse.optBoolean("success", false)) {
                return -1;
            }
            int orderId = Integer.parseInt(jsonResponse.optString("data", "-1"));
            if (orderId <= 0) {
                return -1;
            }
            values.put("id", orderId);
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
            try {
                ensureOrderTable(db);
                long insertResult = db.insert("orderInfo", null, values);
                debugLog("sqlite.insert.orderInfo", "result=" + insertResult + ", orderId=" + orderId);
                return insertResult >= 0 ? orderId : -1;
            } finally {
                db.close();
            }
        }
    }

    private static void showAutoBillNotification(Context context, BillParseResult bill) {
        showAutoBillMainNotification(context, buildAutoBillBundle(bill));
        Log.d(TAG, "auto bill notification shown: " + (bill.money >= 0 ? "收入" : "支出") + " " + String.format("%.2f", Math.abs(bill.money)));
    }

    private static Bundle buildAutoBillBundle(BillParseResult bill) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", bill.id);
        bundle.putInt("year", bill.year);
        bundle.putInt("month", bill.month);
        bundle.putInt("day", bill.day);
        bundle.putString("clock", bill.clock);
        bundle.putDouble("money", bill.money);
        bundle.putString("bankName", bill.bankName);
        bundle.putString("orderRemark", bill.orderRemark);
        bundle.putString("costType", bill.costType);
        return bundle;
    }

    private static void showAutoBillMainNotification(Context context, Bundle billBundle) {
        Intent orderDetail = new Intent(context, OrderDetailActivity.class);
        orderDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        orderDetail.putExtras(billBundle);
        int orderId = billBundle.getInt("id");
        double money = billBundle.getDouble("money");
        String bankName = safeString(billBundle.getString("bankName"));
        String costType = safeString(billBundle.getString("costType"));
        String clock = safeString(billBundle.getString("clock"));
        PendingIntent detailPendingIntent = PendingIntent.getActivity(
                context,
                orderId,
                orderDetail,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String orderType = money >= 0 ? "收入" : "支出";
        String amount = String.format("%.2f", Math.abs(money));
        String summary = bankName + " · " + costType + " · " + clock;
        PendingIntent undoPendingIntent = PendingIntent.getBroadcast(
                context,
                orderId * 10 + 1,
                buildNotificationActionIntent(context, AutoBillNotificationActionReceiver.ACTION_UNDO_AUTO_BILL, billBundle),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        RemoteInput remarkInput = new RemoteInput.Builder(AutoBillNotificationActionReceiver.KEY_TEXT_REPLY)
                .setLabel("添加备注")
                .build();
        PendingIntent remarkPendingIntent = PendingIntent.getBroadcast(
                context,
                orderId * 10 + 2,
                buildNotificationActionIntent(context, AutoBillNotificationActionReceiver.ACTION_ADD_AUTO_BILL_REMARK, billBundle),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        PendingIntent categoryPendingIntent = PendingIntent.getBroadcast(
                context,
                orderId * 10 + 3,
                buildCategoryPageActionIntent(context, billBundle, 0),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ORDER_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(orderType + " " + amount + "元")
                .setContentText(summary)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(orderType + " " + amount + "元")
                        .bigText("来源: " + bankName + "\n分类: " + costType + "\n时间: " + clock + "\n状态: 已记录,可编辑"))
                .setSubText(summary)
                .setContentIntent(detailPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(Notification.DEFAULT_ALL)
                .addAction(R.drawable.ic_launcher_foreground, "撤销记录", undoPendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "更多分类", categoryPendingIntent)
                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_launcher_foreground, "添加备注", remarkPendingIntent)
                        .addRemoteInput(remarkInput)
                        .setAllowGeneratedReplies(false)
                        .build());

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(10000 + orderId, builder.build());
    }

    private static Intent buildNotificationActionIntent(Context context, String action, Bundle billBundle) {
        Intent intent = new Intent(context, AutoBillNotificationActionReceiver.class);
        intent.setAction(action);
        intent.putExtra(AutoBillNotificationActionReceiver.EXTRA_ORDER_ID, billBundle.getInt("id"));
        intent.putExtra(AutoBillNotificationActionReceiver.EXTRA_NOTIFICATION_ID, 10000 + billBundle.getInt("id"));
        intent.putExtra(AutoBillNotificationActionReceiver.EXTRA_YEAR, billBundle.getInt("year"));
        intent.putExtra(AutoBillNotificationActionReceiver.EXTRA_MONTH, billBundle.getInt("month"));
        intent.putExtra(AutoBillNotificationActionReceiver.EXTRA_DAY, billBundle.getInt("day"));
        intent.putExtra(AutoBillNotificationActionReceiver.EXTRA_CLOCK, billBundle.getString("clock"));
        intent.putExtra(AutoBillNotificationActionReceiver.EXTRA_MONEY, billBundle.getDouble("money"));
        intent.putExtra(AutoBillNotificationActionReceiver.EXTRA_BANK_NAME, billBundle.getString("bankName"));
        intent.putExtra(AutoBillNotificationActionReceiver.EXTRA_ORDER_REMARK, billBundle.getString("orderRemark"));
        intent.putExtra(AutoBillNotificationActionReceiver.EXTRA_COST_TYPE, billBundle.getString("costType"));
        return intent;
    }

    private static Intent buildCategoryActionIntent(Context context, Bundle billBundle, String costType) {
        Intent intent = buildNotificationActionIntent(context, AutoBillNotificationActionReceiver.ACTION_UPDATE_AUTO_BILL_CATEGORY, billBundle);
        intent.putExtra(AutoBillNotificationActionReceiver.EXTRA_SELECTED_COST_TYPE, costType);
        return intent;
    }

    private static Intent buildCategoryPageActionIntent(Context context, Bundle billBundle, int page) {
        Intent intent = buildNotificationActionIntent(context, AutoBillNotificationActionReceiver.ACTION_SHOW_AUTO_BILL_CATEGORIES, billBundle);
        intent.putExtra(AutoBillNotificationActionReceiver.EXTRA_CATEGORY_PAGE, page);
        return intent;
    }

    private static Intent buildMainNotificationActionIntent(Context context, Bundle billBundle) {
        return buildNotificationActionIntent(context, AutoBillNotificationActionReceiver.ACTION_SHOW_AUTO_BILL_MAIN, billBundle);
    }

    public static void showAutoBillMainNotificationFromAction(Context context, Intent intent) {
        showAutoBillMainNotification(context, buildAutoBillBundleFromIntent(intent));
    }

    public static void showAutoBillCategoryNotificationFromAction(Context context, Intent intent) {
        showAutoBillCategoryNotification(context, buildAutoBillBundleFromIntent(intent), intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_CATEGORY_PAGE, 0));
    }

    private static void showAutoBillCategoryNotification(Context context, Bundle billBundle, int page) {
        int orderId = billBundle.getInt("id");
        if (orderId <= 0) {
            return;
        }
        int pageCount = (int) Math.ceil((double) ConstVariable.COST_TYPE.length / CATEGORY_ACTIONS_PER_PAGE);
        int safePage = Math.max(0, Math.min(page, pageCount - 1));
        int start = safePage * CATEGORY_ACTIONS_PER_PAGE;
        int end = Math.min(start + CATEGORY_ACTIONS_PER_PAGE, ConstVariable.COST_TYPE.length);
        double money = billBundle.getDouble("money");
        String orderType = money >= 0 ? "收入" : "支出";
        String amount = String.format("%.2f", Math.abs(money));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ORDER_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("选择类别 · " + orderType + " " + amount + "元")
                .setContentText("第 " + (safePage + 1) + "/" + pageCount + " 页")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle("选择消费类别")
                        .bigText("当前分类: " + safeString(billBundle.getString("costType")) + "\n选择后将自动保存到账单"))
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        for (int i = start; i < end; i++) {
            String costType = ConstVariable.COST_TYPE[i];
            builder.addAction(
                    R.drawable.ic_launcher_foreground,
                    costType,
                    PendingIntent.getBroadcast(
                            context,
                            orderId * 100 + i,
                            buildCategoryActionIntent(context, billBundle, costType),
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        }

        boolean hasNextPage = safePage < pageCount - 1;
        Intent navIntent = hasNextPage
                ? buildCategoryPageActionIntent(context, billBundle, safePage + 1)
                : buildMainNotificationActionIntent(context, billBundle);
        builder.addAction(
                R.drawable.ic_launcher_foreground,
                hasNextPage ? "下一页" : "返回",
                PendingIntent.getBroadcast(
                        context,
                        orderId * 100 + 90 + safePage,
                        navIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(10000 + orderId, builder.build());
        debugLog("autoBill.categoryPage", "orderId=" + orderId + ", page=" + safePage);
    }

    private static Bundle buildAutoBillBundleFromIntent(Intent intent) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_ORDER_ID, -1));
        bundle.putInt("year", intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_YEAR, getCurrentYear()));
        bundle.putInt("month", intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_MONTH, getCurrentMonth()));
        bundle.putInt("day", intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_DAY, getCurrentDay()));
        bundle.putString("clock", intent.getStringExtra(AutoBillNotificationActionReceiver.EXTRA_CLOCK));
        bundle.putDouble("money", intent.getDoubleExtra(AutoBillNotificationActionReceiver.EXTRA_MONEY, 0));
        bundle.putString("bankName", intent.getStringExtra(AutoBillNotificationActionReceiver.EXTRA_BANK_NAME));
        bundle.putString("orderRemark", intent.getStringExtra(AutoBillNotificationActionReceiver.EXTRA_ORDER_REMARK));
        bundle.putString("costType", intent.getStringExtra(AutoBillNotificationActionReceiver.EXTRA_COST_TYPE));
        return bundle;
    }

    public static void undoAutoBillFromNotification(Context context, Intent intent) {
        int orderId = intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_ORDER_ID, -1);
        int notificationId = intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_NOTIFICATION_ID, 10000 + orderId);
        if (orderId <= 0) {
            return;
        }
        new Thread(() -> {
            boolean success = deleteOrderRemote(orderId);
            if (success) {
                deleteOrderLocal(context, orderId);
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationId);
                showToastOnMainThread(context, "记录已撤销");
                debugLog("autoBill.undo", "orderId=" + orderId);
            }
        }).start();
    }

    public static void updateAutoBillRemarkFromNotification(Context context, Intent intent, String remark) {
        int orderId = intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_ORDER_ID, -1);
        if (orderId <= 0 || isEmpty(remark)) {
            return;
        }
        new Thread(() -> {
            try {
                JSONObject jsonObject = buildOrderUpdateJsonFromIntent(context, intent);
                jsonObject.put("orderRemark", remark);
                boolean success = modifyOrderRemote(orderId, jsonObject);
                if (success) {
                    updateOrderLocal(context, orderId, "orderRemark", remark);
                    cancelNotification(context, intent);
                    showToastOnMainThread(context, "备注已修改");
                    debugLog("autoBill.remark", "orderId=" + orderId + ", remark=" + remark);
                }
            } catch (JSONException e) {
                Log.e(TAG, "update auto bill remark failed", e);
            }
        }).start();
    }

    public static void updateAutoBillCategoryFromNotification(Context context, Intent intent, String costType) {
        int orderId = intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_ORDER_ID, -1);
        if (orderId <= 0 || isEmpty(costType) || !contains(ConstVariable.COST_TYPE, costType)) {
            return;
        }
        new Thread(() -> {
            try {
                JSONObject jsonObject = buildOrderUpdateJsonFromIntent(context, intent);
                jsonObject.put("costType", costType);
                boolean success = modifyOrderRemote(orderId, jsonObject);
                if (success) {
                    updateOrderLocal(context, orderId, "costType", costType);
                    cancelNotification(context, intent);
                    showToastOnMainThread(context, "类别已修改");
                    debugLog("autoBill.category", "orderId=" + orderId + ", costType=" + costType);
                }
            } catch (JSONException e) {
                Log.e(TAG, "update auto bill category failed", e);
            }
        }).start();
    }

    private static JSONObject buildOrderUpdateJsonFromIntent(Context context, Intent intent) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("month", intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_MONTH, getCurrentMonth()));
        jsonObject.put("day", intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_DAY, getCurrentDay()));
        jsonObject.put("clock", intent.getStringExtra(AutoBillNotificationActionReceiver.EXTRA_CLOCK));
        jsonObject.put("money", intent.getDoubleExtra(AutoBillNotificationActionReceiver.EXTRA_MONEY, 0));
        jsonObject.put("bankName", intent.getStringExtra(AutoBillNotificationActionReceiver.EXTRA_BANK_NAME));
        jsonObject.put("orderRemark", intent.getStringExtra(AutoBillNotificationActionReceiver.EXTRA_ORDER_REMARK));
        jsonObject.put("costType", intent.getStringExtra(AutoBillNotificationActionReceiver.EXTRA_COST_TYPE));
        return jsonObject;
    }

    private static boolean deleteOrderRemote(int orderId) {
        String url = ConstVariable.IP + "/deleteOrder/" + orderId;
        debugLog("deleteOrder.url", url);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(url).delete().build();
        try (Response response = client.newCall(request).execute()) {
            String responseText = response.body() == null ? "" : response.body().string();
            debugLog("deleteOrder.response.status", response.code() + " " + response.message());
            debugLog("deleteOrder.response.body", responseText);
            if (response.code() != 200 || isEmpty(responseText)) {
                return false;
            }
            return new JSONObject(responseText).optBoolean("success", false);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "delete order failed", e);
            return false;
        }
    }

    private static boolean modifyOrderRemote(int orderId, JSONObject jsonObject) {
        String url = ConstVariable.IP + "/modifyOrder/" + orderId;
        String requestText = jsonObject.toString();
        debugLog("modifyOrder.url", url);
        debugLog("modifyOrder.request", requestText);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        RequestBody body = RequestBody.create(requestText, MediaType.parse("application/json;charset=utf-8"));
        Request request = new Request.Builder().url(url).put(body).build();
        try (Response response = client.newCall(request).execute()) {
            String responseText = response.body() == null ? "" : response.body().string();
            debugLog("modifyOrder.response.status", response.code() + " " + response.message());
            debugLog("modifyOrder.response.body", responseText);
            if (response.code() != 200 || isEmpty(responseText)) {
                return false;
            }
            return new JSONObject(responseText).optBoolean("success", false);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "modify order failed", e);
            return false;
        }
    }

    private static void deleteOrderLocal(Context context, int orderId) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
        try {
            ensureOrderTable(db);
            int deleted = db.delete("orderInfo", "id=?", new String[]{String.valueOf(orderId)});
            debugLog("sqlite.delete.orderInfo", "orderId=" + orderId + ", deleted=" + deleted);
        } finally {
            db.close();
        }
    }

    private static void updateOrderLocal(Context context, int orderId, String column, String value) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
        try {
            ensureOrderTable(db);
            ContentValues values = new ContentValues();
            values.put(column, value);
            int updated = db.update("orderInfo", values, "id=?", new String[]{String.valueOf(orderId)});
            debugLog("sqlite.update.orderInfo", "orderId=" + orderId + ", column=" + column + ", updated=" + updated);
        } finally {
            db.close();
        }
    }

    private static void cancelNotification(Context context, Intent intent) {
        int orderId = intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_ORDER_ID, -1);
        int notificationId = intent.getIntExtra(AutoBillNotificationActionReceiver.EXTRA_NOTIFICATION_ID, 10000 + orderId);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    private static void showToastOnMainThread(Context context, String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show());
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
        String contentText = extractStreamingLlmContent(responseText);
        JSONObject root = new JSONObject(extractFirstJsonObject(contentText));

        String choiceContent = extractOpenAiChoiceContent(root);
        if (!isEmpty(choiceContent)) {
            return normalizeLlmJson(choiceContent);
        }

        if (root.has("isBill")) {
            ensureBillJsonSchema(root);
            return root;
        }

        String[] keys = new String[]{"data", "result", "response", "message", "content"};
        for (String key : keys) {
            Object value = root.opt(key);
            if (value instanceof JSONObject) {
                try {
                    return normalizeLlmJson(((JSONObject) value).toString());
                } catch (JSONException ignored) {
                }
            }
            if (value instanceof String && !isEmpty((String) value)) {
                try {
                    return normalizeLlmJson((String) value);
                } catch (JSONException ignored) {
                }
            }
        }

        ensureBillJsonSchema(root);
        return root;
    }

    private static String extractStreamingLlmContent(String responseText) {
        if (isEmpty(responseText) || !responseText.contains("data:")) {
            return responseText;
        }

        StringBuilder content = new StringBuilder();
        String[] lines = responseText.split(String.valueOf((char) 10));
        for (String line : lines) {
            String trimmed = safeString(line).trim();
            if (!trimmed.startsWith("data:")) {
                continue;
            }
            String payload = trimmed.substring(5).trim();
            if (isEmpty(payload) || "[DONE]".equals(payload)) {
                continue;
            }
            try {
                JSONObject event = new JSONObject(payload);
                String choiceContent = extractOpenAiChoiceContent(event);
                if (!isEmpty(choiceContent)) {
                    content.append(choiceContent);
                } else if (event.has("content")) {
                    content.append(event.optString("content", ""));
                }
            } catch (JSONException ignored) {
            }
        }
        return content.length() > 0 ? content.toString() : responseText;
    }

    private static String extractOpenAiChoiceContent(JSONObject root) {
        JSONArray choices = root.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            return "";
        }

        JSONObject choice = choices.optJSONObject(0);
        if (choice == null) {
            return "";
        }

        JSONObject delta = choice.optJSONObject("delta");
        if (delta != null) {
            String content = delta.optString("content", "");
            if (!isEmpty(content)) {
                return content;
            }
        }

        JSONObject message = choice.optJSONObject("message");
        if (message != null) {
            String content = message.optString("content", "");
            if (!isEmpty(content)) {
                return content;
            }
        }

        return choice.optString("text", "");
    }

    private static void ensureBillJsonSchema(JSONObject jsonObject) throws JSONException {
        String[] required = new String[]{"isBill", "year", "month", "day", "clock", "money", "bankName", "orderRemark", "costType", "confidence"};
        for (String key : required) {
            if (!jsonObject.has(key)) {
                throw new JSONException("LLM JSON missing required key: " + key);
            }
        }
    }

    private static String extractFirstJsonObject(String text) throws JSONException {
        if (isEmpty(text)) {
            throw new JSONException("empty json");
        }

        String firstValid = "";
        for (int start = 0; start < text.length(); start++) {
            if (text.charAt(start) != 123) {
                continue;
            }

            int depth = 0;
            boolean inString = false;
            boolean escape = false;
            for (int i = start; i < text.length(); i++) {
                char c = text.charAt(i);
                if (escape) {
                    escape = false;
                    continue;
                }
                if (inString && c == 92) {
                    escape = true;
                    continue;
                }
                if (c == 34) {
                    inString = !inString;
                    continue;
                }
                if (inString) {
                    continue;
                }
                if (c == 123) {
                    depth++;
                } else if (c == 125) {
                    depth--;
                    if (depth == 0) {
                        String candidate = text.substring(start, i + 1);
                        try {
                            JSONObject parsed = new JSONObject(candidate);
                            if (parsed.has("isBill")) {
                                return candidate;
                            }
                            if (isEmpty(firstValid)) {
                                firstValid = candidate;
                            }
                        } catch (JSONException ignored) {
                        }
                        break;
                    }
                }
            }
        }

        if (!isEmpty(firstValid)) {
            return firstValid;
        }
        throw new JSONException("json object not found");
    }

    private static String joinUrl(String baseUrl, String path) {
        if (baseUrl.endsWith("/")) {
            return baseUrl + path;
        }
        return baseUrl + "/" + path;
    }

    private static synchronized boolean isDuplicateNotification(RawNotification rawNotification) {
        String key = rawNotification.contentDedupKey();
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = recentNotificationDedupTimes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (now - entry.getValue() >= NOTIFICATION_DEDUP_WINDOW_MS) {
                iterator.remove();
            }
        }
        Long lastSeenTime = recentNotificationDedupTimes.get(key);
        if (lastSeenTime != null && now - lastSeenTime < NOTIFICATION_DEDUP_WINDOW_MS) {
            return true;
        }
        recentNotificationDedupTimes.put(key, now);
        return false;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().equals("");
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }

    private static void debugLog(String label, String message) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        String safeMessage = message == null ? "null" : message;
        if (safeMessage.length() <= LOG_CHUNK_SIZE) {
            Log.d(TAG, label + ": " + safeMessage);
            return;
        }
        int chunkIndex = 0;
        for (int start = 0; start < safeMessage.length(); start += LOG_CHUNK_SIZE) {
            int end = Math.min(start + LOG_CHUNK_SIZE, safeMessage.length());
            Log.d(TAG, label + "[" + chunkIndex + "]: " + safeMessage.substring(start, end));
            chunkIndex++;
        }
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


        String contentDedupKey() {
            String content = packageName + "|" + title + "|" + text + "|" + firstAmountOrEmpty(readableText());
            return content.replaceAll("\\s+", "");
        }

        JSONObject toDebugJson() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("packageName", packageName);
                jsonObject.put("title", title);
                jsonObject.put("text", text);
                jsonObject.put("subText", subText);
                jsonObject.put("bigText", bigText);
                jsonObject.put("lines", new JSONArray(lines));
                jsonObject.put("postTime", postTime);
                jsonObject.put("key", key);
            } catch (JSONException e) {
                Log.e(TAG, "build raw notification debug json failed", e);
            }
            return jsonObject;
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

            double confidence = jsonObject.optDouble("confidence", 0);
            if (confidence < LLM_MIN_CONFIDENCE) {
                debugLog("bill.ignore", "LLM confidence too low: " + confidence);
                return null;
            }

            if (!amountMatchesNotification(rawNotification, money)) {
                debugLog("bill.ignore", "LLM money does not match notification amount: " + money);
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
                costType = "消费";
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(rawNotification.postTime);
            String clock = new SimpleDateFormat("M月d日 HH:mm").format(new Date(rawNotification.postTime));

            return new BillParseResult(
                    true,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    clock,
                    money,
                    bankName,
                    "",
                    costType,
                    confidence);
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

        JSONObject toDebugJson() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", id);
                jsonObject.put("isBill", isBill);
                jsonObject.put("year", year);
                jsonObject.put("month", month);
                jsonObject.put("day", day);
                jsonObject.put("clock", clock);
                jsonObject.put("money", money);
                jsonObject.put("bankName", bankName);
                jsonObject.put("orderRemark", orderRemark);
                jsonObject.put("costType", costType);
                jsonObject.put("confidence", confidence);
            } catch (JSONException e) {
                Log.e(TAG, "build bill debug json failed", e);
            }
            return jsonObject;
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
