/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dandm_marpg_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dajne Win
 */
public class Core {
    
    public MySQLDatabase databaseInterface;
    private HashSet<Entities> entities;
    
    
    /**
     * Constructor for the core of the server, creates the thread to start listening and the database connection
     * @author Dajne Win
     */
    public Core()
    {
        this.entities = new HashSet();
        databaseInterface = new MySQLDatabase(); //creates a new database object that all threads will pool into
        if (databaseInterface.resetIsLoggedIn())
        {
            try {
                startListening();
            } catch (IOException ex) {
                Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            System.out.println("Error resetting loggedIn or connecting to database!");
        }
    }
    
    public synchronized void addToEntityArray(Entities e)
    {
        entities.add(e);
    }
    
    public synchronized void removeFromEntityArray(Entities e)
    {
        entities.remove(e);
    }
    
    public synchronized HashSet<Entities> returnEntityArray()
    {
        return entities;
    }
    
    private void startListening() throws IOException
    {
        ServerSocket welcomeSocket = new ServerSocket(6789); //starts the server listening on a socket, at this stage 6789
        
        TimerTask task = new ScheduledTask(databaseInterface, this);
        Timer timer = new Timer();
    	timer.schedule(task, 10000,10000);
        
        while(true)
        {
            Socket connectionSocket = welcomeSocket.accept(); //when information comes in through that socket accept it for processing
            NetworkCommunicationThread netThread = new NetworkCommunicationThread(this, connectionSocket); //Create a new Communication Class to thread
            Thread thread = new Thread(netThread); //Create a new network communication thread to process this information
            thread.start(); //Start the network communication thread
            //while true/while the server program is running it will continue to loop this thread waiting for more instructions to process from clients
        }
    }
    
}
