package com.beta.autobookkeeping.activity.orderItemSearch.items;

import static Util.ConstVariable.PERSONAL_MODE;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.beta.autobookkeeping.activity.orderItemSearch.items.dropdownViews.DatePickView;
import com.beta.autobookkeeping.activity.orderItemSearch.items.dropdownViews.VersionItemView;
import com.beta.autobookkeeping.activity.orderItemSearch.items.dropdownViews.otherDescriptionView.OtherDescriptionView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Util.ProjectUtil;

public class SearchConditionEntity {
    private final static SearchConditionEntity INSTANCE = new SearchConditionEntity();
    private Context context;
    private String mode = PERSONAL_MODE;
    private Integer year = ProjectUtil.getCurrentYear();
    private Integer month = ProjectUtil.getCurrentMonth();
    private Integer day = ProjectUtil.getCurrentDay();
    private String searchOrderRemark = "";
    private String[] searchCostType = {};
    private boolean ifIgnoreYear = false,ifIgnoreMonth = false,ifIgnoreDay = false;

    public List<View> getPopupViews() {
        List<View> popupViews = new ArrayList<>();
        popupViews.add(new VersionItemView(context).getVersionItemView());
        popupViews.add(new DatePickView(context).getDatePickView());
        popupViews.add(new OtherDescriptionView(context).getDescriptionView());
        return popupViews;
    }

    //打印搜索条件
    public void printSearchCondition(){
        Log.d("SearchConditionEntity", "printSearchCondition: mode = " + mode);
        Log.d("SearchConditionEntity", "printSearchCondition: year = " + year);
        Log.d("SearchConditionEntity", "printSearchCondition: month = " + month);
        Log.d("SearchConditionEntity", "printSearchCondition: day = " + day);
        Log.d("SearchConditionEntity", "printSearchCondition: searchOrderRemark = " + searchOrderRemark);
        Log.d("SearchConditionEntity", "printSearchCondition: searchCostType = " + Arrays.toString(searchCostType));
        Log.d("SearchConditionEntity", "printSearchCondition: ifIgnoreYear = " + ifIgnoreYear);
        Log.d("SearchConditionEntity", "printSearchCondition: ifIgnoreMonth = " + ifIgnoreMonth);
        Log.d("SearchConditionEntity", "printSearchCondition: ifIgnoreDay = " + ifIgnoreDay);

    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public SearchConditionEntity() {

    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getSearchOrderRemark() {
        return searchOrderRemark;
    }

    public void setSearchOrderRemark(String searchOrderRemark) {
        this.searchOrderRemark = searchOrderRemark;
    }

    public String[] getSearchCostType() {
        //转为String
//        Log.d("SearchConditionEntity", "getSearchCostType: " + Arrays.toString(searchCostType));
        return searchCostType;
    }

    public void setSearchCostType(String[] searchCostType) {
        this.searchCostType = searchCostType;
    }

    public static SearchConditionEntity getINSTANCE() {
        return INSTANCE;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isIfIgnoreYear() {
        return ifIgnoreYear;
    }

    public void setIfIgnoreYear(boolean ifIgnoreYear) {
        this.ifIgnoreYear = ifIgnoreYear;
    }

    public boolean isIfIgnoreMonth() {
        return ifIgnoreMonth;
    }

    public void setIfIgnoreMonth(boolean ifIgnoreMonth) {
        this.ifIgnoreMonth = ifIgnoreMonth;
    }

    public boolean isIfIgnoreDay() {
        return ifIgnoreDay;
    }

    public void setIfIgnoreDay(boolean ifIgnoreDay) {
        this.ifIgnoreDay = ifIgnoreDay;
    }

    @Override
    public String toString() {
        //年月日为0则不显示
        String yearStr = year == 0 ? "" : year + "年";
        String monthStr = month == 0 ? "" : month + "月";
        String dayStr = day == 0 ? "" : day + "日";
        return mode + " " + yearStr + monthStr + dayStr  + " " + Arrays.toString(searchCostType)+ " " + searchOrderRemark;
    }

    public String getDate(){
        //年月日为0则不显示
        String yearStr = year == 0 ? "" : year + "年";
        String monthStr = month == 0 ? "" : month + "月";
        String dayStr = day == 0 ? "" : day + "日";
        return yearStr + monthStr + dayStr;
    }

    public String getOtherDescription(){
        return Arrays.toString(searchCostType)+ " 关键字:" + searchOrderRemark;
    }
}
