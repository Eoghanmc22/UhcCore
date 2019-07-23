package com.gmail.val59000mc.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfiguration;
import com.gmail.val59000mc.configuration.VaultManager;
import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.events.UhcPlayerKillEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerDoesntExistException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.threads.TimeBeforeSendBungeeThread;

public class PlayerDeathListener implements Listener {

	private final boolean enableKillEvent;
	private final double reward;

	public PlayerDeathListener(){
		final GameManager gm = GameManager.getGameManager();
		this.enableKillEvent = gm.getConfiguration().getEnableKillEvent();
		this.reward = gm.getConfiguration().getRewardKillEvent();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final GameManager gm = GameManager.getGameManager();
		final PlayersManager pm = gm.getPlayersManager();
		final MainConfiguration cfg = gm.getConfiguration();
		UhcPlayer uhcPlayer;
		try {
			uhcPlayer = pm.getUhcPlayer(player);
			if(uhcPlayer.getState().equals(PlayerState.PLAYING)){

				// kill event
				final Player killer = player.getKiller();
				UhcPlayerKillEvent killEvent;
				if(killer != null){
					final UhcPlayer uhcKiller = pm.getUhcPlayer(killer);
					uhcKiller.kills++;
					killEvent = new UhcPlayerKillEvent(uhcPlayer, uhcKiller);
					Bukkit.getServer().getPluginManager().callEvent(killEvent);
					if(enableKillEvent){
						VaultManager.addMoney(killer, reward);
						if(!Lang.EVENT_KILL_REWARD.isEmpty())
							killer.sendMessage(Lang.EVENT_KILL_REWARD.replace("%money%", ""+reward));
					}
				}


				// eliminations
				gm.broadcastInfoMessage(Lang.PLAYERS_ELIMINATED.replace("%player%", player.getName()));

				if(cfg.getRegenHeadDropOnPlayerDeath())
					event.getDrops().add(UhcItems.createRegenHead(player));

				if(cfg.getEnableGoldenHeads())
					event.getDrops().add(UhcItems.createGoldenHeadPlayerSkull(player.getName(), player.getUniqueId()));

				if(cfg.getEnableExpDropOnDeath())
					UhcItems.spawnExtraXp(player.getLocation(), cfg.getExpDropOnDeath());

				uhcPlayer.setState(PlayerState.DEAD);
				pm.strikeLightning(uhcPlayer);
				pm.playSoundPlayerDeath();

				// handle player leaving the server
				final boolean canContinueToSpectate = player.hasPermission("uhc-core.spectate.override")
						|| cfg.getCanSpectateAfterDeath();

				if (!canContinueToSpectate)
					if (cfg.getEnableBungeeSupport())
						Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new TimeBeforeSendBungeeThread(uhcPlayer, cfg.getTimeBeforeSendBungeeAfterDeath()));
					else
						player.kickPlayer(Lang.DISPLAY_MESSAGE_PREFIX + " " + Lang.KICK_DEAD);

				pm.checkIfRemainingPlayers();
			}else{
				//player.kickPlayer("Don't cheat !");
			}
		} catch (final UhcPlayerDoesntExistException e) {
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		final GameManager gm = GameManager.getGameManager();
		final PlayersManager pm = gm.getPlayersManager();
		final UhcPlayer uhcPlayer;
		try {
			uhcPlayer = pm.getUhcPlayer(player);

			if(uhcPlayer.getState().equals(PlayerState.DEAD))
				Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), (Runnable) () -> pm.setPlayerSpectateAtLobby(uhcPlayer), 1);
		} catch (final UhcPlayerDoesntExistException e) {
		}
	}

}
