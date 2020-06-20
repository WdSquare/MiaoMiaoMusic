package com.xicheng.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.xicheng.app.MainActivity;


/**
 * Created by Square
 * Date :2020/6/20
 * Description :
 * Version :
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "MyDatabaseHelper";

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, "dbMusic.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE musicinfo(_id INTEGER PRIMARY KEY AUTOINCREMENT,media_id VARCHAR(20),title VARCHAR(20),artist VARCHAR(20)," +
                "album VARCHAR(20),uri VARCHAR(20),duration INTRGER)");
        db.execSQL("CREATE TABLE lovedmusic(_love_id INTEGER PRIMARY KEY AUTOINCREMENT,_id INTEGER,title VARCHAR(20),artist VARCHAR(20)," +
                "album VARCHAR(20),uri VARCHAR(20),duration INTRGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String mediaId, String title, String artist, String album, String uri, long duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        Log.d(TAG, "insert: ");
        values.put("media_id", mediaId);
        values.put("title", title);
        values.put("artist", artist);
        values.put("album", album);
        values.put("uri", uri);
        values.put("duration", duration);
        db.insert("musicinfo", null, values);
        db.close();
    }
    public void insertLovedMusic(int id,String title, String artist, String album, String uri, long duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        Log.d(TAG, "insert: ");
        values.put("_id",id);
        values.put("title", title);
        values.put("artist", artist);
        values.put("album", album);
        values.put("uri", uri);
        values.put("duration", duration);
        db.insert("lovedmusic", null, values);
        db.close();
    }

}
