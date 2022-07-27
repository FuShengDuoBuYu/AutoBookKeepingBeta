package com.beta.autobookkeeping.OrderListView;

public class OrderInfo {
    private String time,bankName,orderRemark,costType;
    private double money;
    public OrderInfo(String time,String bankName,String orderRemark,double money,String costType){
        this.bankName = bankName;
        this.orderRemark = orderRemark;
        this.money = money;
        this.time = time;
        this.costType = costType;
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
}
