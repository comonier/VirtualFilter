# 🟦 VirtualFilter
###### Last Update: 1.7.3
The ultimate item management system with virtual storage, auto-sell, and container protection.

##### 🟩 Main Features
*   **Filter Decision Engine (New):** Smart hierarchy: `AutoSell` ➜ `ISF` ➜ `AutoBlock` ➜ `Inventory`.
*   **AutoSell (ASF):** Sell items automatically based on customizable prices in `prices.yml`.
*   **InfinityStack (ISF):** Infinite virtual storage for any stackable items, accessible by commands or a professional **GUI**.
*   **AutoBlock (ABF):** Automatically blocks the loot event for any item configured in the filter.
*   **AutoFillHand (AFH):** Integrated with **ISF**! Automatically refills your hand with blocks from virtual storage while building.
*   **Independent Logs (New):** Full control via `/lo` (**Personal**) and `/la` (**Nearby players**) logs.
*   **AutoLoot & Magnet:** Advanced 10-block radius item pickup with a 2-second sound cooldown to prevent audio spam.
*   **Bedrock Compatibility:** Full support for **GeyserMC** with specific commands for slot-based withdrawal and filter management.
*   **Bedrock & Geyser Focus:** Full support for mobile/console players via specific chat commands. ( `/isg amount/pack/all` )
*   **Chest Guard System:** Prevents item loss when breaking containers. Processes items into `ISF` or `Inventory` before dropping leftovers.
*   **Smart Grouped Reports:** Summarizes collection results into a single, clean, color-coded chat message (`ISF`, `INV`, or `GROUND`).
*   **Protection Integration:** Advanced **NBT shield** to protect custom items from **Slimefun**, **mcMMO**, and special Bedrock metadata.

##### 🟦 Commands
*   `vf help` / `vf`: Opens the main help menu with all available features.
*   `isf` / `asf` / `abf`: Opens the **GUI** for **InfinityStack**, **AutoSell**, or **AutoBlock** filters.
*   `isg <amount|pack|all>`: Withdraws items from virtual storage using the **Slot ID**. (Unique way to bedrock get packs on isf)
*   `getisf <amount|pack|all>`: Withdraws items from virtual storage using the **Slot ID**. (Unique way to bedrock get packs on isf)
*   `addisf` / `addasf` / `addabf`: Adds the item in your hand to the respective filter. (Unique way to bedrock add filters)
*   `remisf` / `remasf` / `remabf [slotid]`: Removes a filter by held item or specific **Slot ID**. (Unique way to bedrock rem filters)
*   `al` / `afh`: Toggles **AutoLoot** (pickup) or **AutoFillHand** (refill) features.
*   `lo` / `la`: Toggles **Personal Loot Report** Own/All.
*   `vfat`: Toggles sale and refill notifications in the **Action Bar**.
*   `vflang`: Switches personal language between English (**en**) and Portuguese (**pt**).
*   `vfreload`: `virtualfilter.admin` Reloads the configurations, messages, and prices (**Admin only**).

##### 🟨 Permissions
*   `virtualfilter.admin`: Permission to use reload and administrative commands.
*   `virtualfilter.chestdebug`: Permission to toggle the chest break report messages.
*   `virtualfilter.isf.<amount>`: Defines the amount of slots available in **InfinityStack**.
*   `virtualfilter.asf.<amount>`: Defines the amount of slots available in **AutoSell**.
*   `virtualfilter.abf.<amount>`: Defines the amount of slots available in **AutoBlock**.

##### 🟥 Important Notice
*   For the **AutoSell** feature to work, a valid **Economy plugin** and **Vault** must be installed.
*   To prevent **"Message not found"** errors after updating to v1.7.2, please delete your old `messages.yml`.
