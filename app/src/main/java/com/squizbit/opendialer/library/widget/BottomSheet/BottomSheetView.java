package com.squizbit.opendialer.library.widget.BottomSheet;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.squizbit.opendialer.R;

/**
 * A layout which controls the click and dragging of the bottom sheet
 */
public class BottomSheetView extends FrameLayout {

    private Drawable mShadowDrawable;
    private boolean mIsLoadingIn = true;
    private Drawable mNavDrawable;
    private ViewDragHelper mViewDragHelper;
    private BottomSheetSlideAnimationHelper mViewSlideHelper;
    private ScrollView mBottomSheetView;
    private int mBottomSheetStartPosition = 0;
    private int mStatusBarHeight;
    private int mNavigationHeight;
    private OnBottomSheetDismissedListener mOnBottomSheetDismissedListener;
    private float mLastInterceptYCoord;

    /**
     * Creates a new Bottom sheet slide view
     *
     * @param context The activity context
     */
    public BottomSheetView(Context context) {
        super(context, null);
    }

    /**
     * Creates a new Bottom sheet slide view
     *
     * @param context The activity context
     * @param attrs   The attributes set
     */
    public BottomSheetView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mViewDragHelper = ViewDragHelper.create(this, new ViewDragCallback());
        mViewSlideHelper = new BottomSheetSlideAnimationHelper(context);
        mShadowDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.bg_shadow, getContext().getTheme());
        if (mShadowDrawable != null) {
            mShadowDrawable.setAlpha(0);
        }

        ResourcesCompat resourcesCompat = new ResourcesCompat();
        mNavDrawable = new ColorDrawable(resourcesCompat.getColor(getResources(), android.R.color.black, getContext().getTheme()));
        mNavigationHeight = getNavigationBarHeight();
        mStatusBarHeight = getStatusBarHeight();

        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mBottomSheetView = (ScrollView) getChildAt(0);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        mBottomSheetView.measure(
                widthMeasureSpec,
                getChildMeasureSpec(heightMeasureSpec, 0, MeasureSpec.getSize(heightMeasureSpec) - mStatusBarHeight - mNavigationHeight));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mBottomSheetStartPosition =
                getMeasuredHeight() - getContext().getResources().getDimensionPixelOffset(R.dimen.bottomsheet_intial_visiblity);
        getChildAt(0).layout(left, getMeasuredHeight(), right, getMeasuredHeight() + mBottomSheetView.getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        if (mIsLoadingIn) {
            mViewSlideHelper.smoothSlideViewTo(mBottomSheetView, mBottomSheetStartPosition + mStatusBarHeight);
            ViewCompat.postInvalidateOnAnimation(BottomSheetView.this);
            mIsLoadingIn = false;
        }

        super.dispatchDraw(canvas);

        mShadowDrawable.setBounds(getLeft(), 0, getRight(), mBottomSheetView.getTop());
        float shadowAlpha = 1 - (mBottomSheetView.getTop() / (float) getHeight());
        mShadowDrawable.setAlpha((int) (shadowAlpha * 255));
        mShadowDrawable.draw(canvas);
        //Draw navigation blackout bar
        mNavDrawable.setBounds(0, getHeight() - mNavigationHeight, getWidth(), getHeight());
        mNavDrawable.draw(canvas);


    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isViewLockedToTop()) {
            return mViewDragHelper.shouldInterceptTouchEvent(ev);
        }

        //Also intercept if we are scrolling down and the scrollview is at the top
        if(ev.getAction() == MotionEvent.ACTION_MOVE &&
                ev.getY() > mLastInterceptYCoord &&
                mBottomSheetView.getScrollY() == 0){
            return mViewDragHelper.shouldInterceptTouchEvent(ev);
        }

        mLastInterceptYCoord = ev.getY();
        return false;
    }

    /**
     * Determines if a motion event was outside the bottomsheet
     *
     * @param event The motion event to test against
     * @return True if the motion event was outside the bottomsheet, false otherwise
     */
    public boolean wasTouchEventOutsideBottomSheet(MotionEvent event) {
        return event.getY() < mBottomSheetView.getTop();
    }

    private boolean isViewLockedToTop() {
        return getChildAt(0).getTop() == mStatusBarHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (wasTouchEventOutsideBottomSheet(event)) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            mViewDragHelper.processTouchEvent(event);
            return false;
        }

        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

        if (mViewSlideHelper.continueSettling()) {
            ViewCompat.postInvalidateOnAnimation(this);
        }


        if (!mViewSlideHelper.isAnimating() && !mIsLoadingIn && mBottomSheetView.getTop() >= getHeight() - mNavigationHeight) {
            if (mOnBottomSheetDismissedListener != null) {
                mOnBottomSheetDismissedListener.onBottomSheetDismissed();
            }
        }
    }

    public void setOnBottomSheetDismissedListener(OnBottomSheetDismissedListener onBottomSheetDismissedListener) {
        mOnBottomSheetDismissedListener = onBottomSheetDismissedListener;
    }

    private int getNavigationBarHeight() {
        boolean hasNavigationBarHeight;

        int id = getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        hasNavigationBarHeight = id > 0 && getResources().getBoolean(id);

        int orientation = getResources().getConfiguration().orientation;

        int dimenResId = getResources().getIdentifier(
                orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape",
                "dimen", "android");

        if (hasNavigationBarHeight && id > 0) {
            return getResources().getDimensionPixelSize(dimenResId);
        } else {
            return 0;
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void snapToTop() {
        boolean smoothScroll = mViewDragHelper.smoothSlideViewTo(
                mBottomSheetView,
                0,
                mStatusBarHeight);

        if (smoothScroll) {
            Log.e("TEST", "in snap to top");
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void snapToBottom() {
        boolean smoothScroll = mViewDragHelper.smoothSlideViewTo(
                mBottomSheetView,
                0,
                getHeight());

        if (smoothScroll) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void close() {
        snapToBottom();
    }

    /**
     * A class which provides view drag callbacks for the BottomViewSheet
     */
    private class ViewDragCallback extends Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return mBottomSheetView == child;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return Math.max(top, mStatusBarHeight);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return child == mBottomSheetView ? mBottomSheetView.getHeight() : 0;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (yvel < 0) {
                snapToTop();
            } else if (yvel > 0) {
                snapToBottom();
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            ViewCompat.postInvalidateOnAnimation(BottomSheetView.this);
        }
    }

    /**
     * A callback which is invoked when the bottoms heet has been dismissed
     */
    public interface OnBottomSheetDismissedListener {
        /**
         * Invoked when the bottom sheet has been dismissed
         */
        void onBottomSheetDismissed();
    }

}
