# Custom branch

This fork keeps `main` as a clean mirror of `arandomhooman/hoomans-morphe-patches`.

Local patches live on `custom`. To pull upstream changes, sync `main` with the parent fork first, then merge `main` into `custom`.

## Local patches

- **Silent call recording** — Google Phone 161.0.726587057 / 161.0.726587057-downloadable
  - Enables the built-in call recorder by bypassing the client-side country gate.
  - Silences the start/stop spoken announcement resources.
  - CI builds the patch bundle and applies it to a real v161 APK on every push to `custom`.

Do not put local patches directly on `main`; keeping `main` clean makes GitHub's normal fork-sync flow usable.
