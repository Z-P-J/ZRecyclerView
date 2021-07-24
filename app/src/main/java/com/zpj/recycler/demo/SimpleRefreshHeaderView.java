package com.zpj.recycler.demo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.recyclerview.IRefresh;

public class SimpleRefreshHeaderView extends LinearLayout implements IRefresh {

    private static final int ROTATE_ANIM_DURATION = 180;

    private TextView tvRefreshTip;
    private ImageView mIvArrow;
    private ProgressBar mProgress;
    private LinearLayout mContainer;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;

    private int mState = STATE_NORMAL;
    private int mMeasuredHeight;

    private OnRefreshListener mListener;

    public SimpleRefreshHeaderView(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.simple_by_refresh_view, null);
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);

        mIvArrow = findViewById(R.id.iv_arrow);
        mProgress = findViewById(R.id.pb_progress);
        tvRefreshTip = findViewById(R.id.tv_refresh_tip);

        measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMeasuredHeight = getMeasuredHeight();

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);

        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);

        this.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void onMove(float delta) {
        if (getVisibleHeight() > 0 || delta > 0) {
            int visibleHeight = (int) delta / 2;
            if (mState == STATE_REFRESHING) {
                setVisibleHeight(visibleHeight + mMeasuredHeight);
            } else {
                setVisibleHeight(visibleHeight);
            }
            if (mState <= STATE_RELEASE_TO_REFRESH) {
                if (getVisibleHeight() > mMeasuredHeight) {
                    setState(STATE_RELEASE_TO_REFRESH);
                } else {
                    setState(STATE_NORMAL);
                }
            }
        }
    }

    @Override
    public boolean onRelease() {
        boolean isOnRefresh = false;

        if (getVisibleHeight() > mMeasuredHeight && mState < STATE_REFRESHING) {
            setState(STATE_REFRESHING);
            isOnRefresh = true;
        }
        int destHeight = 0;
        if (mState == STATE_REFRESHING) {
            // 处理刷新中时，让其定位到 destHeight 高度
            destHeight = mMeasuredHeight;
        }
        smoothScrollTo(destHeight);

        return isOnRefresh;
    }

    @Override
    public void stopRefresh() {
        if (mState == STATE_NORMAL) {
            return;
        }
        setState(STATE_NORMAL);
        smoothScrollTo(0);
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mListener = listener;
    }

    @Override
    public void setState(int state) {
        if (state == mState) {
            return;
        }

        tvRefreshTip.setVisibility(VISIBLE);
        if (state == STATE_REFRESHING) {
            // show progress
            mIvArrow.setVisibility(View.INVISIBLE);
            mProgress.setVisibility(View.VISIBLE);
        } else {
            // show arrow image
            mIvArrow.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.INVISIBLE);
        }

        switch (state) {
            case STATE_NORMAL:
                if (mState == STATE_RELEASE_TO_REFRESH) {
                    mIvArrow.startAnimation(mRotateDownAnim);
                } else if (mState == STATE_REFRESHING) {
                    mIvArrow.clearAnimation();
                }
                tvRefreshTip.setText(R.string.by_header_hint_normal);
                break;
            case STATE_RELEASE_TO_REFRESH:
                mIvArrow.clearAnimation();
                mIvArrow.startAnimation(mRotateUpAnim);
                tvRefreshTip.setText(R.string.by_header_hint_release);
                break;
            case STATE_REFRESHING:
                mIvArrow.clearAnimation();
                smoothScrollTo(mMeasuredHeight);
                tvRefreshTip.setText(R.string.by_refreshing);
                if (mListener != null) {
                    mListener.onRefresh(this);
                }
                break;
            case STATE_DONE:
                tvRefreshTip.setText(R.string.by_refresh_done);
                break;
            default:
        }
        mState = state;
    }

    private void smoothScrollTo(final int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
        animator.setDuration(300).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setVisibleHeight((int) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Toast.makeText(mContainer.getContext(), "onEnd destHeight=" + destHeight, Toast.LENGTH_SHORT).show();
                if (destHeight == 0) {
                    stopRefresh();
                }
            }
        });
        animator.start();
    }

    private void setVisibleHeight(int height) {
        if (height < 0) {
            height = 0;
        }
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getVisibleHeight() {
        return mContainer.getHeight();
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent) {
        return this;
    }

    @Override
    public View getView() {
        return this;
    }
}
