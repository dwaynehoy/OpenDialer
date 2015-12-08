package com.squizbit.opendialer.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squizbit.opendialer.R;
import com.squizbit.opendialer.data.FavoritesAdapter;
import com.squizbit.opendialer.library.GridSpacingItemDecorator;
import com.squizbit.opendialer.models.ContactThemeColorMatcher;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A fragment which displays a list of the users system contacts, and allows a user to select one
 * to call
 */
public class FavoritesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @InjectView(R.id.recycleViewMain)
    RecyclerView mContactList;

    private FavoritesAdapter mContactsAdapter;

    private FavoritesAdapter.OnContactSelectedListener mOnContactSelectedListener = new FavoritesAdapter.OnContactSelectedListener() {
        @Override
        public void onContactSelected(Cursor contact) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            //TODO: Get number. If has default number, dial it. If not display contact card
            int lookupIndex = contact.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
            String lookupKey = contact.getString(lookupIndex);
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            uri = uri.withAppendedPath(uri, ContactsContract.Contacts.Entity.CONTENT_DIRECTORY);

            Cursor cursor = getContext().getContentResolver().query(
                    uri,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER},
                    ContactsContract.Contacts.Entity.MIMETYPE + " = ? ",
                    new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                    null);

            if(cursor.moveToFirst()){
                int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
                Log.e("TEST", cursor.getString(columnIndex));
                intent.setData(Uri.parse("tel:" + cursor.getString(columnIndex)));
                startActivity(intent);
            }

            cursor.close();
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
        getLoaderManager().initLoader(0, null, this);
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
                                ContactsContract.Contacts.TIMES_CONTACTED

                        },
                ContactsContract.PhoneLookup.IN_VISIBLE_GROUP + " = 1 AND " + ContactsContract.PhoneLookup.HAS_PHONE_NUMBER + " = 1 " +
                        " AND ( " + ContactsContract.PhoneLookup.TIMES_CONTACTED + " > 0 OR " + ContactsContract.PhoneLookup.STARRED + " = 1  ) ",
                null,
                ContactsContract.PhoneLookup.TIMES_CONTACTED + " desc, " + ContactsContract.PhoneLookup.STARRED + " desc ");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mContactsAdapter == null) {
            ContactThemeColorMatcher colorMatcher = new ContactThemeColorMatcher(
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
