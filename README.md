# VirtualFilter
**Last Update: 1.7.3**

The ultimate item filtering and virtual storage system for Paper 1.21.1, featuring infinite stacks, automatic sales, and full Bedrock (Geyser) compatibility.


**Main Features**
│➜ **Triple-Filter System (ISF, ASF, ABF):** Manage item<br>
collection with InfinityStack, AutoSell, and AutoBlock.
│➜ **Bedrock & Geyser Focus:** Dedicated chat commands
(/isg, /add, /rem) for players without mouse shortcuts.
│➜ **Auto-Shift Logic:** Intelligent slot reorganization.
Removing a filter shifts others to fill gaps automatically.
│➜ **Smart Merge Technology:** One-click (Shift+Left) to 
    instantly pull all items from inventory into ISF storage.
│➜ **Native Economy Engine:** Vault integration for AutoSell 
    based on prices defined in the prices.yml file.
│➜ **Independent Log System:** Separate toggles for Personal 
    Loot Logs (/lo) and Nearby Player Logs (/la) within 32m.
│➜ **AutoFillHand (AFH):** Automatic block replenishment from 
    physical inventory or ISF virtual stock while building.
│➜ **AutoLoot (AL):** High-performance item collection for 
    mining, fishing, and mob drops with protection metadata.
│➜ **Anti-Spam Block Alerts:** Visual notifications for blocked 
    items (ABF) limited to once per item type to keep chat clean.
│➜ **Total Alert System:** Action Bar notifications for sales 
    and "Villager No" sound alerts for full inventories.
│➜ **SQLite Pro Core:** High-speed data integrity with WAL 
    journal mode and automatic database vacuuming/reindex.
│➜ **Dual Language System:** Instant switching between English 
    (en) and Portuguese (pt) via messages_xx.yml files.

**Commands**
│➜ **/vf help** Opens the main help menu with all available
    features.
│➜ **/isf | /asf | /abf** Opens the respective filter
    management GUI.
│➜ **/addisf | /addasf | /addabf [slot]** Adds held item to a
    filter.
│➜ **/remisf | /remasf | /remabf [slot]** Removes a filter (ID
    or Hand).
│➜ **/isg [slot] [all|pack|qty]** Withdraws ISF items via chat
    (Bedrock).
│➜ **/al** Toggles AutoLoot (Automatic item collection) on/off.
│➜ **/afh** Toggles AutoFillHand (Block replenishment) on/off.
│➜ **/lo** Toggles Personal Loot logs in chat.
│➜ **/la** Toggles Nearby Player Loot logs (32m radius).
│➜ **/vfat** Toggles AutoSell notifications on the Action Bar.
│➜ **/vfreload** Reloads all configurations, messages, and
    prices.

**Permissions**
│➜ **virtualfilter.admin** Full access to reload and admin
    commands.
│➜ **virtualfilter.isf.[1-54]** Defines max allowed slots for ISF.
│➜ **virtualfilter.asf.[1-54]** Defines max allowed slots for ASF.
│➜ **virtualfilter.abf.[1-54]** Defines max allowed slots for ABF.

**Important Notice**
│➜ **Vault Required:** This plugin requires Vault to process 
│➜ payments for the AutoSell feature.
│➜ **Java 21:** Ensure your server is running on Java 21 or 
│➜ higher for compatibility with the 1.21.1 core.
│➜ **Reporting:** If you encounter any bugs, please use our 
│➜ GitHub Issues section.

**Developed by: Comonier**
