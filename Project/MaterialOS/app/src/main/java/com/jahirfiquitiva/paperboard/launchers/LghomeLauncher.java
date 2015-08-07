package com.jahirfiquitiva.paperboard.launchers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class LghomeLauncher {
    public LghomeLauncher(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.lge.launcher2", "com.lge.launcher2.homesettings.HomeSettingsPrefActivity"));
        context.startActivity(intent);
    }
}
