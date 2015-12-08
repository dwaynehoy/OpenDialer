package com.squizbit.opendialer.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squizbit.opendialer.R;
import com.squizbit.opendialer.data.ContactsAdapter;
import com.squizbit.opendialer.library.widget.BottomSheet.BottomSheet;
import com.squizbit.opendialer.library.widget.ContactDetailViewBuilder;
import com.squizbit.opendialer.library.widget.RecycleviewIndexer.LinearManagerIndexerDecorator;
import com.squizbit.opendialer.models.ContactThemeColorMatcher;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A fragment which displays a list of the users system contacts, and allows a user to select one
 * to call
 */
public class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @InjectView(R.id.contact_list)
    RecyclerView mContactList;

    private ContactsAdapter mContactsAdapter;

    private ContactsAdapter.OnContactSelectedListener mOnContactSelectedListener = new ContactsAdapter.OnContactSelectedListener() {
        @Override
        public void onContactSelected(Cursor contact) {
            BottomSheet dialog = new BottomSheet(getActivity());
            dialog.show(new ContactDetailViewBuilder(getActivity(), contact.getString(1)));
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contacts_list_view, container, false);
        ButterKnife.inject(this, view);
        mContactsAdapter = null;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContactList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactList.addItemDecoration(new LinearManagerIndexerDecorator(getActivity(), R.color.ab_primary, R.dimen.abc_text_size_title_material));
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
                                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                                ContactsContract.Contacts.PHOTO_URI,
                                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                        },
                ContactsContract.Contacts.IN_VISIBLE_GROUP + " = 1 AND " +ContactsContract.Profile.HAS_PHONE_NUMBER + " = " + 1,
                null,
                ContactsContract.Contacts.DISPLAY_NAME);
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

            mContactsAdapter = new ContactsAdapter(getActivity(), data, colorMatcher);
            mContactsAdapter.setContactSelectedListener(mOnContactSelectedListener);
            mContactList.setAdapter(mContactsAdapter);
        } else {
            mContactsAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(mContactsAdapter != null){
            mContactsAdapter.swapCursor(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
