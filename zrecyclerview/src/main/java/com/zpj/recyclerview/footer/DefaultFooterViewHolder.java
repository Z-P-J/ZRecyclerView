package com.zpj.recyclerview.footer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.R;

public class DefaultFooterViewHolder extends AbsFooterViewHolder {

    protected View progressContainer;
    protected TextView tvMsg;

    @Override
    public View onCreateFooterView(ViewGroup root) {
        View view = LayoutInflater.from(root.getContext()).inflate(R.layout.easy_base_footer, null, false);
        progressContainer = view.findViewById(R.id.ll_container_progress);
        tvMsg = view.findViewById(R.id.tv_msg);
        return view;
    }

    @Override
    public void onShowLoading() {
        if (progressContainer != null) {
            progressContainer.setVisibility(View.VISIBLE);
        }
        if (tvMsg != null) {
            tvMsg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onShowHasNoMore() {
        onShowMessage(getView().getResources().getString(R.string.easy_has_no_more));
    }

    @Override
    public void onShowError(String msg) {
        onShowMessage(msg);
    }

    public void onShowMessage(String msg) {
        if (progressContainer != null) {
            progressContainer.setVisibility(View.GONE);
        }
        if (tvMsg != null) {
            tvMsg.setVisibility(View.VISIBLE);
            tvMsg.setText(msg);
        }
    }

}
