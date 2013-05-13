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

    private Socket welcomeSocket; //Socket that we have accepted with the information sent from the client
    private Core core; //reference to the Core class, should be removed in later versions
    private MySQLDatabase databaseInterface; //the single database interface we created in the construction of the Core class
    
    /**
     * Constructor for the NetworkCommunicationThread
     * @param core The main class of the server, should be removed in future versions
     * @param socket The socket and information sent by the client ready for processing
     * @author Dajne Win
     */
    public NetworkCommunicationThread(Core core, Socket socket)
    {
        databaseInterface = core.databaseInterface;
        this.core = core;
        welcomeSocket = socket;
    }
    
    /**
     * The threaded part of the network communication, this is done to prevent I/O blocking so that multiple clients can have commands processed at once
     * @author Dajne Win
     */
    @Override
    public void run() {
        try {
            String clientSentence;
            Socket connectionSocket = welcomeSocket;
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            clientSentence = inFromClient.readLine(); //gets the input from the client
            if (connectionSocket.getInetAddress() != null && !clientSentence.startsWith("movingentities"))
            {
                System.out.println("Received: " + clientSentence + " from: " + connectionSocket.getInetAddress());
            }
            if (clientSentence.startsWith("login")) //if the client commands begins with login, format should be "login username password", user will not end up entering this info manually
            {
                
                String[] str_array = clientSentence.split(" "); //split the string into, hopefully, three strings using the space to seperate each string
                if (str_array.length == 3) //if we have recieved three strings, no more no less
                {
                    int successErrorCode = databaseInterface.loginMySQL(str_array[1], str_array[2]); //check the username and password against the database
                    if (successErrorCode == 99 ) // 99 = login successful
                    {
                        outToClient.writeBytes("You are now logged in!" + '\n'); //send a message to client if login is successful
                        Player player = new Player(str_array[1].toLowerCase(),databaseInterface);
                        core.addToEntityArray(player);
                    }
                    else if (successErrorCode == 97) //97 = already logged in
                    {
                        outToClient.writeBytes("User already logged in!" + '\n'); //send a message to client if login was unsuccessful, doesn't take into account why (could be wrong password, username, database connection broken etc)
                    }
                    else if (successErrorCode == 98) {
                        outToClient.writeBytes("Password is incorrect" + '\n');
                    } 
                    else if (successErrorCode == 0) {
                        outToClient.writeBytes("No such user" + '\n');
                    } 
                    else {
                        outToClient.writeBytes("Some other error" + '\n');
                    }
                }
                else
                {
                    outToClient.writeBytes("Incorrect format! Format = login username password" + '\n'); //send mesaage to client if the command wasn't in the correct form
                }
            }
            else if (clientSentence.startsWith("register")) //if the client commands begins with register, format should be "register username password"
            {
                String[] str_array = clientSentence.split(" "); //split the string into, hopefully, three strings using the space to seperate each string
                if (str_array.length == 3) //if we have recieved three strings, no more no less
                {
                    if (databaseInterface.registerMySQL(str_array[1], str_array[2])) //attempt to register the username and password in the database
                    {
                        outToClient.writeBytes("You are now registered, please log in!" + '\n'); //send a message to client if registration was successful
                    }
                    else
                    {
                        outToClient.writeBytes("Failed to register!" + '\n'); //send a message to client if registration failed, like the login this could be client or server side problems at this stage
                    }
                }
                else
                {
                    outToClient.writeBytes("Incorrect format! Format = register username password" + '\n'); //send mesaage to client if the command wasn't in the correct form
                }
            }
            else if (clientSentence.startsWith("move")) //if the client commands begins with register, format should be "register username password"
            {
                String[] str_array = clientSentence.split(" "); //split the string into, hopefully, three strings using the space to seperate each string
                if (str_array.length == 4) //if we have recieved three strings, no more no less
                {
                    Player checkedPlayer = null;
                    for (Entities e : core.returnEntityArray()) {
                            if (e instanceof Player) {
                                Player playerCheck = (Player) e;
                                if (playerCheck.getName().equals(str_array[1])) {
                                    checkedPlayer = playerCheck;
                                }
                            }
                    }
                    if (checkedPlayer != null && !checkedPlayer.timedOut()) { //checks if the player is still logged in/responding
                        String playersToClient = "";
                        for (Entities e : core.returnEntityArray()) {
                            if (e instanceof Player) {
                                Player player = (Player) e;
                                if (player.getName().equals(str_array[1])) {
                                    boolean allowMove = true;
                                    for (Entities e2 : core.returnEntityArray())
                                    {
                                        if(e2 instanceof Player)
                                        {
                                            Player player2 = (Player)e2;
                                            if(player2.getXCoOrd() == Integer.parseInt(str_array[2]) && player2.getYCoOrd() == Integer.parseInt(str_array[3]))
                                            {
                                                allowMove = false;
                                            }
                                        }
                                    }
                                    if(allowMove)
                                    {
                                        player.setXCoOrd(Integer.parseInt(str_array[2]));
                                        player.setYCoOrd(Integer.parseInt(str_array[3]));
                                        player.updateTimestamp();
                                    }                                    
                                }
                            }
                        }
                    }
                    else
                    {
                        outToClient.writeBytes("Timed Out!" + '\n');
                    }
                }
            }
            else if (clientSentence.startsWith("ping"))
            {
                System.out.println(clientSentence);
                String[] str_array = clientSentence.split(" ");
                if (str_array.length == 2)
                {
                    for (Entities e : core.returnEntityArray()) {
                            if (e instanceof Player) {
                                Player player = (Player) e;
                                if (player.getName().equals(str_array[1])) {
                                    player.updateTimestamp();
                                    outToClient.writeBytes("Pong" + '\n');
                                }
                            }
                    }
                }
                else
                {
                    outToClient.writeBytes("Pong" + '\n');
                }
            }
            else if (clientSentence.startsWith("movingentities"))
            {
                String[] str_array = clientSentence.split(" ");
                String playersToClient = "movingentities";
                for (Entities e : core.returnEntityArray()) {
                            if (e instanceof Player) {
                                Player player = (Player) e;
                                if(!player.getName().equalsIgnoreCase(str_array[1]))
                                {
                                    playersToClient += player.getName() + "/" + player.getXCoOrd() + "/" + player.getYCoOrd() + " ";
                                }
                            }
                }
                outToClient.writeBytes(playersToClient + '\n');
                
            }
            else if (clientSentence.startsWith("chat"))
            {
                    String sentence = clientSentence;
                    System.out.println(sentence);
                    sentence = sentence.replaceFirst("chat", "");
                    String[] str_array = sentence.split("::");
                    if(str_array.length == 2)
                    {
                        for(ChatClient c : core.chatSockets)
                        {
                            System.out.println("rchat" + str_array[0] + ": " + str_array[1] + '\n');
                            c.outToClient.writeBytes("rchat" + str_array[0] + ": " + str_array[1] + '\n');
                        }
                    }
                    else
                    {
                        ChatClient c = new ChatClient(connectionSocket, outToClient, inFromClient);
                        core.chatSockets.add(c);
                        DataOutputStream output = new DataOutputStream(c.chatSocket.getOutputStream());
                        output.writeBytes("rchat" + "Joined the server!" + '\n');
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
    
    /**
     * Creates a hash from a plaintext password so that passwords are not stored or checked in plaintext
     * @author Dajne Win
     * @param String stringToHash
     * @return String HashedPassword
     */
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
    
    /*public void damageHealth(int damage)
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
    }*/
    
}
