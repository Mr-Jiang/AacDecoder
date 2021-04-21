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
 * the company otherwise legal liability will be pursued according to law.
 */
package com.android.aacdecoder.thumbnail;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * local video thumbnail create builder async task.
 *
 * @author : Engineer-Jsp
 * @date : Created by 2021/2/25 15:50
 */
public class ThumbnailBuildTask extends AsyncTask<Void, Void, ArrayList<AlbumFile>> {

    public interface Callback {
        /**
         * The task begins.
         */
        void onThumbnailStart();

        /**
         * Callback results.
         *
         * @param albumFiles result.
         */
        void onThumbnailCallback(ArrayList<AlbumFile> albumFiles);
    }

    private ArrayList<AlbumFile> mAlbumFiles;
    private Callback mCallback;

    private ThumbnailBuilder mThumbnailBuilder;

    public ThumbnailBuildTask(Context context, ArrayList<AlbumFile> albumFiles, Callback callback) {
        this.mAlbumFiles = albumFiles;
        this.mCallback = callback;
        this.mThumbnailBuilder = new ThumbnailBuilder(context);
    }

    @Override
    protected void onPreExecute() {
        mCallback.onThumbnailStart();
    }

    @Override
    protected ArrayList<AlbumFile> doInBackground(Void... params) {
        for (AlbumFile albumFile : mAlbumFiles) {
            int mediaType = albumFile.getMediaType();
            if (mediaType == AlbumFile.TYPE_IMAGE) {
                albumFile.setThumbPath(mThumbnailBuilder.createThumbnailForImage(albumFile.getPath()));
            } else if (mediaType == AlbumFile.TYPE_VIDEO) {
                albumFile.setThumbPath(mThumbnailBuilder.createThumbnailForVideo(albumFile.getPath()));
            }
        }
        return mAlbumFiles;
    }

    @Override
    protected void onPostExecute(ArrayList<AlbumFile> albumFiles) {
        mCallback.onThumbnailCallback(albumFiles);
    }
    
}
