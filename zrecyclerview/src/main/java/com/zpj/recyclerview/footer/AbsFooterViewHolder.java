package com.zpj.recyclerview.footer;

import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.EasyViewHolder;

public abstract class AbsFooterViewHolder implements IFooterViewHolder {

    private View view;

    @Override
    public final EasyViewHolder onCreateViewHolder(ViewGroup root) {
        if (view == null) {
            view = onCreateFooterView(root);
        }
        return new EasyViewHolder(view);
    }

    @Override
    public final View getView() {
        return view;
    }

    @Override
    public void onBindFooter(EasyViewHolder holder) {

    }

    @Override
    public void onShowLoading() {

    }

    @Override
    public void onShowHasNoMore() {

    }

    @Override
    public void onShowError(String msg) {

    }

}
