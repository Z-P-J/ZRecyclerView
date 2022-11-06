package com.zpj.recycler.demo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.recycler.demo.manager.GalleryLayoutManager;
import com.zpj.recycler.demo.manager.HorizontalDecoration;
import com.zpj.recycler.demo.manager.RecentLayoutManager;
import com.zpj.recycler.demo.manager.RecentLayoutManager2;
import com.zpj.recycler.demo.manager.StackLayoutManager;

public class CustomLayoutManagerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    //    private GalleryLayoutManager layoutManager;
    private StackLayoutManager layoutManager;
    private final LinearSnapHelper snapHelper = new LinearSnapHelper();

    private int mCurrentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_layout_manager);

        recyclerView = findViewById(R.id.recycler_view);

//        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        recyclerView.setLayoutManager(layoutManager);

//        layoutManager = new GalleryLayoutManager(recyclerView);
        layoutManager = new StackLayoutManager(recyclerView);
        recyclerView.setLayoutManager(layoutManager);

//        recyclerView.addItemDecoration(new HorizontalDecoration(10));

//        recyclerView.setLayoutManager(new RecentLayoutManager());


        snapHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(new MyAdapter());
    }

    @Override
    public void onBackPressed() {
        if (layoutManager.isExpand()) {
            layoutManager.idle(mCurrentPosition);
            return;
        }
        super.onBackPressed();
    }

    private static class CustomViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTvText;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvText = itemView.findViewById(R.id.tv_text);
        }
    }


    private class MyAdapter extends RecyclerView.Adapter<CustomViewHolder> {

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_text_card, null, false);

            view.setLayoutParams(new ViewGroup.LayoutParams(viewGroup.getWidth() / 3 * 2, viewGroup.getHeight() / 3 * 2));

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, int i) {
            holder.mTvText.setText("pos " + i);
            holder.mTvText.setTag(i);
//            holder.itemView.setScaleX(0.8f);
//            holder.itemView.setScaleY(0.8f);

            holder.mTvText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag();
//                    snapHelper.attachToRecyclerView(null);


                    mCurrentPosition = pos;

                    if (layoutManager.isExpand()) {
                        layoutManager.idle(pos);
                    } else {
                        layoutManager.expand(pos);
                    }

                    Toast.makeText(CustomLayoutManagerActivity.this, "click " + pos, Toast.LENGTH_SHORT).show();

                }
            });
        }

        @Override
        public int getItemCount() {
            return 50;
        }
    }

}