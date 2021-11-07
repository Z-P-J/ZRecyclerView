package com.zpj.recyclerview.manager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.zpj.recyclerview.MultiData;

public class MultiLayoutParams extends RecyclerView.LayoutParams {

    private MultiData<?> multiData;

    public void setMultiData(MultiData<?> multiData) {
        this.multiData = multiData;
    }

    public MultiData<?> getMultiData() {
        return multiData;
    }

    public MultiLayoutParams(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    public MultiLayoutParams(int width, int height) {
        super(width, height);
    }

    public MultiLayoutParams(ViewGroup.MarginLayoutParams source) {
        super(source);
    }

    public MultiLayoutParams(ViewGroup.LayoutParams source) {
        super(source);
    }

    public MultiLayoutParams(RecyclerView.LayoutParams source) {
        super(source);
    }
}
