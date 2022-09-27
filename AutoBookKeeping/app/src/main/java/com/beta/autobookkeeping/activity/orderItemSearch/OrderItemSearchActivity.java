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
    private DropDownMenu dropDownMenu = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchConditionEntity.setContext(OrderItemSearchActivity.this);
        setContentView(R.layout.activity_order_item_search);
        findViewsById();
        initDropDownMenu();
    }

    //找到各个控件并进行数据的初始化
    public void findViewsById(){
        //找到控件
        llDropMenu = findViewById(R.id.ll_down_menu);
        dropDownMenu = findViewById(R.id.drop_down_menu);
    }

    private void initDropDownMenu(){
        //初始化下拉菜单
        ArrayList<String> tabs = new ArrayList<>();
        tabs.add("版本");
        tabs.add("日期");
        tabs.add("其他筛选");
        dropDownMenu.setupDropDownMenu(tabs, searchConditionEntity.getPopupViews());
    }

    public void closeMenu(){
        dropDownMenu.closeMenu();
        searchOrders();
    }

    private void searchOrders(){
        //输出搜索条件
        searchConditionEntity.printSearchCondition();
        //todo:向后端拿数据
    }

}