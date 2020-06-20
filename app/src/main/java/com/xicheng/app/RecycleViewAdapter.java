package com.xicheng.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Square
 * Date :2020/6/1
 * Description :
 * Version :
 */
public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.InnerHolder> {

    private OnItemClickListener mOnItemClickListener;
    Context mContext;

    //构造方法
    public RecycleViewAdapter(Context context) {
        this.mContext = context;
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

        return 20;
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
        private TextView mAlbum;
        private View itemView;


        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemView.setSelected(true);
            mTitle = itemView.findViewById(R.id.item_title);
            mAuthor = itemView.findViewById(R.id.item_author);
            mAlbum = itemView.findViewById(R.id.item_album);
        }

        //设置数据
        public void setData(final int position) {
//            ItemBean itemBean = mData.get(position);
//            mTitle.setText(itemBean.titles);
//            mAuthor.setText(itemBean.author);
//            mAlbum.setText(itemBean.album);
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
