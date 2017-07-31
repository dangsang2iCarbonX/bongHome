

package cn.hackill.bong;

import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by chenupt@gmail.com on 2015/1/18.
 * Description : Drag down to show a menu panel on the top.
 */
public class DragTopLayout extends FrameLayout {

    private static final String TAG = "DragTopLayout";

    private ViewDragHelper dragHelper;
    private int dragRange;
    private View dragContentView;
    private View topView;

    private int contentTop; //内容顶部
    private int topViewHeight;
    private float ratio;
    private boolean isRefreshing;
    private boolean shouldIntercept = true;

    private PanelListener panelListener;
    private float refreshRatio = 1.5f;
    private boolean overDrag = false;
    private int collapseOffset; // 不能折叠的
    private boolean captureTop = true;

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

        static PanelState fromInt(int i) {
            switch (i) {
                case 0:
                    return COLLAPSED;
                case 2:
                    return SLIDING;
                default:
                case 1:
                    return EXPANDED;
            }
        }


        public int toInt() {
            return asInt;
        }
    }


    public DragTopLayout(Context context) {
        this(context, null);
    }

    public DragTopLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragTopLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
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

        topView = getChildAt(0);
        dragContentView = getChildAt(1);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        dragRange = getHeight();

        // In case of resetting the content top to target position before sliding.
        measureTopViewHeight();
        measureContentHeight();

//        Log.i(TAG, "onLayout: contentTop = " + contentTop +", dragRange " + dragRange +",dragContentView.getHeight() = "+ dragContentView.getHeight());

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
            if (ratio > refreshRatio && !isRefreshing) {
                isRefreshing = true;
                panelListener.onRefresh();
            }
        }
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

    @Override
    protected Parcelable onSaveInstanceState() {

        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);
        state.panelState = panelState.toInt();

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE);
            return;
        }

        SavedState s = (SavedState) state;
        super.onRestoreInstanceState(s.getSuperState());

        this.panelState = PanelState.fromInt(s.panelState);
        if (panelState == PanelState.COLLAPSED) {
            closeTopView(false);
        } else {
            openTopView(false);
        }
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            Log.i(TAG, "tryCaptureView: ");

            if (child == topView && captureTop) {
                dragHelper.captureChildView(dragContentView, pointerId);
                return false;
            }
            return child == dragContentView;
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

            float value = Math.min(topViewHeight, Math.max(top, getPaddingTop() + collapseOffset));

            Log.i(TAG, "clampViewPositionVertical: value = " + value + ", top = " + top + ", dy = " + dy);

            return Math.min(topViewHeight * 2, Math.max(top, getPaddingTop() + collapseOffset));

//            if (overDrag) {
//                // Drag over the top view height.
//                return Math.max(top, getPaddingTop() + collapseOffset);
//            } else {
//                return Math.min(topViewHeight, Math.max(top, getPaddingTop() + collapseOffset));
//            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            Log.d(TAG, "onViewReleased() called with: releasedChild = [" + releasedChild + "], xvel = [" + xvel + "], yvel = [" + yvel + "]");
            // yvel > 0 Fling down || yvel < 0 Fling up
            int top;
            if (yvel > 0 || contentTop > topViewHeight) {
                top = topViewHeight + getPaddingTop();
            } else {
                top = getPaddingTop() + collapseOffset;
            }
            dragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
            postInvalidate();
        }

        @Override
        public void onViewDragStateChanged(int state) {
            Log.i(TAG, "onViewDragStateChanged: ");
            super.onViewDragStateChanged(state);
        }
    };

    @Override
    public void computeScroll() {
//        Log.i(TAG, "computeScroll: ");
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {

            boolean intercept = shouldIntercept && dragHelper.shouldInterceptTouchEvent(ev);
            return intercept;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = MotionEventCompat.getActionMasked(event);

        if (!dispatchingChildrenContentView) {
            try {
                // There seems to be a bug on certain devices: "pointerindex out of range" in viewdraghelper
                // https://github.com/umano/AndroidSlidingUpPanel/issues/351
                dragHelper.processTouchEvent(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (action == MotionEvent.ACTION_MOVE && ratio == 0.0f) {
            dispatchingChildrenContentView = true;
            if (!dispatchingChildrenDownFaked) {
                dispatchingChildrenStartedAtY = event.getY();
                event.setAction(MotionEvent.ACTION_DOWN);
                dispatchingChildrenDownFaked = true;
            }
            dragContentView.dispatchTouchEvent(event);
        }

        if (dispatchingChildrenContentView && dispatchingChildrenStartedAtY < event.getY()) {
            resetDispatchingContentView();
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            resetDispatchingContentView();
            dragContentView.dispatchTouchEvent(event);
        }

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

    public DragTopLayout setTouchMode(boolean shouldIntercept) {
        this.shouldIntercept = shouldIntercept;
        return this;
    }

    /**
     * Setup the drag listener.
     *
     * @return SetupWizard
     */
    public DragTopLayout listener(PanelListener panelListener) {
        this.panelListener = panelListener;
        return this;
    }

    /**
     * Set the refresh position while dragging you want.
     * The default value is 1.5f.
     *
     * @return SetupWizard
     */
    public DragTopLayout setRefreshRatio(float ratio) {
        this.refreshRatio = ratio;
        return this;
    }

    /**
     * Set enable drag over.
     * The default value is true.
     *
     * @return SetupWizard
     */
    public DragTopLayout setOverDrag(boolean overDrag) {
        this.overDrag = overDrag;
        return this;
    }


    public boolean isOverDrag() {
        return overDrag;
    }

    /**
     * Get refresh state
     */
    public boolean isRefreshing() {
        return isRefreshing;
    }

    public void setRefreshing(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;
    }

    /**
     * Complete refresh and reset the refresh state.
     */
    public void onRefreshComplete() {
        isRefreshing = false;
    }

    /**
     * Set the collapse offset
     *
     * @return SetupWizard
     */
    public DragTopLayout setCollapseOffset(int px) {
        collapseOffset = px;
        measureContentHeight();
        return this;
    }

    public int getCollapseOffset() {
        return collapseOffset;
    }


    // ---------------------

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

    /**
     * Save the instance state
     */
    private static class SavedState extends BaseSavedState {

        int panelState;

        SavedState(Parcelable superState) {
            super(superState);
        }

    }
}
