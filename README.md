**VirtualFilter** <br>
Last Update: v1.7.8 <br>
The ultimate item management system with virtual filters:<br>

$$\huge\color{red}\textbf{Acronym naming tutorial} $$

➜ **ISF** : Infinity Storage Filter<br>
➜ **ISFe** : Infinity Storage Filter Edited (custom named items)<br>
➜ **ASF** : Auto Sell Filter<br>
➜ **ABF** : Auto Block Filter<br>
➜ **AFH** : Auto Fill Hand<br>
➜ **AL** : Auto Loot<br>
➜ **SD** : Safe Drop<br>

$$\huge\color{red}\textbf{Main Features}$$

│➜ **Filter Decision Engine:** Smart hierarchy: <br>
│=========================================== <br>
│➜ **ASF > ISF > ISFE > ABF > Inventory.** <br>
│➜ It means a item only will get on player inventory after check all filters. <br>
│=========================================== <br>
│➜ **AutoSell (ASF):** Sell items automatically based on customizable <br>
│➜ prices in prices.yml (Vanilla items only). <br>
│➜ **InfinityStack (ISF):** Infinite virtual storage for stackable <br>
│➜ vanilla items, accessible by commands or GUI. <br>
│➜ **InfinityStackEdit (ISFE):** NEW! Infinite virtual storage <br>
│➜ exclusive for EDITED items (Slimefun, RPG, Custom NBT). <br>
│➜ **AutoBlock (ABF):** Automatically blocks vanilla items from <br>
│➜ entering the player's inventory. <br>
│➜ **AutoFillHand (AFH):** Integrated with ISF! Automatically refills your <br>
│➜ hand with blocks from virtual storage while building. <br>
│➜ **SafeDrop (SD):** NEW! Toggles 10s protection for dropped items. <br>
│➜ Shows countdown and instructions in chat when enabled. <br>
│➜ **Independent Logs (New):** Full control via /lo (Personal) and /la <br>
│➜ (Nearby players) logs. Shows player names and destinations. <br>
│➜ **AutoLoot & Magnet:** Advanced 10-block radius item pickup with a <br>
│➜ 2-second sound cooldown to prevent audio spam. <br>
│➜ **Bedrock & Geyser Focus:** Full support for mobile/console players <br>
│➜ via specific chat commands. ( /getisf <slotid> amount/pack/all ) <br>
│➜ **Chest Guard System:** Prevents item loss when breaking <br>
│➜ containers. Processes items into ISF/ISFE or Inventory. <br>
│➜ **Smart Grouped Reports:** Summarizes results into a single message <br>
│➜ (ISF, ISFE, INV, or GROUND) showing the player's name. <br>
│➜ **Protection Integration:** Advanced NBT shield to protect custom <br>
│➜ items. Edited items bypass vanilla filters to prevent data loss. <br>
│=========================================== <br>
│➜ **Shulker Box Integrity:** Complete protection for Shulker Boxes. <br>
│=========================================== <br>
│➜ Shulker boxes now bypass all filters (ASF/ISF/ISFE/ABF) to <br>
│➜ preserve internal NBT data and items when mined. <br>
│➜ **Smart Routing:** Mined shulkers follow a priority path: <br>
│➜ Inventory > EnderChest > Ground (Fallback). <br>
│=========================================== <br>
│➜ **Improved Localization:** Fully updated messages_pt.yml and <br>
│➜ messages_en.yml with new syntax and ISFE keys. <br>

$$\huge\color{red}\textbf{Commands} $$

│➜ **/vf help** | **/vf** Opens the main help menu with all available features. <br>
│➜ **/isf** | **/asf** | **/abf** | **/isfe** Opens the GUI for each specific filter. <br>
│➜ **/getisf** | **/getisfe <slotid> <amount|all>** Withdraws items from ISF <br>
│➜ or ISFE using the Slot ID. (Essential for Bedrock players). <br>
│➜ **/addisf** | **/addasf** | **/addabf** Adds the vanilla item in hand to filters. <br>
│➜ **/addisfe** Adds the edited/custom item in hand to ISFE storage. <br>
│➜ **/remisf** | **/remasf** | **/remabf** | **/remisfe [slotid]** Removes a filter. <br>
│➜ **/al** | **/afh** Toggles AutoLoot (pickup) or AutoFillHand (refill). <br>
│➜ **/lo** | **/la** Toggles Personal Loot Report Own/All (Nearby). <br>
│➜ **/sd** | **/safedrop** Toggles 10s drop protection for other players. <br>
│➜ **/vfat** Toggles sale and refill notifications in the Action Bar. <br>
│➜ **/vflang** Switches personal language between English (en) and <br>
│➜ Portuguese (pt). <br>
│➜ **/vfreload** **virtualfilter.admin** Reloads all configs (Admin only). <br>

$$\huge\color{red}\textbf{Permissions}$$

│➜ **virtualfilter.admin** Permission for reload and admin commands. <br>
│➜ **virtualfilter.chestdebug** Permission to toggle chest break reports. <br>
│➜ **virtualfilter.isf.<number>** Slots available in ISF<br>
│➜ **virtualfilter.isfe.<number>** Slots available in ISFe <br>
│➜ **virtualfilter.asf.<number>** Slots available ASF Filter<br>
│➜ **virtualfilter.abf.<number>** Slots available ABF Filter<br>

$$\huge\color{red}\textbf{Important Notice} $$

│➜ ISFE (Edited Storage) only accepts items with custom names. <br>
│➜ Vanilla filters (ASF/ISF/ABF) now ignore all custom/edited items <br>
│➜ to prevent NBT corruption (Slimefun compatibility). <br>
│➜ Vault and a valid Economy plugin are required for AutoSell. <br>
