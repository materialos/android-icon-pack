package com.afollestad.polar.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ProgressDialogFragment extends DialogFragment {

    private final static String TAG = "[PROGRESS_DIALOG]";

    public static ProgressDialogFragment show(@NonNull AppCompatActivity context, @StringRes int message) {
        final FragmentManager fm = context.getFragmentManager();
        final Fragment frag = fm.findFragmentByTag(TAG);
        if (frag != null) fm.beginTransaction().remove(frag).commit();

        final ProgressDialogFragment dialog = new ProgressDialogFragment();
        final Bundle args = new Bundle();
        args.putInt("message", message);
        dialog.setArguments(args);
        dialog.show(fm, TAG);
        return dialog;
    }

    public void setContent(@StringRes final int content) {
        final MaterialDialog dialog = (MaterialDialog) getDialog();
        if (getActivity() != null && !getActivity().isFinishing() && dialog != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.setContent(content);
                }
            });
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        assert getArguments() != null;
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .content(getArguments().getInt("message"))
                .cancelable(false)
                .progress(true, -1)
                .build();
        setCancelable(false);
        return dialog;
    }
}