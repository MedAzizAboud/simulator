/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.paukov.combinatorics3.Generator;

/**
 *
 * @author aziza
 */
public class MutableThing extends Thing implements Behavior{
    
    private double trustLevel;
    private int cooperationDegree;
    private ArrayList<Capability> allCapabilities;
    private ArrayList<Capability> actualCapabilities;
    public ArrayList<ArrayList<Capability>> possibleStatus;
    public static ArrayList<MutationLog> mutationLogs = new ArrayList<>();
    /**
     * creates new MutableThing
     * @param id
     * @param name
     * @param zone
     * @param trustLevel
     * @param cooperationDegree 
     * @param allCapabilities 
     * @param actualCapabilities 
     */
    public MutableThing(int id, String name, int zone, double trustLevel, int cooperationDegree, ArrayList<Capability> allCapabilities, ArrayList<Capability> actualCapabilities){
        super(id, name, zone);
        this.allCapabilities = allCapabilities;
        this.actualCapabilities = actualCapabilities;
        this.trustLevel = trustLevel;
        this.cooperationDegree=cooperationDegree;
        this.possibleStatus = this.getPossibleStatus();
        Simulator.deviant.put(id, 0);
        Simulator.nonDeviant.put(id, 0);
    }
    /**
     * creates new MutableThing with default trustLevel = 1
     * @param id
     * @param name
     * @param zone
     * @param cooperationDegree 
     * @param allCapabilities 
     * @param actualCapabilities 
     */
    public MutableThing(int id, String name, int zone, int cooperationDegree, ArrayList<Capability> allCapabilities, ArrayList<Capability> actualCapabilities){
        super(id, name, zone);
        this.allCapabilities = allCapabilities;
        this.actualCapabilities = actualCapabilities;
        this.cooperationDegree = cooperationDegree;
        this.trustLevel = 1;
        this.possibleStatus = this.getPossibleStatus();
        Simulator.deviant.put(id, 0);
        Simulator.nonDeviant.put(id, 0);
    }
    
    public MutableThing(int id, String name, int zone, int cooperationDegree, ArrayList<Capability> allCapabilities){
        super(id, name, zone);
        this.allCapabilities = allCapabilities;
        this.cooperationDegree = cooperationDegree;
        this.trustLevel = 1;
        this.possibleStatus = this.getPossibleStatus();
        this.actualCapabilities = this.possibleStatus.get(new Random().nextInt(this.possibleStatus.size()));
        Simulator.deviant.put(id, 0);
        Simulator.nonDeviant.put(id, 0);
    }
    
    public void setTrustLevel(double trusLevel){
        this.trustLevel=trusLevel;
    }
    public double getTrustLevel(){
        return this.trustLevel;
    }
    public void setCooperationDegree(int cooperationDegree){
        this.cooperationDegree=cooperationDegree;
    }
    public int getCooperationDegree(){
        return this.cooperationDegree;
    }
    public ArrayList<Capability> getAllCapabilities(){
        return this.allCapabilities;
    }
    public ArrayList<Capability> getActualCapabilities(){
        return this.actualCapabilities;
    }
    
    /**
     * generate all possible status that a MutableThing can take
     * @return all possible status
     */
    private ArrayList<ArrayList<Capability>> getPossibleStatus(){
        ArrayList<ArrayList<Capability>> capabilities = new ArrayList<>();
        for(int i=1;i<=this.getAllCapabilities().size();i++){
         Iterator<List<Capability>> iterator = Generator.combination(this.getAllCapabilities())
       .simple(i)
       .stream().iterator();
        while(iterator.hasNext()){
            capabilities.add((ArrayList<Capability>) iterator.next());
        }   
        }
        return capabilities;
    }
    /**
     * thing choose if he wants to mutate or not
     * if he wants to mutate call decideMutation to select desired mutation
     * and calls Authority's method handleMutationRequest
     * and calls MutableThing's method executeMutation
     * @return true if MutableThing asks for mutation otherwise return false
     */
    public boolean askForMutation() throws IOException{
        int decision = new Random().nextInt(2);
        if(decision != 0){
            ArrayList<Capability> nextStatus = this.decideMutation();
            Authority.mutationRequests.put(this, nextStatus);
            //write into the  file thing asked for mutation true
            Simulator.myWriter.write("thing"+this.getId()+" asked for mutation: true ");
            return true;
        }
        //write into the file thing asked for mutation false
        Simulator.myWriter.write("thing"+this.getId()+" asked for mutation: false ");
        return false;
    }
    /**
     * the MutableThing chooses the Mutation he wants to execute
     * @return desiredMutation
     */
    private ArrayList<Capability> decideMutation() throws IOException{

        int position = 0;
        boolean goodMutation = false ;
        ArrayList<ArrayList<Capability>> capabilities = this.possibleStatus;
        while(!goodMutation){
        position = new Random().nextInt(capabilities.size());
        if(this.getActualCapabilities().size() != capabilities.get(position).size()){
            goodMutation = true;
        }
        else{
            for(int i=0;i<this.getActualCapabilities().size();i++){
               if(!capabilities.get(position).contains(this.getActualCapabilities().get(i))){
                   goodMutation = true;
                   break;
               } 
            }
        }
        }
        String s ="desired mutation:";
        
        for(int i=0;i<this.possibleStatus.get(position).size();i++){
            s+=" "+this.possibleStatus.get(position).get(i).getType();
        }
        Simulator.myWriter.write(s+" ");
        return capabilities.get(position);
        
    }
    /**
     * calls decide method
     * uses thingDecision and authorityResponse to decide to execute mutation or not
     * if MutableThing decides to mutate, calls bind and/or unbind method and saveMutationLog method
     * @param authorityResponse
     * @param nextStatus
     */
    public void executeMutation(boolean authorityResponse, ArrayList<Capability> nextStatus) throws IOException{
        boolean decision = this.decide();
        Simulator.myWriter.write("decide method returned: "+decision+"\n");
        boolean mutationDecision;
        if(decision){
            mutationDecision = authorityResponse;
            Simulator.nonDeviant.put(this.getId(), Simulator.nonDeviant.get(this.getId()) + 1);
        }else {
            mutationDecision = !authorityResponse;
            Simulator.deviant.put(this.getId(), Simulator.deviant.get(this.getId()) + 1);
        }
         ArrayList<Capability> initialStatus = new ArrayList<>();
            for(int i=0;i<this.getActualCapabilities().size();i++){
                
                initialStatus.add(this.getActualCapabilities().get(i));
            }
        if(mutationDecision){
            
            for(int i=0;i<initialStatus.size();i++){
                if((nextStatus.contains(initialStatus.get(i)))== false){
                    this.unbind(initialStatus.get(i));
                }
            }
            for(int i=0;i<nextStatus.size();i++){
                if((initialStatus.contains(nextStatus.get(i)))== false){
                    this.bind(nextStatus.get(i));
                }
            }
            Simulator.myWriter.write("\n");
            this.saveMutationLog(authorityResponse, true, initialStatus, nextStatus);
        }
        else{
            this.saveMutationLog(authorityResponse, true, initialStatus, initialStatus);
        }
        
    }
    /**
     * the MutableThing aquires a new capability
     * @param capability
     */
    private void bind(Capability capability) throws IOException{
        this.actualCapabilities.add(capability);
        Simulator.myWriter.write("bind: "+capability.getType()+" ");
    }
    /**
     * the MutableThing looses a capability
     * @param capability
     */
    private void unbind(Capability capability) throws IOException{
        this.actualCapabilities.remove(capability);
        Simulator.myWriter.write("unbind: "+capability.getType()+" ");
    }
    /**
     * creates MutationLog object and insert it in MutationLog table in database
     * @param authorityResponse
     * @param thingDecision
     * @param currentState
     * @param nextState 
     */
    private void saveMutationLog(boolean authorityResponse, boolean thingDecision, ArrayList<Capability> currentState, ArrayList<Capability> nextState) throws IOException{
        
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String date = format.format(new Date());
        MutationLog log = new MutationLog(this.getId(), date, Simulator.simulationMode, currentState, nextState, thingDecision, authorityResponse);
        MutableThing.mutationLogs.add(log);
        String s = "MutationLog: "+date+" thing"+this.getId()+" simulationMode: "+Simulator.simulationMode+" thingDecision: "+thingDecision+" authorityResponse: "+authorityResponse+" currentState:";
        for(int i=0;i<currentState.size();i++){
            s += " "+currentState.get(i).getType();
        }
        s+=" nextState:";
        for(int i=0;i<nextState.size();i++){
            s += " "+nextState.get(i).getType();
        }
        Simulator.myWriter.write(s+"\n\n");
        /*try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection("jdbc:mysql://localhost/simulation?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC", "root", "root");
            String query = "insert into MutationLog values (Date='"+log.getDate()+"', SimulationMode='"+Simulator.simulationMode+"', currentState='"+currentState+"', nextState='"+nextState+"', thingDecision='"+thingDecision+"', authorityResponse='"+authorityResponse+"')";
            Statement stm = con.createStatement();
            stm.executeUpdate(query);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(MutableThing.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
    
    /**
     * updates MutableThing trustLevel
     * calls MutableThing's method setTrustLevel
     * @param trustLevel
     */
    public void updateMutableThingTrustLevel(double trustLevel){
        this.setTrustLevel(trustLevel);
    }
    
    /**
     * decides if the Thing is going to respect authority decision or act in a deviant way
     * if thing is going to respect authority decision returns true else return false
     * @return thingDecision
     */
    @Override
    public boolean decide() {
        
        double s;
        switch (this.getCooperationDegree()) {
            case 100:
                s=10;
                break;
            case 90:
                s=3.25;
                break;
            case 80:
                s=2;
                break;
            case 70:
                s=1.25;
                break;
            case 60:
                s=0.75;
                break;
            default:
                s=0.1;
                break;
        }
        ZipfDistribution dist= new ZipfDistribution(2, s);
        return dist.sample()== 1;
    }
    @Override
    public String toString(){
        String s = "name: "+this.getName()+" zone: "+this.getZone()+" trustLevel: "+this.getTrustLevel()+" cooperationdegree: "+this.getCooperationDegree()+ " actualCapabilities: ";
        for(int i=0;i<this.getActualCapabilities().size();i++){
            s += this.getActualCapabilities().get(i).getType()+" ";
        }
        s += "allCapabilities:";
        for(int i=0;i<this.getAllCapabilities().size();i++){
            s += " "+this.getAllCapabilities().get(i).getType();
        }
        s+="\n";
        return s;
    }
}
