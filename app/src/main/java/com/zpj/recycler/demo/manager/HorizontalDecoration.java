package com.zpj.recycler.demo.manager;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 自定义ItemDecoration，设置第一个视图和最后一个视图偏移的距离，确保第一个视图和最后一个视图在屏幕中居中<P/>
 *
 * @author mailanglideguozhe 20210520
 */
public class HorizontalDecoration extends RecyclerView.ItemDecoration {
    private int space = 0;
    /**
     * 第一个视图和最后一个视图偏移的距离
     */
    private int distance = 0;
    private static final String TAG = "HorizontalDecoration";

    /**
     * 设置RecyclerView子视图的边距，本示例仅用于定义两个子视图之间的边距，为space*2
     *
     * @param space 设置的边距
     */
    public HorizontalDecoration(int space) {
        this.space = space;
    }

    /**
     * 获取子视图的边距
     *
     * @param view   子视图
     * @param parent RecyclerView对象
     */
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull final View view, @NonNull final RecyclerView parent, @NonNull RecyclerView.State state) {
        int pos = parent.getChildAdapterPosition(view);
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        /**
         * 仅计算一次偏移边距即可，无需重复计算<P/>
         * 由于此时View并未完成测量，无法基于测量获取其宽度;思路是在view绘制完成后再进行测量，并设置第一个的左边距
         */
        if (distance <= 0) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    distance = dtDistance(parent, view);
                    //设置第一个视图的左边距
                    View childView = parent.getChildAt(0);
                    RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childView.getLayoutParams();
                    layoutParams.setMargins(distance, 0, space, 0);
                    childView.setLayoutParams(layoutParams);
                    //打开后默认显示第一个（居中显示）
                    parent.scrollToPosition(0);
                }
            });
        }
        /**
         * 通过设置Item左右边距实现第一个左侧和最后一个右侧设置边距,确保显示的视图位于屏幕中间
         */
        int itemCount = parent.getAdapter().getItemCount();
        if (pos == 0) {
            layoutParams.setMargins(distance, 0, space, 0);
        } else if (pos == itemCount - 1) {
            layoutParams.setMargins(space, 0, distance, 0);
        } else {
            layoutParams.setMargins(space, 0, space, 0);
        }
        /**
         * 更新子视图的边距
         */
        view.setLayoutParams(layoutParams);
        super.getItemOffsets(outRect, view, parent, state);
    }

    /**
     * 为了使第一个和最后一个item居中，需要设置相应偏移，偏移量为RecyclerView布局宽度减去子视图的一半<P/>
     * 注意此处由于子视图并未实例化完成，无法通过测量得知其宽度,故需要直接获取布局宽度参数得知<P/>
     */
    public int dtDistance(RecyclerView recyclerView, View childView) {
        int width = recyclerView.getWidth() != 0 ? recyclerView.getWidth() : recyclerView.getMeasuredWidth();
        //此处需要获取子视图布局的宽度，注意此处由于子视图并未实例化完成，无法通过测量得知其宽度
        childView.getMeasuredWidth();
        int childWidth = childView.getWidth();
        //第一个视图左侧偏移量，最后一个视图右侧偏移量
        return width / 2 - childWidth / 2;
    }
}

