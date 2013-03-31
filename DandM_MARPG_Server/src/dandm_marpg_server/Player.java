/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dandm_marpg_server;

import java.net.Socket;

/**
 *
 * @author dajnewin
 */
public class Player extends Entities {
    
    private int timeoutMilliseconds = 30000;
    private int x;
    private int y;
    private int currentHealth;
    private Socket socket;
    private MySQLDatabase mysql;
    private long timeStamp;
    
    public Player(String name, MySQLDatabase mysqlClass)
    {
        super(name);
        timeStamp = System.currentTimeMillis();
        x = 0;
        y = 0;
        this.mysql = mysqlClass;
        currentHealth = 100;
    }
    
    public void setXCoOrd(int setX)
    {
        x = setX;
    }
    
    public int getXCoOrd()
    {
        return x;
    }
    
    public void setYCoOrd(int setY)
    {
        y = setY;
    }
    
    public int getYCoOrd()
    {
        return y;
    }
    
    public void setCurrentHealth(int setHealth)
    {
        currentHealth = setHealth;
    }
    
    public int getCurrentHealth()
    {
        return currentHealth;
    }
    
    public boolean timedOut()
    {
        if(System.currentTimeMillis() > (timeStamp + timeoutMilliseconds))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public void updateTimestamp()
    {
        timeStamp = System.currentTimeMillis();
    }
    
}
