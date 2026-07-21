# Backpacks Plugin AGENTS.md

## Project Identity
- Name: Backpacks
- Target: Minecraft 1.26.2 Paper server plugin
- Language: Java 21, Maven build
- Package: com.backpacks.plugin

## Core Architecture Rules
- All backpack data lives on items via PersistentDataContainer (namespaced keys: `backpacks.*`)
- Each backpack has a UUID. Contents are keyed by UUID in a global `Map<UUID, BackpackData>`
- A chestplate can hold up to 6 attached backpacks via NBT list `attached_backpacks`
- `glider` boolean NBT on chestplate indicates elytra combination
- Backpacks never auto-save to disk; they persist as long as the server runs (acceptable for this build)
- Use Bukkit/Paper inventory APIs, not NMS
- All addons are driven by items placed in dedicated backpack slots; sub-GUIs open from the backpack bottom bar

## Tier Definitions
- Copper: 27 slots, color #E67E22
- Iron: 32 slots, color #95A5A6
- Gold: 40 slots, color #F1C40F
- Diamond: 45 slots, color #3498DB
- Netherite: 74 slots (2 pages x 45 usable, page 2 partial), color #2C3E50

## Addon Definitions
- Crafting: 3x3 crafting table GUI
- Jukebox: Play music discs from backpack
- Quiver: 9 arrow stacks; auto-fill bow when out of arrows
- Enchant: Enchanting table GUI with bookshelf support

## Recipe Rules
- Base backpacks: shaped recipe with leather + chest + dye (tier color)
- Addons: smithing table recipe, base item + diamond
- Netherite upgrade: netherite upgrade smithing template + diamond backpack

## GUI Layout
- Rows 0-2: backpack storage (45 slots per page)
- Row 3: addon buttons left, page nav center, detach right
- Row 4: player inventory

## Commands
- `/backpack give <player> <tier>` - give a backpack
- `/backpack reload` - reload configs (stub for future)

## Events
- PlayerInteractEvent: right-click backpack opens GUI
- PlayerInteractEvent: shift+right-click chestplate with attached backpacks opens selection
- PlayerToggleSneakEvent: shift+jump while wearing chestplate opens selection
- InventoryClickEvent: handle addon buttons and page turning
- PlayerItemHeldEvent: none
- EntityDamageEvent: none

## Constraints
- Do not use comments in production code
- Keep classes single-responsibility
- Do not commit secrets or keys
- Build must compile with `mvn clean package`
