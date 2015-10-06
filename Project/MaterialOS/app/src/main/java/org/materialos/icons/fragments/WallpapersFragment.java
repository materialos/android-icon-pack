package org.materialos.icons.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.materialos.icons.R;
import org.materialos.icons.activities.DetailedWallpaperActivity;
import org.materialos.icons.adapters.WallsGridAdapter;
import org.materialos.icons.util.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;

public class WallpapersFragment extends Fragment {

    //TODO: Get rid of this entire mess and rewrite using Retrofit and RxJava (and maybe Glide instead of Picasso).
    //TODO: Shared Element Transition

    public static final String NAME = "name";
    public static final String WALL = "wall";
    private static final int DEFAULT_COLUMNS_PORTRAIT = 2;
    private static final int DEFAULT_COLUMNS_LANDSCAPE = 3;
    private ArrayList<HashMap<String, String>> mImageUrlData;
    private ViewGroup mRoot;
    private ProgressBar mProgress;
    private int mColumnCount;
    private int mNumColumns = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = (ViewGroup) inflater.inflate(R.layout.fragment_wallpapers, container, false);
        mProgress = (ProgressBar) mRoot.findViewById(R.id.progress);

        final ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(R.string.wallpapers);

        final boolean isLandscape = isLandscape();
        int mColumnCountPortrait = DEFAULT_COLUMNS_PORTRAIT;
        int mColumnCountLandscape = DEFAULT_COLUMNS_LANDSCAPE;
        int newColumnCount = isLandscape ? mColumnCountLandscape : mColumnCountPortrait;
        if (mColumnCount != newColumnCount) {
            mColumnCount = newColumnCount;
            mNumColumns = mColumnCount;
        }

        new DownloadJSON().execute();
        return mRoot;
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    // DownloadJSON AsyncTask
    private class DownloadJSON extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            mImageUrlData = new ArrayList<>();
            // Retrieve JSON Objects from the given URL address
            JSONObject json = JSONParser
                    .getJSONfromURL(getResources().getString(R.string.json_file_url));
            if (json != null) {
                try {
                    // Locate the array name in JSON
                    JSONArray jsonarray = json.getJSONArray("wallpapers");

                    for (int i = 0; i < jsonarray.length(); i++) {
                        HashMap<String, String> map = new HashMap<>();
                        json = jsonarray.getJSONObject(i);
                        // Retrieve JSON Objects
                        map.put("name", json.getString("name"));
                        map.put("author", json.getString("author"));
                        map.put("wall", json.getString("url"));
                        // Set the JSON Objects into the array
                        mImageUrlData.add(map);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(!result){
                Toast.makeText(getActivity(), getString(R.string.json_error_toast), Toast.LENGTH_LONG).show();
            }

            final GridView gridView = (GridView) mRoot.findViewById(R.id.gridView);
            gridView.setNumColumns(mNumColumns);
            final WallsGridAdapter mGridAdapter = new WallsGridAdapter(getActivity(), mImageUrlData, mNumColumns);
            gridView.setAdapter(mGridAdapter);
            if (mProgress != null)
                mProgress.setVisibility(View.GONE);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final HashMap<String, String> data = mImageUrlData.get(position);
                    final String wallurl = data.get((WallpapersFragment.WALL));
                    final Intent intent = new Intent(getActivity(), DetailedWallpaperActivity.class)
                            .putExtra("wall", wallurl);
                    startActivity(intent);
                }
            });
        }
    }
}
