package com.beta.autobookkeeping.fragment.orderDetail;

import static Util.ConstVariable.IP;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.main.DialogActivity;
import com.beta.autobookkeeping.activity.main.MainActivity;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.adapter.SuperLvHolder;
import com.hss01248.dialog.config.ConfigBean;
import com.willowtreeapps.spruce.Spruce;
import com.willowtreeapps.spruce.SpruceAnimator;
import com.willowtreeapps.spruce.animation.DefaultAnimations;
import com.willowtreeapps.spruce.sort.DefaultSort;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Util.DensityUtil;
import Util.ImageUtil;
import Util.ProjectUtil;
import Util.SpUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FamilyOrderDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FamilyOrderDetailFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    View rootView;
    LinearLayout ll_FamilyOrders;
    private String mParam1;
    Activity activity = null;
    private Double dayMoney = 0.0;
    private Double monthMoney = 0.0;
    public FamilyOrderDetailFragment() {
        // Required empty public constructor
    }

    public static FamilyOrderDetailFragment newInstance(String param1) {
        FamilyOrderDetailFragment fragment = new FamilyOrderDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView ==null){
            rootView = inflater.inflate(R.layout.fragment_family_order_detail, container, false);
        }
        activity = getActivity();
        findViewById(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        getFamilyOrders();
        //更新头部信息

        super.onResume();
    }

    @Override
    public void onStart() {
        getFamilyOrders();


        super.onStart();
    }

    private void findViewById(View v){
        ll_FamilyOrders = v.findViewById(R.id.ll_familyOrders);
    }

    private void getFamilyOrders(){
        if (SpUtils.get(activity, "familyId", "")==null || SpUtils.get(activity, "familyId", "").equals("")) {
            Toast.makeText(activity, "您还没有加入家庭", Toast.LENGTH_SHORT).show();
            return;
        }
        StyledDialog.buildLoading().show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/findMonthFamilyOrders/"+ SpUtils.get(getContext(),"familyId","")+"/"+String.valueOf(ProjectUtil.getCurrentMonth());
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).get().build();
                try{
                    Response response = client.newCall(request).execute();
                    if(response.code()==200){
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        JSONArray familyOrdersAndFamilyUsers = jsonResponse.getJSONArray("data");
                        afterGetFamilyOrders(familyOrdersAndFamilyUsers,ll_FamilyOrders);
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void afterGetFamilyOrders(JSONArray familyOrdersAndUsers,LinearLayout linearLayout){
        JSONArray familyOrders = null;
        JSONArray familyUsers = null;
        dayMoney = 0.0;
        monthMoney = 0.0;
        try {
            familyOrders = familyOrdersAndUsers.getJSONArray(0);
            familyUsers = familyOrdersAndUsers.getJSONArray(1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //创建一个keyvalue用来存储各个用户的头像信息
        Map<String, Drawable> userPortrait = new HashMap<>();
        for(int i = 0;i < familyUsers.length();i++){
            try {
                if(familyUsers.getJSONObject(i).getString("portrait")==null||"".equals(familyUsers.getJSONObject(i).getString("portrait"))){
                    userPortrait.put(familyUsers.getJSONObject(i).getString("phoneNum"),getContext().getDrawable(R.drawable.ic_portrait));
                }
                else{
                    userPortrait.put(familyUsers.getJSONObject(i).getString("phoneNum"), new BitmapDrawable(ImageUtil.base642bitmap(familyUsers.getJSONObject(i).getString("portrait"))));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONArray finalFamilyOrders = familyOrders;
        getActivity().runOnUiThread(new Runnable() {
            @SuppressLint("ResourceType")
            @Override
            public void run() {
                linearLayout.removeAllViews();
                //获取日总收支和日收支数目
                List<Integer> daysCount = new ArrayList<>();
                List<Double> dayCost = new ArrayList<>();
                Integer nums = 0;
                Double money = 0.0;
                for(int i = 0; i < finalFamilyOrders.length()-1; i++){
                    try {
                        if(finalFamilyOrders.getJSONObject(i).getInt("day")== finalFamilyOrders.getJSONObject(i+1).getInt("day")){
                            money+= finalFamilyOrders.getJSONObject(i).getDouble("money");
                            nums++;
                            //如果是最后一个,就也要加上
                            if(i==finalFamilyOrders.length()-2){
                                nums++;
                                money+= finalFamilyOrders.getJSONObject(i+1).getDouble("money");
                            }
                        }
                        else{
                            nums++;
                            money+= finalFamilyOrders.getJSONObject(i).getDouble("money");
                            daysCount.add(nums);
                            dayCost.add(money);
                            monthMoney+=money;
                            nums = 0;
                            money = 0.0;
                            continue;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("test",String.valueOf(nums));
                daysCount.add(nums);
                dayCost.add(money);
                monthMoney+=money;
                int orderIndex = 0;
                for (int i =0;i < daysCount.size();i++){
                    try {
                        if(finalFamilyOrders.getJSONObject(orderIndex).getInt("day")==ProjectUtil.getCurrentDay()){
                            dayMoney = dayCost.get(i);
                        }

                        linearLayout.addView(ProjectUtil.setDayOrderTitle(finalFamilyOrders.getJSONObject(orderIndex).getString("clock").substring(0,6),(dayCost.get(i)+"元"),getContext()));
                        for (int j = 0; j < daysCount.get(i); j++) {

                            JSONObject order = finalFamilyOrders.getJSONObject(orderIndex);
                            ImageView imageView = new ImageView(getContext());
                            Drawable portrait = userPortrait.get(order.getString("userId"));
                            imageView.setImageDrawable(userPortrait.get(order.getString("userId")));
                            imageView.setPadding(0,0,20,0);
                            imageView.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dpToPx(getContext(),38f), DensityUtil.dpToPx(getContext(),38f)));
                            imageView.setForegroundGravity(Gravity.VERTICAL_GRAVITY_MASK);

                            LinearLayout familyOrderItem = ProjectUtil.setDayOrderItem(
                                    order.getString("costType")+(order.getString("orderRemark").equals("")?"":("-"+order.getString("orderRemark"))),
                                    order.getString("bankName"),
                                     (order.getDouble("money")+"元"),
                                    ProjectUtil.getWeek(new Date(order.getInt("year"), order.getInt("month"),order.getInt("day")))+" "+order.getString("clock").substring(7,order.getString("clock").length()),
                                    getContext(),
                                    imageView
                            );
                            familyOrderItem.setTransitionName("familyOrderItem"+orderIndex);
                            //设置点击事件
                            familyOrderItem.setOnClickListener(v->{
                                showDialog(v);
                            });
                            linearLayout.addView(familyOrderItem);
                            orderIndex++;
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //配置日收支
                if(activity instanceof MainActivity){
                    ((MainActivity) activity).showDayAndMonthMoney(String .format("%.2f",dayMoney),String .format("%.2f",monthMoney));
                }
            }
        });
        StyledDialog.dismissLoading(getActivity());
    }

    private void showDialog(View v){
        //消费图标
        ImageView costTypeIcon = v.findViewById(R.id.order_item_category_image_id);
        Drawable costTypeIconDrawable = costTypeIcon.getDrawable();
        //头像
        ImageView imageView = v.findViewById(R.id.order_item_portrait_id);
        Drawable portraitDrawable = imageView.getDrawable();
        //消费类型
        TextView costType = v.findViewById(R.id.order_item_category_id);
        //消费方式
        TextView payWay = v.findViewById(R.id.order_item_payway_id);
        //消费金额
        TextView money = v.findViewById(R.id.order_item_price_id);
        //时间
        TextView time = v.findViewById(R.id.order_item_time_id);

        //构造pair
        Pair<View, String> pair1 = Pair.create((View)costTypeIcon, costTypeIcon.getTransitionName());
        Pair<View, String> pair2 = Pair.create((View)imageView, imageView.getTransitionName());
        Pair<View, String> pair3 = Pair.create((View)costType, "category");
        Pair<View, String> pair4 = Pair.create((View)payWay, "payway");
        Pair<View, String> pair5 = Pair.create((View)money, "money");
        Pair<View, String> pair6 = Pair.create((View)time, "time");


        MainActivity activity = (MainActivity) getActivity();
        activity.clickFamilyItemToShowDetail(v,pair1,pair2,pair3,pair4,pair5,pair6);

    }
}