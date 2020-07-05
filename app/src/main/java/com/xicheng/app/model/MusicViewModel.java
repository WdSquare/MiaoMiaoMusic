package com.xicheng.app.model;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.xicheng.app.Constants;

import java.util.List;

/**
 * Created by Square
 * Date :2020/7/2
 * Description :
 * Version :
 */
public class MusicViewModel extends AndroidViewModel {
    private static MutableLiveData<MediaMetadataCompat> currentMusic = new MutableLiveData<>();
    private static MutableLiveData<List<MediaBrowserCompat.MediaItem>> currentMusicList = new MutableLiveData<>();
    private static MutableLiveData<Uri> currentUri = new MutableLiveData<>();
    private final MusicProvider musicProvider;
    private static List<MediaBrowserCompat.MediaItem> allMusicList;
    private Application application;
    private String TAG = "MusicViewModel";

    public MusicViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        musicProvider = new MusicProvider(application);
        getStatus();
    }
    public MutableLiveData<Uri> getCurrentUri() {
        return currentUri;
    }

    public void setCurrentUri(Uri currentUri) {
        MusicViewModel.currentUri.setValue(currentUri);
    }
    public void setAllMusicList(List<MediaBrowserCompat.MediaItem> allMusicList) {
        MusicViewModel.allMusicList = allMusicList;
    }

    public MutableLiveData<List<MediaBrowserCompat.MediaItem>> getCurrentList() {
        if (currentMusicList == null) {
            currentMusicList = new MutableLiveData<>();
        }
        return currentMusicList;
    }

    public void setCurrentList(List<MediaBrowserCompat.MediaItem> preMediaItems) {
        if (MusicViewModel.currentMusicList == null) {
            MusicViewModel.currentMusicList = new MutableLiveData<>();
        }
        MusicViewModel.currentMusicList.setValue(preMediaItems);
    }

    public LiveData<MediaMetadataCompat> getCurrentMusic() {
        return currentMusic;
    }

    public synchronized void setCurrentMusic(MediaMetadataCompat preMusic) {
        if (MusicViewModel.currentMusic == null) {
            MusicViewModel.currentMusic = new MutableLiveData<>();
        }
        MusicViewModel.currentMusic.setValue(preMusic);
    }

    public List<MediaBrowserCompat.MediaItem> getListFromListId(String mediaId){
        return musicProvider.getListFromListId(mediaId);
    }

    public List<MediaBrowserCompat.MediaItem> getListFromArtist(String artist){
        return musicProvider.getListFromArtist(artist);
    }
    public MediaMetadataCompat getMetadataFromUri(Uri uri) {
        return musicProvider.getMetadataFromUri(uri);
    }


    private void getStatus() {
        SharedPreferences current = application.getSharedPreferences("current", Context.MODE_PRIVATE);
        String uri = current.getString("currentUri","?");
        Constants.mPosition = current.getInt("mPosition",0);
        Log.d(TAG, "MusicViewModel: " + uri);
        currentMusic.setValue(uri!="?"?musicProvider.getMetadataFromUri(Uri.parse(uri)):musicProvider.getMetadataFromId(0));
    }

}
