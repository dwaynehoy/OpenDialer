package com.squizbit.opendialer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.squizbit.opendialer.fragment.CallLogFragment;
import com.squizbit.opendialer.fragment.ContactsFragment;
import com.squizbit.opendialer.fragment.DialerSearchFragment;
import com.squizbit.opendialer.fragment.FavoritesFragment;
import com.squizbit.opendialer.library.FabControllable;
import com.squizbit.opendialer.library.FabControllerOwner;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * The core activity which handles the navigation between child screens and there transitions.
 */

public class NavActivity extends AppCompatActivity implements FabControllerOwner {

    private static final String DIALER_SEARCH_FRAGMENT = "co.strone.stronedialer.navactivity.dialer_search_fragment";

    //region View fields
    @InjectView(R.id.navViewPager)
    ViewPager mNavViewPager;

    @InjectView(R.id.toolbarMain)
    Toolbar mToolbarMain;

    @InjectView(R.id.primary_container)
    FrameLayout mPrimaryContainer;

    @InjectView(R.id.floatingActionBarNavMain)
    FloatingActionButton mFloatingActionButton;

    @InjectView(R.id.nav_tab_layout)
    TabLayout mNavTabLayout;

    @InjectView(R.id.view_shadow)
    View mViewShadow;
    //endregion

    private FabControllable mFabControllable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_view);
        ButterKnife.inject(this);
        setSupportActionBar(mToolbarMain);

        NavFragmentAdapter fragmentAdapter = new NavFragmentAdapter(getSupportFragmentManager());
        mNavViewPager.setOffscreenPageLimit(3);
        mNavViewPager.setAdapter(fragmentAdapter);
        mNavTabLayout.setupWithViewPager(mNavViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search_contacts) {
            switchToSearch();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        //This can sometimes happen if the activity has been backgrounded too long with the dialer window
        //open
        if (fragment instanceof FabControllable) {
            mFabControllable = (FabControllable) fragment;
            mFabControllable.setFabControllableOwner(this);
        }

        super.onAttachFragment(fragment);
    }


    @Override
    public void onFabStatusChanged(FabControllable fabControllable) {
        animateFABPosition(fabControllable.getFabPosition(), fabControllable.getFabResource(), fabControllable.getFabColor());
    }

    @Override
    public void onFabControllableClosing(FabControllable fabControllable) {
        showActionBar();
        animateFABPosition(FabControllable.FAB_RIGHT, R.drawable.ic_dialpad, getResources().getColor(R.color.ab_primary));
    }

    @Override
    public void onFabControllableClose(FabControllable fabControllable) {
        if (fabControllable.equals(mFabControllable) && fabControllable instanceof Fragment) {
            getSupportFragmentManager().beginTransaction()
                    .remove((Fragment) fabControllable)
                    .commitAllowingStateLoss();

            mFabControllable = null;
        }
    }

    @Override
    public void onBackPressed() {
        boolean doPropagateBackPress = true;

        if (mFabControllable != null) {
            doPropagateBackPress = !mFabControllable.onBackPress();
        }

        if (doPropagateBackPress) {
            super.onBackPressed();
        }
    }

    private void switchToDialer() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(mPrimaryContainer.getId(), DialerSearchFragment.newInstance(DialerSearchFragment.SEARCH_MODE_DIALER), DIALER_SEARCH_FRAGMENT)
                .commit();
        hideActionBar();
    }

    private void switchToSearch() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(mPrimaryContainer.getId(), DialerSearchFragment.newInstance(DialerSearchFragment.SEARCH_MODE_INPUT), DIALER_SEARCH_FRAGMENT)
                .commit();
        hideActionBar();
    }

    @OnClick(R.id.floatingActionBarNavMain)
    public void onFabClicked() {
        if (mFabControllable == null) {
            switchToDialer();
        } else {
            mFabControllable.onFabAction();
        }
    }

    //Suppressing lint for FloatingActionButton.setBackgroundTintList which has compatibility handling
    //for this call.
    @SuppressLint("NewApi")
    private void animateFABPosition(@FabControllable.FabPosition int fabPosition, @DrawableRes final int fabResource, @ColorInt final int fabColor) {
        int translationX;

        if (fabPosition == FabControllable.FAB_CENTER) {
            int screenCenter= mPrimaryContainer.getWidth() / 2;
            int buttonCenter = mFloatingActionButton.getWidth() / 2;
            int buttonOffset = mPrimaryContainer.getWidth() - mFloatingActionButton.getRight();
            //Moving left
            int movementDelta = buttonCenter - screenCenter;

            translationX = movementDelta + buttonOffset;
        } else {
            translationX = 0;
        }

        //Don't animate if we don't need it
        if (translationX == mFloatingActionButton.getTranslationX()) {
            mFloatingActionButton.setImageResource(fabResource);
            mFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(fabColor));

            return;
        }

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(mFloatingActionButton, "alpha", 1, 0).setDuration(150);
        fadeOutAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //Do nothing
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFloatingActionButton.setImageResource(fabResource);
                mFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(fabColor));
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                //Do nothing
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //Do nothing
            }
        });

        ObjectAnimator fadeInAnim = ObjectAnimator.ofFloat(mFloatingActionButton, "alpha", 0, 1).setDuration(1);
        ObjectAnimator slideAnim = ObjectAnimator.ofFloat(mFloatingActionButton, "translationX", mFloatingActionButton.getTranslationX(), translationX).setDuration(300);
        animatorSet.play(fadeOutAnim).before(slideAnim);
        animatorSet.play(fadeInAnim).after(slideAnim);
        animatorSet.start();
    }


    private void hideActionBar() {
        ObjectAnimator.ofFloat(mToolbarMain, "translationY", 0, -mToolbarMain.getHeight())
                .start();

        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator toolbarAnimation = ObjectAnimator.ofFloat(mToolbarMain, "translationY", 0, -mToolbarMain.getHeight());
        ObjectAnimator tabbarAnimation = ObjectAnimator.ofFloat(mNavTabLayout, "translationY", 0, -mToolbarMain.getHeight() - 5);
        ObjectAnimator shadowAnimation = ObjectAnimator.ofFloat(mViewShadow, "translationY", 0, -mToolbarMain.getHeight());

        animatorSet.play(toolbarAnimation).with(tabbarAnimation).with(shadowAnimation);
        animatorSet.start();
    }

    private void showActionBar() {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator toolbarAnimation = ObjectAnimator.ofFloat(mToolbarMain, "translationY", -mToolbarMain.getHeight(), 0);
        ObjectAnimator tabbarAnimation = ObjectAnimator.ofFloat(mNavTabLayout, "translationY", -mToolbarMain.getHeight() - 5, 0);
        ObjectAnimator shadowAnimation = ObjectAnimator.ofFloat(mViewShadow, "translationY", -mToolbarMain.getHeight(), 0);

        animatorSet.play(toolbarAnimation).with(tabbarAnimation).with(shadowAnimation);
        animatorSet.start();

    }

    /**
     * A fragment adapter which manages the different core app pages.
     */
    private class NavFragmentAdapter extends FragmentStatePagerAdapter {

        public NavFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FavoritesFragment();
                case 1:
                    return new ContactsFragment();
                case 2:
                    return new CallLogFragment();
            }
            return new ContactsFragment();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.favorites_page_title);
                case 1:
                    return getString(R.string.contacts_page_title);
                case 2:
                    return getString(R.string.call_log_page_title);
                default:
                    return "";
            }
        }
    }

}
