package com.zpj.recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.List;

public class MultiRecyclerView extends RecyclerView {

    private final MultiRecyclerViewWrapper recyclerView;

    public MultiRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public MultiRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        recyclerView = new MultiRecyclerViewWrapper(this);

    }

    public void init(List<MultiData> list) {
        int max = 1;
        for (MultiData data : list) {
            if (data.getSpanCount() > max) {
                max = data.getSpanCount();
            }
        }

        recyclerView.setData(list)
                .setMaxSpan(max)
                .build();



//        recyclerView.setData(list);
//
//        recyclerView.setMaxSpan(max);
//        recyclerView.setFooterView(LayoutInflater.from(getContext()).inflate(R.layout.easy_base_footer, null, false));
//        recyclerView.build();
//        recyclerView.showContent();
    }


}
