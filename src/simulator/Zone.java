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
public class Zone {
    private int id;
    private int commonResources;
    private int delayedMutationsNumber;
    
    public Zone(int id, int commonResources){
        this.id=id;
        this.commonResources=commonResources;
        this.delayedMutationsNumber=0;
    }
    public int getCommonResources(){
        return this.commonResources;
    }
    public int getId(){
        return this.id;
    }
    public int getDelayedMutationsNumber(){
        return this.delayedMutationsNumber;
    }
    /**
     * allocates resources from the common resources of the zone
     * @param resource 
     */
    public void allocateCommonResources(int resource){
        this.commonResources-=resource;
    }
    /**
     * restores allocated resources to the common resources of a zone
     * @param resource 
     */
    public void restoreCommonResources(int resource){
        this.commonResources+=resource;
    }
    /**
     * increments the number of delayed mutations in the zone
     * @param number 
     */
     public void addDelayedMutationsNumber(int number){
        this.delayedMutationsNumber+=number;
    }
}
