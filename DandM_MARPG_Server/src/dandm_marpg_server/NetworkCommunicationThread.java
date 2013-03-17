/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dandm_marpg_server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dajne Win
 */
public class NetworkCommunicationThread implements Runnable {

    private Socket welcomeSocket;
    private Core core;
    private MySQLDatabase databaseInterface;
    
    public NetworkCommunicationThread(Core core, Socket socket)
    {
        databaseInterface = core.databaseInterface;
        this.core = core;
        welcomeSocket = socket;
    }
    
    @Override
    public void run() {
        try {
            String clientSentence;
            String capitalizedSentence;
            Socket connectionSocket = welcomeSocket;
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            clientSentence = inFromClient.readLine();
            if (connectionSocket.getInetAddress() != null)
            {
                System.out.println("Received: " + clientSentence + " from: " + connectionSocket.getInetAddress());
            }
            if (clientSentence.startsWith("login"))
            {
                
                String[] str_array = clientSentence.split(" ");
                if (str_array.length == 3)
                {
                    if (databaseInterface.loginMySQL(str_array[1], str_array[2]))
                    {
                        outToClient.writeBytes("You are now logged in!" + '\n');
                    }
                    else
                    {
                        outToClient.writeBytes("Failed to log in!" + '\n');
                    }
                }
            }
            else if (clientSentence.startsWith("register"))
            {
                String[] str_array = clientSentence.split(" ");
                if (str_array.length == 3)
                {
                    if (str_array[1].length() > 3 && str_array[2].length() > 3)
                    {
                        core.username = str_array[1];
                        core.password = sha512Hash(str_array[2]);
                        core.maxHealth = 100;
                        core.currentHealth = core.maxHealth;
                        outToClient.writeBytes("Successfully registered " + core.username + '\n');
                    }
                    else
                    {
                        outToClient.writeBytes("Problem with Registration!" + '\n');
                    }
                }
                else
                {
                    outToClient.writeBytes("Invalid Registration!" + '\n');
                }
            }
            else if (clientSentence.startsWith("currentHealth"))
            {
                if (core.loggedIn)
                {
                    outToClient.writeBytes("Current Health: " + core.currentHealth + '\n');
                }
            }
            else if (clientSentence.startsWith("causeDamage"))
            {
                if (core.loggedIn)
                {
                    String[] str_array = clientSentence.split(" ");
                    if (str_array.length == 2)
                    {
                        int foo = Integer.parseInt(str_array[1]);
                        damageHealth(foo);
                        outToClient.writeBytes("Current Health: " + core.currentHealth + '\n');
                    }
                }
            }
            else
            {
                outToClient.writeBytes("Invalid Command!" + '\n');
            }
        } catch (IOException ex) {
            Logger.getLogger(NetworkCommunicationThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String sha512Hash(String stringToHash)
    {
        String output = "";
        StringBuilder sha512Hash = new StringBuilder();
        MessageDigest md = null;
        try {

            md = MessageDigest.getInstance("SHA-256");
            md.update(stringToHash.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] sha512ByteHash = md.digest();
        for (int i = 0; i < sha512ByteHash.length; i++) {
            if ((0xff & sha512ByteHash[i]) < 0x10) {
                sha512Hash.append("0").append(Integer.toHexString((0xFF & sha512ByteHash[i])));
            } else {
                sha512Hash.append(Integer.toHexString(0xFF & sha512ByteHash[i]));
            }
        }
        output = sha512Hash.toString();
        return output;
    }
    
    public void damageHealth(int damage)
    {
        if (!isDead())
        {
            if ((core.currentHealth - damage) > 0)
            {
                core.currentHealth = core.currentHealth - damage;
            }
            else
            {
                core.currentHealth = 0;
            }
        }
    }
    
    public boolean isDead()
    {
        if (core.currentHealth <= 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
}
