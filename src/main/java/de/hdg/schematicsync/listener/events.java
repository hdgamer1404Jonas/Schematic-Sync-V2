package de.hdg.schematicsync.listener;


import com.jcraft.jsch.*;
import de.hdg.schematicsync.SchematicSync;
import de.hdg.schematicsync.commands.websocket;
import de.hdg.schematicsync.inventories.SchemSyncSelection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.websocket.DeploymentException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class events implements Listener {

    @EventHandler
    public void clickEvent(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        Plugin plugin = getPlugin(SchematicSync.class);
        FileConfiguration config = plugin.getConfig();

        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory().getHolder() instanceof SchemSyncSelection) {
            event.setCancelled(true);
            if (item == null) {
                return;
            }
            if (item.getType() == Material.BARRIER) {
                event.getWhoClicked().closeInventory();
            }
            if (item.getType() == Material.GREEN_CONCRETE) {
                Player player = (Player) event.getWhoClicked();
                player.getWorld().getBlockAt(player.getLocation()).setType(Material.OAK_SIGN);
                Sign sing = (Sign) player.getWorld().getBlockAt(player.getLocation()).getState();
                player.openSign(sing);

                //add playerPosition to config
                String signPos = player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ();
                config.set("signPos", signPos);
                plugin.saveConfig();
                plugin.reloadConfig();
            }
            if (item.getType() == Material.RED_CONCRETE) {
                try {
                    String ip = config.getString("serverIP");
                    String port = config.getString("serverPort");
                    String username = config.getString("username");
                    String password = config.getString("password");

                    JSch jsch = new JSch();
                    Session session = null;
                    Channel channel = null;
                    ChannelSftp sftp = null;

                    session = jsch.getSession(username, ip, Integer.parseInt(port));
                    session.setPassword(password);
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.connect();
                    channel = session.openChannel("sftp");
                    channel.connect();
                    sftp = (ChannelSftp) channel;


                    sftp.cd("schematics");
                    sftp.rm("*");

                    sftp.disconnect();
                    channel.disconnect();
                    session.disconnect();

                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Deleted ALL schematics from the Server!");
                } catch (JSchException e) {
                    throw new RuntimeException(e);
                } catch (SftpException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @EventHandler
    public void dragEvent(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SchemSyncSelection) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void signDoneEvent(SignChangeEvent event) {
        Player player = event.getPlayer();
        Plugin plugin = getPlugin(SchematicSync.class);
        FileConfiguration config = plugin.getConfig();
        String playerPos = config.getString("signPos");
        String signPos = player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ();
        if (playerPos.equals(signPos)) {
            //delete config entry
            config.set("playerPosition", null);
            plugin.saveConfig();
            plugin.reloadConfig();
           // check if file exists
            File file = new File("plugins/WorldEdit/schematics/" + event.getLine(0) + ".schem");
            if (!file.exists()) {
                player.sendMessage(ChatColor.RED + "File not found");
                SchemSyncSelection schemSyncSelection = new SchemSyncSelection();
                player.openInventory(new SchemSyncSelection().getInventory());
                return;
            }
            try {
                String schemName = event.getLine(0);
                String ip = config.getString("serverIP");
                String port = config.getString("serverPort");
                String username = config.getString("username");
                String password = config.getString("password");

                JSch jsch = new JSch();
                Session session = null;
                Channel channel = null;
                ChannelSftp sftp = null;

                session = jsch.getSession(username, ip, Integer.parseInt(port));
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                channel = session.openChannel("sftp");
                channel.connect();
                sftp = (ChannelSftp) channel;
                sftp.put("plugins/WorldEdit/schematics/" + schemName + ".schem", "schematics/" + schemName + ".schem");
                sftp.disconnect();
                channel.disconnect();
                session.disconnect();
                player.sendMessage(ChatColor.GREEN + "Schematic uploaded to Server");

                TextArea output = null;

                //i have no idea what i did here, it is 2 am and i just watched some indian guys explaining this

                for (String i : config.getStringList("servers")) {
                    websocket handle = new websocket("ws://" + i, output);
                    try {
                        handle.connect();
                    } catch (DeploymentException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
                //delete sign
                player.getWorld().getBlockAt(player.getLocation()).setType(Material.AIR);
                SchemSyncSelection schemSyncSelection = new SchemSyncSelection();
                player.openInventory(schemSyncSelection.getInventory());
            } catch (JSchException e) {
                throw new RuntimeException(e);
            } catch (SftpException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
