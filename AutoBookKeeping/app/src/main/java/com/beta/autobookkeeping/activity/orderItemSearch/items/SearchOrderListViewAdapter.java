package com.beta.autobookkeeping.activity.orderItemSearch.items;

import static Util.ConstVariable.IP;
import static Util.ProjectUtil.BLUE;
import static Util.ProjectUtil.getCurrentMonth;
import static Util.ProjectUtil.getCurrentYear;
import static Util.ProjectUtil.setDayOrderItem;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.main.DialogActivity;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.activity.orderDetail.OrderDetailActivity;
import com.beta.autobookkeeping.activity.orderItemSearch.OrderItemSearchActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import Util.DensityUtil;
import Util.ImageUtil;
import Util.ProjectUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchOrderListViewAdapter extends BaseAdapter {
    private List<OrderInfo> orderInfoList;
    private Context context;
    private SQLiteDatabase db;
    private Map<String,String> userIdMapPortrait;
    SearchConditionEntity searchConditionEntity = SearchConditionEntity.getINSTANCE();


    public SearchOrderListViewAdapter(List<OrderInfo> orderInfoList, Context context, Map<String,String> userIdMapPortrait) {
        this.orderInfoList = orderInfoList;
        this.context = context;
        this.userIdMapPortrait = userIdMapPortrait;
        db=SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().toString() + "/orderInfo.db", null);
    }
    @Override
    public int getCount() {
        return orderInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return orderInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return orderInfoList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String category = orderInfoList.get(position).getCostType()  + (orderInfoList.get(position).getOrderRemark().equals("")?"":"-"+orderInfoList.get(position).getOrderRemark());
        String payWay = orderInfoList.get(position).getBankName();
        String dayMoney = String.format("%.1f",orderInfoList.get(position).getMoney())+"元";
        //年月日
        String time = orderInfoList.get(position).getYear()+"-"+orderInfoList.get(position).getMonth()+"-"+orderInfoList.get(position).getDay();
        //获取头像
        ImageView imageView = new ImageView(context);
        Drawable drawable = new BitmapDrawable(ImageUtil.base642bitmap(userIdMapPortrait.get(orderInfoList.get(position).getUser())));
        imageView.setBackground(drawable);
        imageView.setPadding(0,0,20,0);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dpToPx(context,38f), DensityUtil.dpToPx(context,38f)));
        imageView.setForegroundGravity(Gravity.VERTICAL_GRAVITY_MASK);
        String portrait = userIdMapPortrait.get(orderInfoList.get(position).getUser());
        LinearLayout res = ProjectUtil.setDayOrderItem(category,payWay,dayMoney,time,context,imageView);
        if (searchConditionEntity.getMode().equals("个人版")){
            //设置点击和长按事件
            res.setOnLongClickListener(v->{
                comfirmDelete(orderInfoList.get(position).getId());
                return true;
            });
            res.setOnClickListener(v->{
                Intent intent = new Intent(context, OrderDetailActivity.class);
                //在数据库里找到这个数据
                int itemIdInDatabase = orderInfoList.get(position).getId();
                Cursor cursor = db.query("orderInfo",null,"id="+itemIdInDatabase,null,null,null,null);
                cursor.moveToNext();
                //将数据传递给OrderDetailActivity
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
                context.startActivity(intent);
            });
        }

        return res;
    }

    private void comfirmDelete(int itemId){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("是否删除该记录?").setIcon(context.getDrawable(R.drawable.ic_warning)).setMessage("删除后该账单将不可恢复!");
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

    private void handleAfterDelete(String itemId){
        Activity activity = (Activity) context;
        activity.runOnUiThread(()->{
            String sql = "delete from orderInfo where id =" + itemId;
            db.execSQL(sql);
            Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
            if (context instanceof OrderItemSearchActivity) {
                ((OrderItemSearchActivity) context).searchOrders();
            }
        });
    }
}
