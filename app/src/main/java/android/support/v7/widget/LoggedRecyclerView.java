package android.support.v7.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class LoggedRecyclerView extends RecyclerView {

    private static final String TAG = "TestRecyclerView";

    public LoggedRecyclerView(@NonNull Context context) {
        super(context);
    }

    public LoggedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoggedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        Log.e(TAG, "onMeasure");
        super.onMeasure(widthSpec, heightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e(TAG, "onLayout changed=" + changed);
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    void dispatchLayout() {
        Log.e(TAG, "dispatchLayout");
        super.dispatchLayout();
    }

    @Override
    public View focusSearch(View focused, int direction) {
        Log.e(TAG, "focusSearch");
        return super.focusSearch(focused, direction);
    }

    @Override
    void stopInterceptRequestLayout(boolean performLayoutChildren) {
        Log.e(TAG, "stopInterceptRequestLayout performLayoutChildren=" + performLayoutChildren);
        super.stopInterceptRequestLayout(performLayoutChildren);
    }

    @Override
    void consumePendingUpdateOperations() {
        Log.e(TAG, "consumePendingUpdateOperations");
        super.consumePendingUpdateOperations();
    }
}
