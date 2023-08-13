package com.zpj.recyclerview;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zpj.recyclerview.core.Scene;

public class LoadCallbackImpl<T> implements MultiData.LoadCallback {

    private static final String TAG = "LoadCallbackImpl";

    @NonNull
    private final MultiSceneAdapter mAdapter;
    private final MultiData<T> mMultiData;

    protected int mLastCount = 0;

    public LoadCallbackImpl(@NonNull MultiSceneAdapter adapter, MultiData<T> multiData) {
        mAdapter = adapter;
        mMultiData = multiData;
    }

    @Override
    public void setHasMore(boolean hasMore) {
        mMultiData.hasMore = hasMore;
    }

    @Override
    public void scrollToPosition(final int position) {
        mAdapter.post(new Runnable() {
            @Override
            public void run() {
                int offset = getPositionOffset();
                mAdapter.getRecyclerView().scrollToPosition(offset + position);

//                int num = getStartCount();
//                for (MultiData<?> data : adapter.getData()) {
//                    if (data == MultiData.this) {
//                        getAdapter().getRecyclerView().scrollToPosition(num + position);
//                        break;
//                    }
//                    num  += data.getCount();
//                }
            }
        });
    }

    @Override
    public void smoothScrollToPosition(final int position) {
        mAdapter.post(new Runnable() {
            @Override
            public void run() {

                int offset = getPositionOffset();
                mAdapter.getRecyclerView().smoothScrollToPosition(offset + position);

//                int num = getStartCount();
//                for (MultiData<?> data : adapter.getData()) {
//                    if (data == MultiData.this) {
//                        getAdapter().getRecyclerView().smoothScrollToPosition(num + position);
//                        break;
//                    }
//                    num  += data.getCount();
//                }
            }
        });
    }

    @Override
    public void notifyItemMove(final int from, final int to) {
        if (isInMainThread()) {
            int offset = getPositionOffset();
            Log.d("notifyItemMove", "offset=" + offset + " getCount=" + mMultiData.getItemCount());
            mAdapter.notifyItemMoved(from + offset, to + offset);

//            int count = getStartCount();
//            for (MultiData<?> data : mAdapter.getData()) {
//                if (data == this) {
//                    Log.d("notifyItemMove", "count=" + count + " getCount=" + getCount());
//                    mAdapter.notifyItemMoved(from + count, to + count);
//                    break;
//                }
//                count  += data.getCount();
//            }
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemMove(from, to);
                }
            });
        }
    }

    @Override
    public void notifyDataSetChange() {
        Log.d(TAG, "notifyDataSetChanged mAdapter=" + mAdapter);

        if (isInMainThread()) {
            Log.d(TAG, "notifyDataSetChanged getItemCount=" + mAdapter.getItemCount() + " count=" + getItemCount() + " tempCount=" + mLastCount);
            if (getItemCount() != mLastCount) {
                if (getItemCount() > mLastCount) {

                    int offset = getPositionOffset();
                    Log.d("MultiData", "notifyDataSetChanged->notifyItemRangeInserted");
                    mAdapter.notifyItemRangeInserted(offset + mLastCount, getItemCount() - mLastCount);

//                    int num = getStartCount();
//                    for (MultiData<?> data : mAdapter.getData()) {
//
//                        if (data instanceof GroupMultiData) {
//                            boolean breakFor = false;
//                            for (MultiData<?> multiData : ((GroupMultiData) data).getData()) {
//                                if (multiData == this) {
//                                    Log.d("MultiData", "GroupMultiData notifyDataSetChanged->notifyItemRangeInserted");
//                                    mAdapter.notifyItemRangeInserted(num + mLastCount, getCount() - mLastCount);
//                                    breakFor = true;
//                                    break;
//                                }
//                                num  += data.getCount();
//                            }
//                            if (breakFor) {
//                                break;
//                            }
//                        } else {
//                            if (data == this) {
//                                Log.d("MultiData", "notifyDataSetChanged->notifyItemRangeInserted");
//                                mAdapter.notifyItemRangeInserted(num + mLastCount, getCount() - mLastCount);
//                                break;
//                            }
//                            num  += data.getCount();
//                        }
//                    }
                } else {
                    Log.d("MultiData", "notifyDataSetChanged->notifyItemRangeRemoved");
                    notifyItemRangeRemoved(getItemCount(), mLastCount - getItemCount());
                }
            }
            Log.d("MultiData", "notifyDataSetChanged->notifyItemRangeChanged");
            notifyItemRangeChanged(0, getItemCount());

            mLastCount = getItemCount();
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter.footerViewHolder != null && mAdapter.footerViewHolder.getView() != null) {
                        mAdapter.footerViewHolder.getView().performClick();
                    }
                }
            });
            Log.d("MultiData", "notifyDataSetChanged->end");
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChange();
                }
            });
        }
    }

    @Override
    public void notifyItemChanged(final int position) {
        notifyItemChanged(position, null);
    }

    @Override
    public void notifyItemChanged(final int position, @Nullable final Object payload) {
        if (isInMainThread()) {
            int offset = getPositionOffset();
            Log.d("postNotifyItemChanged", "offset=" + offset + " position=" + position + " getCount=" + getItemCount());
            mAdapter.notifyItemChanged(offset + position, payload);

//            int count = getStartCount();
//            for (MultiData<?> data : mAdapter.getData()) {
//                if (data == this) {
//                    Log.d("postNotifyItemChanged", "count=" + count + " position=" + position + " getCount=" + getCount());
//                    mAdapter.notifyItemChanged(count + position, payload);
//                    break;
//                }
//                count  += data.getCount();
//            }
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(position, payload);
                }
            });
        }

    }

    @Override
    public void notifyItemRangeChanged(int positionStart, int count) {
        notifyItemRangeChanged(positionStart, count, null);
    }

    @Override
    public void notifyItemRangeChanged(final int positionStart, final int count, @Nullable final Object payload) {
        if (isInMainThread()) {

            int offset = getPositionOffset();
            Log.d(TAG, "notifyItemRangeChanged positionStart=" + positionStart + " count=" + count + " getCount=" + getItemCount());
            mAdapter.notifyItemRangeChanged(offset + positionStart, count, payload);

//            int num = getStartCount();
//            for (MultiData<?> data : mAdapter.getData()) {
//
//                if (data instanceof GroupMultiData) {
//                    boolean breakFor = false;
//                    for (MultiData<?> multiData : ((GroupMultiData) data).getData()) {
//                        if (multiData == this) {
//                            Log.d(TAG, "notifyItemRangeChanged positionStart=" + positionStart + " count=" + count + " getCount=" + getCount());
//                            mAdapter.notifyItemRangeChanged(num + positionStart, count, payload);
//                            breakFor = true;
//                            break;
//                        }
//                        num  += data.getCount();
//                    }
//                    if (breakFor) {
//                        break;
//                    }
//                } else {
//                    if (data == this) {
//                        Log.d(TAG, "notifyItemRangeChanged positionStart=" + positionStart + " count=" + count + " getCount=" + getCount());
//                        mAdapter.notifyItemRangeChanged(num + positionStart, count, payload);
//                        break;
//                    }
//                    num  += data.getCount();
//                }
//            }
            mLastCount = getItemCount();
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeChanged(positionStart, count, payload);
                }
            });
        }

    }

    @Override
    public void notifyItemRangeRemoved() {
        if (isInMainThread()) {

            int offset = getPositionOffset();
            Log.d("notifyItemRangeRemoved", "offset=" + offset + " getCount=" + getItemCount());
            mAdapter.notifyItemRangeRemoved(offset, getItemCount());

//            int count = getStartCount();
//            for (MultiData<?> data : mAdapter.getData()) {
//                if (data == this) {
//                    Log.d("notifyItemRangeRemoved", "count=" + count + " getCount=" + getCount());
//                    mAdapter.notifyItemRangeRemoved(count, getCount());
//                    break;
//                }
//                count  += data.getCount();
//            }
            mLastCount = getItemCount();
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeRemoved();
                }
            });
        }
    }

    @Override
    public void notifyItemRangeRemoved(final int positionStart, final int count) {
        if (isInMainThread()) {
            int offset = getPositionOffset();
            Log.d(TAG, "notifyItemRangeRemoved positionStart=" + positionStart + " count=" + count + " offset=" + offset + " getCount=" + getItemCount());
            mAdapter.notifyItemRangeRemoved(offset + positionStart, count);

//            int num = getStartCount();
//            for (MultiData<?> data : mAdapter.getData()) {
//                if (data == this) {
//                    Log.d(TAG, "notifyItemRangeRemoved positionStart=" + positionStart + " count=" + count + " num=" + num + " getCount=" + getCount());
//                    mAdapter.notifyItemRangeRemoved(num + positionStart, count);
//                    break;
//                }
//                num  += data.getCount();
//            }
            mLastCount = getItemCount();
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeRemoved(positionStart, count);
                }
            });
        }

    }

    @Override
    public void notifyItemRemoved(final int position) {
        if (isInMainThread()) {
            int offset = getPositionOffset();
            Log.d("postNotifyItemRemoved", "offset=" + offset + " position=" + position + " getCount=" + getItemCount());
            mLastCount = getItemCount();
            mAdapter.notifyItemRemoved(offset + position);


//            int count = getStartCount();
//            for (MultiData<?> data : mAdapter.getData()) {
//                if (data == this) {
//                    Log.d("postNotifyItemRemoved", "count=" + count + " position=" + position + " getCount=" + getCount());
//                    mLastCount = getCount();
//                    mAdapter.notifyItemRemoved(count + position);
//                    break;
//                }
//                count  += data.getCount();
//            }
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRemoved(position);
                }
            });
        }

    }

    @Override
    public void notifyItemRangeInserted() {
        if (isInMainThread()) {

            int offset = getPositionOffset();
            Log.d("notifyItemRangeInserted", "offset=" + offset + " getCount=" + getItemCount());
            mLastCount = getItemCount();
            mAdapter.notifyItemRangeInserted(offset, getItemCount());
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter.footerViewHolder != null) {
                        mAdapter.footerViewHolder.getView().performClick();
                    }
                }
            });

//            int count = getStartCount();
//            for (MultiData<?> data : mAdapter.getData()) {
//                if (data == this) {
//                    Log.d("notifyItemRangeInserted", "count=" + count + " getCount=" + getCount());
//                    mLastCount = getCount();
//                    mAdapter.notifyItemRangeInserted(count, getCount());
//                    mAdapter.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (mAdapter.footerViewHolder != null) {
//                                mAdapter.footerViewHolder.getView().performClick();
//                            }
//                        }
//                    });
//                    break;
//                }
//                count  += data.getCount();
//            }
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeInserted();
                }
            });
        }

    }

    @Override
    public void notifyItemRangeInserted(final int positionStart, final int count) {
        if (isInMainThread()) {
            int num = getStartCount();
            for (Scene scene : mAdapter.getItems()) {
                MultiData<?> data = scene.getMultiData();
                if (data == mMultiData) {
//                    if (positionStart >= getCount()) {
//                        return;
//                    }
//
//                    if (positionStart + count > getCount()) {
//                        mAdapter.notifyItemRangeInserted(num + positionStart, getCount() - positionStart);
//                    } else {
//                        mAdapter.notifyItemRangeInserted(num + positionStart, count);
//                    }
                    mAdapter.notifyItemRangeInserted(num + positionStart, count);
                    mAdapter.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter.footerViewHolder != null) {
                                mAdapter.footerViewHolder.getView().performClick();
                            }
                        }
                    });
                    break;
                }
                num  += data.getItemCount();
            }
            mLastCount = getItemCount();
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeInserted(positionStart, count);
                }
            });
        }
    }

    private int getPositionOffset() {
        int offset = getStartCount();
        for (Scene scene : mAdapter.getItems()) {
            MultiData<?> data = scene.getMultiData();
            if (data == mMultiData) {
                return offset;
            } else if (data instanceof GroupMultiData) {
                for (MultiData<?> multiData : ((GroupMultiData) data).getItems()) {
                    if (multiData == mMultiData) {
                        return offset;
                    }
                    offset += data.getItemCount();
                }
            } else {
                offset += data.getItemCount();
            }
        }
        throw new IllegalArgumentException("getPositionOffset illegal multidata");
    }

    protected int getStartCount() {
        int num = mAdapter.headerView == null ? 0 : 1;
        if (mAdapter.mRefreshHeader != null) {
            num++;
        }
        return num;
    }

    protected int getItemCount() {
        return mMultiData.getItemCount();
    }

    private boolean isInMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

}
