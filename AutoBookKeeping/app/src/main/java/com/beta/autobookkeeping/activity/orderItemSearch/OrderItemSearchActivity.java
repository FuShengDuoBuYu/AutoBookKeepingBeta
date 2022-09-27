package com.beta.autobookkeeping.activity.orderItemSearch;

import static Util.ProjectUtil.getCurrentYear;
import static Util.ProjectUtil.setDayOrderItem;
import static Util.ProjectUtil.setDayOrderTitle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.orderItemSearch.items.DropDownMenuItem;
import com.beta.autobookkeeping.activity.orderItemSearch.items.SearchConditionEntity;
import com.beta.autobookkeeping.activity.orderItemSearch.items.VersionItemView;
import com.beta.autobookkeeping.smsTools.SMSDataBase;
import com.wdeo3601.dropdownmenu.DropDownMenu;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Util.ProjectUtil;

public class OrderItemSearchActivity extends AppCompatActivity {

    private LinearLayout llDropMenu = null;
    private SearchConditionEntity searchConditionEntity = SearchConditionEntity.getINSTANCE();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchConditionEntity.setContext(OrderItemSearchActivity.this);
        setContentView(R.layout.activity_order_item_search);
        findViewsAndInit();
    }

    //找到各个控件并进行数据的初始化
    public void findViewsAndInit(){

        //找到控件
        llDropMenu = findViewById(R.id.ll_down_menu);
        LinearLayout dropDownMenu = DropDownMenuItem.getDropDownMenu(OrderItemSearchActivity.this);
        llDropMenu.addView(dropDownMenu);
    }

    public void closeMenu(){
        LinearLayout linearLayout = (LinearLayout) llDropMenu.getChildAt(0);
        DropDownMenu dropDownMenu = (DropDownMenu) linearLayout.getChildAt(0);
        dropDownMenu.closeMenu();
        llDropMenu.removeAllViews();
        LinearLayout dropDownMenu1 = DropDownMenuItem.getDropDownMenu(OrderItemSearchActivity.this);
        llDropMenu.addView(dropDownMenu1);
    }


}