#VirtualFilter<br>
Last Update: 1.7.3<br>
The ultimate item management system with virtual storage, auto-sell, and container protection.<br>

##Main Features<br>
│➜ Filter Decision Engine (New): Smart hierarchy:<br>
│➜ AutoSell ➔ ISF ➔ AutoBlock ➔ Inventory.<br>
│➜ AutoSell (ASF): Sell items automatically based on customizable<br>
│➜ prices in prices.yml<br>
│➜ InfinityStack (ISF): Infinite virtual storage for blocks and ores,<br>
│➜ accessible by commands or a professional GUI.<br>
│➜ AutoBlock (ABF): Automatically compacts ores and materials into<br>
│➜ blocks (e.g., Diamonds ➔ Diamond Blocks) upon collection.<br>
│➜ AutoFillHand (AFH): Integrated with ISF! Automatically refills your<br>
│➜ hand with blocks from virtual storage while building.<br>
│➜ Independent Logs (New): Full control via /lo (Personal) and /la<br>
│➜ (Nearby players) logs.<br>
│➜ AutoLoot & Magnet: Advanced 10-block radius item pickup with a<br>
│➜ 2-second sound cooldown to prevent audio spam.<br>
│➜ Bedrock Compatibility: Full support for GeyserMC with specific<br>
│➜ commands for slot-based withdrawal and filter management.<br>
│➜ Bedrock & Geyser Focus: Full support for mobile/console players<br>
│➜ via specific chat commands. ( /isg <slotid> amount/pack/all )<br>
│➜ Chest Guard System: Prevents item loss when breaking<br>
│➜ containers. Processes items into ISF or Inventory before dropping<br>
│➜ leftovers.<br>
│➜ Smart Grouped Reports: Summarizes collection results into a<br>
│➜ single, clean, color-coded chat message (ISF, INV, or GROUND).<br>
│➜ Protection Integration: Advanced NBT shield to protect custom<br>
│➜ items from Slimefun, mcMMO, and special Bedrock metadata.<br>

##Commands<br>
│➜ vf help / vf Opens the main help menu with all available features.<br>
│➜ isf / asf / abf Opens the GUI for InfinityStack, AutoSell, or AutoBlock filters.<br>
│➜ isg <slotid> <amount|pack|all> Withdraws items from virtual storage<br>
│➜ using the Slot ID. (Unique way to bedrock get packs on isf)<br>
│➜ addisf / addasf / addabf Adds the item in your hand to the<br>
│➜ respective filter. (Unique way to bedrock add filters)<br>
│➜ remisf / remasf / remabf [slotid] Removes a filter by held item or<br>
│➜ specific Slot ID. (Unique way to bedrock rem filters)<br>
│➜ al / afh Toggles AutoLoot (pickup) or AutoFillHand (refill) features.<br>
│➜ lo / la Toggles Personal Loot Report Own/All<br>
│➜ vfat Toggles sale and refill notifications in the Action Bar.<br>
│➜ vflang Switches personal language between English (en) and<br>
│➜ Portuguese (pt).<br>
│➜ vfreload virtualfilter.admin Reloads the configurations, messages,<br>
│➜ and prices (Admin only).<br>

##Permissions<br>
│➜ virtualfilter.admin Permission to use reload and administrative<br>
│➜ commands.<br>
│➜ virtualfilter.chestdebug Permission to toggle the chest break<br>
│➜ report messages.<br>
│➜ virtualfilter.isf.<number> Defines the amount of slots available<br>
│➜ in InfinityStack.<br>
│➜ virtualfilter.asf.<number> Defines the amount of slots available<br>
│➜ in AutoSell.<br>
│➜ virtualfilter.abf.<number> Defines the amount of slots available<br>
│➜ in AutoBlock.<br>

##Important Notice<br>
│➜ For the AutoSell feature to work, a valid Economy plugin and<br>
│➜ Vault must be installed.<br>
│➜ To prevent "Message not found" errors after updating to v1.5,<br>
│➜ please delete your old `messages.yml` or add the new chest debug keys.<br>
