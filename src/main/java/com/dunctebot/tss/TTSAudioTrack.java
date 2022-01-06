package com.dunctebot.tss;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

public class TTSAudioTrack extends DelegatedAudioTrack {
    public TTSAudioTrack(AudioTrackInfo trackInfo) {
        super(trackInfo);
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        //
    }


    @Override
    protected AudioTrack makeShallowClone() {
        return new TTSAudioTrack(this.trackInfo);
    }
}
