package com.beta.autobookkeeping.activity.orderItemSearch.items;

import static Util.ConstVariable.FAMILY_MODE;
import static Util.ConstVariable.PERSONAL_MODE;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.beta.autobookkeeping.activity.orderItemSearch.OrderItemSearchActivity;

import java.util.ArrayList;
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
    private String searchCostType = "";
    private String user = "";

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

    private boolean ifIgnoreYear = false,ifIgnoreMonth = false,ifIgnoreDay = false;
    public List<View> getPopupViews() {
        List<View> popupViews = new ArrayList<>();
        popupViews.add(new VersionItemView(context).getVersionItemView());
        LinearLayout linearLayout1 = new LinearLayout(context);
        linearLayout1.addView(new Button(context));
        popupViews.add(new DatePickView(context).getDatePickView());
        popupViews.add(linearLayout1);
        return popupViews;
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

    public String getSearchCostType() {
        return searchCostType;
    }

    public void setSearchCostType(String searchCostType) {
        this.searchCostType = searchCostType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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
}
