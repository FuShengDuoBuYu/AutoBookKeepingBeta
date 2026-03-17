package com.beta.autobookkeeping.activity.main.checking;

import static Util.ConstVariable.IP;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.Nullable;

import com.hss01248.dialog.StyledDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;

import Util.ProjectUtil;
import Util.SpUtils;
import com.beta.autobookkeeping.activity.main.MainActivity;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserRegister {
    public static void userRegister(String phoneNum, String password, Context context){
        StyledDialog.buildLoading().show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/addUser";
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("phoneNum",phoneNum);
                    jsonObject.put("password",password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                try(Response response = client.newCall(request).execute()){
                    if(response.code()==200 && response.body() != null){
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        if(jsonResponse.getBoolean("success")){
                            cacheUserProfile(context, jsonResponse.optJSONObject("data"));
                            syncOrdersOnLogin(context, phoneNum);
                            handleAfterAddUser(context,phoneNum,jsonResponse.optString("message", "登录成功"));
                        }
                        else{
                            dismissLoadingAndToast(context, jsonResponse.optString("message", "登录失败"));
                        }
                    }
                    else{
                        Log.d("tag",String.valueOf(response.code()));
                        dismissLoadingAndToast(context,"服务器出错");
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    dismissLoadingAndToast(context,"网络异常,请重试");
                }
            }
        }).start();
    }

    private static void dismissLoadingAndToast(Context context, String message){
        if(!(context instanceof Activity)){
            return;
        }
        Activity activity = (Activity) context;
        activity.runOnUiThread(() -> {
            StyledDialog.dismissLoading(activity);
            ProjectUtil.toastMsg(context, message);
        });
    }

    public static void syncUserProfile(Context context, String phoneNum){
        syncUserProfile(context, phoneNum, null);
    }

    public static void syncUserProfile(Context context, String phoneNum, @Nullable Runnable onSuccess){
        if(phoneNum == null || phoneNum.trim().isEmpty()){
            return;
        }
        new Thread(() -> {
            String url = IP+"/user/getUser/"+phoneNum;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = client.newCall(request).execute()) {
                if(response.code() == 200 && response.body() != null){
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    if(jsonResponse.getBoolean("success")){
                        cacheUserProfile(context, jsonResponse.optJSONObject("data"));
                        syncOrdersOnLogin(context, phoneNum);
                        if(onSuccess != null && context instanceof Activity){
                            ((Activity) context).runOnUiThread(onSuccess);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private static String safeString(JSONObject jsonObject, String key){
        if(jsonObject == null || jsonObject.isNull(key)){
            return "";
        }
        return jsonObject.optString(key, "");
    }

    private static String safeStringMulti(JSONObject jsonObject, String... keys){
        if(jsonObject == null || keys == null){
            return "";
        }
        for(String key : keys){
            if(key != null && !jsonObject.isNull(key)){
                String value = jsonObject.optString(key, "");
                if(value != null && !value.trim().isEmpty() && !"null".equalsIgnoreCase(value.trim())){
                    return value;
                }
            }
        }
        return "";
    }

    private static void cacheUserProfile(Context context, JSONObject userData){
        if(userData == null){
            return;
        }
        SpUtils.put(context, "phoneNum", safeStringMulti(userData, "phoneNum", "phone_num"));
        SpUtils.put(context, "nickName", safeStringMulti(userData, "nickname", "nickName"));
        SpUtils.put(context, "familyId", safeStringMulti(userData, "familyId", "family_id"));
        SpUtils.put(context, "portrait", safeString(userData, "portrait"));
    }

    private static void syncOrdersOnLogin(Context context, String phoneNum){
        new Thread(() -> {
            SQLiteDatabase db = null;
            boolean transactionStarted = false;
            try {
                String url = IP + "/getOrderByPhoneNum/" + phoneNum;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).get().build();
                try (Response response = client.newCall(request).execute()) {
                    if(response.code() != 200 || response.body() == null){
                        return;
                    }

                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    if(!jsonResponse.getBoolean("success")){
                        return;
                    }

                    JSONArray orderArray = jsonResponse.getJSONArray("data");
                    db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
                    db.beginTransaction();
                    transactionStarted = true;
                    db.execSQL("delete from orderInfo");
                    for(int i = 0; i < orderArray.length(); i++){
                        JSONObject orderInfo = orderArray.getJSONObject(i);
                        db.execSQL(
                                "insert into orderInfo values(?,?,?,?,?,?,?,?,?,?)",
                                new Object[]{
                                        orderInfo.getInt("id"),
                                        orderInfo.getInt("year"),
                                        orderInfo.getInt("month"),
                                        orderInfo.getInt("day"),
                                        orderInfo.getString("clock"),
                                        orderInfo.getDouble("money"),
                                        orderInfo.getString("bankName"),
                                        orderInfo.getString("orderRemark"),
                                        orderInfo.getString("costType"),
                                        orderInfo.optString("userId", orderInfo.optString("phoneNum", ""))
                                }
                        );
                    }
                    db.setTransactionSuccessful();
                    notifyOrderSyncFinished(context);
                }
            } catch (Exception e) {
                Log.e("UserRegister", "syncOrdersOnLogin failed", e);
            } finally {
                if(db != null){
                    if(transactionStarted){
                        try {
                            db.endTransaction();
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        db.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }).start();
    }

    private static void notifyOrderSyncFinished(Context context){
        if(!(context instanceof Activity)){
            return;
        }
        Activity activity = (Activity) context;
        activity.runOnUiThread(() -> {
            if(activity instanceof MainActivity){
                ((MainActivity) activity).refreshAfterCloudOrderSync();
            }
        });
    }

    public static void handleAfterAddUser(Context context,String phoneNum, String successMessage){
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpUtils.put(context,"phoneNum",phoneNum);
                StyledDialog.dismissLoading(activity);
                ProjectUtil.toastMsg(context,successMessage);
                FamilyChecking.checkFamily(context);
            }
        });
    }
}
