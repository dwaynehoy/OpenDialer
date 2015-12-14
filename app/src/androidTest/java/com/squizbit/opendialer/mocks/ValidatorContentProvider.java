package com.squizbit.opendialer.mocks;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.net.Uri;
import android.test.mock.MockContentProvider;

@SuppressLint("Registered")
public class ValidatorContentProvider extends MockContentProvider {

    public ValidatorContentProvider(Uri expectedUri){
        mExpectedUri = expectedUri;
    }

    ContentValues mContentValues;
    Uri mExpectedUri;

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if(!uri.equals(mExpectedUri)){
            throw new RuntimeException("Uri was different than expected");
        }
        mContentValues = values;
        return uri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if(!uri.equals(mExpectedUri)){
            throw new RuntimeException("Uri was different than expected");
        }

        mContentValues = values;
        return 1;
    }

    public ContentValues getContentValues() {
        return mContentValues;
    }
}
