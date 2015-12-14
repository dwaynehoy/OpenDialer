package com.squizbit.opendialer.library.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

/**
 * A layout which provides a layout and event listeners for a number pad dial key
 */
public class DialLayout extends LinearLayout {
    private OnDialKeyPressStatusListener mDialKeyPressStatusListener;
    private Handler mLongPressHandler;

    private Runnable mLongPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mDialKeyPressStatusListener != null) {
                mDialKeyPressStatusListener.onDialKeyPressedStateChanged(DialLayout.this, true, true);
            }
        }
    };

    /**
     * Creates a new Dialer layout
     * @param context An activity context
     */
    public DialLayout(Context context) {
        super(context);
        setSoundEffectsEnabled(false);
        mLongPressHandler = new Handler();
    }

    /**
     * Creates a new Dialer layout
     * @param context An activity context
     * @param attrs The style attribute set
     */
    public DialLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSoundEffectsEnabled(false);
        mLongPressHandler = new Handler();
    }

    /**
     * Creates a new Dialer layout
     * @param context An activity context
     * @param attrs The style attribute set
     * @param defStyleAttr The default style resource id
     */
    public DialLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSoundEffectsEnabled(false);
        mLongPressHandler = new Handler();
    }

    /**
     * Creates a new DialerLayout
     * @param context An activity context
     * @param attrs The style attribute set
     * @param defStyleAttr The default style resource id
     * @param defStyleRes The default resource
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DialLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setDialKeyPressStatusListener(OnDialKeyPressStatusListener dialKeyPressStatusListener) {
        mDialKeyPressStatusListener = dialKeyPressStatusListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                if (mDialKeyPressStatusListener != null) {
                    mDialKeyPressStatusListener.onDialKeyPressedStateChanged(this, true, false);
                    mLongPressHandler.postDelayed(mLongPressRunnable, ViewConfiguration.getLongPressTimeout());
                }
                break;
            case MotionEvent.ACTION_UP:
                setPressed(false);
                if (mDialKeyPressStatusListener != null) {
                    mDialKeyPressStatusListener.onDialKeyPressedStateChanged(this, false, false);
                    mLongPressHandler.removeCallbacks(mLongPressRunnable);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                if (mDialKeyPressStatusListener != null) {
                    mDialKeyPressStatusListener.onDialKeyPressedStateChanged(this, false, false);
                    mLongPressHandler.removeCallbacks(mLongPressRunnable);
                }
                break;
        }

        return true;
    }

    /**
     * A call back listener that allows an entity to receive press events for a key
     */
    public interface OnDialKeyPressStatusListener {
        /**
         * A callback which is triggered when the key pressed state changed from unpressed to pressed
         * or pressed to unpressed, and whether it was a long press. Please note that short press will
         * be triggered before a long press.
         *
         * @param touchedView The view which state was changed
         * @param pressed     True if the key is now pressed, false otherwise
         * @param longPress   True if the key was a long press, false otherwise.
         */
        public void onDialKeyPressedStateChanged(View touchedView, boolean pressed, boolean longPress);
    }
}
