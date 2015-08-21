package com.jahirfiquitiva.paperboard.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jahirfiquitiva.paperboard.activities.MainActivity;
import com.melnykov.fab.FloatingActionButton;

import org.materialos.icons.R;

public class HomeFragment extends Fragment {

    private static final String MARKET_URL = "https://play.google.com/store/apps/details?id=";

    private String mPlayStoreListing;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);

        mPlayStoreListing = getActivity().getPackageName();

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
        }

        TextView ratebtn = (TextView) root.findViewById(R.id.rate_button);
        ratebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rate = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URL + mPlayStoreListing));
                startActivity(rate);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.apply_btn);
        fab.setColorNormalResId(R.color.accent);
        fab.setColorPressedResId(R.color.accent);
        fab.setColorRippleResId(R.color.ripple_material_light);
        fab.show(true);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).getDrawer().setSelectionByIdentifier(3);
                ((MainActivity) getActivity()).switchFragment(3, getResources().getString(R.string.apply), ApplyFragment.class);
            }
        });

        return root;
    }

}
