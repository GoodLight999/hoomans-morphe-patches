# Google Phone — Morphe patches

Morphe patches for the official Google Phone app (`com.google.android.dialer`).

## Supported version

Google Phone **161.0.726587057** (including `-downloadable`).

## Patches

<!-- PATCHES_START EXPANDED -->
### Google Phone
- **Enable call recording** — enables the built-in call recorder where the region gate hides it.
- **Silence call recording announcements** — Silences Google's spoken start and stop announcements in its built-in call recorder.

These are experimental changes to Google Phone.
<!-- PATCHES_END -->

## Build

```bash
./gradlew :patches:buildAndroid
```

The patch bundle (`.mpp`) is generated under `patches/build/libs/`.

## License

[GPLv3](LICENSE). Compatible with the [Morphe](https://morphe.software) patching ecosystem.
