package com.beta.autobookkeeping.activity.orderItemSearch.items;

import static Util.ProjectUtil.getCurrentMonth;
import static Util.ProjectUtil.getCurrentYear;
import static Util.ProjectUtil.setDayOrderItem;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.beta.autobookkeeping.activity.main.DialogActivity;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

import Util.DensityUtil;
import Util.ImageUtil;
import Util.ProjectUtil;

public class SearchOrderListViewAdapter extends BaseAdapter {
    private List<OrderInfo> orderInfoList;
    private Context context;
    private Map<String,String> userIdMapPortrait;


    public SearchOrderListViewAdapter(List<OrderInfo> orderInfoList, Context context, Map<String,String> userIdMapPortrait) {
        this.orderInfoList = orderInfoList;
        this.context = context;
        this.userIdMapPortrait = userIdMapPortrait;
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
        return ProjectUtil.setDayOrderItem(category,payWay,dayMoney,time,context,imageView);
    }
}
