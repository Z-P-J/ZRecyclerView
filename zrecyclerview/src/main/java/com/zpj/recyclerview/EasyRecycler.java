package com.zpj.recyclerview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EasyRecycler<T> extends BaseRecycler<T, EasyRecycler<T>> {

    public EasyRecycler(@NonNull RecyclerView recyclerView) {
        super(recyclerView);
    }

    public EasyRecycler(@NonNull RecyclerView recyclerView, @NonNull List<T> dataSet) {
        super(recyclerView, dataSet);
    }

    public static <T> EasyRecycler<T> with(@NonNull RecyclerView recyclerView) {
        return new EasyRecycler<>(recyclerView);
    }

    public static <T> EasyRecycler<T> with(@NonNull RecyclerView recyclerView, @NonNull List<T> dataSet) {
        return new EasyRecycler<>(recyclerView, dataSet);
    }

}
