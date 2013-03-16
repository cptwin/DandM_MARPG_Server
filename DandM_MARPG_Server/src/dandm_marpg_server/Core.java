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
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dajne Win
 */
public class Core {
    
    private String username = "";
    private String password = "";
    private int maxHealth = 0;
    private int currentHealth = 0;
    private boolean loggedIn = false;
    
    
    
    
    public Core()
    {
        try {
            startListening();
        } catch (IOException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void startListening() throws IOException
    {
        
        String clientSentence;
        String capitalizedSentence;
        ServerSocket welcomeSocket = new ServerSocket(6789);

        while(true)
        {
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            clientSentence = inFromClient.readLine();
            System.out.println("Received: " + clientSentence + " from: " + connectionSocket.getInetAddress());
            if (clientSentence.startsWith("login"))
            {
                String[] str_array = clientSentence.split(" ");
                if (str_array.length == 3)
                {
                    if (username.equals(str_array[1]))
                    {
                        if (password.contentEquals(sha512Hash(str_array[2])))
                        {
                            loggedIn = true;
                            outToClient.writeBytes("You are now logged in!" + '\n');
                        }
                        else
                        {
                            outToClient.writeBytes("Incorrect Password!" + '\n');
                        }
                    }
                    else
                    {
                        outToClient.writeBytes("Username doesn't exist!" + '\n');
                    }
                }
                else
                {
                    outToClient.writeBytes("Invalid login!" + '\n');
                }
            }
            else if (clientSentence.startsWith("register"))
            {
                String[] str_array = clientSentence.split(" ");
                if (str_array.length == 3)
                {
                    if (str_array[1].length() > 3 && str_array[2].length() > 3)
                    {
                        username = str_array[1];
                        password = sha512Hash(str_array[2]);
                        maxHealth = 100;
                        currentHealth = maxHealth;
                        outToClient.writeBytes("Successfully registered " + username + '\n');
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
                if (loggedIn)
                {
                    outToClient.writeBytes("Current Health: " + currentHealth + '\n');
                }
            }
            else if (clientSentence.startsWith("causeDamage"))
            {
                if (loggedIn)
                {
                    String[] str_array = clientSentence.split(" ");
                    if (str_array.length == 2)
                    {
                        int foo = Integer.parseInt(str_array[1]);
                        damageHealth(foo);
                        outToClient.writeBytes("Current Health: " + currentHealth + '\n');
                    }
                }
            }
            else
            {
                outToClient.writeBytes("Invalid Command!" + '\n');
            }
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
            if ((currentHealth - damage) > 0)
            {
                currentHealth = currentHealth - damage;
            }
            else
            {
                currentHealth = 0;
            }
        }
    }
    
    public boolean isDead()
    {
        if (currentHealth <= 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
}
