/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import static org.apache.commons.math3.util.Precision.round;

/**
 *
 * @author aziza
 */
public class Authority {
    private int id;
    private String name;
    public static Map<MutableThing,ArrayList<Capability>> mutationRequests = new HashMap<>();
    
    /**
     * creates new Authority
     * @param id
     * @param name 
     */
    public Authority(int id, String name){
        this.id=id;
        this.name=name;
    }
    /**
     * @return authority's id
     */
    public int getid(){
        return id;
    }
    /**
     * @return authority's name
     */
    public String getName(){
        return name;
    }
    /**
     * authority handles MutableThing's mutation request
     * if mutation allowed return true else return false
     * @param thing
     * @param nextStatus
     * @return AuthorityResponse
     */
    public boolean handleMutationRequest(MutableThing thing, ArrayList<Capability> nextStatus) throws IOException{
        MutableThing bestThing = thing; 
        for (Map.Entry<MutableThing,ArrayList<Capability>> entry : Authority.mutationRequests.entrySet()){
             if(entry.getValue().equals(nextStatus) && entry.getKey().getTrustLevel()>= thing.getTrustLevel()){
                 if(entry.getKey().getTrustLevel()> thing.getTrustLevel()){
                 bestThing = entry.getKey();
                 }
                 else {
                     if(entry.getKey().getId()< thing.getId()){
                       bestThing = entry.getKey();  
                     }
                 }
             }
         }

        return bestThing.getId() == thing.getId();
    }
    /**
     * calculate MutableThing trustLevel using MutationLog
     * calls MutationLog's methods getDeviantLogs() and getNonDeviantLogs
     * calls TrustHistory's method addTrustLevelHistory
     * calls MutableThing's method updateMutableThingTrustLevel 
     * @return 
     */
    public double calculateTrustLevel() throws IOException{
        
        for(int i=0;i<Simulator.things.size();i++){
            ArrayList<MutationLog> deviantLogs = MutationLog.getDeviantLogs(Simulator.things.get(i).getId());
            ArrayList<MutationLog> nonDeviantLogs = MutationLog.getNonDeviantLogs(Simulator.things.get(i).getId());
            if(deviantLogs.size()>0 || nonDeviantLogs.size()>0){
                // calcul trustLevel
                double trusLevel = round(((double)nonDeviantLogs.size()/(nonDeviantLogs.size()+deviantLogs.size())), 2);
                Simulator.myWriter.write("thing"+Simulator.things.get(i).getId()+" new trustLevel: "+trusLevel+"\n");
                Simulator.things.get(i).updateMutableThingTrustLevel(trusLevel);
                new TrustHistory(i, trusLevel).addTrustLevelHistory(Simulator.things.get(i).getId());
            }
            
        }
        return 0;
    }

}
