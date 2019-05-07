/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

/**
 *
 * @author aziza
 */

public class Thing {
    private int id;
    private String name;
    private int zone;
    
    /**
     * creates new thing
     * @param id
     * @param name
     * @param zone
     */
    public Thing(int id, String name,int zone ){
        this.id=id;
        this.name=name;
        this.zone=zone;
    }

    public void setZone(int zone){
        this.zone=zone;
    }
    public int getZone(){
        return zone;
    }
    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    
}
