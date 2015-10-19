package org.materialos.icons.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import org.materialos.icons.R;
import org.materialos.icons.activities.MainActivity;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle("");
        }

        FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.apply_btn);
        fab.setColorNormalResId(R.color.accent);
        fab.setColorPressedResId(R.color.accent);
        fab.setColorRippleResId(R.color.ripple_material_light);
        fab.show(true);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).getDrawer().setSelection(MainActivity.DRAWER_ITEM_APPLY);
                ((MainActivity) getActivity()).switchFragment(MainActivity.DRAWER_ITEM_APPLY, getResources().getString(R.string.apply), ApplyFragment.class);
            }
        });

        return root;
    }

}
