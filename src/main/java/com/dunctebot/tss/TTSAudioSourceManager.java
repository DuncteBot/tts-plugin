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

        final String audio = this.getAudio(config);
//        final String audio = "";

        if (audio == null) {
            return AudioReference.NO_TRACK;
        }

        System.out.println("AUDIO HERE\n\n" + audio + "\n\nAUDIO HERE");

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

        // req.addHeader("", "");
        // req.addHeader("Content-Type", "application/json");

        req.setEntity(new StringEntity(config.toJson().toString(), ContentType.APPLICATION_JSON));


        try (final CloseableHttpResponse response = httpInterfaceManager.getInterface().execute(req)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("wrong status code?");
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
