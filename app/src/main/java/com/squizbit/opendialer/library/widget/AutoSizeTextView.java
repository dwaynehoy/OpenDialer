package com.squizbit.opendialer.library.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * A view that displays text to a user, this view will automatically determine the text size that will
 * fit in the allocated view space down to the minimum text size supplied.
 */
public class AutoSizeTextView extends TextView {

    private float mMaxFontSize = 70;
    private float mMinFontSize = 40;

    /**
     * Creates a new auto sizing text text view
     * @param context An activity context
     */
    public AutoSizeTextView(Context context) {
        super(context);
        initTextView();
    }

    /**
     * Creates a new auto sizing text text view
     * @param context An activity context
     * @param attrs The style attribute set
     */
    public AutoSizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTextView();
    }

    /**
     * Creates a new auto sizing text text view
     * @param context An activity context
     * @param attrs The style attribute set
     * @param defStyleAttr The default style resource id
     */
    public AutoSizeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTextView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AutoSizeTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initTextView();
    }

    private void initTextView() {
        setSingleLine(true);
        setEllipsize(null);
        mMaxFontSize = getTextSize();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        final int lineIndex = getLineCount() - 1;
        Layout layout = getLayout();


        while (layout != null && layout.getLineWidth(lineIndex) > getUsableWidth()) {
            float textSize = getTextSize();

            if(textSize <= mMinFontSize){
                break;
            }

            setTextSize(TypedValue.COMPLEX_UNIT_PX, (textSize - 1));
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            layout = getLayout();
        }

        while(layout != null && layout.getLineWidth(lineIndex) < getUsableWidth()){
            float textSize = getTextSize();

            if(textSize >= mMaxFontSize){
                break;
            }

            setTextSize(TypedValue.COMPLEX_UNIT_PX, (textSize + 1));
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            layout = getLayout();
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        final int lineIndex = getLineCount() - 1;
        final Layout layout = getLayout();


        if((layout != null && (layout.getLineWidth(lineIndex) > getUsableWidth()) || lengthBefore > lengthAfter)){
            post(new Runnable() {
                @Override
                public void run() {
                    requestLayout();
                }
            });
        }
    }

    private float getUsableWidth(){
        //1.25f is extra padding because we can't get leading and trailing edge spacing
        return Math.max(getWidth() - (getTotalPaddingLeft() * 1.25f) - (getTotalPaddingRight() * 1.25f), 0);
    }
}
