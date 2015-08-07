package com.jahirfiquitiva.paperboard.launchers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class HoloLauncher {
    public HoloLauncher(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.mobint.hololauncher", "com.mobint.hololauncher.SettingsActivity"));
        context.startActivity(intent);
    }
}
