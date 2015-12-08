package com.squizbit.opendialer.library.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

/**
 * Provides an extended ImageView that respects the bounds change flag even when the image is smaller than
 * the expected ImageView boujnds
 */
public class ImageView extends android.widget.ImageView {

    private boolean mDoAdjustViewBound;
    private int mMaxHeight;

    /**
     * Creates a new instance of the image view
     *
     * @param context An activity context
     */
    public ImageView(Context context) {
        super(context);
    }

    /**
     * Creates a new instance of the image view
     *
     * @param context An activity context
     * @param attrs   The style attribute set
     */
    public ImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Creates a new instance of the image view
     *
     * @param context      An activity context
     * @param attrs        The style attribute set
     * @param defStyleAttr The default style resource id
     */
    public ImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public ImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        mDoAdjustViewBound = adjustViewBounds;
        super.setAdjustViewBounds(adjustViewBounds);
    }

    @Override
    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
        super.setMaxHeight(maxHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int drawableWidth = getDrawable().getIntrinsicWidth();
        int drawableHeight = getDrawable().getIntrinsicHeight();

        int w = 0;
        int h = 0;

        // Desired aspect ratio of the view's contents (not including padding)
        float desiredAspect = 0.0f;

        // We are allowed to change the view's width
        boolean resizeWidth = false;

        // We are allowed to change the view's height
        boolean resizeHeight = false;

        if (drawableWidth > 0) {
            w = drawableWidth;
            h = drawableHeight;
            if (w <= 0) w = 1;
            if (h <= 0) h = 1;

            // We are supposed to adjust view bounds to match the aspect
            // ratio of our drawable. See if that is possible.
            if (mDoAdjustViewBound) {

                int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
                int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

                resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
                resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;

                desiredAspect = (float) w / (float) h;
            }
        }

        int pleft = getPaddingLeft();
        int pright = getPaddingRight();
        int ptop = getPaddingTop();
        int pbottom = getPaddingBottom();

        int widthSize;
        int heightSize;

        if (resizeWidth || resizeHeight) {
            /* If we get here, it means we want to resize to match the
                drawables aspect ratio, and we have the freedom to change at
			    least one dimension.
			*/

            // Get the max possible width given our constraints
            widthSize = resolveAdjustedSize(w + pleft + pright,
                    mMaxHeight, widthMeasureSpec);

            // Get the max possible height given our constraints
            heightSize = resolveAdjustedSize(h + ptop + pbottom,
                    mMaxHeight, heightMeasureSpec);

            if (desiredAspect != 0.0f) {
                // See what our actual aspect ratio is
                float actualAspect = (float) (widthSize - pleft - pright) /
                        (heightSize - ptop - pbottom);

                if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {

                    boolean done = false;

                    // Try adjusting width to be proportional to height
                    if (resizeWidth) {
                        int newWidth = (int) (desiredAspect *
                                (heightSize - ptop - pbottom))
                                + pleft + pright;
                        if (newWidth <= widthSize) {
                            widthSize = newWidth;
                            done = true;
                        }
                    }

                    // Try adjusting height to be proportional to width
                    if (!done && resizeHeight) {

                        heightSize = (int)
                                ((widthSize - pleft - pright) / desiredAspect) + ptop + pbottom;
                    }
                }
            }
        } else {
            /* We are either don't want to preserve the drawables aspect ratio,
               or we are not allowed to change view dimensions. Just measure in
			   the normal way.
			*/
            w += pleft + pright;
            h += ptop + pbottom;

            w = Math.max(w, getSuggestedMinimumWidth());
            h = Math.max(h, getSuggestedMinimumHeight());

            widthSize = resolveSize(w, widthMeasureSpec);
            heightSize = resolveSize(h, heightMeasureSpec);
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    private int resolveAdjustedSize(int desiredSize, int maxSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
			/* Parent says we can be as big as we want. Just don't be larger
			than max size imposed on ourselves.
			*/
                result = Math.min(desiredSize, maxSize);
                break;
            case MeasureSpec.AT_MOST:
                // Parent says we can be as big as we want, up to specSize.
                // Don't be larger than specSize, and don't be larger than
                // the max size imposed on ourselves.
                result = Math.min(Math.min(desiredSize, specSize), maxSize);
                break;
            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }
}
