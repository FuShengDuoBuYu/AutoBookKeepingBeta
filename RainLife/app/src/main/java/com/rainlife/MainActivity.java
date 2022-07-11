package com.rainlife;

import Util.Util;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.qmuiteam.qmui.alpha.QMUIAlphaTextView;
import com.rainlife.autobookkeeping.R;
import com.rainlife.autobookkeeping.R.id;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.Nullable;

public final class MainActivity extends AppCompatActivity implements View.OnTouchListener{
    TextView date,time_and_week;
    View autoBookKeepingFragment;
    LinearLayout totalLinearlayout,textTitle,container;
    ScrollView fragments;
    ImageView setBg;
    float pressX,pressY,currentX,currentY;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        findViewsAndInit();
    }

    private void findViewsAndInit(){
        date = findViewById(id.date);
        time_and_week = findViewById(id.time_and_week);
        date.setText(Util.getCurrentMonth()+"/"+Util.getCurrentDay());
        time_and_week.setText(Util.getCurrentHour() + ":" + Util.getCurrentMinute() + " / " + Util.getWeek(new Date(Util.getCurrentYear(), Util.getCurrentMonth(), Util.getCurrentDay())));
        textTitle = findViewById(id.text_title);
        autoBookKeepingFragment = findViewById(R.id.auto_book_keeping_fragment);
        fragments = findViewById(R.id.fragments);
        totalLinearlayout = findViewById(id.total_linearlayout);
        autoBookKeepingFragment.setOnTouchListener(this);
        fragments.setOnTouchListener(this);
        totalLinearlayout.setOnTouchListener(this);
        container = findViewById(R.id.container);
        setBg = findViewById(id.set_bg);
        setBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSystemImageChooser();
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            pressX = motionEvent.getRawX();
            pressY = motionEvent.getRawY();
        }
        if(motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            //当手指移动的时候
            currentX = motionEvent.getRawX();
            currentY = motionEvent.getRawY();
            if(currentY>pressY){
                LinearLayout.LayoutParams fragmentsLp = (LinearLayout.LayoutParams) fragments.getLayoutParams();
                fragmentsLp.setMargins(0,(int)((currentY-pressY)/2),0,0);
                LinearLayout.LayoutParams textTitleLp = (LinearLayout.LayoutParams) textTitle.getLayoutParams();
                textTitleLp.setMargins(0,(int)((currentY-pressY)/3),0,0);
                fragments.requestLayout();
                textTitle.requestLayout();
            }
        }
        //手指松开
        if (motionEvent.getAction()==MotionEvent.ACTION_UP){
            int titleTextCurrentY = (int)(currentY-pressY)/3;
            int fragmentsCurrentY = (int)(currentY-pressY)/2;
            LinearLayout.LayoutParams fragmentsLp = (LinearLayout.LayoutParams) fragments.getLayoutParams();
            fragmentsLp.setMargins(0,0,0,0);
            TransitionManager.beginDelayedTransition(fragments);
            fragments.requestLayout();
            LinearLayout.LayoutParams textTitleLp = (LinearLayout.LayoutParams) textTitle.getLayoutParams();
            textTitleLp.setMargins(0,0,0,0);
            TransitionManager.beginDelayedTransition(textTitle);
            textTitle.requestLayout();
        }
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //获取系统图片
    private void openSystemImageChooser(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 666);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK){
            Util.toastMsg(getBaseContext(),"获取图片不成功");
            return;
        }
        if(requestCode==666){
            Uri uri = data.getData();
            InputStream imageInputStream;
            ContentResolver contentResolver = getContentResolver();
            try {
                imageInputStream = contentResolver.openInputStream(uri);
                // 把输入流解析为 Bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(imageInputStream);
                //把bitmap转为drawable设为背景
                BitmapDrawable bg = new BitmapDrawable(bitmap);
                container.setBackground(bg);
                Util.toastMsg(getBaseContext(),"更换背景成功");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

