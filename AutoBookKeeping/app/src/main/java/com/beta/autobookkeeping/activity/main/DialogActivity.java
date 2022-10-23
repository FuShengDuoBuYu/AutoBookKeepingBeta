package com.beta.autobookkeeping.activity.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.beta.autobookkeeping.R;

public class DialogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main_dialog);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        LinearLayout layout = (LinearLayout) findViewById(R.id.dialog_parent);
//        layout.getLayoutParams().width = screenWidth / 2;
//        layout.getLayoutParams().height = screenWidth / 2;
//        layout.requestLayout();
        layout.setOnClickListener(v->{
            finishAfterTransition();
        });
    }
}
