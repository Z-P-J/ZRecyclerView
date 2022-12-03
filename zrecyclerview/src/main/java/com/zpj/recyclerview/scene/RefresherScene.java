package com.zpj.recyclerview.scene;

import android.view.MotionEvent;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.layouter.VerticalLayouter;
import com.zpj.recyclerview.refresh.IRefresher;

public class RefresherScene extends VerticalScene {

    private final IRefresher mRefreshHeader;

    private float mDownX = -1;
    private float mDownY = -1;
    private float mOffset = 0;
    private boolean isMoveDown;

    public RefresherScene(MultiData<?> multiData, IRefresher refresher) {
        super(multiData);
        mRefreshHeader = refresher;
    }


    @Override
    public boolean onTouchDown(float downX, float downY, MotionEvent event) {
        isMoveDown = false;
        if (mRefreshHeader.getView() != null
                && mRefreshHeader.getView().getParent() != null
                && mRefreshHeader.getState() == IRefresher.STATE_NORMAL) {
            mDownX = downX;
            mDownY = downY;
            mOffset = mRefreshHeader.getDelta();
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchMove(float x, float y, float downX, float downY, MotionEvent event) {
        if (isMoveDown) {
            float deltaY = event.getY() - mDownY + mOffset;
            getLayoutHelper().stopInterceptRequestLayout();
            mRefreshHeader.onMove(deltaY);
            event.setAction(MotionEvent.ACTION_DOWN);
        } else if (mRefreshHeader.getView() != null
                && mRefreshHeader.getView().getParent() != null
                && mRefreshHeader.getState() == IRefresher.STATE_NORMAL) {
            if (mDownY < 0) {
                mDownY = event.getY();
                mOffset = mRefreshHeader.getDelta();
                mRefreshHeader.onDown();
                event.setAction(MotionEvent.ACTION_DOWN);
                return false;
            } else {
                float deltaY = event.getY() - mDownY + mOffset;
                if (deltaY > 0) {
                    isMoveDown = true;
                    getLayoutHelper().stopInterceptRequestLayout();
                    mRefreshHeader.onMove(deltaY);
                    event.setAction(MotionEvent.ACTION_DOWN);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onTouchUp(float velocityX, float velocityY, MotionEvent event) {
        if (isMoveDown) {
            isMoveDown = false;
            mDownX = -1;
            mDownY = -1;
            mOffset = 0;
            mRefreshHeader.onRelease();
            return true;
        }
        return false;
    }
}
