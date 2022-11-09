package com.zpj.recycler.demo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.recycler.demo.manager.StackLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomLayoutManagerActivity extends AppCompatActivity {

    private static final String TAG = "CustomLayoutManager";

    private final List<String> items = new ArrayList<>();

    private RecyclerView recyclerView;
    //    private GalleryLayoutManager layoutManager;
    private StackLayoutManager layoutManager;
    private final LinearSnapHelper snapHelper = new LinearSnapHelper();

    private View mBottomBar;

    private VelocityTracker mTracker;

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

        for (int i = 0; i < 50; i++) {
            items.add("item " + i);
        }

        final MyAdapter adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);



        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder holder, @NonNull RecyclerView.ViewHolder holder1) {
//                int from = holder.getAdapterPosition();
//                int to = holder.getAdapterPosition();
//                Collections.swap(items, from, to);
//                adapter.notifyItemMoved(from, to);
//                return true;
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int pos = viewHolder.getAdapterPosition();
                items.remove(pos);
                layoutManager.removeItem(pos);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        final int maxV = ViewConfiguration.get(this).getScaledMaximumFlingVelocity();

        mBottomBar = findViewById(R.id.bottom_bar);
        mBottomBar.setOnTouchListener(new View.OnTouchListener() {
            boolean isUp = false;
            boolean isSwipe = false;
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Log.d(TAG, "onTouch isExpand=" + layoutManager.isExpand());
                if (!layoutManager.isExpand()) {
                    return false;
                }

                if (mTracker == null) {
                    mTracker = VelocityTracker.obtain();
                }
                mTracker.addMovement(event);

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDownX = event.getRawX();
                    mDownY = event.getRawY();
                    mX = event.getX();
                    mY = event.getY() + mBottomBar.getTop();
                    isUp = false;
                    isSwipe = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float deltaX = event.getRawX() - mDownX;
                    float deltaY = event.getRawY() - mDownY;

//                    if (!isUp) {
//                        if (deltaY > 0 || Math.abs(deltaY) < Math.abs(deltaX)) {
//                            isUp = false;
//                            return true;
//                        }
//                    }


                    if (!isUp && !isSwipe) {
                        if (Math.abs(deltaY) < Math.abs(deltaX)) {
                            isSwipe = true;
                            isUp = false;
                        } else if (deltaY < 0) {
                            isSwipe = false;
                            isUp = true;
                        } else {
                            return false;
                        }
                    }

//                    isUp = true;

                    int y = (int) (mY - event.getY()  + mBottomBar.getTop());
                    y = (int) (mBottomBar.getTop() + event.getY());

                    if (isUp) {
                        layoutManager.moveDrag(mX, mY, (int) event.getX(), y, (int) deltaX, (int) deltaY);
                    } else if (isSwipe) {
                        layoutManager.swipeBy(mX, mY, (int) event.getX(), y, (int) deltaX, (int) deltaY);
                    }


                } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                    mTracker.computeCurrentVelocity(1000, maxV);
                    Toast.makeText(CustomLayoutManagerActivity.this, "vX=" + mTracker.getXVelocity() + " vY=" + mTracker.getYVelocity(), Toast.LENGTH_SHORT).show();

                    if (isUp) {
                        layoutManager.endDrag(mTracker.getYVelocity());
                    } else if (isSwipe) {
                        layoutManager.endSwipe(mTracker.getYVelocity());
                    }
                    mTracker.recycle();
                    mTracker = null;
                    isUp = false;
                    isSwipe = false;
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (layoutManager.isExpand()) {
            layoutManager.idle();
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
        public void onBindViewHolder(@NonNull final CustomViewHolder holder, int i) {
            holder.mTvText.setText(items.get(i));
//            holder.itemView.setScaleX(0.8f);
//            holder.itemView.setScaleY(0.8f);

            holder.mTvText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();

                    layoutManager.setTargetPosition(pos);

                    if (layoutManager.isExpand()) {
                        layoutManager.idle();
                    } else {
                        layoutManager.expand();
                    }

                    Toast.makeText(CustomLayoutManagerActivity.this, "click pos=" + pos, Toast.LENGTH_SHORT).show();

                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

}