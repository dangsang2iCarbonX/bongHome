package cn.hackill.bong;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.tuesda.walker.circlerefresh.R;


/**
 * @author hackill
 * @date on 17/7/31 13:52
 */

public class TopLayout extends FrameLayout {

    private static final String TAG = "FragTopLayout";


    public TopLayout(@NonNull Context context) {
        super(context);
    }

    public TopLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TopLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    View cycTop;
    private Bitmap bitmap;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i(TAG, "onFinishInflate: .....");
        cycTop = LayoutInflater.from(getContext()).inflate(R.layout.cyc_top, null);
        addView(cycTop);
        initView();
        requestLayout();
        //
        bitmap = BitmapUtil.drawableToBitmap(getContext().getResources().getDrawable(R.drawable.home_bg_step));
    }

    BezierLoadingView mLoadingView;


    private void initView() {
        mLoadingView = new BezierLoadingView(getContext());

        int height = (int) DisplayUtil.dp2Px(getContext(), 50);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        mLoadingView.setLayoutParams(params);
        addView(mLoadingView);
        Log.i(TAG, "initView: .....");
        setWillNotDraw(false);
        postInvalidate();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

//        Log.d(TAG, "onLayout() called with: changed = [" + changed + "], left = [" + left + "], top = [" + top + "], right = [" + right + "], bottom = [" + bottom + "]");
        if (changed) {
            int viewOneHeight = cycTop.getMeasuredHeight();
            int viewTwoHeight = mLoadingView.getMeasuredHeight();

//            Log.i(TAG, "onLayout: oneH = " + viewOneHeight + ", twoH = " + viewTwoHeight);
            cycTop.layout(left, 0, right, viewOneHeight);
            mLoadingView.layout(left, viewOneHeight - cycTop.getPaddingBottom(), right, Math.max(viewOneHeight, bottom));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            // 为ScrollerLayout中的每一个子控件测量大小
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw: ......");
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    public BezierLoadingView getLoadingView() {
        return mLoadingView;
    }
}
