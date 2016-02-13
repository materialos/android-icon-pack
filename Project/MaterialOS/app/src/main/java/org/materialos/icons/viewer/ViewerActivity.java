package org.materialos.icons.viewer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.afollestad.assent.AssentActivity;
import com.afollestad.polar.R;
import org.materialos.icons.fragments.WallpapersFragment;
import org.materialos.icons.util.Utils;
import org.materialos.icons.util.WallpaperUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

import static org.materialos.icons.fragments.WallpapersFragment.RQ_CROPANDSETWALLPAPER;

/**
 * @author Aidan Follestad (afollestad)
 */
@SuppressLint("MissingSuperCall")
public class ViewerActivity extends AssentActivity {

    private WallpaperUtils.WallpapersHolder mWallpapers;
    @SuppressWarnings("FieldCanBeLocal")
    private ViewerPageAdapter mAdapter;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.statusbarPlaceholder)
    FrameLayout mStatusbarPlaceholder;

    public static final String STATE_CURRENT_POSITION = "state_current_position";
    private int mCurrentPosition;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_POSITION, mCurrentPosition);
    }

    public int getNavigationBarHeight(boolean portraitOnly, boolean landscapeOnly) {
        final Configuration config = getResources().getConfiguration();
        if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            // Cancel out for tablets~
            return 0;
        }

        final Resources r = getResources();
        int id;
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (portraitOnly) return 0;
            id = r.getIdentifier("navigation_bar_height_landscape", "dimen", "android");
        } else {
            if (landscapeOnly) return 0;
            id = r.getIdentifier("navigation_bar_height", "dimen", "android");
        }
        if (id > 0)
            return r.getDimensionPixelSize(id);
        return 0;
    }

    @SuppressLint("PrivateResource")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // TODO replace with insets?
            final int statusBarHeight = Utils.getStatusBarHeight(this);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mToolbar.getLayoutParams();
            lp.topMargin = statusBarHeight;
            mToolbar.setLayoutParams(lp);
            lp = (FrameLayout.LayoutParams) mStatusbarPlaceholder.getLayoutParams();
            lp.height = statusBarHeight;
            mStatusbarPlaceholder.setLayoutParams(lp);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mStatusbarPlaceholder.setBackgroundColor(Color.TRANSPARENT);
        } else {
            mStatusbarPlaceholder.setVisibility(View.GONE);
        }

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getExtras() != null) {
                mCurrentPosition = getIntent().getExtras().getInt(STATE_CURRENT_POSITION);
            }
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_POSITION);
        }

        if (getIntent() != null) {
            mWallpapers = (WallpaperUtils.WallpapersHolder) getIntent().getSerializableExtra("wallpapers");
        }

        mAdapter = new ViewerPageAdapter(this, mCurrentPosition, mWallpapers);
        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(mAdapter);
        pager.setCurrentItem(mCurrentPosition);

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

        // Prevents nav bar from overlapping toolbar options in landscape
        mToolbar.setPadding(
                mToolbar.getPaddingLeft(),
                mToolbar.getPaddingTop(),
                getNavigationBarHeight(false, true),
                mToolbar.getPaddingBottom()
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
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