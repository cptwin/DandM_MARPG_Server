/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dandm_marpg_server;

/**
 *
 * @author dajnewin
 */
public abstract class Entities {
    
    private String name;
    
    public Entities(String entityName)
    {
        this.name = entityName;
    }
    
    public void setName(String newName)
    {
        name = newName;
    }
    
    public String getName()
    {
        return name;
    }
    
}
