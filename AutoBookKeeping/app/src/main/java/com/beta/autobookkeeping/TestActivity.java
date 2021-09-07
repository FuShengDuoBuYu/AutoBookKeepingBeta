package com.beta.autobookkeeping;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestActivity {
    public static void main(String[] args) {
        String msg1 = "您尾号1276的储蓄卡9月6日0时1分消费支出人民币50.00元,活期余额440.04元。[建设银行]";
        String msg2 = "【中国农业银行】您尾号8475账户09月06日17:36完成支付宝交易人民币-11.00，余额39.99。";
        String msg3 = "【郑州银行】03月25日17:57，您尾号为1758的卡,财付通,支出人民币25.00元,余额159.87元。";
        String regExBank = "[(农业银行)|(建设银行)|(郑州银行)|(工商银行)|(交通银行)]";
        Matcher matchBank = Pattern.compile(regExBank).matcher(msg3);
        String regExMoneyType = "[-|出|入]";
        Matcher matchMoneyType = Pattern.compile(regExMoneyType).matcher(msg2);

        String regExMoney = "([1-9]\\d*\\.\\d*)?";
        Matcher matchMoney = Pattern.compile(regExMoney).matcher(msg2);
    }
    public static String getString(Matcher matcher){
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            sb.append(matcher.group());
            if(!sb.toString().equals("")){
                break;
            }
        }
        String result = sb.toString();
        if(result.equals("出")||result.equals("-")){
            return "支出";
        }else if(result.equals("入")){
            return "收入";
        }
        return result;
    }
}

