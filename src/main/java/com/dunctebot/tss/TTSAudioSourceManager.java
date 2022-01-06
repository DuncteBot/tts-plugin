package com.dunctebot.tss;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class TTSAudioSourceManager implements AudioSourceManager, HttpConfigurable {
    public static final String GOOGLE_API_URL = "https://texttospeech.googleapis.com/";

    private final HttpInterfaceManager httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();

    @Override
    public String getSourceName() {
        return "tts";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        final URI uri = this.parseURI(reference.identifier);

        if (uri == null) {
            return null;
        }

        return new TTSAudioTrack(new AudioTrackInfo(
                "", // input text
                "TTS", // author
                Units.CONTENT_LENGTH_UNKNOWN, // length
                "", // base64 encoded audio
                false,
                uri.toString()
        ));
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        // nothing to encode
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return new TTSAudioTrack(trackInfo);
    }

    @Override
    public void shutdown() {
        ExceptionTools.closeWithWarnings(httpInterfaceManager);
    }

    @Override
    public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
        httpInterfaceManager.configureRequests(configurator);
    }

    @Override
    public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
        httpInterfaceManager.configureBuilder(configurator);
    }

    @Nullable
    private URI parseURI(String uri) {
        if (uri == null || !uri.startsWith("tts://")) {
            return null;
        }

        try {
            final URI parsed = new URI(uri);
            final String query = parsed.getQuery();

            if (query != null) {
                if (query.contains("config=")) {
                    // take config
                }
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }
}
