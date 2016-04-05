package org.materialos.icons.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.afollestad.materialdialogs.util.DialogUtils;
import org.materialos.icons.R;
import org.materialos.icons.adapters.IconMoreAdapter;
import org.materialos.icons.config.Config;
import org.materialos.icons.fragments.IconsFragment;
import org.materialos.icons.transitions.CircularRevealTransition;
import org.materialos.icons.ui.base.BaseThemedActivity;
import org.materialos.icons.ui.base.ISelectionMode;
import org.materialos.icons.util.DrawableXmlParser;
import org.materialos.icons.util.TintUtils;
import org.materialos.icons.util.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Aidan Follestad (afollestad)
 */
public class IconMoreActivity extends BaseThemedActivity
        implements IconMoreAdapter.ClickListener, ISelectionMode {

    public static final String EXTRA_REVEAL_ANIM_LOCATION = "org.materialos.icons.REVEAL_ANIM_LOCATION";
    public static final String EXTRA_CATEGORY = "org.materialos.icons.CATEGORY";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(android.R.id.list)
    RecyclerView mRecyclerView;

    private IconMoreAdapter mAdapter;

    @Override
    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public int getLastStatusBarInsetHeight() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return 0;
        }
        return findViewById(R.id.root).getPaddingTop();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icons_more);
        ButterKnife.bind(this);

        final DrawableXmlParser.Category category = (DrawableXmlParser.Category) getIntent().getSerializableExtra(EXTRA_CATEGORY);

        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(category.getName());

        if (mToolbar.getNavigationIcon() != null) {
            mToolbar.setNavigationIcon(TintUtils.createTintedDrawable(mToolbar.getNavigationIcon(),
                    DialogUtils.resolveColor(this, R.attr.tab_icon_color)));
        }

        final int gridWidth = Config.get().gridWidthIcons();
        mAdapter = new IconMoreAdapter(this, gridWidth, this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridWidth));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setClipToPadding(false);
        mAdapter.set(category.getIcons());

        setUpTransitions();

        supportPostponeEnterTransition();
        Utils.waitForLayout(mRecyclerView, new Utils.LayoutCallback<RecyclerView>() {
            @Override
            public void onLayout(RecyclerView view) {
                //A small delay for RecyclerView to draw images
                //TODO: Use a better method than just waiting
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        supportStartPostponedEnterTransition();
                    }
                }, 50);

            }
        });
    }

    private void setUpTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setSharedElementsUseOverlay(true);

            final float[] buttonLocation = getIntent().getFloatArrayExtra(EXTRA_REVEAL_ANIM_LOCATION);
            final float x = buttonLocation[0];
            final float y = buttonLocation[1];

            CircularRevealTransition circularRevealTransition = new CircularRevealTransition(x, y);
            circularRevealTransition.addTarget(getString(R.string.transition_name_circular_reveal));
            circularRevealTransition.setInterpolator(new FastOutSlowInInterpolator());

            Slide enterSlide = new Slide();
            enterSlide.setDuration(300);
            enterSlide.setStartDelay(400);
            enterSlide.setInterpolator(new FastOutSlowInInterpolator());
            enterSlide.excludeTarget(getString(R.string.transition_name_circular_reveal), true);
            enterSlide.excludeTarget(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME, true);

            Slide returnSide = new Slide();
            returnSide.setDuration(300);
            returnSide.setInterpolator(new FastOutSlowInInterpolator());
            returnSide.excludeTarget(getString(R.string.transition_name_circular_reveal), true);
            returnSide.excludeTarget(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME, true);

            TransitionSet set = new TransitionSet()
                    .addTransition(circularRevealTransition)
                    .addTransition(enterSlide);

            TransitionSet set2 = new TransitionSet()
                    .addTransition(returnSide)
                    .addTransition(circularRevealTransition);

            getWindow().setEnterTransition(set);
            getWindow().setReturnTransition(set2);

            ChangeBounds enterBounds = new ChangeBounds();
            enterBounds.setDuration(300);
            enterBounds.setStartDelay(400);
            enterBounds.setInterpolator(new FastOutSlowInInterpolator());

            ChangeBounds returnBounds = new ChangeBounds();
            returnBounds.setDuration(300);
            returnBounds.setInterpolator(new FastOutSlowInInterpolator());

            getWindow().setSharedElementEnterTransition(enterBounds);
            getWindow().setSharedElementReturnTransition(returnBounds);
        }
    }

    @Override
    protected boolean isTranslucent() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public void onClick(View view, int index) {
        IconsFragment.selectItem(this, null, mAdapter.getIcon(index));
    }

    @Override
    public boolean inSelectionMode() {
        return getIntent().getBooleanExtra("selection_mode", false);
    }
}
