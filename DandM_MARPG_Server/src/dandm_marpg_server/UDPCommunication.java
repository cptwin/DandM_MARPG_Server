/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dandm_marpg_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dajne Win
 */
public class UDPCommunication implements Runnable {

    private DatagramSocket serverSocket; //Socket that we have accepted with the information sent from the client
    private Core core; //reference to the Core class, should be removed in later versions
    private MySQLDatabase databaseInterface; //the single database interface we created in the construction of the Core class

    public UDPCommunication(Core core, DatagramSocket socket) {
        databaseInterface = core.databaseInterface;
        this.core = core;
        serverSocket = socket;
    }

    @Override
    public void run() {
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                System.out.println("RECEIVED: " + sentence);
                if (sentence.startsWith("move")) {
                    String clientSentence = sentence;
                    InetAddress IPAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
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
                                        for (Entities e2 : core.returnEntityArray()) {
                                            if (e2 instanceof Player) {
                                                Player player2 = (Player) e2;
                                                if (player2.getXCoOrd() == Integer.parseInt(str_array[2]) && player2.getYCoOrd() == Integer.parseInt(str_array[3])) {
                                                    allowMove = false;
                                                }
                                            }
                                        }
                                        if (allowMove) {
                                            try {
                                                player.setXCoOrd(Integer.parseInt(str_array[2]));
                                            } catch (NumberFormatException nfe)
                                            {
                                                System.out.println(player.getName() + " Error moving to: " + str_array[2]);
                                            }
                                            try {
                                                player.setYCoOrd(Integer.parseInt(str_array[3]));
                                            } catch (NumberFormatException nfe)
                                            {
                                                System.out.println(player.getName() + " Error moving to: " + str_array[3]);
                                            }
                                            player.updateTimestamp();
                                        }
                                        playersToClient += "move" + player.getName() + "/" + player.getXCoOrd() + "/" + player.getYCoOrd();
                                        //outToClient.writeBytes("Player Move: " + str_array[1] + " " + player.getXCoOrd() + "," + player.getYCoOrd() + '\n');
                                        //System.out.println("Player Move: " + str_array[1] + " " + player.getXCoOrd() + "," + player.getYCoOrd());

                                    }
                                }
                            }
                            for (Entities e : core.returnEntityArray()) {
                                if (e instanceof Player) {
                                    Player player = (Player) e;
                                    if (!player.getName().equals(str_array[1])) {
                                        playersToClient += "%otherplayer" + player.getName() + "/" + player.getXCoOrd() + "/" + player.getYCoOrd();
                                    }
                                }
                            }
                            sendData = playersToClient.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                            serverSocket.send(sendPacket);
                            System.out.println(playersToClient);
                        } else {
                            sendData = "Timed Out!".getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                            serverSocket.send(sendPacket);
                        }
                    }
                }




            } catch (IOException ex) {
                Logger.getLogger(UDPCommunication.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
