package com.squizbit.opendialer.library.widget.RecycleviewIndexer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.View;

/**
 * A decorator adds a list indexer to a RecyclerView using a LinearLayoutManager. The adapter being
 * used must implement {@link LinearManagerIndexerDecorator.IndexedAdapter}
 * in order for this decorator to display the index
 */
public class LinearManagerIndexerDecorator  extends RecyclerView.ItemDecoration  {

    private TextPaint mTextPaint;
    private Rect mTextBounds;

    /**
     * Creates a new LinearManagerIndexerDecorator which will decorate a RecyclerView using a LinearLayoutManager
     * with index characters based on adapter content
     * @param context A valid context
     * @param textColor The color resource id of the index text
     * @param textSize The dimension resource id of the index text size
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.M)
    public LinearManagerIndexerDecorator(Context context, @ColorRes int textColor, @DimenRes int textSize){
        mTextPaint = new TextPaint();
        int color;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            color = context.getColor(textColor);
        } else {
            color = context.getResources().getColor(textColor);
        }

        mTextPaint.setColor(color);
        mTextPaint.setTextSize(context.getResources().getDimensionPixelSize(textSize));
        mTextBounds = new Rect();
        mTextPaint.getTextBounds("A", 0, 1, mTextBounds);
        mTextPaint.setAntiAlias(true);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.offsetTo(mTextBounds.width(), 0);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if(parent.getAdapter() == null || !(parent.getAdapter() instanceof IndexedAdapter) || parent.getAdapter().getItemCount() == 0){
            return;
        }
        String lastLabel;
        String nextLabel = null;

        View child = parent.getChildAt(0);
        lastLabel = ((IndexedAdapter) parent.getAdapter()).getIndexLabel(parent.getChildAdapterPosition(child));

        if(parent.getChildCount() > 1) {
            View nextChild = parent.getChildAt(1);
            nextLabel = ((IndexedAdapter) parent.getAdapter()).getIndexLabel(parent.getChildAdapterPosition(nextChild));
        }

        if(!lastLabel.equals(nextLabel)){
            int parentTop = parent.getPaddingTop() + mTextBounds.height();
            int maxTop = Math.max(child.getTop() + mTextBounds.height(), parentTop);
            int top = Math.min(maxTop, child.getBottom());
            c.drawText(lastLabel, child.getLeft() - mTextBounds.width(), top, mTextPaint);
        } else {
            int parentTop = parent.getPaddingTop() + mTextBounds.height();
            int top = Math.max(child.getTop() + mTextBounds.height(), parentTop);
            c.drawText(lastLabel, child.getLeft() - mTextBounds.width(), top, mTextPaint);
        }

        for(int i = 1; i < parent.getChildCount(); i++){

            child = parent.getChildAt(i);
            String indexLabel = ((IndexedAdapter) parent.getAdapter()).getIndexLabel(parent.getChildAdapterPosition(child));
            if(!lastLabel.equals(indexLabel)){
                int parentTop = parent.getPaddingTop() + mTextBounds.height();
                int top = Math.max(child.getTop() + mTextBounds.height(), parentTop);
                c.drawText(indexLabel, child.getLeft() - mTextBounds.width(), top, mTextPaint);

                lastLabel = indexLabel;
            }
        }
    }

}
