/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dandm_marpg_server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 *
 * @author Dajne Win
 */
public class ChatClient {
    
    public Socket chatSocket;
    public DataOutputStream outToClient;
    public BufferedReader inFromServer;
    
    public ChatClient(Socket cSocket, DataOutputStream dOut, BufferedReader inFS)
    {
        chatSocket = cSocket;
        outToClient = dOut;
        inFromServer = inFS;
    }
    
}
