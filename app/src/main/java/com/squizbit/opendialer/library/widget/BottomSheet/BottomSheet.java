package com.squizbit.opendialer.library.widget.BottomSheet;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.squizbit.opendialer.R;

/**
 * A dialog which presents a contacts details to the user in a slide up panep
 */
public class BottomSheet extends Dialog {

    private BottomSheetView mSlideView;
    private ViewBuilder mViewBuilder;
    private ViewGroup mContentView;
    private ViewBuilder.OnDismissRequestedListener mDismissRequestListener = new ViewBuilder.OnDismissRequestedListener() {
        @Override
        public void onDismissRequested() {
            forceBackPressed();
        }
    };

    /**
     * Creates a new Contacts Details Dialog
     *
     * @param context The activity context
     */
    public BottomSheet(Context context) {
        this(context, R.style.AppTheme_DropSheetTheme);
    }

    /**
     * Creates a new Contacts Details Dialog
     *
     * @param context The activity context
     * @param theme   The theme resource id
     */
    public BottomSheet(Context context, int theme) {
        super(context, theme);
        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.bottom_sheet_view);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            TypedArray attrArray = getContext().getTheme().obtainStyledAttributes(R.style.AppTheme_DropSheetTheme, new int[]{R.attr.colorPrimaryDark});

            attrArray.recycle();
        }


        mSlideView = (BottomSheetView) findViewById(R.id.bottomSheetSlideView);
        mSlideView.setOnBottomSheetDismissedListener(new BottomSheetView.OnBottomSheetDismissedListener() {
            @Override
            public void onBottomSheetDismissed() {
                forceBackPressed();
            }
        });
        mContentView = (ViewGroup) findViewById(R.id.layoutRoot);
    }

    @Override
    public void show() {
        throw new UnsupportedOperationException("Please use show(ViewBuilder) instead");
    }

    /**
     * Displays the bottomsheet
     *
     * @param viewBuilder The view builder that contains the view to display
     */
    public void show(ViewBuilder viewBuilder) {
        mViewBuilder = viewBuilder;
        mViewBuilder.setOnDismissRequestedListener(mDismissRequestListener);
        super.show();
    }

    @Override
    protected void onStart() {
        View view  = mViewBuilder.onCreateView(mContentView);
        mContentView.addView(view);
        super.onStart();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && mSlideView.wasTouchEventOutsideBottomSheet(event)) {
            onBackPressed();
            return true;
        }

        return false;
    }


    @Override
    public void onBackPressed() {
        mSlideView.close();
    }

    public void forceBackPressed() {
        super.onBackPressed();
    }
}
