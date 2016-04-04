package com.allen.allenmusic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.allen.allenmusic.utils.DownloadUtils;
import com.allen.allenmusic.value.SearchResult;

import java.io.File;

/**
 * Created by Allen on 16/3/22.
 */
public class DownloadDialogFragment extends DialogFragment {
    private SearchResult searchResult;
    private MainActivity mainActivity;
    private String[] items;

    public static DownloadDialogFragment newInstance(SearchResult searchResult){
        DownloadDialogFragment downloadDialogFragment=new DownloadDialogFragment();
        downloadDialogFragment.searchResult=searchResult;
        return downloadDialogFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity= (MainActivity) getActivity();
        items=new String[]{"下载","取消"};
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder=new AlertDialog.Builder(mainActivity);
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //执行下载
                        downloadMusic();
                        break;
                    case 1:
                        //取消
                        dialog.dismiss();
                        break;
                }
            }
        });
        return builder.show();
    }

    private void downloadMusic() {
        Toast.makeText(mainActivity,"正在下载:"+searchResult.getMusicName(),Toast.LENGTH_SHORT).show();
        DownloadUtils.getsInstance().setListener(new DownloadUtils.OnDownloadListener(){

            @Override
            public void onDownload(String mp3Url) {
                Toast.makeText(mainActivity,"歌曲下载成功",Toast.LENGTH_SHORT).show();
                //扫描下载的歌曲
//                Uri contenUri= Uri.fromFile(new File(mp3Url));
//                Intent mediaScanIntent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contenUri);
//                getContext().sendBroadcast(mediaScanIntent);
            }

            @Override
            public void onFailed(String error) {
              Toast.makeText(mainActivity,error,Toast.LENGTH_SHORT);
            }
        }).download(searchResult);

    }


}
