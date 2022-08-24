package de.hdg.schematicsync.modules;

import com.jcraft.jsch.*;
import de.hdg.schematicsync.SchematicSync;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class webSocket {

    public static void startWS() throws IOException, NoSuchAlgorithmException {


        //make this while loop not hold up the intire server
        new Thread(() -> {
            //get the Server config
            Plugin plugin = getPlugin(SchematicSync.class);
            FileConfiguration config = plugin.getConfig();

            //Create a Server running on the websocketPort from the Config
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(config.getInt("websocketPort"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            plugin.getLogger().info("WebSocket Server successfully started on Port " + config.getInt("websocketPort") + "!");
            while (true) {
                Socket client = null;
                try {
                    client = serverSocket.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                InputStream in = null;
                try {
                    in = client.getInputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                OutputStream out = null;
                try {
                    out = client.getOutputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Scanner s = new Scanner(in, "UTF-8");

                String data = s.useDelimiter("\\r\\n\\r\\n").next();
                Matcher get = Pattern.compile("^GET").matcher(data);

                if (get.find()) {
                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                    match.find();
                    byte[] response = new byte[0];
                    try {
                        response = ("HTTP/1.1 101 Switching Protocols\r\n"
                                + "Connection: Upgrade\r\n"
                                + "Upgrade: websocket\r\n"
                                + "Sec-WebSocket-Accept: "
                                + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
                                + "\r\n\r\n").getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        out.write(response, 0, response.length);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    String ip = plugin.getConfig().getString("serverIP");
                    int port = plugin.getConfig().getInt("serverPort");
                    String password = plugin.getConfig().getString("password");
                    String username = plugin.getConfig().getString("username");

                    JSch jsch = new JSch();
                    Session session = null;
                    Channel channel = null;
                    ChannelSftp sftp = null;

                    //setup jsch session
                    try {
                        plugin.getServer().broadcastMessage(ChatColor.DARK_PURPLE + "Got request to update Schematics from " + client.getInetAddress().toString() + ":" + client.getPort() +"! Updating schematics...");

                        session = jsch.getSession(username, ip, port);
                        session.setConfig("StrictHostKeyChecking", "no");
                        session.setPassword(password);
                        //connect to host
                        session.connect();
                        channel = session.openChannel("sftp");
                        channel.connect();
                        sftp = (ChannelSftp) channel;

                        //put folder schematics into "./plugins/WorldEdit"
                        //for every file in the folder
                        for(Object file : sftp.ls("schematics")){
                            //if the file is a file
                            if(file instanceof ChannelSftp.LsEntry){
                                //get the file name
                                String fileName = ((ChannelSftp.LsEntry) file).getFilename();
                                //if the file is a .schem
                                if(fileName.endsWith(".schem")){
                                    //download the file
                                    sftp.get("schematics/" + fileName, "plugins/WorldEdit/schematics/" + fileName);
                                }
                            }
                        }

                        plugin.getServer().broadcastMessage(ChatColor.GREEN + "Schematics updated!");

                        sftp.disconnect();
                        channel.disconnect();
                        session.disconnect();
                    } catch (JSchException e) {
                        plugin.getServer().broadcastMessage(ChatColor.RED + "Schematics update failed!");
                        throw new RuntimeException(e);
                    } catch (SftpException e) {
                        plugin.getServer().broadcastMessage(ChatColor.RED + "Schematics update failed!");
                        throw new RuntimeException(e);
                    }


                    //close the socket
                    try {
                        client.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        }).start();


    }
}
