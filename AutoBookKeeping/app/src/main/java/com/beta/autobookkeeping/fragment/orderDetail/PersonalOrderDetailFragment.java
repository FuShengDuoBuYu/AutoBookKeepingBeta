package com.beta.autobookkeeping.fragment.orderDetail;

import static Util.ConstVariable.IP;
import static Util.ProjectUtil.BLUE;
import static Util.ProjectUtil.getCurrentMonth;
import static Util.ProjectUtil.getCurrentYear;
import static Util.ProjectUtil.setDayOrderItem;
import static Util.ProjectUtil.setDayOrderTitle;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.main.MainActivity;
import com.beta.autobookkeeping.activity.main.entity.OrderDayItems;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.activity.orderDetail.OrderDetailActivity;
import com.willowtreeapps.spruce.Spruce;
import com.willowtreeapps.spruce.SpruceAnimator;
import com.willowtreeapps.spruce.animation.DefaultAnimations;
import com.willowtreeapps.spruce.sort.DefaultSort;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import Util.ProjectUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonalOrderDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalOrderDetailFragment extends Fragment {

    private ArrayList<OrderInfo> ordersInfo;
    private ArrayList<OrderDayItems> orderDayItems;
    private SQLiteDatabase db;

    private ScrollView svOrderDetail;
    private LinearLayout lvOrderDetail;

    public PersonalOrderDetailFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static PersonalOrderDetailFragment newInstance(ArrayList<OrderInfo> ordersInfo, String test) {
        PersonalOrderDetailFragment fragment = new PersonalOrderDetailFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //初始化
        View view = inflater.inflate(R.layout.fragment_order_detail, container, false);
        svOrderDetail = view.findViewById(R.id.svOrderDetail);
        lvOrderDetail = view.findViewById(R.id.lvOrderDetail);
        db=SQLiteDatabase.openOrCreateDatabase(this.getActivity().getFilesDir().toString() + "/orderInfo.db", null);
        return view;
    }

    public void addViewByData(Context context){
        //获取要展示的数值
        Activity activity = getActivity();
        if(activity instanceof MainActivity){
            this.ordersInfo = ((MainActivity) activity).getShowOrdersInfo().get(1);
            this.orderDayItems = ((MainActivity) activity).getShowOrdersInfo().get(0);
        }
        //先清除所有view,防止重复显示
        lvOrderDetail.removeAllViews();
        int ordersIndex = 0;
        //加每日标题
        for(int i = 0;i < orderDayItems.size();i++){
            lvOrderDetail.addView(setDayOrderTitle(
                    orderDayItems.get(i).getMonth()+"月"+orderDayItems.get(i).getDay()+"日",
                    orderDayItems.get(i).getCategory()+String.valueOf(orderDayItems.get(i).getDayOfMoney()+"元"),
                    context
            ));
            //加各个账单
            for(int j = 0;j < orderDayItems.get(i).getOrderNums();j++,ordersIndex++){
                int itemIdInDatabase = this.ordersInfo.get(ordersIndex).getId();
                String category = this.ordersInfo.get(ordersIndex).getCostType()  + (this.ordersInfo.get(ordersIndex).getOrderRemark().equals("")?"":"-"+this.ordersInfo.get(ordersIndex).getOrderRemark());
                String payWay = this.ordersInfo.get(ordersIndex).getBankName();
                String dayMoney = String.format("%.1f",this.ordersInfo.get(ordersIndex).getMoney())+"元";
                String time = ProjectUtil.getWeek(new Date(getCurrentYear(),getCurrentMonth(),this.ordersInfo.get(ordersIndex).getDay())) + " " +this.ordersInfo.get(ordersIndex).getClock().substring(this.ordersInfo.get(ordersIndex).getClock().length()-5,this.ordersInfo.get(ordersIndex).getClock().length());
                LinearLayout dayOrderItem = setDayOrderItem(category,payWay,dayMoney,time,context);
                //为每个item设置长按选择删除事件
                dayOrderItem.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Cursor cursor = db.query ("orderInfo",null,null,null,null,null,"id desc");
                        confirmDeleteOrderInfo(cursor,itemIdInDatabase);
                        return true;
                    }
                });
                //为每个item设置点击进行账单修改事件
                dayOrderItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        modifyOrderInfo(itemIdInDatabase);
                    }
                });
                lvOrderDetail.addView(dayOrderItem);
            }
        }
    }

    //设置点击后修改账单信息的事件
    public void modifyOrderInfo(int itemIdInDatabase) {
        //跳转到新增界面
        Intent intent = new Intent(getContext(), OrderDetailActivity.class);
        //在数据库里找到这个数据
        Cursor cursor = db.query("orderInfo",null,"id="+itemIdInDatabase,null,null,null,null);
        //将已有的数据传过去
        cursor.moveToNext();
        Bundle bundle = new Bundle();
        bundle.putInt("id",cursor.getInt(0));
        bundle.putInt("year",cursor.getInt(1));
        bundle.putInt("month",cursor.getInt(2));
        bundle.putInt("day",cursor.getInt(3));
        bundle.putString("clock",cursor.getString(4));
        bundle.putFloat("money",cursor.getFloat(5));
        bundle.putString("bankName",cursor.getString(6));
        bundle.putString("orderRemark",cursor.getString(7));
        bundle.putString("costType",cursor.getString(8));
        intent.putExtras(bundle);
        getContext().startActivity(intent);
    }

    //根据id删除数据
    @SuppressLint("UseCompatLoadingForDrawables")
    public void confirmDeleteOrderInfo(Cursor cursor, int itemId){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("是否删除该记录?").setIcon(getContext().getDrawable(R.drawable.ic_warning)).setMessage("删除后该账单将不可恢复!");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String url = IP+"/deleteOrder/"+itemId;
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(url).delete().build();
                        try{
                            Response response = client.newCall(request).execute();
                            if(response.code()==200){
                                JSONObject jsonResponse = new JSONObject(response.body().string());
                                if(jsonResponse.getBoolean("success")){
                                    handleAfterDelete(String.valueOf(itemId));
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
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(BLUE);
    }

    //删除的主线程操作
    public void handleAfterDelete(String itemId){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //本地删除
                String sql = "delete from orderInfo where id =" + itemId;
                db.execSQL(sql);
                ProjectUtil.toastMsg(getContext(),"删除成功");
                addViewByData(getContext());
                //在activity更新数据
                Activity activity = getActivity();
                if(activity instanceof MainActivity){
                    activity = (MainActivity) activity;
                    ((MainActivity) activity).showDayAndMonthMoney();
                }
            }
        });
    }

    //刷新显示
    @Override
    public void onStart() {
        addViewByData(getContext());
//        SpruceAnimator spruceAnimator = new Spruce
//                .SpruceBuilder(lvOrderDetail)
//                .sortWith(new DefaultSort(/*interObjectDelay=*/50L))
//                .animateWith(new Animator[] {DefaultAnimations.shrinkAnimator(lvOrderDetail, /*duration=*/800)})
//                .start();

        super.onStart();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
}