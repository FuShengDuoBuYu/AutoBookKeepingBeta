package com.beta.autobookkeeping.activity.orderItemSearch.items.otherDescriptionView;

import static Util.ConstVariable.COST_TYPE;
import static Util.ConstVariable.ORDER_TYPE;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.orderItemSearch.OrderItemSearchActivity;
import com.beta.autobookkeeping.activity.orderItemSearch.items.SearchConditionEntity;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.util.ArrayList;
import java.util.Arrays;

import Util.ProjectUtil;

public class OtherDescriptionView {
    private Context context;
    private OrderItemSearchActivity activity;
    private SearchConditionEntity searchConditionEntity = SearchConditionEntity.getINSTANCE();
    private String searchOrderRemark = "";
    private LinearLayout linearLayout;
    private GridView gvOrderType;
    private EditText etSearchOrderRemark;
    private String[] orderTypes = new String[COST_TYPE.length+2];
    private ArrayList<String> searchOrderTypes = new ArrayList<>();
    private QMUIRoundButton btnConfirm;
    public OtherDescriptionView(Context context) {
        this.context = context;
        activity = (OrderItemSearchActivity) context;
    }

    public LinearLayout getDescriptionView() {
        linearLayout = (LinearLayout) View.inflate(context, R.layout.item_activity_order_search_other_description_item, null);
        findViewsById();
        initData();
        initGridView();
        initEditText();
        initConfirmButton();
        recoverFromSearchConditionEntity();
        return linearLayout;
    }

    private void findViewsById(){
        gvOrderType = linearLayout.findViewById(R.id.gv_order_type);
        etSearchOrderRemark = linearLayout.findViewById(R.id.et_search_order_remark);
        btnConfirm = linearLayout.findViewById(R.id.btn_description_confirm);
    }

    private void initData(){
        orderTypes[0] = "不限";
        for (int i = 0; i < COST_TYPE.length; i++) {
            orderTypes[i+1] = COST_TYPE[i];
        }
        orderTypes[orderTypes.length-1] = "收入";
    }

    private void initGridView(){
        GridViewAdapter adapter = new GridViewAdapter(context,orderTypes,searchConditionEntity.getSearchCostType());
        gvOrderType.setAdapter(adapter);
        gvOrderType.setOnItemClickListener((parent, view, position, id) -> {
            //如果数组里有这个账单,代表已经被选择了,那么就取消选择
            if (searchOrderTypes.contains(orderTypes[position])){
                searchOrderTypes.remove(orderTypes[position]);
                //同时修改边框和颜色
                QMUIRoundButton button = (QMUIRoundButton) ((LinearLayout)view).getChildAt(0);
                button.setBackgroundColor(context.getResources().getColor(R.color.item_background));
                button.setTextColor(context.getResources().getColor(R.color.primary_font));
            }
            //如果数组里没有这个账单,代表没有被选择,那么就添加到数组里
            else {
                searchOrderTypes.add(orderTypes[position]);
                //同时修改边框和颜色
                QMUIRoundButton button = (QMUIRoundButton) ((LinearLayout)view).getChildAt(0);
                button.setBackgroundColor(context.getResources().getColor(R.color.blue));
                button.setTextColor(context.getResources().getColor(R.color.white));
            }
        });
    }

    private void initEditText(){
        etSearchOrderRemark.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus){
                searchOrderRemark = etSearchOrderRemark.getText().toString();
            }
        });
    }

    private void initConfirmButton(){
        btnConfirm.setOnClickListener(v -> {
            searchConditionEntity.setSearchCostType(searchOrderTypes.toArray(new String[searchOrderTypes.size()]));
            searchOrderRemark = etSearchOrderRemark.getText().toString();
            searchConditionEntity.setSearchOrderRemark(searchOrderRemark);
            ProjectUtil.toastMsg(context,"进行多项查询");
            activity.closeMenu();
        });
    }

    private void recoverFromSearchConditionEntity(){
        //恢复账单类型
        String[] originSearchOrderType = searchConditionEntity.getSearchCostType();
        searchOrderTypes.addAll(Arrays.asList(originSearchOrderType));
        String searchOrderRemark = searchConditionEntity.getSearchOrderRemark();
        if (searchOrderRemark != null){
            etSearchOrderRemark.setText(searchOrderRemark);
        }
    }
}
