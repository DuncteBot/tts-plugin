package com.dunctebot.tss;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TTSPlugin implements AudioPlayerManagerConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(TTSPlugin.class);
    private JWTGenerator generator;

    public TTSPlugin(LavalinkTTSConfig config) throws Exception {
        LOG.info("Loading GCLOUD TTS plugin");

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
}
