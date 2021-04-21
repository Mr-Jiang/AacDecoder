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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.android.aacdecoder.R;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

/**
 * local video thumbnail create activity.
 *
 * @author : Engineer-Jsp
 * @date : Created by 2021/2/25 14:57
 */
public class ThumbnailActivity extends AppCompatActivity implements ThumbnailBuildTask.Callback {

    /**
     * thumbnail adapter
     */
    private ThumbnailAdapter thumbnailAdapter;

    /**
     * {@link GridView}
     */
    private GridView gridView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        gridView = (GridView) findViewById(R.id.thumbnail_grid);
        thumbnailAdapter = new ThumbnailAdapter();
        gridView.setAdapter(thumbnailAdapter);

        String videoDir = getIntent().getStringExtra("videoDir");
        if (!TextUtils.isEmpty(videoDir)) {

            File file = new File(videoDir);
            File[] files = file.listFiles();
            ArrayList<AlbumFile> videoFileList = new ArrayList<>();

            for (File f : files) {
                if (f.getAbsolutePath().endsWith(AlbumFile.FILENAME_END_WITH)) {
                    AlbumFile albumFile = new AlbumFile();
                    albumFile.setMediaType(AlbumFile.TYPE_VIDEO);
                    albumFile.setPath(f.getAbsolutePath());
                    videoFileList.add(albumFile);
                }
            }
            if (videoFileList.size() > 0) {
                callbackResult(videoFileList);
            }
        }
    }

    /**
     * Callback result action.
     */
    private void callbackResult(ArrayList<AlbumFile> videoFileList) {
        ThumbnailBuildTask task = new ThumbnailBuildTask(this, videoFileList, this);
        task.execute();
    }

    @Override
    public void onThumbnailStart() {

    }

    @Override
    public void onThumbnailCallback(ArrayList<AlbumFile> albumFiles) {
        if (thumbnailAdapter != null) {
            thumbnailAdapter.notifyDataSetChanged(albumFiles);
        }
    }

    private static class ThumbnailAdapter extends BaseAdapter {

        private ArrayList<AlbumFile> albumFiles = new ArrayList<>();

        @Override
        public int getCount() {
            return albumFiles.size();
        }

        @Override
        public Object getItem(int position) {
            return albumFiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                view = inflater.inflate(R.layout.grid_item, parent, false);
            }
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder();
                viewHolder.grid_item = (ImageView) view.findViewById(R.id.grid_item_img);
            }
            if (albumFiles.size() > 0) {
                String thumbnailUrl = albumFiles.get(position).getThumbPath();
                Log.d("thumbnail", "thumbnailUrl = " + thumbnailUrl);
                Glide.with(viewHolder.grid_item.getContext())
                        .load(thumbnailUrl)
                        .placeholder(R.drawable.ic_theme_play_arrow)
                        .into(viewHolder.grid_item);
            }
            return view;
        }

        /**
         * notify data set changed
         *
         * @param albumFilesTemp
         */
        private void notifyDataSetChanged(ArrayList<AlbumFile> albumFilesTemp) {
            if (albumFilesTemp != null && albumFilesTemp.size() > 0) {
                if (albumFiles.size() > 0) {
                    albumFiles.clear();
                }
                albumFiles.addAll(albumFilesTemp);
                notifyDataSetChanged();
                Log.d("thumbnail", "get thumbnail successful.");
            }
        }

        private class ViewHolder {
            private ImageView grid_item;
        }

    }

}
