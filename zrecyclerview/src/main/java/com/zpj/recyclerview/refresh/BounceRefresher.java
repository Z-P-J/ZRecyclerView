package com.zpj.recyclerview.refresh;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 实现RecyclerView弹性回弹
 */
public class BounceRefresher implements IRefresher {

    private FrameLayout container;

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {

    }

    @Override
    public void setState(int state) {
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent) {
        container = new FrameLayout(context);
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return container;
    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void onMove(float delta) {
        ViewGroup.LayoutParams params = container.getLayoutParams();
        params.height = (int) (delta / 3);
        container.setLayoutParams(params);
    }

    @Override
    public boolean onRelease() {
        if (0 == container.getHeight()) {
            return false;
        }
        container.clearAnimation();
        ValueAnimator animator = ValueAnimator.ofInt(container.getHeight(), 0, container.getHeight() / 6, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
                params.height = (int) (animation.getAnimatedValue());
                container.setLayoutParams(params);
            }
        });
        animator.setDuration(500);
        animator.start();
        return false;
    }

    @Override
    public void stopRefresh() {

    }



}
