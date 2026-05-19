package org.honza03210.proximityvc;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProximityVoiceChat extends JavaPlugin implements Listener {
    String SERVER_NAME = "SomeRandomServer";
    String WEBSOCKET_ADDRESS = "https://mc.yappr.cz";
    String VOICE_CHAT_WEBSITE = "https://yappr.cz";
    String PFP_URL = "https://mc-heads.net/avatar/";
    String PFP_SIZE = "32";
    String ROOM_PASSWORD = "Password";
    int WEBSOCKET_PORT = 8080;
    private MCWebSocketServer WSServer;
    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("onEnable is called!");

        // Stop old server if exists -- on reload
        if (WSServer != null) {
            WSServer.stopServer();
            WSServer = null;
        }

        // starting the websocket server
        WSServer = new MCWebSocketServer(WEBSOCKET_PORT);
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            WSServer.start();
        });

        // updating the positions in the active_feeds object
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (!WSServer.active_feeds.containsKey(player.getName())){
                        return;
                    }
                    WSServer.positions_map.put(player.getName(), getPlayerPos(player));
                });
                WSServer.SendPositions();
            });
        }, 0L, 5L);
    }

    @Override
    public void onDisable() {
        WSServer.stopServer();
        // Plugin shutdown logic
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        WSServer.positions_map.put(player.getName(), getPlayerPos(player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        WSServer.last_positions_map.remove(event.getPlayer().getName());
        WSServer.positions_map.remove(event.getPlayer().getName());
        WSServer.active_feeds.remove(event.getPlayer().getName());
    }

    float[] getPlayerPos(Player player){
        return new float[]{(float) player.getLocation().getX(), (float) player.getLocation().getY(), (float) player.getLocation().getZ(), player.getLocation().getPitch(), player.getLocation().getYaw()};
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!command.getName().equalsIgnoreCase("pvc")) return false;

        TextComponent message = new TextComponent("§aClick here to open the voice chat");
        message.setClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL,
                String.format("%s/?username=%s&room_id=%s&websocket_address=%s&user_token=%s&password-INSECURE=%s&autojoin&pfp_url=%s%s/%s", VOICE_CHAT_WEBSITE, sender.getName(), SERVER_NAME, WEBSOCKET_ADDRESS, sender.getName(), ROOM_PASSWORD, PFP_URL, sender.getName(), PFP_SIZE)
        ));

        sender.spigot().sendMessage(message);

        return true;

    }

}
