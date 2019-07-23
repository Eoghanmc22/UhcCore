package com.gmail.val59000mc.customitems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.exceptions.UhcPlayerDoesntExistException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.utils.CompareUtils;
import com.gmail.val59000mc.utils.UniversalMaterial;
import com.gmail.val59000mc.utils.VersionUtils;

public class UhcItems {

	public static void giveLobbyItemTo(Player player){
		final MainConfiguration cfg = GameManager.getGameManager().getConfiguration();
		if (cfg.getMaxPlayersPerTeam() > 1 || !cfg.getTeamAlwaysReady()) {
			final ItemStack item = new ItemStack(Material.IRON_SWORD);
			final ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(Lang.ITEMS_SWORD);
			meta.setLore(Arrays.asList("Lobby"));
			item.setItemMeta(meta);
			player.getInventory().addItem(item);
		}
	}

	public static void giveBungeeItemTo(Player player){
		final GameManager gm = GameManager.getGameManager();
		if (gm.getConfiguration().getEnableBungeeSupport() && gm.getConfiguration().getEnableBungeeLobbyItem()){
			final ItemStack barrier = new ItemStack(Material.BARRIER, 1);
			final ItemMeta barrierItemMeta = barrier.getItemMeta();
			barrierItemMeta.setDisplayName(Lang.ITEMS_BUNGEE);
			barrier.setItemMeta(barrierItemMeta);
			player.getInventory().setItem(8, barrier);
		}
	}

	public static boolean isLobbyItem(ItemStack item){
		return (
				item != null
				&& item.getType().equals(Material.IRON_SWORD)
				&& item.getItemMeta().getLore().contains("Lobby")
				);
	}

	public static boolean isLobbyBarrierItem(ItemStack item){
		return (
				item != null &&
				item.getType().equals(Material.BARRIER) &&
				item.hasItemMeta() &&
				item.getItemMeta().hasDisplayName() &&
				item.getItemMeta().getDisplayName().equals(Lang.ITEMS_BUNGEE)
				);
	}

	public static void openTeamInventory(Player player){
		final int maxSlots = 6*9;
		final Inventory inv = Bukkit.createInventory(null, maxSlots, ChatColor.GREEN+Lang.DISPLAY_MESSAGE_PREFIX+" "+ChatColor.DARK_GREEN+Lang.TEAM_INVENTORY);
		int slot = 0;
		final GameManager gm = GameManager.getGameManager();
		final List<UhcTeam> teams = gm.getPlayersManager().listUhcTeams();
		for(final UhcTeam team : teams)
			if(slot < maxSlots){
				final ItemStack item = createTeamSkullItem(team);
				inv.setItem(slot, item);
				slot++;
			}

		// Leave team item
		if(!gm.getConfiguration().getPreventPlayerFromLeavingTeam()){
			final ItemStack leaveTeamItem = new ItemStack(Material.BARRIER);
			final ItemMeta imLeave = leaveTeamItem.getItemMeta();
			imLeave.setDisplayName(ChatColor.RED+Lang.ITEMS_BARRIER);
			leaveTeamItem.setItemMeta(imLeave);
			inv.setItem(maxSlots-1, leaveTeamItem);
		}

		UhcPlayer uhcPlayer;
		try {
			uhcPlayer = gm.getPlayersManager().getUhcPlayer(player);


			// Team ready/not ready item
			if(uhcPlayer.isTeamLeader() && !gm.getConfiguration().getTeamAlwaysReady()){


				// Red Wool
				ItemStack readyTeamItem = UniversalMaterial.RED_WOOL.getStack();

				String readyState = ChatColor.RED+Lang.TEAM_NOT_READY;

				if(uhcPlayer.getTeam().isReadyToStart()){
					// Lime Wool
					readyTeamItem = UniversalMaterial.LIME_WOOL.getStack();
					readyState = ChatColor.GREEN+Lang.TEAM_READY;
				}

				final ItemMeta imReady = readyTeamItem.getItemMeta();
				imReady.setDisplayName(readyState);
				final List<String> readyLore = new ArrayList<>();
				readyLore.add(ChatColor.GRAY+Lang.TEAM_READY_TOGGLE);
				imReady.setLore(readyLore);
				readyTeamItem.setItemMeta(imReady);
				inv.setItem(maxSlots-2, readyTeamItem);
			}

			player.openInventory(inv);
		} catch (final UhcPlayerDoesntExistException e) {
			e.printStackTrace();
		}

	}


	public static ItemStack createTeamSkullItem(UhcTeam team){
		final UhcPlayer leader = team.getLeader();
		final String leaderName = leader.getName();
		final ItemStack item = VersionUtils.getVersionUtils().createPlayerSkull(leaderName, leader.getUuid());
		final List<String> membersNames = team.getMembersNames();
		item.setAmount(membersNames.size());
		final ItemMeta im = item.getItemMeta();

		// Setting up lore with team members
		final List<String> teamLore = new ArrayList<>();
		teamLore.add(ChatColor.GREEN+"Members");
		for(final String teamMember : membersNames)
			teamLore.add(ChatColor.WHITE+teamMember);

		// Ready State
		if(team.isReadyToStart())
			teamLore.add(ChatColor.GREEN+"--- "+Lang.TEAM_READY+" ---");
		else
			teamLore.add(ChatColor.RED+"--- "+Lang.TEAM_NOT_READY+" ---");

		im.setLore(teamLore);

		im.setDisplayName(leaderName);
		item.setItemMeta(im);
		return item;
	}

	public static boolean isLobbyTeamItem(ItemStack item){
		if(item != null && item.getType() == UniversalMaterial.PLAYER_HEAD.getType()){
			final List<String> lore = item.getItemMeta().getLore();
			return CompareUtils.stringListContains(lore, ChatColor.GREEN+"Members") || CompareUtils.stringListContains(lore, Lang.TEAM_REQUEST_HEAD);
		}
		return false;
	}

	public static boolean isLobbyLeaveTeamItem(ItemStack item){
		return (
				item != null
				&& item.getType() == Material.BARRIER
				&& item.hasItemMeta()
				&& item.getItemMeta().getDisplayName().equals(ChatColor.RED+Lang.ITEMS_BARRIER)
				);
	}



	public static boolean isLobbyReadyTeamItem(ItemStack item) {
		return (
				item != null
				&& (item.getType() == UniversalMaterial.RED_WOOL.getType() || item.getType() == UniversalMaterial.LIME_WOOL.getType())
				&& item.hasItemMeta()
				&& (item.getItemMeta().getDisplayName().equals(ChatColor.RED+Lang.TEAM_NOT_READY)
						|| item.getItemMeta().getDisplayName().equals(ChatColor.GREEN+Lang.TEAM_READY))
				);
	}


	public static boolean isRegenHeadItem(ItemStack item) {
		return (
				item != null
				&& item.getType() == UniversalMaterial.PLAYER_HEAD.getType()
				&& item.hasItemMeta()
				&& item.getItemMeta().getLore().contains(ChatColor.GREEN+Lang.ITEMS_REGEN_HEAD)
				);
	}

	public static boolean doesInventoryContainsLobbyTeamItem(Inventory inv, String name){
		for(final ItemStack item : inv.getContents())
			if(item!=null && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(name) && isLobbyTeamItem(item))
				return true;
		return false;
	}

	public static ItemStack createRegenHead(Player player) {
		final GameManager gm = GameManager.getGameManager();
		final MainConfiguration cfg = gm.getConfiguration();
		final String name = player.getName();
		final ItemStack item = VersionUtils.getVersionUtils().createPlayerSkull(name, player.getUniqueId());
		final ItemMeta im = item.getItemMeta();

		// Setting up lore with team members
		final List<String> lore = new ArrayList<>();
		lore.add(ChatColor.GREEN+Lang.ITEMS_REGEN_HEAD);
		if (cfg.getEnableGoldenHeads())
			lore.add(ChatColor.GREEN+Lang.ITEMS_REGEN_HEAD2);
		im.setLore(lore);
		im.setDisplayName(name);
		item.setItemMeta(im);

		return item;
	}

	public static void giveCompassPlayingTo(Player player) {
		final ItemStack compass = new ItemStack(Material.COMPASS, 1);
		final ItemMeta im = compass.getItemMeta();
		im.setDisplayName(ChatColor.GREEN+Lang.ITEMS_COMPASS_PLAYING);
		compass.setItemMeta(im);
		player.getInventory().addItem(compass);
	}



	public static boolean isCompassPlayingItem(ItemStack item) {
		return (
				item != null
				&& item.getType() == Material.COMPASS
				&& item.getItemMeta().getDisplayName().equals(ChatColor.GREEN+Lang.ITEMS_COMPASS_PLAYING)
				);
	}

	public static boolean isKitSelectionItem(ItemStack item){
		return (
				item != null
				&& item.getType() == Material.IRON_PICKAXE
				&& item.getItemMeta().getDisplayName().equals(ChatColor.GREEN+Lang.ITEMS_KIT_SELECTION)
				);
	}

	public static void giveKitSelectionTo(Player player) {
		if(KitsManager.isAtLeastOneKit()){
			final ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE, 1);
			final ItemMeta im = pickaxe.getItemMeta();
			im.setDisplayName(ChatColor.GREEN+Lang.ITEMS_KIT_SELECTION);
			pickaxe.setItemMeta(im);
			player.getInventory().addItem(pickaxe);
		}
	}

	public static void giveCraftBookTo(Player player) {
		if(CraftsManager.isAtLeastOneCraft()){
			final ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
			final ItemMeta im = book.getItemMeta();
			im.setDisplayName(ChatColor.LIGHT_PURPLE+Lang.ITEMS_CRAFT_BOOK);
			book.setItemMeta(im);
			player.getInventory().addItem(book);
		}
	}

	public static boolean isCraftBookItem(ItemStack item){
		return (
				item != null
				&& item.getType().equals(Material.ENCHANTED_BOOK)
				&& item.hasItemMeta()
				&& item.getItemMeta().hasDisplayName()
				&& item.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE+Lang.ITEMS_CRAFT_BOOK)
				);
	}


	public static void spawnExtraXp(Location location, int quantity) {
		final ExperienceOrb orb = (ExperienceOrb) location.getWorld().spawnEntity(location, EntityType.EXPERIENCE_ORB);
		orb.setExperience(quantity);
	}

	public static ItemStack createGoldenHeadPlayerSkull(String name, UUID uuid){

		final ItemStack itemStack = VersionUtils.getVersionUtils().createPlayerSkull(name, uuid);
		final ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(Lang.ITEMS_GOLDEN_HEAD_SKULL_NAME.replace("%player%", name));

		final List<String> lore = new ArrayList<>();
		lore.add(Lang.ITEMS_GOLDEN_HEAD_SKULL_HELP);
		itemMeta.setLore(lore);

		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	public static ItemStack createGoldenHead(){

		final ItemStack itemStack = new ItemStack(Material.GOLDEN_APPLE);
		final ItemMeta itemMeta = itemStack.getItemMeta();

		itemMeta.setDisplayName(Lang.ITEMS_GOLDEN_HEAD_APPLE_NAME);
		itemMeta.setLore(Collections.singletonList(Lang.ITEMS_GOLDEN_HEAD_APPLE_HELP));

		itemStack.setItemMeta(itemMeta);

		return itemStack;
	}

}