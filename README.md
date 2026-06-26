# Ares (AresMod)

Client-side quality-of-life mod for **Minecraft 1.8.9**, built for Super Smash Mob gameplay servers (Mineplex, Smash Reborn, etc).

This project produces **two installable artifacts**:

| Artifact | Build task | Output |
|----------|------------|--------|
| Forge mod | `:forge-client:remapJar` | `forge-client/build/libs/AresMod-VERSION.jar` |
| LabyMod addon | `:labymod-addon:buildLabyAddon` | `labymod-addon/build/libs/AresMod-VERSION-labymod.jar` |

Build everything:

```bash
gradlew :forge-client:remapJar :labymod-addon:buildLabyAddon
```

Version format: **X.Y.ZZZ** (major.minor.hotfix). Current version is set in `gradle.properties`.

## Requirements

- **JDK 17/21** for Gradle (set `JAVA_HOME` if your default Java is newer than Gradle supports)
- **JDK 8** for the Minecraft project SDK
- IntelliJ IDEA recommended

After importing the Gradle project, sync and use the **Minecraft Client** run configuration from `:forge-client`.

## Server whitelist

Features only activate when connected to a server listed in:

```
.minecraft/config/AresMod/servers.json
```

Example:

```json
{
  "enabled": true,
  "servers": [
    "PLACEHOLDER.example.com",
    "127.0.0.1"
  ],
  "allowSubdomains": true
}
```

The file is created automatically on first launch. Check logs for `Ares active on <server>` or `Ares inactive - server not whitelisted`.

## Mod settings

Global feature flags and defaults live in:

```
.minecraft/config/AresMod/settings.json
```

Example:

```json
{
  "extendedRenderDistance": {
    "enabled": true,
    "renderDistanceWeight": 12.0
  }
}
```

- `extendedRenderDistance.enabled` — toggles extended entity render distance (still requires a whitelisted server)
- `extendedRenderDistance.renderDistanceWeight` — multiplier applied to each entity's `renderDistanceWeight` (vanilla default is `1.0`; higher values keep entities visible from farther away)

The file is created automatically on first launch. Settings are reloaded when you connect to a server.

## Installing for players

### Vanilla Forge 1.8.9

1. Install Forge 1.8.9.
2. Copy `AresMod-VERSION.jar` into `.minecraft/mods/`.
3. Launch the Forge 1.8.9 profile.

### Feather Client

Add `AresMod-VERSION.jar` as a custom Forge mod in the Feather launcher, then launch 1.8.9 with Forge.

### LabyMod 3

1. Copy `AresMod-VERSION-labymod.jar` into your LabyMod addons folder.
2. Enable the **Ares** addon in LabyMod.

### Lunar Client and Badlion Client

These clients do **not** support sideloading arbitrary Forge mods on 1.8.9. Official integration requires a partnership/submission with each client team.