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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.aacdecoder.R;

import java.io.File;
import java.io.IOException;

/**
 * local file select dialog.
 *
 * @author : Engineer-Jsp
 * @date : Created by 2021/01/28 15:42
 */
public class FileListDialog extends Dialog implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "FileListDialog";

    /**
     * {@link Context} instance object
     */
    private Context context;

    /**
     * {@link TextView} instance object
     */
    private TextView mPathView;

    /**
     * {@link LoaderManager} instance object
     */
    private LoaderManager manager;

    /**
     * {@link FileListAdapter} instance object , used to load file list data
     */
    private FileListAdapter mAdapter;

    /**
     * {@link Settings} instance object
     */
    private Settings mSettings;

    /**
     * video file absolute path
     */
    private String mPath;

    /**
     * {@link FileItemClickListener} instance object
     */
    private FileItemClickListener mFileItemClickListener;

    /**
     * construction method , initialization {@link Settings} instance object
     *
     * @param context {@link Context} instance object
     * @param manager {@link LoaderManager} instance object
     */
    public FileListDialog(Context context, LoaderManager manager) {
        super(context, R.style.FileListDialog);
        this.context = context;
        this.manager = manager;
        mSettings = new Settings(context);
    }

    /**
     * set file item click listener
     *
     * @param mFileItemClickListener {@link FileItemClickListener} instance object
     */
    public void setOnFileItemClickListener(FileItemClickListener mFileItemClickListener) {
        this.mFileItemClickListener = mFileItemClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    /**
     * initialization view
     */
    @SuppressLint("InflateParams")
    private void initView() {

        View view = LayoutInflater.from(context).inflate(R.layout.file_list_dialog, null);
        setContentView(view);
        mPathView = (TextView) view.findViewById(R.id.path_view);

        ListView mFileListView = (ListView) view.findViewById(R.id.file_list_view);
        mPathView.setVisibility(View.VISIBLE);

        mAdapter = new FileListAdapter(context);
        mFileListView.setAdapter(mAdapter);
        mFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                if (mFileItemClickListener != null) {
                    String path = mAdapter.getFilePath(position);
                    if (TextUtils.isEmpty(path)) {
                        return;
                    }
                    perfromOpenDirectory(path);
                }
            }
        });

        Window dialogWindow = getWindow();
        assert dialogWindow != null;
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.8);
        dialogWindow.setAttributes(lp);

        mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mPathView.setText(mPath);
        String lastDirectory = mSettings.getLastDirectory();
        if (!TextUtils.isEmpty(lastDirectory) && new File(lastDirectory).isDirectory()) {
            mPath = lastDirectory;
            mPathView.setText(mPath);
        }
        manager.initLoader(1, null, this);
    }

    /**
     * perform open file or open directory
     *
     * @param path file or directory absolute path
     */
    private void perfromOpenDirectory(String path) {
        File f = new File(path);

        try {
            f = f.getAbsoluteFile();
            f = f.getCanonicalFile();
            if (TextUtils.isEmpty(f.toString())) {
                f = new File("/");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (f.isDirectory()) {
            String tempPath = f.toString();
            mSettings.setLastDirectory(tempPath);
            mPath = tempPath;
            mPathView.setText(mPath);
            manager.restartLoader(1, null, this);
        } else if (f.exists()) {
            mFileItemClickListener.onFileItemClick(path);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader...");
        if (TextUtils.isEmpty(mPath)) {
            return null;
        }
        return new PathCursorLoader(context, mPath);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished...");
        mAdapter.swapCursor(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset...");
    }

    /**
     * file list adapter.
     */
    final static class FileListAdapter extends SimpleCursorAdapter {

        /**
         * view holder
         */
        final static class ViewHolder {

            /**
             * file icon
             */
            public ImageView iconImageView;

            /**
             * file name
             */
            public TextView nameTextView;
        }

        /**
         * construction method , initialization file list adapter.
         *
         * @param context {@link Context} instance object
         */
        public FileListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_2, null,
                    new String[]{PathCursor.CN_FILE_NAME, PathCursor.CN_FILE_PATH},
                    new int[]{android.R.id.text1, android.R.id.text2}, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                view = inflater.inflate(R.layout.file_list_item, parent, false);
            }

            ViewHolder viewHolder = (ViewHolder) view.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder();
                viewHolder.iconImageView = (ImageView) view.findViewById(R.id.icon);
                viewHolder.nameTextView = (TextView) view.findViewById(R.id.name);
            }

            if (isDirectory(position)) {
                viewHolder.iconImageView.setImageResource(R.drawable.ic_theme_folder);
            } else if (isVideo(position)) {
                viewHolder.iconImageView.setImageResource(R.drawable.ic_theme_play_arrow);
            } else {
                viewHolder.iconImageView.setImageResource(R.drawable.ic_theme_description);
            }
            viewHolder.nameTextView.setText(getFileName(position));
            return view;
        }

        @Override
        public long getItemId(int position) {
            final Cursor cursor = moveToPosition(position);
            if (cursor == null) {
                return 0;
            }
            return cursor.getLong(PathCursor.CI_ID);
        }

        /**
         * move to position
         *
         * @param position file item position
         * @return {@link Cursor} databases cursor
         */
        Cursor moveToPosition(int position) {
            final Cursor cursor = getCursor();
            if (cursor.getCount() == 0 || position >= cursor.getCount()) {
                return null;
            }
            cursor.moveToPosition(position);
            return cursor;
        }

        /**
         * determine whether it is a directory
         *
         * @param position file item position
         * @return true means is directory
         */
        public boolean isDirectory(int position) {
            final Cursor cursor = moveToPosition(position);
            if (cursor == null) {
                return true;
            }
            return cursor.getInt(PathCursor.CI_IS_DIRECTORY) != 0;
        }

        /**
         * determine whether it is a video
         *
         * @param position file item position
         * @return true means is video
         */
        public boolean isVideo(int position) {
            final Cursor cursor = moveToPosition(position);
            if (cursor == null) {
                return true;
            }
            return cursor.getInt(PathCursor.CI_IS_VIDEO) != 0;
        }

        /**
         * get file name
         *
         * @param position file item position
         * @return {@link String} file name
         */
        public String getFileName(int position) {
            final Cursor cursor = moveToPosition(position);
            if (cursor == null) {
                return "";
            }
            return cursor.getString(PathCursor.CI_FILE_NAME);
        }

        /**
         * get file path
         *
         * @param position file item position
         * @return {@link String} file path
         */
        public String getFilePath(int position) {
            final Cursor cursor = moveToPosition(position);
            if (cursor == null) {
                return "";
            }
            return cursor.getString(PathCursor.CI_FILE_PATH);
        }
    }

    /**
     * file item click listener define.
     */
    public interface FileItemClickListener {

        /**
         * call back file item click event
         *
         * @param path local video file absolute path
         */
        void onFileItemClick(String path);
    }

}