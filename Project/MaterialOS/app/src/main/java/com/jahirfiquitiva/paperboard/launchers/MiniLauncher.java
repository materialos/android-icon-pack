package com.jahirfiquitiva.paperboard.launchers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class MiniLauncher {
    public MiniLauncher(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.jiubang.go.mini.launcher", "com.jiubang.go.mini.launcher.setting.MiniLauncherSettingActivity"));
        context.startActivity(intent);
    }
}
