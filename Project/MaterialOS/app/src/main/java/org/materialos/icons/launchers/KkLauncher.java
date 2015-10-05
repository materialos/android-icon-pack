package org.materialos.icons.launchers;

import android.content.Context;
import android.content.Intent;

import org.materialos.icons.R;

public class KkLauncher {
    public KkLauncher(Context context) {
        Intent kkApply = new Intent("com.kk.launcher.APPLY_ICON_THEME");
        kkApply.putExtra("com.kk.launcher.theme.EXTRA_PKG", context.getPackageName());
        kkApply.putExtra("com.kk.launcher.theme.EXTRA_NAME", context.getResources().getString(R.string.app_name));
        context.startActivity(kkApply);
    }

}
