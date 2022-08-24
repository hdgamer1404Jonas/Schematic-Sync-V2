package de.hdg.schematicsync.commands;


import de.hdg.schematicsync.SchematicSync;
import de.hdg.schematicsync.inventories.SchemSyncSelection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.websocket.DeploymentException;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class sync implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //import config
        Plugin plugin = getPlugin(SchematicSync.class);
        FileConfiguration config = plugin.getConfig();

        if (sender instanceof Player){

            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You don't have permissions to use this Command!");
                return false;
            }

            SchemSyncSelection schemSyncSelection = new SchemSyncSelection();
            ((Player) sender).openInventory(schemSyncSelection.getInventory());




        }else {
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
            return true;
        }


        return false;
    }
}
