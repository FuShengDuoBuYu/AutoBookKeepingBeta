package com.beta.autobookkeeping.activity.presonalInfo.personalInfoItems;

import static Util.ConstVariable.IP;
import static Util.ImageUtil.base642bitmap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beta.autobookkeeping.R;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

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

public class FamilyInfo {
    private static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final Context context;
    private final Activity activity;
    private final JSONArray familyMembers;
    private final Runnable onFamilyChanged;

    public FamilyInfo(Context context, JSONArray familyMembers, Runnable onFamilyChanged) {
        this.context = context;
        this.activity = (Activity) context;
        this.familyMembers = familyMembers == null ? new JSONArray() : familyMembers;
        this.onFamilyChanged = onFamilyChanged;
    }

    public LinearLayout getLayoutView() {
        return familyMembers.length() == 0 ? getUnlinkedFamilyView() : getFamilyMembersView();
    }

    private LinearLayout getUnlinkedFamilyView() {
        LinearLayout layout = (LinearLayout) LinearLayout.inflate(context, R.layout.item_activity_personal_info_add_family, null);
        QMUIRoundButton addFamilyButton = layout.findViewById(R.id.btn_add_family);
        QMUIRoundButton createFamilyButton = layout.findViewById(R.id.btn_create_family);

        addFamilyButton.setOnClickListener(view -> StyledDialog.buildNormalInput(
                "加入家庭",
                "请输入8位家庭ID",
                "请输入您的家庭身份",
                "确定",
                "取消",
                new MyDialogListener() {
                    private String familyId = "";
                    private String familyIdentity = "";

                    @Override
                    public void onFirst() {
                        addFamily(familyId, familyIdentity);
                    }

                    @Override
                    public void onSecond() {
                    }

                    @Override
                    public void onGetInput(CharSequence input1, CharSequence input2) {
                        familyId = input1 == null ? "" : input1.toString().trim();
                        familyIdentity = input2 == null ? "" : input2.toString().trim();
                        super.onGetInput(input1, input2);
                    }
                }).setInput2HideAsPassword(false).show());

        createFamilyButton.setOnClickListener(view -> StyledDialog.buildNormalInput(
                "创建家庭",
                "请输入您的家庭身份",
                null,
                "确定",
                "取消",
                new MyDialogListener() {
                    private String familyIdentity = "";

                    @Override
                    public void onFirst() {
                        createFamily(familyIdentity);
                    }

                    @Override
                    public void onSecond() {
                    }

                    @Override
                    public void onGetInput(CharSequence input1, CharSequence input2) {
                        familyIdentity = input1 == null ? "" : input1.toString().trim();
                        super.onGetInput(input1, input2);
                    }
                }).setInput2HideAsPassword(false).show());
        return layout;
    }

    private void addFamily(String rawFamilyId, String rawFamilyIdentity) {
        String familyId = rawFamilyId == null ? "" : rawFamilyId.trim();
        String familyIdentity = rawFamilyIdentity == null ? "" : rawFamilyIdentity.trim();
        if (!familyId.matches("^[0-9]{8}$")) {
            ProjectUtil.toastMsg(context, "请输入正确的8位家庭ID");
            return;
        }
        if (familyIdentity.isEmpty()) {
            ProjectUtil.toastMsg(context, "家庭身份不能为空");
            return;
        }
        submitFamilyRequest("/user/addFamily", familyId, familyIdentity, false);
    }

    private void createFamily(String rawFamilyIdentity) {
        String familyIdentity = rawFamilyIdentity == null ? "" : rawFamilyIdentity.trim();
        if (familyIdentity.isEmpty()) {
            ProjectUtil.toastMsg(context, "家庭身份不能为空");
            return;
        }
        submitFamilyRequest("/user/createFamily", "", familyIdentity, true);
    }

    private void submitFamilyRequest(String path, String requestedFamilyId, String familyIdentity, boolean creating) {
        StyledDialog.buildLoading().show();
        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("phoneNum", SpUtils.get(context, "phoneNum", ""));
                payload.put("familyIdentity", familyIdentity);
                if (!creating) {
                    payload.put("familyId", requestedFamilyId);
                }
                Request request = new Request.Builder()
                        .url(IP + path)
                        .post(RequestBody.create(payload.toString(), JSON))
                        .build();
                try (Response response = CLIENT.newCall(request).execute()) {
                    String responseText = response.body() == null ? "" : response.body().string();
                    if (!response.isSuccessful() || responseText.isEmpty()) {
                        finishWithError("服务器暂时不可用，请稍后重试");
                        return;
                    }
                    JSONObject jsonResponse = new JSONObject(responseText);
                    if (!jsonResponse.optBoolean("success", false)) {
                        finishWithError(jsonResponse.optString("message", "操作失败"));
                        return;
                    }
                    String familyId = creating ? jsonResponse.optString("data", "") : requestedFamilyId;
                    finishFamilyChange(familyId, familyIdentity, creating ? "家庭创建成功" : "已加入家庭");
                }
            } catch (Exception exception) {
                finishWithError("网络异常，请检查网络后重试");
            }
        }, creating ? "family-create" : "family-join").start();
    }

    private void finishFamilyChange(String familyId, String familyIdentity, String message) {
        activity.runOnUiThread(() -> {
            SpUtils.put(context, "familyId", familyId);
            SpUtils.put(context, "familyIdentity", familyIdentity);
            StyledDialog.dismissLoading(activity);
            ProjectUtil.toastMsg(context, message);
            if (onFamilyChanged != null) {
                onFamilyChanged.run();
            }
        });
    }

    private void finishWithError(String message) {
        activity.runOnUiThread(() -> {
            StyledDialog.dismissLoading(activity);
            ProjectUtil.toastMsg(context, message);
        });
    }

    private void leaveFamily() {
        StyledDialog.buildLoading().show();
        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("phoneNum", SpUtils.get(context, "phoneNum", ""));
                Request request = new Request.Builder()
                        .url(IP + "/user/leaveFamily")
                        .post(RequestBody.create(payload.toString(), JSON))
                        .build();
                try (Response response = CLIENT.newCall(request).execute()) {
                    String responseText = response.body() == null ? "" : response.body().string();
                    if (!response.isSuccessful() || responseText.isEmpty()) {
                        finishWithError("退出家庭失败，请稍后重试");
                        return;
                    }
                    JSONObject jsonResponse = new JSONObject(responseText);
                    if (!jsonResponse.optBoolean("success", false)) {
                        finishWithError(jsonResponse.optString("message", "退出家庭失败"));
                        return;
                    }
                    finishFamilyChange("", "", "已退出家庭");
                }
            } catch (Exception exception) {
                finishWithError("网络异常，请检查网络后重试");
            }
        }, "family-leave").start();
    }

    private LinearLayout getFamilyMembersView() {
        LinearLayout layout = (LinearLayout) LinearLayout.inflate(context, R.layout.item_activity_personal_info_member_family, null);
        TextView familyIdView = layout.findViewById(R.id.tv_family_id);
        familyIdView.setText("家庭ID：" + SpUtils.get(context, "familyId", ""));
        QMUIRoundButton leaveFamilyButton = layout.findViewById(R.id.btn_leave_family);
        leaveFamilyButton.setOnClickListener(view -> StyledDialog.buildIosAlert(
                "退出家庭",
                "退出后将不再看到家庭成员账单，本人的账单不会被删除。",
                new MyDialogListener() {
                    @Override
                    public void onFirst() {
                        leaveFamily();
                    }

                    @Override
                    public void onSecond() {
                    }
                }).setBtnText("确认退出", "取消").show());

        for (int index = 0; index < familyMembers.length(); index++) {
            JSONObject member = familyMembers.optJSONObject(index);
            if (member == null) {
                continue;
            }
            LinearLayout memberView = (LinearLayout) LinearLayout.inflate(context, R.layout.item_activity_personal_info_member_family_item, null);
            ImageView portraitView = memberView.findViewById(R.id.iv_family_member_portrait);
            TextView identityAndNicknameView = memberView.findViewById(R.id.tv_family_member_identity_and_nickname);
            TextView phoneView = memberView.findViewById(R.id.tv_family_member_phoneNum);

            String portrait = member.optString("portrait", "");
            Bitmap portraitBitmap = portrait.isEmpty() ? null : base642bitmap(portrait);
            if (portraitBitmap == null) {
                portraitView.setImageDrawable(context.getDrawable(R.drawable.ic_portrait));
            } else {
                portraitView.setImageDrawable(new BitmapDrawable(context.getResources(), portraitBitmap));
            }

            String identity = valueOrPlaceholder(member.optString("familyIdentity", ""), "成员");
            String nickname = valueOrPlaceholder(member.optString("nickname", ""), "未设置昵称");
            identityAndNicknameView.setText(identity + " · " + nickname);
            phoneView.setText(member.optString("phoneNum", ""));
            layout.addView(memberView);
        }
        return layout;
    }

    private static String valueOrPlaceholder(String value, String placeholder) {
        return value == null || value.trim().isEmpty() ? placeholder : value.trim();
    }
}
