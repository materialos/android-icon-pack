package com.afollestad.polar.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;
import com.afollestad.polar.util.DrawableXmlParser;


public class IconDetailsDialog extends DialogFragment {

    public IconDetailsDialog() {
    }

    public static IconDetailsDialog create(@Nullable Bitmap bmp, @NonNull DrawableXmlParser.Icon icon) {
        IconDetailsDialog dialog = new IconDetailsDialog();
        Bundle args = new Bundle();
        if (bmp != null)
            args.putParcelable("icon", bmp);
        args.putSerializable("iconobj", icon);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        assert getArguments() != null;
        DrawableXmlParser.Icon icon = (DrawableXmlParser.Icon) getArguments().getSerializable("iconobj");
        assert icon != null;

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title(icon.getName())
                .negativeText(R.string.dismiss);
        final MaterialDialog dialog;

        if (getArguments().containsKey("icon")) {
            dialog = builder.customView(R.layout.dialog_icon_view, false).build();
            assert dialog.getCustomView() != null;

            ImageView iconView = (ImageView) dialog.getCustomView().findViewById(R.id.icon);
            final Bitmap bmp = getArguments().getParcelable("icon");
            iconView.setImageBitmap(bmp);

            final TextView negative = dialog.getActionButton(DialogAction.NEGATIVE);
            negative.setAlpha(0f);
            negative.animate().setDuration(500)
                    .alpha(1f).start();

            if (bmp != null) {
                Palette.from(bmp).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        if (getDialog() == null || !isAdded() || getActivity() == null) return;
                        final MaterialDialog dialog = (MaterialDialog) getDialog();
                        int color = palette.getVibrantColor(0);
                        if (color == 0)
                            color = palette.getMutedColor(0);
                        if (color == 0)
                            color = DialogUtils.resolveColor(getActivity(), R.attr.colorAccent);
                        dialog.getActionButton(DialogAction.NEGATIVE).setTextColor(color);
                    }
                });
            }
        } else {
            dialog = builder
                    .content(Html.fromHtml(getString(R.string.invalid_drawable_error, icon.getDrawable())))
                    .contentLineSpacing(1.4f)
                    .build();
        }

        return dialog;
    }
}