package org.materialos.icons.launchers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import org.materialos.icons.R;

public class NineLauncher {
    public NineLauncher(Context context) {
        Intent nineApply = context.getPackageManager().getLaunchIntentForPackage("com.gidappsinc.launcher");
        Intent nine = new Intent("com.gridappsinc.launcher.action.THEME");
        try {
            int NineLauncherVersion = context.getPackageManager().getPackageInfo("com.gidappsinc.launcher", 0).versionCode;
            if (NineLauncherVersion >= 12210) {
                nine.putExtra("iconpkg", context.getPackageName());
                nine.putExtra("launch", true);
                context.sendBroadcast(nine);
            } else {
                Toast.makeText(context, R.string.updateninelauncher, Toast.LENGTH_SHORT).show();
            }
            context.startActivity(nineApply);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

}
