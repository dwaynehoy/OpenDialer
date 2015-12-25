package com.squizbit.opendialer.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squizbit.opendialer.R;
import com.squizbit.opendialer.data.FavoritesAdapter;
import com.squizbit.opendialer.library.GridSpacingItemDecorator;
import com.squizbit.opendialer.models.ContactColorGenerator;
import com.squizbit.opendialer.models.DialerHelper;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A fragment which displays a list of the users system contacts, and allows a user to select one
 * to call
 */
public class FavoritesFragment extends Fragment {

    //region View fields
    @InjectView(R.id.recycleViewMain)
    RecyclerView mContactList;

    @InjectView(R.id.textViewErrorMessage)
    TextView mTextViewErrorMessage;
    //endregion

    private FavoritesAdapter mContactsAdapter;
    private DialerHelper mDialerHelper;

    private FavoritesAdapter.OnContactSelectedListener mOnContactSelectedListener = new FavoritesAdapter.OnContactSelectedListener() {
        @Override
        public void onContactSelected(Cursor contact) {
            int lookupIndex = contact.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
            String lookupKey = contact.getString(lookupIndex);
            String number = getDefaultNumber(lookupKey);
            mDialerHelper.dialNumber(number);
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
        View view = inflater.inflate(R.layout.generic_list_view, container, false);
        ButterKnife.inject(this, view);
        mContactsAdapter = null;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContactList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mContactList.addItemDecoration(new GridSpacingItemDecorator(2, getResources().getDimensionPixelOffset(R.dimen.favorite_card_grid_padding), true));
        mContactList.setPadding(0, 0, 0, 0);

        mDialerHelper = new DialerHelper(getActivity());

        initFavoriteDataLoader();
    }

    private void initFavoriteDataLoader() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            getLoaderManager().initLoader(0, null, new FavoriteLoaderCallback());
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

    @Nullable
    private String getDefaultNumber(String lookupKey) {
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
        uri = Uri.withAppendedPath(uri, ContactsContract.Contacts.Entity.CONTENT_DIRECTORY);

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                null,
                ContactsContract.Contacts.Entity.MIMETYPE + " = ?",
                new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY + " desc ");

        String dialNumber = null;
        if (cursor != null && cursor.moveToFirst()) {
            String normalizedNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            dialNumber = normalizedNumber != null? normalizedNumber: number;

            cursor.close();
        }

        return dialNumber;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mDialerHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initFavoriteDataLoader();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private class FavoriteLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        /**
         * Creates a new FavouriteLoaderCallback instance
         */
        @RequiresPermission(Manifest.permission.READ_CONTACTS)
        public FavoriteLoaderCallback() {
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
                                    ContactsContract.Contacts.DISPLAY_NAME,
                                    ContactsContract.Contacts.PHOTO_URI,
                                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                                    ContactsContract.Contacts.STARRED,
                                    ContactsContract.Contacts.TIMES_CONTACTED,
                                    ContactsContract.Contacts.LAST_TIME_CONTACTED

                            },
                    ContactsContract.PhoneLookup.IN_VISIBLE_GROUP + " = 1 AND " + ContactsContract.PhoneLookup.HAS_PHONE_NUMBER + " = 1 " +
                            " AND ( " + ContactsContract.PhoneLookup.TIMES_CONTACTED + " > 4 OR " + ContactsContract.PhoneLookup.STARRED + " = 1  ) ",
                    null,
                    ContactsContract.PhoneLookup.STARRED + " desc, " +
                            ContactsContract.PhoneLookup.TIMES_CONTACTED + " desc ");
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
                mContactsAdapter = new FavoritesAdapter(getActivity(), data, colorMatcher);
                mContactsAdapter.setContactSelectedListener(mOnContactSelectedListener);
                mContactList.setAdapter(mContactsAdapter);
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
