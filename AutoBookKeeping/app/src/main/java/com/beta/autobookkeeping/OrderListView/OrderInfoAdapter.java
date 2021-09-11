package com.beta.autobookkeeping.OrderListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.beta.autobookkeeping.R;

import java.util.List;

public class OrderInfoAdapter extends ArrayAdapter<OrderInfo> {
    int resourceId;
    //将要适配的内容传过来
    public OrderInfoAdapter(Context context, int textViewResourceId,List<OrderInfo> orderInfoList){
        super(context,textViewResourceId,orderInfoList);
        resourceId = textViewResourceId;
    }

    // convertView 参数用于将之前加载好的布局进行缓存
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        OrderInfo orderInfo=getItem(position); //获取当前项的orderInfo实例

        // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
        View view;
        ViewHolder viewHolder;
        if (convertView==null){

            // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
            view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

            // 避免每次调用getView()时都要重新获取控件实例
            viewHolder=new ViewHolder();
            viewHolder.tvOrderDetail=view.findViewById(R.id.tvOrderDetail);

            // 将ViewHolder存储在View中（即将控件的实例存储在其中）
            view.setTag(viewHolder);
        } else{
            view=convertView;
            viewHolder=(ViewHolder) view.getTag();
        }

        // 获取控件实例，并调用set...方法使其显示出来
        viewHolder.tvOrderDetail.setText(orderInfo.getTime());
        return view;
    }
    // 定义一个内部类，用于对控件的实例进行缓存
    class ViewHolder{
        TextView tvOrderDetail;
    }
}
