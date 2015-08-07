package com.jahirfiquitiva.paperboard.launchers;

import android.content.Context;
import android.content.Intent;

public class AdwexLauncher {
    public AdwexLauncher(Context context) {
        Intent intent = new Intent("org.adwfreak.launcher.SET_THEME");
        intent.putExtra("org.adwfreak.launcher.theme.NAME", context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
