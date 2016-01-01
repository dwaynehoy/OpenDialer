package com.squizbit.opendialer.library.widget.BottomSheet;


import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A base class that is consumed by the BottomSheet in order to build and populate the bottomsheets
 * view.
 */
public abstract class ViewBuilder {
    private FragmentActivity mActivity;
    private OnDismissRequestedListener mDismissRequestedListener;

    /**
     * Creates a new ViewBuilder instance
     * @param owner The activity owner of the ViewBuilder
     */
    public ViewBuilder(FragmentActivity owner){
        mActivity = owner;
    }

    /**
     * Called when the BottomSheet is about to be displayed and needs content to display
     * @return The view to be displayed in the bottomsheet
     */
    public abstract View onCreateView(ViewGroup parent);

    /**
     * Retreives the loader manager which can be used to streamline background loading
     * @return A valid loader manager
     */
    public LoaderManager getLoaderManager(){
        return mActivity.getSupportLoaderManager();
    }

    /**
     * Retreives a layout inflater
     * @return A layout inflater
     */
    public LayoutInflater getLayoutInflater(){
        return mActivity.getLayoutInflater();
    }

    /**
     * Sets a dismiss request listener which will be triggered when the ViewBuild wishes the
     * dialog to close
     * @param dismissRequestedListener The listener to receive the dismiss request callback
     */
    public void setOnDismissRequestedListener(OnDismissRequestedListener dismissRequestedListener) {
        mDismissRequestedListener = dismissRequestedListener;
    }

    /**
     * Requests the bottomsheet to be dismissed, this will forcefully dismiss the bottom sheet providing
     * no animations.
     */
    protected void requestBottomSheetDismiss(){
        if(mDismissRequestedListener != null){
            mDismissRequestedListener.onDismissRequested();
        }
    }

    /**
     * Retreives the context
     * @return The context
     */
    public Context getContext() {
        return mActivity;
    }

    /**
     * Returns the owning activity
     * @return The owning activity
     */
    public FragmentActivity getOwner(){
        return mActivity;
    }

    /**
     * A lister which allows the BottomSheet to receive a callback from the ViewBuilder when it's requesting
     * the window to be dismissed
     */
    public interface OnDismissRequestedListener{
        void onDismissRequested();
    }

}
