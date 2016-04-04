package com.allen.allenmusic.adapter;

import android.content.Context;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.allen.allenmusic.R;
import com.allen.allenmusic.utils.MediaUtils;
import com.allen.allenmusic.value.Mp3Info;

import java.util.ArrayList;

/**
 * Created by Allen on 16/3/5.
 */
public class MyMusicListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Mp3Info> mp3Infos;
    public MyMusicListAdapter(Context context,ArrayList<Mp3Info> mp3Infos){
        this.context=context;
        this.mp3Infos=mp3Infos;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public int getCount() {
        return mp3Infos.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3Infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder vh;
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.item_music_list,null);
            vh=new viewHolder();
            vh.textView1_title= (TextView) convertView.findViewById(R.id.textView1_title);
            vh.textView2_singer= (TextView) convertView.findViewById(R.id.textView2_singer);
            vh.textView3_time= (TextView) convertView.findViewById(R.id.textView3_time);
            convertView.setTag(vh);
        }
        vh= (viewHolder) convertView.getTag();
        Mp3Info mp3Info=mp3Infos.get(position);
        vh.textView1_title.setText(mp3Info.getTitle());
        vh.textView2_singer.setText(mp3Info.getArtist());
        vh.textView3_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));

        return convertView;
    }

    static class viewHolder{
        TextView textView1_title;
        TextView textView2_singer;
        TextView textView3_time;

    }
}
