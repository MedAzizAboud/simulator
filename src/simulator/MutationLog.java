/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.util.ArrayList;

/**
 *
 * @author aziza
 */
public class MutationLog {
    private int id;
    private String date;
    private int simulationMode;
    private ArrayList<Capability> currentState;
    private ArrayList<Capability> nextState;
    private boolean thingDecision;
    private boolean authorityDecision;
    
    /**
     * creates new MutationLog object
     * @param date
     * @param simulationMode
     * @param currentState
     * @param nextState
     * @param thingDecision
     * @param authorityDecision 
     */
    public MutationLog(int id, String date, int simulationMode, ArrayList<Capability> currentState, ArrayList<Capability> nextState, boolean thingDecision, boolean authorityDecision){
        this.id=id;
        this.date=date;
        this.simulationMode=simulationMode;
        this.currentState=currentState;
        this.nextState=nextState;
        this.thingDecision=thingDecision;
        this.authorityDecision=authorityDecision;
    }
    public int getId(){
        return id;
    }
    public String getDate(){
        return date;
    }
    public int getSimulationMode(){
        return simulationMode;
    }
    public ArrayList<Capability> getCurrentState(){
        return currentState;
    }
    public ArrayList<Capability> getNextState(){
        return nextState;
    }
    public boolean getThingDecision(){
        return thingDecision;
    }
    public boolean getAuthorityDecision(){
        return authorityDecision;
    }
    /**
     * gets deviant logs corresponding to a MutableThing
     * @param thingId
     * @return list of mutationLogs
     */
    public static ArrayList<MutationLog> getDeviantLogs(int thingId){
        ArrayList<MutationLog> deviantLogs = new ArrayList<>();
        for (MutationLog mutationLog : MutableThing.mutationLogs) {
            if(mutationLog.getId() == thingId){
            boolean sameState = false;
            int count = 0;
            if(mutationLog.getCurrentState().size() == mutationLog.getNextState().size()){
                for(int i=0;i<mutationLog.getCurrentState().size();i++){
                    if(!mutationLog.getNextState().contains(mutationLog.getCurrentState().get(i))){
                        break;
                    }
                    count++;
                }
                if(count == mutationLog.getCurrentState().size()){
                    sameState = true;
                }
            }
            if(mutationLog.getAuthorityDecision() == sameState){
                deviantLogs.add(mutationLog);
            }
            }
        }
        return deviantLogs;
    }
    /**
     * gets nonDeviant logs corresponding to a MutableThing
     * @param thingId
     * @return list of mutationLogs
     */
    public static ArrayList<MutationLog> getNonDeviantLogs(int thingId){
        ArrayList<MutationLog> nonDeviantLogs = new ArrayList<>();
        for (MutationLog mutationLog : MutableThing.mutationLogs) {
            if(mutationLog.getId() == thingId){
            boolean sameState = false;
            int count = 0;
            if(mutationLog.getCurrentState().size() == mutationLog.getNextState().size()){
                for(int i=0;i<mutationLog.getCurrentState().size();i++){
                    if(!mutationLog.getNextState().contains(mutationLog.getCurrentState().get(i))){
                        break;
                    }
                    count++;
                }
                if(count == mutationLog.getCurrentState().size()){
                    sameState = true;
                }
            }
            if(mutationLog.getAuthorityDecision() != sameState){
                nonDeviantLogs.add(mutationLog);
            }
            }
        }
        return nonDeviantLogs;
    }
}
