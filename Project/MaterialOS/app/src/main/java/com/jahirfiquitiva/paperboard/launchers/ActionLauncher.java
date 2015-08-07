package com.jahirfiquitiva.paperboard.launchers;

import android.content.Context;
import android.content.Intent;

public class ActionLauncher {
    public ActionLauncher(Context context) {
        Intent action = context.getPackageManager().getLaunchIntentForPackage("com.actionlauncher.playstore");
        action.putExtra("apply_icon_pack", context.getPackageName());
        context.startActivity(action);
    }
}
