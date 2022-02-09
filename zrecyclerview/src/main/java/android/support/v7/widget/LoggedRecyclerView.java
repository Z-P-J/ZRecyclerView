package android.support.v7.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class LoggedRecyclerView extends RecyclerView {

    private static final String TAG = "LoggedRecyclerView";

    public LoggedRecyclerView(@NonNull Context context) {
        super(context);
    }

    public LoggedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoggedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        Log.d(TAG, "setLayoutParams params=" + params);
        super.setLayoutParams(params);
    }

    @Override
    void stopInterceptRequestLayout(boolean performLayoutChildren) {
        Log.d(TAG, "stopInterceptRequestLayout performLayoutChildren=" + performLayoutChildren);
        super.stopInterceptRequestLayout(performLayoutChildren);
    }

    @Override
    void startInterceptRequestLayout() {
        Log.d(TAG, "startInterceptRequestLayout");
        super.startInterceptRequestLayout();
    }

    @Override
    public void requestLayout() {

        try {
            Field field = RecyclerView.class.getDeclaredField("mInterceptRequestLayoutDepth");
            field.setAccessible(true);
            int mInterceptRequestLayoutDepth = (int) field.get(this);
            Log.d(TAG, "requestLayout mInterceptRequestLayoutDepth=" + mInterceptRequestLayoutDepth + " mLayoutFrozen=" + mLayoutFrozen);
            if (mInterceptRequestLayoutDepth == 0 && !this.mLayoutFrozen) {
                super.requestLayout();
            } else {
                this.mLayoutWasDefered = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        Log.d(TAG, "onMeasure widthSpec=" + widthSpec + " heightSpec=" + heightSpec);
        super.onMeasure(widthSpec, heightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout changed=" + changed);
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    void dispatchLayout() {
        if (this.mAdapter == null) {
            Log.e("RecyclerView", "No adapter attached; skipping layout");
        } else if (this.mLayout == null) {
            Log.e("RecyclerView", "No layout manager attached; skipping layout");
        } else {
            this.mState.mIsMeasuring = false;
            if (this.mState.mLayoutStep == 1) {
                Log.d(TAG, "dispatchLayout this.mState.mLayoutStep=1");
                this.dispatchLayoutStep1();
                this.mLayout.setExactMeasureSpecsFrom(this);
                this.dispatchLayoutStep2();
            } else if (!this.mAdapterHelper.hasUpdates() && this.mLayout.getWidth() == this.getWidth() && this.mLayout.getHeight() == this.getHeight()) {
                Log.d(TAG, "dispatchLayout !hasUpdates");
                this.mLayout.setExactMeasureSpecsFrom(this);
            } else {
                Log.d(TAG, "dispatchLayout --> dispatchLayoutStep2");
                this.mLayout.setExactMeasureSpecsFrom(this);
                this.dispatchLayoutStep2();
            }

            try {
                Method method = RecyclerView.class.getDeclaredMethod("dispatchLayoutStep3");
                method.setAccessible(true);
                Log.d(TAG, "dispatchLayout --> dispatchLayoutStep3");
                method.invoke(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            this.dispatchLayoutStep3();
        }
    }

    private void dispatchLayoutStep1() {
        Log.d(TAG, "dispatchLayoutStep1");
        this.mState.assertLayoutStep(1);
        this.fillRemainingScrollValues(this.mState);
        this.mState.mIsMeasuring = false;
        this.startInterceptRequestLayout();
        this.mViewInfoStore.clear();
        this.onEnterLayoutOrScroll();
        this.processAdapterUpdatesAndSetAnimationFlags();
        this.saveFocusInfo();
        this.mState.mTrackOldChangeHolders = this.mState.mRunSimpleAnimations && this.mItemsChanged;
        this.mItemsAddedOrRemoved = this.mItemsChanged = false;
        this.mState.mInPreLayout = this.mState.mRunPredictiveAnimations;
        this.mState.mItemCount = this.mAdapter.getItemCount();
        try {
            Field field = RecyclerView.class.getDeclaredField("mMinMaxLayoutPositions");
            field.setAccessible(true);
            int[] mMinMaxLayoutPositions = (int[]) field.get(this);
            this.findMinMaxChildLayoutPositions(mMinMaxLayoutPositions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int i;
        if (this.mState.mRunSimpleAnimations) {
            int count = this.mChildHelper.getChildCount();

            for(i = 0; i < count; ++i) {
                RecyclerView.ViewHolder holder = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
                if (!holder.shouldIgnore() && (!holder.isInvalid() || this.mAdapter.hasStableIds())) {
                    RecyclerView.ItemAnimator.ItemHolderInfo animationInfo = this.mItemAnimator.recordPreLayoutInformation(this.mState, holder, RecyclerView.ItemAnimator.buildAdapterChangeFlagsForAnimations(holder), holder.getUnmodifiedPayloads());
                    this.mViewInfoStore.addToPreLayout(holder, animationInfo);
                    if (this.mState.mTrackOldChangeHolders && holder.isUpdated() && !holder.isRemoved() && !holder.shouldIgnore() && !holder.isInvalid()) {
                        long key = this.getChangedHolderKey(holder);
                        this.mViewInfoStore.addToOldChangeHolders(key, holder);
                    }
                }
            }
        }

        if (this.mState.mRunPredictiveAnimations) {
            this.saveOldPositions();
            boolean didStructureChange = this.mState.mStructureChanged;
            this.mState.mStructureChanged = false;
            this.mLayout.onLayoutChildren(this.mRecycler, this.mState);
            this.mState.mStructureChanged = didStructureChange;

            for(i = 0; i < this.mChildHelper.getChildCount(); ++i) {
                View child = this.mChildHelper.getChildAt(i);
                RecyclerView.ViewHolder viewHolder = getChildViewHolderInt(child);
                if (!viewHolder.shouldIgnore() && !this.mViewInfoStore.isInPreLayout(viewHolder)) {
                    int flags = RecyclerView.ItemAnimator.buildAdapterChangeFlagsForAnimations(viewHolder);
                    boolean wasHidden = viewHolder.hasAnyOfTheFlags(8192);
                    if (!wasHidden) {
                        flags |= 4096;
                    }

                    RecyclerView.ItemAnimator.ItemHolderInfo animationInfo = this.mItemAnimator.recordPreLayoutInformation(this.mState, viewHolder, flags, viewHolder.getUnmodifiedPayloads());
                    if (wasHidden) {
                        this.recordAnimationInfoIfBouncedHiddenView(viewHolder, animationInfo);
                    } else {
                        this.mViewInfoStore.addToAppearedInPreLayoutHolders(viewHolder, animationInfo);
                    }
                }
            }

            this.clearOldPositions();
        } else {
            this.clearOldPositions();
        }

        this.onExitLayoutOrScroll();
        this.stopInterceptRequestLayout(false);
        this.mState.mLayoutStep = 2;
    }

    private void dispatchLayoutStep2() {
        Log.d(TAG, "dispatchLayoutStep2");
        this.startInterceptRequestLayout();
        this.onEnterLayoutOrScroll();
        this.mState.assertLayoutStep(6);
        this.mAdapterHelper.consumeUpdatesInOnePass();
        this.mState.mItemCount = this.mAdapter.getItemCount();
        this.mState.mDeletedInvisibleItemCountSincePreviousLayout = 0;
        this.mState.mInPreLayout = false;
        this.mLayout.onLayoutChildren(this.mRecycler, this.mState);
        this.mState.mStructureChanged = false;
        try {
            Field field = RecyclerView.class.getDeclaredField("mPendingSavedState");
            field.setAccessible(true);
            field.set(this, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mState.mRunSimpleAnimations = this.mState.mRunSimpleAnimations && this.mItemAnimator != null;
        this.mState.mLayoutStep = 4;
        this.onExitLayoutOrScroll();
        this.stopInterceptRequestLayout(false);
    }

    private void findMinMaxChildLayoutPositions(int[] into) {
        int count = this.mChildHelper.getChildCount();
        if (count == 0) {
            into[0] = -1;
            into[1] = -1;
        } else {
            int minPositionPreLayout = 2147483647;
            int maxPositionPreLayout = -2147483648;

            for(int i = 0; i < count; ++i) {
                RecyclerView.ViewHolder holder = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
                if (!holder.shouldIgnore()) {
                    int pos = holder.getLayoutPosition();
                    if (pos < minPositionPreLayout) {
                        minPositionPreLayout = pos;
                    }

                    if (pos > maxPositionPreLayout) {
                        maxPositionPreLayout = pos;
                    }
                }
            }

            into[0] = minPositionPreLayout;
            into[1] = maxPositionPreLayout;
        }
    }

    private void saveFocusInfo() {
        View child = null;
        if (getPreserveFocusAfterLayout() && this.hasFocus() && this.mAdapter != null) {
            child = this.getFocusedChild();
        }

        RecyclerView.ViewHolder focusedVh = child == null ? null : this.findContainingViewHolder(child);
        if (focusedVh == null) {
            this.resetFocusInfo();
        } else {
            this.mState.mFocusedItemId = this.mAdapter.hasStableIds() ? focusedVh.getItemId() : -1L;
            this.mState.mFocusedItemPosition = this.mDataSetHasChangedAfterLayout ? -1 : (focusedVh.isRemoved() ? focusedVh.mOldPosition : focusedVh.getAdapterPosition());
            this.mState.mFocusedSubChildId = this.getDeepestFocusedViewWithId(focusedVh.itemView);
        }

    }

    private int getDeepestFocusedViewWithId(View view) {
        int lastKnownId = view.getId();

        while(!view.isFocused() && view instanceof ViewGroup && view.hasFocus()) {
            view = ((ViewGroup)view).getFocusedChild();
            int id = view.getId();
            if (id != -1) {
                lastKnownId = view.getId();
            }
        }

        return lastKnownId;
    }

    private void resetFocusInfo() {
        this.mState.mFocusedItemId = -1L;
        this.mState.mFocusedItemPosition = -1;
        this.mState.mFocusedSubChildId = -1;
    }

    private void processAdapterUpdatesAndSetAnimationFlags() {
        if (this.mDataSetHasChangedAfterLayout) {
            this.mAdapterHelper.reset();
            if (this.mDispatchItemsChangedEvent) {
                this.mLayout.onItemsChanged(this);
            }
        }

        if (this.predictiveItemAnimationsEnabled()) {
            this.mAdapterHelper.preProcess();
        } else {
            this.mAdapterHelper.consumeUpdatesInOnePass();
        }

        boolean animationTypeSupported = this.mItemsAddedOrRemoved || this.mItemsChanged;
        this.mState.mRunSimpleAnimations = this.mFirstLayoutComplete && this.mItemAnimator != null && (this.mDataSetHasChangedAfterLayout || animationTypeSupported || this.mLayout.mRequestedSimpleAnimations) && (!this.mDataSetHasChangedAfterLayout || this.mAdapter.hasStableIds());
        this.mState.mRunPredictiveAnimations = this.mState.mRunSimpleAnimations && animationTypeSupported && !this.mDataSetHasChangedAfterLayout && this.predictiveItemAnimationsEnabled();
    }

    private boolean predictiveItemAnimationsEnabled() {
        return this.mItemAnimator != null && this.mLayout.supportsPredictiveItemAnimations();
    }

}
