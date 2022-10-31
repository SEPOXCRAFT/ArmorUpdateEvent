package sepox.events.armorupdateevent;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

public class Ignore extends JavaPlugin {
	private final List<String> bm = new ArrayList<String>();
	
	public void onEnable() {
		addBlockedMaterials();
		getServer().getPluginManager().registerEvents(new ArmorListener(bm, this), this);
	}
	
	private void addBlockedMaterials() {
		String[] bm2 = new String[] {
				"FURNACE"
				, "CHEST"
				, "TRAPPED_CHEST"
				, "BEACON"
				, "DISPENSER"
				, "DROPPER"
				, "HOPPER"
				, "WORKBENCH"
				, "ENCHANTMENT_TABLE"
				, "ENDER_CHEST"
				, "ANVIL"
				, "BED_BLOCK"
				, "FENCE_GATE"
				, "SPRUCE_FENCE_GATE"
				, "BIRCH_FENCE_GATE"
				, "ACACIA_FENCE_GATE"
				, "JUNGLE_FENCE_GATE"
				, "DARK_OAK_FENCE_GATE"
				, "IRON_DOOR_BLOCK"
				, "WOODEN_DOOR"
				, "SPRUCE_DOOR"
				, "BIRCH_DOOR"
				, "JUNGLE_DOOR"
				, "ACACIA_DOOR"
				, "DARK_OAK_DOOR"
				, "WOOD_BUTTON"
				, "STONE_BUTTON"
				, "TRAP_DOOR"
				, "IRON_TRAPDOOR"
				, "DIODE_BLOCK_OFF"
				, "DIODE_BLOCK_ON"
				, "REDSTONE_COMPARATOR_OFF"
				, "REDSTONE_COMPARATOR_ON"
				, "FENCE"
				, "SPRUCE_FENCE"
				, "BIRCH_FENCE"
				, "JUNGLE_FENCE"
				, "DARK_OAK_FENCE"
				, "ACACIA_FENCE"
				, "NETHER_FENCE"
				, "BREWING_STAND"
				, "CAULDRON"
				, "LEGACY_SIGN_POST"
				, "LEGACY_WALL_SIGN"
				, "LEGACY_SIGN"
				, "ACACIA_SIGN"
				, "ACACIA_WALL_SIGN"
				, "BIRCH_SIGN"
				, "BIRCH_WALL_SIGN"
				, "DARK_OAK_SIGN"
				, "DARK_OAK_WALL_SIGN"
				, "JUNGLE_SIGN"
				, "JUNGLE_WALL_SIGN"
				, "OAK_SIGN"
				, "OAK_WALL_SIGN"
				, "SPRUCE_SIGN"
				, "SPRUCE_WALL_SIGN"
				, "LEVER"
				, "BLACK_SHULKER_BOX"
				, "BLUE_SHULKER_BOX"
				, "BROWN_SHULKER_BOX"
				, "CYAN_SHULKER_BOX"
				, "GRAY_SHULKER_BOX"
				, "GREEN_SHULKER_BOX"
				, "LIGHT_BLUE_SHULKER_BOX"
				, "LIME_SHULKER_BOX"
				, "MAGENTA_SHULKER_BOX"
				, "ORANGE_SHULKER_BOX"
				, "PINK_SHULKER_BOX"
				, "PURPLE_SHULKER_BOX"
				, "RED_SHULKER_BOX"
				, "SILVER_SHULKER_BOX"
				, "WHITE_SHULKER_BOX"
				, "YELLOW_SHULKER_BOX"
				, "DAYLIGHT_DETECTOR_INVERTED"
				, "DAYLIGHT_DETECTOR"
				, "BARREL"
				, "BLAST_FURNACE"
				, "SMOKER"
				, "CARTOGRAPHY_TABLE"
				, "COMPOSTER"
				, "GRINDSTONE"
				, "LECTERN"
				, "LOOM"
				, "STONECUTTER"
				, "BELL"
		};
		
		for (String s : bm2)
			bm.add(s);
	}
}
