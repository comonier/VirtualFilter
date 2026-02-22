# 💎 VirtualFilter - Professional Filtering System

1.21.11 ( you can test the plugin on my server: ip: hu3.org [java] and [bedrock] )

**VirtualFilter** is a high-performance, standalone Minecraft plugin designed for Factions and Survival servers. It allows players to manage their loot efficiently through three specialized virtual systems: **Auto-Block**, **Infinity-Stack**, and **Auto-Sell**.



**##🚀 NEW IN VERSION (1.3.3)**
Major stability and automation improvements for the ultimate user experience.

🏗️ **Infinite Build** (AFH + ISF): AutoFillHand now automatically pulls 64 units directly from your InfinityStack (ISF) storage when your inventory runs out.<br>
🔗 **New Aliases**: You can now use /vf or /vfilter as shortcuts for the help menu.<br>
🐛 **Command Fix**: Fixed a critical "Unhandled Exception" crash when using /vfhelp for new players.<br>
📡 **Database UPSERT**: Rebuilt SQL logic to ensure all player settings (Language, AFH, AL) are correctly initialized upon login.

---

## 🚀 Auto Loot: Critical Updates (v1.3.2)
<p>This update focuses on fixing edge cases and ensuring full compatibility with custom items.</p>

⚡ **AutoLoot (v1.3.2):** Automatically collects drops directly into your inventory or filters when breaking blocks. <br>
🛡️ **NBT Shield:** Items with custom names or lore (Slimefun, mcMMO) are ignored by filters to prevent accidental selling. <br>
📦 **Shulker Box Protection:** Shulkers now go directly to your inventory while preserving ALL internal content (NBT). <br>
🧩 **Multi-Block Fix:** Fixed issue where beds and doors were destroyed by the loot funnel. <br>
🧲 **Magnet Mode (10 Blocks):** Increased pickup range to catch all drops from explosions or TreeCutter. <br>
🔇 **Smart Audio:** Teleport sound plays ONLY for extra drops (Magnet) with an anti-spam cooldown. <br>

---

## 💎 VirtualFilter v1.3

### ⚠️ 🛑 **IMPORTANT: DATABASE BACKUP REQUIRED** 🛑 ⚠️
<p>It is highly recommended to perform a full backup of your <b>storage.db</b> file before starting the server with this new version. This update performs a database schema migration to include the new AutoLoot settings.</p>

### 💡 ℹ️ **TROUBLESHOOTING: COMMAND /AL** ℹ️ 💡
<p>If the new <b>/al</b> command displays a "Message not found" error, simply delete your current <b>messages.yml</b> file and run <b>/vfreload</b>. This will force the plugin to generate the new message keys correctly.</p>

---

## 🚀 Version 1.2 Highlights

   **🛠️ AutoFillHand (AFH):** Never stop building or mining! Automatically replenishes identical items in your hand when a stack runs out or a tool breaks (supports non-enchanted items only). Toggle it via `/afh`.
   **📥 Massive Withdraw:** In the **ISF** menu, use `Shift + Left Click` on an item to instantly fill your inventory with the stored stock.
   **⚖️ Smart Hierarchy:** Automatic priority system: **Sell (ASF) > Storage (ISF) > Block (ABF)**.

---

## 💎 Key Features

⚡ **AutoLoot (v1.3):** Automatically collects drops directly into your inventory or filters when breaking blocks. <br>
🚫 **AutoBlockFilter (ABF):** Automatically prevents unwanted items from entering your inventory. <br>
📦 **InfinityStackFilter (ISF):** Stores items in a virtual infinite warehouse. <br>
🔄 **Auto-Merge:** Automatically merges new items into existing stock. <br>
🧲 **Inventory Suck:** Instantly pulls all matching items from your inventory into the filter upon creation. <br>
💰 **AutoSellFilter (ASF):** Sells items automatically with configurable prices in prices.yml via Vault. <br>
🎮 **Modern UX:** Enhanced user experience with intuitive menus. <br>
🖱️ **Quick-Add:** Shift + Left Click in your inventory to add items instantly to a filter. <br>
♻️ **Drag & Drop:** Replace filters by dragging items within the menu. <br>
🔔 **Action Bar:** Real-time notifications (toggle via /vfat). <br>
🌍 **Multi-Language:** Native support for English and Portuguese (/vflang).


---

## 🛠 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/al` | Toggle Auto Loot | `virtualfilter.use` |
| `/abf`, `/isf`, `/asf` | Opens the respective filter menus | `virtualfilter.use` |
| `/afh` | Toggles automatic hand refill | `virtualfilter.use` |
| `/add<type>` | Adds/Merges held item to a filter | `virtualfilter.use` |
| `/vfat` | Toggles Action Bar notifications | `virtualfilter.use` |
| `/vflang <en/pt>` | Changes your personal language | `virtualfilter.use` |
| `/vfhelp` | Shows the dynamic help menu | `virtualfilter.use` |
| `/vfreload` | Reloads configurations and prices | `virtualfilter.admin` |

---

### 🔑 Slot Management
Unlock slots dynamically using permissions (1 to 54):
*   `virtualfilter.abf.X` (e.g., `virtualfilter.abf.9` for 1 row)
*   `virtualfilter.isf.X`
*   `virtualfilter.asf.X`

---

## 📦 Requirements

*   **Server:** [PaperMC](https://papermc.io/downloads/paper) (1.21.1+)
*   **Economy:** [Vault](https://www.spigotmc.org/resources/vault.34315/) (Required for Auto-Sell payments)
*   **Permissions:** [LuckPerms](https://luckperms.net) (Recommended for slot management)

---

## ⚙️ Installation

2.  Drop it into your server's `plugins` folder.
3.  Restart your server.
4.  Configure your item prices in `plugins/VirtualFilter/prices.yml`.
5.  Use `/vfreload` and you are ready to go!

---
**Developed by:** [comonier](https://github.com/comonier)  
**Support:** [Join our Discord](https://discord.gg/hPXr9NPn7W)
