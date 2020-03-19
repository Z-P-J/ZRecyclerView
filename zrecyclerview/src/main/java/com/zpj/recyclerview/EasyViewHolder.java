package com.zpj.recyclerview;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public final class EasyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private IEasy.OnItemClickCallback clickCallback;
    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;
    private final View itemView;
    private int position;
    private int viewType = 0;

    public EasyViewHolder(@NonNull View view) {
        super(view);
        this.itemView = view;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public <T extends View> T getView(@IdRes int id) {
        return itemView.findViewById(id);
    }

    public void setVisible(@IdRes int id, boolean visible) {
        View view = itemView.findViewById(id);
        if (view != null) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setInVisible(@IdRes int id) {
        View view = itemView.findViewById(id);
        if (view != null) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    public <T extends TextView> T getTextView(@IdRes int id) {
        return itemView.findViewById(id);
    }

    public void setText(@IdRes int id, CharSequence text) {
        getTextView(id).setText(text);
    }

    public ImageView getImageView(@IdRes int id) {
        return itemView.findViewById(id);
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getHolderPosition() {
        return position;
    }

    void setItemClickCallback(IEasy.OnItemClickCallback clickCallback) {
        this.clickCallback = clickCallback;
    }

    public void setOnItemClickListener(final View.OnClickListener listener) {
        onClickListener = listener;
    }

    public void setOnItemLongClickListener(final View.OnLongClickListener listener) {
        onLongClickListener = listener;
    }

    public View getItemView() {
        return itemView;
    }

    public boolean performClick() {
        return itemView.performClick();
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public int getViewType() {
        return viewType;
    }

    @Override
    public void onClick(View v) {
        if (clickCallback != null && clickCallback.shouldIgnoreClick(itemView)) {
            return;
        }
        if (onClickListener != null) {
            onClickListener.onClick(v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (clickCallback != null && clickCallback.shouldIgnoreClick(itemView)) {
            return true;
        } else if (onLongClickListener != null) {
            return onLongClickListener.onLongClick(v);
        }
        return false;
    }
}
