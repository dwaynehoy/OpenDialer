package com.squizbit.opendialer.library.widget;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squizbit.opendialer.R;
import com.squizbit.opendialer.models.DialerHelper;
import com.squizbit.opendialer.library.widget.BottomSheet.ViewBuilder;
import com.squizbit.opendialer.models.Preferences;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Builds a view which displays a users
 */
public class ContactDetailViewBuilder extends ViewBuilder implements LoaderManager.LoaderCallbacks<Cursor> {

    private String mLookupId;
    private View mView;
    private ViewGroup mParent;
    private DialerHelper mDialerHelper;

    public ContactDetailViewBuilder(FragmentActivity owner, String lookupId, DialerHelper dialerHelper) {
        super(owner);
        mLookupId = lookupId;
        mDialerHelper = dialerHelper;
    }

    @Override
    public View onCreateView(ViewGroup parent) {
        mView = getLayoutInflater().inflate(R.layout.contact_detail_view, parent, false);
        mParent = parent;
        getLoaderManager().restartLoader(9980, null, this);

        return mView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, mLookupId);
        uri = uri.withAppendedPath(uri, ContactsContract.Contacts.Entity.CONTENT_DIRECTORY);

        return new CursorLoader(
                getContext(),
                uri,
                null,
                null,
                null,
                ContactsContract.Contacts.Entity.MIMETYPE);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        String displayImage = null;
        String phoneNumber = null;

        if (data.moveToFirst()) {
            do {
                String mimeType = data.getString(data.getColumnIndex(ContactsContract.Contacts.Entity.MIMETYPE));

                if (displayImage == null && mimeType.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                    displayImage = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI));
                } else if (phoneNumber == null && mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                    insertPhoneNumber(
                            data.getInt(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)),
                            data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                            data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)),
                            data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)),
                            data.getInt(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)) == 1
                    );

                }


            } while (data.moveToNext());

            Bitmap contactBitmap = getContactImage(displayImage, Math.min(mParent.getWidth(), 720));
            if (contactBitmap != null) {
                ((android.widget.ImageView) mView.findViewById(R.id.imageViewContact)).setImageBitmap(contactBitmap);
            }
        }
    }

    private void insertPhoneNumber(int typeResId, String number, String normalizedNumber, String label, boolean isPrimary) {
        ViewGroup parent = (ViewGroup) mView.findViewById(R.id.linearLayoutNumberContainer);
        String type;

        if (typeResId != ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM) {
            type = getContext().getString(ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(typeResId));
        } else {
            type = label;
        }

        View view = getLayoutInflater().inflate(R.layout.contact_number_view, parent, false);
        if (normalizedNumber == null) {
            normalizedNumber = number;
        }
        view.setTag(normalizedNumber);

        ((TextView) view.findViewById(R.id.textViewItem)).setText(number);
        ((TextView) view.findViewById(R.id.textViewType)).setText(type);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = (String) v.getTag();
                Preferences preferences = new Preferences(getContext());
                if(!mDialerHelper.dialNumber(number)){
                    preferences.setLastDialedNumber(number);
                }
            }
        });

        if(isPrimary){
            parent.addView(view, 0);
            if(parent.getChildCount() > 1){
                parent.getChildAt(1).findViewById(R.id.imageViewType).setVisibility(View.INVISIBLE);
            }
        } else {
            parent.addView(view);
            if(parent.getChildCount() > 1){
                view.findViewById(R.id.imageViewType).setVisibility(View.INVISIBLE);
            }
        }
    }


    private Bitmap getContactImage(String uriString, int dimen) {
        if (uriString == null || uriString.isEmpty()) {
            return null;
        }

        try {
            Uri uri = Uri.parse(uriString);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
            return Bitmap.createScaledBitmap(bitmap, dimen, dimen, true);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

    }

    /**
     * Callback which should be passed back by the parent activity in order, to handle the result of
     * the permission request
     *
     * @param requestCode  The result code
     * @param permissions  A permission array containing the permission
     * @param grantResults The results array containing the permission results
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }

        @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Nothing to do here
    }
}
