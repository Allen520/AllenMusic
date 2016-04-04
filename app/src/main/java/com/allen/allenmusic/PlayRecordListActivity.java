package com.allen.allenmusic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.allen.allenmusic.adapter.MyMusicListAdapter;
import com.allen.allenmusic.utils.Constant;
import com.allen.allenmusic.value.Mp3Info;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayRecordListActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView listView_play_record;
    private TextView textView3,textView_no_data;
    private CodingkePlayerApp app;
    private ArrayList<Mp3Info> mp3Infos;
    private MyMusicListAdapter adapter;

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_record_list);
        app= (CodingkePlayerApp) getApplication();
        listView_play_record= (ListView) findViewById(R.id.listView_play_record);
        textView_no_data= (TextView) findViewById(R.id.textView2_no_data);
        textView3= (TextView) findViewById(R.id.textView3);
        listView_play_record.setOnItemClickListener(this);
        initData();
    }
//初始化数据
    private void initData() {
        try {
            List<Mp3Info> list=app.dbUtils.findAll(Selector.from(Mp3Info.class).where("playTime","!=",0).orderBy("playTime",true).limit(Constant.PLAY_RECORD_NUM));
            if (list == null||list.size()==0) {
                textView_no_data.setVisibility(View.VISIBLE);
                listView_play_record.setVisibility(View.GONE);
            }else {
                textView_no_data.setVisibility(View.GONE);
                listView_play_record.setVisibility(View.VISIBLE);
            mp3Infos= (ArrayList<Mp3Info>) list;
            adapter=new MyMusicListAdapter(this,mp3Infos);
            listView_play_record.setAdapter(adapter);}
        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_record_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(playService.getChangePlayList()!=PlayService.LIKE_MUSIC_LIST){
            playService.setMp3Infos(mp3Infos);
            playService.setChangePlayList(PlayService.PLAY_RECORD_MUSIC_LIST);
        }
        try {
            playService.play(position);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
