package com.beta.autobookkeeping.activity.presonalInfo;

import static com.hss01248.dialog.StyledDialog.context;
import static Util.ConstVariable.IP;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.presonalInfo.personalInfoItems.BasicInfo;
import com.beta.autobookkeeping.activity.presonalInfo.personalInfoItems.FamilyInfo;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Util.ProjectUtil;
import Util.SpUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PersonlInfoActivity extends AppCompatActivity {

    private TextView phoneNum,nickname;
    private LinearLayout familyDetail,llContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personl_info);
        findViewByIdAndInit();
        //查找个人基本家庭信息
        getFamilyInfo();
    }

    private void findViewByIdAndInit(){
        llContainer = findViewById(R.id.ll_container);
        BasicInfo basicInfo = new BasicInfo((String) SpUtils.get(this,"phoneNum",""),(SpUtils.get(this,"nickName","")==null||SpUtils.get(this,"nickName","").equals(""))?"暂未设置":(String) SpUtils.get(this,"nickName",""),this);
        LinearLayout basicInfoLayoutView = basicInfo.getLayoutView();
        llContainer.addView(basicInfoLayoutView);
    }

    private void getFamilyInfo(){
        if(SpUtils.get(PersonlInfoActivity.this,"familyId","")==null || "".equals(SpUtils.get(PersonlInfoActivity.this,"familyId",""))){
            afterGetFamilyMembers(new JSONArray());
        }
        else {
            StyledDialog.buildLoading().show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String familyId = (String) SpUtils.get(PersonlInfoActivity.this,"familyId","");
                    String url = IP+"/user/getFamilyMembers/"+familyId;
                    OkHttpClient client = new OkHttpClient();
                    JSONObject jsonObject = new JSONObject();
                    Request request = new Request.Builder().url(url).get().build();
                    try{
                        Response response = client.newCall(request).execute();
                        if(response.code()==200){
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            if(jsonResponse.getBoolean("success")){
                                JSONArray familyMembers = jsonResponse.getJSONArray("data");
                                afterGetFamilyMembers(familyMembers);
                            }
                            else{
                                Looper.prepare();
                                StyledDialog.dismissLoading(PersonlInfoActivity.this);
                                ProjectUtil.toastMsg(context,jsonResponse.getString("message"));
                                Looper.loop();
                            }
                        }
                        else{
                            Looper.prepare();
                            StyledDialog.dismissLoading(PersonlInfoActivity.this);
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
    }

    private void afterGetFamilyMembers(JSONArray familyMembers){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StyledDialog.dismissLoading(PersonlInfoActivity.this);
                FamilyInfo familyInfo = new FamilyInfo(PersonlInfoActivity.this,familyMembers);
                llContainer.addView(familyInfo.getLayoutView());
            }
        });
    }

}