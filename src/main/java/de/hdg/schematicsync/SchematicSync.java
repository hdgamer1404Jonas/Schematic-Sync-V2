package de.hdg.schematicsync;

import de.hdg.schematicsync.commands.sync;
import de.hdg.schematicsync.listener.events;
import de.hdg.schematicsync.modules.webSocket;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SchematicSync extends JavaPlugin {

    @Override
    public void onEnable() {
        //create the config in plugins/SchematicSync/config.yml if it doesn't exist
        saveDefaultConfig();

        //get the Config
        FileConfiguration config = getConfig();

        //check if all the entries are filled
        if (
                        config.getString("serverIP") == "" ||
                        config.getString("username") == "" ||
                        config.getString("password") == "" ||
                                config.getString("servers").isEmpty()
        ) {
            //write an error to the console and shut down the Plugin if the config is not filled
            getLogger().severe("Config is not filled out. Please fill out the config and restart the server.");
            getServer().getPluginManager().disablePlugin(this);
        }
        if(config.getInt("serverPort") == 0 || config.getInt("websocketPort") == 0){
            getLogger().severe("Config is not filled out. Please fill out the config and restart the server.");
            getServer().getPluginManager().disablePlugin(this);
        }

        //check if all the websocket servers in the servers list are filled out correctly
        for (String server : config.getStringList("servers")) {
            //check if server includes ip, port and password
            //regex to check following format: ip:port
            Pattern p = Pattern.compile("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}:[0-9]{1,5}$");
            Matcher m = p.matcher(server);
            if (!m.find()) {
                getLogger().severe("Error in Config! Server " + server + " is not filled out correctly.");
                getServer().getPluginManager().disablePlugin(this);
            }
        }

        try {
            webSocket.startWS();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        //register the commands
        getCommand("sync").setExecutor(new sync());

        //register the events
        getServer().getPluginManager().registerEvents(new events(), this);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
