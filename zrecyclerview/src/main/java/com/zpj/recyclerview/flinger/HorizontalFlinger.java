package com.zpj.recyclerview.flinger;

import android.support.annotation.NonNull;

import com.zpj.recyclerview.core.MultiScene;

public class HorizontalFlinger extends AbsFlinger {

    private boolean stopOverScroll = false;

    public HorizontalFlinger(@NonNull MultiScene scene) {
        super(scene);
    }

    @Override
    public boolean onComputeScroll(int dx, int dy) {
        if (dx == 0) {
            return true;
        }
        int consumed = mScene.scrollHorizontallyBy(dx);

        if (consumed != dx) {
            stopOverScroll = true;
            return false;
        }
        return true;
    }

    @Override
    public void onFinished() {
        super.onFinished();
        if (stopOverScroll) {
            stopOverScroll = false;
            mScene.onStopOverScroll();
        }
    }
}
