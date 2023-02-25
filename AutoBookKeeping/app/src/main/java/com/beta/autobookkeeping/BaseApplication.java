package com.beta.autobookkeeping;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.beta.autobookkeeping.activity.main.MainActivity;
import com.gyf.immersionbar.ImmersionBar;
import com.hss01248.dialog.ActivityStackManager;
import com.hss01248.dialog.StyledDialog;

import site.gemus.openingstartanimation.NormalDrawStrategy;
import site.gemus.openingstartanimation.OpeningStartAnimation;

public class BaseApplication extends Application {

    @Override
    //先将数据设置为空
    public void onCreate() {
        StyledDialog.init(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if(activity instanceof MainActivity){
                    ImmersionBar.with(activity).statusBarColor(R.color.blue).init();
                }
                else {
                    ImmersionBar.with(activity).fitsSystemWindows(true).statusBarColor(R.color.blue).init();
                }
                ActivityStackManager.getInstance().addActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                ActivityStackManager.getInstance().removeActivity(activity);
            }
        });
        super.onCreate();
    }
}
