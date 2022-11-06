package com.zpj.recyclerview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

public final class EasyViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {

    private final SparseArray<View> mViews;

    private IEasy.OnItemClickCallback clickCallback;
    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;
    private final View itemView;
    private int position;
    private int viewType = 0;
    private Object tag;

    public EasyViewHolder(@NonNull View view) {
        super(view);
        this.mViews = new SparseArray<>();
        this.itemView = view;
    }

    public EasyViewHolder(@NonNull View view, int viewType) {
        this(view);
        this.viewType = viewType;
    }

    public boolean post(Runnable runnable) {
        return this.itemView.post(runnable);
    }

    public boolean postDelayed(Runnable action, long delayMillis) {
        return this.itemView.postDelayed(action, delayMillis);
    }

    public Context getContext() {
        return this.itemView.getContext();
    }

    void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(@IdRes int id, Object tag) {
        View view = getView(id);
        if (view != null) {
            view.setTag(tag);
        }
    }

    public void setTag(@IdRes int id, int key, Object tag) {
        View view = getView(id);
        if (view != null) {
            view.setTag(key, tag);
        }
    }

    public Object getTag(@IdRes int id) {
        View view = getView(id);
        if (view != null) {
            return view.getTag();
        }
        return null;
    }

    public Object getTag(@IdRes int id, int key) {
        View view = getView(id);
        if (view != null) {
            return view.getTag(key);
        }
        return null;
    }

    public <T extends View> T getView(@IdRes int id) {
        View view = mViews.get(id);
        if (view == null) {
            view = itemView.findViewById(id);
            mViews.put(id, view);
        }
        return (T) view;
    }

    public void setChecked(@IdRes int viewId, boolean checked) {
        View view = getView(viewId);
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(checked);
        }
    }

    public void setEnabled(@IdRes int viewId, boolean enable) {
        View view = getView(viewId);
        if (view != null) {
            view.setEnabled(enable);
        }
    }

    public void setBackground(@IdRes int id, Drawable drawable) {
        View view = getView(id);
        if (view != null) {
            view.setBackground(drawable);
        }
    }

    public void setBackgroundColor(@IdRes int id, int color) {
        View view = getView(id);
        if (view != null) {
            view.setBackgroundColor(color);
        }
    }

    public void setBackgroundResource(@IdRes int id, int resId) {
        View view = getView(id);
        if (view != null) {
            view.setBackgroundResource(resId);
        }
    }

    public void setOnClickListener(@IdRes int id, View.OnClickListener listener) {
        View view = getView(id);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    private View.OnClickListener onViewClickListener;

    void setOnViewClickListener(View.OnClickListener listener) {
        this.onViewClickListener = listener;
    }

    private View.OnLongClickListener onViewLongClickListener;

    void setOnViewLongClickListener(View.OnLongClickListener listener) {
        this.onViewLongClickListener = listener;
    }

    public void addOnClickListener(@IdRes int id) {
        View view = getView(id);
        if (view != null) {
            view.setOnClickListener(onViewClickListener);
        }
    }

    public void addOnLongClickListener(@IdRes int id) {
        View view = getView(id);
        if (view != null) {
            view.setOnLongClickListener(onViewLongClickListener);
        }
    }

    public void removeOnClickListener(@IdRes int id) {
        View view = getView(id);
        if (view != null) {
            view.setOnClickListener(null);
        }
    }

    public void removeOnLongClickListener(@IdRes int id) {
        View view = getView(id);
        if (view != null) {
            view.setOnLongClickListener(null);
        }
    }

    public void setOnLongClickListener(@IdRes int id, View.OnLongClickListener listener) {
        View view = getView(id);
        if (view != null) {
            view.setOnLongClickListener(listener);
        }
    }

    public void setVisible(@IdRes int id, boolean visible) {
        View view = getView(id);
        if (view != null) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setInVisible(@IdRes int id) {
        View view = getView(id);
        if (view != null) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    public <T extends TextView> T getTextView(@IdRes int id) {
        return getView(id);
    }

    public void setText(@IdRes int id, CharSequence text) {
        View view = getView(id);
        if (view instanceof TextView) {
            ((TextView) view).setText(text);
        }
    }

    public void setText(@IdRes int id, int textId) {
        View view = getView(id);
        if (view instanceof TextView) {
            ((TextView) view).setText(textId);
        }
    }

    public void setTextColor(@IdRes int id, int color) {
        View view = getView(id);
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        }
    }

    public void setTextColor(@IdRes int id, ColorStateList color) {
        View view = getView(id);
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        }
    }

    public void setTextSize(@IdRes int id, float size) {
        View view = getView(id);
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(size);
        }
    }

    public void setTextSize(@IdRes int id, int unit, float size) {
        View view = getView(id);
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(unit, size);
        }
    }

    public ImageView getImageView(@IdRes int id) {
        return getView(id);
    }

    public ImageButton getImageButton(@IdRes int id) {
        return getView(id);
    }

    public void setAlpha(@IdRes int id, float alpha) {
        View view = getView(id);
        if (view != null) {
            view.setAlpha(alpha);
        }
    }

    public void setImageDrawable(@IdRes int id, Drawable drawable) {
        View view = getView(id);
        if (view instanceof ImageView) {
            ((ImageView) view).setImageDrawable(drawable);
        }
    }

    public void setImageResource(@IdRes int id, int resId) {
        View view = getView(id);
        if (view instanceof ImageView) {
            ((ImageView) view).setImageResource(resId);
        }
    }

    public void setImageBitmap(@IdRes int id, Bitmap bitmap) {
        View view = getView(id);
        if (view instanceof ImageView) {
            ((ImageView) view).setImageBitmap(bitmap);
        }
    }

    public void setLayoutParams(@IdRes int id, ViewGroup.LayoutParams params) {
        View view = getView(id);
        if (view != null) {
            view.setLayoutParams(params);
        }
    }

    public void setPadding(int left, int top, int right, int bottom) {
        this.itemView.setPadding(left, top, right, bottom);
    }

    public void setPaddingRelative(int left, int top, int right, int bottom) {
        this.itemView.setPaddingRelative(left, top, right, bottom);
    }

    public void setPadding(int id, int left, int top, int right, int bottom) {
        View view = getView(id);
        if (view != null) {
            view.setPadding(left, top, right, bottom);
        }
    }

    public void setPaddingRelative(int id, int left, int top, int right, int bottom) {
        View view = getView(id);
        if (view != null) {
            view.setPaddingRelative(left, top, right, bottom);
        }
    }

    public void setProgress(@IdRes int viewId, int progress) {
        View view = getView(viewId);
        if (view instanceof ProgressBar) {
            ((ProgressBar) view).setProgress(progress);
        }
    }

    public void setProgress(@IdRes int viewId, int progress, int max) {
        View view = getView(viewId);
        if (view instanceof ProgressBar) {
            ((ProgressBar) view).setMax(max);
            ((ProgressBar) view).setProgress(progress);
        }
    }

    public void setMax(@IdRes int viewId, int max) {
        View view = getView(viewId);
        if (view instanceof  ProgressBar) {
            ((ProgressBar) view).setMax(max);
        }
    }

    public void setRating(@IdRes int viewId, float rating) {
        View view = getView(viewId);
        if (view instanceof RatingBar) {
            ((RatingBar) view).setRating(rating);
        }
    }

    public void setRating(@IdRes int viewId, float rating, int max) {
        View view = getView(viewId);
        if (view instanceof RatingBar) {
            ((RatingBar) view).setMax(max);
            ((RatingBar) view).setRating(rating);
        }
    }

    public void setOnCheckedChangeListener(@IdRes int viewId, CompoundButton.OnCheckedChangeListener listener) {
        View view = getView(viewId);
        if (view instanceof CompoundButton) {
            ((CompoundButton) view).setOnCheckedChangeListener(listener);
        }
    }

    void setRealPosition(int position) {
        this.position = position;
    }

    public int getRealPosition() {
        return position;
    }

    void setItemClickCallback(IEasy.OnItemClickCallback clickCallback) {
        this.clickCallback = clickCallback;
    }

    public void setOnItemClickListener(final View.OnClickListener listener) {
        itemView.setOnClickListener(this);
        onClickListener = listener;
    }

    public void setOnItemLongClickListener(final View.OnLongClickListener listener) {
        itemView.setOnLongClickListener(this);
        onLongClickListener = listener;
    }

    public View getItemView() {
        return itemView;
    }

    public boolean performClick() {
        return itemView.performClick();
    }

    void setViewType(int viewType) {
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
