

package cn.hackill.bong;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class HomeLayout extends FrameLayout {

    private static final String TAG = "HomeLayout";

    private ViewDragHelper dragHelper;
    private int dragRange;
    private View dragContentView;
    private TopLayout topView;

    private int contentTop; //内容顶部
    private int topViewHeight;
    private float ratio;
    private boolean shouldIntercept = true;

    private PanelListener panelListener;
    private int collapseOffset = 0; // 不能折叠的

    // Used for scrolling
    private boolean dispatchingChildrenDownFaked = false;
    private boolean dispatchingChildrenContentView = false;
    private float dispatchingChildrenStartedAtY = Float.MAX_VALUE;

    private PanelState panelState = PanelState.EXPANDED;

    public enum PanelState {

        COLLAPSED(0),
        EXPANDED(1),
        SLIDING(2);

        private int asInt;

        PanelState(int i) {
            this.asInt = i;
        }

    }


    public HomeLayout(Context context) {
        this(context, null);
    }

    public HomeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dragHelper = ViewDragHelper.create(this, 1.0f, callback);
        // 不能折叠的距离 default = 0
        setCollapseOffset(0);
        //默认展开还是折叠
        initOpen(true);
    }

    private void initOpen(boolean initOpen) {
        if (initOpen) {
            panelState = PanelState.EXPANDED;
        } else {
            panelState = PanelState.COLLAPSED;
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        if (getChildCount() < 2) {
            throw new RuntimeException("Content view must contains two child views at least.");
        }

        topView = (TopLayout) getChildAt(0);
        dragContentView = getChildAt(1);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        dragRange = getHeight();

        // In case of resetting the content top to target position before sliding.
        measureTopViewHeight();
        measureContentHeight();

        Log.d(TAG, "onLayout: contentTop = " + contentTop + ", dragRange " + dragRange + ",dragContentView.getHeight() = " + dragContentView.getHeight());

        topView.layout(left, Math.min(topView.getPaddingTop(), contentTop - topViewHeight), right,
                contentTop);

        dragContentView.layout(left, contentTop, right,
                contentTop + dragContentView.getHeight());
    }

    private void measureTopViewHeight() {
        int newTopHeight = topView.getHeight();
        // Top layout is changed
        if (topViewHeight != newTopHeight) {

            if (panelState == PanelState.EXPANDED) {
                // 展开情况下 内容顶部位置
                contentTop = newTopHeight;
                handleSlide(newTopHeight);
            } else if (panelState == PanelState.COLLAPSED) {
                //折叠情况下 顶部位置
                contentTop = collapseOffset;

            }
            topViewHeight = newTopHeight;
        }
    }

    private void measureContentHeight() {
        if (dragContentView != null && dragContentView.getHeight() != 0) {
            ViewGroup.LayoutParams layoutParams = dragContentView.getLayoutParams();
            layoutParams.height = getHeight() - collapseOffset;
            dragContentView.setLayoutParams(layoutParams);
        }
    }

    private void handleSlide(final int top) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                dragHelper.smoothSlideViewTo(dragContentView, getPaddingLeft(), top);
                postInvalidate();
            }
        });
    }

    private void resetDragContent(boolean anim, int top) {
        contentTop = top;
        if (anim) {
            dragHelper.smoothSlideViewTo(dragContentView, getPaddingLeft(), contentTop);
            postInvalidate();
        } else {
            requestLayout();
        }
    }

    private void calculateRatio(float top) {
        ratio = (top - collapseOffset) / (topViewHeight - collapseOffset);
        if (dispatchingChildrenContentView) {
            resetDispatchingContentView();
        }

        if (panelListener != null) {
            // Calculate the ratio while dragging.
            panelListener.onSliding(ratio);
        }
//        Log.i(TAG, "calculateRatio: ...ratio = " + ratio);
    }

    private void updatePanelState() {
        if (contentTop <= getPaddingTop() + collapseOffset) {
            panelState = PanelState.COLLAPSED;
        } else if (contentTop >= topView.getHeight()) {
            panelState = PanelState.EXPANDED;
        } else {
            panelState = PanelState.SLIDING;
        }

        if (panelListener != null) {
            panelListener.onPanelStateChanged(panelState);
        }
    }

    private boolean needInter = false;

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {

            Log.i(TAG, "tryCaptureView: child = " + child + ",pointerId = " + pointerId);

            if (child == topView) {
                dragHelper.captureChildView(dragContentView, pointerId);
                return false;
            }
            return child == dragContentView && shouldIntercept;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            Log.d(TAG, "onViewPositionChanged() called with: changedView = [" + changedView + "], left = [" + left + "], top = [" + top + "], dx = [" + dx + "], dy = [" + dy + "]");
            contentTop = top;
            requestLayout();
            calculateRatio(contentTop);
            updatePanelState();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return dragRange;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {

            Log.i(TAG, "clampViewPositionVertical: top = " + top);

            if (top < 0 && contentTop > 0) {
                needInter = true;
            } else {
                needInter = false;
            }

            boolean isRefreshing = topView.getLoadingView().isRefresh();
//            if (isRefreshing) {
//                return Math.min(topViewHeight, Math.max(top, getPaddingTop() + collapseOffset));
//            }

            return (int) Math.min(1.22f * topViewHeight, Math.max(top, getPaddingTop() + collapseOffset));
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            Log.d(TAG, "onViewReleased() called with: releasedChild = [" + releasedChild.getId() + "], xvel = [" + xvel + "], yvel = [" + yvel + "]" + ", contentTop = " + contentTop);
            // yvel > 0 Fling down || yvel < 0 Fling up
            int top;
            if (yvel > 0 || contentTop > topViewHeight / 2) {
                top = topViewHeight + getPaddingTop();
            } else {
                top = getPaddingTop() + collapseOffset;
            }
            dragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
            topView.getLoadingView().releaseFinger();
            postInvalidate();
        }

        @Override
        public void onViewDragStateChanged(int state) {
            Log.i(TAG, "onViewDragStateChanged: state = " + state);
            super.onViewDragStateChanged(state);
        }
    };

    @Override
    public void computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

//        Log.i(TAG, "dispatchTouchEvent:  dragHelper.shouldInterceptTouchEvent(ev) = " +  dragHelper.shouldInterceptTouchEvent(ev));

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {

            boolean intercept = shouldIntercept && dragHelper.shouldInterceptTouchEvent(ev);
            Log.i(TAG, "onInterceptTouchEvent:   intercept = " + intercept + ", shouldIntercept = " + shouldIntercept + ", needInter = " + needInter);
            return intercept || needInter;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = MotionEventCompat.getActionMasked(event);

        Log.i(TAG, "onTouchEvent: dispatchingChildrenContentView = " + dispatchingChildrenContentView);
        if (!dispatchingChildrenContentView) {
            try {
                // There seems to be a bug on certain devices: "pointerindex out of range" in viewdraghelper
                // https://github.com/umano/AndroidSlidingUpPanel/issues/351
                dragHelper.processTouchEvent(event); //EXPAND的时候才执行，否则是contentView自己的滑动事件？
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // ratio == 0, when top = 0, COLLAPSED
        // test：以下是处理特殊情况的code，COLLAPSED时先向上在快速向下滑动能保证回到首条后topview能Expand
        if (action == MotionEvent.ACTION_MOVE && ratio == 0.0f) {
            dispatchingChildrenContentView = true;// true 表明contentView自己去处理滑动事件
            if (!dispatchingChildrenDownFaked) {
                dispatchingChildrenStartedAtY = event.getY();// 记录contentView开始处理滑动的初始坐标
                event.setAction(MotionEvent.ACTION_DOWN);
                dispatchingChildrenDownFaked = true;
            }
            dragContentView.dispatchTouchEvent(event);
        }
        // dispatchingChildrenStartedAtY 变量的作用：你接管滑动的初始坐标小于滑动到的坐标时，不再接管，事件交由ViewDragHelper
        if (dispatchingChildrenContentView && dispatchingChildrenStartedAtY < event.getY()) {
            resetDispatchingContentView();
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            resetDispatchingContentView();// 结束性的事件，恢复
            dragContentView.dispatchTouchEvent(event);
        }
        // true标记touch事件已经被消费掉，不再向下传递
        return true;
    }

    private void resetDispatchingContentView() {
        dispatchingChildrenDownFaked = false;
        dispatchingChildrenContentView = false;
        dispatchingChildrenStartedAtY = Float.MAX_VALUE;
    }


    //================
    // public
    //================

    public PanelState getState() {
        return panelState;
    }

    public void openTopView(boolean anim) {
        // Before created
        if (dragContentView.getHeight() == 0) {
            panelState = PanelState.EXPANDED;
            if (panelListener != null) {
                panelListener.onSliding(1.0f);
            }
        } else {
            resetDragContent(anim, topViewHeight);
        }
    }

    public void closeTopView(boolean anim) {
        if (dragContentView.getHeight() == 0) {
            panelState = PanelState.COLLAPSED;
            if (panelListener != null) {
                panelListener.onSliding(0.0f);
            }
        } else {
            resetDragContent(anim, getPaddingTop() + collapseOffset);
        }
    }

    public void toggleTopView() {
        toggleTopView(false);
    }

    public void toggleTopView(boolean touchMode) {
        switch (panelState) {
            case COLLAPSED:
                openTopView(true);
                if (touchMode) {
                    setTouchMode(true);
                }
                break;
            case EXPANDED:
                closeTopView(true);
                if (touchMode) {
                    setTouchMode(false);
                }
                break;
        }
    }

    public HomeLayout setTouchMode(boolean shouldIntercept) {
        this.shouldIntercept = shouldIntercept;
        return this;
    }

    /**
     * Setup the drag listener.
     *
     * @return SetupWizard
     */
    public HomeLayout listener(PanelListener panelListener) {
        this.panelListener = panelListener;
        return this;
    }


    public void setRefreshing(boolean isRefreshing) {
        topView.getLoadingView().setRefresh(isRefreshing);
    }

    /**
     * Set the collapse offset
     *
     * @return SetupWizard
     */
    public HomeLayout setCollapseOffset(int px) {
        collapseOffset = px;
        measureContentHeight();
        return this;
    }

    public interface PanelListener {
        /**
         * Called while the panel state is changed.
         */
        void onPanelStateChanged(PanelState panelState);

        /**
         * Called while dragging.
         * ratio >= 0.
         */
        void onSliding(float ratio);

        /**
         * Called while the ratio over refreshRatio.
         */
        void onRefresh();
    }


}
