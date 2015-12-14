package com.squizbit.opendialer.library;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridSpacingItemDecorator extends RecyclerView.ItemDecoration {

    private int mSpanCount;
    private int mPadding;
    private boolean mDoIncludeTopPadding = true;

    public GridSpacingItemDecorator(int spanCount, int padding) {
        mSpanCount = spanCount;
        mPadding = padding;
    }

    public GridSpacingItemDecorator(int spanCount, int padding, boolean doIncludeTopPadding) {
        mSpanCount = spanCount;
        mPadding = padding;
        mDoIncludeTopPadding = doIncludeTopPadding;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position
        int column = position % mSpanCount; // item column

        if(column == 0) {
            outRect.left = mPadding;
        } else {
            outRect.left = mPadding/2;
        }

        if(column == mSpanCount - 1){
            outRect.right = mPadding;
        } else {
            outRect.right = mPadding/2;
        }


        if (mDoIncludeTopPadding && position < mSpanCount) { // top edge
            outRect.top = mPadding;
        }

        outRect.bottom = mPadding; // item bottom
    }
}