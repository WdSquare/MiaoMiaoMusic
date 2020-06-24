package com.xicheng.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.browse.MediaBrowser;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

/**
 * Created by Square
 * Date :2020/6/1
 * Description :
 * Version :
 */
public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.InnerHolder> {

    private OnItemClickListener mOnItemClickListener;
    private String TAG = "RecycleViewAdapter";
    private Context mContext;
    private List<MediaBrowserCompat.MediaItem> mMusicList;

    //构造方法
    public RecycleViewAdapter(Context context, List<MediaBrowserCompat.MediaItem> musicList) {
        this.mContext = context;
        this.mMusicList = musicList;
    }

    /*
     * 创建条目view
     * */
    @NonNull
    @Override
    public RecycleViewAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_recycle_item, null, false);
        return new InnerHolder(view);
    }

    //绑定holder
    @Override
    public void onBindViewHolder(@NonNull RecycleViewAdapter.InnerHolder holder, int position) {
        //设置数据
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        //Log.d(TAG, "getItemCount: " + mMusicList.size());
        return mMusicList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }


    //该内部类用来将数据放入布局文件中
    public class InnerHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private TextView mAuthor;
        private View itemView;


        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemView.setSelected(true);
            mTitle = itemView.findViewById(R.id.item_title);
            mAuthor = itemView.findViewById(R.id.item_author);

        }

        //设置数据
        @SuppressLint("SetTextI18n")
        public void setData(final int position) {
            MediaBrowserCompat.MediaItem mediaItem = mMusicList.get(position);
            mTitle.setText(mediaItem.getDescription().getTitle());
            mAuthor.setText(mediaItem.getDescription().getSubtitle() +
                    " - " + Objects.requireNonNull(mediaItem.getDescription().getExtras()).getString(MediaMetadataCompat.METADATA_KEY_ALBUM));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position);
                    }
                }
            });

        }
    }
}
