package com.beta.autobookkeeping.fragment.Settings;

import static Util.ConstVariable.IP;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundLinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Util.ProjectUtil;
import Util.SpUtils;
import Util.StringUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TargetCostWaterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TargetCostWaterFragment extends Fragment {
    private Context context;
    private Activity activity;
    private Button btnPersonalCostTarget,btnFamilyCostTarget;
    private Boolean ifAnimationRunning = false;
    private float rotationAngle = 0.0f;
    private int currentRotation = 0;
    private CardView cvPersonalTarget,cvFamilyTarget;
    private TextView tvPersonalTargetCostNumber,tvPersonalCostNumber,tvFamilyTargetCostNumber,tvFamilyCostNumber;
    final double[] familyMonthCost = {0.0};
    private ImageView ivWaveWater;
    public TargetCostWaterFragment() {
    }

    //dialog listener
    View.OnClickListener onPersonalClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            StyledDialog.buildNormalInput("设置个人目标支出", "请输入数字", null, "确定", "取消", new MyDialogListener() {
                Integer targetPersonalCost;
                @Override
                public void onFirst() {
                    //存到本地
                    SpUtils.put(context,"targetPersonalCost",targetPersonalCost);
                    ProjectUtil.toastMsg(context,"设置成功");
                    initPersonalBtn();
                    initTextViews();
                }
                @Override
                public void onSecond() {}
                @Override
                public void onGetInput(CharSequence input1, CharSequence input2) {
                    targetPersonalCost = Integer.parseInt(input1.toString());
                    super.onGetInput(input1, input2);
                }
            }).show();
        }
    };
    View.OnClickListener onFamilyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            StyledDialog.buildNormalInput("设置家庭目标支出", "请输入数字", null, "确定", "取消", new MyDialogListener() {
                Integer targetFamilyCost;
                @Override
                public void onFirst() {
                    //存到本地
                    SpUtils.put(context,"targetFamilyCost",targetFamilyCost);
                    ProjectUtil.toastMsg(context,"设置成功");
                    initFamilyBtn((int) familyMonthCost[0]);
                    initTextViews();
                }
                @Override
                public void onSecond() {}
                @Override
                public void onGetInput(CharSequence input1, CharSequence input2) {
                    targetFamilyCost = Integer.parseInt(input1.toString());
                    super.onGetInput(input1, input2);
                }
            }).show();
        }
    };
    public static TargetCostWaterFragment newInstance(String param1, String param2) {
        return new TargetCostWaterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_target_cost_water, container, false);
        findViewByIdAndInit(v);
        getPhoneRotationAngle();
        return v;
    }

    private void findViewByIdAndInit(View v){
        context = getContext();
        activity = getActivity();
        btnPersonalCostTarget = v.findViewById(R.id.btn_personal_target_cost);
        btnFamilyCostTarget = v.findViewById(R.id.btn_family_target_cost);
        initPersonalBtn();
        getSomeMonthMoney();
        cvPersonalTarget = v.findViewById(R.id.cv_personal_target);
        cvFamilyTarget = v.findViewById(R.id.cv_family_target);
        initCardViews();
        tvPersonalTargetCostNumber = v.findViewById(R.id.tv_personal_target_cost_number);
        tvPersonalCostNumber = v.findViewById(R.id.tv_personal_cost_number);
        tvFamilyTargetCostNumber = v.findViewById(R.id.tv_family_target_cost_number);
        tvFamilyCostNumber = v.findViewById(R.id.tv_family_cost_number);
        initTextViews();
    }

    private void initPersonalBtn(){
        double personalMonthCost = ProjectUtil.getMonthCost(ProjectUtil.getCurrentYear(),ProjectUtil.getCurrentMonth(),context);
        int personalTargetCost = (SpUtils.get(context,"targetPersonalCost",0)==null)?2000:(Integer) SpUtils.get(context,"targetPersonalCost",0);
        int personalPercent = Math.abs((int) (Math.abs(personalMonthCost)/personalTargetCost*100));
        personalPercent = Math.min(personalPercent, 100);
        //设置距离顶部高度
        float marginTopPx = context.getResources().getDimension(R.dimen.water_round_container_height)*(100- personalPercent) / 100;
        //设置marginTop
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) btnPersonalCostTarget.getLayoutParams();
        marginLayoutParams.setMargins(0, (int) marginTopPx,0,0);
        btnPersonalCostTarget.setLayoutParams(marginLayoutParams);
        //设置点击事件
        btnPersonalCostTarget.setOnClickListener(onPersonalClickListener);
    }

    private void initFamilyBtn(int familyMonthCost){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int familyTargetCost = (SpUtils.get(context,"targetFamilyCost",0)==null)?2000:(Integer) SpUtils.get(context,"targetFamilyCost",0);
                int familyPercent = (Math.abs(familyMonthCost)*100/familyTargetCost);
                familyPercent = Math.min(familyPercent, 100);
                //设置距离顶部高度
                float marginTopPx = context.getResources().getDimension(R.dimen.water_round_container_height)*(100- familyPercent) / 100;
                //设置marginTop
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) btnFamilyCostTarget.getLayoutParams();
                marginLayoutParams.setMargins(0, (int) marginTopPx,0,0);
                btnFamilyCostTarget.setLayoutParams(marginLayoutParams);
                //设置点击事件
                btnFamilyCostTarget.setOnClickListener(onFamilyClickListener);
                initTextViews();
//                initFamilyBtn(familyMonthCost[0]);
            }
        });
    }

    private void initCardViews(){
        cvPersonalTarget.setOnClickListener(onPersonalClickListener);
        cvFamilyTarget.setOnClickListener(onFamilyClickListener);
    }

    private void initTextViews(){
        tvPersonalTargetCostNumber.setText(SpUtils.get(context,"targetPersonalCost",0)==null?"/2000元":"/"+SpUtils.get(context,"targetPersonalCost",0)+"元");
        //取整
        tvPersonalCostNumber.setText(String.valueOf((int)Math.abs(ProjectUtil.getMonthCost(ProjectUtil.getCurrentYear(),ProjectUtil.getCurrentMonth(),context)))+"元");
        tvFamilyTargetCostNumber.setText(SpUtils.get(context,"targetFamilyCost",0)==null?"/2000元":"/"+SpUtils.get(context,"targetFamilyCost",0)+"元");
        //取整
        tvFamilyCostNumber.setText(String.valueOf((int)Math.abs(familyMonthCost[0]))+"元");
    }

    //获取手机旋转的角度
    private void getPhoneRotationAngle(){
        OrientationEventListener mOrientationListener = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int orientation) {
                currentRotation = orientation;
                initWaterButton(orientation);
            }
        };
        mOrientationListener.enable();
    }

    private void initWaterButton(int orientation){
        //设置动画
        if(ifAnimationRunning)
            return;
        Animation.AnimationListener listener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ifAnimationRunning = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ifAnimationRunning = false;
                if(currentRotation-rotationAngle*3>12||rotationAngle*3-currentRotation>12){
                    initWaterButton(currentRotation);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        //手机右转
        if(orientation>0&&orientation<90){
            RotateAnimation rotateAnimation = new RotateAnimation(rotationAngle,-orientation/3f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            RotateAnimation rotateAnimation1 = new RotateAnimation(rotationAngle,-orientation/3f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

            rotationAngle = -orientation/3f;
            rotateAnimation.setFillAfter(true);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setAnimationListener(listener);
            btnPersonalCostTarget.startAnimation(rotateAnimation);
            btnFamilyCostTarget.startAnimation(rotateAnimation);



//            //让波浪也旋转,同时平移
//            rotateAnimation1.setFillAfter(true);
//            rotateAnimation1.setDuration(3000);
//            //平移整个长度
//            TranslateAnimation translateAnimation = new TranslateAnimation(0,-ivWaveWater.getWidth()+cvPersonalTarget.getWidth(),0,0);
//            translateAnimation.setDuration(3000);
//            translateAnimation.setFillAfter(false);
//            //不重复
//            translateAnimation.setRepeatCount(0);
//            AnimationSet animationSet = new AnimationSet(true);
//            animationSet.addAnimation(rotateAnimation1);
//            animationSet.addAnimation(translateAnimation);
//            animationSet.setAnimationListener(listener);
//            ivWaveWater.startAnimation(animationSet);
        }
        //左转
        else if (orientation>=270&& orientation<=360){
            RotateAnimation rotateAnimation = new RotateAnimation(rotationAngle,(360-orientation)/3f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotationAngle = (360-orientation)/3f;
            rotateAnimation.setFillAfter(true);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setAnimationListener(listener);
            btnPersonalCostTarget.startAnimation(rotateAnimation);
            btnFamilyCostTarget.startAnimation(rotateAnimation);
        }
        //平放
        else{
            RotateAnimation rotateAnimation = new RotateAnimation(rotationAngle,0,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotationAngle = 0;
            rotateAnimation.setFillAfter(true);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setAnimationListener(listener);
            btnPersonalCostTarget.startAnimation(rotateAnimation);
            btnFamilyCostTarget.startAnimation(rotateAnimation);
        }
    }

    //获取某个月的家庭支出
    private void getSomeMonthMoney(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/findFamilySomeMonthCosts/"+ SpUtils.get(getContext(),"familyId","")+"/"+ProjectUtil.getCurrentYear()+"/"+ProjectUtil.getCurrentMonth();
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).get().build();
                try{
                    Response response = client.newCall(request).execute();
                    if(response.code()==200){
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        if(jsonResponse.getBoolean("success")){
                            //将Map转换为Map
                            Map<String,Object> map = StringUtil.Json2Map(jsonResponse.getJSONObject("data").toString());
                            Map<String, ArrayList<OrderInfo>> map1 = new HashMap<>();
                            //遍历Map
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                JSONArray jsonArray = new JSONArray(entry.getValue().toString());
                                for(int i = 0;i < jsonArray.length();i++){
                                    familyMonthCost[0] += jsonArray.getJSONObject(i).getDouble("money");
                                }
                            }
                            initFamilyBtn((int)familyMonthCost[0]);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}