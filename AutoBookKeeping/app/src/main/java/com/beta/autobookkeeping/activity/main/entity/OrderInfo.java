package com.beta.autobookkeeping.activity.main.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderInfo implements Parcelable {
    private int id,year,month,day;
    private String clock,bankName,orderRemark,costType,user;
    private double money;

    public int getId() {
        return id;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public String getClock() {
        return clock;
    }

    public String getBankName() {
        return bankName;
    }

    public String getOrderRemark() {
        return orderRemark;
    }

    public String getCostType() {
        return costType;
    }

    public String getUser() {
        return user;
    }

    public double getMoney() {
        return money;
    }

    public OrderInfo(int id, int year, int month, int day, String clock, double money, String bankName, String orderRemark, String costType, String user) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.day = day;
        this.clock = clock;
        this.bankName = bankName;
        this.orderRemark = orderRemark;
        this.costType = costType;
        this.user = user;
        this.money = money;
    }

    protected OrderInfo(Parcel in) {
        id = in.readInt();
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
        clock = in.readString();
        bankName = in.readString();
        orderRemark = in.readString();
        costType = in.readString();
        user = in.readString();
        money = in.readDouble();
    }

    public static final Creator<OrderInfo> CREATOR = new Creator<OrderInfo>() {
        @Override
        public OrderInfo createFromParcel(Parcel in) {
            return new OrderInfo(in);
        }

        @Override
        public OrderInfo[] newArray(int size) {
            return new OrderInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(year);
        parcel.writeInt(month);
        parcel.writeInt(day);
        parcel.writeString(clock);
        parcel.writeString(bankName);
        parcel.writeString(orderRemark);
        parcel.writeString(costType);
        parcel.writeString(user);
        parcel.writeDouble(money);
    }
}
