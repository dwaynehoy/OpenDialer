package com.squizbit.opendialer.models;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 * A class which provides permission compatibility when requesting the OS to dial a number
 */
public class DialerHelper {

    public static final int PERMISSION_REQUEST_CODE = 111;

    private Activity mActivity;
    private String mNumber;
    //Boolean flag which ensures only DialerHelpers that are waiting on permissions, actively
    //process them. This means that the caller doesn't need to know the inner workings.
    private boolean mAwaitingPermissions;

    /**
     * Creates a new DialerHelper instance.
     *
     * @param activity The parent activity
     */
    public DialerHelper(Activity activity) {
        mActivity = activity;
    }

    /**
     * This will first check for permissions, and if permissions are found this will trigger the OS to
     * directly dial the specified number. If permissions are not found, this will automatically request
     * permissions, as such Activity onRequestPermissionResult(..) should be passed through to
     * {@link #onRequestPermissionsResult(int, String[], int[])} which will dial the number once permissions
     * have been granted
     *
     * @param number
     * @return True if requesting permission, false otherwise
     */
    public boolean dialNumber(String number) {
        mNumber = number;

        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CODE);
            mAwaitingPermissions = true;
            return true;
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + mNumber));
            mActivity.startActivity(intent);
            mAwaitingPermissions = false;
            return false;
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

        if (mAwaitingPermissions && requestCode == PERMISSION_REQUEST_CODE) {
            mAwaitingPermissions = false;

            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + mNumber));

                mActivity.startActivity(intent);
            }
        }
    }

}