//package com.zpj.recyclerview.layouter;
//
//import android.support.annotation.NonNull;
//import android.support.v7.widget.BaseMultiLayoutManager;
//import android.util.Log;
//import android.view.View;
//
//import com.zpj.recyclerview.GroupMultiData;
//import com.zpj.recyclerview.MultiData;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class GroupLayouter implements Layouter {
//
//    @NonNull
//    private final GroupMultiData mGroupData;
//
//    public GroupLayouter(@NonNull GroupMultiData multiData) {
//        mGroupData = multiData;
//    }
//
//    private Layouter mTouchLayouter;
//
//
//
//    protected int mPositionOffset;
//
//    protected int mFirstPosition = 0;
//    protected int mFirstOffset;
//
//
//    @Override
//    public void layoutChildren(MultiData<?> data, int currentPosition) {
//        // TODO
//        int positionOffset = getPositionOffset();
//
//        int firstPosition = currentPosition - positionOffset;
//
//        Layouter last = null;
//        for (MultiData<?> multiData : mGroupData.getData()) {
//            Layouter layouter = multiData.getLayouter();
//            layouter.setPositionOffset(positionOffset);
//
//
//            if (positionOffset >= currentPosition) {
//
//            }
//
//            if (last != null) {
//                layouter.setTop(last.getBottom());
//                layouter.layoutChildren(multiData, positionOffset);
//            } else {
//                layouter.setTop(layouter.getTop() + mTopOffset);
//                mTopOffset = 0;
//                layouter.layoutChildren(multiData, mTopPosition + positionOffset);
//                topPosition = mTopPosition + positionOffset;
//            }
//            last = layouter;
//            positionOffset += getLayoutHelper().getCount(multiData);
//        }
//    }
//
//
//    @Override
//    public boolean onTouchDown(MultiData<?> multiData, float downX, float downY) {
//        for (MultiData<?> data : mGroupData.getData()) {
//            Layouter layouter = data.getLayouter();
//            if (downY >= layouter.getTop() && downY <= layouter.getBottom()) {
//                if (layouter.onTouchDown(multiData, downX, downY)) {
//                    mTouchLayouter = layouter;
//                    return true;
//                }
//                return false;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public boolean onTouchMove(MultiData<?> multiData, float x, float y, float downX, float downY) {
//        if (mTouchLayouter == null) {
//            return false;
//        }
//        return mTouchLayouter.onTouchMove(multiData, x, y, downX, downY);
//    }
//
//    @Override
//    public boolean onTouchUp(MultiData<?> multiData, float velocityX, float velocityY) {
//        if (mTouchLayouter == null) {
//            return false;
//        }
//        return mTouchLayouter.onTouchUp(multiData, velocityX, velocityY);
//    }
//
//    @Override
//    public void setLayoutManager(BaseMultiLayoutManager manager) {
//        for (MultiData<?> data : mGroupData.getData()) {
//            Layouter layouter = data.getLayouter();
//            layouter.setLayoutManager(manager);
//        }
//    }
//
//    @Override
//    public LayoutHelper getLayoutHelper() {
//        return null;
//    }
//
//    @Override
//    public void setLeft(int left) {
//
//    }
//
//    @Override
//    public void setTop(int top) {
//
//    }
//
//    @Override
//    public void setRight(int right) {
//
//    }
//
//    @Override
//    public void setBottom(int bottom) {
//
//    }
//
//    @Override
//    public int getLeft() {
//        return 0;
//    }
//
//    @Override
//    public int getTop() {
//        return 0;
//    }
//
//    @Override
//    public int getRight() {
//        return 0;
//    }
//
//    @Override
//    public int getBottom() {
//        return 0;
//    }
//
//    @Override
//    public void offsetLeftAndRight(int offset) {
//
//    }
//
//    @Override
//    public void offsetTopAndBottom(int offset) {
//
//    }
//
//    @Override
//    public void setPositionOffset(int offset) {
//        mPositionOffset = offset;
//    }
//
//    @Override
//    public int getPositionOffset() {
//        return mPositionOffset;
//    }
//
//    @Override
//    public void setChildOffset(int offset) {
//
//    }
//
//    @Override
//    public int getChildOffset() {
//        return 0;
//    }
//
//    @Override
//    public boolean canScrollHorizontally() {
//        return false;
//    }
//
//    @Override
//    public boolean canScrollVertically() {
//        return false;
//    }
//
//    @Override
//    public int fillVertical(View anchorView, int dy, MultiData<?> multiData) {
//        return 0;
//    }
//
//    @Override
//    public int fillHorizontal(View anchorView, int dx, MultiData<?> multiData) {
//        return 0;
//    }
//
//    @Override
//    public int scrollHorizontallyBy(int dx, MultiData<?> scrollMultiData) {
//        return 0;
//    }
//
//    @Override
//    public void saveState(int firstPosition, int firstOffset) {
//        this.mFirstPosition = Math.max(0, firstPosition - getPositionOffset());
//        this.mFirstOffset = firstOffset;
//    }
//}
