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

    private int tempWidth;

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        boolean isChanged = parent.getWidth() != tempWidth;
        if (isChanged) {
            tempWidth = parent.getWidth();
        }

        if (!parent.canScrollVertically(-1)) {
            return;
        }

        if (mTopGradient == null || isChanged) {
            mTopGradient = new LinearGradient(
                    parent.getWidth() / 2f, 0,
                    parent.getWidth() / 2f, 30,
                    Color.parseColor("#10000000"), Color.TRANSPARENT,
                    Shader.TileMode.MIRROR
            );
        }

        mPaint.setShader(mTopGradient);

        mRect.set(0, 0, parent.getWidth(), 30);
        c.drawRect(mRect, mPaint);

        if (!parent.canScrollVertically(1)) {
            return;
        }

        if (mBottomGradient == null || isChanged) {
            mBottomGradient = new LinearGradient(
                    parent.getWidth() / 2f, 0,
                    parent.getWidth() / 2f, 30,
                    Color.TRANSPARENT, Color.parseColor("#10000000"),
                    Shader.TileMode.MIRROR
            );
        }

        mPaint.setShader(mBottomGradient);

        mRect.set(0, parent.getHeight() - 30, parent.getWidth(), parent.getHeight());
        c.drawRect(mRect, mPaint);

    }

}
