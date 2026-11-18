# CinemaOS (Phone App)

CinemaOS is a free Android app for browsing and watching movies, TV shows and anime on your phone. It ships with built-in sources, so there is nothing to configure before you start watching.

## Features

- Movies, shows and anime in one place, with a home screen of curated rows, genre rows and an anime hero.
- Built-in sources, with automatic fallback to another source from inside the player if one fails.
- Rich source cards showing quality, language/audio labels, release names and media badges.
- Full-featured player: settings menu, quality selection, subtitles, fit button, episodes list, skip segments, and a gradient loader while buffering or seeking.
- CinemaOS account sync: sign in through the QR / website login flow to share your watch history and watchlist with [cinemaos.tech](https://cinemaos.tech).
- In-app updates delivered from this repository's GitHub Releases.

## Install

1. Open the [Releases](../../releases) page of this repository.
2. Download the latest `CinemaOS-<version>-*.apk` that matches your device (arm64-v8a for most modern phones; the universal APK works everywhere).
3. Open the file on your phone and allow installation from your browser or file manager if prompted.

After the first install, the app notifies you when a new release is available.

## Building

APKs are built by the manual GitHub Actions workflow [`build-apk.yml`](.github/workflows/build-apk.yml):

1. Add a `TMDB_API_KEY` repository secret (Settings, Secrets and variables, Actions).
2. Open the Actions tab, choose **Build APK** and click **Run workflow** on the `cinemaos` branch.
3. Download the APKs from the workflow artifacts or the generated GitHub Release.

The app is built with Kotlin Multiplatform and Compose Multiplatform.

## Credits

CinemaOS is based on the open-source [Nuvio](https://github.com/NuvioMedia/NuvioMobile) project. Thanks to its authors and contributors.

## License

[GNU General Public License v3.0](./LICENSE). Original copyright and attribution notices are retained as required by the license.
