package com.beta.autobookkeeping.activity.presonalInfo.personalInfoItems;

import static Util.ConstVariable.IP;
import static Util.ImageUtil.base642bitmap;
import static Util.ImageUtil.getCircleBitmap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beta.autobookkeeping.R;
import com.hss01248.dialog.DialogAssigner;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.config.ConfigBean;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.qmuiteam.qmui.widget.tab.QMUIBasicTabSegment;
import com.qmuiteam.qmui.widget.tab.QMUIBasicTabSegment.Mode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import Util.ProjectUtil;
import Util.SpUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FamilyInfo {
    private Context context;
    private Activity activity;
    private JSONArray familyMembers;
    public FamilyInfo(Context context,JSONArray familyMembers) {
        this.context = context;
        this.familyMembers = familyMembers;
        this.activity = (Activity) context;
    }



    public LinearLayout getLayoutView(){
        if(this.familyMembers.length()==0){
            return getUnLinkedFamilyView();
        }
        else {
            return getFamilyMembers();
        }
    }

    private LinearLayout getUnLinkedFamilyView(){
        LinearLayout linearLayout = (LinearLayout) LinearLayout.inflate(context, R.layout.item_activity_personal_info_add_family,null);
        QMUIRoundButton btnAddFamily = linearLayout.findViewById(R.id.btn_add_family);
        QMUIRoundButton btnCreateFamily = linearLayout.findViewById(R.id.btn_create_family);
        btnAddFamily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StyledDialog.buildNormalInput("添加家庭", "请输入8位家庭ID", "请输入您的家庭身份", "确定", "取消", new MyDialogListener() {
                    String familyId;
                    String familyIdentity;
                    @Override
                    public void onFirst() {
                        addFamily(familyId,familyIdentity);
                    }
                    @Override
                    public void onSecond() {}
                    @Override
                    public void onGetInput(CharSequence input1, CharSequence input2) {
                        familyId = input1.toString();
                        familyIdentity = input2.toString();
                        super.onGetInput(input1, input2);
                    }
                }).setInput2HideAsPassword(false).show();
            }
        });
        btnCreateFamily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StyledDialog.buildNormalInput("创建家庭\n如有家庭成员已经创建家庭,请点击添加家庭输入id后加入家庭", "请输入您的家庭身份",null,"确定","取消", new MyDialogListener() {
                    private String familyIdentity;
                    @Override
                    public void onFirst() {
                        createFamily(familyIdentity);
                    }
                    @Override
                    public void onSecond() {}

                    @Override
                    public void onGetInput(CharSequence input1, CharSequence input2) {
                        familyIdentity = input1.toString();
                        super.onGetInput(input1, input2);
                    }
                }).setInput2HideAsPassword(false).show();
            }
        });
        return linearLayout;
    }

    private void addFamily(String familyId, String familyIdentity){
        StyledDialog.buildLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/user/addFamily";
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("familyId",familyId);
                    jsonObject.put("familyIdentity",familyIdentity);
                    jsonObject.put("phoneNum",SpUtils.get(context,"phoneNum",""));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));
                Request request = new Request.Builder().url(url).post(body).build();
                try{
                    Response response = client.newCall(request).execute();
                    if(response.code()==200){
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        if(jsonResponse.getBoolean("success")){
                            afterAddFamily(familyId);
                        }
                        else{
                            Looper.prepare();
                            StyledDialog.dismissLoading(activity);
                            ProjectUtil.toastMsg(context,jsonResponse.getString("message"));
                            Looper.loop();
                        }
                    }
                    else{
                        Looper.prepare();
                        StyledDialog.dismissLoading(activity);
                        ProjectUtil.toastMsg(context,"服务器出错");
                        Looper.loop();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void afterAddFamily(String familyId){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpUtils.put(context,"familyId",familyId);
                ProjectUtil.toastMsg(context,"加入家庭成功");
                StyledDialog.dismissLoading(activity);
            }
        });
    }

    private void createFamily(String familyIdentity){
        StyledDialog.buildLoading().show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/user/createFamily";
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("phoneNum",SpUtils.get(context,"phoneNum",""));
                    jsonObject.put("familyIdentity",familyIdentity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));
                Request request = new Request.Builder().url(url).post(body).build();
                try{
                    Response response = client.newCall(request).execute();
                    if(response.code()==200){
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        Log.d("tag",jsonResponse.toString());
                        if(jsonResponse.getBoolean("success")){
                            afterCreateFamily((String)jsonResponse.get("data"));
                        }
                        else{
                            Looper.prepare();
                            StyledDialog.dismissLoading(activity);
                            ProjectUtil.toastMsg(context,jsonResponse.getString("message"));
                            Looper.loop();
                        }
                    }
                    else{
                        Looper.prepare();
                        StyledDialog.dismissLoading(activity);
                        ProjectUtil.toastMsg(context,"服务器出错");
                        Looper.loop();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void afterCreateFamily(String familyId){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpUtils.put(context,"familyId",familyId);
                ProjectUtil.toastMsg(context,"创建成功");
                StyledDialog.dismissLoading(activity);
            }
        });
    }

    private LinearLayout getFamilyMembers(){
        LinearLayout linearLayout = (LinearLayout) LinearLayout.inflate(context,R.layout.item_activity_personal_info_member_family,null);
        TextView tvFamilyId = linearLayout.findViewById(R.id.tv_family_id);
        tvFamilyId.setText("我的家庭id是 "+SpUtils.get(context,"familyId",""));
        for (int i = 0; i < familyMembers.length(); i++) {
            LinearLayout linearLayoutItem = (LinearLayout) LinearLayout.inflate(context,R.layout.item_activity_personal_info_member_family_item,null);
            ImageView ivFamilyMemberPortrait = linearLayoutItem.findViewById(R.id.iv_family_member_portrait);
            TextView tvFamilyMemberIdentityAndNickname = linearLayoutItem.findViewById(R.id.tv_family_member_identity_and_nickname);
            TextView tvFamilyMemberPhoneNum = linearLayoutItem.findViewById(R.id.tv_family_member_phoneNum);
            //设置原始头像/自定义头像
            try {
                if(familyMembers.getJSONObject(i).getString("portrait")==null||"".equals(familyMembers.getJSONObject(i).getString("portrait"))){
                    ivFamilyMemberPortrait.setBackground(context.getDrawable(R.drawable.ic_portrait));
                }
                else{
                    ivFamilyMemberPortrait.setBackground(new BitmapDrawable(base642bitmap(familyMembers.getJSONObject(i).getString("portrait"))));
                }
                tvFamilyMemberIdentityAndNickname.setText(familyMembers.getJSONObject(i).getString("familyIdentity")+"·"+familyMembers.getJSONObject(i).getString("nickname"));
                tvFamilyMemberPhoneNum.setText(familyMembers.getJSONObject(i).getString("phoneNum"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            linearLayout.addView(linearLayoutItem);
        }
        return linearLayout;
    }
}
