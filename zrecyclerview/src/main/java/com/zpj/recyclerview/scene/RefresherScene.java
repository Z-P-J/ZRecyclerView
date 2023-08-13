package com.zpj.recyclerview.scene;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.refresh.IRefresher;

import java.util.List;

public class RefresherScene extends VerticalScene {

    private final IRefresher mRefreshHeader;

    private float mDownX = -1;
    private float mDownY = -1;
    private float mOffset = 0;
    private boolean isMoveDown;

    public RefresherScene(IRefresher refresher) {
        super(new RefresherMultiData(refresher));
        mRefreshHeader = refresher;
    }

    public IRefresher getRefresher() {
        return mRefreshHeader;
    }

    @Override
    public boolean onTouchDown(MotionEvent event) {
        isMoveDown = false;
        if (mRefreshHeader.getView() != null
                && mRefreshHeader.getView().getParent() != null
                && mRefreshHeader.getState() == IRefresher.STATE_NORMAL) {
            mDownX = event.getX();
            mDownY = event.getY();
            mOffset = mRefreshHeader.getDelta();
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchMove(MotionEvent event) {
        if (isMoveDown) {
            float deltaY = event.getY() - mDownY + mOffset;
            stopInterceptRequestLayout();
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
                    stopInterceptRequestLayout();
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
    public boolean onTouchUp(MotionEvent event, float velocityX, float velocityY) {
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

    public void stopInterceptRequestLayout() {
        getLayoutHelper().stopInterceptRequestLayout();
    }

    private static class RefresherMultiData extends MultiData<Void> {

        private final IRefresher mRefresher;

        public RefresherMultiData(IRefresher mRefresher) {
            super();
            hasMore = false;
            this.mRefresher = mRefresher;
        }

        @Override
        public View onCreateView(Context context, ViewGroup container, int viewType) {
            if (mRefresher.getView() == null) {
                return mRefresher.onCreateView(context, container);
            }
            return mRefresher.getView();
        }

        @Override
        public int getViewType(int position) {
            return mRefresher.hashCode();
        }

        @Override
        public boolean hasViewType(int viewType) {
            return viewType == mRefresher.hashCode();
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        @Override
        public int getLayoutId(int viewType) {
            return 0;
        }

        @Override
        public boolean loadData() {
            return false;
        }

        @Override
        public void onBindViewHolder(EasyViewHolder holder, List<Void> list, int position, List<Object> payloads) {

        }
    }

}
