package cn.hackill.bong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @author hackill
 * @date on 17/8/1 13:35
 */

public class BezierLoadingView extends View {

    private static final String TAG = "BezierLoadingView";

    private int mRefreshStart = 90;
    private int mRefreshStop = 90;
    private boolean mIsStart = true;
    private boolean mCanRefresh;
    private float mRadius = 10;
    private Path mCurvePath = new Path();
    private Paint mBezierPaint, mCirclePaint;

    private RectF mContentRectF = new RectF();
    private RectF mCircleRectF = new RectF();

    private int RECT_HEIGHT = 50;
    private float INIT_HEIGHT = 10; //初始化高度

    private int mHeight;


    private LoadingStatus mLoadingStatus = LoadingStatus.NORMAL;


    public BezierLoadingView(Context context) {
        this(context, null);
    }

    public BezierLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initControl();
    }


    private void initControl() {
        mBezierPaint = new Paint();
        mBezierPaint.setAntiAlias(true);
        mBezierPaint.setStyle(Paint.Style.FILL);
        mBezierPaint.setColor(0xfff7f7f7);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(DisplayUtil.dp2Px(getContext(), 3));
        mCirclePaint.setColor(0xFFFFFFFF);

        mRadius = DisplayUtil.dp2Px(getContext(), 12);
        INIT_HEIGHT = DisplayUtil.dp2Px(getContext(), 50);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            Log.d(TAG, "onLayout() called with: changed = [" + changed + "], left = [" + left + "], top = [" + top + "], right = [" + right + "], bottom = [" + bottom + "]");
            mHeight = getHeight();
            mContentRectF.set(0, RECT_HEIGHT, right, mHeight * 0.9f);
            mCircleRectF.set(mContentRectF.centerX() - mRadius, mRadius, mContentRectF.centerX() + mRadius, 3 * mRadius);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if (mHeight > RECT_HEIGHT) {

            mCurvePath.reset();
            mCurvePath.moveTo(0, mHeight);
            mCurvePath.lineTo(0, mContentRectF.top);
            mCurvePath.quadTo(mContentRectF.centerX(), mContentRectF.top + (mContentRectF.bottom - mContentRectF.top) * 2,
                    mContentRectF.right, mContentRectF.top);
            mCurvePath.lineTo(mContentRectF.right, mHeight);
            mCurvePath.close();

            canvas.drawPath(mCurvePath, mBezierPaint);

            if (mLoadingStatus == LoadingStatus.NORMAL) {
                if (mHeight >= INIT_HEIGHT) {
                    drawPullDownCircle(canvas);
                }
            } else if (mLoadingStatus == LoadingStatus.REFRESH) {
                drawRefreshingCircle(canvas);
            }
        }
    }


    public void releaseFinger() {
        if (mCanRefresh) {
            mLoadingStatus = LoadingStatus.REFRESH;
        } else {
            mLoadingStatus = LoadingStatus.NORMAL;
        }
        postInvalidate();
    }


    private void drawRefreshingCircle(Canvas canvas) {

        mRefreshStart += mIsStart ? 3 : 10;
        mRefreshStop += mIsStart ? 10 : 3;
        mRefreshStart = mRefreshStart % 360;
        mRefreshStop = mRefreshStop % 360;

        int swipe = mRefreshStop - mRefreshStart;
        swipe = swipe < 0 ? swipe + 360 : swipe;

        Log.i(TAG, "drawRefreshing: refresh mRefreshStart = " + mRefreshStart + ", swipe = " + swipe);

        canvas.drawArc(mCircleRectF, mRefreshStart, swipe, false, mCirclePaint);

        if (swipe >= 300) {
            mIsStart = false;
        } else if (swipe <= 10) {
            mIsStart = true;
        }
        postInvalidate();
    }

    private void drawPullDownCircle(Canvas canvas) {

        float deltaHeight = (mHeight - INIT_HEIGHT) * 0.8f;

        int sweepAngle = (int) (deltaHeight / INIT_HEIGHT * 360);

        if (sweepAngle <= 0) sweepAngle = 0;

        if (sweepAngle >= 360) {
            mCanRefresh = true;
        } else {
            mCanRefresh = false;
        }
        canvas.drawArc(mCircleRectF, -90, sweepAngle, false, mCirclePaint);
    }

    public boolean isRefresh() {
        return mLoadingStatus == LoadingStatus.REFRESH;
    }

    public void setRefresh(boolean stop) {
        mCanRefresh = stop;
        releaseFinger();
    }


    public enum LoadingStatus {
        PULL_DOWN, NORMAL, REFRESH
    }

}
