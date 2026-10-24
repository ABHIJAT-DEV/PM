<div align="center">

  <!-- ==================== HERO COMPONENT ==================== -->
  <img src="assets/components/card_hero.svg" width="100%" alt="PixelMusic Hero" />

  <br /><br />

  <!-- Material 3 Expressive Interactive Action Badges -->
  <p align="center">
    <a href="https://pixelmusic.pages.dev">
      <img src="assets/badges/badge_download.svg" height="52" alt="Download APK" />
    </a>
    &nbsp;&nbsp;
    <a href="https://t.me/PixelMusicAppBot">
      <img src="assets/badges/badge_bot.svg" height="52" alt="Telegram Bot" />
    </a>
    &nbsp;&nbsp;
    <a href="https://pixelmusic.pages.dev">
      <img src="assets/badges/badge_website.svg" height="52" alt="Official Website" />
    </a>
    &nbsp;&nbsp;
    <a href="https://t.me/Saurav124x">
      <img src="assets/badges/badge_telegram.svg" height="52" alt="Telegram Channel" />
    </a>
  </p>

  <br />

  <!-- ==================== DISTRIBUTION HUB COMPONENT ==================== -->
  <img src="assets/components/card_download_hub.svg" width="100%" alt="Official Distribution Hub" />

  <br /><br />

  <!-- ==================== SHOWCASE COMPONENT ==================== -->
  <img src="assets/components/card_showcase.svg" width="100%" alt="Interface Showcase Gallery" />

  <br /><br />

  <table>
    <tr>
      <td align="center" width="33%">
        <img src="assets/IMG_20260923_100206.jpg" width="100%" alt="Explore & Discover" /><br />
        <b>Explore &amp; Recommendations</b><br />
        <sub>InnerTube mood carousels &amp; charts</sub>
      </td>
      <td align="center" width="33%">
        <img src="assets/IMG_20260923_100221.jpg" width="100%" alt="Now Playing Screen" /><br />
        <b>Expressive Now Playing</b><br />
        <sub>Wallpaper-reactive fluid AGSL scrim</sub>
      </td>
      <td align="center" width="33%">
        <img src="assets/IMG_20260923_100233.jpg" width="100%" alt="Synchronized Lyrics" /><br />
        <b>Karaoke Synchronized Lyrics</b><br />
        <sub>Live auto-scrolling with AI rescue</sub>
      </td>
    </tr>
    <tr>
      <td align="center" width="33%">
        <img src="assets/IMG_20260923_100255.jpg" width="100%" alt="Media Library" /><br />
        <b>Smart Offline Library</b><br />
        <sub>Tagged downloads &amp; cloud playlists</sub>
      </td>
      <td align="center" width="33%">
        <img src="assets/IMG_20260923_100305.jpg" width="100%" alt="Queue & Radio" /><br />
        <b>DualPlayer Preload Queue</b><br />
        <sub>Zero-gap transitions &amp; auto-queue</sub>
      </td>
      <td align="center" width="33%">
        <img src="assets/IMG_20260923_100314.jpg" width="100%" alt="Artist Page" /><br />
        <b>Rich Artist &amp; Album View</b><br />
        <sub>Discography, singles, and related artists</sub>
      </td>
    </tr>
    <tr>
      <td align="center" width="33%">
        <img src="assets/IMG_20260923_100325.jpg" width="100%" alt="Settings & Theming" /><br />
        <b>Deep Personalization</b><br />
        <sub>Dynamic palette &amp; custom accents</sub>
      </td>
      <td align="center" width="33%">
        <img src="assets/IMG_20260923_100349.jpg" width="100%" alt="Subsonic Integration" /><br />
        <b>Cloud &amp; Subsonic Servers</b><br />
        <sub>Navidrome, Airsonic &amp; NetEase support</sub>
      </td>
      <td align="center" width="33%">
        <img src="assets/IMG_20260923_100359.jpg" width="100%" alt="Dynamic Island & Capsule" /><br />
        <b>System Capsule &amp; Widgets</b><br />
        <sub>OriginOS, HyperOS, ColorOS dynamic island</sub>
      </td>
    </tr>
  </table>

  <br />

  <!-- ==================== SIGNATURE FEATURES (BIG COMPONENTS) ==================== -->
  <img src="assets/components/card_features_audio.svg" width="100%" alt="Direct CDN Streaming Engine" />

  <br /><br />

  <img src="assets/components/card_features_ui.svg" width="100%" alt="Material 3 Expressive Aesthetics" />

  <br /><br />

  <img src="assets/components/card_features_lyrics.svg" width="100%" alt="Synchronized Lyrics & AI Rescue" />

  <br /><br />

  <img src="assets/components/card_features_downloader.svg" width="100%" alt="Multi-Threaded Chunk Downloader" />

  <br /><br />

  <img src="assets/components/card_features_cloud.svg" width="100%" alt="Multi-Cloud & Subsonic Sync" />

  <br /><br />

  <img src="assets/components/card_features_system.svg" width="100%" alt="System Capsule & OEM Integrations" />

  <br /><br />

  <!-- ==================== AUDIO PIPELINE ARCHITECTURE ==================== -->
  <img src="assets/components/card_architecture.svg" width="100%" alt="Audio Engine Architecture" />

</div>

```mermaid
flowchart TD
    subgraph SOURCING ["1. Multi-Source Resolver"]
        YT["YouTube Music / InnerTube API"]
        SUB["Subsonic (Navidrome / Airsonic)"]
        NE["NetEase Music Provider"]
        LOCAL["Local MediaStore & Offline Cache"]
    end

    subgraph ENGINE ["2. Direct CDN & Audio Pipeline"]
        RESOLVE["Direct GoogleVideo CDN Stream Resolver"]
        DUAL["DualPlayer Pre-buffering Queue"]
        EXO["AndroidX Media3 ExoPlayer Engine"]
        DSP["DSP Equalizer • ReplayGain • Crossfade"]
    end

    subgraph PRESENTATION ["3. Material 3 Expressive UI"]
        M3E["Material You Dynamic Palette"]
        AGSL["AGSL Blur & Scrim Shaders"]
        LYRICS["Synced .lrc & Gemini AI Lyrics"]
        CAPSULE["Dynamic Island / System Capsule"]
    end

    SOURCING --> RESOLVE
    LOCAL --> EXO
    RESOLVE --> DUAL
    DUAL --> EXO
    EXO --> DSP
    DSP --> OUTPUT["Hi-Res Audio Output (Bluetooth / DAC / Speaker)"]
    EXO -. Telemetry & Art .-> PRESENTATION
```

<div align="center">

  <br />

  <!-- ==================== TECH STACK COMPONENT ==================== -->
  <img src="assets/components/card_tech_stack.svg" width="100%" alt="System Specifications & Tech Stack" />

  <br /><br />

  <!-- ==================== COMMUNITY & CONTRIBUTORS COMPONENT ==================== -->
  <img src="assets/components/card_community.svg" width="100%" alt="Community & Contributors" />

  <br /><br />

  <!-- Action Badges -->
  <p align="center">
    <a href="https://github.com/atappu805/Clean2.0/issues">
      <img src="assets/badges/badge_issues.svg" height="46" alt="Report an Issue" />
    </a>
    &nbsp;&nbsp;
    <a href="https://github.com/atappu805/Clean2.0/pulls">
      <img src="assets/badges/badge_contribute.svg" height="46" alt="Contribute" />
    </a>
    &nbsp;&nbsp;
    <a href="LICENSE">
      <img src="assets/badges/badge_license.svg" height="46" alt="GPL-3.0 License" />
    </a>
  </p>

  <br />

  <sub>PixelMusic is an open-source project created for personal and educational music listening. All audio, album art, and artist trademarks remain the intellectual property of their respective copyright holders.</sub>

</div>