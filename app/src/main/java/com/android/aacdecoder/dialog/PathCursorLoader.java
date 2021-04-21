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
import android.database.Cursor;
import android.os.Environment;
import android.support.v4.content.AsyncTaskLoader;
import java.io.File;

/**
 * 
 * path cursor loader.
 *
 * @author : Engineer-Jsp
 * @date : Created by 2021/01/28 15:43
 */
public class PathCursorLoader extends AsyncTaskLoader<Cursor> {

	/**
	 * {@link File} instance object
	 */
	private File mPath;

	/**
	 * construction method
	 * 
	 * @param context
	 *            {@link Context} instance object
	 */
	public PathCursorLoader(Context context) {
		this(context, Environment.getExternalStorageDirectory());
	}

	/**
	 * construction method
	 * 
	 * @param context
	 *            {@link Context} instance object
	 * @param path
	 *            file absolute path
	 */
	public PathCursorLoader(Context context, String path) {
		super(context);
		mPath = new File(path).getAbsoluteFile();
	}

	/**
	 * construction method
	 * 
	 * @param context
	 *            {@link Context} instance object
	 * @param path
	 *            {@link File} instance object
	 */
	public PathCursorLoader(Context context, File path) {
		super(context);
		mPath = path;
	}

	@Override
	public Cursor loadInBackground() {
		File[] file_list = mPath.listFiles();
		return new PathCursor(mPath, file_list);
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

}