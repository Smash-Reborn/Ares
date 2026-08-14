## ARES MOD - SCOPE

- [x] Customisable MAX_RENDER_DISTANCE var which forces all entities to remain rendered on the client
- [x] Mob head renders next to the players head in the tab-bar, representing the current 'Disguise' of the player
- [x] Remove the arrow bouncing animation (requires mixin, injection or raw nms replacement of specific lines in EntityArrow.class)
- [x] Remove enderman randomly teleporting clientside (requires mixin, injection or raw nms replacement of specific lines in EntityEnderman.class)
- [x] Improved F3+B (render bounding boxes) which shows the disguised entity AABB, the invisible original player and the invisible hittable squid
- [x] Restore disguised entity nametag above the mobs head (hide the artificial armor-stand one)
- [x] Config command and settings GUI that let's you configure the mods settings at runtime
- [x] When displaying the tab-bar, render underneath a visual that tells you whether the mod is actively running on the server
- [ ] Chat message which tells you whether the mod is actively enabled on the server (delayed so server JOIN messages are shown first)
- [ ] Backport GLOWING render from modern versions (with full RGB color support) and allow players & their respective projectiles to be GLOWING during gameplay. (for teams mode, can make the GLOW match the team color so you can differentiate between teams)
- [x] Color selector config options for hittable nametag AABB, serverside player AABB and player nametag color
- [ ] Give each disguise mob its own sneaking animation ? (technically the data-watcher updates this field, at least on mineplex, so its doable)
- [ ] Allow disguise mobs to inherit their disguised players capes, rendered at certain positions per mob (ig a modded backport of my modern disguise impl)

- [x] Proper MC-MOD description & sprite icon
- [ ] Simple build scripts that will automatically compile all/various platform versions of the mod (automatically handle JDK and gradle version selection)
