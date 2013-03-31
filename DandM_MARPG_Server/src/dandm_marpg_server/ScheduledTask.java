/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dandm_marpg_server;

import java.util.TimerTask;

/**
 *
 * @author dajnewin
 */
public class ScheduledTask extends TimerTask {
    
    private MySQLDatabase databaseInterface;
    private Core core;
    
    public ScheduledTask(MySQLDatabase mysql, Core c)
    {
        this.databaseInterface = mysql;
        this.core = c;
    }

    @Override
    public void run() {
        Player z = null;
        for (Entities e : core.returnEntityArray())
        {
            if (e instanceof Player)
            {
                Player player = (Player)e;
                if(player.timedOut())
                {
                    databaseInterface.logoutUser(player.getName());
                    z = player;
                    System.out.println(player.getName() + " timed out!");
                }
            }
        }
        if (z != null)
        {
            core.removeFromEntityArray(z);
        }
    }
    
}
