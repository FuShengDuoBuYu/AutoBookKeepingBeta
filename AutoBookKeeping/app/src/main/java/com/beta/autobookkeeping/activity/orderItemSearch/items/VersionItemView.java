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
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import Util.ProjectUtil;

public class VersionItemView {
    private Context context;
    private OrderItemSearchActivity activity;
    private LinearLayout versionItemView;
    QMUIRoundButton btnPersonalVersion,btnFamilyVersion;
    SearchConditionEntity searchConditionEntity = SearchConditionEntity.getINSTANCE();

    public LinearLayout getVersionItemView(){
        versionItemView = (LinearLayout) View.inflate(context, R.layout.item_activity_order_search_version_item, null);
        findViewsById();
        initBtns();
        recoverFromSearchConditionEntity();
        return versionItemView;
    }

    private void findViewsById(){
        btnPersonalVersion = versionItemView.findViewById(R.id.btn_personal_version);
        btnFamilyVersion = versionItemView.findViewById(R.id.btn_family_version);
    }

    private void initBtns(){
        btnPersonalVersion.setOnClickListener(new View.OnClickListener() {
        @Override
            public void onClick(View v) {
                //修改按钮样式
                btnPersonalVersion.setBackgroundColor(context.getResources().getColor(R.color.blue));
                btnPersonalVersion.setTextColor(context.getResources().getColor(R.color.white));
                btnFamilyVersion.setBackgroundColor(context.getResources().getColor(R.color.item_background));
                btnFamilyVersion.setTextColor(context.getResources().getColor(R.color.primary_font));
                //将搜索条件存入实体类
                searchConditionEntity.setMode(PERSONAL_MODE);
                ProjectUtil.toastMsg(context,"您选择了"+PERSONAL_MODE+"模式");
                activity.closeMenu();
            }
        });
        btnFamilyVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //修改按钮样式
                btnFamilyVersion.setBackgroundColor(context.getResources().getColor(R.color.blue));
                btnFamilyVersion.setTextColor(context.getResources().getColor(R.color.white));
                btnPersonalVersion.setBackgroundColor(context.getResources().getColor(R.color.item_background));
                btnPersonalVersion.setTextColor(context.getResources().getColor(R.color.primary_font));
                //将搜索条件存入实体类
                searchConditionEntity.setMode(FAMILY_MODE);
                ProjectUtil.toastMsg(context,"您选择了"+FAMILY_MODE+"模式");
                activity.closeMenu();
            }
        });
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

    private void recoverFromSearchConditionEntity(){
        if(searchConditionEntity.getMode().equals(PERSONAL_MODE)){
            btnPersonalVersion.setBackgroundColor(context.getResources().getColor(R.color.blue));
            btnPersonalVersion.setTextColor(context.getResources().getColor(R.color.white));
            btnFamilyVersion.setBackgroundColor(context.getResources().getColor(R.color.item_background));
            btnFamilyVersion.setTextColor(context.getResources().getColor(R.color.primary_font));
        }else{
            btnFamilyVersion.setBackgroundColor(context.getResources().getColor(R.color.blue));
            btnFamilyVersion.setTextColor(context.getResources().getColor(R.color.white));
            btnPersonalVersion.setBackgroundColor(context.getResources().getColor(R.color.item_background));
            btnPersonalVersion.setTextColor(context.getResources().getColor(R.color.primary_font));
        }
    }
}
