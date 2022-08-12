package com.beta.autobookkeeping.activity.main.entity;

public class OrderDayItems {
    private int year,month,day;
    private String category;
    private int orderNums;
    private double dayOfMoney;

    public OrderDayItems(int year,int month, int day, String category, int orderNums, double dayOfMoney) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.category = category;
        this.orderNums = orderNums;
        this.dayOfMoney = dayOfMoney;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getOrderNums() {
        return orderNums;
    }

    public void setOrderNums(int orderNums) {
        this.orderNums = orderNums;
    }

    public double getDayOfMoney() {
        return dayOfMoney;
    }

    public void setDayOfMoney(double dayOfMoney) {
        this.dayOfMoney = dayOfMoney;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
