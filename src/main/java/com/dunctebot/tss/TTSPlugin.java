package com.dunctebot.tss;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Files;

@Service
public class TTSPlugin implements AudioPlayerManagerConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(TTSPlugin.class);
    private JWTGenerator generator;

    public TTSPlugin(LavalinkTTSConfig config) throws Exception {
        LOG.info("Loading GCLOUD TTS plugin");

        this.checkEnvVar(config);

        if (StringUtils.isEmpty(config.getClientEmail())) {
            LOG.error("Client email is not set in the config, aborting. Config key is 'plugins.dunctebot.tts.clientEmail'");
            return;
        }
        if (StringUtils.isEmpty(config.getPrivateKey())) {
            LOG.error("Private key is not set in the config, aborting. Config key is 'plugins.dunctebot.tts.privateKey'");
            return;
        }

        this.generator = new JWTGenerator(
                config.getClientEmail(), config.getPrivateKey()
        );
    }

    @Override
    public AudioPlayerManager configure(AudioPlayerManager manager) {
        LOG.info("Registring source manager");
        manager.registerSourceManager(new TTSAudioSourceManager(this.generator));

        return manager;
    }

    private void checkEnvVar(LavalinkTTSConfig config) {
        final String googleCreds = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        if (googleCreds == null) {
            return;
        }

        try {
            final String json = Files.readString(new File(googleCreds).toPath());
            final JsonBrowser browser = JsonBrowser.parse(json);

            config.setClientEmail(browser.get("client_email").text());
            config.setPrivateKey(browser.get("private_key").text());
        } catch (Exception e) {
            // Fail silently?
            LOG.error("Found google application credentials but could not parse json", e);
        }
    }
}
