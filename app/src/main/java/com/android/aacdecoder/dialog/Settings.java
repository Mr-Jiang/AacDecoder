/*
 * Copyright (c) 2020. Engineer-Jsp
 *
 * Any document of this project is owned by Engineer-Jsp;
 * Without the permission of the company, it is forbidden to send any
 * documents of this project to anyone who has nothing to do with the project.
 * About the project author , You can visit he's other open source projects or he's blog
 *
 * CSDN   : https://blog.csdn.net/jspping
 * GitHub : https://github.com/Mr-Jiang
 *
 * Once again explanation, it is forbidden to disclose any documents of the project
 * to anyone who has nothing to do with the project without the permission of
 * the Engineer-Jsp otherwise legal liability will be pursued according to law.
 */
package com.android.aacdecoder.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.aacdecoder.R;

/**
 * shared preferences secondary packaging class.
 *
 * @author : Engineer-Jsp
 * @date : Created by 2021/01/28 15:43
 */
public class Settings {

    /**
     * {@link Context} instance object
     */
    private Context mAppContext;

    /**
     * {@link SharedPreferences} instance object
     */
    private SharedPreferences mSharedPreferences;

    /**
     * construction method , initialization {@link SharedPreferences} instance
     * object
     *
     * @param context {@link Context} instance object
     */
    public Settings(Context context) {
        mAppContext = context.getApplicationContext();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
    }

    /**
     * get last directory
     *
     * @return {@link String} last directory string
     */
    public String getLastDirectory() {
        String key = mAppContext.getString(R.string.pref_key_last_directory);
        return mSharedPreferences.getString(key, "");
    }

    /**
     * save last directory
     *
     * @param path file absolute path
     */
    public void setLastDirectory(String path) {
        String key = mAppContext.getString(R.string.pref_key_last_directory);
        mSharedPreferences.edit().putString(key, path).apply();
    }

}