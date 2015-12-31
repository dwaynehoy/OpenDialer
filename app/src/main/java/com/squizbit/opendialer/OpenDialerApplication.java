package com.squizbit.opendialer;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * The main application file for OpenDialer. Contains Application accessible variables. THIS SHOULD
 * NOT BE A DUMPING GROUND FOR CONSTANTS AND LAZY VARIABLES.
 */
public class OpenDialerApplication extends Application {
    private Tracker mTracker;

    public static OpenDialerApplication getApplication(Application application){
        return (OpenDialerApplication) application;
    }

    /**
     * Returns the App instance of the Analytics tracker.
     * @return The App instance of the Analytics tracker
     */
    public Tracker getAnalyticsTracker(){
        if(mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(BuildConfig.OPENDIALER_ANALYTICS_KEY);
            mTracker.enableExceptionReporting(true);

        }

        return mTracker;
    }
}
