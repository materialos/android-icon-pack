package com.jahirfiquitiva.paperboard.launchers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class EpicLauncher {
    public EpicLauncher(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.epic.launcher", "com.epic.launcher.s"));
        context.startActivity(intent);
    }
}
