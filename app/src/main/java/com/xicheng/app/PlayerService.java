package com.xicheng.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.PowerManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.xicheng.app.model.MusicProvider;
import com.xicheng.app.model.MusicViewModel;

import java.io.IOException;
import java.util.List;

/**
 * Created by Square
 * Date :2020/6/18
 * Description : 播放服务，受控端。实现后台音乐播放。
 * Version :1.0
 */
public class PlayerService extends MediaBrowserServiceCompat implements MediaPlayer.OnPreparedListener {
    private static final String CHANNEL_ID = "my_notifi";
    public static final String EXTRA_CONNECTED_CAST = "extra_connected_cast";
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String LOG_TAG = "PlayerService";
    private static final String TAG = "PlayerService";
    private MediaPlayer mPlayer;
    private MediaSessionCompat mediaSession;//媒体会话，连接受控端和客户端
    private PlaybackStateCompat.Builder stateBuilder;//媒体的播放状态
    //private MusicProvider musicProvider;
    private MusicViewModel musicViewModel;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service启动");
        //musicProvider = new MusicProvider(this.getApplication());
        initSession();
        //initMediaPlayer();
        musicViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication()).create(MusicViewModel.class);

    }


    private void initMediaPlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void initSession() {
        // 新建一个媒体会话
        mediaSession = new MediaSessionCompat(this, LOG_TAG);

        //
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // 创建媒体状态
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE)
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f);
        mediaSession.setPlaybackState(stateBuilder.build());
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        // 会话回调，客户端的操作通过这个回调控制播放器
        mediaSession.setCallback(new MySessionCallback());
        // 设置会话令牌，让客户端可以调用会话
        setSessionToken(mediaSession.getSessionToken());
    }

    /**
     * 可根据uid设置是否允许获取内容，如果返回为空则拒绝。否则返回root
     *
     * @param clientPackageName 包名
     * @param clientUid         客户端id
     * @param rootHints         。
     * @return 如果为空，则拒绝获取内容。否则允许。
     */
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(MY_MEDIA_ROOT_ID, null);
    }

    /**
     * 客户端通过订阅获取内容时会调用
     *
     * @param parentId id
     * @param result   返回的客户端数据
     */
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(musicViewModel.getListFromListId(parentId));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayer.start();
    }

    class MySessionCallback extends MediaSessionCompat.Callback {
        private static final String TAG = "MySessionCallback";
        private PlaybackStateCompat mPlaybackState;

        @Override
        public void onPlay() {
            Log.d(TAG, "onPlay: ");
            super.onPlay();
            // Set the session active  (and update metadata and state)
            if (mPlayer == null) {
                mPlayer = MediaPlayer.create(PlayerService.this, Uri.parse(musicViewModel.getCurrentMusic().getValue().getString(MediaMetadataCompat.METADATA_KEY_DATE)));
            }
            mediaSession.setActive(true);
            mPlayer.start();
            mPlaybackState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                    .build();
            mediaSession.setPlaybackState(mPlaybackState);
            if (mediaSession.getController().getMetadata() != null) {
                initNotification(true);
            }
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            Log.d(TAG, "onPlayFromUri: " + uri);
            mediaSession.setMetadata(musicViewModel.getMetadataFromUri(uri));
            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
            }
            if (!mPlayer.isPlaying()) {
                mPlayer = MediaPlayer.create(PlayerService.this, uri);
                onPlay();
            } else {
                mPlayer.stop();
                try {
                    mPlayer = new MediaPlayer();
                    mPlayer.setDataSource(PlayerService.this, uri);
                    mPlayer.prepare();
                    mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            onPlay();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //当前歌曲播放完毕
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mPlaybackState = new PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
                            .build();
                    mediaSession.setPlaybackState(mPlaybackState);
                }
            });
        }

        @Override
        public void onPause() {
            mPlayer.pause();
            mPlaybackState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f)
                    .build();
            mediaSession.setPlaybackState(mPlaybackState);
            if (mediaSession.getController().getMetadata() != null) {
                initNotification(false);
            }
        }

    }

    void initNotification(Boolean isPlay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "音乐控制";
            String description = "允许在通知栏控制音乐";
            int importance = NotificationManager.IMPORTANCE_MIN;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        MediaDescriptionCompat description = mediaSession.getController().getMetadata().getDescription();
        // Add a cancel button
        Notification mNotification = new NotificationCompat.Builder(PlayerService.this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_icon)
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle() +
                        " - " + mediaSession.getController().getMetadata()
                        .getString(MediaMetadataCompat.METADATA_KEY_ALBUM))
                .setLargeIcon(loadingCover(mediaSession.getController().getMetadata().getString(MediaMetadataCompat.METADATA_KEY_DATE)))
                .setContentIntent(mediaSession.getController().getSessionActivity())
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(PlayerService.this,
                        PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(new NotificationCompat.Action(
                        R.drawable.noti_loved, getString(R.string.pause),
                        PendingIntent.getBroadcast(this, 1, new Intent("SONG_LOVED"), 0)))
                .addAction(new NotificationCompat.Action(
                        R.drawable.noti_pre, getString(R.string.pause),
                        PendingIntent.getBroadcast(this, 1, new Intent("SONG_PRE"), 0)))
                .addAction(new NotificationCompat.Action(
                        isPlay ? R.drawable.noti_play : R.drawable.noti_pause, getString(R.string.pause),
                        PendingIntent.getBroadcast(this, 1, new Intent("SONG_PLAY"), 0)))
                .addAction(new NotificationCompat.Action(
                        R.drawable.noti_next, getString(R.string.pause),
                        PendingIntent.getBroadcast(this, 1, new Intent("SONG_NEXT"), 0)))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0)
                        // Add a cancel button
                        .setShowCancelButton(false)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(PlayerService.this,
                                PlaybackStateCompat.ACTION_STOP))).build();
        startForeground(1, mNotification);
        ;
    }

    private Bitmap loadingCover(String mediaUri) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(mediaUri);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
        if (picture != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            return bitmap;
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cover_image, new BitmapFactory.Options());
            return bitmap;
        }
    }

}


