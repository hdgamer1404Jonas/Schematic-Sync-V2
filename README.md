# Schematic-Sync-V2
Plugin that let's you syncronise schematics across multiple minecraft servers

## How to set up

1. Put the Plugin in your Plugins folder.
2. Start the server and shut it down again.
3. Fill out the config (the config is located in plugins/schematicSync/config.yml)
4. Start the server. If everything is set up correctly, you should be able to use the /sync command.

## Usage

- You can run the /sync command as a player and in the console. If you run the command in the console IMPORTANT: it will only download all files on the sftp server. If you run the command as a player, a Gui should open.

- You can use the sync button (green concrete) to sync a schematic with other servers. If you click the button, a sign will open, where you must type the name of the schematic.

- The button "Delete" (red concrete) will delete ALL schematics from the sftp server. (IMPORTANT: only do this, if you're having problems with the syncronisation.)
