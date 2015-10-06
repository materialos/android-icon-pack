package org.materialos.icons.muzei;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.apps.muzei.api.UserCommand;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.materialos.icons.R;
import org.materialos.icons.util.Preferences;

import java.util.ArrayList;
import java.util.Random;

public class ArtSource extends RemoteMuzeiArtSource {

    private static final String ARTSOURCE_NAME = "MaterialOS";
    private static final String JSON_URL = "https://raw.githubusercontent.com/materialos/Android-Icon-Pack/master/Wallpapers/wallpapers.json";
    private static final String MARKET_URL = "https://play.google.com/store/apps/details?id=";
    private static final int COMMAND_ID_SHARE = 1337;
    private WallsDatabase wdb;
    private ArrayList<WallpaperInfo> wallslist;
    private Preferences mPrefs;

    public ArtSource() {
        super(ARTSOURCE_NAME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = intent.getExtras().getString("service");
        if (command != null) {
            try {
                onTryUpdate(UPDATE_REASON_USER_NEXT);
            } catch (RetryException e) {
                Log.d("MuzeiArtSource", Log.getStackTraceString(e));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wdb = new WallsDatabase(getApplicationContext());
        wallslist = new ArrayList<>();

        mPrefs = new Preferences(ArtSource.this);

        ArrayList<UserCommand> commands = new ArrayList<>();
        commands.add(new UserCommand(BUILTIN_COMMAND_ID_NEXT_ARTWORK));
        commands.add(new UserCommand(COMMAND_ID_SHARE, getString(R.string.justshare)));

        setUserCommands(commands);

    }

    @Override
    public void onCustomCommand(int id) {
        super.onCustomCommand(id);
        if (id == COMMAND_ID_SHARE) {
            Artwork currentArtwork = getCurrentArtwork();
            Intent shareWall = new Intent(Intent.ACTION_SEND);
            shareWall.setType("text/plain");

            String wallName = currentArtwork.getTitle();
            String authorName = currentArtwork.getByline();
            String storeUrl = MARKET_URL + getResources().getString(R.string.package_name);
            String iconPackName = getString(R.string.app_name);

            shareWall.putExtra(Intent.EXTRA_TEXT,
                    getString(R.string.partone) + wallName +
                            getString(R.string.parttwo) + authorName +
                            getString(R.string.partthree) + iconPackName +
                            getString(R.string.partfour) + storeUrl);

            shareWall = Intent.createChooser(shareWall, getString(R.string.share_via));
            shareWall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(shareWall);
        }
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        if (wallslist.size() == 0)
            getWallpapersFromUrl(JSON_URL);
        int i = getRandomInt();
        String token = wallslist.get(i).getWallURL();
        publishArtwork(new Artwork.Builder()
                .title(wallslist.get(i).getWallName())
                .byline(wallslist.get(i).getWallAuthor())
                .imageUri(Uri.parse(wallslist.get(i).getWallURL()))
                .token(token)
                .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(wallslist.get(i).getWallURL())))
                .build());
        scheduleUpdate(System.currentTimeMillis() + mPrefs.getRotateTime());
    }

    private int getRandomInt() {
        return new Random().nextInt(wallslist.size());
    }

    private void getWallpapersFromUrl(String url) {
        wallslist.clear();
        wallslist = wdb.getAllWalls();

        if (wallslist.size() == 0) {
            try {
                HttpClient cl = new DefaultHttpClient();
                HttpResponse response = cl.execute(new HttpGet(url));
                if (response.getStatusLine().getStatusCode() == 200) {
                    final String data = EntityUtils.toString(response.getEntity());
                    JSONObject jsonobject = new JSONObject(data);
                    final JSONArray jsonarray = jsonobject.getJSONArray("wallpapers");
                    wallslist.clear();
                    wdb.deleteAllWallpapers();
                    for (int i = 0; i < jsonarray.length(); i++) {
                        jsonobject = jsonarray.getJSONObject(i);
                        WallpaperInfo jsondata = new WallpaperInfo(
                                jsonobject.getString("name"),
                                jsonobject.getString("author"),
                                jsonobject.getString("url")
                        );
                        wdb.addWallpaper(jsondata);
                        wallslist.add(jsondata);
                    }
                }
            } catch (Exception e) {
                Log.d("Wallpapers", Log.getStackTraceString(e));
            }
        }
    }
}