package com.squizbit.opendialer.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.CallLog;
import android.support.v4.app.LoaderManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squizbit.opendialer.R;
import com.squizbit.opendialer.models.ContactThemeColorMatcher;
import com.squizbit.opendialer.models.RelativeTimeFormat;

import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * An adapter which ...
 */
public class CallLogAdapter extends ContactLookupAdapter<CallLogAdapter.CallLogViewHolder> {

    private static final int INCOMING_CALL_SYMBOL = 0;
    private static final int OUTGOING_CALL_SYMBOL = 1;
    private static final int MISSED_OR_REJECTED_CALL_SYMBOL = 2;
    private static final int MISSED_OUTGOING_CALL_SYMBOL = 3;

    private final Context mContext;
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private ArrayList<Drawable> mStatusDrawableList;
    private OnCallLogEntryActionListener mOnCallLogEntryActionListener;
    private RelativeTimeFormat mRelativeTime;
    private ContactThemeColorMatcher mColorMatcher;

    public final View.OnClickListener mOnCallClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mOnCallLogEntryActionListener != null && v.getTag() != null){
                mOnCallLogEntryActionListener.onCallActionTriggered((String) v.getTag());
            }
        }
    };

    private View.OnClickListener mOnContactClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mOnCallLogEntryActionListener.onContactActionTriggered((String) v.getTag());
        }
    };

    public CallLogAdapter(Context context, Cursor cursor, LoaderManager loaderManager, ContactThemeColorMatcher colorMatcher) {
        super(loaderManager, context);
        mContext = context;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(mContext);
        buildSymbolArray();
        mRelativeTime = new RelativeTimeFormat(new Date());
        mColorMatcher = colorMatcher;
    }

    @SuppressWarnings("deprecation")
    private void buildSymbolArray() {
        mStatusDrawableList = new ArrayList<>(4);
        Drawable symbol = DrawableCompat.wrap(mContext.getResources().getDrawable(R.drawable.ic_incoming_call_status));
        symbol = symbol.mutate();
        DrawableCompat.setTint(symbol, mContext.getResources().getColor(R.color.incoming_call_status));
        mStatusDrawableList.add(INCOMING_CALL_SYMBOL, symbol);

        symbol = DrawableCompat.wrap(mContext.getResources().getDrawable(R.drawable.ic_outgoing_call_status));
        symbol = symbol.mutate();
        DrawableCompat.setTint(symbol, mContext.getResources().getColor(R.color.outgoing_call_status));
        mStatusDrawableList.add(OUTGOING_CALL_SYMBOL, symbol);

        symbol = DrawableCompat.wrap(mContext.getResources().getDrawable(R.drawable.ic_incoming_call_status));
        symbol = symbol.mutate();
        DrawableCompat.setTint(symbol, mContext.getResources().getColor(R.color.missed_call_status));
        mStatusDrawableList.add(MISSED_OR_REJECTED_CALL_SYMBOL, symbol);

        symbol = DrawableCompat.wrap(mContext.getResources().getDrawable(R.drawable.ic_outgoing_call_status));
        symbol = symbol.mutate();
        DrawableCompat.setTint(symbol, mContext.getResources().getColor(R.color.missed_call_status));
        mStatusDrawableList.add(MISSED_OUTGOING_CALL_SYMBOL, symbol);
    }

    @Override
    public CallLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.calllog_listitem_view, parent, false);

        return new CallLogViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public void onBindViewHolder(CallLogViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String number = mCursor.getString(mCursor.getColumnIndex(CallLog.Calls.NUMBER));
        Contact contact = getContact(number);
        if(contact != null) {
            holder.mTextViewName.setText(contact.getName());
            holder.mImageViewContact.setTag(contact.getContactLookupKey());
            holder.mImageViewContact.setOnClickListener(mOnContactClickListener);
        } else {
            holder.mTextViewName.setText(number);
        }

        holder.mImageViewCall.setTag(number);
        holder.mImageViewCall.setOnClickListener(mOnCallClickListener);

        Drawable contactDrawable = getRoundedContactImage(contact);
        if(contactDrawable != null) {
            holder.mImageViewContact.setImageDrawable(contactDrawable);
            holder.mImageViewContact.setBackground(null);
        } else if(contact != null){
            holder.mImageViewContact.setImageResource(R.drawable.img_contact_placeholder_small);
            holder.mImageViewContact.setBackground(mColorMatcher.getCircularBackgroundDrawable(contact.getContactLookupKey()));
        } else {
            holder.mImageViewContact.setImageResource(R.drawable.img_contact_placeholder_small);
            holder.mImageViewContact.setBackground(null);
        }

        int type = mCursor.getInt(mCursor.getColumnIndex(CallLog.Calls.TYPE));

        long date = mCursor.getLong(mCursor.getColumnIndex(CallLog.Calls.DATE));
        holder.mTextViewRelativeTime.setCompoundDrawablesWithIntrinsicBounds(getStatusIcon(type), null, null, null);
        holder.mTextViewRelativeTime.setText(mRelativeTime.format(new Date(date)));

    }

    private Drawable getStatusIcon(int type){
        switch(type){
            case CallLog.Calls.INCOMING_TYPE:
                return mStatusDrawableList.get(INCOMING_CALL_SYMBOL);
            case CallLog.Calls.OUTGOING_TYPE:
                return mStatusDrawableList.get(OUTGOING_CALL_SYMBOL);
            case CallLog.Calls.MISSED_TYPE:
                return mStatusDrawableList.get(MISSED_OR_REJECTED_CALL_SYMBOL);
            case 5: // Rejected call type isn't defined in contract.
                return mStatusDrawableList.get(MISSED_OR_REJECTED_CALL_SYMBOL);
            default:
                Log.e("TEST", "Call type " + type);
                return null;
        }
    }

    public void swapCursor(Cursor cursor){
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public void setOnCallLogEntryActionListener(OnCallLogEntryActionListener onCallLogEntryActionListener) {
        mOnCallLogEntryActionListener = onCallLogEntryActionListener;
    }

    /**
     * Forces a time update on the list
     */
    public void updateRelativeTimes(){
        mRelativeTime = new RelativeTimeFormat(new Date());
        notifyDataSetChanged();
    }

    public static class CallLogViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.imageViewContact)
        ImageView mImageViewContact;
        @InjectView(R.id.textViewName)
        TextView mTextViewName;
        @InjectView(R.id.textViewRelativeTime)
        TextView mTextViewRelativeTime;
        @InjectView(R.id.imageViewCall)
        ImageView mImageViewCall;

        public CallLogViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    /**
     * An interface that allows a class instance to listen to call log entry action callbacks
     */
    public interface OnCallLogEntryActionListener {
        /**
         * Triggered when the user clicks the call button on a call log entry
         *
         * @param number The number associated with the contact
         */
        void onCallActionTriggered(String number);

        /**
         * Triggered when the user clicks the contacts image
         *
         * @param lookupKey The contact lookup key
         */
        void onContactActionTriggered(String lookupKey);
    }

}
