package com.zpj.recyclerview.core;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class SceneLayoutParams extends RecyclerView.LayoutParams {

    private Scene mScene;

    public void setScene(Scene mScene) {
        this.mScene = mScene;
    }

    public Scene getScene() {
        return mScene;
    }

    public SceneLayoutParams(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    public SceneLayoutParams(int width, int height) {
        super(width, height);
    }

    public SceneLayoutParams(ViewGroup.MarginLayoutParams source) {
        super(source);
    }

    public SceneLayoutParams(ViewGroup.LayoutParams source) {
        super(source);
    }

    public SceneLayoutParams(RecyclerView.LayoutParams source) {
        super(source);
    }
}
