package com.jahirfiquitiva.paperboard.launchers;

import android.content.Context;
import android.content.Intent;

public class GoLauncher {
    public GoLauncher(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.gau.go.launcherex");
        Intent go = new Intent("com.gau.go.launcherex.MyThemes.mythemeaction");
        go.putExtra("type", 1);
        go.putExtra("pkgname", context.getPackageName());
        context.sendBroadcast(go);
        context.startActivity(intent);
    }
}
