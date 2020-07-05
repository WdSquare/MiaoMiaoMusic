package com.xicheng.app.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.xicheng.app.Constants
import com.xicheng.app.R
import com.xicheng.app.RecycleViewAdapter
import com.xicheng.app.model.MusicViewModel
import kotlinx.android.synthetic.main.fragment_all_music.*


/**
 * A simple [Fragment] subclass.
 */
class AllMusicFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val musicViewModel: MusicViewModel by viewModels()
        val recycleViewAdapter = RecycleViewAdapter(this.context, musicViewModel.currentList.value)
        recycle_allmusic.layoutManager = LinearLayoutManager(this.context)
        recycle_allmusic.adapter = recycleViewAdapter
        //设置监听
        recycleViewAdapter.setOnItemClickListener {
            Constants.mPosition = it
            musicViewModel.setCurrentUri(musicViewModel.currentList.value?.get(it)?.description?.mediaUri)
            Log.d(tag, "点击了第" + it + "条")
        }
    }
}
