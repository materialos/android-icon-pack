package com.jahirfiquitiva.paperboard.launchers;

import android.content.Context;
import android.content.Intent;

public class InspireLauncher {
    public InspireLauncher(Context context) {
        Intent inspireMain = context.getPackageManager().getLaunchIntentForPackage("com.bam.android.inspirelauncher");
        Intent inspire = new Intent("com.bam.android.inspirelauncher.action.ACTION_SET_THEME");
        inspire.putExtra("icon_pack_name", context.getPackageName());
        context.sendBroadcast(inspire);
        context.startActivity(inspireMain);
    }
}
