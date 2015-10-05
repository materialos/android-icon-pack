package org.materialos.icons.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.materialos.icons.R;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ApplyFragment extends Fragment {

    private static final String MARKET_URL = "https://play.google.com/store/apps/details?id=";
    private List<Launcher> mLaunchers;
    private String mIntentString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_apply, container, false);
        mLaunchers = new ArrayList<>();

        // Splits all launcher arrays by the | delimiter {name}|{package}
        String[] launcherArray = getResources().getStringArray(R.array.launchers);
        for (String launcher : launcherArray)
            mLaunchers.add(new Launcher(launcher.split("\\|")));

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(R.string.apply);

        ListView launchersList = (ListView) root.findViewById(R.id.launcherslist);

        LaunchersAdapter adapter = new LaunchersAdapter(mLaunchers);
        launchersList.setAdapter(adapter);
        launchersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO: Hardcoded GNL Launcher name
                if (mLaunchers.get(position).name.equals("Google Now Launcher"))
                    gnlDialog();
                else if (launcherIsInstalled(mLaunchers.get(position).packageName))
                    openLauncher(mLaunchers.get(position).name);
                else
                    openInPlayStore(mLaunchers.get(position));
            }
        });

        return root;
    }

    private boolean launcherIsInstalled(String packageName) {
        final PackageManager pm = getActivity().getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }


    private void openLauncher(String name) {
        //TODO: Sketchy code using reflection and whatnot
        final String className = "com.jahirfiquitiva.paperboard" + ".launchers."
                + Character.toUpperCase(name.charAt(0))
                + name.substring(1).toLowerCase().replace(" ", "").replace("launcher", "")
                + "Launcher";

        Class<?> cl = null;
        try {
            cl = Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.e("LAUNCHER CLASS MISSING", "Launcher class for: '" + name + "' missing!");
        }
        if (cl != null) {
            Constructor<?> constructor = null;
            try {
                constructor = cl.getConstructor(Context.class);
            } catch (NoSuchMethodException e) {
                Log.e("LAUNCHER CLASS CONS",
                        "Launcher class for: '" + name + "' is missing a constructor!");
            }
            try {
                if (constructor != null)
                    constructor.newInstance(getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openInPlayStore(final Launcher launcher) {
        mIntentString = MARKET_URL + launcher.packageName;
        final String LauncherName = launcher.name;
        final String cmName = "CM Theme Engine";
        String dialogContent;

        if (LauncherName.equals(cmName)) {
            dialogContent = launcher.name + getResources().getString(R.string.cm_dialog_content);
            mIntentString = "http://download.cyanogenmod.org/";
        } else {
            dialogContent = launcher.name + getResources().getString(R.string.lni_content);
            mIntentString = MARKET_URL + launcher.packageName;
        }

        new MaterialDialog.Builder(getActivity())
                .title(launcher.name + getResources().getString(R.string.lni_title))
                .content(dialogContent)
                .positiveText(R.string.lni_yes)
                .negativeText(R.string.lni_no)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(mIntentString));
                        startActivity(intent);
                    }
                }).show();
    }

    private void gnlDialog() {
        final String appLink = MARKET_URL + getResources().getString(R.string.extraapp);
        new MaterialDialog.Builder(getActivity())
                .title(R.string.gnl_title)
                .content(R.string.gnl_content)
                .positiveText(R.string.lni_yes)
                .negativeText(R.string.lni_no)
                .callback(new MaterialDialog.ButtonCallback() {
                              @Override
                              public void onPositive(MaterialDialog dialog) {
                                  super.onPositive(dialog);
                                  Intent intent = new Intent(Intent.ACTION_VIEW);
                                  intent.setData(Uri.parse(appLink));
                                  startActivity(intent);
                              }
                          }
                ).show();
    }

    public class Launcher {

        public final String name;
        public final String packageName;

        public Launcher(String[] values) {
            name = values[0];
            packageName = values[1];
        }
    }

    private class LaunchersAdapter extends ArrayAdapter<Launcher> {

        private final List<Launcher> mLaunchers;

        private LaunchersAdapter(List<Launcher> launchers) {
            super(getActivity(), R.layout.list_item_launcher, R.id.launchername, launchers);
            this.mLaunchers = launchers;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View item = convertView;
            LauncherHolder holder;

            if (item == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                item = inflater.inflate(R.layout.list_item_launcher, parent, false);
                holder = new LauncherHolder(item);
                item.setTag(holder);
            } else {
                holder = (LauncherHolder) item.getTag();

            }
            // Turns Launcher name "Something Pro" to "ic_something_pro"
            int iconResource = getActivity().getResources().getIdentifier(
                    "ic_" + mLaunchers.get(position).name.toLowerCase().replace(" ", "_"),
                    "drawable",
                    getActivity().getPackageName()
            );

            holder.icon.setImageResource(iconResource);
            holder.launchername.setText(mLaunchers.get(position).name);

            if (launcherIsInstalled(mLaunchers.get(position).packageName)) {
                holder.isInstalled.setText(R.string.installed);
                holder.isInstalled.setTextColor(ContextCompat.getColor(getActivity(), R.color.launcher_installed_text));
            } else {
                holder.isInstalled.setText(R.string.noninstalled);
                holder.isInstalled.setTextColor(ContextCompat.getColor(getActivity(), R.color.launcher_not_installed_text));
            }

            return item;
        }

        class LauncherHolder {

            final ImageView icon;
            final TextView launchername;
            final TextView isInstalled;

            LauncherHolder(View v) {
                icon = (ImageView) v.findViewById(R.id.launchericon);
                launchername = (TextView) v.findViewById(R.id.launchername);
                isInstalled = (TextView) v.findViewById(R.id.launcherinstalled);
            }
        }
    }
}
