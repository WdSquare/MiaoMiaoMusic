package com.xicheng.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MyHolder> {

    Context mContext;
    private ImageView mItemView;
    private TextView mItemTitle;
    private TextView mItemAuthor;

    public RecycleViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(mContext).inflate(R.layout.view_recycle_item,null,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        mItemTitle.setText("aaa");
        mItemAuthor.setText("bbb");
    }

    @Override
    public int getItemCount() {
        return 20;
    }

    public class MyHolder extends RecyclerView.ViewHolder{

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            mItemView = itemView.findViewById(R.id.recycle_item_image);
            mItemTitle = itemView.findViewById(R.id.recycle_item_title);
            mItemAuthor = itemView.findViewById(R.id.recycle_item_author);
        }
    }
}
