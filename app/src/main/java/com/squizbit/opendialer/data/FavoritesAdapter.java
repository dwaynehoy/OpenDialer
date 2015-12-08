package com.squizbit.opendialer.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.squizbit.opendialer.R;
import com.squizbit.opendialer.library.widget.RecycleviewIndexer.IndexedAdapter;
import com.squizbit.opendialer.models.ContactThemeColorMatcher;

import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * An adapter which displays a list of contacts to the user
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ContactsViewHolder> implements OnClickListener, IndexedAdapter {

    private final ContactThemeColorMatcher mColorMatcher;
    private Context mContext;
    private Cursor mCursor;

    private final int mKeyIndex;
    private int mNameIndex;
    private int mPhotoUriIndex;
    private int mStarredIndex;

    private OnContactSelectedListener mContactSelectedListener;

    /**
     * Creates a new Contacts Adapter which will be used to populate a recycle view
     * @param cursor The cursor containing the contacts
     */
    public FavoritesAdapter(Context context, Cursor cursor, ContactThemeColorMatcher colorMatcher){
        mContext = context;
        mCursor = cursor;
        mColorMatcher = colorMatcher;

        mKeyIndex = mCursor.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY);
        mNameIndex = mCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
        mPhotoUriIndex = mCursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI);
        mStarredIndex = mCursor.getColumnIndex(ContactsContract.PhoneLookup.STARRED);
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ContactsViewHolder(inflater.inflate(R.layout.contact_card_view, parent, false));
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
    public void onBindViewHolder(ContactsViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        Bitmap contactBitmap = getContactImage(
                mCursor.getString(mPhotoUriIndex),
                mContext.getResources().getDimensionPixelOffset(R.dimen.small_card_height));
        if(contactBitmap == null) {
            contactBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.img_contact_placeholder_medium);
            holder.imageViewContact.setBackgroundColor(mColorMatcher.getContactColor(mCursor.getString(mKeyIndex)));
        }

        holder.imageViewContact.setImageBitmap(contactBitmap);
        holder.textViewName.setText(mCursor.getString(mNameIndex));
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(this);

        if(mCursor.getInt(mStarredIndex) == 1){ //Starred
            holder.imageViewStarred.setImageResource(R.drawable.ic_star_selected);
            holder.imageViewStarred.setContentDescription(mContext.getString(R.string.starred_contact_desc));
        } else {
            holder.imageViewStarred.setImageResource(R.drawable.ic_star_unselected);
            holder.imageViewStarred.setContentDescription(mContext.getString(R.string.frequent_contact_desc));
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

    @Override
    public String getIndexLabel(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getString(mNameIndex).substring(0, 1);
    }

    /**
     * The viewholder for the contact item views
     */
    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.textViewName)
        TextView textViewName;

        @InjectView(R.id.imageViewContact)
        ImageView imageViewContact;

        @InjectView(R.id.imageViewStarred)
        ImageView imageViewStarred;

        ContactsViewHolder(View view) {
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
