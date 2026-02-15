# 💎 VirtualFilter - Professional Filtering System

1.21.11 ( you can test the plugin on my server: ip: hu3.org [java] and [bedrock] )

**VirtualFilter** is a high-performance, standalone Minecraft plugin designed for Factions and Survival servers. It allows players to manage their loot efficiently through three specialized virtual systems: **Auto-Block**, **Infinity-Stack**, and **Auto-Sell**.

---

## 🚀 Version 1.2 Highlights

*   **🛠️ AutoFillHand (AFH):** Never stop building or mining! Automatically replenishes identical items in your hand when a stack runs out or a tool breaks (supports non-enchanted items only). Toggle it via `/afh`.
*   **📥 Massive Withdraw:** In the **ISF** menu, use `Shift + Left Click` on an item to instantly fill your inventory with the stored stock.
*   **⚖️ Smart Hierarchy:** Automatic priority system: **Sell (ASF) > Storage (ISF) > Block (ABF)**.

---

## 💎 Key Features

*   **🚫 AutoBlockFilter (ABF):** Automatically prevents unwanted items from entering your inventory.
*   **📦 InfinityStackFilter (ISF):** Stores items in a virtual infinite warehouse.
    *   **Auto-Merge:** Automatically merges new items into existing stock.
    *   **Inventory Suck:** Instantly pulls all matching items from your inventory into the filter upon creation.
*   **💰 AutoSellFilter (ASF):** Sells items automatically with configurable prices in `prices.yml` via **Vault**.
*   **🎮 Modern UX:** 
    *   **Quick-Add:** `Shift + Left Click` in your inventory to add items instantly to a filter.
    *   **Drag & Drop:** Replace filters by dragging items within the menu.
    *   **Action Bar:** Real-time notifications (toggle via `/vfat`).
*   **🌍 Multi-Language:** Native support for **English** and **Portuguese** (`/vflang`).

---

## 🛠 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/abf`, `/isf`, `/asf` | Opens the respective filter menus | `virtualfilter.use` |
| `/afh` | Toggles automatic hand refill | `virtualfilter.use` |
| `/add<type>` | Adds/Merges held item to a filter | `virtualfilter.use` |
| `/vfat` | Toggles Action Bar notifications | `virtualfilter.use` |
| `/vflang <en/pt>` | Changes your personal language | `virtualfilter.use` |
| `/vfhelp` | Shows the dynamic help menu | `virtualfilter.use` |
| `/vfreload` | Reloads configurations and prices | `virtualfilter.admin` |

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

1.  Download the latest JAR from the [Download](https://github.com/comonier/VirtualFilter/releases/download/1.0/VirtualFilter-1.0.jar).
2.  Drop it into your server's `plugins` folder.
3.  Restart your server.
4.  Configure your item prices in `plugins/VirtualFilter/prices.yml`.
5.  Use `/vfreload` and you are ready to go!

---
**Developed by:** [comonier](https://github.com/comonier)  
**Support:** [Join our Discord](https://discord.gg/hPXr9NPn7W)
