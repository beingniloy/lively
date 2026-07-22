# Lively Wallpaper

Lively Wallpaper is a premium, open-source Android application designed to transform your device with beautiful, high-performance live wallpapers. Built with Jetpack Compose and Material 3, it offers a seamless experience for picking, editing, and applying video wallpapers with granular control.

![App Banner](https://raw.githubusercontent.com/beingniloy/lively/main/.github/assets/banner.png)

## Star History

<a href="https://www.star-history.com/?repos=beingniloy%2Flively&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=beingniloy/lively&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=beingniloy/lively&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=beingniloy/lively&type=date&legend=top-left" />
 </picture>
</a>

## ✨ Features

- **Video Wallpaper Engine**: High-performance rendering of local video files as system wallpapers.
- **Advanced Editor**: Precise control over your wallpaper with:
    - **Fit Modes**: Choose between Fit, Fill, or Center Crop.
    - **Transformations**: Pinch-to-zoom, position adjustments (X/Y), and scale sliders.
    - **Real-time Preview**: See exactly how your wallpaper will look before applying it.
- **Collections & Library**: Organize your favorite videos and browse recent wallpapers.
- **Material 3 Expressive**: A modern UI utilizing the Nunito font family and Material Symbols.
- **Theming**: Full support for Light, Dark, and System themes with Android 12+ Dynamic Color integration.
- **Battery Optimized**: Designed to be light on resources with optional battery-saver modes.
- **Privacy First**: Completely offline. No tracking, no account required, and no data leaves your device.

## 📸 Screenshots

| Home & Discovery | Video Picker | Wallpaper Editor |
| :---: | :---: | :---: |
| ![Home](https://raw.githubusercontent.com/beingniloy/lively/main/assets/screen_home.png) | ![Picker](https://raw.githubusercontent.com/beingniloy/lively/main/assets/screen_picker.png) | ![Editor](https://raw.githubusercontent.com/beingniloy/lively/main/assets/screen_editor.png) |
| *Personalized dashboard with your library and recent wallpapers.* | *Seamless integration with the Android system picker for high-quality video selection.* | *Granular control over scale, position, and fit modes with live interaction.* |

## 🚀 Installation

1. Download the latest APK from the [Releases](https://github.com/beingniloy/lively/releases) page.
2. Enable "Install from Unknown Sources" in your device settings if prompted.
3. Open the app and select a video to get started.

## 🛠️ Development

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 35

### Building from source
```bash
git clone https://github.com/beingniloy/lively.git
cd lively
./gradlew assembleDebug
```

## 🛠️ Troubleshooting

### Wallpaper not applying
- **Permissions**: Ensure you have granted the app permission to access media files.
- **System Restrictions**: Some custom Android skins (MIUI, ColorOS) might prevent third-party apps from setting wallpapers. Try setting it from the system wallpaper picker if the app button fails.

### App is killed in background
- **Battery Optimization**: Go to **Settings > Apps > Lively Wallpaper > Battery** and set it to **"Unrestricted"**. This ensures the live wallpaper engine doesn't stutter or stop unexpectedly.

### Video stutters
- **Hardware Acceleration**: Lively Wallpaper uses hardware decoding. If a video stutters, it might be due to an unsupported codec or extremely high bitrate (e.g., 8K videos on older devices).

## 🛡️ Privacy Policy

Lively Wallpaper is built with privacy as a core value:
- **No Data Collection**: We do not collect, store, or transmit any personal information.
- **Local Processing**: All video processing and wallpaper applications happen locally on your device.
- **Permissions**: We only request necessary permissions (like storage access for video selection) to function correctly.
- **Open Source**: Our code is public and can be audited by anyone.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.
