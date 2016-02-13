package org.materialos.icons.viewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.afollestad.assent.AssentFragment;
import com.afollestad.polar.R;
import org.materialos.icons.util.WallpaperUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerPageFragment extends AssentFragment {

    @Bind(R.id.progress)
    ProgressBar mProgress;
    @Bind(R.id.photo)
    ImageView mPhoto;

    private WallpaperUtils.Wallpaper mWallpaper;
    private boolean isActive;
    private int mIndex;

    public String getTitle() {
        return mWallpaper.name;
    }

    public String getSubTitle() {
        return mWallpaper.author;
    }

    public static ViewerPageFragment create(WallpaperUtils.Wallpaper wallpaper, int index) {
        ViewerPageFragment frag = new ViewerPageFragment();
        frag.mWallpaper = wallpaper;
        Bundle args = new Bundle();
        args.putSerializable("wallpaper", wallpaper);
        args.putInt("index", index);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWallpaper = (WallpaperUtils.Wallpaper) getArguments().getSerializable("wallpaper");
        mIndex = getArguments().getInt("index");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        WallpaperUtils.download(getActivity(), mWallpaper, item.getItemId() == R.id.apply);
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_viewer, container, false);
    }

    public ViewerPageFragment setIsActive(boolean active) {
        isActive = active;
        final ViewerActivity act = (ViewerActivity) getActivity();
        if (act != null && isActive) {
            act.mToolbar.setTitle(getTitle());
            act.mToolbar.setSubtitle(getSubTitle());
        }
        return this;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        ViewCompat.setTransitionName(mPhoto, "view_" + mIndex);
        mProgress.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(mWallpaper.url)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        mProgress.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mProgress.setVisibility(View.GONE);
                        return false;
                    }
                }).into(mPhoto);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setIsActive(isActive);
    }
}