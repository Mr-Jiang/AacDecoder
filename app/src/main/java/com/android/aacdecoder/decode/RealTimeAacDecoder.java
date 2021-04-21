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
import android.media.MediaFormat;
import android.util.Log;

import com.android.aacdecoder.utils.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * realtime aac audio decoder.
 *
 * @author : Engineer-Jsp
 * @date : Created by 2021/1/28 13:30
 */
public class RealTimeAacDecoder extends BaseAacDecoder implements Runnable {

    /**
     * debug log output tag
     */
    private static final String TAG = "LocalAacDecoder";

    /**
     * adts header start flag , default 0xFFF
     */
    private static final int ADTS_HEADER_START_FLAG = 0xFFF;

    /**
     * adts header start flag bit size , default 12
     */
    private static final int ADTS_HEADER_START_FLAG_BIT_SIZE = 0x0C;

    /**
     * adts header audio profile flag , default 0x03
     */
    private static final int ADTS_HEADER_PROFILE_FLAG = 0x03;

    /**
     * adts header sampling frequency index flag , default 0x0F
     */
    private static final int ADTS_HEADER_SAMPLE_INDEX_FLAG = 0x0F;

    /**
     * adts header audio profile bit size , default 2
     */
    private static final int ADTS_HEADER_PROFILE_BIT_SIZE = 0x02;

    /**
     * adts header sampling frequency index bit size , default 4
     */
    private static final int ADTS_HEADER_SAMPLE_INDEX_BIT_SIZE = 0x04;

    /**
     * adts header channel config bit size , default 3
     */
    private static final int ADTS_HEADER_CHANNEL_CONFIG_BIT_SIZE = 0x03;

    /**
     * audio sample rate
     */
    private int sampleRate;

    /**
     * audio channel config
     */
    private int channelConfig;

    /**
     * aac audio adts header
     */
    private byte[] adtsAudioHeader;

    /**
     * decoding flag , true means currently is decoding , default false.
     */
    private boolean isDecoding;

    /**
     * aac audio file decode thread
     */
    private Thread worker;

    /**
     * {@link MediaCodec} aac decoder
     */
    private MediaCodec aacDecoder;

    /**
     * {@link AudioTrack} pcm audio data player
     */
    private AudioTrack audioTrack;

    /**
     * aac decode lock
     */
    private final Object decodeLock = new Object();

    /**
     * wait time sum
     */
    private long waitTimeSum;

    /**
     * sampling frequency int array
     */
    private static final int samplingFrequencys[] = {
            96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050,
            16000, 12000, 11025, 8000
    };

    /**
     * aac audio data cache queue
     */
    private ConcurrentLinkedQueue<byte[]> aacFrameQueue = new ConcurrentLinkedQueue<>();

    /**
     * check aac adts audio header
     *
     * @param aac aac audio data
     */
    private boolean checkAacAdtsHeader(byte[] aac) {
        byte[] dtsFixedHeader = new byte[2];
        System.arraycopy(aac, 0, dtsFixedHeader, 0, dtsFixedHeader.length);
        int bitMoveValue = dtsFixedHeader.length * 8 - ADTS_HEADER_START_FLAG_BIT_SIZE;
        int adtsFixedHeaderValue = bytesToInt(dtsFixedHeader);
        int syncwordValue = ADTS_HEADER_START_FLAG << bitMoveValue;
        boolean isAdtsHeader = (adtsFixedHeaderValue & syncwordValue) >> bitMoveValue == ADTS_HEADER_START_FLAG;
        if (!isAdtsHeader) {
            Log.e(TAG, "adts header start flag not match , so return!");
            return false;
        }
        System.arraycopy(aac, 2, dtsFixedHeader, 0, dtsFixedHeader.length);
        return parseAdtsHeaderKeyData(dtsFixedHeader);
    }

    /**
     * parse adts header key byte array data
     *
     * @param adtsHeaderValue adts fixed header byte array
     */
    private boolean parseAdtsHeaderKeyData(byte[] adtsHeaderValue) {
        int adtsFixedHeaderValue = bytesToInt(adtsHeaderValue);

        // bitMoveValue = 16(2 * 8) - 2(aac profile 3bit)
        int bitMoveValue = adtsHeaderValue.length * 8 - ADTS_HEADER_PROFILE_BIT_SIZE;
        // profile : 01 (aac profile) 2 bit
        int audioProfile = adtsFixedHeaderValue & (ADTS_HEADER_PROFILE_FLAG << bitMoveValue);
        // 1: AAC Main -- MediaCodecInfo.CodecProfileLevel.AACObjectMain
        // 2: AAC LC (Low Complexity)  -- MediaCodecInfo.CodecProfileLevel.AACObjectLC
        // 3: AAC SSR (Scalable Sample Rate) -- MediaCodecInfo.CodecProfileLevel.AACObjectSSR
        audioProfile = audioProfile >> bitMoveValue;

        // bitMoveValue = 16(2 * 8) - 2(aac profile 3bit) - 4(Sampling Frequency Index 4 bit)
        bitMoveValue -= ADTS_HEADER_SAMPLE_INDEX_BIT_SIZE;
        // Sampling Frequency Index : 1011 (value is 11，sample rate 8000) 4 bit
        int sampleIndex = adtsFixedHeaderValue & (ADTS_HEADER_SAMPLE_INDEX_FLAG << bitMoveValue);
        sampleIndex = sampleIndex >> bitMoveValue;
        sampleRate = samplingFrequencys[sampleIndex];

        // private bit ：0 (encoding set 0 ，decoding ignore) 1 bit
        // Channel Configuration : 001 (Channel Configuration) 3 bit
        // bitMoveValue = bitMoveValue - 1(private bit 1bit) +  3(Channel Configuration 3bit)
        bitMoveValue -= (1 + ADTS_HEADER_CHANNEL_CONFIG_BIT_SIZE);
        channelConfig = adtsFixedHeaderValue & (ADTS_HEADER_SAMPLE_INDEX_FLAG << bitMoveValue);
        channelConfig = channelConfig >> bitMoveValue;
        // ......
        // create csd-0(audio adts header)
        adtsAudioHeader = new byte[2];
        adtsAudioHeader[0] = (byte) ((audioProfile << 3) | (sampleIndex >> 1));
        adtsAudioHeader[1] = (byte) ((byte) ((sampleIndex << 7) & 0x80) | (channelConfig << 3));

        Log.d(TAG, "audioProfile = " + audioProfile + " , sampleIndex = " + sampleIndex + "(" + sampleRate + ")" + " , channelConfig = " + channelConfig
                + " , audio csd-0 = " + Utils.bytesToHexStringNo0xChar(adtsAudioHeader));

        return createDefaultDecoder();
    }

    /**
     * create default decoder
     */
    private boolean createDefaultDecoder() {
        if (adtsAudioHeader == null || adtsAudioHeader.length == 0) {
            Log.e(TAG, "realtime aac decoder create failure , adts audio header is null , so return false!");
            return false;
        }
        try {
            aacDecoder = MediaCodec.createDecoderByType(AUDIO_DECODE_MIME_TYPE);
            MediaFormat mediaFormat = new MediaFormat();
            mediaFormat.setString(MediaFormat.KEY_MIME, AUDIO_DECODE_MIME_TYPE);
            mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
            mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelConfig);
            ByteBuffer byteBuffer = ByteBuffer.allocate(adtsAudioHeader.length);
            byteBuffer.put(adtsAudioHeader);
            byteBuffer.flip();
            mediaFormat.setByteBuffer(CSD_MIME_TYPE_0, byteBuffer);
            aacDecoder.configure(mediaFormat, null, null, 0);
        } catch (IOException e) {
            Log.e(TAG, "realtime aac decoder create failure , happened exception : " + e.toString());
            if (aacDecoder != null) {
                aacDecoder.stop();
                aacDecoder.release();
            }
            aacDecoder = null;
        }
        if (aacDecoder == null) {
            return false;
        }
        // initialization audio track , use for play pcm audio data
        // audio output channel param channelConfig according device support select
        int buffsize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT);
        // author using channelConfig is AudioFormat.CHANNEL_OUT_MONO
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig,
                AudioFormat.ENCODING_PCM_16BIT, buffsize, AudioTrack.MODE_STREAM);
        audioTrack.play();
        aacDecoder.start();
        return true;
    }

    /**
     * bytes convert to int
     *
     * @param bytes byte array
     * @return int value
     */
    private int bytesToInt(byte[] bytes) {
        return (((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff));
    }

    @Override
    public void start() {
        if (worker == null) {
            isDecoding = true;
            waitTimeSum = 0;
            worker = new Thread(this, TAG);
            worker.start();
        }
    }

    /**
     * put realtime aac audio data
     *
     * @param aac aac audio data
     */
    public void putAacData(byte[] aac) {
        if (isDecoding) {
            aacFrameQueue.add(aac);
            synchronized (decodeLock) {
                waitTimeSum = 0;
                decodeLock.notifyAll();
            }
        }
    }

    /**
     * @param aac aac audio data
     * @return true means has aad decoder
     */
    private boolean hasAacDecoder(byte[] aac) {
        if (aacDecoder != null) {
            return true;
        }
        return checkAacAdtsHeader(aac);
    }

    @Override
    public void run() {
        final long timeOut = 5 * 1000;
        final long waitTime = 500;
        while (isDecoding) {
            while (!aacFrameQueue.isEmpty()) {
                byte[] aac = aacFrameQueue.poll();
                if (aac != null) {
                    if (!hasAacDecoder(aac)) {
                        Log.d(TAG, "aac decoder create failure , so break!");
                        break;
                    }
                    // todo decode aac audio data.
                    // remove aac audio adts header
                    byte[] aacTemp = new byte[aac.length - 7];
                    // data copy
                    System.arraycopy(aac, 7, aacTemp, 0, aacTemp.length);
                    // decode aac audio
                    decode(aacTemp, aacTemp.length);
                }
            }
            // Waiting for next frame
            synchronized (decodeLock) {
                try {
                    // isEmpty() may take some time, so we set timeout to detect next frame
                    decodeLock.wait(waitTime);
                    waitTimeSum += waitTime;
                    if (waitTimeSum >= timeOut) {
                        Log.d(TAG, "realtime aac decode thread read timeout , so break!");
                        break;
                    }
                } catch (InterruptedException ie) {
                    worker.interrupt();
                }
            }
        }
        Log.d(TAG, "realtime aac decode thread stop!");
        if (aacDecoder != null) {
            aacDecoder.stop();
            aacDecoder.release();
            aacDecoder = null;
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
        aacFrameQueue.clear();
        adtsAudioHeader = null;
        isDecoding = false;
        worker = null;
    }

    /**
     * aac audio data decode
     *
     * @param buf    aac audio data
     * @param length aac audio data length
     */
    private void decode(byte[] buf, int length) {
        try {
            ByteBuffer[] codecInputBuffers = aacDecoder.getInputBuffers();
            ByteBuffer[] codecOutputBuffers = aacDecoder.getOutputBuffers();
            long kTimeOutUs = 0;
            int inputBufIndex = aacDecoder.dequeueInputBuffer(kTimeOutUs);
            if (inputBufIndex >= 0) {
                ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                dstBuf.clear();
                dstBuf.put(buf, 0, length);
                aacDecoder.queueInputBuffer(inputBufIndex, 0, length, 0, 0);
            }
            ByteBuffer outputBuffer;
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int outputBufferIndex = aacDecoder.dequeueOutputBuffer(info, kTimeOutUs);
            while (outputBufferIndex >= 0) {
                outputBuffer = codecOutputBuffers[outputBufferIndex];
                byte[] outData = new byte[info.size];
                outputBuffer.get(outData);
                outputBuffer.clear();
                if (audioTrack != null) {
                    audioTrack.write(outData, 0, info.size);
                }
                aacDecoder.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = aacDecoder.dequeueOutputBuffer(info, kTimeOutUs);
            }
        } catch (Exception e) {
            Log.e(TAG, "realtime aac decode happened exception : " + e.toString());
        }
    }

    @Override
    public boolean isDecoding() {
        return isDecoding;
    }

    @Override
    public void stop() {
        isDecoding = false;
        aacFrameQueue.clear();
        if (worker != null) {
            worker.interrupt();
            try {
                worker.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                worker.interrupt();
            }
            worker = null;
        }
    }

    @Override
    public void release() {
        stop();
    }

}
