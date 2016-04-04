package com.allen.allenmusic;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.allen.allenmusic.utils.MediaUtils;
import com.allen.allenmusic.value.Mp3Info;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.io.IOException;
import java.util.ArrayList;

public class PlayActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final int UPDATE_TIME = 0x1;
    private TextView textView_tile, textView_end_time, textView_start_time;
    private SeekBar seekBar;
    private ImageView imageView1_album, imageView_play_mode, imageView_previous, imageView1_next, imageView1_play_pause, imageView_favorite;
    //private ArrayList<Mp3Info> mp3Infos;
    private ArrayList<View> views;
    private ViewPager viewPager;
    private CodingkePlayerApp app;

    @Override
    public void publish(int progress) {
        Message msg = myHandler.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;
        myHandler.sendMessage(msg);
        seekBar.setProgress(progress);

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView1_play_pause: {
                if (playService.isPlaying()) {
                    imageView1_play_pause.setImageResource(R.mipmap.play);
                    playService.pause();
                } else {
                    if (playService.isPause()) {
                        imageView1_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                        playService.start();
                    } else {
                        try {
                            playService.play(playService.getCurrentPosition());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                break;
            }
            case R.id.imageView1_next: {
                try {
                    playService.next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.imageView_previous: {
                try {
                    playService.prev();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.imageView_play_mode: {
                int mode = (int) imageView_play_mode.getTag();
                switch (mode) {
                    case PlayService.ORDER_PLAY:
                        imageView_play_mode.setImageResource(R.mipmap.random);
                        imageView_play_mode.setTag(PlayService.RANDOM_PLAY);
                        playService.setPlay_mode(PlayService.RANDOM_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.random_play), Toast.LENGTH_SHORT);
                        break;
                    case PlayService.RANDOM_PLAY:
                        imageView_play_mode.setImageResource(R.mipmap.single);
                        imageView_play_mode.setTag(PlayService.SINGLE_PLAY);
                        playService.setPlay_mode(PlayService.SINGLE_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.single_play), Toast.LENGTH_SHORT);
                        break;
                    case PlayService.SINGLE_PLAY:
                        imageView_play_mode.setImageResource(R.mipmap.order);
                        imageView_play_mode.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_mode(PlayService.ORDER_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.order_play), Toast.LENGTH_SHORT);
                        break;
                }
                break;
            }
            case R.id.imageView_favorite: {
                Mp3Info mp3Info = playService.mp3Infos.get(playService.getCurrentPosition());
                Log.d("mp3","likefavirote = "+mp3Info.getId());

                Mp3Info likeMp3Info = null;
                try {
                    likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=", getId(mp3Info)));

                    int isLike=likeMp3Info.getIsLike();
                        if (isLike == 1) {
                            likeMp3Info.setIsLike(0);
                            imageView_favorite.setImageResource(R.mipmap.xin_bai);
                        }
                        else{
                            likeMp3Info.setIsLike(1);
                            imageView_favorite.setImageResource(R.mipmap.xin_hong);
                        }

                    app.dbUtils.update(likeMp3Info,"isLike");
                } catch (DbException e) {
                    e.printStackTrace();
                }
                //dbUtilsapp.dbUtils.deleteById(Mp3Info.class, likeMp3Info.getId())

                break;
            }
            default:
                break;
        }
    }

    private long getId(Mp3Info mp3Info) {
        //初始化收藏状态
        long id=0;
        switch (playService.getChangePlayList()){
            case PlayService.MY_MUSIC_LIST:
                id=mp3Info.getId();
                break;
            case PlayService.LIKE_MUSIC_LIST:
                id =mp3Info.getMp3InfoId();
                break;
            default:
                break;
        }
        return id;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            playService.pause();
            playService.seekTo(progress);
            playService.start();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    static class MyHandler extends Handler {
        private PlayActivity playActivity;

        public MyHandler(PlayActivity playActivity) {
            this.playActivity = playActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (playActivity != null) {
                switch (msg.what) {
                    case UPDATE_TIME:
                        playActivity.textView_start_time.setText(MediaUtils.formatTime(msg.arg1));
                        break;

                }
            }
        }
    }

    @Override
    public void change(int position) {
//       if(this.playService.isPlaying())
//       {
        Mp3Info mp3Info = playService.mp3Infos.get(position);
        textView_tile.setText(mp3Info.getTitle());
        Bitmap albumBitmap = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
        imageView1_album.setImageBitmap(albumBitmap);
        textView_end_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        seekBar.setProgress(0);
        seekBar.setMax((int) mp3Info.getDuration());
        if (this.playService.isPlaying()) {
            imageView1_play_pause.setImageResource(R.mipmap.pause);
        }
        if (this.playService.isPause()) {
            imageView1_play_pause.setImageResource(R.mipmap.play);
        }
        switch (playService.getPlay_mode()) {
            case PlayService.ORDER_PLAY:
                imageView_play_mode.setImageResource(R.mipmap.order);
                imageView_play_mode.setTag(PlayService.ORDER_PLAY);
                break;
            case PlayService.RANDOM_PLAY:
                imageView_play_mode.setImageResource(R.mipmap.random);
                imageView_play_mode.setTag(PlayService.RANDOM_PLAY);
                break;
            case PlayService.SINGLE_PLAY:
                imageView_play_mode.setImageResource(R.mipmap.single);
                imageView_play_mode.setTag(PlayService.SINGLE_PLAY);
                break;
            default:
                break;
        }

        //初始化收藏状态
        try {

            Mp3Info likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",getId(mp3Info)));
            Log.d("mp3","likeMp3Info = "+likeMp3Info.getId());
            Log.d("mp3","mp3Info = "+mp3Info.getId());
                if (likeMp3Info.getIsLike() == 1) {
                    imageView_favorite.setImageResource(R.mipmap.xin_hong);
                } else {
                    imageView_favorite.setImageResource(R.mipmap.xin_bai);
                }


        } catch (DbException e) {
            e.printStackTrace();
        }

//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        app = (CodingkePlayerApp) getApplication();
        textView_end_time = (TextView) findViewById(R.id.textView_end_time);
        textView_start_time = (TextView) findViewById(R.id.textView_start_time);
        //textView_tile= (TextView) findViewById(R.id.textView_title);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        //imageView1_album= (ImageView) findViewById(R.id.imageView1_album);
        imageView_play_mode = (ImageView) findViewById(R.id.imageView_play_mode);
        imageView1_play_pause = (ImageView) findViewById(R.id.imageView1_play_pause);
        imageView1_next = (ImageView) findViewById(R.id.imageView1_next);
        imageView_previous = (ImageView) findViewById(R.id.imageView_previous);
        imageView_favorite = (ImageView) findViewById(R.id.imageView_favorite);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        initViewPager();

        imageView1_play_pause.setOnClickListener(this);
        imageView1_next.setOnClickListener(this);
        imageView_previous.setOnClickListener(this);
        imageView_play_mode.setOnClickListener(this);
        imageView_favorite.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        //mp3Infos = MediaUtils.getMp3Infos(this);
        //bindPlayService();
        myHandler = new MyHandler(this);


    }

    private static MyHandler myHandler;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
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
    protected void onDestroy() {
        super.onDestroy();
        unbindPlayService();
    }

    private void initViewPager() {
        views = new ArrayList<View>();

        View album_image_layout = getLayoutInflater().inflate(R.layout.album_image_layout, null);
        imageView1_album = (ImageView) album_image_layout.findViewById(R.id.imageView1_album);
        textView_tile = (TextView) album_image_layout.findViewById(R.id.textView_title);
        views.add(album_image_layout);
        views.add(getLayoutInflater().inflate(R.layout.lrc_layout, null));

        viewPager.setAdapter(new mAdapter());

    }

    private class mAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = views.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }
    }
}
