# Google cloud text to speech plugin
This is a lavalink plugin for the text to speech api that google provides.

**NOTE:** The Google cloud tts api is a paid api, if you are looking for a free version have a look at this plugin: https://github.com/DuncteBot/skybot-lavalink-plugin

# Installation
Latest version: ![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/DuncteBot/tts-plugin)

To install this plugin either download the latest release and place it in your `plugins` folder or add the following to your `application.yml`
```yml
lavalink:
  plugins:
    - dependency: "com.dunctebot:tts-plugin:VERSION" # replace VERSION with the version listed above!
      repository: "https://jitpack.io"
```

# Configuration
The plugin exposes these configuration options
<br><b>NOTE:</b> This plugins block is a root level object, don't place it where you import the plugin
<br><b>NOTE:</b> The double quotes are required for the private key
<br><b>NOTE:</b> Having a `GOOGLE_APPLICATION_CREDENTIALS` env var set with the path to your credentials file overrides these settings
```yml
plugins:
  dunctebot:
    tts:
      clientEmail: "" # The client_email from your service account json
      privateKey: "" # The private_key from your service account json
```

# Usage
This plugin allows for two ways of interaction with the Google tts api.
- Simple usage: basic config has been configured, you only need to supply the text and an optional language
- Advanced usage: you will need to supply the entire config, ***ONLY USE THIS IF YOU KNOW WHAT YOU ARE DOING***

## Simple usage
Simple usage will suit most users as it only exposes the bare basics of the configuration.

The format for requesting a tts message is as following `tts://Thing I want to say?language=de-DE` where the language query parameter is optional.
The language will default to `en-US`.
Make sure that the scheme is exactly `tts://` with two slashes because the text has to be in the authority section of the URI.

Below is a javascript example, javascript was chosen here because it is easy to understand for most people that want to interpret it.
```js
// random lavalink lib, just for illustration
const lavalink = new Node({
    connection: { host: "localhost", port: 2333, password: "youshallnotpass" },
    sendGatewayPayload: (id, payload) => sendWithDiscordLib(id, payload)
});

// user input from your bot
const messageFromUser = 'I am a message!';
// The message has to be URL/URI encoded since we are using the URI format to house the parameters
const messageEncoded = encodeURIComponent(messageFromUser);
// what we are sending to the /loadtracks endpoint on lavalink
const lookupURI = `tts://${messageEncoded}`;

// user your lavalink client to play the track
const results = await lavalink.rest.loadTracks(lookupURI);
```

## Advanced usage
With advanced usage you are able to override all config items for the [text.synthesize][gconfig] method, the config spec can be found [here][gconfig].
The `audioConfig.audioEncoding` property will always be `OGG_OPUS` to ensure that lavaplayer is able to play the output format, this cannot be overridden.

To use the advanced config setting all you need to do is send the config for the tts protocol, an example of that would be:
```
tts://?config=%7B%22input%22%3A%7B%22text%22%3A%22I%27ve%20added%20the%20event%20to%20your%20calendar.%22%7D%2C%22voice%22%3A%7B%22languageCode%22%3A%22en-gb%22%2C%22name%22%3A%22en-GB-Standard-A%22%2C%22ssmlGender%22%3A%22FEMALE%22%7D%2C%22audioConfig%22%3A%7B%22audioEncoding%22%3A%22OGG_OPUS%22%7D%7D
```
With the config being:
```json
{
  "input":{
    "text":"I've added the event to your calendar."
  },
  "voice":{
    "languageCode":"en-gb",
    "name":"en-GB-Standard-A",
    "ssmlGender":"FEMALE"
  },
  "audioConfig":{
    "audioEncoding":"OGG_OPUS"
  }
}
```
as you can see we had to url encode the config to ensure it being valid for the URI.

[gconfig]: https://cloud.google.com/text-to-speech/docs/reference/rest/v1/text/synthesize