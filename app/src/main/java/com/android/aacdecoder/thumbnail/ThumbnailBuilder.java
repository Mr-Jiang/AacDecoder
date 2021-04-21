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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.HashMap;

/**
 * local video thumbnail create builder.
 *
 * @author : Engineer-Jsp
 * @date : Created by 2021/2/25 15:00
 */
public class ThumbnailBuilder {

    /**
     * thumbnail cache directory
     */
    private static final String CACHE_DIRECTORY = "ThumbnailCache";

    /**
     * thumbnail size
     */
    private static final int THUMBNAIL_SIZE = 360;

    /**
     * thumbnail quality
     */
    private static final int THUMBNAIL_QUALITY = 80;

    /**
     * my cache directory
     */
    private File mCacheDir;

    /**
     * thumbnail create builder construction.
     *
     * @param context {@link Context}
     */
    public ThumbnailBuilder(Context context) {
        this.mCacheDir = getAlbumRootPath(context);
        if (mCacheDir.exists() && mCacheDir.isFile()) {
            mCacheDir.delete();
        }
        if (!mCacheDir.exists()) {
            mCacheDir.mkdirs();
        }
    }

    /**
     * Get a writable root directory.
     *
     * @param context context.
     * @return {@link File}.
     */
    public static File getAlbumRootPath(Context context) {
        if (sdCardIsAvailable()) {
            return new File(Environment.getExternalStorageDirectory(), CACHE_DIRECTORY);
        } else {
            return new File(context.getFilesDir(), CACHE_DIRECTORY);
        }
    }

    /**
     * SD card is available.
     *
     * @return true when available, other wise is false.
     */
    public static boolean sdCardIsAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().canWrite();
        } else {
            return false;
        }
    }

    /**
     * Create a thumbnail for the image.
     *
     * @param imagePath image path.
     * @return thumbnail path.
     */
    public String createThumbnailForImage(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            return null;
        }
        File inFile = new File(imagePath);
        if (!inFile.exists()) {
            return null;
        }
        File thumbnailFile = randomPath(imagePath);
        if (thumbnailFile.exists()) {
            return thumbnailFile.getAbsolutePath();
        }
        Bitmap inBitmap = readImageFromPath(imagePath, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        if (inBitmap == null) {
            return null;
        }
        ByteArrayOutputStream compressStream = new ByteArrayOutputStream();
        inBitmap.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, compressStream);
        try {
            compressStream.close();
            thumbnailFile.createNewFile();
            FileOutputStream writeStream = new FileOutputStream(thumbnailFile);
            writeStream.write(compressStream.toByteArray());
            writeStream.flush();
            writeStream.close();
            return thumbnailFile.getAbsolutePath();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Deposit in the province read images, mViewWidth is high, the greater the picture clearer, but also the memory.
     *
     * @param imagePath pictures in the path of the memory card.
     * @return bitmap.
     */
    public static Bitmap readImageFromPath(String imagePath, int width, int height) {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            try {
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(imageFile));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();

                options.inJustDecodeBounds = false;
                options.inSampleSize = computeSampleSize(options, width, height);

                Bitmap sampledBitmap = null;
                boolean attemptSuccess = false;
                while (!attemptSuccess) {
                    inputStream = new BufferedInputStream(new FileInputStream(imageFile));
                    try {
                        sampledBitmap = BitmapFactory.decodeStream(inputStream, null, options);
                        attemptSuccess = true;
                    } catch (Exception e) {
                        options.inSampleSize *= 2;
                    }
                    inputStream.close();
                }

                String lowerPath = imagePath.toLowerCase();
                if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
                    int degrees = computeDegree(imagePath);
                    if (degrees > 0) {
                        Matrix matrix = new Matrix();
                        matrix.setRotate(degrees);
                        Bitmap newBitmap = Bitmap.createBitmap(sampledBitmap, 0, 0, sampledBitmap.getWidth(), sampledBitmap.getHeight(), matrix, true);
                        if (newBitmap != sampledBitmap) {
                            sampledBitmap.recycle();
                            sampledBitmap = newBitmap;
                        }
                    }
                }
                return sampledBitmap;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static int computeSampleSize(BitmapFactory.Options options, int width, int height) {
        int inSampleSize = 1;
        if (options.outWidth > width || options.outHeight > height) {
            int widthRatio = Math.round((float) options.outWidth / (float) width);
            int heightRatio = Math.round((float) options.outHeight / (float) height);
            inSampleSize = Math.min(widthRatio, heightRatio);
        }
        return inSampleSize;
    }

    private static int computeDegree(String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90: {
                    return 90;
                }
                case ExifInterface.ORIENTATION_ROTATE_180: {
                    return 180;
                }
                case ExifInterface.ORIENTATION_ROTATE_270: {
                    return 270;
                }
                default: {
                    return 0;
                }
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Create a thumbnail for the video.
     *
     * @param videoPath video path.
     * @return thumbnail path.
     */
    public String createThumbnailForVideo(String videoPath) {
        if (TextUtils.isEmpty(videoPath)) {
            return null;
        }
        File thumbnailFile = randomPath(videoPath);
        if (thumbnailFile.exists()) {
            return thumbnailFile.getAbsolutePath();
        }
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            if (URLUtil.isNetworkUrl(videoPath)) {
                retriever.setDataSource(videoPath, new HashMap<String, String>());
            } else {
                retriever.setDataSource(videoPath);
            }
            Bitmap bitmap = retriever.getFrameAtTime();
            thumbnailFile.createNewFile();
            bitmap.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, new FileOutputStream(thumbnailFile));
            return thumbnailFile.getAbsolutePath();
        } catch (Exception ignored) {
            Log.d("thumbnail", ignored.toString());
            return null;
        }
    }

    private File randomPath(String filePath) {
        String outFilePath = getMD5ForString(filePath) + ".album";
        return new File(mCacheDir, outFilePath);
    }

    /**
     * Get the MD5 value of string.
     *
     * @param content the target string.
     * @return the MD5 value.
     */
    public static String getMD5ForString(String content) {
        StringBuilder md5Buffer = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] tempBytes = digest.digest(content.getBytes());
            int digital;
            for (int i = 0; i < tempBytes.length; i++) {
                digital = tempBytes[i];
                if (digital < 0) {
                    digital += 256;
                }
                if (digital < 16) {
                    md5Buffer.append("0");
                }
                md5Buffer.append(Integer.toHexString(digital));
            }
        } catch (Exception ignored) {
            return Integer.toString(content.hashCode());
        }
        return md5Buffer.toString();
    }

}
