package com.zpj.recyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshViewHolder2 implements IRefresh {

    private int mState = STATE_NORMAL;
    private FrameLayout container;
    private View view;

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {

    }

    @Override
    public void setState(int state) {
        this.mState = state;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent) {
//        view = LayoutInflater.from(context).inflate(R.layout.easy_base_footer, null);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.TOP);
        TextView progressBar = new TextView(context);
        progressBar.setText("1111111111");
        linearLayout.addView(progressBar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
        view = linearLayout;
        container = new FrameLayout(context);
        container.addView(view);
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
//        container.setTranslationY(-1000);
        container.setClipChildren(false);
        return container;
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void onMove(float delta) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = (int) (delta / 2);
        view.setLayoutParams(params);
//        container.setTranslationY(delta / 2);
    }

    @Override
    public boolean onRelease() {
        ValueAnimator animator = ValueAnimator.ofInt(view.getHeight(), 0);
        animator.setDuration(300).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                onMove((int) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setState(STATE_NORMAL);
            }
        });
        animator.start();
        return false;
    }

    @Override
    public void stopRefresh() {
        setState(STATE_NORMAL);
    }

}
