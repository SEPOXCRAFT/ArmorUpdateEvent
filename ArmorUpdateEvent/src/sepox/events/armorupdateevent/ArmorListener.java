package sepox.events.armorupdateevent;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import sepox.events.armorupdateevent.ArmorUpdateEvent.EquipMethod;

public class ArmorListener implements Listener{

	private final List<String> blockedMaterials;
	JavaPlugin plugin;

	public ArmorListener(List<String> blockedMaterials, JavaPlugin plugin){
		this.plugin = plugin;
		this.blockedMaterials = blockedMaterials;
	}
	//Event Priority is highest because other plugins might cancel the events before we check.

	@EventHandler(priority =  EventPriority.HIGHEST, ignoreCancelled = true)
	public final void inventoryClick(final InventoryClickEvent e){
		boolean shift = false, numberkey = false;
		if(e.isCancelled()) return;
		if(e.getAction() == InventoryAction.NOTHING) return;// Why does this get called if nothing happens??
		if(e.getClick().equals(ClickType.SHIFT_LEFT) || e.getClick().equals(ClickType.SHIFT_RIGHT)){
			shift = true;
		}
		if(e.getClick().equals(ClickType.NUMBER_KEY)){
			numberkey = true;
		}
		if(e.getSlotType() != SlotType.ARMOR && e.getSlotType() != SlotType.QUICKBAR && e.getSlotType() != SlotType.CONTAINER) return;
		if(e.getClickedInventory() != null && !e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
		if (!e.getInventory().getType().equals(InventoryType.CRAFTING) && !e.getInventory().getType().equals(InventoryType.PLAYER)) return;
		if(!(e.getWhoClicked() instanceof Player)) return;
		ArmorType newArmorType = ArmorType.matchType(shift ? e.getCurrentItem() : e.getCursor());
		if(!shift && newArmorType != null && e.getRawSlot() != newArmorType.getSlot()){
			// Used for drag and drop checking to make sure you aren't trying to place a helmet in the boots slot.
			return;
		}
		if(shift){
			newArmorType = ArmorType.matchType(e.getCurrentItem());
			if(newArmorType != null){
				boolean equipping = true;
				if(e.getRawSlot() == newArmorType.getSlot()){
					equipping = false;
				}
				if(newArmorType.equals(ArmorType.HELMET) && (equipping ? isAirOrNull(e.getWhoClicked().getInventory().getHelmet()) : !isAirOrNull(e.getWhoClicked().getInventory().getHelmet())) || newArmorType.equals(ArmorType.CHESTPLATE) && (equipping ? isAirOrNull(e.getWhoClicked().getInventory().getChestplate()) : !isAirOrNull(e.getWhoClicked().getInventory().getChestplate())) || newArmorType.equals(ArmorType.LEGGINGS) && (equipping ? isAirOrNull(e.getWhoClicked().getInventory().getLeggings()) : !isAirOrNull(e.getWhoClicked().getInventory().getLeggings())) || newArmorType.equals(ArmorType.BOOTS) && (equipping ? isAirOrNull(e.getWhoClicked().getInventory().getBoots()) : !isAirOrNull(e.getWhoClicked().getInventory().getBoots()))){
					ArmorUpdateEvent armorEquipEvent = new ArmorUpdateEvent((Player) e.getWhoClicked(), EquipMethod.SHIFT_CLICK, newArmorType, equipping ? null : e.getCurrentItem(), equipping ? e.getCurrentItem() : null);
					Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
					if(armorEquipEvent.isCancelled()){
						e.setCancelled(true);
					}
				}
			}
		}else{
			ItemStack newArmorPiece = e.getCursor();
			ItemStack oldArmorPiece = e.getCurrentItem();
			if(numberkey){
				if(e.getClickedInventory().getType().equals(InventoryType.PLAYER)){// Prevents shit in the 2by2 crafting
					// e.getClickedInventory() == The players inventory
					// e.getHotBarButton() == key people are pressing to equip or unequip the item to or from.
					// e.getRawSlot() == The slot the item is going to.
					// e.getSlot() == Armor slot, can't use e.getRawSlot() as that gives a hotbar slot ;-;
					ItemStack hotbarItem = e.getClickedInventory().getItem(e.getHotbarButton());
					if(!isAirOrNull(hotbarItem)){// Equipping
						newArmorType = ArmorType.matchType(hotbarItem);
						newArmorPiece = hotbarItem;
						oldArmorPiece = e.getClickedInventory().getItem(e.getSlot());
					}else{// Unequipping
						newArmorType = ArmorType.matchType(!isAirOrNull(e.getCurrentItem()) ? e.getCurrentItem() : e.getCursor());
					}
				}
			}else{
				if(isAirOrNull(e.getCursor()) && !isAirOrNull(e.getCurrentItem())){// unequip with no new item going into the slot.
					newArmorType = ArmorType.matchType(e.getCurrentItem());
				}
				// e.getCurrentItem() == Unequip
				// e.getCursor() == Equip
				// newArmorType = ArmorType.matchType(!isAirOrNull(e.getCurrentItem()) ? e.getCurrentItem() : e.getCursor());
			}
			if(newArmorType != null && e.getRawSlot() == newArmorType.getSlot()){
				EquipMethod method = EquipMethod.PICK_DROP;
				if(e.getAction().equals(InventoryAction.HOTBAR_SWAP) || numberkey) method = EquipMethod.HOTBAR_SWAP;
				ArmorUpdateEvent armorEquipEvent = new ArmorUpdateEvent((Player) e.getWhoClicked(), method, newArmorType, oldArmorPiece, newArmorPiece);
				Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
				if(armorEquipEvent.isCancelled()){
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void consoleChatEvent(ServerCommandEvent event) {
		if (event.getCommand().split(" ").length < 2) return;
		String[] command = event.getCommand().split(" ");
		String arg0 = command[1];
		String cmd = command[0];
		Player player = Bukkit.getServer().getPlayer(arg0);
		
		if (cmd.equalsIgnoreCase("clear")) {
			ArmorUpdateEvent armorEquipEvent = new ArmorUpdateEvent(player, EquipMethod.PLUGIN, ArmorType.matchType(new ItemStack(Material.AIR)), null, new ItemStack(Material.AIR));
			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
			if(armorEquipEvent.isCancelled()){
				event.setCancelled(true);
				player.updateInventory();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerChatEvent(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String cmd = event.getMessage();
		String[] args = cmd.split(" ", 2);
		if (args.length > 1) {
			args = args[1].split(" ");
			for (String s : args) {
				Player tempplr = Bukkit.getServer().getPlayer(s);
				if (tempplr != null && !tempplr.equals(null)) {
					player = tempplr;
				}
			}
		}
		
		final Player theplayer = player;
		
		ItemStack oldHelmet = player.getInventory().getHelmet();
    	ItemStack oldChestplate = player.getInventory().getChestplate();
    	ItemStack oldLeggings = player.getInventory().getLeggings();
    	ItemStack oldBoots = player.getInventory().getBoots();
		new BukkitRunnable() {
	        
            @Override
            public void run() {
            	ItemStack newHelmet = theplayer.getInventory().getHelmet();
            	ItemStack newChestplate = theplayer.getInventory().getChestplate();
            	ItemStack newLeggings = theplayer.getInventory().getLeggings();
            	ItemStack newBoots = theplayer.getInventory().getBoots();
            	
            	ArmorUpdateEvent armorEquipEvent = new ArmorUpdateEvent(theplayer, EquipMethod.PLUGIN, ArmorType.matchType(newHelmet), oldHelmet, newHelmet);
    			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
    			if(armorEquipEvent.isCancelled()){
    				event.setCancelled(true);
    				theplayer.updateInventory();
    			}
    			
    			armorEquipEvent = new ArmorUpdateEvent(theplayer, EquipMethod.PLUGIN, ArmorType.matchType(newChestplate), oldChestplate, newChestplate);
    			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
    			if(armorEquipEvent.isCancelled()){
    				event.setCancelled(true);
    				theplayer.updateInventory();
    			}
    			
    			armorEquipEvent = new ArmorUpdateEvent(theplayer, EquipMethod.PLUGIN, ArmorType.matchType(newLeggings), oldLeggings, newLeggings);
    			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
    			if(armorEquipEvent.isCancelled()){
    				event.setCancelled(true);
    				theplayer.updateInventory();
    			}
    			
    			armorEquipEvent = new ArmorUpdateEvent(theplayer, EquipMethod.PLUGIN, ArmorType.matchType(newBoots), oldBoots, newBoots);
    			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
    			if(armorEquipEvent.isCancelled()){
    				event.setCancelled(true);
    				theplayer.updateInventory();
    			}
            }
            
		}.runTaskLater(plugin, ticks(100));
		
		/*if (command.equalsIgnoreCase("/clear")) {
			if (args.length > 1)
				if (args[0].length() > 0)
					player = Bukkit.getServer().getPlayer(args[0]);
			ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, EquipMethod.PLUGIN, ArmorType.matchType(new ItemStack(Material.AIR)), null, new ItemStack(Material.AIR));
			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
			if(armorEquipEvent.isCancelled()){
				event.setCancelled(true);
				player.updateInventory();
			}
		}*/
	}
	
	@EventHandler(ignoreCancelled = true, priority =  EventPriority.LOWEST)
	public void playerInteractEvent(PlayerInteractEvent e){
		if(e.useItemInHand().equals(Result.DENY))return;
		//
		if(e.getAction() == Action.PHYSICAL) return;
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
			Player player = e.getPlayer();
			if(!e.useInteractedBlock().equals(Result.DENY)){
				if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && (e.getClickedBlock().getType().equals(Material.SIGN) || e.getClickedBlock().getType().equals(Material.SIGN_POST) || e.getClickedBlock().getType().equals(Material.WALL_SIGN))) {
					ItemStack oldHelmet = e.getPlayer().getInventory().getHelmet();
	            	ItemStack oldChestplate = e.getPlayer().getInventory().getChestplate();
	            	ItemStack oldLeggings = e.getPlayer().getInventory().getLeggings();
	            	ItemStack oldBoots = e.getPlayer().getInventory().getBoots();
					new BukkitRunnable() {
				        
			            @Override
			            public void run() {
							ArmorUpdateEvent armorEquipEvent = new ArmorUpdateEvent(e.getPlayer(), EquipMethod.PLUGIN, ArmorType.matchType(e.getPlayer().getInventory().getHelmet()), oldHelmet, e.getPlayer().getInventory().getHelmet());
							Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
							if(armorEquipEvent.isCancelled()){
								e.setCancelled(true);
								player.updateInventory();
							}
							ArmorUpdateEvent armorEquipEvent2 = new ArmorUpdateEvent(e.getPlayer(), EquipMethod.PLUGIN, ArmorType.matchType(e.getPlayer().getInventory().getChestplate()), oldChestplate, e.getPlayer().getInventory().getChestplate());
							Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent2);
							if(armorEquipEvent2.isCancelled()){
								e.setCancelled(true);
								player.updateInventory();
							}
							ArmorUpdateEvent armorEquipEvent3 = new ArmorUpdateEvent(e.getPlayer(), EquipMethod.PLUGIN, ArmorType.matchType(e.getPlayer().getInventory().getLeggings()), oldLeggings, e.getPlayer().getInventory().getLeggings());
							Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent3);
							if(armorEquipEvent3.isCancelled()){
								e.setCancelled(true);
								player.updateInventory();
							}
							ArmorUpdateEvent armorEquipEvent4 = new ArmorUpdateEvent(e.getPlayer(), EquipMethod.PLUGIN, ArmorType.matchType(e.getPlayer().getInventory().getBoots()), oldBoots, e.getPlayer().getInventory().getBoots());
							Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent4);
							if(armorEquipEvent4.isCancelled()){
								e.setCancelled(true);
								player.updateInventory();
							}
			            }
			            
			        }.runTaskLater(this.plugin, ticks(100));
				}
				if(e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()){// Having both of these checks is useless, might as well do it though.
					// Some blocks have actions when you right click them which stops the client from equipping the armor in hand.
					Material mat = e.getClickedBlock().getType();
					for(String s : blockedMaterials){
						if(mat.name().equalsIgnoreCase(s)) return;
					}
				}
			}
			ArmorType newArmorType = ArmorType.matchType(e.getItem());
			if(newArmorType != null){
				if(newArmorType.equals(ArmorType.HELMET) && isAirOrNull(e.getPlayer().getInventory().getHelmet()) || newArmorType.equals(ArmorType.CHESTPLATE) && isAirOrNull(e.getPlayer().getInventory().getChestplate()) || newArmorType.equals(ArmorType.LEGGINGS) && isAirOrNull(e.getPlayer().getInventory().getLeggings()) || newArmorType.equals(ArmorType.BOOTS) && isAirOrNull(e.getPlayer().getInventory().getBoots())){
					ArmorUpdateEvent armorEquipEvent = new ArmorUpdateEvent(e.getPlayer(), EquipMethod.HOTBAR, ArmorType.matchType(e.getItem()), null, e.getItem());
					Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
					if(armorEquipEvent.isCancelled()){
						e.setCancelled(true);
						player.updateInventory();
					}
				}
			}
		}
	}
	
	private long ticks(int millis) {
		int defaultTicks = 20;
		return defaultTicks * (millis / 1000);
	}

	@EventHandler(priority =  EventPriority.HIGHEST, ignoreCancelled = true)
	public void inventoryDrag(InventoryDragEvent event){
		// getType() seems to always be even.
		// Old Cursor gives the item you are equipping
		// Raw slot is the ArmorType slot
		// Can't replace armor using this method making getCursor() useless.
		ArmorType type = ArmorType.matchType(event.getOldCursor());
		if(event.getRawSlots().isEmpty()) return;// Idk if this will ever happen
		if(type != null && type.getSlot() == event.getRawSlots().stream().findFirst().orElse(0)){
			ArmorUpdateEvent armorEquipEvent = new ArmorUpdateEvent((Player) event.getWhoClicked(), EquipMethod.DRAG, type, null, event.getOldCursor());
			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
			if(armorEquipEvent.isCancelled()){
				event.setResult(Result.DENY);
				event.setCancelled(true);
			}
		}
		// Debug shit
		/*System.out.println("Slots: " + event.getInventorySlots().toString());
		System.out.println("Raw Slots: " + event.getRawSlots().toString());
		if(event.getCursor() != null){
			System.out.println("Cursor: " + event.getCursor().getType().name());
		}
		if(event.getOldCursor() != null){
			System.out.println("OldCursor: " + event.getOldCursor().getType().name());
		}
		System.out.println("Type: " + event.getType().name());*/
	}

	@EventHandler
	public void itemBreakEvent(PlayerItemBreakEvent e){
		ArmorType type = ArmorType.matchType(e.getBrokenItem());
		if(type != null){
			Player p = e.getPlayer();
			ArmorUpdateEvent armorEquipEvent = new ArmorUpdateEvent(p, EquipMethod.BROKE, type, e.getBrokenItem(), null);
			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
			if(armorEquipEvent.isCancelled()){
				ItemStack i = e.getBrokenItem().clone();
				i.setAmount(1);
				i.setDurability((short) (i.getDurability() - 1));
				if(type.equals(ArmorType.HELMET)){
					p.getInventory().setHelmet(i);
				}else if(type.equals(ArmorType.CHESTPLATE)){
					p.getInventory().setChestplate(i);
				}else if(type.equals(ArmorType.LEGGINGS)){
					p.getInventory().setLeggings(i);
				}else if(type.equals(ArmorType.BOOTS)){
					p.getInventory().setBoots(i);
				}
			}
		}
	}

	@EventHandler
	public void playerDeathEvent(PlayerDeathEvent e){
		Player p = e.getEntity();
		if(e.getKeepInventory()) return;
		for(ItemStack i : p.getInventory().getArmorContents()){
			if(!isAirOrNull(i)){
				Bukkit.getServer().getPluginManager().callEvent(new ArmorUpdateEvent(p, EquipMethod.DEATH, ArmorType.matchType(i), i, null));
				// No way to cancel a death event.
			}
		}
	}

	/**
	 * A utility method to support versions that use null or air ItemStacks.
	 */
	public static boolean isAirOrNull(ItemStack item){
		return item == null || item.getType().equals(Material.AIR);
	}
}