# Usage
Audio encoding will always be `OGG_OPUS` to ensure that no encoding will take place at 100% volume

# Configuration
The plugin exposes these configuration options
<br><b>NOTE:</b> this plugins block is a root level object, don't place it where you import the plugin
```yml
plugins:
  dunctebot:
    tts:
      clientEmail: '' # The client_email from your service account json
      privateKey: '' # The private_key from your service account json
```