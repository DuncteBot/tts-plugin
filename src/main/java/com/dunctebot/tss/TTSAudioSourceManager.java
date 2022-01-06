package com.dunctebot.tss;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TTSAudioSourceManager implements AudioSourceManager, HttpConfigurable {
    public static final String GOOGLE_API_URL = "https://texttospeech.googleapis.com/";

    private final HttpInterfaceManager httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();
    private final JWTGenerator generator;

    public TTSAudioSourceManager(JWTGenerator generator) {
        this.generator = generator;

        this.configureBuilder(
                (config) -> config.setDefaultHeaders(List.of(new Header() {
                    @Override
                    public HeaderElement[] getElements() throws ParseException {
                        return new HeaderElement[0];
                    }

                    @Override
                    public String getName() {
                        return "Authorization";
                    }

                    @Override
                    public String getValue() {
                        return "Bearer " + generator.getJWT();
                    }
                }))
        );
    }

    @Override
    public String getSourceName() {
        return "gcloud-tts";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (this.generator == null) {
            return null;
        }

        final GoogleTTSConfig config = this.parseURI(reference.identifier);

        if (config == null) {
            return null;
        }

        final String audio = this.getAudio(config);

        if (audio == null) {
            return AudioReference.NO_TRACK;
        }

        return new TTSAudioTrack(new AudioTrackInfo(
                config.getSynthesisInput().getEffectiveText(), // input text
                "TTS", // author
                Units.CONTENT_LENGTH_UNKNOWN, // length
                audio, // base64 encoded audio
                false,
                config.getUri().toString()
        ));
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // nothing to encode
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
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
    private String getAudio(GoogleTTSConfig config) {
        return null;
    }

    @Nullable
    private GoogleTTSConfig parseURI(String uri) {
        if (uri == null || !uri.startsWith("tts://")) {
            return null;
        }

        try {
            final URIBuilder parsed = new URIBuilder(uri);
            final URI builtUri = parsed.build();
            final List<NameValuePair> queryParams = parsed.getQueryParams();
            final GoogleTTSConfig config = new GoogleTTSConfig().setUri(builtUri);

            if (!queryParams.isEmpty()) {
                if (queryParams.stream().anyMatch((p) -> "config".equals(p.getName()))) {
                    final NameValuePair jsonConfig = queryParams.stream()
                            .filter(
                                    (p) -> "config".equals(p.getName())
                            )
                            .findFirst()
                            .orElse(null);

                    assert jsonConfig != null; // will never be null :)
                    final JsonBrowser parse = JsonBrowser.parse(jsonConfig.getValue());

                    // take config
                    // make config from param and return
                    return parse.as(GoogleTTSConfig.class).setUri(builtUri);
                }

                // parse predefined query params
                if (queryParams.stream().anyMatch((p) -> "language".equals(p.getName()))) {
                    queryParams.stream()
                            .filter(
                                    (p) -> "language".equals(p.getName())
                            )
                            .findFirst()
                            .ifPresent(
                                    (language) -> config.getVoiceSelectionParams()
                                            .setLanguageCode(language.getValue())
                            );

                }

            }

            if (StringUtils.isEmpty(parsed.getPath())) {
                return null;
            }

            config.getSynthesisInput().setText(parsed.getPath());

            return config;
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
