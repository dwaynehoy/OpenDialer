package com.squizbit.opendialer.library;

import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An interface which allows the owner to call a actionable event when the fab button is called, and
 * also
 * has the ability to tell the owner what the button icon should like like and where it's located
 */
public interface FabControllable extends FabActionable {

    @IntDef({FAB_CENTER, FAB_LEFT, FAB_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    @interface FabPosition {}

    int FAB_CENTER = 1;
    int FAB_LEFT = 2;
    int FAB_RIGHT = 3;

    /**
     * Returns the resource id of the desiged fab icon drawable to be displayed on the global fab button
     * @return The resource id of the desiged fab icon drawable to be displayed on the global fab button
     */
    @DrawableRes
    int getFabResource();

    /**
     * Returns the desired position of the FAB
     * @return The desired position of the FAB
     */
    @FabPosition
    int getFabPosition();

    /**
     * Returns the button color for the FAB
     * @return The button color for the FAB
     */
    @ColorInt
    int getFabColor();

    /**
     * Returns a flag determining of the owner should propagate the call of onBackPress or swallow it
     * @return True if the back press has been delt with and should not be propagated, false otherwise
     */
    boolean onBackPress();

    /**
     * Sets the owner of the FabController instance
     * @param owner The owner of the FabController instance
     */
    void setFabControllableOwner(FabControllerOwner owner);
}
