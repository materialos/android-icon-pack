package com.jahirfiquitiva.paperboard.muzei;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

class WallsDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dashboard";
    private static final String TABLE_WALLPAPERS = "wallpapers";
    private static final String KEY_ID = "id";

    private static final String
            KEY_WALLNAME = "wallname",
            KEY_WALLAUTHOR = "wallauthor",
            KEY_WALLURL = "wallurl";

    public WallsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_WALLPAPER = "CREATE TABLE " + TABLE_WALLPAPERS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                KEY_WALLNAME + " TEXT NOT NULL," +
                KEY_WALLAUTHOR + " TEXT NOT NULL," +
                KEY_WALLURL + " TEXT NOT NULL" + ")";
        db.execSQL(CREATE_TABLE_WALLPAPER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WALLPAPERS);
        onCreate(db);
    }


    public void addWallpaper(WallpaperInfo arraylist) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_WALLNAME, arraylist.getWallName());
        values.put(KEY_WALLAUTHOR, arraylist.getWallAuthor());
        values.put(KEY_WALLURL, arraylist.getWallURL());

        db.insert(TABLE_WALLPAPERS, null, values);
    }

    public ArrayList<WallpaperInfo> getAllWalls() {
        ArrayList<WallpaperInfo> arrayList = new ArrayList<>();
        String SELECT = "SELECT * FROM " + TABLE_WALLPAPERS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(SELECT, null);

        if (cursor.moveToFirst()) {
            do {
                WallpaperInfo data = new WallpaperInfo(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3));

                arrayList.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return arrayList;
    }

    public void deleteAllWallpapers() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_WALLPAPERS, null, null);
        db.close();
    }
}