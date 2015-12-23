package com.squizbit.opendialer.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.squizbit.opendialer.R;
import com.squizbit.opendialer.models.ContactColorGenerator;
import com.squizbit.opendialer.models.ContactImage;

import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * An adapter which displays a list of contacts to the user
 */
public class ContactSearchAdapter extends RecyclerView.Adapter<ContactSearchAdapter.ContactsSearchViewHolder> implements OnClickListener {

    private Context mContext;
    private Cursor mCursor;
    private int mNameIndex;

    private final int mKeyIndex;
    private int mNumberIndex;
    private int mPhotoUriIndex;

    private String  mDefaultCountryCode;
    private PhoneNumberUtil mPhoneNumberUtil;
    private ContactColorGenerator mColorMatcher;

    private OnContactSelectedListener mContactSelectedListener;

    /**
     * Creates a new Contacts Adapter which will be used to populate a recycle view
     * @param cursor The cursor containing the contacts
     * @param defaultCountryCode The country code of the user's home country
     */
    public ContactSearchAdapter(Context context, Cursor cursor, String defaultCountryCode, ContactColorGenerator colorMatcher){
        mContext = context;
        mCursor = cursor;
        mDefaultCountryCode = defaultCountryCode;
        mPhoneNumberUtil = PhoneNumberUtil.getInstance();
        mColorMatcher = colorMatcher;

        mKeyIndex = mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY);
        mNameIndex = mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
        mNumberIndex = mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        mPhotoUriIndex = mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);
    }

    @Override
    public ContactsSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new ContactsSearchViewHolder(inflater.inflate(R.layout.contact_search_listitem_view, parent, false));
    }

    public void setContactSelectedListener(OnContactSelectedListener contactSelectedListener) {
        mContactSelectedListener = contactSelectedListener;
    }

    public void swapCursor(Cursor cursor){
        mCursor = cursor;

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if(mCursor != null) {
            count = mCursor.getCount();
        }

        return count;
    }

    @Override
    public void onBindViewHolder(ContactsSearchViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        ContactImage contactImage = new ContactImage(mContext, mCursor.getString(mPhotoUriIndex));
        Drawable contactDrawable = contactImage.getRoundContactDrawable(mContext.getResources().getDimensionPixelOffset(R.dimen.contact_thumbnail));

        if(contactDrawable == null) {
            Bitmap contactBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.img_contact_placeholder_small);
            holder.imageViewContact.setImageBitmap(contactBitmap);
            holder.imageViewContact.setBackground(mColorMatcher.getContactPlaceholderDrawable(mCursor.getString(mKeyIndex)));
        } else {
            holder.imageViewContact.setImageDrawable(contactDrawable);
            holder.imageViewContact.setBackground(null);
        }

        holder.textViewName.setText(mCursor.getString(mNameIndex));
        holder.textViewNumber.setText(formatNumber(mCursor.getString(mNumberIndex)));

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(this);
    }

    private String formatNumber(String number){
        try {
            Phonenumber.PhoneNumber phoneNumber =
                    mPhoneNumberUtil.parse(mCursor.getString(mNumberIndex), mDefaultCountryCode);
            return mPhoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);

        } catch (NumberParseException e) {
            return number;
        }
    }

    private Bitmap getContactImage(String uriString, int dimen){
        if(uriString == null || uriString.isEmpty()){
            return null;
        }

        try {
            Uri uri = Uri.parse(uriString);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
            return Bitmap.createScaledBitmap(bitmap, dimen, dimen, true);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

    }

    @Override
    public void onClick(View v) {
        if(mContactSelectedListener != null) {
            Integer position = (Integer) v.getTag();
            mCursor.moveToPosition(position);

            mContactSelectedListener.onContactSelected(mCursor);
        }
    }

    /**
     * The viewholder for the contact item views
     */
    public static class ContactsSearchViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.textViewName)
        TextView textViewName;

        @InjectView(R.id.textViewNumber)
        TextView textViewNumber;

        @InjectView(R.id.imageViewContact)
        ImageView imageViewContact;

        ContactsSearchViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    /**
     * A callback interface which allows a object instance to receive callbacks when the user
     * selects a contact
     */
    public interface OnContactSelectedListener{

        /**
         * Triggered when the user has selected a contact
         * @param contact The cursor data of the contact selected
         */
        void onContactSelected(Cursor contact);
    }

}
