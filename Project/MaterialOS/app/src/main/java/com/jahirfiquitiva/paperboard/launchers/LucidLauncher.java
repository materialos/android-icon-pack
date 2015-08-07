package com.jahirfiquitiva.paperboard.launchers;

import android.content.Context;
import android.content.Intent;

public class LucidLauncher {
    public LucidLauncher(Context context) {
        Intent lucidApply = new Intent("com.powerpoint45.action.APPLY_THEME", null);
        lucidApply.putExtra("icontheme", context.getPackageName());
        context.startActivity(lucidApply);
    }
}
