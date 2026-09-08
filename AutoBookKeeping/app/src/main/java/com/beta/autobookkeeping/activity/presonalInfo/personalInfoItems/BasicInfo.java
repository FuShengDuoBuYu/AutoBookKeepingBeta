package com.beta.autobookkeeping.activity.presonalInfo.personalInfoItems;

import static Util.ConstVariable.IP;
import static Util.ImageUtil.base642bitmap;
import static Util.ImageUtil.bitmap2Base64;
import static Util.ImageUtil.getCircleBitmap;
import static Util.ImageUtil.scaleDown;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beta.autobookkeeping.R;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

import Util.ProjectUtil;
import Util.SpUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BasicInfo {
    public static final int REQUEST_PORTRAIT = 4102;
    private static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final String phoneNum;
    private final String nickname;
    private final Context context;
    private final Activity activity;
    private ImageView portraitView;
    private TextView nicknameView;

    public BasicInfo(String phoneNum, String nickname, Context context) {
        this.phoneNum = phoneNum;
        this.nickname = nickname;
        this.context = context;
        this.activity = (Activity) context;
    }

    public LinearLayout getLayoutView() {
        LinearLayout layout = (LinearLayout) LinearLayout.inflate(context, R.layout.item_activity_personal_info_basic_info, null);
        TextView phoneView = layout.findViewById(R.id.user_phone_num);
        nicknameView = layout.findViewById(R.id.user_nickname);
        LinearLayout nicknameRow = layout.findViewById(R.id.ll_nick_name);
        LinearLayout portraitRow = layout.findViewById(R.id.ll_portrait);
        portraitView = layout.findViewById(R.id.iv_portrait);

        showPortrait((String) SpUtils.get(context, "portrait", ""));
        phoneView.setText(phoneNum);
        nicknameView.setText(nickname);

        nicknameRow.setOnClickListener(view -> StyledDialog.buildNormalInput(
                "修改昵称",
                "请输入昵称",
                null,
                "确定",
                "取消",
                new MyDialogListener() {
                    private String inputNickname = "";

                    @Override
                    public void onFirst() {
                        modifyNickname(inputNickname);
                    }

                    @Override
                    public void onSecond() {
                    }

                    @Override
                    public void onGetInput(CharSequence input1, CharSequence input2) {
                        inputNickname = input1 == null ? "" : input1.toString().trim();
                        super.onGetInput(input1, input2);
                    }
                }).show());

        portraitRow.setOnClickListener(view -> StyledDialog.buildIosAlert(
                "选择头像",
                "选择并裁剪图片后，应用会自动压缩再上传。",
                new MyDialogListener() {
                    @Override
                    public void onFirst() {
                        Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        picker.addCategory(Intent.CATEGORY_OPENABLE);
                        picker.setType("image/*");
                        picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        try {
                            activity.startActivityForResult(picker, REQUEST_PORTRAIT);
                        } catch (ActivityNotFoundException exception) {
                            ProjectUtil.toastMsg(context, "未找到系统图片选择器，请启用系统文件应用");
                        }
                    }

                    @Override
                    public void onSecond() {
                    }
                }).setBtnText("选择图片", "取消").show());
        return layout;
    }

    private void showPortrait(String portrait) {
        Bitmap portraitBitmap = portrait == null || portrait.isEmpty() ? null : base642bitmap(portrait);
        if (portraitBitmap == null) {
            portraitView.setImageDrawable(context.getDrawable(R.drawable.ic_portrait));
        } else {
            portraitView.setImageDrawable(new BitmapDrawable(context.getResources(), portraitBitmap));
        }
    }

    public void startUcrop(Uri uri) {
        Uri destinationUri = Uri.fromFile(new File(activity.getFilesDir(), "portrait.jpg"));
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setAllowedGestures(UCropActivity.ALL, UCropActivity.ALL, UCropActivity.ALL);
        options.setCircleDimmedLayer(true);
        options.setShowCropFrame(false);
        options.setShowCropGrid(false);
        options.setCompressionQuality(80);
        UCrop.of(uri, destinationUri)
                .withOptions(options)
                .withAspectRatio(1, 1)
                .start(activity);
    }

    public void modifyPortrait(Bitmap bitmap) {
        if (bitmap == null) {
            ProjectUtil.toastMsg(context, "头像读取失败，请重新选择");
            return;
        }
        StyledDialog.buildLoading().show();
        Bitmap resizedBitmap = scaleDown(bitmap, 256);
        Bitmap circularBitmap = getCircleBitmap(resizedBitmap);
        String base64Image = bitmap2Base64(circularBitmap);
        if (base64Image.length() > 500000) {
            circularBitmap = getCircleBitmap(scaleDown(bitmap, 160));
            base64Image = bitmap2Base64(circularBitmap);
        }
        Bitmap finalBitmap = circularBitmap;
        String finalBase64Image = base64Image;

        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("phoneNum", phoneNum);
                payload.put("newPortrait", finalBase64Image);
                Request request = new Request.Builder()
                        .url(IP + "/user/modifyPortrait")
                        .put(RequestBody.create(payload.toString(), JSON))
                        .build();
                try (Response response = CLIENT.newCall(request).execute()) {
                    String responseText = response.body() == null ? "" : response.body().string();
                    if (!response.isSuccessful() || responseText.isEmpty()) {
                        finishWithError("头像上传失败，请稍后重试");
                        return;
                    }
                    JSONObject jsonResponse = new JSONObject(responseText);
                    if (!jsonResponse.optBoolean("success", false)) {
                        finishWithError(jsonResponse.optString("message", "头像上传失败"));
                        return;
                    }
                    activity.runOnUiThread(() -> {
                        SpUtils.put(context, "portrait", finalBase64Image);
                        portraitView.setImageDrawable(new BitmapDrawable(context.getResources(), finalBitmap));
                        StyledDialog.dismissLoading(activity);
                        ProjectUtil.toastMsg(context, "头像更换成功");
                    });
                }
            } catch (Exception exception) {
                finishWithError("网络异常，请检查网络后重试");
            }
        }, "portrait-upload").start();
    }

    private void modifyNickname(String rawNickname) {
        String newNickname = rawNickname == null ? "" : rawNickname.trim();
        if (newNickname.isEmpty()) {
            ProjectUtil.toastMsg(context, "昵称不能为空");
            return;
        }
        if (newNickname.length() > 30) {
            ProjectUtil.toastMsg(context, "昵称不能超过30个字符");
            return;
        }
        StyledDialog.buildLoading().show();
        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("phoneNum", phoneNum);
                payload.put("newNickname", newNickname);
                Request request = new Request.Builder()
                        .url(IP + "/user/modifyNickname")
                        .put(RequestBody.create(payload.toString(), JSON))
                        .build();
                try (Response response = CLIENT.newCall(request).execute()) {
                    String responseText = response.body() == null ? "" : response.body().string();
                    if (!response.isSuccessful() || responseText.isEmpty()) {
                        finishWithError("昵称修改失败，请稍后重试");
                        return;
                    }
                    JSONObject jsonResponse = new JSONObject(responseText);
                    if (!jsonResponse.optBoolean("success", false)) {
                        finishWithError(jsonResponse.optString("message", "昵称修改失败"));
                        return;
                    }
                    activity.runOnUiThread(() -> {
                        SpUtils.put(context, "nickName", newNickname);
                        nicknameView.setText(newNickname);
                        StyledDialog.dismissLoading(activity);
                        ProjectUtil.toastMsg(context, "昵称修改成功");
                    });
                }
            } catch (Exception exception) {
                finishWithError("网络异常，请检查网络后重试");
            }
        }, "nickname-update").start();
    }

    private void finishWithError(String message) {
        activity.runOnUiThread(() -> {
            StyledDialog.dismissLoading(activity);
            ProjectUtil.toastMsg(context, message);
        });
    }
}
