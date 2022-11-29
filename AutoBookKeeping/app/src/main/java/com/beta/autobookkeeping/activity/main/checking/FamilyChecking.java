package com.beta.autobookkeeping.activity.main.checking;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.beta.autobookkeeping.activity.presonalInfo.PersonlInfoActivity;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;

import Util.ProjectUtil;
import Util.SpUtils;

public class FamilyChecking {
    //检验是否有family属性
    public static void checkFamily(Context context){
        if(SpUtils.get(context,"familyId","")==null || SpUtils.get(context,"familyId","").equals("")){
//            StyledDialog.buildIosAlert("家庭设置", "检测到您还未设置家庭信息,是否现在前往设置?", new MyDialogListener() {
//                @Override
//                public void onFirst() {
//                    Intent intent = new Intent(context, PersonlInfoActivity.class);
//                    context.startActivity(intent);
//                }
//
//                @Override
//                public void onSecond() {
//                    ProjectUtil.toastMsg(context,"一会");
//                }
//            }).setBtnText("现在就去","稍后再去").show();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("家庭设置");
            builder.setMessage("检测到您还未设置家庭信息,是否现在前往设置?");
            builder.setPositiveButton("现在就去", (dialog, which) -> {
                Intent intent = new Intent(context, PersonlInfoActivity.class);
                context.startActivity(intent);
            });
            builder.setNegativeButton("稍后再去", (dialog, which) -> {
                ProjectUtil.toastMsg(context,"一会");
            });
            builder.show();
        }
    }
}
