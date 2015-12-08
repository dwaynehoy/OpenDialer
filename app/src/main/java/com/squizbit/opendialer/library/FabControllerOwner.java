package com.squizbit.opendialer.library;

/**
 * A callback which allows children of this instance to control the global FAB button
 */
public interface FabControllerOwner {

    /**
     * Invoked when a child FabController's state has changed in such a way that it wishes to modify
     * the state of the global FAB button
     * @param fabControllable The child that is requesting the FAB update
     */
    void onFabStatusChanged(FabControllable fabControllable);

    /**
     * Invoked when a child FabController is about to invoke onFabControllableClose, the delay between
     * this call and onFabControllableClose is to allow cleanup of the FabController
     * @param fabControllable The child that now invalid
     */
    void onFabControllableClosing(FabControllable fabControllable);

    /**
     * Invoked when a child FabController is no longer in a valid state i.e. has been closed, and the
     * FabControllerOwner should no longer send any requests
     * @param fabControllable The child that now invalid
     */
    void onFabControllableClose(FabControllable fabControllable);
}
