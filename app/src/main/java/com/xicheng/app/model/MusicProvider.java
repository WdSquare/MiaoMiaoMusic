package com.xicheng.app.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.xicheng.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Square
 * Date :2020/6/18
 * Description :
 * Version :
 */
public class MusicProvider {
    Context mContext;
    private List<MediaBrowserCompat.MediaItem> allMusic = new ArrayList<>();//全部音乐

    public MusicProvider(Context context) {
        this.mContext = context;
    }

    public List<MediaBrowserCompat.MediaItem> getAllMusic() {
        //扫描媒体库
        scanAllAudioFiles();
        return allMusic;
    }

    private void scanAllAudioFiles() {
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String mediaId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
//                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//                mediaMetadataRetriever.setDataSource(url);
//                byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
//                Bitmap bitmap;
//                if (picture != null) {
//                    bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
//                } else {
//                   bitmap = null;
//                }
                MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        .putString(MediaMetadataCompat.METADATA_KEY_DATE, url)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                        .build();
                MediaDescriptionCompat descriptionCompat = new MediaDescriptionCompat.Builder()
                        .build();

                MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(descriptionCompat,
                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
                allMusic.add();
                cursor.moveToNext();
            }
        }
    }

    public void updateDatabase() {

    }

}
