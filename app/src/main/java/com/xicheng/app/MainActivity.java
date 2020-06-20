package com.xicheng.app;


import android.content.ComponentName;
import android.media.AudioManager;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xicheng.app.model.MyDatabaseHelper;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMediaBrowserCompat();
        initRecycleView();
        MyDatabaseHelper MyDatabaseHelper = new MyDatabaseHelper(this);
        MyDatabaseHelper.insert("a","aaa","aa","aa","aa",1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowser.connect();
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
        ImageButton playButton = findViewById(R.id.main_bar_play);
        Log.d(TAG, "buildTransportControls: ");
        //播放按钮被点击
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pbState = MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState().getState();
                Log.d(TAG, "onClick: ");
                if(pbState == PlaybackStateCompat.STATE_NONE){
                    MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().play();
                }
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
                MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToNext();
            }
        });

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);

        // 界面显示
        MediaMetadataCompat metadata = mediaController.getMetadata();
        PlaybackStateCompat pbState = mediaController.getPlaybackState();
//        String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
//        String artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
//        TextView titleBar = findViewById(R.id.title_bar_main);
//        TextView artistBar = findViewById(R.id.author_bar_main);
//        titleBar.setText(title);
//        artistBar.setText(artist);
        // 注册控制回调
        mediaController.registerCallback(mControllerCallback);
    }
  /*  private void initBarTop() {
        mTitleDataList = new ArrayList<>();
        mTitleDataList.add("我的");
        mTitleDataList.add("我的");
        mTitleDataList.add("我的");
        MagicIndicator magicIndicator = (MagicIndicator) findViewById(R.id.magic_indicator);
        mViewPager = findViewById(R.id.view_pager);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return mTitleDataList == null ? 0 : mTitleDataList.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                ColorTransitionPagerTitleView colorTransitionPagerTitleView = new ColorTransitionPagerTitleView(context);
                colorTransitionPagerTitleView.setNormalColor(Color.GRAY);
                colorTransitionPagerTitleView.setSelectedColor(Color.BLACK);
                colorTransitionPagerTitleView.setText(mTitleDataList.get(index));
                colorTransitionPagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mViewPager.setCurrentItem(index);
                    }
                });
                return colorTransitionPagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                return indicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        mFragmentContainerHelper = new FragmentContainerHelper(magicIndicator);

// ...

        // mFragmentContainerHelper.handlePageSelected(1);
    }
*/

    /**
     * 初始化RecycleView
     */
    void initRecycleView() {
        RecyclerView recyclerView = findViewById(R.id.main_recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecycleViewAdapter adapter = new RecycleViewAdapter(this);
        recyclerView.setAdapter(adapter);
    }


    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    super.onConnected();
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
                    // 更新ui
                    buildTransportControls();
                }
            };
    MediaControllerCompat.Callback mControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    Log.d(TAG, "onPlaybackStateChanged: ");
                }
            };
}
