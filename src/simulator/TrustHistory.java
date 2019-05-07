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
public class TrustHistory {
    
    private int id;
    private double trustLevel;
    
    /**
     * creates new TrustHistory object
     * @param id
     * @param trustLevel 
     */
    public TrustHistory(int id, double trustLevel){
        this.id=id;
        this.trustLevel=trustLevel;
    }
     /**
     * adds new line with thingId and corresponding new trustLevel in table TrustHistory
     * @param MutableThingId
     */
    public void addTrustLevelHistory(int MutableThingId){
        
    }
    public int getId(){
        return id;
    }
    public double getTrustLevel(){
        return trustLevel;
    }
    public void setTrustLevel(double trustLevel){
        this.trustLevel=trustLevel;
    }
}
