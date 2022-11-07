package com.zpj.recycler.demo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.recycler.demo.manager.GalleryLayoutManager;
import com.zpj.recycler.demo.manager.HorizontalDecoration;
import com.zpj.recycler.demo.manager.RecentLayoutManager;
import com.zpj.recycler.demo.manager.RecentLayoutManager2;
import com.zpj.recycler.demo.manager.StackLayoutManager;

public class CustomLayoutManagerActivity extends AppCompatActivity {

    private static final String TAG = "CustomLayoutManager";

    private RecyclerView recyclerView;
    //    private GalleryLayoutManager layoutManager;
    private StackLayoutManager layoutManager;
    private final LinearSnapHelper snapHelper = new LinearSnapHelper();

    private View mBottomBar;

    private VelocityTracker mTracker;

    private int mCurrentPosition;

    private float mDownX;
    private float mDownY;

    private float mX;
    private float mY;

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



        final int maxV = ViewConfiguration.get(this).getScaledMaximumFlingVelocity();


        mBottomBar = findViewById(R.id.bottom_bar);
        mBottomBar.setOnTouchListener(new View.OnTouchListener() {
            boolean isUp = false;
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Log.d(TAG, "onTouch isExpand=" + layoutManager.isExpand());
                if (!layoutManager.isExpand()) {
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mTracker == null) {
                        mTracker = VelocityTracker.obtain();
                    } else {
                        mTracker.clear();
                    }
                    mTracker.addMovement(event);
                    mDownX = event.getRawX();
                    mDownY = event.getRawY();
                    mX = event.getX();
                    mY = event.getY() + mBottomBar.getTop();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float deltaX = event.getRawX() - mDownX;
                    float deltaY = event.getRawY() - mDownY;

//                    if (!isUp) {
//                        if (deltaY > 0 || Math.abs(deltaY) < Math.abs(deltaX)) {
//                            isUp = false;
//                            return true;
//                        }
//                    }

                    isUp = true;
                    mTracker.addMovement(event);




                    int y = (int) (mY - event.getY()  + mBottomBar.getTop());
                    y = (int) (mBottomBar.getTop() + event.getY());

                    layoutManager.moveDrag(mCurrentPosition, mX, mY, (int) event.getX(), y, (int) deltaX, (int) deltaY);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                    mTracker.addMovement(event);
                    mTracker.computeCurrentVelocity(1000, maxV);
                    Toast.makeText(CustomLayoutManagerActivity.this, "vX=" + mTracker.getXVelocity() + " vY=" + mTracker.getYVelocity(), Toast.LENGTH_SHORT).show();

                    if (isUp) {
                        layoutManager.endDrag(mCurrentPosition, mTracker.getYVelocity());
                    }

                }
                return true;
            }
        });
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