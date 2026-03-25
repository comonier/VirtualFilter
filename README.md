# VirtualFilter
**Version:** 1.8.2
**Developer:** Comonier  
**Compatibility:** Paper/Spigot 1.21.1+ (Java 21)

The ultimate item management system with virtual filters and NBT integrity.

$$\huge\color{red}\textbf{Acronym naming tutorial} $$

➜ **ISF** : Infinity Storage Filter (Vanilla)  
➜ **ISFe** : Infinity Storage Filter Edited (Custom NBT/Slimefun)  
➜ **ASF** : Auto Sell Filter  
➜ **ABF** : Auto Block Filter  
➜ **AFH** : Auto Fill Hand  
➜ **AL** : Auto Loot  
➜ **SD** : Safe Drop  

$$\huge\color{red}\textbf{Main Features}$$

│➜ **Filter Decision Engine:** Smart hierarchy:  
│===========================================  
│➜ **ASF > ISF > ISFE > ABF > Inventory.**  
│➜ A item will only reach the player inventory after passing all filter checks.  
│===========================================  
│➜ **Dual-Database Architecture (New!):**  
│➜ **storage.db:** Safe storage for Vanilla items and player settings.  
│➜ **storage_edit.db:** Isolated high-performance binary storage for ISFE.  
│===========================================  
│➜ **NBT DNA Shield (Slimefun Compatible):**  
│➜ **Binary Serialization:** ISFE now uses BLOB (Binary Large Object)  
│➜ storage to preserve all 14+ item components and hidden NBT tags.  
│➜ **"Pick Block" Logic:** Clones the original item template during  
│➜ withdraw to ensure custom machines (Slimefun/RPG) recognize them.  
│===========================================  
│➜ **AutoSell (ASF):** Sell items automatically based on customizable  
│➜ prices in prices.yml (Vanilla items only).  
│➜ **InfinityStack (ISF):** Infinite virtual storage for stackable  
│➜ vanilla items.  
│➜ **AutoFillHand (AFH):** Integrated with ISF! Automatically refills your  
│➜ hand with blocks from virtual storage while building.  
│➜ **SafeDrop (SD):** Toggles 10s protection for dropped items with  
│➜ visual countdown in chat.  
│➜ **Independent Logs:** Full control via /lo (Personal) and /la  
│➜ (Nearby players) logs.  
│➜ **AutoLoot & Magnet:** Advanced pickup with a 2-second sound  
│➜ cooldown to prevent audio spam.  
│➜ **Bedrock & Geyser Focus:** Full support for mobile players via  
│➜ specific chat commands (e.g., /getisf <slot> <amount>).  
│➜ **Shulker Box Integrity:** Mined shulkers bypass all filters to  
│➜ preserve internal data (Inventory > EnderChest > Ground).  

$$\huge\color{red}\textbf{Commands} $$

│➜ **/vf help** | **/vf** Opens the main help menu.  
│➜ **/isf** | **/asf** | **/abf** | **/isfe** Opens the specific filter GUI.  
│➜ **/getisf** | **/getisfe <slotid> <amount|all>** Withdraws items from storage.  
│➜ **/addisf** | **/addasf** | **/addabf** Adds hand item to vanilla filters.  
│➜ **/addisfe** Adds the custom/NBT item in hand to ISFE (Binary Storage).  
│➜ **/remisf** | **/remasf** | **/remabf** | **/remisfe [slotid]** Removes a filter.  
│➜ **/al** | **/afh** Toggles AutoLoot or AutoFillHand.  
│➜ **/lo** | **/la** Toggles Loot Reports (Own/Nearby).  
│➜ **/sd** | **/safedrop** Toggles 10s drop protection.  
│➜ **/vfat** Toggles Action Bar notifications.  
│➜ **/vflang** Switches language (en/pt).  
│➜ **/vfreload** Reloads all configs and databases (**virtualfilter.admin**).  

$$\huge\color{red}\textbf{Permissions}$$

│➜ **virtualfilter.admin** Access to reload and admin tools.  
│➜ **virtualfilter.chestdebug** Toggle chest break detailed reports.  
│➜ **virtualfilter.isf.<number>** Max slots available in ISF.  
│➜ **virtualfilter.isfe.<number>** Max slots available in ISFe.  
│➜ **virtualfilter.asf.<number>** Max slots available in ASF.  
│➜ **virtualfilter.abf.<number>** Max slots available in ABF.  

$$\huge\color{red}\textbf{Important Notice} $$

│➜ **ISFE (Edited Storage)** now requires its own database file.  
│➜ **Data Integrity:** Vanilla filters (ASF/ISF/ABF) automatically ignore  
│➜ custom items to prevent NBT corruption.  
│➜ **Requirements:** Vault and an Economy plugin are needed for ASF.  
