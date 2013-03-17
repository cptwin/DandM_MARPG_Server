/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dandm_marpg_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dajne Win
 */
public class Core {
    
    public String username = "";
    public String password = "";
    public int maxHealth = 0;
    public int currentHealth = 0;
    public boolean loggedIn = false;
    public MySQLDatabase databaseInterface;
    
    
    
    public Core()
    {
        databaseInterface = new MySQLDatabase();
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
            NetworkCommunicationThread netThread = new NetworkCommunicationThread(this, connectionSocket);
            Thread thread = new Thread(netThread);
            thread.start();
        }
    }
    
}
