<div align="center">

# 💎 VirtualFilter v1.6
**Professional Independent Virtual Filtering System**

**Support:** Minecraft 1.21+ (Tested on **1.21.1**)
**Test Server:** `hu3.org` (Java & Bedrock - Default Ports)

---

## 🛡️ Chest Guard System (v1.6)
**NEVER lose an item.** When breaking containers (chests, hoppers, etc.) with **AutoLoot** enabled or disabled, the system processes items through security layers:
1. **Virtual Filters:** Top priority for **ASF** (AutoSell) or **ISF** (Storage).
2. **Inventory:** If no filter exists, the item tries to enter the player's inventory.
3. **Ground Security:** If no space remains, the item is dropped on the ground.
*The system waits for extra drops from other plugins (like **mcMMO**) to ensure mining/excavation bonuses are not lost.*

## 📊 Detailed Collection Report (vfcb)
**Total transparency.** Receive a color-coded log informing the exact destination of each item collected: how many units were sold, how many went to virtual storage, and what remained on the ground. 

## 🏗️ Infinite Building with AutoFillHand
**Build without interruptions.** Integrated with the database, the system detects when the block in your hand runs out and automatically replenishes a new stack of 64 units directly from your **ISF** storage, as long as you have stock.

## 📱 Full Bedrock Compatibility (GeyserMC)
**Mobile and Console friendly.** Short and intuitive commands allow you to manage filters, add items, and withdraw from storage by **Slot ID**, without relying on menu clicks that may fail on touch devices.

---

## ⚡ Collection Priority Hierarchy
To ensure the best economy, the system follows this strict order:
**1st ASF (Sell)** > **2nd ISF (Storage)** > **3rd ABF (Ignore/Ground)**

---

## 💻 Commands and Permissions Table



| Command | Description | Permission |
| :--- | :--- | :--- |
| `/vf` / `/vfilter` | Main help menu | `virtualfilter.use` |
| `/abf` / `/isf` / `/asf` | Opens the visual filter menu | `virtualfilter.use` |
| `/isg <slot> <amt\|all>` | Withdraws ISF items by Slot ID | `virtualfilter.use` |
| `/add[type] [slot]` | Adds item in hand to filter | `virtualfilter.use` |
| `/rem[type] [slot]` | Removes filter (only if stock = 0) | `virtualfilter.use` |
| `/al` | Toggles AutoLoot (Auto collection) | `virtualfilter.use` |
| `/afh` | Toggles AutoFill (Hand refill) | `virtualfilter.use` |
| `/vfcb` | Toggles Chest Collection Reports | `virtualfilter.chestdebug` |
| `/vflang <en\|pt>` | Changes personal language | `virtualfilter.use` |
| `/vfreload` | Reloads configurations (Admin) | `virtualfilter.admin` |

---

## 📜 Full Command List

*   **Menus:** `/abf`, `/isf`, `/asf` (Visual access to filters).
*   **Item Management:**
    *   `/addabf`, `/addisf`, `/addasf`: Adds the item in hand to the next free slot or a specific ID.
    *   `/remabf`, `/remisf`, `/remasf`: Removes the filter. **Note:** ISF filters can only be removed if the stock is zero.
    *   `/isg <id> <amount>`: Fast withdraw from virtual storage by Slot ID (1 to 54).
*   **Player Settings:**
    *   `/al`: Toggles automatic drop collection (Supports mcMMO).
    *   `/afh`: Toggles automatic block replenishment in hand.
    *   `/vfcb`: Toggles log messages when breaking containers.
    *   `/vflang`: Switches between English and Portuguese.
*   **Administration:**
    *   `/vfreload`: Updates `config.yml`, `messages.yml`, and prices without restarting.

---

## 🔑 System Permissions List

*   `virtualfilter.admin`: Full access, reload commands, and unlocks all 54 slots.
*   `virtualfilter.chestdebug`: Allows viewing chest collection reports.
*   `virtualfilter.asf.<number>`: Defines available slots in the AutoSell filter.
*   `virtualfilter.isf.<number>`: Defines available slots in the InfinityStack filter.
*   `virtualfilter.abf.<number>`: Defines available slots in the AutoBlock filter.
*   `virtualfilter.slot.<number>`: Specific permission to unlock individual slots by ID.

---

## 🛠️ Installation and Configuration

1.  **Installation:** Simply drag the `.jar` file into your `/plugins/` folder and start the server. The plugin will automatically create the data folder and the SQLite database.
2.  **Security:** While the system has built-in protection against item loss, we recommend always performing a **backup** of your `/plugins/VirtualFilter/` folder before major updates.
3.  **Customization:** You can edit messages in `messages.yml` and slot rates in `config.yml`. After changing, use `/vfreload` to apply changes instantly.
4.  **Integration:** Ensure you have **Vault** installed so the AutoSell (ASF) system can pay players.

---
*Developed by **comonier**.*
*Tested and approved at **hu3.org***
</div>
