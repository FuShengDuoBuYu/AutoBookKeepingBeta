package com.beta.autobookkeeping.fragment.orderDetail;

import static Util.ConstVariable.IP;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
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
import java.util.List;
import java.util.Map;

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
                            JSONArray familyOrders = jsonResponse.getJSONArray("data");
                            afterGetFamilyOrders(familyOrders,ll_FamilyOrders);
                        }
                        else{
                            Looper.prepare();
                            ProjectUtil.toastMsg(getContext(),jsonResponse.getString("message"));
                            Looper.loop();
                        }
                    }
                    else{
                        Looper.prepare();
                        ProjectUtil.toastMsg(getContext(),"服务器出错");
                        Looper.loop();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void afterGetFamilyOrders(JSONArray familyOrders,LinearLayout linearLayout){
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
                for(int i = 0;i < familyOrders.length()-1;i++){
                    try {
                        if(familyOrders.getJSONObject(i).getInt("day")==familyOrders.getJSONObject(i+1).getInt("day")){
                            money+=familyOrders.getJSONObject(i).getDouble("money");
                            nums++;
                        }
                        else{
                            nums++;
                            money+=familyOrders.getJSONObject(i).getDouble("money");
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
                        linearLayout.addView(ProjectUtil.setDayOrderTitle(familyOrders.getJSONObject(orderIndex).getString("clock").substring(0,6),(dayCost.get(i)+"元"),getContext()));
                        for (int j = 0; j < daysCount.get(i); j++) {
                            ImageView imageView = new ImageView(getContext());
                            imageView.setImageDrawable(getContext().getDrawable(R.drawable.ic_portrait));
                            imageView.setPadding(0,0,20,0);
                            JSONObject order = familyOrders.getJSONObject(orderIndex);

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