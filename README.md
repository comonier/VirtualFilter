# 🟦 VirtualFilter
###### Last Update: 1.7.4
The ultimate item management system with virtual storage, auto-sell, and container protection.

##### 🟩 Main Features
*   **Filter Decision Engine:** Smart hierarchy: `AutoSell` ➜ `ISF` ➜ `AutoBlock` ➜ `Inventory`.
*   **Shulker Box Integrity (New):** Complete protection for Shulker Boxes. They bypass filters and AutoLoot to preserve internal data, moving to **Inventory** ➜ **EnderChest** ➜ **Ground**.
*   **AutoSell (ASF):** Sell items automatically based on customizable prices in `prices.yml`.
*   **InfinityStack (ISF):** Infinite virtual storage for stackable items, accessible by commands or a professional **GUI**.
*   **AutoBlock (ABF):** Automatically blocks the loot event for any item configured in the filter.
*   **AutoFillHand (AFH):** Integrated with **ISF**! Automatically refills your hand with blocks from virtual storage while building.
*   **Independent Logs:** Full control via `/lo` (**Personal**) and `/la` (**Nearby players**) logs.
*   **AutoLoot & Magnet:** Advanced item pickup with a 2-second sound cooldown to prevent audio spam.
*   **Bedrock & Geyser Focus:** Full support for mobile/console players via specific chat commands for withdrawal and filter management.
*   **Chest Guard System:** Prevents item loss when breaking containers. Processes items into `ISF` or `Inventory` before dropping leftovers.
*   **Smart Grouped Reports:** Summarizes collection results into a single, clean, color-coded chat message including **ENDERCHEST** destinations.

##### 🟦 Commands
*   `vf help` / `vf`: Opens the main help menu.
*   `isf` / `asf` / `abf`: Opens the **GUI** for respective filters.
*   `isg <slot> [amount|pack|all]`: Withdraws items from virtual storage using the **Slot ID**.
*   `addisf` / `addasf` / `addabf`: Adds the item in your hand to the respective filter.
*   `remisf` / `remasf` / `remabf [slot]`: Removes a filter by held item or specific **Slot ID**.
*   `al` / `afh`: Toggles **AutoLoot** or **AutoFillHand** features.
*   `lo` / `la`: Toggles **Loot Reports** Own/All.
*   `vfat`: Toggles notifications in the **Action Bar**.
*   `vflang`: Switches language between English (**en**) and Portuguese (**pt**).
*   `vfreload`: `virtualfilter.admin` Reloads configurations (Admin only).

##### 🟨 Permissions
*   `virtualfilter.admin`: Access to reload and admin commands.
*   `virtualfilter.isf.[1-54]`: Max slots for **InfinityStack**.
*   `virtualfilter.asf.[1-54]`: Max slots for **AutoSell**.
*   `virtualfilter.abf.[1-54]`: Max slots for **AutoBlock**.

##### 🟥 Important Notice
*   **Vault Required:** Requires an economy provider for **AutoSell**.
*   **Java 21:** Minimum requirement for compatibility.
*   **Shulker Safe:** Shulkers are protected from virtual storage to ensure NBT safety.

**Developed by: Comonier**
