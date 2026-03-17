package Util;

import com.beta.autobookkeeping.BuildConfig;

public class ConstVariable {
    public static final String IP = BuildConfig.API_BASE_URL;
    public static final String FAMILY_MODE = "家庭版";
    public static final String PERSONAL_MODE = "个人版";
    public static final String[] COST_TYPE = {"消费","饮食","交通","体育","聚会","娱乐","购物","通讯","红包","医疗","房租与水电","学习","其他"};
    public static final String[] PAY_WAY = {"银行卡","支付宝","微信","现金"};
    public static final String[] ORDER_TYPE = {"支出","收入"};
}
