package com.squizbit.opendialer.library.widget.BottomSheet;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.squizbit.opendialer.R;

/**
 * A layout which controls the click and dragging of the bottom sheet
 */
public class BottomSheetView extends FrameLayout {

    private Drawable mShadowDrawable;
    private boolean mIsLoadingIn = true;
    private Drawable mNavDrawable;
    private ViewDragHelper mViewDragHelper;
    private BottomSheetSlideHelper mViewSlideHelper;
    private ViewGroup mBottomSheetView;
    private int mBottomSheetStartPosition = 0;
    private int mStatusBarHeight;
    private int mNavigationHeight;
    private OnBottomSheetDismissedListener mOnBottomSheetDismissedListener;

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
        mViewSlideHelper = new BottomSheetSlideHelper(context);

        mShadowDrawable = getResources().getDrawable(R.drawable.bg_shadow);
        mShadowDrawable.setAlpha(0);

        mNavDrawable = new ColorDrawable(getResources().getColor(android.R.color.black));
        mNavigationHeight = getNavigationBarHeight();
        mStatusBarHeight = getResources().getDimensionPixelOffset(R.dimen.statusbar_height);

        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mBottomSheetView = (ViewGroup) getChildAt(0);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mBottomSheetStartPosition =
                getMeasuredHeight() - getContext().getResources().getDimensionPixelOffset(R.dimen.bottomsheet_intial_visiblity);
        getChildAt(0).layout(left, getMeasuredHeight() - mNavigationHeight, right, getMeasuredHeight() + getMeasuredHeight() - mNavigationHeight);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        if(mIsLoadingIn){
            mViewSlideHelper.smoothSlideViewTo(mBottomSheetView, mBottomSheetStartPosition + mStatusBarHeight);
            ViewCompat.postInvalidateOnAnimation(BottomSheetView.this);
            mIsLoadingIn = false;
        }

        super.dispatchDraw(canvas);

        mShadowDrawable.setBounds(getLeft(), 0, getRight(), mBottomSheetView.getTop());
        float shadowAlpha = 1 - (mBottomSheetView.getTop() / (float) getHeight());
        mShadowDrawable.setAlpha((int) (shadowAlpha * 255));
        mShadowDrawable.draw(canvas);
        mNavDrawable.setBounds(0, getHeight() - mNavigationHeight, getWidth(), getHeight());
        mNavDrawable.draw(canvas);


    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    /**
     * Determines if a motion event was outside the bottomsheet
     * @param event The motion event to test against
     * @return True if the motion event was outside the bottomsheet, false otherwise
     */
    public boolean wasTouchEventOutsideBottomSheet(MotionEvent event){
        return  event.getY() < mBottomSheetView.getTop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(wasTouchEventOutsideBottomSheet(event)){
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            mViewDragHelper.processTouchEvent(event);
            return false;
        } else {
            mViewDragHelper.processTouchEvent(event);
        }

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


        if(!mViewSlideHelper.isAnimating() && !mIsLoadingIn && mBottomSheetView.getTop() >= getHeight() - mNavigationHeight){
            if(mOnBottomSheetDismissedListener != null){
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
        if (id > 0) {
            hasNavigationBarHeight = getResources().getBoolean(id);
        }
        else {
            hasNavigationBarHeight = false;
        }

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

    private void snapToTop(){
        boolean smoothScroll = mViewDragHelper.smoothSlideViewTo(
                mBottomSheetView,
                0,
                mStatusBarHeight);

        if (smoothScroll) {
            Log.e("TEST", "in snap to top");
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void snapToBottom(){
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
            if(yvel < 0) {
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
