package com.dunctebot.tss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleTTSConfig {
    // uri that this config originated from
    private URI uri;

    private SynthesisInput synthesisInput = new SynthesisInput();
    private VoiceSelectionParams voiceSelectionParams = new VoiceSelectionParams();
    private AudioConfig audioConfig = new AudioConfig();

    public URI getUri() {
        return this.uri;
    }

    public GoogleTTSConfig setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public SynthesisInput getSynthesisInput() {
        return synthesisInput;
    }

    public void setSynthesisInput(SynthesisInput synthesisInput) {
        this.synthesisInput = synthesisInput;
    }

    public VoiceSelectionParams getVoiceSelectionParams() {
        return voiceSelectionParams;
    }

    public void setVoiceSelectionParams(VoiceSelectionParams voiceSelectionParams) {
        this.voiceSelectionParams = voiceSelectionParams;
    }

    public AudioConfig getAudioConfig() {
        return audioConfig;
    }

    public void setAudioConfig(AudioConfig audioConfig) {
        this.audioConfig = audioConfig;
    }

    public JSONObject toJson() {
        final JSONObject jsonBrowser = new JSONObject();

        jsonBrowser.put("input", this.synthesisInput.toJson());
        jsonBrowser.put("voice", this.voiceSelectionParams.toJson());
        jsonBrowser.put("audioConfig", this.audioConfig.toJson());

        return jsonBrowser;
    }

    public static class SynthesisInput {
        private String text = null;
        private String ssml = null;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getSsml() {
            return ssml;
        }

        public void setSsml(String ssml) {
            this.ssml = ssml;
        }

        public String getEffectiveText() {
            if (this.ssml != null) {
                return this.ssml;
            }

            return this.text;
        }

        public JSONObject toJson() {
            final JSONObject jsonBrowser = new JSONObject();

            if (this.text != null) {
                jsonBrowser.put("text", this.text);
            }

            if (this.ssml != null) {
                jsonBrowser.put("ssml", this.ssml);
            }

            return jsonBrowser;
        }
    }

    public static class VoiceSelectionParams {
        private String languageCode = "en-GB";
        private String name = null;
        private String ssmlGender = null;

        public String getLanguageCode() {
            return languageCode;
        }

        public void setLanguageCode(String languageCode) {
            this.languageCode = languageCode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSsmlGender() {
            return ssmlGender;
        }

        public void setSsmlGender(String ssmlGender) {
            this.ssmlGender = ssmlGender;
        }

        public JSONObject toJson() {
            final JSONObject jsonBrowser = new JSONObject();

            jsonBrowser.put("languageCode", this.languageCode);

            if (this.name != null) {
                jsonBrowser.put("name", this.name);
            }

            if (this.ssmlGender != null) {
                jsonBrowser.put("name", this.ssmlGender);
            }

            return jsonBrowser;
        }
    }

    public static class AudioConfig {
        private Double speakingRate = null;
        private Double pitch = null;
        private Double volumeGainDb = null;
        private Integer sampleRateHertz = null;
        private String[] effectsProfileId = null;

        public Double getSpeakingRate() {
            return speakingRate;
        }

        public void setSpeakingRate(Double speakingRate) {
            this.speakingRate = speakingRate;
        }

        public Double getPitch() {
            return pitch;
        }

        public void setPitch(Double pitch) {
            this.pitch = pitch;
        }

        public Double getVolumeGainDb() {
            return volumeGainDb;
        }

        public void setVolumeGainDb(Double volumeGainDb) {
            this.volumeGainDb = volumeGainDb;
        }

        public Integer getSampleRateHertz() {
            return sampleRateHertz;
        }

        public void setSampleRateHertz(Integer sampleRateHertz) {
            this.sampleRateHertz = sampleRateHertz;
        }

        public String[] getEffectsProfileId() {
            return effectsProfileId;
        }

        public void setEffectsProfileId(String[] effectsProfileId) {
            this.effectsProfileId = effectsProfileId;
        }

        public JSONObject toJson() {
            final JSONObject json = new JSONObject();

            // force opus no matter what
            json.put("audioEncoding", "OGG_OPUS");

            if (this.speakingRate != null) {
                json.put("speakingRate", this.speakingRate);
            }

            if (this.pitch != null) {
                json.put("pitch", this.pitch);
            }

            if (this.volumeGainDb != null) {
                json.put("volumeGainDb", this.volumeGainDb);
            }

            if (this.sampleRateHertz != null) {
                json.put("sampleRateHertz", this.sampleRateHertz);
            }

            if (this.effectsProfileId != null) {
                json.put("effectsProfileId", this.effectsProfileId);
            }

            return json;
        }
    }
}
