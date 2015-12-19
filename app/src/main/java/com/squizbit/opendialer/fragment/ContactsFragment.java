package com.squizbit.opendialer.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
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
import com.squizbit.opendialer.data.ContactsAdapter;
import com.squizbit.opendialer.models.ContactColorGenerator;
import com.squizbit.opendialer.models.DialerHelper;
import com.squizbit.opendialer.library.widget.BottomSheet.BottomSheet;
import com.squizbit.opendialer.library.widget.ContactDetailViewBuilder;
import com.squizbit.opendialer.library.widget.RecycleviewIndexer.LinearManagerIndexerDecorator;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A fragment which displays a list of the users system contacts, and allows a user to select one
 * to call.
 */
public class ContactsFragment extends Fragment {

    //region View fields
    @InjectView(R.id.contact_list)
    RecyclerView mRecycleViewMain;
    
    @InjectView(R.id.textViewErrorMessage)
    TextView mTextViewErrorMessage;
    //endregion

    private ContactsAdapter mContactsAdapter;
    private DialerHelper mDialerHelper;

    private ContactsAdapter.OnContactSelectedListener mOnContactSelectedListener = new ContactsAdapter.OnContactSelectedListener() {
        @Override
        public void onContactSelected(Cursor contact) {
            BottomSheet dialog = new BottomSheet(getActivity());
            dialog.show(new ContactDetailViewBuilder(getActivity(), contact.getString(1), mDialerHelper));
        }
    };

    private View.OnClickListener mOnPermissionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    1);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_list_view, container, false);
        ButterKnife.inject(this, view);
        mContactsAdapter = null;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecycleViewMain.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycleViewMain.addItemDecoration(new LinearManagerIndexerDecorator(getActivity(), R.color.ab_primary, R.dimen.abc_text_size_title_material));
        mDialerHelper = new DialerHelper(getActivity());
        initContactsLoader();
    }

    private void initContactsLoader() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            getLoaderManager().initLoader(0, null, new ContactsLoaderCallbacks());
            mTextViewErrorMessage.setText(null);
            mTextViewErrorMessage.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            mTextViewErrorMessage.setOnClickListener(null);
        } else {
            mTextViewErrorMessage.setText(R.string.error_missing_contacts_permission);
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
        initContactsLoader();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private class ContactsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @RequiresPermission(Manifest.permission.READ_CONTACTS)
        public ContactsLoaderCallbacks(){
            //Default constructor for permission checking.
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(
                    getActivity(),
                    ContactsContract.Contacts.CONTENT_URI,
                    new String[]
                            {
                                    ContactsContract.Contacts._ID,
                                    ContactsContract.Contacts.LOOKUP_KEY,
                                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                                    ContactsContract.Contacts.PHOTO_URI,
                                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                            },
                    ContactsContract.Contacts.IN_VISIBLE_GROUP + " = 1 AND " + ContactsContract.Profile.HAS_PHONE_NUMBER + " = " + 1,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (mContactsAdapter == null) {
                ContactColorGenerator colorMatcher = new ContactColorGenerator(
                        getContext(),
                        R.color.contact_red,
                        R.color.contact_blue,
                        R.color.contact_purple,
                        R.color.contact_yellow,
                        R.color.contact_green);

                mContactsAdapter = new ContactsAdapter(getActivity(), data, colorMatcher);
                mContactsAdapter.setContactSelectedListener(mOnContactSelectedListener);
                mRecycleViewMain.setAdapter(mContactsAdapter);
            } else {
                mContactsAdapter.swapCursor(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (mContactsAdapter != null) {
                mContactsAdapter.swapCursor(null);
            }
        }
    }
}
