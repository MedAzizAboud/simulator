/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import javafx.util.Pair;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import static org.apache.commons.math3.util.Precision.round;

/**
 *
 * @author aziza
 */
public class Authority {
    private int id;
    private String name;
    public static Queue<Mutation> mutationRequests = new LinkedList<>();
    public static Queue<Pair<Integer,Mutation>> mutationsToRemoveFromZonesQueues = new LinkedList<>();
    
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
     * also manages the execution queue with the execution duration of each mutation
     * and removes mutations from the execution queue when they are done and restores zone's common resources 
     * and calls updateImpact method to update the impact of a MutableThing each time a MutableThing's mutation has an extra delay and increments the zone's total number of delayed mutations
     */
    public void handleMutationRequest() throws IOException{
           Queue<Mutation> mutationsToRemove= new LinkedList<>();
        int count=0;
        for (Mutation mutation: mutationRequests) {
                  if(mutation.mutationDuration == 0){
                      count++;
                  }
        }  
           for (Mutation mutation: mutationRequests) {
                  if(mutation.mutationDuration == 0){
                      mutation.mutationDelay-=1;
                      
                      this.updateImpact(mutation.thing, Simulator.zonesWaitingQueues.get(mutation.thing.getZone().getId()).size()/count);
                      mutation.thing.getZone().addDelayedMutationsNumber(Simulator.zonesWaitingQueues.get(mutation.thing.getZone().getId()).size());
                      if(mutation.mutationDelay == 0){
                          
                          mutationsToRemove.add(mutation);
                          mutation.thing.getZone().restoreCommonResources(1);
                      }
                  }
                  else{
                      mutation.mutationDuration-=1;
                      if(mutation.mutationDelay == 0 && mutation.mutationDuration == 0){
                          
                          mutationsToRemove.add(mutation);
                          mutation.thing.getZone().restoreCommonResources(1);
                      }
                  }
		}
        while(!mutationsToRemove.isEmpty()){
            Authority.mutationRequests.remove(mutationsToRemove.poll());
        }
        
    }
    /**
     * the authority handles the zone's waiting queues and gives her response for mutations in the waiting queues
     * @throws IOException 
     */
    public void addMutationRequest() throws IOException{
        
        for (Map.Entry<Integer,Queue<Mutation>> entry : Simulator.zonesWaitingQueues.entrySet()){
            for(Mutation mutation: entry.getValue()){
                Queue<Mutation> otherMutationsSInSameZone = Simulator.zonesWaitingQueues.get(entry.getKey());
                otherMutationsSInSameZone.remove(mutation);
                boolean response = true;
                while(!otherMutationsSInSameZone.isEmpty()){
                    Mutation otherMutation = otherMutationsSInSameZone.poll();
                    if(mutation.capabilities.equals(otherMutation.capabilities) && otherMutation.thing.getTrustLevel()>mutation.thing.getTrustLevel()){
                        response=false;
                    }
                }
                    mutation.thing.executeMutation(response, mutation.capabilities, mutation);
            }
        }
        while(!mutationsToRemoveFromZonesQueues.isEmpty()){
            Pair<Integer,Mutation> pair= mutationsToRemoveFromZonesQueues.poll();
            Simulator.zonesWaitingQueues.get(pair.getKey()).remove(pair.getValue());
        }
    }
    /**
     * the authority handles the zone's waiting queues and gives her response for mutations in the waiting queues
     * @param minimalTrustLevel
     * @throws IOException 
     */
    public void addMutationRequest(double minimalTrustLevel) throws IOException{
        
     for (Map.Entry<Integer,Queue<Mutation>> entry : Simulator.zonesWaitingQueues.entrySet()){
            for(Mutation mutation: entry.getValue()){
                if(mutation.thing.getTrustLevel()>= minimalTrustLevel){
                    mutation.thing.executeMutation(true, mutation.capabilities, mutation);
                }
                else{
                    mutation.thing.executeMutation(false, mutation.capabilities, mutation);
                }
            }
        }
        while(!mutationsToRemoveFromZonesQueues.isEmpty()){
            Pair<Integer,Mutation> pair= mutationsToRemoveFromZonesQueues.poll();
            Simulator.zonesWaitingQueues.get(pair.getKey()).remove(pair.getValue());
        }   
    }
    
    /**
     * calculate MutableThing trustLevel using MutationLog
     * calls MutationLog's methods getDeviantLogs() and getNonDeviantLogs
     * calls TrustHistory's method addTrustLevelHistory
     * calls MutableThing's method updateMutableThingTrustLevel 
     * @return trustlevel
     * @throws java.io.IOException 
     */
    public double calculateTrustLevel() throws IOException{
        double trustLevel=0;
        String csv = "trustlevel.csv";
        FileWriter writer = new FileWriter(csv, true);
        CSVWriter csvWriter = new CSVWriter(writer,
                                    ';',
                                    CSVWriter.NO_QUOTE_CHARACTER,
                                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                    CSVWriter.DEFAULT_LINE_END);
        for(int i=0;i<Simulator.things.size();i++){
            
                int thingId=Simulator.things.get(i).getId();
                MutableThing thing=Simulator.things.get(i);
                trustLevel = round((Simulator.resourceWeight*this.calculateResource(thingId)+Simulator.mutationResultWeight*this.calculateMutationResult(thingId))*this.calculateImpact(thing)*this.calculateNeed(thing), 2);
                double f = round(Simulator.resourceWeight*this.calculateResource(thingId)+Simulator.mutationResultWeight*this.calculateMutationResult(thingId),2);
                double d = round(this.calculateImpact(thing)*this.calculateNeed(thing),2);
                String[] data = { Integer.toString(thingId), Double.toString(trustLevel), Double.toString(f), Double.toString(d)}; 
                csvWriter.writeNext(data);
                
                Simulator.myWriter.write("thing"+Simulator.things.get(i).getId()+" new trustLevel: "+trustLevel+"\n");
                Simulator.myWriter.write("mutationresult:"+this.calculateMutationResult(thingId)+" need:"+this.calculateNeed(thing)+" impact:"+this.calculateImpact(thing)+"\n");
                Simulator.things.get(i).updateMutableThingTrustLevel(trustLevel);
                new TrustHistory(i, trustLevel, Simulator.things.get(i).getImpact()).addTrustLevelHistory(Simulator.things.get(i).getId());
            
            
        }
        writer.close(); 
        return trustLevel;
    }
    /**
     * calculates the cooperativeness component of the trustlevel of a mutablething
     * @param thing
     * @return cooperativeness
     */
   /* private double calculateCooperativeness(MutableThing thing){
        double cooperativeness= round(Simulator.mutationResultWeight*this.calculateMutationResult(thing.getId())+Simulator.impactWeight*this.calculateImpact(thing),2);
        
        return cooperativeness;
    }
    */
    /**
     * calculates the honesty component of the trustlevel of a mutablething
     * @param thingId
     * @return honesty
     */
    /*private double calculateHonesty(int thingId){
        double honesty=0;
        
            ArrayList<MutationLog> deviantLogs = MutationLog.getDeviantLogs(thingId);
            ArrayList<MutationLog> nonDeviantLogs = MutationLog.getNonDeviantLogs(thingId);
            if(deviantLogs.size()>0 || nonDeviantLogs.size()>0){
                
                honesty = round(((double)nonDeviantLogs.size()/(nonDeviantLogs.size()+deviantLogs.size())), 2);
            }
            
        
        return honesty;
    }
    */
    /**
     * calculates the resource component of the trustlevel of a mutablething
     * @param thingId
     * @return resource
     */
    private double calculateResource(int thingId){
        double resource=0;
        int estimatedResourceConsumption=0,actualResourceConsumption=0;
        int count=0;
        for (MutationLog mutationLog : MutableThing.mutationLogs) {
            if(mutationLog.getId() == thingId && mutationLog.getAuthorityDecision()){
                count++;
                actualResourceConsumption = mutationLog.getActualResourceConsumption();
                estimatedResourceConsumption = mutationLog.getEstimatedResourceConsumption();
                if((actualResourceConsumption-estimatedResourceConsumption)<=(Simulator.resourceConsumptionThreshold*estimatedResourceConsumption/100)){
                    resource+=1;
                }
            }
        }
        if(count>0){
            return round(resource/count,2);
        }
        return resource;
    }
    /**
     * calculate the mutationresult component of cooperativeness for a mutablething
     * @param thingId
     * @return mutationResult
     */
    private double calculateMutationResult(int thingId){
        double mutationResult=1,achievementDegree=0;
        int count = 0;
        for (MutationLog mutationLog : MutableThing.mutationLogs) {
            if(mutationLog.getId() == thingId && mutationLog.getAuthorityDecision()){
                count++;
                achievementDegree+= mutationLog.getAchievementDegree();
            }
        }
        if(count>0){
            mutationResult= round(achievementDegree/count,2);
        }
        return mutationResult;
    }
    /**
     * calculates the achievement degree of a mutation 
     * @param thing
     * @return achievementDegree
     */
    public double calculateAchievementDegree(MutableThing thing){
        //return round(new Random().nextDouble(),2);
        //ExponentialDistribution dist= new ExponentialDistribution(((double) Simulator.cycleCounter/ (double) 10));
        double achievementDegree = new Random().nextDouble()*(1.0-(double)thing.getCooperationDegree()/100) + (double)thing.getCooperationDegree()/100;
        if(thing.decide()){
            return round(achievementDegree,2);
        }
         return round(1.0-achievementDegree,2);
    }
    /**
     * calculates the real resource consumption of a mutablething for a mutation
     * @param estimatedResourceConsumption
     * @return resourceConsumption
     */
    public int calculateActualResourceConsumption(int estimatedResourceConsumption){
        return new Random().nextInt(estimatedResourceConsumption)+estimatedResourceConsumption;
    }
    /**
     * calls MutableThing addImpact method to increment the number of delayed mutations caused by a MutableThing
     * @param thing
     * @param impact 
     */
    public void updateImpact( MutableThing thing, int impact){
        thing.addImpact(impact);
    }
    /**
     * calculates the impact of a MutableThing
     * @param thing
     * @return impact
     */
    public double calculateImpact(MutableThing thing){
        
        double impact;
        if(thing.getZone().getDelayedMutationsNumber() > 0){
            impact = round((double)thing.getImpact()/thing.getZone().getDelayedMutationsNumber(),2);
        }
        else{
            impact = 0;
        }
        return round(1-impact,2);
    }
    /**
     * updates the need of a MutableThing each time he does a mutation that decrements the working level of the zone in which he is
     * @param thing
     * @param previousState
     * @param actualState 
     */
    public void updateNeed(MutableThing thing,ArrayList<Constraint> previousState, ArrayList<Constraint> actualState){
        double actualWorkingPercentage=0;
        double previousWorkingPercentage=0;
        for(int i=0;i<previousState.size();i++){
            previousWorkingPercentage += previousState.get(i).number;
            actualWorkingPercentage += actualState.get(i).number;
        }
        ArrayList<Constraint> supposedState = Simulator.zonesRules.get(thing.getZone().getId());
        int supposedNumber = 0;
        for(int i=0;i<supposedState.size();i++){
            supposedNumber += supposedState.get(i).number;
        }
        previousWorkingPercentage = round((previousWorkingPercentage/supposedNumber)*100,0);
        actualWorkingPercentage = round((actualWorkingPercentage/supposedNumber)*100,0);
        
        if(previousWorkingPercentage > 100){
            previousWorkingPercentage=100;
        }
        if(actualWorkingPercentage > 100){
            actualWorkingPercentage=100;
        }
        if(previousWorkingPercentage == actualWorkingPercentage){
            if(actualWorkingPercentage < 100){
                for(int i=0;i<supposedState.size();i++){
                    if(thing.getAllCapabilities().contains(Simulator.capabilities.get(supposedState.get(i).capability)) && !thing.getActualCapabilities().contains(Simulator.capabilities.get(supposedState.get(i).capability))){
                        thing.addNeed(1);
                        break;
                    }
                }
            }
        }
        else if(actualWorkingPercentage < previousWorkingPercentage){
            thing.addNeed(1);
        }
    }
    /**
     * calculates the need component for a MutableThing
     * @param thing
     * @return 
     */
    public double calculateNeed(MutableThing thing){
        double need;
        int count=0;
        for (MutationLog mutationLog : MutableThing.mutationLogs) {
            if(mutationLog.getId() == thing.getId()&& mutationLog.getAuthorityDecision()){
                count++;
            }
        }
        if(count > 0){
            need = round((double)thing.getNeed()/count,2);
        }
        else{
            need = 0;
        }
        return round(1-need,2);
    }

}
