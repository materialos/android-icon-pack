package com.jahirfiquitiva.paperboard.launchers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class TsfLauncher {
    public TsfLauncher(Context context) {
        Intent tsfApply = context.getPackageManager().getLaunchIntentForPackage("com.tsf.shell");
        Intent tsf = new Intent("android.intent.action.MAIN");
        tsf.setComponent(new ComponentName("com.tsf.shell", "com.tsf.shell.ShellActivity"));
        context.sendBroadcast(tsf);
        context.startActivity(tsfApply);
    }
}
