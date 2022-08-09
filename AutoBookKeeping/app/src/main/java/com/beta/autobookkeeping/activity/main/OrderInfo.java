package com.beta.autobookkeeping.activity.main;

public class OrderInfo {
    private String time,bankName,orderRemark,costType,user;
    private double money;
    public OrderInfo(String time,String bankName,String orderRemark,double money,String costType,String user){
        this.bankName = bankName;
        this.orderRemark = orderRemark;
        this.money = money;
        this.time = time;
        this.costType = costType;
        this.user = user;
    }
    public String getTvOrderTypeAndRemark(){
            return costType+' '+orderRemark;
    }
    public String getTvPayWay(){
        return bankName;
    }
    public String getTvOrderMoney(){
        return String.valueOf(money)+'元';
    }
    public String getTvOrderTime(){
        return time;
    }
    public String getUser(){return user;}
}
