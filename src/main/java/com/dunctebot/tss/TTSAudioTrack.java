package com.dunctebot.tss;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.util.Base64;

public class TTSAudioTrack extends DelegatedAudioTrack {
    private final TTSAudioSourceManager sourceManager;

    public TTSAudioTrack(AudioTrackInfo trackInfo, TTSAudioSourceManager sourceManager) {
        super(trackInfo);
        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        final byte[] audio = Base64.getDecoder().decode(this.trackInfo.identifier);
        // use NonSeekableInputStream + ByteBufferInputStream/ByteArrayInputStream?
        // make a custom impl of SeekableInputStream with ByteArrayInputStream?
        // audio track: OggAudioTrack
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new TTSAudioTrack(this.trackInfo, this.sourceManager);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return this.sourceManager;
    }
}
