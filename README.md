# 💎 VirtualFilter - Professional Filtering System

![Minecraft Version](1.21.11)
![Build Success](https://img.shields.io)
![License](https://img.shields.io)

**VirtualFilter** is a high-performance, standalone Minecraft plugin designed for Factions and Survival servers. It allows players to manage their loot efficiently through three specialized virtual systems: **Auto-Block**, **Infinity-Stack**, and **Auto-Sell**.

---

## 🚀 Key Features

*   **🚫 AutoBlockFilter (ABF):** Automatically prevents unwanted items from entering the player's inventory.
*   **📦 InfinityStackFilter (ISF):** Stores items in a virtual infinite warehouse.
    *   **Auto-Merge:** If you add an item already in your filter, it automatically merges the stock instead of creating a duplicate slot.
    *   **Inventory Suck:** Instantly pulls all matching items from your inventory into the storage upon filter creation.
*   **💰 AutoSellFilter (ASF):** Automatically sells collected items at custom prices defined in `prices.yml`, with direct deposits via **Vault**.
*   **🎮 Modern UX:** 
    *   **Quick-Add:** `Shift + Left Click` on any item in your inventory to add it instantly.
    *   **Drag & Drop:** Replace or add filters by simply dropping a new item over the menu slots.
    *   **Action Bar Notifications:** Real-time sale and storage updates (toggle via `/vfat`).
*   **🌍 Multi-Language:** Support for **English** and **Portuguese**. Players choose via `/vflang`.
*   **🛡️ Safety First:** Blocks non-stackable items (tools, armor) to prevent accidental loss.

---

## 🛠 Commands & Permissions
| Command | Description | Permission |
| :--- | :--- | :--- |
| `/abf`, `/isf`, `/asf` | Opens the respective filter menus | `virtualfilter.use` |
| `/add<type>` | Adds/Merges held item to filter (e.g., `/addisf`) | `virtualfilter.use` |
| `/vfhelp` | Shows the dynamic help menu | `virtualfilter.use` |
| `/vflang` | Changes personal language (`en` / `pt`) | `virtualfilter.use` |
| `/vfat` | Toggles Action Bar notifications | `virtualfilter.use` |
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

1.  Download the latest JAR from the [target folder](https://github.com).
2.  Drop it into your server's `plugins` folder.
3.  Restart your server.
4.  Configure your item prices in `plugins/VirtualFilter/prices.yml`.
5.  Use `/vfreload` and you are ready to go!

---
**Developed by:** [comonier](https://github.com/comonier)  
**Support:** [Join our Discord](https://discord.gg/hPXr9NPn7W)
