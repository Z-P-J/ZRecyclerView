package com.zpj.recyclerview.footer;

import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.EasyViewHolder;

public interface IFooterViewHolder {

    EasyViewHolder onCreateViewHolder(ViewGroup root);

    View onCreateFooterView(ViewGroup root);

    View getView();

    void onBindFooter(EasyViewHolder holder);

    void onShowLoading();

    void onShowHasNoMore();

    void onShowError(String msg);

}
