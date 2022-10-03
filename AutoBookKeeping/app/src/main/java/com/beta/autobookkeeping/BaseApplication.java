package com.beta.autobookkeeping;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.hss01248.dialog.ActivityStackManager;
import com.hss01248.dialog.StyledDialog;

public class BaseApplication extends Application {

    @Override
    //先将数据设置为空
    public void onCreate() {
        StyledDialog.init(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
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
