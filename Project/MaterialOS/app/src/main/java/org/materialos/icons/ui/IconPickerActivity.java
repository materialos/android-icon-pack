package org.materialos.icons.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;
import org.materialos.icons.fragments.IconsFragment;
import org.materialos.icons.ui.base.BaseThemedActivity;
import org.materialos.icons.util.TintUtils;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * @author Aidan Follestad (afollestad)
 */
public class IconPickerActivity extends BaseThemedActivity {

    @Bind(R.id.toolbar)
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

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);

        if (toolbar.getNavigationIcon() != null) {
            toolbar.setNavigationIcon(TintUtils.createTintedDrawable(toolbar.getNavigationIcon(),
                    DialogUtils.resolveColor(this, R.attr.tab_icon_color)));
        }

        getFragmentManager().beginTransaction().replace(R.id.container, new IconsFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}