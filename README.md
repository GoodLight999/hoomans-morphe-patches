# Google Phone — Morphe patches

Morphe patches for the official Google Phone app (`com.google.android.dialer`).

## Supported version

Google Phone **161.0.726587057** (including `-downloadable`).

## Patches

<!-- PATCHES_START EXPANDED -->
### Google Phone
- **Enable call recording** — exposes Google's built-in call recorder where the client-side country gate hides it.

The upstream recording announcement behavior is not changed by this patch.
<!-- PATCHES_END -->

## Build

```bash
./gradlew :patches:buildAndroid
```

The patch bundle (`.mpp`) is generated under `patches/build/libs/`.

## License

[GPLv3](LICENSE). Compatible with the [Morphe](https://morphe.software) patching ecosystem.
