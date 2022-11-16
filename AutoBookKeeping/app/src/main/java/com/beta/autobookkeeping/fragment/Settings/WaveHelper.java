package com.beta.autobookkeeping.fragment.Settings;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.Toast;

import com.gelitenight.waveview.library.WaveView;

public class WaveHelper {
    private WaveView waveView;
    private Button waveBtn;

    private Animator.AnimatorListener animatorListener = new android.animation.Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(android.animation.Animator animation) {
            Log.d("WaveHelper", "onAnimationStart");
            waveBtn.setVisibility(Button.INVISIBLE);
        }

        @Override
        public void onAnimationEnd(android.animation.Animator animation) {
//                waveBtn.setEnabled(true);
            waveBtn.setVisibility(Button.VISIBLE);
        }

        @Override
        public void onAnimationCancel(android.animation.Animator animation) {

        }

        @Override
        public void onAnimationRepeat(android.animation.Animator animation) {

        }
    };

    public WaveHelper(WaveView waveView,Button waveBtn) {
        this.waveView = waveView;
        this.waveBtn = waveBtn;
    }

    public void initAnimator(float startHeight,float endHeight) {
        ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(
                waveView, "waveShiftRatio", 0f, 1f);
        waveShiftAnim.setRepeatCount(2);
        waveShiftAnim.setDuration(1000);
        waveShiftAnim.setInterpolator(new LinearInterpolator());

        ObjectAnimator waterLevelAnim = ObjectAnimator.ofFloat(
                waveView, "waterLevelRatio", startHeight,endHeight);
        waterLevelAnim.setDuration(2000);
        waterLevelAnim.setInterpolator(new DecelerateInterpolator());
        waveShiftAnim.start();

        ObjectAnimator amplitudeAnim = ObjectAnimator.ofFloat(
                waveView, "amplitudeRatio", 0.05f, 0f);
        amplitudeAnim.setRepeatCount(2);
        amplitudeAnim.setRepeatMode(ValueAnimator.REVERSE);
        amplitudeAnim.setDuration(1000);
        amplitudeAnim.setInterpolator(new LinearInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(animatorListener);
        animatorSet.playTogether(waveShiftAnim, waterLevelAnim, amplitudeAnim);
        animatorSet.start();
    }

    public void cancelAnimator(){
        waveView.clearAnimation();
    }

}
