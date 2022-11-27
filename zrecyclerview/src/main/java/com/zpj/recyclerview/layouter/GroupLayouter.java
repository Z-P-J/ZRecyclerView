//package com.zpj.recyclerview.layouter;
//
//import android.support.annotation.NonNull;
//import android.support.v7.widget.BaseMultiLayoutManager;
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
//    public void scrapOrRecycleView(BaseMultiLayoutManager manager, int index, View view) {
//
//    }
//
//    @Override
//    public void addViewToRecycler(View view) {
//
//    }
//
//    @Override
//    public void setLayoutManager(BaseMultiLayoutManager manager) {
//
//    }
//
//    @Override
//    public BaseMultiLayoutManager getLayoutManager() {
//        return null;
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
//
//    }
//
//    @Override
//    public int getPositionOffset() {
//        return 0;
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
//    public void layoutChildren(MultiData<?> multiData, int currentPosition) {
//
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
//    public void saveState(int firstPosition, int firstOffset) {
//
//    }
//}
