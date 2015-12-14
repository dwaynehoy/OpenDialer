package com.squizbit.opendialer.library.widget.RecycleviewIndexer;

/**
 * An interface which allows the Indexer's to receive the index for a current adapter position
 */
public interface IndexedAdapter {

    /**
     * Invoked when an Indexer requires an index label lookup on a item in the adapter
     * @param position The position of the item
     * @return A string label containing the index value
     */
    String getIndexLabel(int position);
}