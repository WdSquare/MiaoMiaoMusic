package com.xicheng.app

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.xicheng.app.fragment.AllMusicFragment
import com.xicheng.app.fragment.MusicListsFragment
import com.xicheng.app.model.MusicViewModel
import com.xicheng.app.utils.CoverHelper
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    //数据模型
    private lateinit var mMusicViewModel: MusicViewModel

    //客户端
    private lateinit var mMediaBrowser: MediaBrowserCompat

    //控制器
    private lateinit var mMediaController: MediaControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        //请求权限
        initRequestPermissions()
        //初始化ViewModel
        val musicViewModel: MusicViewModel by viewModels()
        mMusicViewModel = musicViewModel
        //初始化客户端
        initMediaBrowserCompat()
        setBarView()
        //建立观察
        initObserve()
    }

    private fun initObserve() {
        mMusicViewModel.currentMusic.observe(this) {

            Log.d("aa", "bb")
            title_bar_main.text = it.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            artist_bar_main.text = it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            val bitmap = CoverHelper().getCover(it.getString(MediaMetadataCompat.METADATA_KEY_DATE))
            if (bitmap != null) {
                main_bar_image.setImageDrawable(null)
                main_bar_image.setImageBitmap(bitmap)
            } else {
                main_bar_image.setImageDrawable(getDrawable(R.drawable.cover_image))

            }
        }
        mMusicViewModel.currentUri.observe(this) {
            if (it != null) {
                mMediaController.transportControls.playFromUri(it, Bundle())
            }
        }
    }

    //更新底部状态栏
    private fun setBarView() {

        //播放或暂停
        main_bar_play.setOnClickListener {
           playOnClick()
        }
        //下一首
        main_bar_next.setOnClickListener {
            nextOnClick()
           }
        if (mMusicViewModel.currentMusic.value != null) {
            title_bar_main.text = mMusicViewModel.currentMusic.value!!.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            artist_bar_main.text = mMusicViewModel.currentMusic.value!!.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            val bitmap = CoverHelper().getCover(mMusicViewModel.currentMusic.value!!.getString(MediaMetadataCompat.METADATA_KEY_DATE))
            if (bitmap != null) {
                main_bar_image.setImageDrawable(null)
                main_bar_image.setImageBitmap(bitmap)
            } else {
                main_bar_image.setImageDrawable(getDrawable(R.drawable.cover_image))
            }
        }
    }

    /**
     * 播放或暂停
     */
    private fun playOnClick(){
        if (mMediaController.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
            mMediaController.transportControls.play()
        } else {
            mMediaController.transportControls.pause()
        }
    }

    /**
     * 下一首
     */
    private fun nextOnClick(){
        Constants.mPosition =
                if (Constants.mPosition == (mMusicViewModel.currentList.value?.size?.minus(1))) 0
                else ++Constants.mPosition
        mMediaController.transportControls.playFromUri(mMusicViewModel.currentList.value?.get(Constants.mPosition)?.description?.mediaUri, Bundle())

    }

    /**
     * 与服务器连接回调
     */
    private val mConnectionCallback =
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    super.onConnected()
                    if (mMediaBrowser.isConnected) {
                        var token = mMediaBrowser.sessionToken
                        //新建一个控制器
                        mMediaController = MediaControllerCompat(this@HomeActivity, token)
                        //绑定控制器到客户端
                        MediaControllerCompat.setMediaController(this@HomeActivity, mMediaController)
                        //设置控制器回调
                        mMediaController.registerCallback(mControllerCallback)
                        //订阅全部歌曲
                        mMediaBrowser.unsubscribe(Constants.LIST_ALL);
                        mMediaBrowser.subscribe(Constants.LIST_ALL, allMusicSubscriptionCallback)

                    }
                }
            }

    /**
     * 订阅所有音乐
     */
    private val allMusicSubscriptionCallback =
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
                    Log.d("tag", "" + children)
                    mMusicViewModel.setCurrentList(children);
                    //初始化ViewPager
                    initViewPager()
                }
            }

    /**
     * 控制器回调，播放信息改变时调用
     */
    private val mControllerCallback =
            object : MediaControllerCompat.Callback() {
                override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                    mMusicViewModel.setCurrentMusic(metadata)
                }

                override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                    if (state != null) {
                        if (state.state == PlaybackStateCompat.STATE_PLAYING) {
                            main_bar_play.setImageDrawable(resources.getDrawable(R.drawable.pause_icon))
                        } else if (state.state == PlaybackStateCompat.STATE_PAUSED){
                            main_bar_play.setImageDrawable(resources.getDrawable(R.drawable.play_icon))
                        }else if(state.state == PlaybackStateCompat.STATE_STOPPED){
                            //设置播放模式，默认下一首
                            nextOnClick()
                        }
                    }
                }
            }

    /**
     * 初始化客户端
     */
    private fun initMediaBrowserCompat() {
        mMediaBrowser = MediaBrowserCompat(this,
                ComponentName(this, PlayerService::class.java), mConnectionCallback, null)
        mMediaBrowser.connect()
    }

    /**
     * 初始化ViewPager和TabLayout
     */
    private fun initViewPager() {
        viewpager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> AllMusicFragment()
                    else -> MusicListsFragment()
                }
            }
        }
        TabLayoutMediator(tabLayout, viewpager2) { tab, position ->
            when (position) {
                0 -> tab.text = "全部音乐"
                //tab.view.setBackgroundColor(resources.getColor(R.color.colorAccent))
                else -> tab.text = "播放列表"
            }
        }.attach()
    }

    /**
     * 请求权限
     */
    private fun initRequestPermissions() {
        val permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

}
