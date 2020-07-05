package com.xicheng.app.model;

import android.app.Application;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.ArraySet;

import com.xicheng.app.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Square
 * Date :2020/6/18
 * Description :
 * Version :
 */
public class MusicProvider {
    private Application mContext;
    private List<MediaBrowserCompat.MediaItem> mAllMusicList = new ArrayList<>();//全部音乐
    private List<MediaMetadataCompat> mMetadataList = new ArrayList<>();//全部音乐

    public MusicProvider(Application context) {
        this.mContext = context;
        //扫描媒体库
        scanAllAudioFiles();
    }

    public List<MediaBrowserCompat.MediaItem> getListFromListId(String mediaId) {
        switch (mediaId){
            case Constants.LIST_ALL:
                return getAllMusicList();
            case Constants.LIST_FAVOURITE:
                return getFavourite();
        }
        return null;
    }

    private List<MediaBrowserCompat.MediaItem> getFavourite() {
        return null;
    }

    private List<MediaBrowserCompat.MediaItem> getAllMusicList() {
        return mAllMusicList;
    }

    List<MediaBrowserCompat.MediaItem> getListFromArtist(String artist){
        List<MediaBrowserCompat.MediaItem> list = new ArrayList<>();
        for (MediaBrowserCompat.MediaItem mediaItem : mAllMusicList) {
            if(Objects.equals(mediaItem.getDescription().getSubtitle(), artist)){
                list.add(mediaItem);
            }
        }
        return list;
    }
    public Set<String> getAllArtist(){
        Set<String> stringSet = new ArraySet<>();
        for (MediaBrowserCompat.MediaItem mediaItem : mAllMusicList) {
            stringSet.add(String.valueOf(mediaItem.getDescription().getSubtitle()));
        }
        return stringSet;
    }

    /**
     * 通过uri拿到元数据
     * @param uri 传入的uri
     * @return 找到的元数据
     */
    public MediaMetadataCompat getMetadataFromUri(Uri uri) {
        for (MediaMetadataCompat metadata : mMetadataList) {
            if (Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_DATE)).equals(uri)) {
                return metadata;
            }
        }
        return null;
    }

    public MediaMetadataCompat getMetadataFromId(int id) {
        if(mMetadataList == null){
            scanAllAudioFiles();
        }
        if(id < mMetadataList.size()){
            return mMetadataList.get(id );
        }else {
            return null;
        }

    }

    /**
     * 扫描本地媒体库，得到数据
     */
    private void scanAllAudioFiles() {

        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String mediaId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        .putString(MediaMetadataCompat.METADATA_KEY_DATE, url)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                        .build();
                mMetadataList.add(metadata);
                Bundle bundle = new Bundle();
                bundle.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album);
                bundle.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);
                //MediaDescriptionCompat descriptionCompat = metadata.getDescription();
                MediaDescriptionCompat descriptionCompat = new MediaDescriptionCompat.Builder()
                        .setMediaId(mediaId)
                        .setTitle(title)
                        .setMediaUri(Uri.parse(url))
                        .setSubtitle(artist)
                        .setExtras(bundle)
                        .build();
                MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(descriptionCompat,
                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
                mAllMusicList.add(mediaItem);
                cursor.moveToNext();
            }
        }
        cursor.close();
    }



}
