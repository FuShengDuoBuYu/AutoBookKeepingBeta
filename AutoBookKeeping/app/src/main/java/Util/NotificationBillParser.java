package Util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Deterministic, offline parser for payment notifications. */
public final class NotificationBillParser {
    private static final Pattern IGNORE_PATTERN = Pattern.compile(
            "待支付|未支付|还未支付|支付失败|付款失败|扣款失败|交易关闭|自动取消|验证码|登录提醒|配置更新"
    );
    private static final Pattern EXPENSE_PATTERN = Pattern.compile(
            "支出|付款|支付|消费|扣款|转出|提现|买单|代扣"
    );
    private static final Pattern INCOME_PATTERN = Pattern.compile(
            "退款|退回|返还|收入|收款|到账|入账|转入"
    );
    private static final Pattern REFUND_PATTERN = Pattern.compile("退款|退回|返还");
    private static final Pattern CURRENCY_AMOUNT_PATTERN = Pattern.compile(
            "(?:(?:￥|¥|人民币|RMB|CNY)\\s*([0-9]+(?:,[0-9]{3})*(?:\\.\\d{1,2})?|[0-9]+(?:\\.\\d{1,2})?)|([0-9]+(?:,[0-9]{3})*(?:\\.\\d{1,2})?|[0-9]+(?:\\.\\d{1,2})?)\\s*(?:元|块|圆))"
    );
    private static final Pattern SEMANTIC_AMOUNT_PATTERN = Pattern.compile(
            "(?:金额|消费|支付|支出|收入|转入|转出|扣款|付款|收款|入账|提现|退款)[^0-9￥¥]{0,8}(?:￥|¥)?\\s*([0-9]+(?:,[0-9]{3})*(?:\\.\\d{1,2})?|[0-9]+(?:\\.\\d{1,2})?)"
    );

    private NotificationBillParser() {
    }

    public static ParsedBill parse(
            String packageName,
            String title,
            String notificationText,
            String readableText,
            String xiaohebaoNickname
    ) {
        String safePackage = safe(packageName);
        String safeTitle = safe(title);
        String safeNotificationText = safe(notificationText);
        String safeReadableText = safe(readableText);
        String compactText = safeReadableText.replaceAll("\\s+", "");
        if (compactText.isEmpty() || IGNORE_PATTERN.matcher(compactText).find()) {
            return null;
        }

        boolean isAlipay = "com.eg.android.AlipayGphone".equals(safePackage) || safePackage.startsWith("com.alipay.");
        boolean isWechat = "com.tencent.mm".equals(safePackage);
        if (compactText.contains("小荷包") && !isOwnXiaohebaoPayment(compactText, safeNotificationText, xiaohebaoNickname)) {
            return null;
        }

        boolean hasExpenseKeyword = EXPENSE_PATTERN.matcher(compactText).find();
        boolean hasIncomeKeyword = INCOME_PATTERN.matcher(compactText).find();
        boolean isRefund = REFUND_PATTERN.matcher(compactText).find();
        boolean matchesHistoricalTitle = (isWechat && safeTitle.contains("微信支付"))
                || (isAlipay && (safeTitle.contains("交易提醒") || safeTitle.contains("小荷包资金变动提醒")));
        if (!hasExpenseKeyword && !hasIncomeKeyword && !matchesHistoricalTitle) {
            return null;
        }

        ArrayList<Double> amounts = extractAmounts(safeReadableText);
        if (amounts.isEmpty()) {
            return null;
        }
        double amount = Math.abs(amounts.get(0));
        boolean income = isRefund || (hasIncomeKeyword && !hasExpenseKeyword);
        return new ParsedBill(income ? amount : -amount, inferPayWay(safePackage), income ? "收入" : "消费");
    }

    private static boolean isOwnXiaohebaoPayment(String compactText, String notificationText, String nickname) {
        String safeNickname = safe(nickname).trim();
        if (safeNickname.isEmpty()) {
            return false;
        }
        Pattern ownPayment = Pattern.compile(
                Pattern.quote(safeNickname) + "(?:消费了|支付了|付款了|支出了|消费|支付|付款|支出)"
        );
        return notificationText.trim().startsWith(safeNickname) || ownPayment.matcher(compactText).find();
    }

    private static ArrayList<Double> extractAmounts(String text) {
        ArrayList<Double> amounts = new ArrayList<>();
        addMatchedAmounts(amounts, text, CURRENCY_AMOUNT_PATTERN);
        addMatchedAmounts(amounts, text, SEMANTIC_AMOUNT_PATTERN);
        return amounts;
    }

    private static void addMatchedAmounts(ArrayList<Double> amounts, String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String value = firstNonEmptyGroup(matcher);
            try {
                double amount = Double.parseDouble(value.replace(",", ""));
                if (amount > 0 && !containsAmount(amounts, amount)) {
                    amounts.add(amount);
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static String firstNonEmptyGroup(Matcher matcher) {
        for (int index = 1; index <= matcher.groupCount(); index++) {
            String value = matcher.group(index);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private static boolean containsAmount(ArrayList<Double> amounts, double target) {
        for (double amount : amounts) {
            if (Math.abs(amount - target) < 0.01) {
                return true;
            }
        }
        return false;
    }

    private static String inferPayWay(String packageName) {
        if ("com.tencent.mm".equals(packageName)) {
            return "微信";
        }
        if ("com.eg.android.AlipayGphone".equals(packageName) || packageName.startsWith("com.alipay.")) {
            return "支付宝";
        }
        return "银行卡";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public static final class ParsedBill {
        public final double money;
        public final String bankName;
        public final String costType;

        ParsedBill(double money, String bankName, String costType) {
            this.money = money;
            this.bankName = bankName;
            this.costType = costType;
        }
    }
}
