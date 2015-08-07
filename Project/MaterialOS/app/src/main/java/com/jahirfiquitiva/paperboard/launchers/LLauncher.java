package com.jahirfiquitiva.paperboard.launchers;

import android.content.Context;
import android.content.Intent;

public class LLauncher {
    public LLauncher(Context context) {
        Intent l = new Intent("com.l.launcher.APPLY_ICON_THEME", null);
        l.putExtra("com.l.launcher.theme.EXTRA_PKG", context.getPackageName());
        context.startActivity(l);
    }

}
