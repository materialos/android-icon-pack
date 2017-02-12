package com.afollestad.polar.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;
import com.afollestad.polar.fragments.IconsFragment;
import com.afollestad.polar.ui.base.BaseThemedActivity;
import com.afollestad.polar.ui.base.ISelectionMode;
import com.afollestad.polar.util.TintUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Aidan Follestad (afollestad)
 */
public class IconPickerActivity extends BaseThemedActivity implements ISelectionMode {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);

        ButterKnife.bind(this);

        toolbar.setTitle(R.string.select_icon);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);

        if (toolbar.getNavigationIcon() != null) {
            toolbar.setNavigationIcon(TintUtils.createTintedDrawable(toolbar.getNavigationIcon(),
                    DialogUtils.resolveColor(this, R.attr.tab_icon_color)));
        }

        getFragmentManager().beginTransaction().replace(R.id.container, IconsFragment.create(true)).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setResult(resultCode, data);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean inSelectionMode() {
        return true;
    }

    @Override
    public boolean allowResourceResult() {
        for (String extra : EXTRAS_PICKER_RESOURCE_MODE) {
            if (getIntent().getBooleanExtra(extra, false)) {
                return true;
            }
        }
        return false;
    }
}