# 💎 VirtualFilter v1.7 (Modular Edition)

**VirtualFilter** is an advanced item filtering and virtual storage system, fully optimized for **Paper 1.21.1** servers with complete support for **Bedrock (GeyserMC)** players.

## 🚀 What's New in v1.7
- **Bedrock Accessibility:** Redesigned withdrawal and addition commands for players without mouse shortcuts (Shift/Right-click).
- **Auto-Shift (Mission 2):** Automatic slot reorganization. When a filter is removed, subsequent filters occupy the empty space, eliminating gaps.
- **Smart Merge:** Add items to existing filters with one click (Shift+Left-click) to add to stock instantly.
- **Independent Logs:** Full control over what you see in chat. Separate options for personal logs and nearby player logs.
- **Total Alert:** Visual and sound notifications (Villager No) for full inventory across all platforms.

## 🎮 Player Commands

### 📦 Filters and Menus
* `/abf` - Opens the **AutoBlock** menu (Items destroyed upon collection).
* `/isf` - Opens the **InfinityStack** menu (Infinite virtual storage).
* `/asf` - Opens the **AutoSell** menu (Automatic sale via Vault).

### 🛠️ Item Management (Java & Bedrock)
* `/add<type> [slot]` - Adds the item in hand to the filter (Ex: `/addisf`). In ISF, it captures the entire inventory.
* `/rem<type> [slot]` - Removes a filter by slot ID or by item in hand.
* `/isg <slot> <all/pack/amount>` - **(Bedrock Focus)** Withdraws ISF items via chat.

### 🤖 Automation and Logs
* `/al` - Toggles **AutoLoot** (Automatic drop collection).
* `/afh` - Toggles **AutoFillHand** (Automatic block replenishment in hand).
* `/lo` - Toggles the display of **your own** loot logs.
* `/la` - Toggles the display of **other players'** loot logs (32m radius).
* `/vfat` - Toggles sale notifications in the **Action Bar**.

## 🔑 Permissions
- `virtualfilter.admin`: Access to the `/vfreload` command.
- `virtualfilter.<type>.<slot>`: Defines how many slots the player can have (Ex: `virtualfilter.isf.54`).

## 🛠️ Installation
1. Requires **Java 21**.
2. Mandatory dependency: **Vault**.
3. Optional dependency: **ShopGUI+** (For automatic price extraction).
4. Place the JAR in the `plugins` folder and restart the server.

---
*Developed with a focus on performance and data integrity via SQLite.*
