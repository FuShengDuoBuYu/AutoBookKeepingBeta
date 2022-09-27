package com.beta.autobookkeeping.activity.orderItemSearch.items;

import static Util.ConstVariable.FAMILY_MODE;
import static Util.ConstVariable.PERSONAL_MODE;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.orderItemSearch.OrderItemSearchActivity;

import Util.ProjectUtil;

public class VersionItemView {
    private Context context;
    private OrderItemSearchActivity activity;
    private LinearLayout versionItemView;
    SearchConditionEntity searchConditionEntity = SearchConditionEntity.getINSTANCE();
    public LinearLayout getVersionItemView(){
        versionItemView = new LinearLayout(context);
        versionItemView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        versionItemView.setOrientation(LinearLayout.VERTICAL);
        versionItemView.addView(initItem("个人版"));
        versionItemView.addView(initItem("家庭版"));
        return versionItemView;
    }

    private LinearLayout initItem(String text){
        LinearLayout item = new LinearLayout(context);
        item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //背景白色
        item.setBackgroundColor(context.getResources().getColor(R.color.white));
        TextView textView = new TextView(context);
        if(text.equals(searchConditionEntity.getMode())){
            //蓝色
            textView.setTextColor(context.getResources().getColor(R.color.blue));
        }else{
            textView.setTextColor(context.getResources().getColor(R.color.black));
        }
        //字体大小
        textView.setTextSize(18);
        //字体居中
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(text);
        //margin
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 20);
        textView.setLayoutParams(layoutParams);
        //padding
        textView.setPadding(0,30,0,30);
        item.addView(textView);

        //点击事件
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProjectUtil.toastMsg(context, "您选择了"+text);
                searchConditionEntity.setMode(text.equals(FAMILY_MODE)?FAMILY_MODE:PERSONAL_MODE);
                activity.closeMenu();
            }
        });
        return item;
    }


    public VersionItemView(Context context) {
        this.context = context;
        this.activity = (OrderItemSearchActivity) context;
    }
}
