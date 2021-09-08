package com.zpj.recyclerview.refresh;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;

public abstract class DecorationRefresher extends AbsRefresher {

    protected RecyclerView mRecyclerView;

    public final void bindRecyclerView(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                DecorationRefresher.this.onDraw(c, parent, state, mDelta);
            }

            @Override
            public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                DecorationRefresher.this.onDrawOver(c,  parent, state, mDelta);
            }
        });
    }

    @Override
    public View onCreateRefreshView(Context context, ViewGroup parent) {
        return new Space(context);
    }

    @Override
    public void onMove(float delta) {
        super.onMove(delta);
        invalidate();
    }

    public void invalidate() {
        if (mRecyclerView != null) {
            mRecyclerView.invalidate();
        }
    }

    public abstract void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state, float delta);

    public abstract void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state, float delta);

}
