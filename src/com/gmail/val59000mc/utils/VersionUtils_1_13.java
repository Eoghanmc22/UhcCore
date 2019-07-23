package com.gmail.val59000mc.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;

import com.gmail.val59000mc.UhcCore;

public class VersionUtils_1_13 extends VersionUtils{

	@Override
	public ShapedRecipe createShapedRecipe(ItemStack craft, String craftKey) {
		final NamespacedKey namespacedKey = new NamespacedKey(UhcCore.getPlugin(), craftKey);
		return new ShapedRecipe(namespacedKey, craft);
	}

	@Override
	public ItemStack createPlayerSkull(String name, UUID uuid) {
		final ItemStack item = UniversalMaterial.PLAYER_HEAD.getStack();
		final SkullMeta im = (SkullMeta) item.getItemMeta();
		im.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
		item.setItemMeta(im);
		return item;
	}
    @Override
    public Objective registerObjective(Scoreboard scoreboard, String name, String criteria) {
        if (criteria.equals("health")){
            return scoreboard.registerNewObjective(name, criteria, name, RenderType.HEARTS);
        }
        return scoreboard.registerNewObjective(name, criteria, name);
    }

	@Override
	public void setPlayerMaxHealth(Player player, double maxHealth) {
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
	}

	@Override
	public void replaceOceanBiomes() {
		Bukkit.getLogger().warning("[UhcCore] Ocean biomes won't be replaced, this feature is not supported on 1." + UhcCore.getVersion());
	}

	@Override @SuppressWarnings("unchecked")
	public void setGameRuleValue(World world, String name, Object value){
		final GameRule gameRule = GameRule.getByName(name);
		world.setGameRule(gameRule, value);
	}

	@Override
	public boolean hasEye(Block block) {
		final EndPortalFrame portalFrame = (EndPortalFrame) block.getBlockData();
		return portalFrame.hasEye();
	}

	@Override
	public void setEye(Block block, boolean eye) {
		final EndPortalFrame portalFrame = (EndPortalFrame) block.getBlockData();
		portalFrame.setEye(eye);
		block.setBlockData(portalFrame);
	}

	@Override
	public void setEndPortalFrameOrientation(Block block, BlockFace blockFace) {
		final EndPortalFrame portalFrame = (EndPortalFrame) block.getBlockData();
		portalFrame.setFacing(blockFace);
		block.setBlockData(portalFrame);
	}

}