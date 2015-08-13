package com.jahirfiquitiva.paperboard.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jahirfiquitiva.paperboard.views.SlidingTabLayout;

import org.materialos.icons.R;

public class PreviewsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_icons, container, false);

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(R.string.section_two);

        ViewPager mPager = (ViewPager) root.findViewById(R.id.pager);
        mPager.setAdapter(new MyPagerAdapter(getActivity().getFragmentManager()));

        SlidingTabLayout mTabs = (SlidingTabLayout) root.findViewById(R.id.tabs);
        mTabs.setViewPager(mPager);
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.accent);
            }
        });

        return root;
    }

    class MyPagerAdapter extends FragmentStatePagerAdapter {

        final String[] tabs;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            tabs = getResources().getStringArray(R.array.tabs);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = new Fragment();
            switch (position) {
                case 0:
                    f = IconsFragment.newInstance(R.array.latest);
                    break;
                case 1:
                    f = IconsFragment.newInstance(R.array.icon_pack_names);
                    break;
                case 2:
                    f = IconsFragment.newInstance(R.array.google);
                    break;
                case 3:
                    f = IconsFragment.newInstance(R.array.games);
                    break;

            }
            return f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }

        @Override
        public int getCount() {
            return tabs.length;
        }
    }
}
