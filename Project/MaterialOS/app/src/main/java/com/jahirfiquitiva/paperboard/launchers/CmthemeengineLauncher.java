package com.jahirfiquitiva.paperboard.launchers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class CmthemeengineLauncher {
    public CmthemeengineLauncher(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName("org.cyanogenmod.theme.chooser", "org.cyanogenmod.theme.chooser.ChooserActivity"));
        intent.putExtra("pkgName", context.getPackageName());
        context.startActivity(intent);
    }
}
