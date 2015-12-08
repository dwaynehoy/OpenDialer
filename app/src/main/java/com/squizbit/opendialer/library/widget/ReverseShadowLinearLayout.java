package com.squizbit.opendialer.library.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.squizbit.opendialer.R;

/**
 * A linear layout which provides a shadow along the top most edge of the layout.
 */
public class ReverseShadowLinearLayout extends LinearLayout {

    private Drawable mGradientDrawable;
    private Drawable mBackground;
    private int mGradientHeight;

    /**
     * Creates a new ReverseShadowLinearLayout instance
     * @param context A valid context
     */
    public ReverseShadowLinearLayout(Context context) {
        super(context);
        init();
    }

    /**
     * Creates a new ReverseShadowLinearLayout instance
     * @param context A valid context
     * @param attrs The attribute set to set the theme from
     */
    public ReverseShadowLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Creates a new ReverseShadowLinearLayout instance
     * @param context A valid context
     * @param attrs The attribute set to set the theme from
     * @param defStyleAttr A resource id for the default style attributes
     */
    public ReverseShadowLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ReverseShadowLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        setWillNotDraw(false);
        mGradientHeight = getResources().getDimensionPixelOffset(R.dimen.large_shadow_height);
                setPadding(
                getPaddingLeft(),
                getPaddingTop() + mGradientHeight,
                getPaddingRight(),
                getPaddingBottom());
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            mGradientDrawable = getResources().getDrawable(R.drawable.bg_reverse_shadow_gradient, null);
            mBackground = getResources().getDrawable(R.color.bg_window_light, null);
        } else {
            //noinspection deprecation
            mGradientDrawable = getResources().getDrawable(R.drawable.bg_reverse_shadow_gradient);
            //noinspection deprecation
            mBackground = getResources().getDrawable(R.color.bg_window_light);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        mBackground.setBounds(0, mGradientHeight, getWidth(), getHeight());
        mBackground.draw(canvas);
        mGradientDrawable.setBounds(0, 0, getWidth(), mGradientHeight);
        mGradientDrawable.draw(canvas);
        super.onDraw(canvas);
    }
}
