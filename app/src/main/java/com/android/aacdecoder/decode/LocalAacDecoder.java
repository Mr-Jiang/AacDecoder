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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * local aac audio decoder.
 *
 * @author : Engineer-Jsp
 * @date : Created by 2021/1/28 13:29
 */
public class LocalAacDecoder extends BaseAacDecoder implements Runnable {

    /**
     * debug log output tag
     */
    private static final String TAG = "LocalAacDecoder";

    /**
     * decoding flag , true means currently is decoding , default false.
     */
    private boolean isDecoding;

    /**
     * aac audio file decode thread
     */
    private Thread worker;

    /**
     * {@link MediaExtractor} media extractor instance object
     */
    private MediaExtractor mediaExtractor;

    /**
     * {@link MediaCodec} aac decoder
     */
    private MediaCodec aacDecoder;

    /**
     * aac audio adts header
     */
    private byte[] adtsAudioHeader;

    /**
     * audio sample rate
     */
    private int sampleRate;

    /**
     * audio channel count
     */
    private int channelCount;

    /**
     * audio output channel , author default AudioFormat.CHANNEL_OUT_MONO
     */
    private int channelConfig = AudioFormat.CHANNEL_OUT_MONO;

    /**
     * determine whether it is low version
     */
    private boolean isLowVersion = false;

    /**
     * set decode file path
     *
     * @param decodeFilePath decode file path
     */
    public void setDecodeFilePath(String decodeFilePath) {
        if (TextUtils.isEmpty(decodeFilePath)) {
            throw new RuntimeException("decode file path must not be null!");
        }
        mediaExtractor = getMediaExtractor(decodeFilePath);
    }

    /**
     * get media extractor
     *
     * @param videoPath need extract of tht video file absolute path
     * @return {@link MediaExtractor} media extractor instance object
     * @throws IOException
     */
    protected MediaExtractor getMediaExtractor(String videoPath) {
        MediaExtractor mMediaExtractor = new MediaExtractor();
        try {
            // set file path
            mMediaExtractor.setDataSource(videoPath);
            // get source file track count
            int trackCount = mMediaExtractor.getTrackCount();
            for (int i = 0; i < trackCount; i++) {
                // get current media track media format
                MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(i);
                // if media format object not be null
                if (mediaFormat != null) {
                    // get media mime type
                    String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
                    // media mime type match audio
                    if (mimeType.startsWith(AUDIO_MIME_TYE)) {
                        // set media track is audio
                        mMediaExtractor.selectTrack(i);
                        // you can using media format object call getByteBuffer method and input key "csd-0" get it value , if you want.
                        // it is aac adts audio header.
                        adtsAudioHeader = mediaFormat.getByteBuffer(CSD_MIME_TYPE_0).array();
                        // get audio sample
                        sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                        // get audio channel count
                        channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                        return mMediaExtractor;
                    }
                    // >>>>>>>>>>> expand start >>>>>>>>>>>
                    // media mime type match video
                    // else if (mimeType.startsWith(VIDEO_MIME_TYE)) {
                    // get video sps header
                    // byte[] spsVideoHeader = mediaFormat.getByteBuffer(CSD_MIME_TYPE_0).array();
                    // get video pps header
                    // byte[] ppsVideoHeader = mediaFormat.getByteBuffer(CSD_MIME_TYPE_1).array();
                    // }
                    // <<<<<<<<<<< expand end <<<<<<<<<<<
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "happened io exception : " + e.toString());
            if (mMediaExtractor != null) {
                mMediaExtractor.release();
            }
        }
        return null;
    }

    /**
     * create default aac decoder
     *
     * @return {@link MediaCodec} aac audio decoder
     */
    private MediaCodec createDefaultDecoder() {
        try {
            MediaFormat mediaFormat = new MediaFormat();
            mediaFormat.setString(MediaFormat.KEY_MIME, AUDIO_DECODE_MIME_TYPE);
            mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
            mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);
            ByteBuffer byteBuffer = ByteBuffer.allocate(adtsAudioHeader.length);
            byteBuffer.put(adtsAudioHeader);
            byteBuffer.flip();
            mediaFormat.setByteBuffer(CSD_MIME_TYPE_0, byteBuffer);
            MediaCodec aacDecoder = MediaCodec.createDecoderByType(AUDIO_DECODE_MIME_TYPE);
            aacDecoder.configure(mediaFormat, null, null, 0);
            aacDecoder.start();
            return aacDecoder;
        } catch (IOException e) {
            Log.e(TAG, "create aac audio decoder happened io exception : " + e.toString());
        }
        return null;
    }

    @Override
    public void start() {
        if (mediaExtractor == null) {
            Log.e(TAG, "media extractor is null , so return!");
            return;
        }
        if (adtsAudioHeader == null || adtsAudioHeader.length == 0) {
            Log.e(TAG, "aac audio adts header is null , so return!");
            return;
        }
        aacDecoder = createDefaultDecoder();
        if (aacDecoder == null) {
            Log.e(TAG, "aac audio decoder is null , so return!");
            return;
        }
        if (worker == null) {
            isDecoding = true;
            worker = new Thread(this, TAG);
            worker.start();
        }
    }

    @Override
    public boolean isDecoding() {
        return isDecoding;
    }

    @Override
    public void stop() {
        isDecoding = false;
        if (worker != null) {
            worker.interrupt();
            try {
                worker.join();
            } catch (InterruptedException e) {
                worker.interrupt();
            }
            worker = null;
        }
    }

    @Override
    public void release() {
        stop();
    }

    @Override
    public void run() {
        aacDecodeToPcm();
    }

    /**
     * aac audio format decode to pcm audi format
     */
    private void aacDecodeToPcm() {
        isLowVersion = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP;
        ByteBuffer[] aacDecodeInputBuffers = null;
        ByteBuffer[] aacDecodeOutputBuffers = null;
        if (isLowVersion) {
            aacDecodeInputBuffers = aacDecoder.getInputBuffers();
            aacDecodeOutputBuffers = aacDecoder.getOutputBuffers();
        }
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        // initialization audio track , use for play pcm audio data
        // audio output channel param channelConfig according device support select
        int buffsize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig,
                AudioFormat.ENCODING_PCM_16BIT, buffsize, AudioTrack.MODE_STREAM);
        audioTrack.play();

        Log.d(TAG, "aac audio decode thread start");
        while (isDecoding) {
            // This method will return immediately if timeoutUs == 0
            // wait indefinitely for the availability of an input buffer if timeoutUs < 0
            // wait up to "timeoutUs" microseconds if timeoutUs > 0.
            int aacDecodeInputBuffersIndex = aacDecoder.dequeueInputBuffer(2000);
            // no such buffer is currently available , if aacDecodeInputBuffersIndex is -1
            if (aacDecodeInputBuffersIndex >= 0) {
                ByteBuffer sampleDataBuffer;
                if (isLowVersion) {
                    sampleDataBuffer = aacDecodeInputBuffers[aacDecodeInputBuffersIndex];
                } else {
                    sampleDataBuffer = aacDecoder.getInputBuffer(aacDecodeInputBuffersIndex);
                }
                int sampleDataSize = mediaExtractor.readSampleData(sampleDataBuffer, 0);
                if (sampleDataSize < 0) {
                    aacDecoder.queueInputBuffer(aacDecodeInputBuffersIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                } else {
                    try {
                        long presentationTimeUs = mediaExtractor.getSampleTime();
                        aacDecoder.queueInputBuffer(aacDecodeInputBuffersIndex, 0, sampleDataSize, presentationTimeUs, 0);
                        mediaExtractor.advance();
                    } catch (Exception e) {
                        Log.e(TAG, "aac decode to pcm happened Exception : " + e.toString());
                        continue;
                    }
                }

                int aacDecodeOutputBuffersIndex = aacDecoder.dequeueOutputBuffer(info, 2000);
                if (aacDecodeOutputBuffersIndex >= 0) {
                    if (((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)) {
                        Log.d(TAG, "aac decode thread read sample data done!");
                        break;
                    } else {
                        ByteBuffer pcmOutputBuffer;
                        if (isLowVersion) {
                            pcmOutputBuffer = aacDecodeOutputBuffers[aacDecodeOutputBuffersIndex];
                        } else {
                            pcmOutputBuffer = aacDecoder.getOutputBuffer(aacDecodeOutputBuffersIndex);
                        }
                        ByteBuffer copyBuffer = ByteBuffer.allocate(pcmOutputBuffer.remaining());
                        copyBuffer.put(pcmOutputBuffer);
                        copyBuffer.flip();

                        final byte[] pcm = new byte[info.size];
                        copyBuffer.get(pcm);
                        copyBuffer.clear();
                        audioTrack.write(pcm, 0, info.size);
                        aacDecoder.releaseOutputBuffer(aacDecodeOutputBuffersIndex, false);
                    }
                }
            }
        }
        Log.d(TAG, "aac audio decode thread stop");

        if (aacDecoder != null) {
            aacDecoder.stop();
            aacDecoder.release();
            aacDecoder = null;
        }
        if (mediaExtractor != null) {
            mediaExtractor.release();
            mediaExtractor = null;
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
        isDecoding = false;
        worker = null;
    }

}
