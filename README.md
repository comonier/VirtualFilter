# 🟦 VirtualFilter
###### Last Update: 1.7.5
The ultimate item management system with virtual storage, auto-sell, and container protection.

##### 🟩 Main Features
*   **Filter Decision Engine:** Smart hierarchy: `AutoSell` ➜ `ISF` ➜ `AutoBlock` ➜ `Inventory`.
*   **Shulker Box Integrity (New 1.7.5):** Complete protection for Shulker Boxes. They bypass filters and AutoLoot to preserve internal data, moving to **Inventory** ➜ **EnderChest** ➜ **Ground** (for both Mining and Pickup events).
*   **AutoSell (ASF):** Sell items automatically based on customizable prices.
*   **InfinityStack (ISF):** Infinite virtual storage for stackable items.
*   **AutoBlock (ABF):** Automatically blocks items from entering the inventory.
*   **AutoFillHand (AFH):** Automatically refills hand with blocks from **ISF** while building.
*   **Independent Logs:** Full control via `/lo` (Personal) and `/la` (Nearby) logs.
*   **AutoLoot & Magnet:** Advanced item pickup with sound cooldown.
*   **Bedrock & Geyser Focus:** Full support for mobile players via specific chat commands for withdrawal and filter management.
*   **Chest Guard System:** Processes items into `ISF` or `Inventory` before dropping leftovers from broken containers.
*   **Smart Grouped Reports:** Summarizes collection results, including **ENDERCHEST** destinations.

##### 🟦 Commands
*   `vf help` / `vf`: Opens the main help menu.
*   `isf` / `asf` / `abf`: Opens the filter GUIs.
*   `isg <slot> [amount|pack|all]`: Withdraws items from virtual storage (Bedrock friendly).
*   `addisf` / `addasf` / `addabf`: Adds held item to the respective filter.
*   `remisf` / `remasf` / `remabf [slot]`: Removes a filter by item or Slot ID.
*   `al` / `afh`: Toggles **AutoLoot** or **AutoFillHand**.
*   `lo` / `la`: Toggles **Loot Reports** Own/All.
*   `vfat`: Toggles Action Bar notifications.
*   `vflang`: Switches language (en/pt).
*   `vfreload`: `virtualfilter.admin` Reloads configurations.

##### 🟨 Permissions
*   `virtualfilter.admin`: Access to administrative commands.
*   `virtualfilter.isf.[1-54]`: Max slots for ISF.
*   `virtualfilter.asf.[1-54]`: Max slots for ASF.
*   `virtualfilter.abf.[1-54]`: Max slots for ABF.

##### 🟥 Important Notice
*   **Vault Required:** Requires an economy provider for AutoSell.
*   **Java 21:** Minimum requirement.
*   **Shulker Pickup Safe:** Shulkers will automatically move to your EnderChest if your inventory is full during pickup.

**Developed by: Comonier**
