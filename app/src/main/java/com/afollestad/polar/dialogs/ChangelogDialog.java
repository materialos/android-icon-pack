package com.afollestad.polar.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.polar.R;
import com.afollestad.polar.adapters.BulletPointListViewAdapter;


/**
 * @author Aidan Follestad (afollestad)
 */
public class ChangelogDialog extends DialogFragment {

    public static void show(AppCompatActivity context) {
        Fragment frag = context.getSupportFragmentManager().findFragmentByTag("POINTS_EARNED_TUTORIAL");
        if (frag != null)
            ((ChangelogDialog) frag).dismiss();
        new ChangelogDialog().show(context.getSupportFragmentManager(), "POINTS_EARNED_TUTORIAL");
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.changelog)
                .titleGravity(GravityEnum.CENTER)
                .titleColorAttr(R.attr.colorAccent)
                .adapter(new BulletPointListViewAdapter(getActivity(), R.array.changelog), null)
                .positiveText(R.string.cool)
                .build();
    }
}