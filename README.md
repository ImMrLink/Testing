# SimpleCore Plugin - Permissions & Staff Mode Update

## New Features
1. **Permissions & Groups System**
   - Create groups in `permissions.yml`.
   - Each group can have a prefix, suffix, and a list of permissions.
   - Players can be assigned to groups.
   - Prefix and suffix appear in chat.

2. **Staff Mode**
   - `/staffmode` toggles staff mode.
   - Staff mode gives the player a special hotbar with:
     - Fly toggle item
     - Vanish (spectator mode) toggle item
     - Inventory view item (right-click a player)
     - Punishment menu item (opens GUI for selected player)

3. **Punishments System**
   - Punishment GUI offers:
     - Warning
     - Jail (4h, 12h, 24h, 48h)
     - Mute (4h, 12h, 24h, 48h)
     - Kick
     - Temp Ban
     - Permanent Ban
     - IP Ban
   - Jail and mute track durations and auto-expire.

4. **Configuration**
   - `permissions.yml` for groups/prefix/suffix.
   - `staffmode.yml` for customizing staff hotbar items.
   - `punishments.yml` for customizing punishment messages and durations.

## Installation
1. Place the JAR into your `plugins` folder.
2. Start the server to generate configs.
3. Configure permissions/groups in `permissions.yml`.
