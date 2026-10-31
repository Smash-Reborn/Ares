# Ares II

### Clientside quality-of-life mod for **Minecraft 26.3**, built for Super Smash Mob gameplay servers (Mineplex, Smash Reborn, etc).<br>
![](https://img.shields.io/badge/environment-CLIENT-green) ![](https://img.shields.io/badge/client_version-26.3-green
) ![](https://img.shields.io/badge/mod_loader-FABRIC-orange
)<br><br>


> [!IMPORTANT]
> Mod is actively being contributed to and updated. See RELEASES tab for version changes.
<br>

## Supported Features
- Dynamic-extended entity render distance, allows you to see entities from far away
- Anti arrow bounce animation, prevents arrows from playing the bounce animation clientside when players miss with arrow projectiles
- Configurable preferences with a native in-game settings menu via `/ssmprefs`<br><br>

## Compiling & Building

Shared logic lives in `:core`. Loader modules sit beside it. Fabric is the active module; a Forge module can be added later without folding it into core.

| Artifact | Build task | Output |
|----------|------------|--------|
| Fabric mod | `:fabric:build` | `fabric/build/libs/AresMod-VERSION.jar` |
<br>
Build the Fabric mod:

```bash
gradlew :fabric:build
```
<br>
Version format: **X.Y.ZZZ** 
<br>
&emsp;-> `X` - Major Release<br>
&emsp;-> `Y` - Minor Release<br>
&emsp;-> `ZZZ` - Hotfix Release<br>
<br>
Current version is set in `gradle.properties`.

## Requirements

- **JDK 25** to run Gradle and to compile for Minecraft 26.3. Fabric Loom will not configure on an older JVM, even if a Java 25 toolchain is available. Set `JAVA_HOME` to a JDK 25 install.
- IntelliJ IDEA recommended

After importing the Gradle project, sync and use the **Minecraft Client** run configuration from `:fabric`.

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

### Fabric 26.3

1. Install Fabric Loader for Minecraft 26.3.
2. Install Fabric API for 26.3.
3. Copy `AresMod-VERSION.jar` into `.minecraft/mods/`.
4. Launch the Fabric 26.3 profile.

## Issues

> [!CAUTION]
> Mod might not be compatible with other mods. This is on a case-by-case basis.
> Open up an ISSUE with the `bug` tag & provide stacktrace or details.
