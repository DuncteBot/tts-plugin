package com.dunctebot.tss;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "plugins.dunctebot.tts")
public class LavalinkTTSConfig {
}
