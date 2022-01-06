package com.dunctebot.tss;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
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

//        final String audio = this.getAudio(config);
        final String audio = "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";

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
        ), this);
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
        return new TTSAudioTrack(trackInfo, this);
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
        HttpPost req = new HttpPost(GOOGLE_API_URL + "v1/text:synthesize");

        req.setEntity(new StringEntity(config.toJson().toString(), ContentType.APPLICATION_JSON));


        try (final CloseableHttpResponse response = httpInterfaceManager.getInterface().execute(req)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                return null;
            }

            final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            final JsonBrowser json = JsonBrowser.parse(content);

            return json.get("audioContent").text();
        } catch (IOException e) {
            throw new FriendlyException("Could not generate audio", Severity.COMMON, e);
        }
    }

    @Nullable
    private GoogleTTSConfig parseURI(String uri) {
        System.out.println("URI: " + uri);

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

            System.out.println("Path: " + parsed.getPath());
            System.out.println("Host: " + parsed.getHost());
            System.out.println("Fragment: " + parsed.getFragment());
            System.out.println("userInfo: " + parsed.getUserInfo());
            System.out.println("Scheme: " + parsed.getScheme());
            System.out.println("Authority: " + builtUri.getAuthority());

            if (StringUtils.isEmpty(builtUri.getAuthority())) {
                return null;
            }

            config.getSynthesisInput().setText(builtUri.getAuthority());

            return config;
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
