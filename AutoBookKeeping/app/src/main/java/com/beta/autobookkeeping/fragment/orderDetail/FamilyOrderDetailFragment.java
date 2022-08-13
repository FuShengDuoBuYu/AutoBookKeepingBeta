package com.beta.autobookkeeping.fragment.orderDetail;

import static Util.ConstVariable.IP;

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beta.autobookkeeping.R;
import com.hss01248.dialog.StyledDialog;

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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    View rootView;
    LinearLayout ll_FamilyOrders;
    // TODO: Rename and change types of parameters
    private String mParam1;

    public FamilyOrderDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment FamilyOrderDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView ==null){
            rootView = inflater.inflate(R.layout.fragment_family_order_detail, container, false);

        }
        findViewById(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        getFamilyOrders();
        super.onResume();
    }

    private void findViewById(View v){
        ll_FamilyOrders = v.findViewById(R.id.ll_familyOrders);
    }

    private void getFamilyOrders(){
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
                        if(jsonResponse.getBoolean("success")){
                            JSONArray familyOrdersAndFamilyUsers = jsonResponse.getJSONArray("data");
                            afterGetFamilyOrders(familyOrdersAndFamilyUsers,ll_FamilyOrders);
                        }
                        else{
                            Looper.prepare();
                            ProjectUtil.toastMsg(getContext(),jsonResponse.getString("message"));
                            Looper.loop();
                        }
                    }
                    else{
                        Looper.prepare();
                        Log.d("test",String.valueOf(response.code()));
                        Log.d("test",String.valueOf(response.toString()));
                        ProjectUtil.toastMsg(getContext(),"服务器出错");
                        Looper.loop();
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
                        }
                        else{
                            nums++;
                            money+= finalFamilyOrders.getJSONObject(i).getDouble("money");
                            daysCount.add(nums);
                            dayCost.add(money);
                            nums = 0;
                            money = 0.0;
                            continue;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                daysCount.add(nums);
                dayCost.add(money);
                int orderIndex = 0;
                for (int i =0;i < daysCount.size();i++){
                    try {
                        linearLayout.addView(ProjectUtil.setDayOrderTitle(finalFamilyOrders.getJSONObject(orderIndex).getString("clock").substring(0,6),(dayCost.get(i)+"元"),getContext()));
                        for (int j = 0; j < daysCount.get(i); j++) {

                            JSONObject order = finalFamilyOrders.getJSONObject(orderIndex);
                            ImageView imageView = new ImageView(getContext());
                            imageView.setBackground(userPortrait.get(order.getString("userId")));
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

                            linearLayout.addView(familyOrderItem);
                            orderIndex++;
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        StyledDialog.dismissLoading(getActivity());
    }
}