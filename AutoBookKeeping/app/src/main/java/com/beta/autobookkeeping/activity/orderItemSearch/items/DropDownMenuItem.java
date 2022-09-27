package com.beta.autobookkeeping.activity.orderItemSearch.items;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.orderItemSearch.OrderItemSearchActivity;
import com.hss01248.dialog.StyledDialog;
import com.wdeo3601.dropdownmenu.DropDownMenu;

import java.util.ArrayList;

import Util.ProjectUtil;

public class DropDownMenuItem {
    public static LinearLayout getDropDownMenu(Context context) {
        OrderItemSearchActivity activity = (OrderItemSearchActivity) context;
        ArrayList<String> tabs = new ArrayList<>();
        tabs.add("版本");
        tabs.add("日期");
        tabs.add("其他筛选");
        LinearLayout llDropMenu = (LinearLayout) ViewGroup.inflate(context,R.layout.item_activity_order_search_drop_down_menu_item,null);
        DropDownMenu dropDownMenu = llDropMenu.findViewById(R.id.dropdown_menu);
        dropDownMenu.setupDropDownMenu(tabs, SearchConditionEntity.getINSTANCE().getPopupViews());
        dropDownMenu.setOnMenuStateChangeListener(new DropDownMenu.OnMenuStateChangeListener() {
            @Override
            public void onMenuShow(int tabPosition) {
            }

            @Override
            public void onMenuClose() {
                searchOrders();
            }
        });
        return llDropMenu;
    }

    private static void searchOrders(){
        StyledDialog.buildLoading().show();
    }
}
