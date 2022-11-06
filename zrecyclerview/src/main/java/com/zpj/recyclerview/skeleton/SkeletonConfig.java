package com.zpj.recyclerview.skeleton;

import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import android.view.View;

public class SkeletonConfig {

    private final boolean mShimmer;
    private final int mItemCount;
    private final int mItemResID;
    private final int mShimmerColor;
    private final int mShimmerDuration;
    private final int mShimmerAngle;
    private final boolean mFrozen;

    private SkeletonConfig(Builder builder) {
        this.mShimmer = builder.mShimmer;
        this.mItemCount = builder.mItemCount;
        this.mItemResID = builder.mItemResID;
        this.mShimmerColor = builder.mShimmerColor;
        this.mShimmerDuration = builder.mShimmerDuration;
        this.mShimmerAngle = builder.mShimmerAngle;
        this.mFrozen = builder.mFrozen;
    }

    public boolean isShimmer() {
        return mShimmer;
    }

    public int getItemCount() {
        return mItemCount;
    }

    public int getItemResID() {
        return mItemResID;
    }

    public int getShimmerColor() {
        return mShimmerColor;
    }

    public int getShimmerDuration() {
        return mShimmerDuration;
    }

    public int getShimmerAngle() {
        return mShimmerAngle;
    }

    public boolean isFrozen() {
        return mFrozen;
    }

    public static class Builder {
        private boolean mShimmer = true;
        private int mItemCount = Integer.MAX_VALUE;
        private int mItemResID = View.NO_ID;
        private int mShimmerColor = Color.parseColor("#40878787");
        private int mShimmerDuration = 800;
        private int mShimmerAngle = 20;
        private boolean mFrozen = true;


        public Builder count(@IntRange(from = 1, to = Integer.MAX_VALUE) int itemCount) {
            this.mItemCount = itemCount;
            return this;
        }

        public Builder shimmer(boolean shimmer) {
            this.mShimmer = shimmer;
            return this;
        }

        public Builder duration(int shimmerDuration) {
            this.mShimmerDuration = shimmerDuration;
            return this;
        }

        public Builder color(@ColorInt int shimmerColor) {
            this.mShimmerColor = shimmerColor;
            return this;
        }

        public Builder angle(@IntRange(from = 0, to = 30) int shimmerAngle) {
            this.mShimmerAngle = shimmerAngle;
            return this;
        }

        public Builder skeletonItemRes(@LayoutRes int skeletonLayoutResID) {
            this.mItemResID = skeletonLayoutResID;
            return this;
        }


        public Builder frozen(boolean frozen) {
            this.mFrozen = frozen;
            return this;
        }

        public SkeletonConfig build() {
            return new SkeletonConfig(this);
        }
    }

}
