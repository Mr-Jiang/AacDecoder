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

import android.database.AbstractCursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * path cursor.
 *
 * @author : Engineer-Jsp
 * @date : Created by 2021/01/28 15:44
 */
public class PathCursor extends AbstractCursor {

	/**
	 * {@link FileItem} file item list
	 */
	private List<FileItem> mFileList = new ArrayList<FileItem>();

	/**
	 * file auto increment primary key
	 */
	public static final String CN_ID = BaseColumns._ID;

	/**
	 * file name column
	 */
	public static final String CN_FILE_NAME = "file_name";

	/**
	 * file path column
	 */
	public static final String CN_FILE_PATH = "file_path";

	/**
	 * determine whether it is a directory
	 */
	public static final String CN_IS_DIRECTORY = "is_directory";

	/**
	 * determine whether it is a video
	 */
	public static final String CN_IS_VIDEO = "is_video";

	/**
	 * file column array
	 */
	public static final String[] columnNames = new String[] { CN_ID, CN_FILE_NAME, CN_FILE_PATH, CN_IS_DIRECTORY,
			CN_IS_VIDEO };

	/**
	 * file auto increment primary key integer value
	 */
	public static final int CI_ID = 0;

	/**
	 * file name column integer value
	 */
	public static final int CI_FILE_NAME = 1;

	/**
	 * file path column integer value
	 */
	public static final int CI_FILE_PATH = 2;

	/**
	 * determine whether it is a directory integer value
	 */
	public static final int CI_IS_DIRECTORY = 3;

	/**
	 * determine whether it is a video integer value
	 */
	public static final int CI_IS_VIDEO = 4;

	/**
	 * construction method
	 * 
	 * @param parentDirectory
	 *            {@link File} parent directory
	 * @param fileList
	 *            {@link File} array
	 */
	PathCursor(File parentDirectory, File[] fileList) {
		if (parentDirectory.getParent() != null) {
			FileItem parentFile = new FileItem(new File(parentDirectory, ".."));
			parentFile.isDirectory = true;
			mFileList.add(parentFile);
		}

		if (fileList != null) {
			for (File file : fileList) {
				mFileList.add(new FileItem(file));
			}
			Collections.sort(this.mFileList, sComparator);
		}
	}

	@Override
	public int getCount() {
		return mFileList.size();
	}

	@Override
	public String[] getColumnNames() {
		return columnNames;
	}

	@Override
	public String getString(int column) {
		switch (column) {
		case CI_FILE_NAME:
			return mFileList.get(getPosition()).file.getName();
		case CI_FILE_PATH:
			return mFileList.get(getPosition()).file.toString();
		}
		return null;
	}

	@Override
	public short getShort(int column) {
		return (short) getLong(column);
	}

	@Override
	public int getInt(int column) {
		return (int) getLong(column);
	}

	@Override
	public long getLong(int column) {
		switch (column) {
		case CI_ID:
			return getPosition();
		case CI_IS_DIRECTORY:
			return mFileList.get(getPosition()).isDirectory ? 1 : 0;
		case CI_IS_VIDEO:
			return mFileList.get(getPosition()).isVideo ? 1 : 0;
		}
		return 0;
	}

	@Override
	public float getFloat(int column) {
		return 0;
	}

	@Override
	public double getDouble(int column) {
		return 0;
	}

	@Override
	public boolean isNull(int column) {
		return mFileList == null;
	}

	/**
	 * file sort
	 */
	public static Comparator<FileItem> sComparator = new Comparator<FileItem>() {
		@Override
		public int compare(FileItem lhs, FileItem rhs) {
			if (lhs.isDirectory && !rhs.isDirectory){
				return -1;
			}
			else if (!lhs.isDirectory && rhs.isDirectory){
				return 1;
			}
			return lhs.file.getName().compareToIgnoreCase(rhs.file.getName());
		}
	};

	/**
	 * video file suffix name
	 */
	private static Set<String> sMediaExtSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

	static {
		sMediaExtSet.add("flv");
		sMediaExtSet.add("mp4");
	}

	/**
	 * file item class define
	 */
	private class FileItem {

		/**
		 * {@link File} instance object
		 */
		public File file;

		/**
		 * determine whether it is a directory
		 */
		public boolean isDirectory;

		/**
		 * determine whether it is a video
		 */
		public boolean isVideo;

		/**
		 * construction method
		 * 
		 * @param file
		 *            file absolute path
		 */
		@SuppressWarnings("unused")
		public FileItem(String file) {
			this(new File(file));
		}

		/**
		 * construction method
		 * 
		 * @param file
		 *            {@link File} instance object
		 */
		public FileItem(File file) {
			this.file = file;
			this.isDirectory = file.isDirectory();

			String fileName = file.getName();
			if (!TextUtils.isEmpty(fileName)) {
				int extPos = fileName.lastIndexOf('.');
				if (extPos >= 0) {
					String ext = fileName.substring(extPos + 1);
					if (!TextUtils.isEmpty(ext) && sMediaExtSet.contains(ext)) {
						this.isVideo = true;
					}
				}
			}
		}
	}

}