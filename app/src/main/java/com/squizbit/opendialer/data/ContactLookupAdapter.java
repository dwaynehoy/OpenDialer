package com.squizbit.opendialer.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.squizbit.opendialer.models.ContactImage;

import java.util.Locale;

/**
 * A base adapter which provides Contacts lookup on number.
 */
public abstract class ContactLookupAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T>
        implements LoaderManager.LoaderCallbacks<Cursor> {

    protected Context mContext;
    private Cursor mContactsCursor;
    //Column indexes
    private int mNameColumnIndex;
    private int mPictureUriIndex;
    private int mLookupKeyIndex;
    private ArrayMap<String, Integer> mContactPositionLookup;
    private ImageCache mImageCache;
    private PhoneNumberUtil mPhoneNumberUtil;

    /**
     * Creates a new ContactLookupAdapter
     * @param loaderManager The activities loader manager which will manage loading the contact data
     * @param context A valid context
     */
    public ContactLookupAdapter(LoaderManager loaderManager, Context context) {
        mContext = context;
        loaderManager.initLoader(99, null, this);
        mPhoneNumberUtil = PhoneNumberUtil.getInstance();
    }

    protected void indexContacts() {
        mContactPositionLookup = new ArrayMap<>();
        mImageCache = new ImageCache();

        if(mContactsCursor == null){
            return;
        }

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        int normalizedNumberIndex = mContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
        int numberIndex = mContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        mNameColumnIndex = mContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
        mPictureUriIndex = mContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI);
        mLookupKeyIndex = mContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY);

        if (mContactsCursor.moveToFirst()) {
            do {
                String number = mContactsCursor.getString(normalizedNumberIndex);
                if(number == null){
                    try {
                        number = phoneNumberUtil.format(
                                phoneNumberUtil.parse(mContactsCursor.getString(numberIndex), Locale.getDefault().getCountry()),
                                PhoneNumberUtil.PhoneNumberFormat.E164);
                    } catch (NumberParseException e) {
                        number = mContactsCursor.getString(numberIndex);
                    }
                }

                mContactPositionLookup.put(number, mContactsCursor.getPosition());
            } while (mContactsCursor.moveToNext());
        }
    }

    /**
     * Returns the contact based on the number if found
     * @param number The number to perform the contact lookup for.
     * @return The contact if found, null if not or the READ_CONTACTS permission is not granted
     */
    @Nullable
    protected Contact getContact(String number){
        if(ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED){
            return null;
        }

        int contactPosition = lookupContact(number);
        if(mContactsCursor == null || mContactsCursor.isClosed()){
            contactPosition = -1;
        }

        if (contactPosition != -1) {
            mContactsCursor.moveToPosition(contactPosition);
            Contact contact = new Contact();
            contact.mName = mContactsCursor.getString(mNameColumnIndex);
            contact.mPictureUri = mContactsCursor.getString(mPictureUriIndex);
            contact.mContactLookupKey = mContactsCursor.getString(mLookupKeyIndex);
            return contact;
        } else {
            return null;
        }

    }

    private int lookupContact(String number) {
        if (mContactsCursor == null) {
            return -1;
        }
        String formattedNumber = formatNumberToE164(number);
        if (mContactPositionLookup.containsKey(formattedNumber)) {
            return mContactPositionLookup.get(formattedNumber);
        } else {
            return -1;
        }
    }

    private String formatNumberToE164(String number){
        try {
            Phonenumber.PhoneNumber phoneNumber = mPhoneNumberUtil.parse(number, Locale.getDefault().getCountry());
            return mPhoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            return number;
        }
    }

    /**
     * Swaps an existing cursor with a new cursor and triggers a data update
     * @param cursor The cursor to switch to
     */
    public void swapContactsCursor(Cursor cursor) {
        mContactsCursor = cursor;
        indexContacts();
        notifyDataSetChanged();
    }


    @Nullable
    protected Drawable getRoundedContactImage(Contact contact, int dimen) {
        if(contact == null){
            return null;
        }

        ContactImage contactImage = new ContactImage(mContext, contact.getPictureUri());
        return contactImage.getRoundContactDrawable(dimen);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                mContext,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]
                        {
                                ContactsContract.CommonDataKinds.Phone._ID,
                                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                                ContactsContract.CommonDataKinds.Phone.NUMBER,
                                ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
                        },
                ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP + " = 1 AND " + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + " = " + 1,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor contactCursor) {
        mContactsCursor = contactCursor;
        indexContacts();
        notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mContactsCursor = null;
        indexContacts();
        notifyDataSetChanged();
    }

    /**
     * A simple container class which contains details of a contact
     */
    public static class Contact {
        private String mName;
        private String mPictureUri;
        private String mContactLookupKey;

        public String getName() {
            return mName;
        }

        public String getPictureUri() {
            return mPictureUri;
        }

        public String getContactLookupKey() {
            return mContactLookupKey;
        }
    }

    /**
     * A class which provides in memory image caching
     */
    private class ImageCache extends LruCache<String, Bitmap>{

        private static final int MAX_CACHE_SIZE = 1024;
        /**
         * Crates a new ImageCache instance
         */
        public ImageCache() {
            super(MAX_CACHE_SIZE);
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            float kbCount = Math.max(value.getByteCount()/1024f, 1);

            return (int) Math.round(Math.ceil(kbCount));
        }

    }
}
