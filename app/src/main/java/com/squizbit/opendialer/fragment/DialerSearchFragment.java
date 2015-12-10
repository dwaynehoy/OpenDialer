package com.squizbit.opendialer.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.squizbit.opendialer.R;
import com.squizbit.opendialer.data.ContactSearchAdapter;
import com.squizbit.opendialer.models.DialerHelper;
import com.squizbit.opendialer.library.FabControllable;
import com.squizbit.opendialer.library.FabControllerOwner;
import com.squizbit.opendialer.models.Preferences;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A fragment which contains a dialer and provides a list of items that match the dialed numbers.
 * The user can select an item for the list to dial
 */
public class DialerSearchFragment extends Fragment implements FabControllable, ContactSearchAdapter.OnContactSelectedListener, ContactSearchFragment.OnScrollStateChangedListener {

    private static final String DIALER_FRAGMENT = "com.strone.stronedialer.DIALER_FRAGMENT";
    private static final String CONTACT_SEARCH_FRAGMENT = "com.strone.stronedialer.CONTACT_SEARCH_FRAGMENT";
    private static final long DIALER_ANIMATION_DURATION = 300;
    private static final long FADE_IN_DURATION = 300;

    public static final int SEARCH_MODE_DIALER = 0;
    public static final int SEARCH_MODE_INPUT = 1;

    //region View fields
    @InjectView(R.id.relativeLayoutSearchView)
    RelativeLayout mSearchRoot;

    @InjectView(R.id.search_container)
    FrameLayout mSearchContainer;

    @InjectView(R.id.dialer_container)
    LinearLayout mDialerContainer;
    //endregion

    private FabControllerOwner mOwner;
    private int mSearchMode;
    private boolean mIsDialerVisible;
    private DialerFragment mDialerFragment;
    private ContactSearchFragment mContactSearchFragment;
    private DialerHelper mDialerHelper;

    private DialerFragment.OnNumberChangedListener mOnNumberChangedListener = new DialerFragment.OnNumberChangedListener() {
        @Override
        public void onNumberChangeListener(String number) {
            mContactSearchFragment.setQuery(mDialerFragment.getNumber());
        }
    };

    private ContactSearchAdapter.OnContactSelectedListener mOnContactSelectedListener = new ContactSearchAdapter.OnContactSelectedListener() {
        @Override
        public void onContactSelected(Cursor contact) {
            String number = !contact.isNull(3) ? contact.getString(3) : contact.getString(4);
            mDialerFragment.setNumber(number);
            dialNumberAndClose(number);
        }
    };

    private void dialNumberAndClose(String number) {

        Preferences preferences = new Preferences(getActivity());

        if (number.isEmpty()) {
            mDialerFragment.setNumber(preferences.getLastDialedNumber());
        } else {
            preferences.setLastDialedNumber(number);
            mDialerHelper.dialNumber(number);

            mOwner.onFabControllableClosing(DialerSearchFragment.this);
            closeFragment();
        }
    }

    /**
     * Creates a new instance of the DialerSearchFragment
     *
     * @param mode A flag indicating the current mode of the DialerSearchFragment. This can be any
     *             of the following values
     *             <ul>
     *             <li>{@link #SEARCH_MODE_DIALER} - This displays the dialer on initial load</li>
     *             <li>{@link #SEARCH_MODE_INPUT} - This displays the text input search field on initial load</li>
     *             </ul>
     * @return A new instance of the DialerSearchFragment
     */
    public static Fragment newInstance(int mode) {
        Bundle args = new Bundle();
        args.putInt("mode", mode);

        Fragment fragment = new DialerSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialer_overlap_view, container, false);
        ButterKnife.inject(this, view);
        mSearchMode = getArguments().getInt("mode", SEARCH_MODE_DIALER);

        if (savedInstanceState == null) {
            initFragments();
        } else {
            restoreFragments();
            mIsDialerVisible = mDialerContainer.getTranslationY() == 0;
        }
        mContactSearchFragment.setOnContactSelectedListener(mOnContactSelectedListener);
        mDialerFragment.setOnNumberChangedListener(mOnNumberChangedListener);

        mSearchRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mOwner.onFabStatusChanged(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mDialerHelper = new DialerHelper(getActivity());

        super.onActivityCreated(savedInstanceState);
    }

    private void initFragments() {
        mDialerFragment = (DialerFragment) DialerFragment.newInstance();
        mContactSearchFragment = (ContactSearchFragment) ContactSearchFragment.newInstance(
                getString(R.string.make_call_to_label), true, mSearchMode == SEARCH_MODE_DIALER);
        mContactSearchFragment.setOnContactSelectedListener(this);
        mContactSearchFragment.setOnScrollStateChangedListener(this);

        getChildFragmentManager()
                .beginTransaction()
                .add(mSearchContainer.getId(), mContactSearchFragment, CONTACT_SEARCH_FRAGMENT)
                .commit();

        getChildFragmentManager()
                .beginTransaction()
                .add(mDialerContainer.getId(), mDialerFragment, DIALER_FRAGMENT)
                .commit();

        ObjectAnimator.ofFloat(
                mSearchContainer,
                "alpha",
                mSearchContainer.getAlpha(),
                1).setDuration(FADE_IN_DURATION)
                .start();

        mDialerContainer.setTranslationY(getResources().getDimensionPixelOffset(R.dimen.dialer_height));
    }

    private void restoreFragments() {
        mDialerFragment = (DialerFragment) getChildFragmentManager().findFragmentByTag(DIALER_FRAGMENT);
        mContactSearchFragment = (ContactSearchFragment) getChildFragmentManager().findFragmentByTag(CONTACT_SEARCH_FRAGMENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mDialerHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Passing this on to any child fragments that may be interested
        List<Fragment> fragmentList = getChildFragmentManager().getFragments();
        for(int i = 0; i < fragmentList.size(); i++){
            fragmentList.get(i).onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void closeFragment() {
        mIsDialerVisible = false;

        AnimatorSet closeSet = new AnimatorSet();

        AnimatorSet.Builder animationBuilder = closeSet.play(
                ObjectAnimator.ofFloat(
                        mSearchContainer,
                        "alpha",
                        mSearchContainer.getAlpha(),
                        0).setDuration(FADE_IN_DURATION));

        Fragment fragment = getChildFragmentManager().findFragmentByTag(DIALER_FRAGMENT);

        if (fragment != null) {
            animationBuilder.with(
                    ObjectAnimator.ofFloat(
                            fragment.getView(), "translationY",
                            fragment.getView().getTranslationY(),
                            fragment.getView().getHeight())
                            .setDuration(DIALER_ANIMATION_DURATION));
        }

        closeSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //Do nothing
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mOwner.onFabControllableClose(DialerSearchFragment.this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mOwner.onFabControllableClose(DialerSearchFragment.this);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //Do nothing
            }
        });

        closeSet.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mSearchMode == SEARCH_MODE_DIALER) {
            showDialer();
        }

        mDialerFragment.setNumber(getArguments().getString("number", ""));
    }

    private void showDialer() {
        ContactSearchFragment contactSearchFragment =
                (ContactSearchFragment) getChildFragmentManager().findFragmentByTag(CONTACT_SEARCH_FRAGMENT);
        if (contactSearchFragment != null) {
            contactSearchFragment.hideSearchBar();
        }

        mIsDialerVisible = true;
        mOwner.onFabStatusChanged(this);

        ObjectAnimator.ofFloat(
                mDialerContainer, "translationY",
                mDialerContainer.getTranslationY(),
                0)
                .setDuration(DIALER_ANIMATION_DURATION)
                .start();
    }

    private void hideDialer() {
        ContactSearchFragment contactSearchFragment =
                (ContactSearchFragment) getChildFragmentManager().findFragmentByTag(CONTACT_SEARCH_FRAGMENT);
        if (contactSearchFragment != null) {
            contactSearchFragment.showSearchBar();
        }

        mIsDialerVisible = false;
        mOwner.onFabStatusChanged(this);

        ObjectAnimator animator = ObjectAnimator.ofFloat(
                mDialerContainer, "translationY",
                mDialerContainer.getTranslationY(),
                mDialerContainer.getHeight())
                .setDuration(DIALER_ANIMATION_DURATION);
        animator.start();
    }


    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public boolean onFabAction() {
        if (!mIsDialerVisible) {
            showDialer();
        } else {
            dialNumberAndClose(mDialerFragment.getNumber());
        }

        return false;
    }

    @Override
    public int getFabResource() {
        if (mIsDialerVisible) {
            return R.drawable.ic_accept;
        } else {
            return R.drawable.ic_dialpad;
        }
    }

    @Override
    public int getFabPosition() {
        if (mIsDialerVisible) {
            return FAB_CENTER;
        } else {
            return FAB_LEFT;
        }
    }

    @Override
    public int getFabColor() {
        if (mIsDialerVisible) {
            return getContext().getResources().getColor(R.color.bg_accept_call);
        } else {
            return getContext().getResources().getColor(R.color.ab_primary);
        }
    }

    @Override
    public boolean onBackPress() {
        if (mSearchMode == 1 && mIsDialerVisible) {
            hideDialer();
        } else {
            closeFragment();
            mOwner.onFabControllableClosing(this);
        }

        return true;
    }

    @Override
    public void setFabControllableOwner(FabControllerOwner owner) {
        mOwner = owner;
    }

    @Override
    public void onContactSelected(Cursor contact) {
        //Make a call
    }

    @Override
    public void onStrollStateChanged(int newState) {
        if (newState == SCROLL_STATE_DRAGGING) {
            hideDialer();
        }
    }

}
