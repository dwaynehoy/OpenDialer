package com.squizbit.opendialer.library.widget.BottomSheet;

import android.content.Context;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * A slide helper class which helps in creating a start up slide animation for the bottomsheet library
 */
public class BottomSheetSlideHelper {
    private static final int SLIDE_DURATION = 300;
    private View mTargetView;
    private Scroller mScroller;
    private boolean mIsAnimating = false;
    private int mStartY;
    private int mEndY;

    /**
     * Interpolator defining the animation curve for mScroller
     */
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    /**
     * Creates a new BottomSheetSlide helper
     * @param context
     */
    public BottomSheetSlideHelper(Context context){
        mScroller = new Scroller(context, sInterpolator);
    }

    /**
     * Smoothly slides the view to the final position of y
     * @param view The view to slide
     * @param y The position it should slide to
     */
    public void smoothSlideViewTo(View view, int y){
        mStartY = view.getTop();
        mEndY = y;
        mTargetView = view;

        int dy = mEndY - mStartY;
        mScroller.startScroll(0, mStartY, 0, dy, SLIDE_DURATION);
        mIsAnimating = true;
    }

    /**
     * Calculates the next step of the animation and determines if the animating view had settled
     * or still needs to be animated
     * @return True if continueSetting should be called again, false otherwise
     */
    public boolean continueSettling(){
        if(mTargetView == null || !mIsAnimating){
            return false;
        }

        boolean keepGoing = mScroller.computeScrollOffset();
        int y = mScroller.getCurrY();
        int dy = y - mTargetView.getTop();

        if(dy != 0){
            mTargetView.offsetTopAndBottom(dy);
        }

        if(y == mScroller.getFinalY()){
            mScroller.abortAnimation();
            keepGoing = false;
        }

        if(!keepGoing){
            mIsAnimating = false;
        }

        return keepGoing;
    }

    /**
     * Determines if the view is currently animating or not
     * @return True if the view is animating, false if it's reached it's final position.
     */
    public boolean isAnimating() {
        return false;
    }
}
