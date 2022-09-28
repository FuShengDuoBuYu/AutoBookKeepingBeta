package com.beta.autobookkeeping.activity.orderItemSearch.items.dropdownViews;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.orderItemSearch.OrderItemSearchActivity;
import com.beta.autobookkeeping.activity.orderItemSearch.items.SearchConditionEntity;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.util.Calendar;

import Util.ProjectUtil;
import top.leefeng.datepicker.DatePickerView;

public class DatePickView {
    private SearchConditionEntity searchConditionEntity = SearchConditionEntity.getINSTANCE();
    private Context context;
    private OrderItemSearchActivity activity;
    private DatePickerView datePickerView;
    private LinearLayout datePickView;
    private boolean ifIgnoreYear = false,ifIgnoreMonth = false,ifIgnoreDay = false;
    private QMUIRoundButton btnIgnoreYear, btnIgnoreMonth, btnIgnoreDay,btnConfirm;
    public LinearLayout getDatePickView(){
        datePickView = (LinearLayout) View.inflate(context, R.layout.item_activity_order_search_date_pick_item, null);
        findViewsById();
        recoverFromSearchConditionEntity();
        return datePickView;
    }


    private void findViewsById() {
        datePickerView = datePickView.findViewById(R.id.date_picker_view);
        btnIgnoreYear = datePickView.findViewById(R.id.btn_ignore_year);
        btnIgnoreYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setYearIgnoreOrNot();
            }
        });
        btnIgnoreMonth = datePickView.findViewById(R.id.btn_ignore_month);
        btnIgnoreMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMonthIgnoreOrNot();
            }
        });
        btnIgnoreDay = datePickView.findViewById(R.id.btn_ignore_day);
        btnIgnoreDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDayIgnoreOrNot();
            }
        });
        btnConfirm = datePickView.findViewById(R.id.btn_date_picker_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmChooseDate();
            }
        });
    }

    public DatePickView(Context context) {
        this.context = context;
        this.activity = (OrderItemSearchActivity) context;
    }

    private void setYearIgnoreOrNot(){

        //未忽略,要忽略
        if(btnIgnoreYear.getCurrentTextColor() == context.getResources().getColor(R.color.primary_font)) {
            ifIgnoreYear = true;
            btnIgnoreYear.setTextColor(context.getResources().getColor(R.color.white));
            btnIgnoreYear.setBackgroundColor(context.getResources().getColor(R.color.blue));
        }
        //已忽略,不忽略
        else{
            ifIgnoreYear = false;
            btnIgnoreYear.setTextColor(context.getResources().getColor(R.color.black));
            btnIgnoreYear.setBackgroundColor(context.getResources().getColor(R.color.item_background));
        }
        setChildrenViewIfVisible();
    }

    private void setMonthIgnoreOrNot(){

        //未忽略,要忽略
        if(btnIgnoreMonth.getCurrentTextColor() == context.getResources().getColor(R.color.primary_font)) {
            ifIgnoreMonth = true;
            btnIgnoreMonth.setTextColor(context.getResources().getColor(R.color.white));
            btnIgnoreMonth.setBackgroundColor(context.getResources().getColor(R.color.blue));
        }
        //已忽略,不忽略
        else{
            ifIgnoreMonth = false;
            btnIgnoreMonth.setTextColor(context.getResources().getColor(R.color.black));
            btnIgnoreMonth.setBackgroundColor(context.getResources().getColor(R.color.item_background));
        }
        setChildrenViewIfVisible();
    }

    private void setDayIgnoreOrNot(){

        //未忽略,要忽略
        if(btnIgnoreDay.getCurrentTextColor() == context.getResources().getColor(R.color.primary_font)) {
            ifIgnoreDay = true;
            btnIgnoreDay.setTextColor(context.getResources().getColor(R.color.white));
            btnIgnoreDay.setBackgroundColor(context.getResources().getColor(R.color.blue));
        }
        //已忽略,不忽略
        else{
            ifIgnoreDay = false;
            btnIgnoreDay.setTextColor(context.getResources().getColor(R.color.black));
            btnIgnoreDay.setBackgroundColor(context.getResources().getColor(R.color.item_background));
        }
        setChildrenViewIfVisible();
    }

    private void setChildrenViewIfVisible(){
        View childrenYear = datePickerView.getChildAt(0);
        View childrenMonth = datePickerView.getChildAt(2);
        View childrenDay = datePickerView.getChildAt(4);
        boolean ifShowYear = !ifIgnoreYear;
        boolean ifShowMonth = (ifShowYear && !ifIgnoreMonth);
        boolean ifShowDay = (ifShowYear && ifShowMonth && !ifIgnoreDay);
        childrenYear.setVisibility(ifShowYear?View.VISIBLE:View.GONE);
        childrenMonth.setVisibility(ifShowMonth?View.VISIBLE:View.GONE);
        childrenDay.setVisibility(ifShowDay?View.VISIBLE:View.GONE);
    }

    private void confirmChooseDate(){
        String date = datePickerView.getDateString();
        //获取年月日
        //将date按照-分割
        String[] dateArray = date.split("-");
        int year,month,day;
        //找全部账单
        if(ifIgnoreYear){
            year = 0;
            month = 0;
            day = 0;
        }
        //找某年
        else if(ifIgnoreMonth){
            year = Integer.parseInt(dateArray[0]);
            month = 0;
            day = 0;
        }
        //找某月
        else if(ifIgnoreDay){
            year = Integer.parseInt(dateArray[0]);
            month = Integer.parseInt(dateArray[1]);
            day = 0;
        }
        //找某日
        else{
            year = Integer.parseInt(dateArray[0]);
            month = Integer.parseInt(dateArray[1]);
            day = Integer.parseInt(dateArray[2]);
        }
        //保存到单例配置中
        if(compareDate(formatDate(year,month,day),getCurrentDate())){
            ProjectUtil.toastMsg(context,"日期不能大于当前日期");
            return;
        }
        searchConditionEntity.setYear(year);
        searchConditionEntity.setMonth(month);
        searchConditionEntity.setDay(day);
        searchConditionEntity.setIfIgnoreDay(ifIgnoreDay);
        searchConditionEntity.setIfIgnoreMonth(ifIgnoreMonth);
        searchConditionEntity.setIfIgnoreYear(ifIgnoreYear);
        ProjectUtil.toastMsg(context,"已选择日期");
        //关闭菜单
        activity.closeMenu();
    }

    private void recoverFromSearchConditionEntity(){
        //配置数据
        ifIgnoreYear = searchConditionEntity.isIfIgnoreYear();
        ifIgnoreMonth = searchConditionEntity.isIfIgnoreMonth();
        ifIgnoreDay = searchConditionEntity.isIfIgnoreDay();
        //控制按钮颜色
        if(ifIgnoreYear){
            btnIgnoreYear.setTextColor(context.getResources().getColor(R.color.white));
            btnIgnoreYear.setBackgroundColor(context.getResources().getColor(R.color.blue));
        }
        else{
            btnIgnoreYear.setTextColor(context.getResources().getColor(R.color.black));
            btnIgnoreYear.setBackgroundColor(context.getResources().getColor(R.color.item_background));
        }
        if(ifIgnoreMonth){
            btnIgnoreMonth.setTextColor(context.getResources().getColor(R.color.white));
            btnIgnoreMonth.setBackgroundColor(context.getResources().getColor(R.color.blue));
        }
        else{
            btnIgnoreMonth.setTextColor(context.getResources().getColor(R.color.black));
            btnIgnoreMonth.setBackgroundColor(context.getResources().getColor(R.color.item_background));
        }
        if(ifIgnoreDay){
            btnIgnoreDay.setTextColor(context.getResources().getColor(R.color.white));
            btnIgnoreDay.setBackgroundColor(context.getResources().getColor(R.color.blue));
        }
        else{
            btnIgnoreDay.setTextColor(context.getResources().getColor(R.color.black));
            btnIgnoreDay.setBackgroundColor(context.getResources().getColor(R.color.item_background));
        }
        //控制日期选择器的显示
        setChildrenViewIfVisible();
        //设置日期选择器的日期
        int year = searchConditionEntity.getYear();
        int month = searchConditionEntity.getMonth();
        int day = searchConditionEntity.getDay();
        if(year == 0){
            datePickerView.setDate("2001-03-15",getCurrentDate(),getCurrentDate());
        }
        else if(month == 0){
            datePickerView.setDate("2001-03-15",getCurrentDate(),formatDate(year,ProjectUtil.getCurrentMonth(),ProjectUtil.getCurrentDay()));
        }
        else if(day == 0){
            datePickerView.setDate("2001-03-15",getCurrentDate(),formatDate(year,month,ProjectUtil.getCurrentDay()));
        }
        else{
            datePickerView.setDate("2001-03-15",getCurrentDate(),formatDate(year,month,day));
        }
    }

    //获取当前的日期
    private String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return year+"-"+month+"-"+day;
    }

    //格式化日期
    private String formatDate(int year,int month,int day){
        String monthStr = month+"";
        String dayStr = day+"";
        if(month < 10){
            monthStr = "0"+month;
        }
        if(day < 10){
            dayStr = "0"+day;
        }
        return year+"-"+monthStr+"-"+dayStr;
    }

    //比较两个日期的大小
    private boolean compareDate(String date1,String date2){
        //将date按照-分割
        String[] dateArray1 = date1.split("-");
        String[] dateArray2 = date2.split("-");
        int year1 = Integer.parseInt(dateArray1[0]);
        int month1 = Integer.parseInt(dateArray1[1]);
        int day1 = Integer.parseInt(dateArray1[2]);
        int year2 = Integer.parseInt(dateArray2[0]);
        int month2 = Integer.parseInt(dateArray2[1]);
        int day2 = Integer.parseInt(dateArray2[2]);
        if(year1 > year2){
            return true;
        }
        else if(year1 == year2){
            if(month1 > month2){
                return true;
            }
            else if(month1 == month2){
                if(day1 > day2){
                    return true;
                }
            }
        }
        return false;
    }
}
