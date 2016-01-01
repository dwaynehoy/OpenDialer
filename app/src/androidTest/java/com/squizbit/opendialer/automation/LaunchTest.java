package com.squizbit.opendialer.automation;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import com.squizbit.opendialer.LauncherActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class LaunchTest {

    private UiDevice mDevice;

    @Rule
    public ActivityTestRule<LauncherActivity> mActivityRule =  new ActivityTestRule<>(LauncherActivity.class, true, false);

    @Before
    public void setup(){
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void testPermissions(){
        mActivityRule.launchActivity(null);
    }
}
