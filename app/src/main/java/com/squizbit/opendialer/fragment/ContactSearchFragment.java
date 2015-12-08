package com.squizbit.opendialer.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.squizbit.opendialer.R;
import com.squizbit.opendialer.data.ContactSearchAdapter;
import com.squizbit.opendialer.library.widget.RecycleviewIndexer.RecyclerViewFastScroller;
import com.squizbit.opendialer.models.ContactThemeColorMatcher;

import java.util.Locale;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A fragment which displays a list of the users system contacts, and allows a user to select one
 * to call
 */
public class ContactSearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        TextWatcher {

    private static final Pattern PHONE_NUMBER_MATCHER = Pattern.compile("^\\+?[0-9\\-]+\\*?$");

    //region View fields
    @InjectView(R.id.linear_layout_contact_list_main)
    LinearLayout mLinearLayoutMain;

    @InjectView(R.id.contact_list)
    RecyclerView mContactList;

    @InjectView(R.id.toolbarMain)
    Toolbar mToolbarMain;

    @InjectView(R.id.recyclerViewFastScroller)
    RecyclerViewFastScroller mRecyclerViewFastScroller;

    @InjectView(R.id.cardViewToolbarContainer)
    CardView mCardViewToolbarContainer;

    @InjectView(R.id.editTextSearch)
    EditText mEditTextSearch;
    //endregion

    private ContactSearchAdapter mContactsAdapter;
    private String mQuery = "";
    private OnContactSelectedListener mRecipientSelectedListener;
    private KeyboardDismisser mKeyboardDismisser;
    private OnScrollStateChangedListener mOnScrollStateChangedListener;

    private String[] mContactColumns = new String[]
            {
                    ContactsContract.CommonDataKinds.Phone._ID,
                    ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            };

    private ContactSearchAdapter.OnContactSelectedListener mOnContactSelectedListener = new ContactSearchAdapter.OnContactSelectedListener() {
        @Override
        public void onContactSelected(Cursor contact) {
            if (mRecipientSelectedListener != null) {
                mRecipientSelectedListener.onRecipientSelected(contact.getString(1), !contact.isNull(3) ? contact.getString(3) : contact.getString(4));
            }
        }
    };

    /**
     * Creates a new instance of the Contact Search Fragment
     *
     * @param rawSearchAction     The action instruction for the raw search string if it's a number
     *                            i.e.
     *                            Send message to (search number)
     * @param displayOnlyOnSearch A flag indicating whether results should be shown if the query
     *                            string
     *                            is empty
     * @param preHideSearchBar A flag which indicates whether the search bar should be initially visible
     * @return A new instance of the ContactSearchFragment
     */
    public static Fragment newInstance(String rawSearchAction, boolean displayOnlyOnSearch, boolean preHideSearchBar) {
        Bundle args = new Bundle();
        args.putString("rawSearchAction", rawSearchAction);
        args.putBoolean("displayOnlyOnSearch", displayOnlyOnSearch);
        args.putBoolean("preHideSearchBar", preHideSearchBar);

        Fragment fragment = new ContactSearchFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contacts_list_view, container, false);
        ButterKnife.inject(this, view);

        mContactList.setFocusable(true);
        mEditTextSearch.addTextChangedListener(this);
        mCardViewToolbarContainer.setVisibility(View.VISIBLE);
        mKeyboardDismisser = new KeyboardDismisser();
        mContactsAdapter = null;

        mCardViewToolbarContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (getArguments().getBoolean("preHideSearchBar")) {
                    mCardViewToolbarContainer.setTranslationY(-mCardViewToolbarContainer.getHeight());
                    mRecyclerViewFastScroller.setTranslationY(-mCardViewToolbarContainer.getHeight());
                    mCardViewToolbarContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(mEditTextSearch, 0);
                    mCardViewToolbarContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerViewFastScroller.setOnScrollListener(mKeyboardDismisser);
        mEditTextSearch.requestFocus();
        mContactList.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (doRunQuery()) {
            getLoaderManager().initLoader(0, null, this);
        }
    }

    /**
     * Sets a callback which will be invoked when a recipient is selected by the user
     *
     * @param recipientSelectedListener The callback to be invoked
     */
    public void setRecipientSelectedListener(OnContactSelectedListener recipientSelectedListener) {
        mRecipientSelectedListener = recipientSelectedListener;
    }

    /**
     * Hides the search bar by animating it upwards off the scren
     */
    public void hideSearchBar(){

        ObjectAnimator searchBarAnimation = ObjectAnimator
                .ofFloat(
                        mCardViewToolbarContainer,
                        "translationY",
                        mCardViewToolbarContainer.getTranslationY(),
                        -mCardViewToolbarContainer.getHeight());

        ObjectAnimator listAnimation = ObjectAnimator
                .ofFloat(
                        mRecyclerViewFastScroller,
                        "translationY",
                        mCardViewToolbarContainer.getTranslationY(),
                        -mCardViewToolbarContainer.getHeight());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .play(searchBarAnimation)
                .with(listAnimation);

        animatorSet.start();
    }

    /**
     * Displays the search bar by animating it downwards onto the screen
     */
    public void showSearchBar(){
        ObjectAnimator searchBarAnimation = ObjectAnimator
                .ofFloat(
                        mCardViewToolbarContainer,
                        "translationY",
                        mCardViewToolbarContainer.getTranslationY(),
                        0);

        ObjectAnimator listAnimation = ObjectAnimator
                .ofFloat(
                        mRecyclerViewFastScroller,
                        "translationY",
                        mCardViewToolbarContainer.getTranslationY(),
                        0);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .play(searchBarAnimation)
                .with(listAnimation);

        animatorSet.start();
    }

    /**
     * Sets a callback which is invoked when a contact has been selected
     * @param onContactSelectedListener The callback to be invoked when a contact has been selected
     */
    public void setOnContactSelectedListener(ContactSearchAdapter.OnContactSelectedListener onContactSelectedListener) {
        mOnContactSelectedListener = onContactSelectedListener;
    }

    /**
     * Sets a callback which is invoked when the contact search list scroll state has changed
     * @param onScrollStateChangedListener The callback to be invoked when the scroll state changes
     */
    public void setOnScrollStateChangedListener(OnScrollStateChangedListener onScrollStateChangedListener){
        mOnScrollStateChangedListener = onScrollStateChangedListener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String whereQuery = "";
        String[] whereParameters = null;

        String whereTemplate =
                " AND (replace(replace(%s, ' ', ''),'-', '') like ? OR " +
                        " replace(replace(%s, ' ', ''),'-', '') like ? OR " +
                        " %s like ? )";

        if (!mQuery.isEmpty()) {
            whereQuery = String.format(
                    whereTemplate,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);

            whereParameters = new String[]{"%" + mQuery + "%", "%" + mQuery + "%", "%" + mQuery + "%"};
        }

        return new CursorLoader(
                getActivity(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                mContactColumns,
                ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP + " = 1 AND "
                        + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + " = " + 1
                        + whereQuery,
                whereParameters,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(!doRunQuery()){
            return;
        }

        //If the user has entered a valid phone number, add a "send message to" option for
        //the user to select a non-contact phone number
        if (PHONE_NUMBER_MATCHER.matcher(mQuery).matches()) {
            MatrixCursor matrixCursor = new MatrixCursor(mContactColumns);
            Object[] row = new Object[]{
                    -1,
                    -1,
                    getArguments().getString("rawSearchAction", getString(R.string.make_call_to_label)),
                    mQuery,
                    mQuery,
                    null,
                    null};
            matrixCursor.addRow(row);
            data = new MergeCursor(new Cursor[]{matrixCursor, data});
        }

        if (mContactsAdapter == null) {
            ContactThemeColorMatcher colorMatcher = new ContactThemeColorMatcher(
                    getContext(),
                    R.color.contact_red,
                    R.color.contact_blue,
                    R.color.contact_purple,
                    R.color.contact_yellow,
                    R.color.contact_green);

            mContactsAdapter = new ContactSearchAdapter(
                    getActivity(),
                    data,
                    Locale.getDefault().getCountry(),
                    colorMatcher);
            mContactsAdapter.setContactSelectedListener(mOnContactSelectedListener);
            mContactList.setAdapter(mContactsAdapter);

        } else {
            mContactsAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mContactsAdapter != null) {
            mContactsAdapter.swapCursor(null);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //Nothing to do
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //Nothing to do
    }

    @Override
    public void afterTextChanged(Editable s) {
        setQuery(s.toString());
    }

    /**
     * Sets the query to perform the search on
     * @param query The query to search
     */
    public void setQuery(String query) {
        mQuery = query;

        if (doRunQuery()) {
            getLoaderManager().restartLoader(0, null, this);
        } else if(mContactsAdapter != null){
            MatrixCursor matrixCursor = new MatrixCursor(mContactColumns);
            mContactsAdapter.swapCursor(matrixCursor);
        }
    }

    private boolean doRunQuery() {
        return !mQuery.isEmpty() ||
                !getArguments().getBoolean("displayOnlyOnSearch");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * A helper class which dismisses the keyboard when the recycler view scrolls
     */
    private class KeyboardDismisser extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if(mOnScrollStateChangedListener != null){
                mOnScrollStateChangedListener.onStrollStateChanged(newState);
            }

            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                mLinearLayoutMain.requestFocus(View.FOCUS_UP);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditTextSearch.getWindowToken(), 0);
            }
        }
    }

    /**
     * A callback which is invoked when a contact is selected from the contact search list
     */
    public interface OnContactSelectedListener {

        /**
         * Invoked when a contact is selected from the contact search list
         * @param contactLookupKey The contact lookup key
         * @param recipient The name of the recipient that has been selected by the user
         */
        void onRecipientSelected(String contactLookupKey, String recipient);
    }

    /**
     * An callback interface which is invoked when the scroll state of the contact search list
     * changes.
     */
    public interface OnScrollStateChangedListener {

        /**
         * The ContactSearchFragment is not currently scrolling.
         */
        int SCROLL_STATE_IDLE = RecyclerView.SCROLL_STATE_IDLE;

        /**
         * The ContactSearchFragment is currently being dragged by outside input such as user touch input.
         */
        int SCROLL_STATE_DRAGGING = RecyclerView.SCROLL_STATE_DRAGGING;

        /**
         * The ContactSearchFragment is currently animating to a final position while not under
         * outside control.
         */
        int SCROLL_STATE_SETTLING = RecyclerView.SCROLL_STATE_SETTLING;

        /**
         * Invoked when the scroll satate of the contact search list changes
         * @param newState The new scroll state of the contact search lsit
         */
        void onStrollStateChanged(int newState);
    }

}
