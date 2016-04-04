package com.allen.allenmusic.utils;

import android.app.DownloadManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.allen.allenmusic.R;
import com.allen.allenmusic.value.SearchResult;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Allen on 16/3/22.
 */
public class DownloadUtils {
    private static final String DOWNLOAD_URL = "/download?_o%2Fsearch%Fsong";
    public static final int SUCCESS_LRC = 1;
    public static final int FAILED_LRC = 2;
    public static final int SUCCESS_MP3 = 3;
    public static final int FAILED_MP3 = 4;
    public static final int GET_MP3_URL = 5;
    public static final int GET_FAILED_MP3_URL = 6;
    public static final int MUSIC_EXISTS = 7;

    private static DownloadUtils sInstance;
    private OnDownloadListener mListener;

    private ExecutorService mThreadPool;

    public DownloadUtils setListener(OnDownloadListener mListener) {
        this.mListener = mListener;
        return this;
    }
    public interface OnDownloadListener {
        public void onDownload(String url);
        public void onFailed(String url);
    }
    public synchronized static DownloadUtils getsInstance() {
        if (sInstance == null) {
            try {
                sInstance = new DownloadUtils();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    public DownloadUtils() throws ParserConfigurationException {
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    public void download(final SearchResult searchResult) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SUCCESS_LRC:
                        if (mListener != null) mListener.onDownload("歌词下载成功");
                        break;
                    case FAILED_LRC:
                        if (mListener != null) mListener.onDownload("歌词下载失败");
                        break;
                    case GET_MP3_URL:
                        downloadMusic(searchResult, (String) msg.obj, this);
                        break;
                    case GET_FAILED_MP3_URL:
                        if (mListener != null) mListener.onFailed("下载失败,该歌曲为收费VIP类型或不存在");
                        break;
                    case SUCCESS_MP3:
                        if (mListener != null)
                            mListener.onDownload(searchResult.getMusicName() + "已下载");
                        String url = Constant.BAIDU_URL + searchResult.getUrl();
                        downloadLRC(url, searchResult.getMusicName(), this);
                        break;
                    case FAILED_MP3:
                        if (mListener != null)
                            mListener.onFailed(searchResult.getMusicName() + "下载失败");
                        break;
                    case MUSIC_EXISTS:
                        if (mListener != null) mListener.onFailed("音乐已存在");
                        break;
                }
            }
        };
        getDownloadMusicURL(searchResult, handler);
    }
//下载歌词
    private void downloadLRC(final String url, final String musicName,final Handler handler) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();
                    Elements targetElements = doc.select("a[data_btndata]");
                    Elements lrcTag = doc.select("div.lyric-content");
                    String lrcURL = lrcTag.attr("data-lrclink");
                    File lrcDirFile = new File(Environment.getExternalStorageDirectory() + Constant.DIR_LRC);

                    if (!lrcDirFile.exists()) {
                        lrcDirFile.mkdirs();
                    }
                    lrcURL = Constant.BAIDU_URL + lrcURL;
                    String target = lrcDirFile + "/" + musicName + ".lrc";

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(lrcURL).build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            PrintStream ps = null;
                            ps = new PrintStream(new File((target)));
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes, 0, bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_LRC).sendToTarget();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                     catch (IOException e) {
                        e.printStackTrace();
                    }


            }


        });
    }



    //下载音乐
    private void downloadMusic(final SearchResult searchResult, final String url, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                File musicDirFile = new File(Environment.getExternalStorageDirectory() + Constant.DIR_MUSIC);
                if (!musicDirFile.exists()) {
                    musicDirFile.mkdirs();
                }
                String mp3url = Constant.BAIDU_URL + url;
                String target = musicDirFile + "/" + searchResult.getMusicName() + ".mp3";
                File fileTarget = new File(target);
                if (fileTarget.exists()) {
                    handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                    return;
                } else {
                    //使用OKHttpClient组件
                    try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(mp3url).build();

                    Response response = null;
                    response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            PrintStream ps = null;
                            ps = new PrintStream(fileTarget);
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes, 0, bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_MP3).sendToTarget();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        });
    }


    private void getDownloadMusicURL(final SearchResult searchResult, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = Constant.BAIDU_URL + "/song/" + searchResult.getUrl().substring(searchResult.getUrl().lastIndexOf("/") + 1) + DOWNLOAD_URL;
                    Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();
                    Elements targetElements = doc.select("a[data_btndata]");
                    //下面的不成立
                    if (targetElements.size() <= 0) {
                        handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                        return;
                    }
                    for (Element e : targetElements) {
                        if (e.attr("href").contains(".mp3")) {
                            String result = e.attr("href");
                            Message msg = handler.obtainMessage(GET_MP3_URL, result);
                            msg.sendToTarget();
                            return;
                        }
                        if (e.attr("href").startsWith("/vip")) {
                            targetElements.remove(e);
                        }
                    }
                    if (targetElements.size() <= 0) {
                        handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                        return;
                    }

                    //下面的才是有效的
                    String result = targetElements.get(0).attr("href");
                    Message msg = handler.obtainMessage(GET_MP3_URL, result);
                    msg.sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
