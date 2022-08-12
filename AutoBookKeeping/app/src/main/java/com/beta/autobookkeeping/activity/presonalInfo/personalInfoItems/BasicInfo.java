package com.beta.autobookkeeping.activity.presonalInfo.personalInfoItems;

import static Util.ConstVariable.IP;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beta.autobookkeeping.R;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;

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

public class BasicInfo {
    private String phoneNum;
    private String nickname;
    private Context context;
    private TextView tv_nickName,tv_phoneNum;
    private LinearLayout ll_nickName;
    private Activity activity;

    public BasicInfo(String phoneNum, String nickname,Context context) {
        this.phoneNum = phoneNum;
        this.nickname = nickname;
        this.context = context;
        activity = (Activity) context;
    }

    //返回view用于添加
    public LinearLayout getLayoutView(){
        LinearLayout basicInfo = (LinearLayout) LinearLayout.inflate(context, R.layout.item_activity_personal_info_basic_info,null);
        findViewById(basicInfo);
        //设置值
        tv_phoneNum.setText(this.phoneNum);
        tv_nickName.setText(this.nickname);
        //设置监听
        ll_nickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StyledDialog.buildNormalInput("修改昵称", "请输入昵称", null, "确定", "取消", new MyDialogListener() {
                    String nickname;
                    @Override
                    public void onFirst() {
                        modifyNickName(nickname);
                    }
                    @Override
                    public void onSecond() {}
                    @Override
                    public void onGetInput(CharSequence input1, CharSequence input2) {
                        nickname = input1.toString();
                        super.onGetInput(input1, input2);
                    }
                }).show();
            }
        });
        return basicInfo;
    }

    private void modifyNickName(String newNickName){
        StyledDialog.buildLoading().show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/user/modifyNickname";
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("phoneNum",phoneNum);
                    jsonObject.put("newNickname",newNickName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));
                Request request = new Request.Builder().url(url).put(body).build();
                try{
                    Response response = client.newCall(request).execute();
                    if(response.code()==200){
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        if(jsonResponse.getBoolean("success")){
                            refreshNickname(newNickName);
                        }
                        else{
                            Looper.prepare();
                            ProjectUtil.toastMsg(context,jsonResponse.getString("message"));
                            Looper.loop();
                        }
                    }
                    else{
                        Looper.prepare();
                        ProjectUtil.toastMsg(context,"服务器出错");
                        Looper.loop();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void refreshNickname(String newNickname){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpUtils.put(context,"nickName",newNickname);
                tv_nickName.setText(newNickname);
                StyledDialog.dismissLoading(activity);
                ProjectUtil.toastMsg(context,"修改成功");
            }
        });
    }

    private void findViewById(LinearLayout l){
        tv_phoneNum = l.findViewById(R.id.user_phone_num);
        tv_nickName = l.findViewById(R.id.user_nickname);
        ll_nickName = l.findViewById(R.id.ll_nick_name);
    }
}
