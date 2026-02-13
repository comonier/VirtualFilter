# 💎 VirtualFilter - Professional Filtering System

![Minecraft Version](https://img.shields.io)
![Build Success](https://img.shields.io)
![License](https://img.shields.io)

**VirtualFilter** is a high-performance, standalone Minecraft plugin designed for Factions and Survival servers. It allows players to manage their loot efficiently through three specialized virtual systems: **Auto-Block**, **Infinity-Stack**, and **Auto-Sell**.

---

## 🚀 Key Features

*   **🚫 AutoBlockFilter (ABF):** Automatically prevents unwanted items from entering the player's inventory.
*   **📦 InfinityStackFilter (ISF):** Stores items in a virtual infinite warehouse. Players can withdraw items stack-by-stack whenever they need.
*   **💰 AutoSellFilter (ASF):** Automatically sells collected items at custom prices defined in `prices.yml`, with direct deposits via **Vault**.
*   **🎮 Modern UX:** 
    *   **Quick-Add:** `Shift + Left Click` on any item in your inventory to add it instantly to a filter.
    *   **Drag & Drop:** Replace existing filters by simply dropping a new item over them in the menu.
    *   **Action Bar Notifications:** Real-time sale updates on your action bar (can be toggled via `/vfat`).
*   **🌍 Multi-Language:** Built-in support for **English** and **Portuguese**. Each player can choose their preferred language via `/vflang`.
*   **🛡️ Safety First:** Automatically blocks non-stackable items (tools, armor, etc.) to prevent accidental loss.

---

## 🛠 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/abf` | Opens the Auto-Block menu | `virtualfilter.use` |
| `/isf` | Opens the Infinity-Stack menu | `virtualfilter.use` |
| `/asf` | Opens the Auto-Sell menu | `virtualfilter.use` |
| `/add<type>` | Adds the held item to a filter (e.g., `/addabf`) | `virtualfilter.use` |
| `/vfhelp` | Shows the dynamic help menu | `virtualfilter.use` |
| `/vflang` | Changes personal language (`en` / `pt`) | `virtualfilter.use` |
| `/vfat` | Toggles Action Bar sale notifications | `virtualfilter.use` |
| `/vftc` | Toggles deletion confirmation | `virtualfilter.use` |
| `/vfreload` | Reloads configurations and prices | `virtualfilter.admin` |

### 🔑 Slot Management
Unlock slots dynamically using permissions (1 to 54):
*   `virtualfilter.abf.X` (e.g., `virtualfilter.abf.9` for 1 row)
*   `virtualfilter.isf.X`
*   `virtualfilter.asf.X`

---

## 📦 Requirements

*   **Server:** [PaperMC](https://papermc.io) or [Spigot](https://papermc.io/downloads/paper) (1.21.1+)
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
