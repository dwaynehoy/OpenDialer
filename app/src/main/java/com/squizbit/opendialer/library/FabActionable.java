package com.squizbit.opendialer.library;

/**
 * An interface which allows the owner to call a actionable event when the fab button is called
 */
public interface FabActionable {
    /**
     * Triggers an action associated with the floating action button
     * @return True if the actionable is done and should be closed/removed or false if it's visual state should not change
     */
    boolean onFabAction();
}
