package com.zpj.recyclerview.decoration;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

public class ShadowItemDecoration extends RecyclerView.ItemDecoration {

    private final Paint mPaint = new Paint();
    private final Rect mRect = new Rect();

    private LinearGradient mTopGradient;
    private LinearGradient mBottomGradient;

    private int mShadowHeight;

    private int tempWidth;
    private int tempHeight;

    public ShadowItemDecoration() {
        this.mShadowHeight = 0;
    }

    public ShadowItemDecoration(int shadowHeight) {
        this.mShadowHeight = shadowHeight;
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        boolean isChanged = parent.getWidth() != tempWidth;
        if (isChanged) {
            tempWidth = parent.getWidth();
        }

        if (mShadowHeight <= 0) {
            mShadowHeight = (int) (parent.getResources().getDisplayMetrics().density * 8);
        }

        if (parent.canScrollVertically(-1)) {
            if (mTopGradient == null || isChanged) {
                mTopGradient = new LinearGradient(
                        parent.getWidth() / 2f, 0,
                        parent.getWidth() / 2f, mShadowHeight,
                        Color.parseColor("#10000000"), Color.TRANSPARENT,
                        Shader.TileMode.MIRROR
                );
            }

            mPaint.setShader(mTopGradient);

            mRect.set(0, 0, parent.getWidth(), mShadowHeight);
            c.drawRect(mRect, mPaint);
        }



        if (parent.canScrollVertically(1)) {
            if (mBottomGradient == null || isChanged || tempHeight != parent.getHeight()) {
                tempHeight = parent.getHeight();
                mBottomGradient = new LinearGradient(
                        parent.getWidth() / 2f, parent.getHeight() - mShadowHeight,
                        parent.getWidth() / 2f, parent.getHeight(),
                        Color.TRANSPARENT, Color.parseColor("#10000000"),
                        Shader.TileMode.MIRROR
                );
            }

            mPaint.setShader(mBottomGradient);

            mRect.set(0, parent.getHeight() - mShadowHeight, parent.getWidth(), parent.getHeight());
            c.drawRect(mRect, mPaint);
            return;
        }



    }

}
