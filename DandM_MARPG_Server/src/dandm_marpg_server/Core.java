/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dandm_marpg_server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dajne Win
 */
public class Core {
    
    
    
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
            System.out.println("Received: " + clientSentence);
            if (clientSentence.startsWith("login"))
            {
                String[] str_array = clientSentence.split(" ");
                if (str_array.length == 3)
                {
                    outToClient.writeBytes("Attempting to login!" + '\n');
                }
                else
                {
                    outToClient.writeBytes("Invalid login!" + '\n');
                }
            }
            else
            {
                outToClient.writeBytes("Invalid Command!" + '\n');
            }
         }
    }
    
}
