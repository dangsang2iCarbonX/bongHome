package cn.hackill.bong;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.tuesda.walker.circlerefresh.R;


@Deprecated
public class CircleRefreshLayout extends LinearLayout {

    private static String TAG = "pullToRefresh";

    private static final long BACK_TOP_DUR = 600;
    private static final long REL_DRAG_DUR = 200;

    private int mHeaderBackColor = 0xff8b90af;
    private int mHeaderForeColor = 0xffffffff;
    private int mHeaderCircleSmaller = 6;


    private float mPullHeight;
    private float mHeaderHeight;
    private View mChildView;
    private AnimationView mHeaderAnimationView;

    private boolean mIsRefreshing;

    private float mTouchStartY;

    private ValueAnimator mUpTopAnimator;


    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(10);

    public CircleRefreshLayout(Context context) {
        this(context, null, 0);
    }

    public CircleRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {


        setOrientation(VERTICAL);

        setAttrs(attrs);
        mPullHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());
        mHeaderHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());

        this.post(new Runnable() {
            @Override
            public void run() {
                if (getChildCount() > 1) {
                    throw new RuntimeException("you can only attach one child");
                }
                mChildView = getChildAt(0);
                addHeaderView();
            }
        });

    }

    private void setAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CirCleRefreshLayout);

        mHeaderBackColor = a.getColor(R.styleable.CirCleRefreshLayout_AniBackColor, mHeaderBackColor);
        mHeaderForeColor = a.getColor(R.styleable.CirCleRefreshLayout_AniForeColor, mHeaderForeColor);
        mHeaderCircleSmaller = a.getInt(R.styleable.CirCleRefreshLayout_CircleSmaller, mHeaderCircleSmaller);

        a.recycle();
    }

    private void addHeaderView() {
        mHeaderAnimationView = new AnimationView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
//        params.gravity = Gravity.TOP;
        mHeaderAnimationView.setLayoutParams(params);

//        addViewInternal(mHeaderAnimationView);
        addView(mHeaderAnimationView);

//        mChildView.setTranslationY(250);

        mHeaderAnimationView.setAniBackColor(mHeaderBackColor);
        mHeaderAnimationView.setAniForeColor(mHeaderForeColor);
        mHeaderAnimationView.setRadius(mHeaderCircleSmaller);

        setUpChildAnimation();
    }

    private void setUpChildAnimation() {
        if (mChildView == null) {
            return;
        }

        mUpTopAnimator = ValueAnimator.ofFloat(mHeaderHeight, 0);
        mUpTopAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                Log.i(TAG, "onAnimationUpdate: ，。。。。");
                
                float val = (float) animation.getAnimatedValue();
                val = decelerateInterpolator.getInterpolation(val / mHeaderHeight) * val;
                if (mChildView != null) {
                    mChildView.setTranslationY(val);
                }
                mHeaderAnimationView.getLayoutParams().height = (int) val ;
                mHeaderAnimationView.requestLayout();
            }
        });
        mUpTopAnimator.setDuration(BACK_TOP_DUR);

        mHeaderAnimationView.setOnViewAniDone(new AnimationView.OnViewAniDone() {
            @Override
            public void viewAniDone() {
                Log.i(TAG, "viewAniDone: 。。。。");
                mUpTopAnimator.start();
            }
        });
    }

    private void upLayoutBack(float startHeight) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startHeight, mHeaderHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                if (mChildView != null) {
                    mChildView.setTranslationY(val);
                }
            }
        });
        valueAnimator.setDuration(REL_DRAG_DUR);
        valueAnimator.start();
    }

//    private void addViewInternal(@NonNull View child) {
//        super.addView(child);
//    }

// 手动添加view
//    @Override
//    public void addView(View child) {
//        Log.d(TAG, "addView() called with: child = [" + child + "]");
//
//        if (getChildCount() >= 1) {
//            throw new RuntimeException("you can only attach one child");
//        }
//
//        mChildView = child;
//        super.addView(child);
//        setUpChildAnimation();
//    }

    private boolean canChildScrollUp() {
        if (mChildView == null) {
            return false;
        }

        return ViewCompat.canScrollVertically(mChildView, -1);
    }


    private MotionEvent mLastMoveEvent;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.d(TAG, "dispatchTouchEvent: dy = " + 0 + ", canScrollup = " + canChildScrollUp() + ", 2 = " + mChildView.canScrollVertically(-1) + ", 3= " + canChildScrollUp(mChildView));
        if (mChildView == null) {
            return super.dispatchTouchEvent(event);
        }

        if (mIsRefreshing || mHeaderAnimationView.isClosing()) {
            Log.e(TAG, "onInterceptTouchEvent: ...refreshing.....");
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartY = event.getY();
                super.dispatchTouchEvent(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = event;
                float curY = event.getY();
                float curX = event.getX();

                float deltaY = curY - mTouchStartY;
                Log.i(TAG, "onInterceptTouchEvent: dy = " + deltaY + ", canScrollup = " + canChildScrollUp());

                if (deltaY > 0 && !canChildScrollUp()) {

                    deltaY = Math.min(mPullHeight * 2, deltaY);
                    deltaY = Math.max(0, deltaY);

//                    mHeaderAnimationView.setWidthOffset(curX / mChildView.getWidth());

                    float offsetY = decelerateInterpolator.getInterpolation(deltaY / 2 / mPullHeight) * deltaY / 2;
                    mChildView.setTranslationY(offsetY);
                    mHeaderAnimationView.getLayoutParams().height = (int) offsetY + 0;
                    mHeaderAnimationView.requestLayout();
                    sendCancelEvent();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                if (mChildView.getTranslationY() >= mHeaderHeight) {
                    upLayoutBack(mHeaderAnimationView.getViewHeight());
                    mHeaderAnimationView.releaseDrag();
                    mIsRefreshing = true;
                    if (onCircleRefreshListener != null) {
                        onCircleRefreshListener.refreshing();
                    }

                } else {
                    float height = mChildView.getTranslationY();
                    Log.i(TAG, "dispatchTouchEvent: .... height = " + height);
                    if (height > 0) {
                        ValueAnimator backTopAni = ValueAnimator.ofFloat(height, 0);
                        backTopAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float val = (float) animation.getAnimatedValue();
                                val = decelerateInterpolator.getInterpolation(val / mHeaderHeight) * val;
                                if (mChildView != null) {
                                    mChildView.setTranslationY(val);
                                }
                                mHeaderAnimationView.getLayoutParams().height = (int) val;
                                mHeaderAnimationView.requestLayout();
                            }
                        });
                        backTopAni.setDuration((long) (height * BACK_TOP_DUR / mHeaderHeight));
                        backTopAni.start();
                    }
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void sendCancelEvent() {
        // The ScrollChecker will update position and lead to send cancel event when mLastMoveEvent is null.
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    public static boolean canChildScrollUp(View view) {
        if (view instanceof AbsListView) {
            final AbsListView absListView = (AbsListView) view;
            return absListView.getChildCount() > 0
                    && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                    .getTop() < absListView.getPaddingTop());
        } else {
            return view.getScrollY() > 0;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void finishRefreshing() {
        if (onCircleRefreshListener != null) {
            onCircleRefreshListener.completeRefresh();
        }
        mIsRefreshing = false;
        mHeaderAnimationView.setRefreshing(false);
    }

    private OnCircleRefreshListener onCircleRefreshListener;

    public void setOnRefreshListener(OnCircleRefreshListener onCircleRefreshListener) {
        this.onCircleRefreshListener = onCircleRefreshListener;
    }

    public interface OnCircleRefreshListener {
        void completeRefresh();

        void refreshing();
    }
}
