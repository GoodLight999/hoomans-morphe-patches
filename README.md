# Google Phone — Morphe patches

Morphe patches for the official Google Phone app (`com.google.android.dialer`).

## Supported version

Google Phone **161.0.726587057** (including `-downloadable`) and **247.0.1016877637** (including `-pixel`).

## Patches

<!-- PATCHES_START EXPANDED -->
### Google Phone
- **Enable call recording** — exposes Google's built-in call recorder where the client-side country gate hides it.
- **Silence call recording announcement** — replaces the start/end recording announcement (TTS, built-in clip and beep) with silence on both sides of the call. Recording itself is unchanged. If the silent clip cannot be prepared, playback fails instead of announcing.
<!-- PATCHES_END -->

## Build

```bash
./gradlew :patches:buildAndroid
```

The patch bundle (`.mpp`) is generated under `patches/build/libs/`.

## License

[GPLv3](LICENSE). Compatible with the [Morphe](https://morphe.software) patching ecosystem.
