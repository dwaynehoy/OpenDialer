package com.squizbit.opendialer.models;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;

import com.squizbit.opendialer.R;

/**
 * Provides a theme color for a contact based on their details
 */
public class ContactThemeColorMatcher {

    private final Context mContext;
    private int[] mColorArray;
    private int[] mPrimes = new int[]{
            3, 5, 7, 11, 13, 17,
            19, 23, 29, 31, 37, 41,
            43, 47, 53, 59, 61, 67};

    /**
     * Creates a new ContactThemeColorMatcher instance
     * @param context A valid context
     */
    public ContactThemeColorMatcher(Context context, @ColorRes  int... colorIds){
        mContext = context;
        mColorArray = colorIds;
    }

    @ColorInt
    public int getContactColor(String contactKey){
        int colorIndex = 0;

        for(int i = 0; i < Math.min(contactKey.length(), 6); i++){
            colorIndex += (int)contactKey.charAt(i);
        }

        for(int i = 0; i < mColorArray.length - 1; i++){
            if(colorIndex % mPrimes[i] == 0){
                return mContext.getResources().getColor(mColorArray[i]);
            }
        }

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //noinspection deprecation
            return mContext.getResources().getColor(mColorArray[mColorArray.length - 1]);
        } else {
            return mContext.getResources().getColor(mColorArray[mColorArray.length - 1], null);
        }
    }

    public Drawable getCircularBackgroundDrawable(String contactKey){
        int color = getContactColor(contactKey);
        Drawable drawableTemplate;

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //noinspection deprecation
            drawableTemplate = mContext.getResources().getDrawable(R.drawable.bg_round_template);
        } else {
            drawableTemplate = mContext.getResources().getDrawable(R.drawable.bg_round_template, null);
        }

        if(drawableTemplate != null){
            drawableTemplate = drawableTemplate.mutate();
            drawableTemplate.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }

        return drawableTemplate;

    }
}
