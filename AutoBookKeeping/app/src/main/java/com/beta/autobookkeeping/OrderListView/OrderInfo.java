package com.beta.autobookkeeping.OrderListView;

public class OrderInfo {
    private String time,bankName,orderRemark;
    private double money;
    public OrderInfo(String time,String bankName,String orderRemark,double money){
        this.bankName = bankName;
        this.orderRemark = orderRemark;
        this.money = money;
        this.time = time;
    }
    public String getTime(){
        return time;
    }
}
