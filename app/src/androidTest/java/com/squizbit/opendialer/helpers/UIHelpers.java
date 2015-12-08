package com.squizbit.opendialer.helpers;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.pressBack;

/**
 * Created by dwaynehoy on 31/08/15.
 */
public class UIHelpers {
    public static void closeSoftKeyboardAndWait(){
        closeSoftKeyboard();
        try {Thread.sleep(250);} catch (InterruptedException e) {}
    }

    public static void pressBackButtonAndWait(){
        pressBack();
        try {Thread.sleep(500);} catch (InterruptedException e) {}
    }
}
