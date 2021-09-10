package com.zpj.recyclerview.refresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zpj.recyclerview.R;

public class SimpleRefresher extends AbsRefresher {

    private View view;
    private TextView mTvMsg;
    private View mLoadingView;
    private int mHeight;
    private ValueAnimator mAnimator;

    @Override
    public void setState(int state) {
        super.setState(state);
        if (state == STATE_REFRESHING) {
            mLoadingView.setVisibility(View.VISIBLE);
            mTvMsg.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onMove(float delta) {
        super.onMove(delta);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = Math.min((int) (delta / 2), 3 * mHeight);
        view.setLayoutParams(params);
        if (params.height < mHeight) {
            showText(R.string.text_pull_to_refresh);
        } else {
            showText(R.string.text_release_to_refresh);
        }
    }

    @Override
    public View onCreateRefreshView(Context context, ViewGroup parent) {
        mHeight = (int) (56 * context.getResources().getDisplayMetrics().density);
        view = LayoutInflater.from(context).inflate(R.layout.easy_base_footer, null);
        mLoadingView = view.findViewById(R.id.ll_container_progress);
        mTvMsg = view.findViewById(R.id.tv_msg);

        FrameLayout container = new FrameLayout(context);
        container.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return container;
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
        } else if (view.getHeight() == mHeight) {
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
        showText(R.string.text_success_to_refresh);
        startAnimate(0, new Runnable() {
            @Override
            public void run() {
                setState(STATE_NORMAL);
            }
        });
    }

    private void showText(int res) {
        mTvMsg.setVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.INVISIBLE);
        mTvMsg.setText(res);
    }

    private void startAnimate(int height, final Runnable runnable) {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        mAnimator = ValueAnimator.ofInt(view.getHeight(), height);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = (int) (animation.getAnimatedValue());
                view.setLayoutParams(params);
                mDelta = params.height * 2;
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        mAnimator.setDuration(300);
        mAnimator.start();
    }


}
