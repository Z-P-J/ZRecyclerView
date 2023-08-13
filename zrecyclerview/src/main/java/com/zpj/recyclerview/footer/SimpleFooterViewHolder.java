package com.zpj.recyclerview.footer;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.R;

public class SimpleFooterViewHolder extends AbsFooterViewHolder {

    protected View loadingView;
    protected View textView;

    protected int loadingViewId;
    protected int textViewId;

    public SimpleFooterViewHolder(@LayoutRes int loadingViewId) {
        this(loadingViewId, 0);
    }

    public SimpleFooterViewHolder(@LayoutRes int loadingViewId, @LayoutRes int textViewId) {
        this.loadingViewId = loadingViewId;
        this.textViewId = textViewId;
    }

    public SimpleFooterViewHolder(View loadingView) {
        this(loadingView, null);
    }

    public SimpleFooterViewHolder(View loadingView, View textView) {
        this.loadingView = loadingView;
        this.textView = textView;
    }

    @Override
    public View onCreateFooterView(ViewGroup root) {
        Context context = root.getContext();
        FrameLayout frameLayout = new FrameLayout(context);
        if (loadingView == null && loadingViewId > 0) {
            loadingView = LayoutInflater.from(context).inflate(loadingViewId, root, false);
        }
        if (loadingView != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            frameLayout.addView(loadingView, params);
        }
        if (textView == null) {
            if (textViewId > 0) {
                textView = LayoutInflater.from(context).inflate(textViewId, root, false);
            } else {
                TextView text = new TextView(context);
                text.setGravity(Gravity.CENTER);
                int padding = (int) (context.getResources().getDisplayMetrics().density * 16);
                text.setPadding(padding, padding, padding, padding);
                textView = text;
            }
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        frameLayout.addView(textView, params);
        textView.setVisibility(View.GONE);
        return frameLayout;
    }

    @Override
    public void onShowLoading() {
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
        }
        if (textView != null) {
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onShowHasNoMore() {
        onShowError(getView().getResources().getString(R.string.easy_has_no_more));
    }

    @Override
    public void onShowError(String msg) {
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
        if (textView != null) {
            textView.setVisibility(View.VISIBLE);
            showMessage(textView, msg);
        }
    }

    public void showMessage(View textView, String msg) {
        if (textView instanceof TextView) {
            ((TextView) textView).setText(msg);
        }
    }


}
