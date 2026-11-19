# Google Phone — Morphe patches

This repository contains Morphe patches for the official Google Phone app (`com.google.android.dialer`).

## Supported version

Google Phone **161.0.726587057** (including `-downloadable`).

## Patches

<!-- PATCHES_START EXPANDED -->
### Google Phone
- **Enable call recording** — enables Google's built-in recorder where the region check hides it.
- **Silent call recording (GoodLight999)** — development patch to enable recording, remove spoken/beep disclosures, and provide logcat diagnostics.

Both patches target Google Phone only. The silence patch is under active development and is not guaranteed to work on every device.
<!-- PATCHES_END -->

## Build

```bash
./gradlew :patches:buildAndroid
```

The patch bundle (`.mpp`) is generated under `patches/build/libs/`.

## License

[GPLv3](LICENSE). Compatible with the [Morphe](https://morphe.software) patching ecosystem.
