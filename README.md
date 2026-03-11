**VirtualFilter**
Last Update: v1.7.7
The ultimate item management system with virtual storage, auto-sell, and container protection.

**Main Features**
│➜ Filter Decision Engine: Smart hierarchy:
│===========================================
│➜ AutoSell ASF > InfinityStorageFilter ISF > InfinityStackEdit ISFE > AutoBlockFilter ABF > Inventory.
│➜ It means a item only will get on player inventory after check all filters.
│===========================================
│➜ **AutoSell (ASF):** Sell items automatically based on customizable
│➜ prices in prices.yml (Vanilla items only).
│➜ **InfinityStack (ISF):** Infinite virtual storage for stackable 
│➜ vanilla items, accessible by commands or GUI.
│➜ **InfinityStackEdit (ISFE):** NEW! Infinite virtual storage 
│➜ exclusive for EDITED items (Slimefun, RPG, Custom NBT).
│➜ **AutoBlock (ABF):** Automatically blocks vanilla items from 
│➜ entering the player's inventory.
│➜ **AutoFillHand (AFH):** Integrated with ISF! Automatically refills your
│➜ hand with blocks from virtual storage while building.
│➜ **SafeDrop (SD):** NEW! Toggles 10s protection for dropped items.
│➜ Shows countdown and instructions in chat when enabled.
│➜ **Independent Logs (New):** Full control via /lo (Personal) and /la
│➜ (Nearby players) logs. Shows player names and destinations.
│➜ **AutoLoot & Magnet:** Advanced 10-block radius item pickup with a
│➜ 2-second sound cooldown to prevent audio spam.
│➜ **Bedrock & Geyser Focus:** Full support for mobile/console players
│➜ via specific chat commands. ( /getisf <slotid> amount/pack/all )
│➜ **Chest Guard System:** Prevents item loss when breaking
│➜ containers. Processes items into ISF/ISFE or Inventory.
│➜ **Smart Grouped Reports:** Summarizes results into a single message
│➜ (ISF, ISFE, INV, or GROUND) showing the player's name.
│➜ **Protection Integration:** Advanced NBT shield to protect custom
│➜ items. Edited items bypass vanilla filters to prevent data loss.
│===========================================
│➜ **Shulker Box Integrity:** Complete protection for Shulker Boxes.
│===========================================
│➜ Shulker boxes now bypass all filters (ASF/ISF/ISFE/ABF) to 
│➜ preserve internal NBT data and items when mined.
│➜ **Smart Routing:** Mined shulkers follow a priority path:
│➜ Inventory > EnderChest > Ground (Fallback).
│===========================================
│➜ **Improved Localization:** Fully updated messages_pt.yml and
│➜ messages_en.yml with new syntax and ISFE keys.


**Commands**
│➜ **/vf help** | **/vf** Opens the main help menu with all available features.
│➜ **/isf** | **/asf** | **/abf** | **/isfe** Opens the GUI for each specific filter.
│➜ **/getisf** | **/getisfe <slotid> <amount|all>** Withdraws items from ISF 
│➜ or ISFE using the Slot ID. (Essential for Bedrock players).
│➜ **/addisf** | **/addasf** | **/addabf** Adds the vanilla item in hand to filters.
│➜ **/addisfe** Adds the edited/custom item in hand to ISFE storage.
│➜ **/remisf** | **/remasf** | **/remabf** | **/remisfe [slotid]** Removes a filter.
│➜ **/al** | **/afh** Toggles AutoLoot (pickup) or AutoFillHand (refill).
│➜ **/lo** | **/la** Toggles Personal Loot Report Own/All (Nearby).
│➜ **/sd** | **/safedrop** Toggles 10s drop protection for other players.
│➜ **/vfat** Toggles sale and refill notifications in the Action Bar.
│➜ **/vflang** Switches personal language between English (en) and 
│➜ Portuguese (pt).
│➜ **/vfreload** **virtualfilter.admin** Reloads all configs (Admin only).

**Permissions**
│➜ **virtualfilter.admin** Permission for reload and admin commands.
│➜ **virtualfilter.chestdebug** Permission to toggle chest break reports.
│➜ **virtualfilter.isf.<number>** Slots available in ISF/ASF/ABF.
│➜ **virtualfilter.isfe.<number>** Slots available in InfinityStackEdit.

**Important Notice**
│➜ ISFE (Edited Storage) only accepts items with custom names.
│➜ Vanilla filters (ASF/ISF/ABF) now ignore all custom/edited items
│➜ to prevent NBT corruption (Slimefun compatibility).
│➜ Vault and a valid Economy plugin are required for AutoSell.
