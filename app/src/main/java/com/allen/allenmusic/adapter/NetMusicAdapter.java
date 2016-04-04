package com.allen.allenmusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.allen.allenmusic.BaseActivity;
import com.allen.allenmusic.R;
import com.allen.allenmusic.value.SearchResult;

import java.util.ArrayList;

/**
 * Created by Allen on 16/3/22.
 */
public class NetMusicAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<SearchResult> searchResults;

    public NetMusicAdapter(Context context, ArrayList<SearchResult> searchResults) {
        this.context = context;
        this.searchResults = searchResults;
    }

    public ArrayList<SearchResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(ArrayList<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    @Override
    public int getCount() {
        return searchResults.size();
    }

    @Override
    public Object getItem(int position) {
        return searchResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.net_layout_music, null);
            vh = new ViewHolder();
            vh.textView1_title = (TextView) convertView.findViewById(R.id.textView1_title);
            vh.textView2_singer = (TextView) convertView.findViewById(R.id.textView2_singer);
            convertView.setTag(vh);
        }
        vh = (ViewHolder) convertView.getTag();
        SearchResult result = searchResults.get(position);
        vh.textView1_title.setText(result.getMusicName());
        vh.textView2_singer.setText(result.getArtist());


        return convertView;
    }

    static class ViewHolder {
        TextView textView1_title;
        TextView textView2_singer;
    }
}
