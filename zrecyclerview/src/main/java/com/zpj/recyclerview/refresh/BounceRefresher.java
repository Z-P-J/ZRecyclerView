package com.zpj.recyclerview.refresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 实现RecyclerView弹性回弹
 */
public class BounceRefresher extends AbsRefresher {

    private FrameLayout container;
    private ValueAnimator mAnimator;

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
    }

    @Override
    public void setState(int state) {
    }

    @Override
    public int getState() {
        return STATE_NORMAL;
    }

    @Override
    public void onMove(float delta) {
        super.onMove(delta);
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        ViewGroup.LayoutParams params = container.getLayoutParams();
        params.height = (int) (delta / 3);
        container.setLayoutParams(params);
    }

    @Override
    public float getDelta() {
        return mDelta;
    }

    @Override
    public View onCreateRefreshView(Context context, ViewGroup parent) {
        container = new FrameLayout(context);
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return container;
    }

    @Override
    public boolean onRelease() {
        if (0 == container.getHeight()) {
            return false;
        }
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofInt(container.getHeight(), 0);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
                params.height = (int) (animation.getAnimatedValue());
                container.setLayoutParams(params);
                mDelta = params.height * 3;
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setState(STATE_NORMAL);
            }
        });
        mAnimator.setDuration(500);
        mAnimator.start();
        return false;
    }

    @Override
    public void stopRefresh() {
    }



}
