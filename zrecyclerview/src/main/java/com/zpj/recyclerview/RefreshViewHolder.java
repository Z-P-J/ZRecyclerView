package com.zpj.recyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class RefreshViewHolder implements IRefresh {

    private int mState = STATE_NORMAL;
    private OnRefreshListener mListener;
    private FrameLayout container;
    private View view;
    private int mHeight;

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mListener = listener;
    }

    @Override
    public void setState(int state) {
        this.mState = state;
        if (mState == STATE_REFRESHING) {
            if (mListener != null) {
                mListener.onRefresh(this);
            }
        }
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent) {
        mHeight = (int) (56 * context.getResources().getDisplayMetrics().density);
        view = LayoutInflater.from(context).inflate(R.layout.easy_base_footer, null);
        container = new FrameLayout(context);
        container.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
    }

    @Override
    public boolean onRelease() {
        setState(STATE_RELEASE_TO_REFRESH);
        if (view.getHeight() > mHeight) {
            startAnimate(mHeight, new Runnable() {
                @Override
                public void run() {
                    setState(STATE_REFRESHING);
                }
            });
        } else if(view.getHeight() == mHeight) {
            setState(STATE_REFRESHING);
        } else {
            stopRefresh();
        }
        return false;
    }

    @Override
    public void stopRefresh() {
        if (mState == STATE_NORMAL) {
            return;
        }
        startAnimate(0, new Runnable() {
            @Override
            public void run() {
                setState(STATE_NORMAL);
            }
        });
    }

    private void startAnimate(int height, final Runnable runnable) {
        ValueAnimator animator = ValueAnimator.ofInt(view.getHeight(), height);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                onMove((int) animation.getAnimatedValue());

                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = (int) (animation.getAnimatedValue());
                view.setLayoutParams(params);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        animator.setDuration(300);
        animator.start();
    }



}
