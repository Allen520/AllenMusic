package com.allen.allenmusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.allen.allenmusic.utils.MediaUtils;
import com.allen.allenmusic.value.Mp3Info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//音乐播放的服务组件
//实现的功能:播放,暂停,上一首,下一首,获取当前歌曲播放的进度
public class PlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private MediaPlayer mPlayer;
    private int currentPosition = 0;//当前正在播放的歌曲的位置
    ArrayList<Mp3Info> mp3Infos;
    private MusicUpdateListener musicUpdateListener;
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private boolean isPause = false;
    //播放模式
    private int play_mode = ORDER_PLAY;
    public static final int ORDER_PLAY = 1;
    public static final int RANDOM_PLAY = 2;
    public static final int SINGLE_PLAY = 3;
    //切换音乐播放列表
    public static final int MY_MUSIC_LIST=1;
    public static final int LIKE_MUSIC_LIST=2;
    public static final int PLAY_RECORD_MUSIC_LIST=3;
    private int changePlayList=MY_MUSIC_LIST;

    public int getChangePlayList() {
        return changePlayList;
    }

    public void setChangePlayList(int changePlayList) {
        this.changePlayList = changePlayList;
    }

    //设置播放模式,参数取值
    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    public int getPlay_mode() {
        return play_mode;
    }

    public boolean isPause() {
        return isPause;
    }

    public PlayService() {
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CodingkePlayerApp app = (CodingkePlayerApp) getApplication();
        currentPosition = app.sp.getInt("currentPosition", 0);
        play_mode = app.sp.getInt("play_mode", PlayService.ORDER_PLAY);
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mp3Infos = MediaUtils.getMp3Infos(this);
        es.execute(updateStatusRunnable);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (es != null && !es.isShutdown()) {
            es.shutdown();
            es = null;
        }
    }

    //播放
    public void play(int position) throws IOException {
        Mp3Info mp3Info=null;
        if (position <0 || position >= mp3Infos.size()) {
            position=0;
        }
            mp3Info = mp3Infos.get(position);
            try {
                mPlayer.reset();
                mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
                mPlayer.prepare();
                mPlayer.start();

                currentPosition = position;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (musicUpdateListener != null) {
                musicUpdateListener.onChange(currentPosition);
            }
        }



    Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (musicUpdateListener != null && mPlayer != null && mPlayer.isPlaying()) {
                    musicUpdateListener.onPublish(getCurrentProgress());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    public int getCurrentProgress() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            isPause = true;

        }

    }

    public void next() throws IOException {
        if (currentPosition + 1 > mp3Infos.size() - 1) {
            currentPosition = 0;
        } else {
            currentPosition++;
        }
        play(currentPosition);

    }

    public void prev() throws IOException {
        if (currentPosition - 1 < 0) {
            currentPosition = mp3Infos.size() - 1;
        } else {
            currentPosition--;
        }
        play(currentPosition);

    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    private Random random = new Random();

    //一首歌播放完成所做的事,选择播放模式
    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_mode) {
            case ORDER_PLAY:
                try {
                    next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case SINGLE_PLAY:
                try {
                    play(currentPosition);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case RANDOM_PLAY:
                try {
                    play(random.nextInt(mp3Infos.size()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    //内部类,通过onbind方法返回playbinder,然后得到playservice对象,这样后来就可以实现里面的方法
    class PlayBinder extends Binder {
        public PlayService getPlayService() {
            return PlayService.this;
        }

    }

    public void start() {
        if (!mPlayer.isPlaying() && mPlayer != null) {
            mPlayer.start();

        }
    }

    public int getDuration() {
        return mPlayer.getDuration();
    }

    public void seekTo(int msec) {
        mPlayer.seekTo(msec);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return new PlayBinder();
    }

    //更新状态的接口
    public interface MusicUpdateListener {
        public void onPublish(int progress);

        public void onChange(int position);
    }


    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }
}
