package com.beta.autobookkeeping.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.RemoteInput;

import Util.ProjectUtil;

public class AutoBillNotificationActionReceiver extends BroadcastReceiver {
    public static final String ACTION_UNDO_AUTO_BILL = "com.beta.autobookkeeping.action.UNDO_AUTO_BILL";
    public static final String ACTION_ADD_AUTO_BILL_REMARK = "com.beta.autobookkeeping.action.ADD_AUTO_BILL_REMARK";
    public static final String ACTION_UPDATE_AUTO_BILL_CATEGORY = "com.beta.autobookkeeping.action.UPDATE_AUTO_BILL_CATEGORY";
    public static final String ACTION_SHOW_AUTO_BILL_CATEGORIES = "com.beta.autobookkeeping.action.SHOW_AUTO_BILL_CATEGORIES";
    public static final String ACTION_SHOW_AUTO_BILL_MAIN = "com.beta.autobookkeeping.action.SHOW_AUTO_BILL_MAIN";
    public static final String EXTRA_ORDER_ID = "orderId";
    public static final String EXTRA_NOTIFICATION_ID = "notificationId";
    public static final String EXTRA_YEAR = "year";
    public static final String EXTRA_MONTH = "month";
    public static final String EXTRA_DAY = "day";
    public static final String EXTRA_CLOCK = "clock";
    public static final String EXTRA_MONEY = "money";
    public static final String EXTRA_BANK_NAME = "bankName";
    public static final String EXTRA_ORDER_REMARK = "orderRemark";
    public static final String EXTRA_COST_TYPE = "costType";
    public static final String EXTRA_SELECTED_COST_TYPE = "selectedCostType";
    public static final String EXTRA_CATEGORY_PAGE = "categoryPage";
    public static final String KEY_TEXT_REPLY = "auto_bill_remark";
    public static final String KEY_CATEGORY_REPLY = "auto_bill_category";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        if (ACTION_UNDO_AUTO_BILL.equals(intent.getAction())) {
            ProjectUtil.undoAutoBillFromNotification(context, intent);
            return;
        }
        if (ACTION_ADD_AUTO_BILL_REMARK.equals(intent.getAction())) {
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            CharSequence reply = remoteInput == null ? "" : remoteInput.getCharSequence(KEY_TEXT_REPLY);
            ProjectUtil.updateAutoBillRemarkFromNotification(context, intent, reply == null ? "" : reply.toString());
            return;
        }
        if (ACTION_UPDATE_AUTO_BILL_CATEGORY.equals(intent.getAction())) {
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            CharSequence reply = intent.getStringExtra(EXTRA_SELECTED_COST_TYPE);
            if ((reply == null || reply.length() == 0) && remoteInput != null) {
                reply = remoteInput.getCharSequence(KEY_CATEGORY_REPLY);
            }
            ProjectUtil.updateAutoBillCategoryFromNotification(context, intent, reply == null ? "" : reply.toString());
            return;
        }
        if (ACTION_SHOW_AUTO_BILL_CATEGORIES.equals(intent.getAction())) {
            ProjectUtil.showAutoBillCategoryNotificationFromAction(context, intent);
            return;
        }
        if (ACTION_SHOW_AUTO_BILL_MAIN.equals(intent.getAction())) {
            ProjectUtil.showAutoBillMainNotificationFromAction(context, intent);
        }
    }
}
