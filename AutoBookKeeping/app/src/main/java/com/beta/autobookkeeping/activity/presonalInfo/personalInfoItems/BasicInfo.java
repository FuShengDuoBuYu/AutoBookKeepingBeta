package com.beta.autobookkeeping.activity.presonalInfo.personalInfoItems;

import static Util.ConstVariable.IP;
import static Util.ImageUtil.base642bitmap;
import static Util.ImageUtil.bitmap2Base64;
import static Util.ImageUtil.getCircleBitmap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.beta.autobookkeeping.R;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.engine.CropEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

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
    private ImageView iv_portrait;
    private TextView tv_nickName,tv_phoneNum;
    private LinearLayout ll_nickName,ll_portrait;
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
        //设置头像
        if(SpUtils.get(context,"portrait","")==null||"".equals(SpUtils.get(context,"portrait",""))){
            iv_portrait.setBackground(context.getDrawable(R.drawable.ic_portrait));
        }
        else{
            iv_portrait.setBackground(new BitmapDrawable(base642bitmap((String) SpUtils.get(context,"portrait",""))));
        }
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
        ll_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StyledDialog.buildIosAlert("选择图片", "请将图片压缩分辨率或其他方法控制在5KB左右,否则会非常非常卡\n(开发者这里偷懒了)", new MyDialogListener() {
                    @Override
                    public void onFirst() {
                        PictureSelector.create(context)
                        .openSystemGallery(SelectMimeType.ofImage())
                        .forSystemResult(new OnResultCallbackListener<LocalMedia>() {
                            @Override
                            public void onResult(ArrayList<LocalMedia> result) {
                                startUcrop(Uri.parse(result.get(0).getPath()));
                            }
                            @Override
                            public void onCancel() {

                            }
                        });
                    }

                    @Override
                    public void onSecond() {

                    }
                }).setBtnText("我知道啦","取消").show();
            }
        });
        return basicInfo;
    }

    //裁剪图片
    private void startUcrop(Uri uri){
        Uri destinationUri = Uri.fromFile(new File(activity.getFilesDir(),"portrait.png"));
        UCrop uCrop = UCrop.of(uri,destinationUri);
        //裁剪设置
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        //只允许缩放,不允许旋转等
        options.setAllowedGestures(UCropActivity.ALL,UCropActivity.ALL,UCropActivity.ALL);
        options.setCircleDimmedLayer(true);
        options.setShowCropFrame(false);
        options.setShowCropGrid(false);
        options.setCompressionQuality(10);
        uCrop.withOptions(options).withAspectRatio(1,1);
        uCrop.start(activity);
    }

    //收取裁剪后的uri
    public void modifyPortrait(Bitmap bitmap){
        StyledDialog.buildLoading().show();
        Bitmap newPortraitBitmap = getCircleBitmap(bitmap);
        BitmapDrawable newPortrait = new BitmapDrawable(newPortraitBitmap);
        String base64Image = bitmap2Base64(newPortraitBitmap);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/user/modifyPortrait";
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("phoneNum",phoneNum);
                    jsonObject.put("newPortrait",base64Image);
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
                            refreshPortrait(newPortrait,base64Image);
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

    private void refreshPortrait(BitmapDrawable newPortrait,String newPortraitString){
        SpUtils.put(context,"portrait",newPortraitString);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StyledDialog.dismissLoading(activity);
                iv_portrait.setBackground(null);
                iv_portrait.setBackground(newPortrait);
                ProjectUtil.toastMsg(context,"更换头像成功");
            }
        });
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
        ll_portrait = l.findViewById(R.id.ll_portrait);
        iv_portrait = l.findViewById(R.id.iv_portrait);
    }
}
