package com.afollestad.polar.viewer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.assent.AssentActivity;
import com.afollestad.polar.R;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.fragments.WallpapersFragment;
import com.afollestad.polar.util.GravityArcMotion;
import com.afollestad.polar.util.WallpaperUtils;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.afollestad.polar.fragments.WallpapersFragment.RQ_CROPANDSETWALLPAPER;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerActivity extends AssentActivity {

    public static final String EXTRA_WIDTH = "com.afollestad.impression.Width";
    public static final String EXTRA_HEIGHT = "com.afollestad.impression.Height";
    public static final String STATE_CURRENT_POSITION = "state_current_position";
    public static final long SHARED_ELEMENT_TRANSITION_DURATION = 300;

    @BindView(R.id.app_bar)
    View appBar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    private WallpaperUtils.WallpapersHolder mWallpapers;
    private ViewerPageAdapter mAdapter;
    private int mCurrentPosition;
    private boolean isReturning;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_POSITION, mCurrentPosition);
    }

    private void setTransition() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        final TransitionSet transition = new TransitionSet();

        ChangeBounds transition1 = new ChangeBounds();
        transition.addTransition(transition1);
        ChangeTransform transition2 = new ChangeTransform();
        transition.addTransition(transition2);
        ChangeClipBounds transition3 = new ChangeClipBounds();
        transition.addTransition(transition3);
        ChangeImageTransform transition4 = new ChangeImageTransform();
        transition.addTransition(transition4);

        transition.setDuration(SHARED_ELEMENT_TRANSITION_DURATION);

        FastOutSlowInInterpolator interpolator = new FastOutSlowInInterpolator();
        transition1.setInterpolator(interpolator);
        transition2.setInterpolator(interpolator);
        transition3.setInterpolator(interpolator);
        transition4.setInterpolator(interpolator);

        final GravityArcMotion pathMotion = new GravityArcMotion();
        transition.setPathMotion(pathMotion);

        getWindow().setSharedElementEnterTransition(transition);
        getWindow().setSharedElementReturnTransition(transition);
        getWindow().setSharedElementsUseOverlay(false);

        Slide slide = new Slide(Gravity.TOP);
        slide.setInterpolator(new LinearOutSlowInInterpolator());
        slide.addTarget(appBar);
        slide.setDuration(225);
        slide.setStartDelay(100);
        getWindow().setEnterTransition(slide);
        Slide slideOut = (Slide) slide.clone();
        slideOut.setInterpolator(new FastOutLinearInInterpolator());
        slideOut.setStartDelay(0);
        getWindow().setReturnTransition(slideOut);
    }

    @SuppressLint("PrivateResource")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationIcon(R.drawable.ic_action_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getExtras() != null) {
                mCurrentPosition = getIntent().getExtras().getInt(STATE_CURRENT_POSITION);
            }
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_POSITION);
        }

        setResult(RESULT_OK, getIntent().putExtra(STATE_CURRENT_POSITION, mCurrentPosition));

        if (getIntent() != null) {
            mWallpapers = (WallpaperUtils.WallpapersHolder) getIntent().getSerializableExtra("wallpapers");
        }

        mAdapter = new ViewerPageAdapter(this, mCurrentPosition, mWallpapers,
                getIntent().getIntExtra(EXTRA_WIDTH, -1),
                getIntent().getIntExtra(EXTRA_HEIGHT, -1));
        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(mAdapter);
        pager.setCurrentItem(mCurrentPosition);

        supportPostponeEnterTransition();
        setTransition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterSharedElementCallback(new SharedElementCallback() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if (isReturning) {
                        ViewerPageFragment active = (ViewerPageFragment) getFragmentManager().findFragmentByTag("page:" + mCurrentPosition);
                        ImageView sharedElement = active.getSharedElement();

                        names.clear();
                        names.add(sharedElement.getTransitionName());
                        sharedElements.clear();
                        sharedElements.put(sharedElement.getTransitionName(), sharedElement);
                    }
                }
            });
        }

        // When the view pager is swiped, fragments are notified if they're active or not
        // And the menu updates based on the color mode (light or dark).
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            int previousState;
            boolean userScrollChange;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ViewerPageFragment noActive = (ViewerPageFragment) getFragmentManager().findFragmentByTag("page:" + mCurrentPosition);
                if (noActive != null)
                    noActive.setIsActive(false);
                mCurrentPosition = position;
                ViewerPageFragment active = (ViewerPageFragment) getFragmentManager().findFragmentByTag("page:" + mCurrentPosition);
                if (active != null) {
                    active.setIsActive(true);
                }
                mAdapter.mCurrentPage = position;
                setResult(RESULT_OK, getIntent().putExtra(STATE_CURRENT_POSITION, position));
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (previousState == ViewPager.SCROLL_STATE_DRAGGING
                        && state == ViewPager.SCROLL_STATE_SETTLING)
                    userScrollChange = true;
                else if (previousState == ViewPager.SCROLL_STATE_SETTLING
                        && state == ViewPager.SCROLL_STATE_IDLE)
                    userScrollChange = false;

                previousState = state;
            }
        });
    }

    @Override
    public void finishAfterTransition() {
        isReturning = true;
        super.finishAfterTransition();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RQ_CROPANDSETWALLPAPER) {
            WallpapersFragment.showToast(this, R.string.wallpaper_set);
            WallpaperUtils.resetOptionCache(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewer, menu);
        menu.findItem(R.id.save).setVisible(Config.get().wallpapersAllowDownload());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ViewerPageFragment active = (ViewerPageFragment) getFragmentManager().findFragmentByTag("page:" + mCurrentPosition);
        if (active != null) {
            active.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mToolbar.animate().cancel();
    }
}