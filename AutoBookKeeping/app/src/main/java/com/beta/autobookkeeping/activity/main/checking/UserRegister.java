package com.beta.autobookkeeping.activity.main.checking;

import static Util.ConstVariable.IP;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.Nullable;

import com.beta.autobookkeeping.activity.main.MainActivity;
import com.hss01248.dialog.StyledDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import Util.ProjectUtil;
import Util.SpUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class UserRegister {
    private static final String TAG = "UserRegister";
    private static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private UserRegister() {
    }

    public interface SyncCallback {
        void onSuccess();

        void onFailure(String message);
    }

    public static void login(String phoneNum, String password, Context context) {
        authenticate("/auth/login", phoneNum, password, context);
    }

    public static void register(String phoneNum, String password, Context context) {
        authenticate("/auth/register", phoneNum, password, context);
    }

    /** Kept only for compatibility with code from old releases. */
    @Deprecated
    public static void userRegister(String phoneNum, String password, Context context) {
        authenticate("/addUser", phoneNum, password, context);
    }

    private static void authenticate(String path, String rawPhoneNum, String password, Context context) {
        String phoneNum = rawPhoneNum == null ? "" : rawPhoneNum.trim();
        StyledDialog.buildLoading().show();
        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("phoneNum", phoneNum);
                payload.put("password", password == null ? "" : password);
                Request request = new Request.Builder()
                        .url(IP + path)
                        .post(RequestBody.create(payload.toString(), JSON))
                        .build();
                try (Response response = CLIENT.newCall(request).execute()) {
                    String responseText = response.body() == null ? "" : response.body().string();
                    if (!response.isSuccessful() || responseText.isEmpty()) {
                        showAuthenticationFailure(context, "服务器暂时不可用，请稍后重试");
                        return;
                    }
                    JSONObject jsonResponse = new JSONObject(responseText);
                    if (!jsonResponse.optBoolean("success", false)) {
                        showAuthenticationFailure(context, jsonResponse.optString("message", "登录失败"));
                        return;
                    }

                    JSONObject profile = jsonResponse.optJSONObject("data");
                    if (profile == null) {
                        showAuthenticationFailure(context, "账号资料不完整，请稍后重试");
                        return;
                    }
                    boolean orderSyncSucceeded = syncOrders(context, phoneNum);
                    if (!orderSyncSucceeded) {
                        showAuthenticationFailure(context, "账号验证成功，但账单同步失败；本机原有数据未改动");
                        return;
                    }
                    cacheUserProfile(context, profile);
                    SpUtils.put(context, "phoneNum", phoneNum);
                    finishAuthentication(
                            context,
                            jsonResponse.optString("message", "登录成功"),
                            true
                    );
                }
            } catch (Exception exception) {
                Log.e(TAG, "authenticate failed", exception);
                showAuthenticationFailure(context, "网络异常，请检查网络后重试");
            }
        }, "account-auth").start();
    }

    /** Refreshes both account metadata and orders when an existing installation starts. */
    public static void syncAccountOnLaunch(Context context, String phoneNum, @Nullable SyncCallback callback) {
        String normalizedPhone = phoneNum == null ? "" : phoneNum.trim();
        if (normalizedPhone.isEmpty()) {
            dispatchFailure(context, callback, "本机没有登录信息");
            return;
        }
        new Thread(() -> {
            try {
                JSONObject profile = fetchUserProfile(normalizedPhone);
                if (profile == null) {
                    dispatchFailure(context, callback, "账号信息同步失败，已保留本机数据");
                    return;
                }
                cacheUserProfile(context, profile);
                boolean orderSyncSucceeded = syncOrders(context, normalizedPhone);
                if (orderSyncSucceeded) {
                    dispatchSuccess(context, callback);
                } else {
                    dispatchFailure(context, callback, "账单同步失败，已保留本机数据");
                }
            } catch (Exception exception) {
                Log.e(TAG, "syncAccountOnLaunch failed", exception);
                dispatchFailure(context, callback, "云端同步失败，已保留本机数据");
            }
        }, "account-launch-sync").start();
    }

    public static void syncUserProfile(Context context, String phoneNum) {
        syncUserProfile(context, phoneNum, null);
    }

    public static void syncUserProfile(Context context, String phoneNum, @Nullable SyncCallback callback) {
        String normalizedPhone = phoneNum == null ? "" : phoneNum.trim();
        if (normalizedPhone.isEmpty()) {
            dispatchFailure(context, callback, "本机没有登录信息");
            return;
        }
        new Thread(() -> {
            try {
                JSONObject profile = fetchUserProfile(normalizedPhone);
                if (profile == null) {
                    dispatchFailure(context, callback, "个人资料同步失败");
                    return;
                }
                cacheUserProfile(context, profile);
                dispatchSuccess(context, callback);
            } catch (Exception exception) {
                Log.e(TAG, "syncUserProfile failed", exception);
                dispatchFailure(context, callback, "个人资料同步失败");
            }
        }, "profile-sync").start();
    }

    @Nullable
    private static JSONObject fetchUserProfile(String phoneNum) throws Exception {
        Request request = new Request.Builder().url(IP + "/user/getUser/" + phoneNum).get().build();
        try (Response response = CLIENT.newCall(request).execute()) {
            String responseText = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful() || responseText.isEmpty()) {
                return null;
            }
            JSONObject jsonResponse = new JSONObject(responseText);
            if (!jsonResponse.optBoolean("success", false)) {
                return null;
            }
            return jsonResponse.optJSONObject("data");
        }
    }

    private static void cacheUserProfile(Context context, @Nullable JSONObject userData) {
        if (userData == null) {
            return;
        }
        String phoneNum = safeStringMulti(userData, "phoneNum", "phone_num");
        if (!phoneNum.isEmpty()) {
            SpUtils.put(context, "phoneNum", phoneNum);
        }
        SpUtils.put(context, "nickName", safeStringMulti(userData, "nickname", "nickName"));
        SpUtils.put(context, "familyId", safeStringMulti(userData, "familyId", "family_id"));
        SpUtils.put(context, "familyIdentity", safeStringMulti(userData, "familyIdentity", "family_identity"));
        SpUtils.put(context, "portrait", safeStringMulti(userData, "portrait"));
        com.beta.autobookkeeping.widget.OrderWidget.refreshAll(context.getApplicationContext());
        JSONObject settings = userData.optJSONObject("settings");
        JSONObject xiaohebao = settings == null ? null : settings.optJSONObject("xiaohebao");
        if (xiaohebao != null) {
            String nickname = xiaohebao.optString("nickname", "").trim();
            SpUtils.put(context, "xiaohebao_nickname", nickname);
            SpUtils.put(context, "is_alipay_xiaohebao",
                    xiaohebao.optBoolean("enabled", false) ? nickname : "");
        }
    }

    private static String safeStringMulti(JSONObject jsonObject, String... keys) {
        if (jsonObject == null || keys == null) {
            return "";
        }
        for (String key : keys) {
            if (key == null || jsonObject.isNull(key)) {
                continue;
            }
            String value = jsonObject.optString(key, "");
            if (value != null && !value.trim().isEmpty() && !"null".equalsIgnoreCase(value.trim())) {
                return value;
            }
        }
        return "";
    }

    /** Fetch first, then replace in one transaction so a failed request never clears local bills. */
    private static boolean syncOrders(Context context, String phoneNum) {
        SQLiteDatabase database = null;
        boolean transactionStarted = false;
        try {
            Request request = new Request.Builder().url(IP + "/getOrderByPhoneNum/" + phoneNum).get().build();
            JSONArray orders;
            try (Response response = CLIENT.newCall(request).execute()) {
                String responseText = response.body() == null ? "" : response.body().string();
                if (!response.isSuccessful() || responseText.isEmpty()) {
                    return false;
                }
                JSONObject jsonResponse = new JSONObject(responseText);
                if (!jsonResponse.optBoolean("success", false)) {
                    return false;
                }
                orders = jsonResponse.optJSONArray("data");
                if (orders == null) {
                    return false;
                }
            }

            database = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir() + "/orderInfo.db", null);
            database.execSQL("create table if not exists orderInfo(id int(8),year int(4),month int(2),day int(2),clock varchar(20),money numeric(10,2),bankName varchar(255),orderRemark varchar(255),costType varchar(255),userId varchar(255))");
            database.beginTransaction();
            transactionStarted = true;
            database.delete("orderInfo", null, null);
            for (int index = 0; index < orders.length(); index++) {
                JSONObject order = orders.getJSONObject(index);
                database.execSQL(
                        "insert into orderInfo values(?,?,?,?,?,?,?,?,?,?)",
                        new Object[]{
                                order.optInt("id"),
                                order.optInt("year"),
                                order.optInt("month"),
                                order.optInt("day"),
                                order.optString("clock", ""),
                                order.optDouble("money"),
                                order.optString("bankName", ""),
                                order.optString("orderRemark", ""),
                                order.optString("costType", ""),
                                safeStringMulti(order, "userId", "phoneNum", "phone_num")
                        }
                );
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception exception) {
            Log.e(TAG, "syncOrders failed", exception);
            return false;
        } finally {
            if (database != null) {
                if (transactionStarted) {
                    try {
                        database.endTransaction();
                    } catch (Exception ignored) {
                    }
                }
                try {
                    database.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void finishAuthentication(Context context, String successMessage, boolean orderSyncSucceeded) {
        if (!(context instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) context;
        activity.runOnUiThread(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;
            StyledDialog.dismissLoading(activity);
            ProjectUtil.toastMsg(
                    context,
                    orderSyncSucceeded ? successMessage : successMessage + "，但账单同步失败，请稍后重试"
            );
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).onAuthenticationSuccess();
            }
            FamilyChecking.checkFamily(context);
        });
    }

    private static void showAuthenticationFailure(Context context, String message) {
        if (!(context instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) context;
        activity.runOnUiThread(() -> {
            StyledDialog.dismissLoading(activity);
            ProjectUtil.toastMsg(context, message);
        });
    }

    private static void dispatchSuccess(Context context, @Nullable SyncCallback callback) {
        if (callback == null) {
            return;
        }
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.runOnUiThread(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed()) callback.onSuccess();
            });
        } else {
            callback.onSuccess();
        }
    }

    private static void dispatchFailure(Context context, @Nullable SyncCallback callback, String message) {
        if (callback == null) {
            return;
        }
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.runOnUiThread(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed()) callback.onFailure(message);
            });
        } else {
            callback.onFailure(message);
        }
    }
}
