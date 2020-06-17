package com.xicheng.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecycleView();
        setTitle("我的音乐");
        initBarView();
    }
    void initRecycleView(){
        RecyclerView recyclerView = findViewById(R.id.main_recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecycleViewAdapter adapter = new RecycleViewAdapter(this);
        recyclerView.setAdapter(adapter);
    }
    void initBarView(){

    }
}
