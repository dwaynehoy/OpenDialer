package com.squizbit.opendialer.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squizbit.opendialer.R;
import com.squizbit.opendialer.library.widget.RecycleviewIndexer.IndexedAdapter;
import com.squizbit.opendialer.models.ContactColorGenerator;
import com.squizbit.opendialer.models.ContactImage;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * An adapter which displays a list of contacts to the user
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> implements OnClickListener, IndexedAdapter {

    private final ContactColorGenerator mColorMatcher;
    private Context mContext;
    private Cursor mCursor;
    private int mNameIndex;
    private int mPhotoUriIndex;
    private int mKeyIndex;

    private OnContactSelectedListener mContactSelectedListener;

    /**
     * Creates a new Contacts Adapter which will be used to populate a recycle view
     * @param cursor The cursor containing the contacts
     */
    public ContactsAdapter(Context context, Cursor cursor, ContactColorGenerator colorMatcher){
        mContext = context;
        mCursor = cursor;
        mColorMatcher = colorMatcher;

        mKeyIndex = mCursor.getColumnIndex(ContactsContract.Profile.LOOKUP_KEY);
        mNameIndex = mCursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME_PRIMARY);
        mPhotoUriIndex = mCursor.getColumnIndex(ContactsContract.Profile.PHOTO_THUMBNAIL_URI);
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ContactsViewHolder(inflater.inflate(R.layout.contact_listitem_view, parent, false));
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

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(this);
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
