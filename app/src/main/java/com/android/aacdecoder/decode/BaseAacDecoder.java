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
package com.android.aacdecoder.decode;

/**
 * base aac decoder.
 *
 * @author : Engineer-Jsp
 * @date : Created by 2021/1/28 13:54
 */
public abstract class BaseAacDecoder {

    /**
     * audio mime type
     */
    protected static final String AUDIO_MIME_TYE = "audio/";

    /**
     * video mime type
     */
    protected static final String VIDEO_MIME_TYE = "video/";

    /**
     * audio decode mime type
     */
    protected static final String AUDIO_DECODE_MIME_TYPE = "audio/mp4a-latm";

    /**
     * csd-0 mime type
     */
    protected static final String CSD_MIME_TYPE_0 = "csd-0";

    /**
     * csd-1 mime type
     */
    protected static final String CSD_MIME_TYPE_1 = "csd-1";

    /**
     * start decode
     */
    public abstract void start();

    /**
     * if return true means currently is decoding
     *
     * @return
     */
    public abstract boolean isDecoding();

    /**
     * stop decode
     */
    public abstract void stop();

    /**
     * release decoder
     */
    public abstract void release();

}
