package Util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

public class NotificationBillParserTest {
    @Test
    public void parsesAlipayExpenseOffline() {
        NotificationBillParser.ParsedBill bill = NotificationBillParser.parse(
                "com.eg.android.AlipayGphone",
                "交易提醒",
                "你有一笔0.10元的支出",
                "交易提醒\n你有一笔0.10元的支出",
                ""
        );

        assertNotNull(bill);
        assertEquals(-0.10, bill.money, 0.001);
        assertEquals("支付宝", bill.bankName);
        assertEquals("消费", bill.costType);
    }

    @Test
    public void parsesWechatPayment() {
        NotificationBillParser.ParsedBill bill = NotificationBillParser.parse(
                "com.tencent.mm",
                "微信支付",
                "付款金额¥23.45",
                "微信支付\n付款金额¥23.45",
                ""
        );

        assertNotNull(bill);
        assertEquals(-23.45, bill.money, 0.001);
        assertEquals("微信", bill.bankName);
    }

    @Test
    public void onlyRecordsConfiguredXiaohebaoMember() {
        NotificationBillParser.ParsedBill ownBill = NotificationBillParser.parse(
                "com.eg.android.AlipayGphone",
                "小荷包资金变动提醒",
                "小林消费了12.80元",
                "小荷包资金变动提醒\n小林消费了12.80元",
                "小林"
        );
        NotificationBillParser.ParsedBill otherMemberBill = NotificationBillParser.parse(
                "com.eg.android.AlipayGphone",
                "小荷包资金变动提醒",
                "小王消费了12.80元",
                "小荷包资金变动提醒\n小王消费了12.80元",
                "小林"
        );

        assertNotNull(ownBill);
        assertNull(otherMemberBill);
    }

    @Test
    public void rejectsPendingOrFailedPayments() {
        assertNull(NotificationBillParser.parse(
                "com.eg.android.AlipayGphone",
                "交易提醒",
                "订单待支付20.00元",
                "交易提醒\n订单待支付20.00元",
                ""
        ));
    }

    @Test
    public void parsesIncomeAsPositive() {
        NotificationBillParser.ParsedBill bill = NotificationBillParser.parse(
                "com.unionpay",
                "动账提醒",
                "银行卡入账88.00元",
                "动账提醒\n银行卡入账88.00元",
                ""
        );

        assertNotNull(bill);
        assertEquals(88.00, bill.money, 0.001);
        assertEquals("收入", bill.costType);
    }
}
