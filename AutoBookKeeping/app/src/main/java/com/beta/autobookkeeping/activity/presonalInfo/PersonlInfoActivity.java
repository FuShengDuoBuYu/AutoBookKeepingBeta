package com.beta.autobookkeeping.activity.presonalInfo;

import static Util.ConstVariable.IP;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.main.checking.UserRegister;
import com.beta.autobookkeeping.activity.presonalInfo.personalInfoItems.BasicInfo;
import com.beta.autobookkeeping.activity.presonalInfo.personalInfoItems.FamilyInfo;
import com.hss01248.dialog.StyledDialog;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import Util.ProjectUtil;
import Util.SpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PersonlInfoActivity extends AppCompatActivity {
    private static final String TAG = "PersonlInfoActivity";
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private BasicInfo basicInfo;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personl_info);
        container = findViewById(R.id.ll_container);
        loadAccountInfo();
    }

    /** Always refresh the server profile first so a new device does not use stale family/avatar data. */
    private void loadAccountInfo() {
        StyledDialog.buildLoading().show();
        String phoneNum = (String) SpUtils.get(this, "phoneNum", "");
        UserRegister.syncUserProfile(this, phoneNum, new UserRegister.SyncCallback() {
            @Override
            public void onSuccess() {
                renderBasicInfo();
                loadFamilyMembers();
            }

            @Override
            public void onFailure(String message) {
                ProjectUtil.toastMsg(PersonlInfoActivity.this, message + "，当前显示本机缓存");
                renderBasicInfo();
                loadFamilyMembers();
            }
        });
    }

    private void renderBasicInfo() {
        container.removeAllViews();
        String phoneNum = (String) SpUtils.get(this, "phoneNum", "");
        String nickname = (String) SpUtils.get(this, "nickName", "");
        basicInfo = new BasicInfo(phoneNum, nickname == null || nickname.trim().isEmpty() ? "暂未设置" : nickname, this);
        container.addView(basicInfo.getLayoutView());
    }

    private void loadFamilyMembers() {
        String familyId = (String) SpUtils.get(this, "familyId", "");
        if (familyId == null || familyId.trim().isEmpty()) {
            showFamilyMembers(new JSONArray());
            return;
        }

        String normalizedFamilyId = familyId.trim();
        new Thread(() -> {
            Request request = new Request.Builder()
                    .url(IP + "/user/getFamilyMembers/" + normalizedFamilyId)
                    .get()
                    .build();
            try (Response response = CLIENT.newCall(request).execute()) {
                String responseText = response.body() == null ? "" : response.body().string();
                if (!response.isSuccessful() || responseText.isEmpty()) {
                    showFamilyLoadError("家庭信息加载失败，请稍后重试");
                    return;
                }
                JSONObject jsonResponse = new JSONObject(responseText);
                if (!jsonResponse.optBoolean("success", false)) {
                    String message = jsonResponse.optString("message", "家庭信息加载失败");
                    if ("家庭不存在".equals(message)) {
                        SpUtils.put(PersonlInfoActivity.this, "familyId", "");
                        SpUtils.put(PersonlInfoActivity.this, "familyIdentity", "");
                        showFamilyMembers(new JSONArray());
                    } else {
                        showFamilyLoadError(message);
                    }
                    return;
                }
                JSONArray familyMembers = jsonResponse.optJSONArray("data");
                showFamilyMembers(familyMembers == null ? new JSONArray() : familyMembers);
            } catch (Exception exception) {
                Log.e(TAG, "loadFamilyMembers failed", exception);
                showFamilyLoadError("家庭信息加载失败，请检查网络");
            }
        }, "family-members-sync").start();
    }

    private void showFamilyMembers(JSONArray familyMembers) {
        runOnUiThread(() -> {
            StyledDialog.dismissLoading(PersonlInfoActivity.this);
            FamilyInfo familyInfo = new FamilyInfo(PersonlInfoActivity.this, familyMembers, this::loadAccountInfo);
            container.addView(familyInfo.getLayoutView());
        });
    }

    private void showFamilyLoadError(String message) {
        runOnUiThread(() -> {
            StyledDialog.dismissLoading(PersonlInfoActivity.this);
            ProjectUtil.toastMsg(PersonlInfoActivity.this, message);
            TextView errorView = new TextView(PersonlInfoActivity.this);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            errorView.setPadding(padding, padding, padding, padding);
            errorView.setText("家庭信息暂时无法加载，下次进入会自动重试");
            container.addView(errorView);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == BasicInfo.REQUEST_PORTRAIT && resultCode == RESULT_OK
                && data != null && data.getData() != null && basicInfo != null) {
            basicInfo.startUcrop(data.getData());
        }
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && data != null) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null && basicInfo != null) {
                ContentResolver contentResolver = getContentResolver();
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(resultUri));
                    if (bitmap != null) {
                        basicInfo.modifyPortrait(bitmap);
                    }
                } catch (FileNotFoundException exception) {
                    Log.e(TAG, "cropped portrait not found", exception);
                    ProjectUtil.toastMsg(this, "头像读取失败，请重新选择");
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
            Log.e(TAG, "portrait crop failed", UCrop.getError(data));
            ProjectUtil.toastMsg(this, "头像裁剪失败，请重试");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
