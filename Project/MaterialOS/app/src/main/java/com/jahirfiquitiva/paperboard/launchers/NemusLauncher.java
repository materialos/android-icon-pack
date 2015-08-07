package com.jahirfiquitiva.paperboard.launchers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class NemusLauncher {
    public NemusLauncher(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.nemustech.launcher", "com.nemustech.spareparts.SettingMainActivity"));
        context.startActivity(intent);
    }
}
