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
package com.android.aacdecoder;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.aacdecoder.decode.LocalAacDecoder;
import com.android.aacdecoder.decode.RealTimeAacDecoder;
import com.android.aacdecoder.dialog.FileListDialog;
import com.android.aacdecoder.thumbnail.AlbumFile;
import com.android.aacdecoder.thumbnail.ThumbnailActivity;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * aac audio decode main activity
 *
 * @author : Engineer-Jsp
 * @date : Created by 2021/01/28 12:51
 */
public class MainActivity extends AppCompatActivity {

    /**
     * {@link LocalAacDecoder} local aac audio decoder instance object
     */
    private LocalAacDecoder localAacDecoder;

    /**
     * {@link RealTimeAacDecoder} realtime aac audio decoder instance object
     */
    private RealTimeAacDecoder realTimeAacDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * local aac audio decode button pressed callback
     *
     * @param view {@link View} instance object
     */
    public void localAacDecode(View view) {
        selectVideoFile();
    }

    /**
     * select local mp4 format video file
     */
    private void selectVideoFile() {
        final FileListDialog dialog = new FileListDialog(MainActivity.this, getSupportLoaderManager());
        // true means if touch outside of the dialog , dialog need close
        // else needn't close
        dialog.setCancelable(true);
        dialog.setOnFileItemClickListener(new FileListDialog.FileItemClickListener() {
            @Override
            public void onFileItemClick(String path) {
                dialog.dismiss();
                if (path.endsWith(AlbumFile.FILENAME_END_WITH)) {
                    if (localAacDecoder == null) {
                        localAacDecoder = new LocalAacDecoder();
                    }
                    if (!localAacDecoder.isDecoding()) {
                        localAacDecoder.setDecodeFilePath(path);
                        localAacDecoder.start();
                        return;
                    }
                    Toast.makeText(MainActivity.this, "currently is decoding , please wait for litter try again!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Only support mp4 file format.", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        dialog.show();
    }

    /**
     * realtime aac audio decode button pressed callback
     *
     * @param view {@link View} instance object
     */
    public void realTimeAacDecode(View view) {
        if (realTimeAacDecoder == null) {
            realTimeAacDecoder = new RealTimeAacDecoder();
            realTimeAacDecoder.start();
        }
    }

    /**
     * local video thumbnail create
     *
     * @param view {@link View} instance object
     */
    public void videoThumbnailCrete(View view) {
        selectVideoFileDirectory();
    }

    /**
     * select local mp4 format video file directory
     */
    private void selectVideoFileDirectory() {
        final FileListDialog dialog = new FileListDialog(MainActivity.this, getSupportLoaderManager());
        // true means if touch outside of the dialog , dialog need close
        // else needn't close
        dialog.setCancelable(true);
        dialog.setOnFileItemClickListener(new FileListDialog.FileItemClickListener() {
            @Override
            public void onFileItemClick(String path) {
                dialog.dismiss();
                File file = new File(path);
                if (file.exists() /*&& file.isDirectory()*/) {
                    file = file.getParentFile();
                    path = file.getAbsolutePath();
                    File[] files = file.listFiles();
                    for (File f : files) {
                        if (f.getAbsolutePath().endsWith(AlbumFile.FILENAME_END_WITH)) {
                            Intent intent = new Intent(MainActivity.this, ThumbnailActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("videoDir", path);
                            MainActivity.this.startActivity(intent);
                            return;
                        }
                    }
                    Toast.makeText(MainActivity.this, "this directory is not exist mp4 format video file.", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(MainActivity.this, "file not exist or file non-directory.", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localAacDecoder != null) {
            localAacDecoder.release();
        }
        if (realTimeAacDecoder != null) {
            realTimeAacDecoder.release();
        }
    }


}