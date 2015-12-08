package com.squizbit.opendialer.library.widget.RecycleviewIndexer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.squizbit.opendialer.R;

/**
 * A ViewGroup that contains a RecycleView and adds fast scrolling and indexer support.
 */

public class RecyclerViewFastScroller extends FrameLayout {

    private RecyclerView mRecyclerView;
    private BitmapDrawable mBitmapDrawable;
    private int mHandlePaddingRight;
    private int mHandlePaddingTop;
    private float mScrollProportion = 0;
    private RecyclerView.OnScrollListener mOnScrollListener;
    private int mListVisibilityRange = -1;
    private Rect mHandleHitRect = new Rect();
    private int mInitialTouchRegion = 0;
    private boolean mFastScrollHandleSelected = false;

    private boolean mIsIndexBubbleEnabled = true;
    private int mIndexBubblePadding;
    private Drawable mIndexBubbleDrawable;
    private Rect mIndexBubbleRect;
    private TextPaint mIndexTextPaint;
    private Rect mTextBounds;


    private ColorFilter mSelectedColorFilter;
    private ColorFilter mUnselectedColorFilter;



    /**
     * Creates a new RecyclerViewFastSroller
     *
     * @param context The activity context
     */
    public RecyclerViewFastScroller(Context context) {
        this(context, null, 0);
    }

    /**
     * Creates a new RecyclerViewFastScroller
     *
     * @param context The activity context
     * @param attrs   The attribute set
     */
    public RecyclerViewFastScroller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    /**
     * Creates a new RecyclerViewFastScroller
     *
     * @param context      The activity context
     * @param attrs        The attribute set
     * @param defStyleAttr The default style resource id
     */
    public RecyclerViewFastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings({"deprecation"})
    private void init() {
        setWillNotDraw(false);
        int activeColor;
        int inactiveColor;
        int indexBubbleColor;

        mBitmapDrawable = (BitmapDrawable) getContext().getResources().getDrawable(R.drawable.ic_fastscroll_handle);
        activeColor = getContext().getResources().getColor(R.color.ab_primary);
        inactiveColor = getContext().getResources().getColor(R.color.handle_normal);
        indexBubbleColor = getContext().getResources().getColor(R.color.bubble_index_text);
        mIndexBubbleDrawable = DrawableCompat.wrap(getContext().getResources().getDrawable(R.drawable.bg_quick_scroll_bubble));
        DrawableCompat.setTint(mIndexBubbleDrawable, activeColor);

        mSelectedColorFilter = new PorterDuffColorFilter(
                activeColor,
                PorterDuff.Mode.SRC_ATOP);

        mUnselectedColorFilter = new PorterDuffColorFilter(
                inactiveColor,
                PorterDuff.Mode.SRC_ATOP);
        mHandlePaddingRight = getContext().getResources().getDimensionPixelOffset(R.dimen.fastscroll_padding_right);
        mHandlePaddingTop = getContext().getResources().getDimensionPixelOffset(R.dimen.fastscroll_padding_top);

        mIndexBubbleRect = new Rect();
        mIndexTextPaint = new TextPaint();
        mIndexTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.index_bubble_text));
        mIndexTextPaint.setColor(indexBubbleColor);
        mIndexTextPaint.setAntiAlias(true);
        mTextBounds = new Rect();
        mIndexTextPaint.getTextBounds("A", 0, 1, mTextBounds);
        mIndexTextPaint.setTextAlign(Paint.Align.CENTER);

        mIndexBubblePadding = getContext().getResources().getDimensionPixelOffset(R.dimen.index_bubble_padding);
    }

    /**
     * Creates a new recyclerViewFastScroller which provides fast scrolling functionality to a Recyclerview
     *
     * @param context      The activity context
     * @param attrs        The attribute set
     * @param defStyleAttr The default style resource id
     * @param defStyleRes  The default style resource
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecyclerViewFastScroller(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void addView(View child) {
        if ((mRecyclerView != null && !child.equals(mRecyclerView)) || !(child instanceof RecyclerView)) {
            throw new IllegalStateException("RecyclerViewFastScroller expects a single RecyclerView as a child");
        } else {
            initRecyclerView((RecyclerView) child);
            super.addView(child);
        }
    }

    @Override
    public void addView(View child, int index) {
        if ((mRecyclerView != null && !child.equals(mRecyclerView)) || !(child instanceof RecyclerView)) {
            throw new IllegalStateException("RecyclerViewFastScroller expects a single RecyclerView as a child");
        } else {
            initRecyclerView((RecyclerView) child);
            super.addView(child, index);
        }
    }

    @Override
    public void addView(View child, int width, int height) {
        if ((mRecyclerView != null && !child.equals(mRecyclerView)) || !(child instanceof RecyclerView)) {
            throw new IllegalStateException("RecyclerViewFastScroller expects a single RecyclerView as a child");
        } else {
            initRecyclerView((RecyclerView) child);
            super.addView(child, width, height);
        }
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if ((mRecyclerView != null && !child.equals(mRecyclerView)) || !(child instanceof RecyclerView)) {
            throw new IllegalStateException("RecyclerViewFastScroller expects a single RecyclerView as a child");
        } else {
            initRecyclerView((RecyclerView) child);
            super.addView(child, params);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if ((mRecyclerView != null && !child.equals(mRecyclerView)) || !(child instanceof RecyclerView)) {
            throw new IllegalStateException("RecyclerViewFastScroller expects a single RecyclerView as a child");
        } else {
            initRecyclerView((RecyclerView) child);
            super.addView(child, index, params);
        }
    }

    private void initRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.setVerticalScrollBarEnabled(false);
        mRecyclerView.setOnScrollListener(new InternalOnScrollListener());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (!shouldDisplayFastScroll()) {
            return;
        }

        int scrollHeight = getHeight() - mBitmapDrawable.getIntrinsicHeight() - (mHandlePaddingTop * 2);

        mHandleHitRect.offsetTo(mHandleHitRect.left, (int) (mHandlePaddingTop + (scrollHeight * mScrollProportion)));

        ColorFilter colorFilter = mFastScrollHandleSelected ? mSelectedColorFilter : mUnselectedColorFilter;
        mBitmapDrawable.setColorFilter(colorFilter);

        mBitmapDrawable.setBounds(getWidth() - mHandlePaddingRight - mBitmapDrawable.getIntrinsicWidth(),
                (int) (mHandlePaddingTop + (scrollHeight * mScrollProportion)),
                getWidth() - mHandlePaddingRight,
                (int) (mHandlePaddingTop + (scrollHeight * mScrollProportion)) + mBitmapDrawable.getIntrinsicHeight());
        mBitmapDrawable.draw(canvas);


        String index = null;

        if(shouldDisplayIndexBubble()) {
            index = getIndexForTopItem();
        }

        if(index != null) {
            mIndexBubbleRect.set(
                    mBitmapDrawable.getBounds().left - mIndexBubbleDrawable.getIntrinsicWidth() - mIndexBubblePadding,
                    mHandleHitRect.centerY() - mIndexBubbleDrawable.getIntrinsicHeight(),
                    mBitmapDrawable.getBounds().left - mIndexBubblePadding,
                    mHandleHitRect.centerY());

            if (mIndexBubbleRect.top < 0) {
                mIndexBubbleRect.offsetTo(mIndexBubbleRect.left, 0);
            } else if (mIndexBubbleRect.bottom > getHeight()) {
                mIndexBubbleRect.offset(0, mIndexBubbleRect.bottom - getHeight());
            }

            mIndexBubbleDrawable.setBounds(mIndexBubbleRect);
            mIndexBubbleDrawable.draw(canvas);

            canvas.drawText(index, mIndexBubbleRect.centerX(), mIndexBubbleRect.centerY() + (mTextBounds.height() / 2), mIndexTextPaint);
        }
    }

    private String getIndexForTopItem() {
        View child = mRecyclerView.getChildAt(0);
        if(child != null){
            int index = mRecyclerView.getChildAdapterPosition(child);

            return ((IndexedAdapter)mRecyclerView.getAdapter()).getIndexLabel(index);
        }

        return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mHandleHitRect.set(
                getMeasuredWidth() - mHandlePaddingRight - mBitmapDrawable.getIntrinsicWidth() - mHandlePaddingRight,
                mHandlePaddingTop,
                getMeasuredWidth(),
                mHandlePaddingTop + mBitmapDrawable.getIntrinsicHeight());
    }

    public void setOnScrollListener(RecyclerView.OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mHandleHitRect.contains((int) ev.getX(), (int) ev.getY());
    }

    private boolean shouldDisplayFastScroll() {
        return mRecyclerView.getAdapter() != null && getVisibleRange() < mRecyclerView.getAdapter().getItemCount();
    }

    private boolean shouldDisplayIndexBubble() {
        return shouldDisplayFastScroll() &&
                mRecyclerView.getAdapter() instanceof IndexedAdapter &&
                mFastScrollHandleSelected &&
                mIsIndexBubbleEnabled;
    }

    /**
     * Toggles the index scroll bubble on or off
     *
     * @param isIndexBubbleEnabled True if the index bubble should be displayed, false otherwise
     */
    public void setIsIndexBubbleEnabled(boolean isIndexBubbleEnabled) {
        mIsIndexBubbleEnabled = isIndexBubbleEnabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!shouldDisplayFastScroll()) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mFastScrollHandleSelected = true;
            mInitialTouchRegion = (int) (event.getY() - mBitmapDrawable.getBounds().top);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mScrollProportion = Math.max(Math.min((event.getY() - mInitialTouchRegion) / getHeight(), 1), 0);

            ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(
                    (int) (mScrollProportion * (mRecyclerView.getAdapter().getItemCount() - getVisibleRange() + 1)), 0);
            invalidate();
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(mRecyclerView, RecyclerView.SCROLL_STATE_DRAGGING);
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            mFastScrollHandleSelected = false;
            invalidate();
        }

        return false;

    }

    private int getVisibleRange() {
        if (mListVisibilityRange == -1) {
            //Initial visibility range does not include a buffer view at top
            mListVisibilityRange = mRecyclerView.getChildCount();
        }
        return mListVisibilityRange;
    }

    /**
     * An internal listener which listens to the child recyclerview's OnScroll methods and positions
     * the fastscroller handle appropriately
     */
    private class InternalOnScrollListener extends OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            if (!mFastScrollHandleSelected) {
                View firstChild = mRecyclerView.getChildAt(0);
                if (firstChild == null) {
                    return;
                }

                float firstVisiblePosition = mRecyclerView.getChildAdapterPosition(firstChild);

                float visibleRange = getVisibleRange();
                float visibilityDelta = recyclerView.getAdapter().getItemCount() - visibleRange;

                float currentScrollPercentage = firstVisiblePosition / visibilityDelta;
                float nextScrollPercentage = (firstVisiblePosition + 1) / visibilityDelta;

                float currentCellProportion =
                        Math.min((currentScrollPercentage - nextScrollPercentage) *
                                ((float) firstChild.getTop() / (float) firstChild.getHeight()), nextScrollPercentage);

                mScrollProportion = Math.min((firstVisiblePosition / visibilityDelta) + currentCellProportion, 1);
                invalidate();
            }

            if (mOnScrollListener != null) {
                mOnScrollListener.onScrolled(recyclerView, dx, dy);
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(recyclerView, newState);
            }
        }


    }
}
