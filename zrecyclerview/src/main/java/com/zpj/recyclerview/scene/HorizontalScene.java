


package com.zpj.recyclerview.scene;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.core.MultiScene;
import com.zpj.recyclerview.layouter.HorizontalLayouter;
import com.zpj.recyclerview.layouter.Layouter;

public class HorizontalScene extends MultiScene {

    private static final String TAG = "HorizontalLayouter";

    protected boolean mIsInfinite = true;

    public HorizontalScene(MultiData<?> multiData) {
        this(multiData, new HorizontalLayouter(), false);
    }

    public HorizontalScene(MultiData<?> multiData, boolean isInfinite) {
        this(multiData, new HorizontalLayouter(), isInfinite);
    }

    public HorizontalScene(MultiData<?> multiData, HorizontalLayouter layouter, boolean isInfinite) {
        super(multiData, layouter);
        mIsInfinite = isInfinite;
    }

    public void setIsInfinite(boolean isInfinite) {
        this.mIsInfinite = isInfinite;
    }

    public boolean isInfinite() {
        return mIsInfinite;
    }

//    @Override
//    public void saveState(int firstPosition, int firstOffset) {
//        if (isInfinite()) {
//            this.mFirstPosition = Math.max(0, firstPosition - mPositionOffset);
//            this.mFirstOffset = Math.min(0, firstOffset);
//        } else {
//            Log.d(TAG, "saveState firstPosition=" + firstPosition + " firstOffset=" + firstOffset);
//            super.saveState(firstPosition, firstOffset);
//        }
//    }

    @Override
    public void saveState(View firstChild) {
        super.saveState(firstChild);
        if (isInfinite()) {
            mAnchorInfo.x = Math.min(0, mAnchorInfo.x);
        }
    }

    @Override
    public void onDetached() {
        if (isInfinite()) {
            if (mFlinger != null) {
                mFlinger.stop();
            }
        } else {
            super.onDetached();
        }
    }

    @Override
    public boolean onTouchUp(float velocityX, float velocityY, MotionEvent event) {
        if (isInfinite()) {
            if (canScrollHorizontally() && mFlinger != null) {
                mFlinger.fling(velocityX, velocityY);
            }
            return false;
        }
        return super.onTouchUp(velocityX, velocityY, event);
    }
}
