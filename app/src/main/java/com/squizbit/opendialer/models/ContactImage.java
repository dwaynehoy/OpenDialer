package com.squizbit.opendialer.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A model which represents a Contact's Image
 */
public class ContactImage {

    private final Context mContext;
    private final String mImageUri;

    /**
     * Creates a new instance of ContactImage
     * @param context A valid contect
     * @param imageUrl The contact's image Uri
     */
    public ContactImage(Context context, String imageUrl){
        mContext = context;
        mImageUri = imageUrl;
    }

    /**
     * Retrieves a contacts image in the specified dimensions
     * @param dimen The dimension of the resulting bitmap
     * @return The contacts image in the specified resolution, null if not found.
     */
    @Nullable
    public Bitmap getContactImageBitmap(int dimen){
        if(mImageUri == null || mImageUri.isEmpty()){
            return null;
        }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri.parse(mImageUri));
            return Bitmap.createScaledBitmap(bitmap, dimen, dimen, true);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Retrieves a contact's image as a rounded image in the specified dimensions
     * @param dimen The dimension of the resulting bitmap
     * @return A rounded drawable containing the contact's profile image, null if not found.
     */
    @Nullable
    public Drawable getRoundContactDrawable(int dimen) {
        Bitmap contactBitmap = getContactImageBitmap(dimen);
        if(contactBitmap != null){
            RoundedBitmapDrawable contactDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), contactBitmap);
            contactDrawable.setAntiAlias(true);
            contactDrawable.setCornerRadius(contactDrawable.getIntrinsicWidth()/2);
            return contactDrawable;
        }

        return null;
    }

    /**
     * Retrieves a contact's image as a rounded image in the specified dimensions
     * @param dimen The dimension of the resulting bitmap
     * @return A rounded drawable containing the contact's profile image, null if not found.
     */
    @Nullable
    public Drawable createRoundedPlaceholder(int dimen, int placeholderResourceId) {
        Bitmap contactBitmap = getContactImageBitmap(dimen);
        if(contactBitmap != null){
            RoundedBitmapDrawable contactDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), contactBitmap);
            contactDrawable.setAntiAlias(true);
            contactDrawable.setCornerRadius(contactDrawable.getIntrinsicWidth()/2);
            return contactDrawable;
        }

        return null;
    }
}
