package com.zpj.recyclerview.refresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.zpj.recyclerview.R;

/**
 * 仿SwipeRefreshLayout效果
 */
public class SwipeDecorationRefresher extends DecorationRefresher {

    private static final float CIRCULAR = 360f;

    private final Paint mPaint = new Paint();
    private final Paint mShadowPaint = new Paint();
    private final RectF mRect = new RectF();

    private final float mMinProgress = 20f;
    private final float mMaxProgress = 100f;
    private float mProgress;

    private float mStartAngle;

    private final float mBackgroundRadius;
    private final float mProgressRadius;
    private final float mMaxOffset;
    private final float mRefreshOffset;
    private float mOffset;

    private final ValueAnimator mProgressAnimator;
    private ValueAnimator mOffsetAnimator;

    public SwipeDecorationRefresher() {
        float density = Resources.getSystem().getDisplayMetrics().density;
        mBackgroundRadius = density * 18;
        mProgressRadius = density * 10;
        mMaxOffset = 6 * mBackgroundRadius;
        mRefreshOffset = 4 * mBackgroundRadius;

        mProgressAnimator = ValueAnimator.ofFloat(mMinProgress, mMaxProgress - mMinProgress, mMinProgress);
        mProgressAnimator.setDuration(1000);
        mProgressAnimator.setRepeatCount(-1);
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            private float tempProgress = mMinProgress;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                mProgress = progress;
                if (animation.getAnimatedFraction() > 0.5f) {
                    mStartAngle += (4 + (tempProgress - progress) / mMaxProgress * CIRCULAR);
                } else {
                    mStartAngle += 4;
                }
                tempProgress = progress;
                invalidate();
            }
        });

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBackgroundRadius / 6);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);

        mShadowPaint.setShadowLayer(8, 0, 0, Color.LTGRAY);
        mShadowPaint.setAntiAlias(true);
    }

    @Override
    protected void onAttachRecyclerView(RecyclerView recyclerView) {
        mPaint.setColor(getColor(mRecyclerView.getContext(), R.attr.colorPrimary, Color.BLACK));
    }

    public static int getColor(Context context, int colorAttrId, int defaultColor) {
        int[] ints = { colorAttrId };
        TypedArray typedArray = context.obtainStyledAttributes(ints);
        int color = typedArray.getColor(0, defaultColor);
        typedArray.recycle();
        return color;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state, float delta) {

    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state, float delta) {
        float cx = parent.getWidth() / 2f;
        float cy = -2 * mBackgroundRadius + mOffset;
        mShadowPaint.setColor(Color.WHITE);
        mShadowPaint.setStyle(Paint.Style.FILL);
        c.drawCircle(cx, cy, mBackgroundRadius, mShadowPaint);


        float left = cx - mProgressRadius;
        float top = cy - mProgressRadius;
        float right = cx + mProgressRadius;
        float bottom = cy + mProgressRadius;
        mRect.set(left, top, right, bottom);
        float angle = CIRCULAR / mMaxProgress * mProgress;
        c.drawArc(mRect, mStartAngle + angle, CIRCULAR - angle, false, mPaint);
    }

    @Override
    public void onDown() {
        if (mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }
        if (mOffsetAnimator != null && mOffsetAnimator.isRunning()) {
            mOffsetAnimator.cancel();
        }
    }

    @Override
    public void onMove(float delta) {
        super.onMove(delta);
        if (mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }
        if (mOffsetAnimator != null && mOffsetAnimator.isRunning()) {
            mOffsetAnimator.cancel();
        }
        if (delta < 0) {
            return;
        }
        mOffset = Math.min(mMaxOffset, delta / 2);
        float percent = Math.min(1f, mOffset / mMaxOffset);
        mProgress = (1 - percent) * mMaxProgress + (2 * percent - 1) * mMinProgress;
        float angle = CIRCULAR / mMaxProgress * mProgress;
        mStartAngle = -angle - 180 * (1 - percent);
        invalidate();
    }

    @Override
    public boolean onRelease() {
        if (mOffset > mRefreshOffset) {
            if (mOffsetAnimator != null) {
                mOffsetAnimator.cancel();
            }
            mOffsetAnimator = ValueAnimator.ofFloat(mOffset, mRefreshOffset);
            mOffsetAnimator.setDuration(200);
            final float deltaProgress = mProgress - mMinProgress;
            mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mOffset = (float) animation.getAnimatedValue();
                    mDelta = mOffset * 2;
                    float percent = animation.getAnimatedFraction();
                    mProgress = mMinProgress - deltaProgress * (1 - percent);
                    invalidate();
                }
            });
            mOffsetAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setState(STATE_REFRESHING);
                    mProgressAnimator.start();
                }
            });
            mOffsetAnimator.start();
        } else {
            stopRefresh();
        }
        return false;
    }

    @Override
    public void stopRefresh() {
        if (mOffsetAnimator != null) {
            mOffsetAnimator.cancel();
        }
        mOffsetAnimator = ValueAnimator.ofFloat(mOffset, 0);
        Log.d("stopRefresh", "mOffset=" + mOffset + " mRefreshOffset=" + mRefreshOffset + " value=" + (500 * mOffset / mRefreshOffset));
        mOffsetAnimator.setDuration((int) (500 * mOffset / mRefreshOffset));
        mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOffset = (float) animation.getAnimatedValue();
                mDelta = mOffset * 2;
                invalidate();
            }
        });
        mOffsetAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressAnimator.cancel();
                setState(STATE_NORMAL);
            }
        });
        mOffsetAnimator.start();
    }
}
