package com.squizbit.opendialer.library.widget;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squizbit.opendialer.R;
import com.squizbit.opendialer.library.widget.BottomSheet.ViewBuilder;
import com.squizbit.opendialer.models.ContactColorGenerator;
import com.squizbit.opendialer.models.DialerHelper;
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
    Preferences mPreferences;

    private View.OnClickListener mOnNumberClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String number = (String) v.getTag();

            if (!mDialerHelper.dialNumber(number)) {
                mPreferences.setLastDialedNumber(number);
            }
        }
    };

    private View.OnClickListener mOnEmailClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = (String) v.getTag();

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",email, null));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, email);
            getContext().startActivity(emailIntent);
        }
    };

    private View.OnClickListener mOnWebLinkClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String url = (String) v.getTag();

            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            getContext().startActivity(webIntent);
        }
    };

    private View.OnLongClickListener mOnPopupMenuTriggerListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View v) {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            MenuItem menuItem = popupMenu.getMenu().add(0, 1, 0, R.string.context_menu_copy);
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newPlainText("", v.getTag().toString()));

                    return true;
                }
            });
            popupMenu.show();

            return true;
        }
    };

    /**
     * Creates a new ContactDetailViewBuilder instance
     * @param owner The owning activity
     * @param lookupId The contact lookup id
     * @param dialerHelper The dialer helper responsible for trigger any call requests
     */
    public ContactDetailViewBuilder(FragmentActivity owner, String lookupId, DialerHelper dialerHelper) {
        super(owner);
        mLookupId = lookupId;
        mDialerHelper = dialerHelper;
        mPreferences = new Preferences(getContext());
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
        uri = Uri.withAppendedPath(uri, ContactsContract.Contacts.Entity.CONTENT_DIRECTORY);

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

        if (data.moveToFirst()) {
            do {
                String mimeType = data.getString(data.getColumnIndex(ContactsContract.Contacts.Entity.MIMETYPE));

                if (displayImage == null && mimeType.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                    displayImage = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI));
                } else if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                    insertPhoneNumber(
                            data.getInt(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)),
                            data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                            data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)),
                            data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)),
                            data.getInt(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)) == 1
                    );

                } else if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                    insertEmail(data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
                } else if (mimeType.equals(ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)) {
                    insertWebPage(data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
                }
            } while (data.moveToNext());

            Bitmap contactBitmap = getContactImage(displayImage, Math.min(mParent.getWidth(), 720));

            int color = - 1;
            if (contactBitmap != null) {
                ((ImageView) mView.findViewById(R.id.imageViewContact)).setImageBitmap(contactBitmap);
                Palette bitmapPallet = Palette.from(contactBitmap).generate();
                color = bitmapPallet.getDarkVibrantColor(-1);
                if(color == -1){
                    color = bitmapPallet.getLightMutedColor(-1);
                }
            } else {
                ContactColorGenerator generator = new ContactColorGenerator(
                        getContext(),
                        R.color.contact_red,
                        R.color.contact_blue,
                        R.color.contact_purple,
                        R.color.contact_yellow,
                        R.color.contact_green);
                color = generator.getContactColor(mLookupId);

                mView.findViewById(R.id.imageViewContact).setBackgroundColor(color);
            }

            addSectionIcons(color);
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

        view.setOnClickListener(mOnNumberClickListener);
        view.setOnLongClickListener(mOnPopupMenuTriggerListener);

        if(isPrimary){
            parent.addView(view, 0);
        } else {
            parent.addView(view);
        }

    }

    private void insertEmail(String email) {
        ViewGroup parent = (ViewGroup) mView.findViewById(R.id.linearLayoutEmailContainer);

        View view = getLayoutInflater().inflate(R.layout.contact_detail_item_view, parent, false);
        view.setTag(email);
        view.setOnClickListener(mOnEmailClickListener);
        view.setOnLongClickListener(mOnPopupMenuTriggerListener);

        ((TextView) view.findViewById(R.id.textViewItem)).setText(email);

        parent.addView(view);
    }

    private void insertWebPage(String url) {
        mView.findViewById(R.id.cardViewContactWebpage).setVisibility(View.VISIBLE);
        ViewGroup parent = (ViewGroup) mView.findViewById(R.id.linearLayoutWebpageContainer);

        View view = getLayoutInflater().inflate(R.layout.contact_detail_item_view, parent, false);
        view.setTag(url);
        view.setOnClickListener(mOnWebLinkClick);
        view.setOnLongClickListener(mOnPopupMenuTriggerListener);

        ((TextView) view.findViewById(R.id.textViewItem)).setText(url);

        parent.addView(view);
    }

    private void addSectionIcons(@ColorInt int color){
        ViewGroup sectionContainer = (ViewGroup)mView.findViewById(R.id.linearLayoutNumberContainer);
        if(sectionContainer.getChildCount() > 0){
            Drawable drawable = ResourcesCompat.getDrawable(
                    getContext().getResources(),
                    R.drawable.ic_call_tintable,
                    getContext().getTheme());
            if(drawable != null){
                drawable = drawable.mutate();
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, color);
            }

            ((ImageView)sectionContainer.getChildAt(0).findViewById(R.id.imageViewType)).setImageDrawable(drawable);
        }

        sectionContainer = (ViewGroup)mView.findViewById(R.id.linearLayoutEmailContainer);
        if(sectionContainer.getChildCount() > 0){
            Drawable drawable = ResourcesCompat.getDrawable(
                    getContext().getResources(),
                    R.drawable.ic_email_tintable,
                    getContext().getTheme());
            if(drawable != null){
                drawable = drawable.mutate();
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, color);
            }

            ((ImageView)sectionContainer.getChildAt(0).findViewById(R.id.imageViewType)).setImageDrawable(drawable);
        }

        sectionContainer = (ViewGroup)mView.findViewById(R.id.linearLayoutWebpageContainer);
        if(sectionContainer.getChildCount() > 0){
            Drawable drawable = ResourcesCompat.getDrawable(
                    getContext().getResources(),
                    R.drawable.ic_web_tintable,
                    getContext().getTheme());
            if(drawable != null){
                drawable = drawable.mutate();
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, color);
            }

            ((ImageView)sectionContainer.getChildAt(0).findViewById(R.id.imageViewType)).setImageDrawable(drawable);
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
