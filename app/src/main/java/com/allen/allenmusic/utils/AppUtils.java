package com.allen.allenmusic.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.allen.allenmusic.CodingkePlayerApp;

/**
 * Created by Allen on 16/3/22.
 */
public class AppUtils {
    public static void hideInputMethod(View view){
        InputMethodManager imm= (InputMethodManager) CodingkePlayerApp.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);}
    }
}
