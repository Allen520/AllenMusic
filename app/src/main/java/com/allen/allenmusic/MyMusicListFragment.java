package com.allen.allenmusic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.allen.allenmusic.adapter.MyMusicListAdapter;
import com.allen.allenmusic.utils.MediaUtils;
import com.allen.allenmusic.value.Mp3Info;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Allen on 16/3/3.
 */
public class MyMusicListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    private ListView listView_my_music;
    private ArrayList<Mp3Info> mp3Infos;
    private MainActivity mainActivity;
    private MyMusicListAdapter myMusicListAdapter;
    private ImageView imageView_album;
    private TextView textView1_songName, textView2_singer;
    private ImageView imageView_play_pause, imageView_next;
    private boolean isPause = false;
    private int position = 0;
    private  CodingkePlayerApp app ;



    @Override
    public void onResume() {
        super.onResume();
        mainActivity.bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        mainActivity.unbindPlayService();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public static MyMusicListFragment newInstance() {
        MyMusicListFragment my = new MyMusicListFragment();
        return my;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_music_list_layout, null);
        CodingkePlayerApp app = (CodingkePlayerApp) mainActivity.getApplication();

        imageView_play_pause = (ImageView) view.findViewById(R.id.imageView_play_pause);
        imageView_album = (ImageView) view.findViewById(R.id.imageView_album);
        imageView_next = (ImageView) view.findViewById(R.id.imageView_next);
        textView1_songName = (TextView) view.findViewById(R.id.textView1_songName);
        textView2_singer = (TextView) view.findViewById(R.id.textView2_singer);
        listView_my_music = (ListView) view.findViewById(R.id.listView_my_music);
        imageView_album.setOnClickListener(this);
        listView_my_music.setOnItemClickListener(this);
        imageView_play_pause.setOnClickListener(this);

        imageView_next.setOnClickListener(this);
        //loadData();
        return view;
    }

    //加载本地音乐列表
    public void loadData() {
        mp3Infos = MediaUtils.getMp3Infos(mainActivity);
        //mp3Infos=mainActivity.playService.mp3Infos;
        myMusicListAdapter = new MyMusicListAdapter(mainActivity,mp3Infos);
        listView_my_music.setAdapter(myMusicListAdapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainActivity.unbindPlayService();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mainActivity.playService.getChangePlayList() != mainActivity.playService.MY_MUSIC_LIST) {

            mainActivity.playService.setMp3Infos(mp3Infos);
            mainActivity.playService.setChangePlayList(mainActivity.playService.MY_MUSIC_LIST);
        }
            try {
                mainActivity.playService.play(position);
            } catch (IOException e) {
                e.printStackTrace();
            }
        savePlayRecord();

    }
//保存播放记录
    private void savePlayRecord() {
        //获取当前正在播放的音乐
        Mp3Info mp3Info = mainActivity.playService.mp3Infos.get(mainActivity.playService.getCurrentPosition());
        try {
            Mp3Info playRecordMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getId()));
        if (playRecordMp3Info == null) {
            mp3Info.setMp3InfoId(mp3Info.getId());
            mp3Info.setPlayTime(System.currentTimeMillis());
            app.dbUtils.save(mp3Info);
        }else{
            playRecordMp3Info.setPlayTime(System.currentTimeMillis());
            app.dbUtils.update(playRecordMp3Info,"playTime");
        }
        } catch (DbException e) {
            e.printStackTrace();
        }



    }

    //回调播放状态下的UI设置
    public void changeUIStatusOnPlay(int position) {
        if (position >= 0 && position <= mainActivity.playService.mp3Infos.size()) {
            Mp3Info mp3Info = mainActivity.playService.mp3Infos.get(position);
            textView2_singer.setText(mp3Info.getArtist());
            textView1_songName.setText(mp3Info.getTitle());
            if (mainActivity.playService.isPlaying()) {
                imageView_play_pause.setImageResource(R.mipmap.pause);
            } else {
                imageView_play_pause.setImageResource(R.mipmap.play);
            }
            Bitmap albumBitmap = MediaUtils.getArtwork(mainActivity, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
            imageView_album.setImageBitmap(albumBitmap);
            this.position = position;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView_play_pause: {
                if (mainActivity.playService.isPlaying()) {
                    imageView_play_pause.setImageResource(R.mipmap.player_btn_play_normal);
                    mainActivity.playService.pause();

                } else {
                    if (mainActivity.playService.isPause()) {
                        imageView_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                        mainActivity.playService.start();
                    } else {
                        try {
                            mainActivity.playService.play(mainActivity.playService.getCurrentPosition());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            }
            case R.id.imageView_next: {
                try {
                    mainActivity.playService.next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.imageView_album: {
                Intent intent = new Intent(mainActivity, PlayActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }
}
