package com.jahirfiquitiva.paperboard.launchers;

import android.content.Context;
import android.content.Intent;

public class NextLauncher {
    public NextLauncher(Context context) {
        Intent nextApply = context.getPackageManager().getLaunchIntentForPackage("com.gtp.nextlauncher");
        if (nextApply == null) {
            nextApply = context.getPackageManager().getLaunchIntentForPackage("com.gtp.nextlauncher.trial");
        }
        Intent next = new Intent("com.gau.go.launcherex.MyThemes.mythemeaction");
        next.putExtra("type", 1);
        next.putExtra("pkgname", context.getPackageName());
        context.sendBroadcast(next);
        context.startActivity(nextApply);
    }

}
