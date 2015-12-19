package com.squizbit.opendialer.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squizbit.opendialer.R;
import com.squizbit.opendialer.data.CallLogAdapter;
import com.squizbit.opendialer.models.ContactColorGenerator;
import com.squizbit.opendialer.models.DialerHelper;
import com.squizbit.opendialer.library.widget.BottomSheet.BottomSheet;
import com.squizbit.opendialer.library.widget.ContactDetailViewBuilder;
import com.squizbit.opendialer.models.Preferences;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A fragment which displays a recent calls log to the user and provides the following
 * functionality
 * - Ability to call the number associated to a call log
 * - Ability to see whether the call was missed, made, or received
 */
public class CallLogFragment extends Fragment {

    private static final int RELOAD_DELAY_MS = 5000;

    //region View fragment
    @InjectView(R.id.recycleViewMain)
    RecyclerView mRecycleViewMain;

    @InjectView(R.id.textViewErrorMessage)
    TextView mTextViewErrorMessage;
    //endregion

    private CallLogAdapter mAdapter;
    private boolean mIsVisibleToUser = false;
    private DialerHelper mDialerHelper;

    private Handler mRefreshHandler = new Handler();
    private Runnable mRefreshRunner = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null) {
                if (mAdapter != null) {
                    mAdapter.updateRelativeTimes();
                }
                mRefreshHandler.postDelayed(mRefreshRunner, RELOAD_DELAY_MS);
            }
        }
    };

    private CallLogAdapter.OnCallLogEntryActionListener mOnCallClickListener = new CallLogAdapter.OnCallLogEntryActionListener() {
        @Override
        public void onCallActionTriggered(String number) {
            Preferences preferences = new Preferences(getActivity());
            preferences.setLastDialedNumber(number);

            mDialerHelper.dialNumber(number);
        }

        @Override
        public void onContactActionTriggered(String lookupKey) {
            BottomSheet dialog = new BottomSheet(getActivity());
            dialog.show(new ContactDetailViewBuilder(getActivity(), lookupKey, mDialerHelper));
        }
    };
    private View.OnClickListener mOnPermissionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            List<String> permissionList = new ArrayList<>();
            if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                permissionList.add(Manifest.permission.READ_CONTACTS);
            }

            if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
                permissionList.add(Manifest.permission.READ_CALL_LOG);
            }

            ActivityCompat.requestPermissions(
                    getActivity(),
                    permissionList.toArray(new String[permissionList.size()]),
                    1);
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.generic_list_view, container, false);
        ButterKnife.inject(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDialerHelper = new DialerHelper(getActivity());
        mRecycleViewMain.setLayoutManager(new LinearLayoutManager(getActivity()));
        initCallLogDataLoader();
    }

    private void initCallLogDataLoader() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            getLoaderManager().initLoader(0, null, new CallLogLoaderCallback());
            mTextViewErrorMessage.setText(null);
            mTextViewErrorMessage.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            mTextViewErrorMessage.setOnClickListener(null);
        } else {
            mTextViewErrorMessage.setText(R.string.error_missing_call_log_permission);
            mTextViewErrorMessage.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    ResourcesCompat.getDrawable(getResources(), R.drawable.ic_missing_contact_permission, getContext().getTheme()),
                    null,
                    null);
            mTextViewErrorMessage.setOnClickListener(mOnPermissionClickListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mDialerHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initCallLogDataLoader();
    }

    @Override
    public void onResume() {
        if (mIsVisibleToUser) {
            mRefreshHandler.post(mRefreshRunner);
        }
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        if (mIsVisibleToUser) {
            mRefreshHandler.post(mRefreshRunner);
        } else {
            mRefreshHandler.removeCallbacks(mRefreshRunner);
        }
    }

    @Override
    public void onPause() {
        mRefreshHandler.removeCallbacks(mRefreshRunner);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private class CallLogLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        @RequiresPermission(Manifest.permission.READ_CONTACTS)
        public CallLogLoaderCallback(){
            //Default constructor for permission annotation
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(
                    getActivity(),
                    CallLog.Calls.CONTENT_URI,
                    //// FIXME: 4/12/2015 Look into NUMBER_PRESENTATION for backwards compatibility
                    new String[]{
                            CallLog.Calls.NUMBER,
                            CallLog.Calls.NUMBER_PRESENTATION,
                            CallLog.Calls.TYPE,
                            CallLog.Calls.DURATION,
                            CallLog.Calls.DATE,
                    },
                    null,
                    null,
                    CallLog.Calls.DEFAULT_SORT_ORDER);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (mAdapter == null) {
                ContactColorGenerator colorMatcher = new ContactColorGenerator(
                        getContext(),
                        R.color.contact_red,
                        R.color.contact_blue,
                        R.color.contact_purple,
                        R.color.contact_yellow,
                        R.color.contact_green);

                mAdapter = new CallLogAdapter(getContext(), data, getLoaderManager(), colorMatcher);
                mAdapter.setOnCallLogEntryActionListener(mOnCallClickListener);
                mRecycleViewMain.setAdapter(mAdapter);

            } else {
                mAdapter.swapCursor(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    }


}
