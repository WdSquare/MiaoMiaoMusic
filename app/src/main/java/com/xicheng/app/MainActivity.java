package com.xicheng.app;


import android.Manifest;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xicheng.app.model.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private List<MediaBrowserCompat.MediaItem> allMusic;
    private RecycleViewAdapter mRecycleViewAdapter;
    private TextView mTitleBar;
    private TextView mArtistBar;
    private ImageView mImageBar;
    private RecyclerView recyclerView;
    private Uri mUri;
    private int mPosition;
    private ImageButton playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRequestPermissions();
        allMusic = new ArrayList<>();
        initMediaBrowserCompat();
        mMediaBrowser.connect();
        initView();
        MyDatabaseHelper MyDatabaseHelper = new MyDatabaseHelper(this);
        MyDatabaseHelper.insert("a", "aaa", "aa", "aa", "aa", 1);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(mControllerCallback);
        }
        mMediaBrowser.disconnect();
    }

    private void initMediaBrowserCompat() {
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, PlayerService.class), mConnectionCallback, null);
    }

    /**
     * 加载底栏信息
     */
    private void buildTransportControls() {
        playButton = findViewById(R.id.main_bar_play);
        Log.d(TAG, "buildTransportControls: ");
        //播放按钮被点击
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pbState = MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState().getState();
                Log.d(TAG, "onClick: ");
                if (pbState == PlaybackStateCompat.STATE_PAUSED) {
                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().play();
                } else {
                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().pause();
                }
            }
        });
        ImageButton nextButton = findViewById(R.id.main_bar_next);
        //下一首
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPosition = mPosition == allMusic.size()-1 ? 0 : ++ mPosition;
                MediaControllerCompat.getMediaController(MainActivity.this).
                        getTransportControls().playFromUri(allMusic.get(mPosition).getDescription().getMediaUri(),new Bundle());
            }
        });

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);
        // 注册控制回调
        mediaController.registerCallback(mControllerCallback);

    }

    /**
     * 初始化View
     */
    void initView() {
        //底栏
        mTitleBar = findViewById(R.id.title_bar_main);
        mArtistBar = findViewById(R.id.author_bar_main);
        mImageBar = findViewById(R.id.main_bar_image);
        //列表
        recyclerView = findViewById(R.id.main_recycle);
    }

    private void resetRecyclerVIew(final List<MediaBrowserCompat.MediaItem> mediaItems) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecycleViewAdapter = new RecycleViewAdapter(this, mediaItems);
        Log.d(TAG, "initRecycleView: " + mediaItems.size());
        recyclerView.setAdapter(mRecycleViewAdapter);
        mRecycleViewAdapter.setOnItemClickListener(new RecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //Log.d(TAG, "onItemClick: " + allMusic.get(position).getDescription().getMediaUri());
                mPosition = position;
                mUri =mediaItems.get(position).getDescription().getMediaUri();
                MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls()
                        .playFromUri(mUri, new Bundle());
            }
        });
    }

    //连接回调，和服务器连接成功后回调
    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    super.onConnected();
                    if (mMediaBrowser.isConnected()) {
                        // 拿到会话令牌
                        MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
                        try {
                            Log.d(TAG, "onConnected: ");
                            // 创建控制器
                            mMediaController = new MediaControllerCompat(MainActivity.this, // Context
                                    token);
                            // 绑定控制器
                            MediaControllerCompat.setMediaController(MainActivity.this, mMediaController);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        //取消订阅
                        mMediaBrowser.unsubscribe(Constants.LIST_ALL);
                        //订阅
                        mMediaBrowser.subscribe(Constants.LIST_ALL, subscriptionCallback);
                    }
                    // 更新ui
                    buildTransportControls();
                }
            };
    //订阅回调，拿到音乐列表
    final MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            if (children != null) {
                Log.d(TAG, "onChildrenLoaded: 列表大小" + children.size());
                for (MediaBrowserCompat.MediaItem mediaItem : children) {
                    allMusic.add(mediaItem);
                }
            }
            resetRecyclerVIew(allMusic);
        }
    };
    //控制器回调，播放状态及元数据改变时回调
    final MediaControllerCompat.Callback mControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    if (metadata != null) {
                        setBar(metadata);
                    }
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    Log.d(TAG, "onPlaybackStateChanged: ");
                    if(state.getState() == PlaybackStateCompat.STATE_PLAYING){
                        playButton.setImageDrawable(getResources().getDrawable(R.drawable.pause_icon));
                    }else {
                        playButton.setImageDrawable(getResources().getDrawable(R.drawable.play_icon));
                    }
                }
            };

    private void setBar(MediaMetadataCompat metadata) {
        mTitleBar.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        mArtistBar.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        String uri = metadata.getString(MediaMetadataCompat.METADATA_KEY_DATE);
        loadingCover(uri);
    }

    private void loadingCover(String mediaUri) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(mediaUri);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
        if (picture != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            mImageBar.setImageDrawable(null);
            mImageBar.setImageBitmap(bitmap);
        } else {
            mImageBar.setImageDrawable(getDrawable(R.drawable.next));
        }
    }
    /**
     * 请求权限
     */
    private void initRequestPermissions() {
        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

    }
}
