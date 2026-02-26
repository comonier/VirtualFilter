<div align="center">

# 💎 VirtualFilter v1.5
**Professional Independent Virtual Filtering System**

An advanced item management system designed for high-performance Minecraft servers (1.21.1+).

---

## 🛡️ Chest Guard System (v1.5)
**This system prevents item loss when breaking chests or containers. When a chest is destroyed, the plugin processes items in three stages: first, it attempts to send them to the ISF virtual storage; next, to the player's inventory; and finally, if no space remains, it drops the rest on the ground with total security.**

## 📊 Detailed Collection Report
**Whenever a chest is broken, the player receives a color-coded chat log informing the exact destination of each item. You can see how many items went to the ISF, how many entered the inventory, and if anything was dropped due to lack of space. This function can be toggled via command.**

## 🏗️ Infinite Building with ISF
**The AutoFillHand system is integrated with the database. If the block in your hand runs out while you are building, the plugin automatically fetches a new stack of 64 units directly from your virtual storage, allowing for uninterrupted construction.**

## 📱 Full Bedrock Compatibility
**Developed to work perfectly with GeyserMC. Mobile and console players can manage filters, add items, remove by slot ID, and withdraw from storage using short commands without relying on menu clicks that may fail.**

---

## 💻 Player Commands



| Command | Description |
| :---: | :--- |
| `/vf` or `/vfilter` | **Opens the main help menu with all commands.** |
| `/isg <slot> <amount|all>` | **Withdraws items from the ISF virtual storage using the slot number.** |
| `/addasf /addisf /addabf` | **Adds the item in hand to the AutoSell, Storage, or Block filters.** |
| `/remasf /remisf /remabf` | **Removes the filter for the item in hand or from a specific slot.** |
| `/al` | **Toggles automatic item pickup (AutoLoot) on or off.** |
| `/afh` | **Toggles automatic block replenishment (AutoFill) on or off.** |
| `/vfcb` or `/chestdebug` | **Toggles report messages when breaking chests on or off.** |
| `/vflang <en|pt>` | **Changes the plugin's message language for the player.** |
| `/vfreload` | **Reloads plugin configurations and prices (Admin).** |

---

## 🔑 System Permissions

* **virtualfilter.admin** - **Full access to administration and reload commands.**
* **virtualfilter.chestdebug** - **Allows the player to see and toggle chest messages.**
* **virtualfilter.asf.<number>** - **Defines available slots in the AutoSell filter.**
* **virtualfilter.isf.<number>** - **Defines available slots in the InfinityStack filter.**
* **virtualfilter.abf.<number>** - **Defines available slots in the AutoBlock filter.**

---
*Developed by **comonier**.*

</div>
