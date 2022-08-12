package com.beta.autobookkeeping.activity.main.checking;

import static Util.ConstVariable.IP;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.hss01248.dialog.StyledDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import Util.ProjectUtil;
import Util.SpUtils;
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
                try{
                    Response response = client.newCall(request).execute();
                    if(response.code()==200){
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        if(jsonResponse.getBoolean("success")){
                            handleAfterAddUser(context,phoneNum);
                        }
                        else{
                            Looper.prepare();
                            ProjectUtil.toastMsg(context,jsonResponse.getString("message"));
                            Looper.loop();
                        }
                    }
                    else{
                        Looper.prepare();
                        Log.d("tag",String.valueOf(response.code()));
                        ProjectUtil.toastMsg(context,"服务器出错");
                        Looper.loop();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void handleAfterAddUser(Context context,String phoneNum){
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpUtils.put(context,"phoneNum",phoneNum);
                StyledDialog.dismissLoading(activity);
                ProjectUtil.toastMsg(context,"注册成功");
            }
        });
    }
}
