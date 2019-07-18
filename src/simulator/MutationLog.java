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
    private double achievementDegree;
    private int actualResourceConsumption;
    private int estimatedResourceConsumption;
    
    /**
     * creates new MutationLog object
     * @param date
     * @param simulationMode
     * @param currentState
     * @param nextState
     * @param thingDecision
     * @param authorityDecision 
     */
    public MutationLog(int id, String date, int simulationMode, ArrayList<Capability> currentState, ArrayList<Capability> nextState, boolean thingDecision, boolean authorityDecision, double achievementDegree, int actualResourceConsumption, int estimatedResourceConsumption){
        this.id=id;
        this.date=date;
        this.simulationMode=simulationMode;
        this.currentState=currentState;
        this.nextState=nextState;
        this.thingDecision=thingDecision;
        this.authorityDecision=authorityDecision;
        this.achievementDegree=achievementDegree;
        this.estimatedResourceConsumption=estimatedResourceConsumption;
        this.actualResourceConsumption=actualResourceConsumption;
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
    public double getAchievementDegree(){
        return achievementDegree;
    }
    public int getActualResourceConsumption(){
        return actualResourceConsumption;
    }
    public int getEstimatedResourceConsumption(){
        return estimatedResourceConsumption;
    }
    
}
