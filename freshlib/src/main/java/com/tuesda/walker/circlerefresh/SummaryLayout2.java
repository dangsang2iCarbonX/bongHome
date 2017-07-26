//package com.tuesda.walker.circlerefresh;
//
//import android.animation.Animator;
//import android.animation.ValueAnimator;
//import android.content.Context;
//import android.support.v4.view.MotionEventCompat;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.VelocityTracker;
//import android.view.View;
//import android.widget.FrameLayout;
//
//import java.util.NoSuchElementException;
//
//public class SummaryLayout2 extends FrameLayout {
//
//    private static final String TAG = "SummaryLayout";
//
//    private View mHeader;
//    private View mContent;
//    private View mRefresh;
//    private int ANIMATION_TIME = 300;
//
//
//    private OnHeaderUpdate mUpdateListener;
//    private CanScrollListener mCanScrollListener;
//    private OnRefreshListener mRefreshListener;
//
//    private int mHeaderTop = -1, mHeaderBottom = -1, mHeaderHeight = -1;
//    private int mRefreshPoint = -1, mRefreshHeight = -1, mMaxRefreshPoint = -1;
//    private float mJumpDownPoint, mJumpUpPoint;
//
//    private boolean isFlyed = false;
//    private boolean isJumping = false;
//    private boolean isDraggingTested = false;
//    private boolean isDraggingVertical = false;
//    private boolean isDragging;
//    private boolean isRefreshing = false;
//
//    private boolean isDownEvent = false;
//    /**
//     * 防止重复触发
//     */
//    private boolean isRefreshViewShow = false;
//
//    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
//
//    private float mLastY;
//
//    private float mSweep = 1.0f;
//
//    private MotionEvent mDownEvent;
//
//
//    private ValueAnimator mAnimator;
//
//    private int mDragPinterId;
//
//    public SummaryLayout2(Context context) {
//        this(context, null);
//    }
//
//    public SummaryLayout2(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public SummaryLayout2(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//
//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//
//        mHeader = findViewById(R.id.bong_day_header);
//        mContent = findViewById(R.id.bong_day_content);
//        mRefresh = findViewById(R.id.bong_day_refresh);
//        if (mHeader == null || mContent == null || mRefresh == null)
//            throw new NoSuchElementException("not define bong_day_header or bong_day_content or bong_day_refresh");
//
//    }
//
//
//    public boolean isCollapsed() {
//        return mContent.getTop() == mHeaderTop;
//    }
//
//
//    public void finishRefresh() {
//        isRefreshing = false;
//
//        if (mHeaderTop == -1 || mRefreshHeight == -1)
//            return;
//        smoothSetRefresh(mRefresh.getTop(), mHeaderTop - mRefreshHeight, ANIMATION_TIME);
//    }
//
//    public void startRefresh() {
//        if (isRefreshing) {
//            // if is refreshing do nothing
//            smoothSetRefresh(mRefresh.getTop(), mHeaderTop, ANIMATION_TIME);
//            return;
//        }
//
//        isRefreshing = true;
//
//
//        smoothSetRefresh(mRefresh.getTop(), mHeaderTop, ANIMATION_TIME,
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        OnRefreshListener listener = mRefreshListener;
//                        if (listener != null) {
//                            listener.onRefresh();
//                        }
//                    }
//                });
//
//    }
//
//
//    private ValueAnimator mRefreshAnimator = null;
//
//    private void smoothSetRefresh(int from, int to, long durationMillis) {
//
//        smoothSetRefresh(from, to, durationMillis, null);
//
//    }
//
//    private synchronized void smoothSetRefresh(int from, int to, long durationMillis, final Runnable doAfter) {
//
//        if (mRefreshAnimator != null && mRefreshAnimator.isRunning()) {
//            mRefreshAnimator.cancel();
//        }
//
//        ValueAnimator animator = ValueAnimator.ofInt(from, to);
//        animator.setDuration(durationMillis);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                int top = (Integer) animation.getAnimatedValue();
//                setRefreshTop(top);
//            }
//        });
//
//        if (doAfter != null) {
//            animator.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    doAfter.run();
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//
//                }
//            });
//        }
//        animator.start();
//
//        mRefreshAnimator = animator;
//
//    }
//
//    public synchronized void smoothSetHeaderHeight(final int from, final int to, long durationMillis) {
//
//        smoothSetHeaderHeight(from, to, durationMillis, new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                isJumping = false;
//                if (mUpdateListener != null) {
//
//                    mUpdateListener.onFinishUpdate(isCollapsed(), 0);
//
//                }
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//    }
//
//    public synchronized void smoothSetHeaderHeight(final int from, final int to, long durationMilles,
//                                                   Animator.AnimatorListener animationListener) {
//
//        if (mAnimator != null && mAnimator.isRunning()) {
//            mAnimator.cancel();
//            mAnimator = null;
//        }
//
//        mAnimator = ValueAnimator.ofInt(from, to);
//        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                setContentTop((Integer) animation.getAnimatedValue());
//            }
//        });
//
//        mAnimator.addListener(animationListener);
//
//
//        mAnimator.setDuration(durationMilles);
//        mAnimator.start();
//    }
//
//    private void setContentTop(int top) {
//        mSweep = (mHeaderBottom - top) / (float) mHeaderHeight;
//
//        if (mUpdateListener != null) {
//            mUpdateListener.onUpdate(mHeaderHeight, (mHeaderBottom - top), mSweep);
//        }
//        mContent.offsetTopAndBottom(-mContent.getTop() + top);
//        mHeader.offsetTopAndBottom((-mHeader.getBottom() + top));
////        mHeader.setRotationX(90 * mSweep);
//        mHeader.setAlpha(1 - mSweep);
//        invalidate();
//    }
//
//    private void setRefreshOffsetTopAndBottom(int distanceY) {
//
//        final OnRefreshListener orl = mRefreshListener;
//
//        if (orl != null && !isRefreshing) {
//            if ((mRefresh.getTop() > mRefreshPoint && mRefresh.getTop() + distanceY < mRefreshPoint)
//                    || (mRefresh.getTop() < mRefreshPoint && mRefresh.getTop() + distanceY >= mRefreshPoint)) {
//                if (distanceY < 0) {
//                    post(new Runnable() {
//                        @Override
//                        public void run() {
//                            orl.cancelRefresh();
//                        }
//                    });
//                } else {
//                    post(new Runnable() {
//                        @Override
//                        public void run() {
//                            orl.waitRefresh();
//
//                        }
//                    });
//                }
//            }
//            if (mRefresh.getTop() <= mRefreshPoint) {
//                post(new Runnable() {
//                    @Override
//                    public void run() {
//                        orl.onRefreshActiveProgress(mRefresh.getTop() / (float) mRefreshPoint);
////                        Log.d(TAG, "run rt:" + mRefresh.getTop() + " rp:" + mRefreshPoint);
//                    }
//                });
//            }
//        }
//        //下拉阻尼
//        if (distanceY > 0) {
////            Log.d(TAG, "setRefreshOffsetTopAndBottom before dy:" + distanceY + " max:" + mMaxRefreshPoint + " rb:" + mRefresh.getBottom());
////            distanceY = mRefresh.getTop() > mRefreshPoint ?
////                    (int) ((mMaxRefreshPoint - mRefresh.getBottom()) / (float) mMaxRefreshPoint * distanceY)
////                    : distanceY;
//            distanceY = (int) ((mMaxRefreshPoint - mRefresh.getBottom()) / (float) mMaxRefreshPoint * distanceY) + 3;
//
////            Log.d(TAG, "setRefreshOffsetTopAndBottom after dy:" + distanceY);
//        }
//
//        mRefresh.offsetTopAndBottom(distanceY);
//
//        mHeader.offsetTopAndBottom(distanceY);
//        mContent.offsetTopAndBottom(distanceY);
//
//
//        if (mRefresh.getBottom() <= mHeaderTop && isRefreshViewShow) {
//            //hide refresh view
//            isRefreshViewShow = false;
//            if (orl != null) {
//                orl.hideRefreshView();
//                isRefreshing = false;
//            }
//
//        } else if (mRefresh.getBottom() > mHeaderTop && !isRefreshViewShow) {
//            isRefreshViewShow = true;
//            if (orl != null)
//                orl.showRefreshView();
//        }
//
//        invalidate();
//    }
//
//    private void setRefreshTop(int top) {
//        setRefreshOffsetTopAndBottom(top - mRefresh.getTop());
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//
//        LayoutParams params = (LayoutParams) mHeader.getLayoutParams();
//        int hLeft = params.leftMargin;
//        int hTop = params.topMargin;
//        mHeader.layout(hLeft, hTop + mHeader.getTop(), hLeft + mHeader.getMeasuredWidth(), hTop + mHeader.getTop() + mHeader.getMeasuredHeight());
//        if (mHeaderTop == -1) {
//            mHeaderTop = mHeader.getTop();
//            mHeaderBottom = mHeader.getBottom();
//            mHeaderHeight = mHeader.getMeasuredHeight();
//            mJumpDownPoint = mHeaderTop + mHeaderHeight * 0.3f;
//            mJumpUpPoint = mHeaderBottom - mHeaderHeight * 0.3f;
//            mContent.layout(0, mHeaderHeight, right, mHeaderHeight + mContent.getMeasuredHeight());
//            //put refresh view above header
//            mRefresh.layout(0, -mRefresh.getMeasuredHeight(), mRefresh.getMeasuredWidth(), 0);
//            mRefreshPoint = (int) (mRefresh.getMeasuredHeight() * 1.5f + 10);
//            mMaxRefreshPoint = (int) (mRefreshPoint * 2.5f);
//            mRefreshHeight = mRefresh.getMeasuredHeight();
//        } else {
//            mContent.layout(0, mContent.getTop(), mContent.getBottom(), mContent.getTop() + mContent.getMeasuredHeight());
//
////            //put refresh view above header
////            mRefresh.layout(0, mRefresh.getTop(), mRefresh.getMeasuredWidth(), 0);
//        }
//    }
//
//
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (isDownEvent && mDragPinterId != MotionEventCompat.getPointerId(ev, ev.getActionIndex())) {
//            return true;
//        }
//        int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
//
//
//        mVelocityTracker.addMovement(ev);
//
//        MotionEvent cme;
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//
//                mDownEvent = MotionEvent.obtain(ev);
//
//                mDragPinterId = MotionEventCompat.getPointerId(ev, 0);
//
//                isDraggingTested = false;
//                isDraggingVertical = false;
//                isDragging = false;
//                isFlyed = false;
//                isDownEvent = true;
//
//
//                mLastY = mDownEvent.getY();
//
//                super.dispatchTouchEvent(ev);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (isJumping) {
//                    // if is jumping view not deal touch event
//                    break;
//                }
//
//
//                if (!isDraggingTested) {
//                    if (Math.abs(ev.getY() - mDownEvent.getY()) < 6 || Math.abs(ev.getX() - mDownEvent.getX()) < 6)
//                        break;
//                    isDraggingTested = true;
//                    if (Math.abs(ev.getX() - mDownEvent.getX()) < Math.abs(ev.getY() - mDownEvent.getY()) * 1.5f) {
//                        isDraggingVertical = true;
//                    }
//                }
//
//                if (isDraggingVertical) {
//                    float distanceY = mLastY - ev.getY();
//                    if ((mHeaderTop < mContent.getTop() && mHeaderBottom > mContent.getTop()) //最高点和最低点之间移动 content 视图
//                            || (distanceY < 0 && mHeaderTop == mContent.getTop() && mCanScrollListener != null && !mCanScrollListener.canScrollDown())
//                            || (distanceY > 0 && mHeaderBottom == mContent.getTop())) {
//                        // should drag view to move
//                        if (!isDragging) {
//                            //start drag
//                            cme = MotionEvent.obtain(ev);
//                            cme.setAction(MotionEvent.ACTION_CANCEL);
//                            super.dispatchTouchEvent(cme);
//
//                            isDragging = true;
//                            if (mUpdateListener != null)
//                                mUpdateListener.onStartUpdate(isCollapsed());
//                        }
//
//
//                        int top = (int) (mContent.getTop() - distanceY);
//
//                        if (top < mHeaderTop) {
//                            //不可超过header 顶部
//                            top = mHeaderTop;
//                        } else if (top > mHeaderBottom) {
//                            //不可低于header 底部
//                            top = mHeaderBottom;
//                        }
//
//                        if (distanceY < 0 && top >= mJumpDownPoint && mContent.getTop() <= mJumpDownPoint) {
//                            isJumping = true;
//                            smoothSetHeaderHeight(mContent.getTop(), mHeaderBottom, 150);
//
//                        } else if (distanceY > 0 && top <= mJumpUpPoint && mContent.getTop() >= mJumpUpPoint) {
//                            isJumping = true;
//                            smoothSetHeaderHeight(mContent.getTop(), mHeaderTop, 150);
//                        } else {
//                            setContentTop(top);
//                        }
//                    } else {
//                        //not dragging
//                        if (isDragging) {
//                            isDragging = false;
//                            cme = MotionEvent.obtain(ev);
//                            cme.setAction(MotionEvent.ACTION_DOWN);
//                            super.dispatchTouchEvent(cme);
//
//                            if (mUpdateListener != null)
//                                mUpdateListener.onFinishUpdate(isCollapsed(), 0);
//                        }
////                        Log.d(TAG, "dispatchTouchEvent ct:" + mContent.getTop() + " rt:" + mRefresh.getTop() + " rH:" + mRefresh.getHeight() + " rp:" + mRefreshPoint + " hb:" + mHeaderBottom);
//
//                        //set pull to refresh arrange
//                        if (mContent.getTop() >= mHeaderBottom) {
//                            if (mHeader.getTop() - distanceY < mHeaderTop) {
//                                //顶部不能超过视图范围
//                                distanceY = mHeader.getTop() - mHeaderTop;
//                            }
////                            else if (mRefresh.getTop() - distanceY > mMaxRefreshPoint) {
////                                //底部不能超过 3倍的 refresh高度
////                                distanceY = mRefresh.getTop() - mMaxRefreshPoint;
////                            }
//
//                            setRefreshOffsetTopAndBottom((int) -distanceY);
//                        } else {
////                            Log.e(TAG, "dispatchTouchEvent not do anything in dragging vertical");
//                            super.dispatchTouchEvent(ev);
//                        }
//                    }
//
//                } else {
//                    ev.setLocation(ev.getX(), mLastY);
//                    super.dispatchTouchEvent(ev);
//                }
//
//                break;
//            case MotionEvent.ACTION_CANCEL:
//                super.dispatchTouchEvent(ev);
//
//            case MotionEvent.ACTION_POINTER_UP:
//            case MotionEvent.ACTION_UP:
//                isDownEvent = false;
//
//                isDraggingTested = false;
//                isDraggingVertical = false;
//
//                if (mContent.getTop() < mHeaderBottom && mContent.getTop() > mHeaderTop) {
//                    // in drag view area
//                    if (isDragging && !isJumping) {
//                        mVelocityTracker.computeCurrentVelocity(100, Float.MAX_VALUE);
//                        final float velocity = mVelocityTracker.getYVelocity();
//
//                        if (Math.abs(velocity) > 80 && !isDocked()) {
//                            isFlyed = true;
//                            smoothSetHeaderHeight(mContent.getTop()
//                                    , velocity > 0 ? mHeaderBottom : mHeaderTop
//                                    , 150
//                                    , new Animator.AnimatorListener() {
//                                        @Override
//                                        public void onAnimationStart(Animator animation) {
//
//                                        }
//
//                                        @Override
//                                        public void onAnimationEnd(Animator animation) {
//                                            isJumping = false;
//                                            if (mUpdateListener != null) {
//                                                mUpdateListener.onFinishUpdate(isCollapsed(), velocity);
//
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onAnimationCancel(Animator animation) {
//
//                                        }
//
//                                        @Override
//                                        public void onAnimationRepeat(Animator animation) {
//
//                                        }
//                                    });
//                        }
//                    }
//
//
//                    if (!isFlyed && !isDocked() && !isJumping) {
//                        if (mSweep < 0.5) {
//                            //down
//                            headerDock(false);
//                        } else {
//                            //up
//                            headerDock(true);
//                        }
//                    }
//                } else if (mContent.getTop() > mHeaderBottom) {
//                    //in refresh area
//                    Log.d(TAG, "dispatchTouchEvent: in refresh area");
//                    if (mRefresh.getTop() >= mRefreshPoint) {
//                        startRefresh();
//                    } else {
//                        finishRefresh();
//                    }
//                } else {
//                    Log.d(TAG, "dispatchTouchEvent: handle by child");
//                    super.dispatchTouchEvent(ev);
//                }
//
//
//                isDragging = false;
//
//
//                break;
//        }
//
//
//        mLastY = ev.getY();
//
//
//        return true;
//
//    }
//
//
//    public boolean isDraggingVertical() {
//        return isDraggingVertical;
//    }
//
//    public boolean isDocked() {
//        return mContent.getTop() == mHeaderTop || mContent.getTop() == mHeaderBottom;
//    }
//
//    public void headerDock(boolean isUp) {
//
//        if (!isUp) {
//            //move down
//            smoothSetHeaderHeight(mContent.getTop(), mHeaderBottom, (long) (ANIMATION_TIME * mSweep));
//        } else {
//            //move up
//            smoothSetHeaderHeight(mContent.getTop(), mHeaderTop, (long) (ANIMATION_TIME * (1 - mSweep)));
//        }
//    }
//
//    public void setHeaderUpdateListener(OnHeaderUpdate listener) {
//        mUpdateListener = listener;
//    }
//
//    public void setCanScrollListener(CanScrollListener listener) {
//        mCanScrollListener = listener;
//    }
//
//    public void setOnRefreshListener(OnRefreshListener listener) {
//        mRefreshListener = listener;
//    }
//
//    public interface OnHeaderUpdate {
//        void onStartUpdate(boolean toCollapse);
//
//        void onUpdate(int total, int current, float factor);
//
//        void onFinishUpdate(boolean isCollapsed, float velocityY);
//    }
//
//    public interface CanScrollListener {
//        boolean canScrollDown();
//    }
//
//    public interface OnRefreshListener {
//        /**
//         * 隐藏刷新界面
//         */
//        void showRefreshView();
//
//        /**
//         * 隐藏刷新界面
//         */
//        void hideRefreshView();
//
//        /**
//         * 刷新回调
//         */
//        void onRefresh();
//
//        /**
//         * @param progress 触发同步的进度
//         */
//        void onRefreshActiveProgress(float progress);
//
//        /**
//         * 达到出发refresh 的条件
//         */
//        void waitRefresh();
//
//        void cancelRefresh();
//    }
//
//}
