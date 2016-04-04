package com.allen.allenmusic;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.allen.allenmusic.adapter.MyMusicListAdapter;
import com.allen.allenmusic.value.Mp3Info;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Allen on 16/3/14.
 */
public class MyLikeMusicListActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView listView_like;
    private CodingkePlayerApp app;
    private ArrayList<Mp3Info> likeMp3Infos;
    private MyMusicListAdapter adapter;
    private boolean isChange=false;//表示当前列表是否为收藏列表
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app= (CodingkePlayerApp) getApplication();
        setContentView(R.layout.activity_like_music_list);
        listView_like = (ListView) findViewById(R.id.listView_like);
        listView_like.setOnItemClickListener(this);
    //    ActionBar actionBar = getActionBar();
//       actionBar.hide();
//        actionBar.show();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayService();
    }

    private void initData() {
        try {
            List<Mp3Info> list=app.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLike","=","1"));
            if (list == null||list.size()==0) {
                return;
            }
            likeMp3Infos= (ArrayList<Mp3Info>) list;
            adapter=new MyMusicListAdapter(this,likeMp3Infos);
            listView_like.setAdapter(adapter);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
     if(playService.getChangePlayList()!=PlayService.LIKE_MUSIC_LIST){
         playService.setMp3Infos(likeMp3Infos);
         playService.setChangePlayList(PlayService.LIKE_MUSIC_LIST);
     }
        try {
            playService.play(position);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
