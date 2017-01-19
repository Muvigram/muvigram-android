package com.estsoft.muvicam.transcoder.wrappers;

/**
 * Created by estsoft on 2017-01-19.
 */

public interface MediaTranscoder {

    void initVideoTarget(int interval, int frameRate, int bitrate, int rotation, int width, int height );
    void initAudioTarget( int sampleRate, int channelCount, int bitrate );

    void addSegment(String inputFilePath, long startTimeUs, long endTimeUs, int audioVolume  );
    void addMusicSegment(String inputFilePath, long offset, int audioVolume );

    void startWork();



}