/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
import javafx.util.Pair;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.paukov.combinatorics3.Generator;

/**
 *
 * @author aziza
 */
public class MutableThing extends Thing implements Behavior{
    
    private double trustLevel;
    private int cooperationDegree;
    private int impact;
    private int need;
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
    public MutableThing(int id, String name, Zone zone, double trustLevel, int cooperationDegree, ArrayList<Capability> allCapabilities, ArrayList<Capability> actualCapabilities){
        super(id, name, zone);
        this.allCapabilities = allCapabilities;
        this.actualCapabilities = actualCapabilities;
        this.trustLevel = trustLevel;
        this.cooperationDegree=cooperationDegree;
        this.possibleStatus = this.getPossibleStatus();
        this.impact=0;
        this.need=0;
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
    public MutableThing(int id, String name, Zone zone, int cooperationDegree, ArrayList<Capability> allCapabilities, ArrayList<Capability> actualCapabilities){
        super(id, name, zone);
        this.allCapabilities = allCapabilities;
        this.actualCapabilities = actualCapabilities;
        this.cooperationDegree = cooperationDegree;
        this.trustLevel = 1;
        this.possibleStatus = this.getPossibleStatus();
        this.impact=0;
        this.need=0;
    }
    
    public MutableThing(int id, String name, Zone zone, int cooperationDegree, ArrayList<Capability> allCapabilities){
        super(id, name, zone);
        this.allCapabilities = allCapabilities;
        this.cooperationDegree = cooperationDegree;
        this.trustLevel = 1;
        this.possibleStatus = this.getPossibleStatus();
        this.actualCapabilities = this.possibleStatus.get(new Random(/*Simulator.actualCapabilitiesSeed*/).nextInt(this.possibleStatus.size()));
        //Simulator.actualCapabilitiesSeed++;
        this.impact=0;
        this.need=0;
    }
    
    public MutableThing(int id, String name, Zone zone, double trustLevel, int cooperationDegree, ArrayList<Capability> allCapabilities){
        super(id, name, zone);
        this.allCapabilities = allCapabilities;
        this.trustLevel = trustLevel;
        this.cooperationDegree=cooperationDegree;
        this.possibleStatus = this.getPossibleStatus();
        this.actualCapabilities = this.possibleStatus.get(new Random(/*Simulator.actualCapabilitiesSeed*/).nextInt(this.possibleStatus.size()));
        //Simulator.actualCapabilitiesSeed++;
        this.impact=0;
        this.need=0;
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
     * increments the value of the impact of a MutableThing
     * @param impact 
     */
    public void addImpact( int impact){
        this.impact+=impact;
    }
    public int getImpact(){
        return this.impact;
    }
    public void addNeed(int need){
        this.need+=need;
    }
    public int getNeed(){
        return this.need;
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
     * and adds his mutation to his zone's mutationWaitingqueue
     * with the duration and the delay if there is any
     * @return true if MutableThing asks for mutation otherwise return false
     */
    public boolean askForMutation() throws IOException, SQLException, ClassNotFoundException{
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con= DriverManager.getConnection("jdbc:mysql://localhost/simulation?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC", "root", "root");
        Statement stm = con.createStatement();
        String query;
        
        int decision = new Random(/*Simulator.askForMutationSeed*/).nextInt(2);
        //Simulator.askForMutationSeed++;
        if(decision != 0){
            // decide to do bad or good mutation
            boolean cancelMutation = false;
            boolean goodDecision = this.decide();
            ArrayList<Capability> nextStatus= new ArrayList<>();
            ArrayList<Capability> actualCapa = this.getActualCapabilities();
            ArrayList<Constraint> zoneConstraint = Simulator.zonesRules.get(this.getZone().getId());
            if(!goodDecision){
              query = "insert into statistique (passolliciter,solliciterbien,sollicitermauvais,simulationtime,thingid) values("+0+","+0+","+1+","+Simulator.cycleCounter+","+this.getId()+")";
              stm.executeUpdate(query);
              for(int i=0;i<zoneConstraint.size();i++){
                  if(actualCapa.contains(Simulator.capabilities.get(zoneConstraint.get(i).capability))&& actualCapa.size()>1){
                      //nextStatus = actualCapa;
                      for(int j=0;j<actualCapa.size();j++){
                          nextStatus.add(actualCapa.get(j));
                      }
                      nextStatus.remove(Simulator.capabilities.get(zoneConstraint.get(i).capability));
                      break;
                  }
              }
              if(nextStatus.isEmpty()){
                  for(int i=0;i<zoneConstraint.size();i++){
                      nextStatus = this.getMutationNotContainingC(zoneConstraint.get(i).capability);
                      if(!nextStatus.isEmpty()){
                          break;
                      }
                  }
              }
              if(nextStatus.isEmpty()){
                  nextStatus = this.decideMutation();  
              }
            }
            else{
              query = "insert into statistique (passolliciter,solliciterbien,sollicitermauvais,simulationtime,thingid) values("+0+","+1+","+0+","+Simulator.cycleCounter+","+this.getId()+")";
              stm.executeUpdate(query);
                for(int i=0;i<zoneConstraint.size();i++){
                    if(!actualCapa.contains(Simulator.capabilities.get(zoneConstraint.get(i).capability))){

                        ArrayList<Capability> allCapa = this.getAllCapabilities();
                        if(allCapa.contains(Simulator.capabilities.get(zoneConstraint.get(i).capability))){
                            
                        for(int j=0;j<actualCapa.size();j++){
                          nextStatus.add(actualCapa.get(j));
                        }
                        nextStatus.add(Simulator.capabilities.get(zoneConstraint.get(i).capability));
                        break;
                        }
                    }
                }
                if(nextStatus.isEmpty()){
                    for(int i=0;i<actualCapa.size();i++){
                        int count=0;
                        for(int j=0;j<zoneConstraint.size();j++){
                            if(!actualCapa.get(i).getType().equals(zoneConstraint.get(j).capability)){
                                count++;
                            }
                        }
                        if(count == zoneConstraint.size()&& actualCapa.size()>1){
                            //nextStatus = actualCapa;
                            for(int j=0;j<actualCapa.size();j++){
                          nextStatus.add(actualCapa.get(j));
                          }
                            nextStatus.remove(actualCapa.get(i));
                            break;
                        }
                    }
              }
                if(nextStatus.isEmpty()){
                    ArrayList<Capability> allCapa = this.getAllCapabilities();
                    for(int i=0;i<allCapa.size();i++){
                        if(!actualCapa.contains(allCapa.get(i))){
                            for(int j=0;j<actualCapa.size();j++){
                             nextStatus.add(actualCapa.get(j));
                            }
                            nextStatus.add(allCapa.get(i));
                            break;
                        }
                    }
                }
                if(nextStatus.isEmpty()){
                 // nextStatus = this.decideMutation();
                 cancelMutation=true;
                 //il ne mute meme pas
              }  
              
            }
            if(cancelMutation){
                Simulator.myWriter.write("thing"+this.getId()+" asked for mutation: false ");
                return false;
            }
            else{
                
            int duration = this.calculateMutationDuration(nextStatus);
            int delay;
            if(this.decide()){
                delay=0;
            }
            else{
                delay= 2;//new Random().nextInt(duration)+1;
            }
            Simulator.zonesWaitingQueues.get(this.getZone().getId()).add(new Mutation(this,nextStatus,delay,duration));
            //write into the  file thing asked for mutation true
            Simulator.myWriter.write("thing"+this.getId()+" asked for mutation: true, desiredmutation: ");
            for(int i=0;i<nextStatus.size();i++){
               Simulator.myWriter.write(nextStatus.get(i).getType()); 
            }
            Simulator.myWriter.write(" Decision "+goodDecision);
            con.close();
            return true;
            }
            
        }
        //}
        //write into the file thing asked for mutation false
        Simulator.myWriter.write("thing"+this.getId()+" asked for mutation: false ");
        query = "insert into statistique (passolliciter,solliciterbien,sollicitermauvais,simulationtime,thingid) values("+1+","+0+","+0+","+Simulator.cycleCounter+","+this.getId()+")";
        stm.executeUpdate(query);
        con.close();
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
        /*String s ="desired mutation:";
        
        for(int i=0;i<this.possibleStatus.get(position).size();i++){
            s+=" "+this.possibleStatus.get(position).get(i).getType();
        }
        Simulator.myWriter.write(s+" ");*/
        return capabilities.get(position);
        
    }
    /**
     * calls decide method
     * if MutableThing is allowed to mutate, he verifies if there's enough resources in his zone to do so 
     * if its the case he adds his mutation to the execution queue and removes it from the corresponding waitingQueue and decrements the zone common resources
     * and he calls bind and/or unbind method 
     * at the end calls saveMutationLog method
     * @param authorityResponse
     * @param nextStatus
     * @param mutation
     */
    public void executeMutation(boolean authorityResponse, ArrayList<Capability> nextStatus, Mutation mutation) throws IOException{
        /*boolean decision = this.decide();
        Simulator.myWriter.write("decide method returned: "+decision+"\n");
        boolean mutationDecision;
        */
        int estimatedResourceConsumption=0;
        
        /*if(decision){
            mutationDecision = authorityResponse;
            Simulator.nonDeviant.put(this.getId(), Simulator.nonDeviant.get(this.getId()) + 1);
        }else {
            mutationDecision = !authorityResponse;
            Simulator.deviant.put(this.getId(), Simulator.deviant.get(this.getId()) + 1);
            mutation.mutationDelay+=mutation.mutationDuration;
            mutation.mutationDuration=0;
        }*/
         ArrayList<Capability> initialStatus = new ArrayList<>();
            for(int i=0;i<this.getActualCapabilities().size();i++){
                
                initialStatus.add(this.getActualCapabilities().get(i));
            }
        double achievementDegree = Simulator.authority.calculateAchievementDegree(this);
        for(int i=0;i<nextStatus.size();i++){
                    estimatedResourceConsumption+= nextStatus.get(i).getResourceConsumption();
                }
        int actualResourceConsumption= Simulator.authority.calculateActualResourceConsumption(estimatedResourceConsumption);
        // memoriser l'etat de la zone
        ArrayList<Constraint> previousState = Simulator.zonesStates.get(this.getZone().getId());
        if(authorityResponse){
            if(this.getZone().getCommonResources()>0){
                
                this.getZone().allocateCommonResources(1);
                Authority.mutationsToRemoveFromZonesQueues.add(new Pair(this.getZone().getId(),mutation));
                Authority.mutationRequests.add(mutation);
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
            this.saveMutationLog(authorityResponse, true, initialStatus, nextStatus, achievementDegree, estimatedResourceConsumption, actualResourceConsumption, mutation.mutationDelay);
            ArrayList<Constraint> actualState = Simulator.zonesStates.get(this.getZone().getId());
            Simulator.authority.updateNeed(this,previousState,actualState);
            }
            else{
                this.saveMutationLog(authorityResponse, true, initialStatus, initialStatus, achievementDegree, estimatedResourceConsumption, 0, mutation.mutationDelay);   
            }
        }
        else{
            Authority.mutationsToRemoveFromZonesQueues.add(new Pair(this.getZone().getId(),mutation));
            this.saveMutationLog(authorityResponse, true, initialStatus, initialStatus, achievementDegree, estimatedResourceConsumption, 0, mutation.mutationDelay);
        }
        
    }
    /**
     * the MutableThing aquires a new capability and updates the thing's zoneState
     * @param capability
     */
    private void bind(Capability capability) throws IOException{
        this.actualCapabilities.add(capability);
        Simulator.myWriter.write("bind: "+capability.getType()+" ");
        ArrayList<Constraint> constraint = Simulator.zonesStates.get(this.getZone().getId());
        for(int i=0;i<constraint.size();i++){
           if(constraint.get(i).capability.equals(capability.getType())){
               constraint.get(i).number++;
           } 
        }
    }
    /**
     * the MutableThing looses a capability and updates the thing's zoneState
     * @param capability
     */
    private void unbind(Capability capability) throws IOException{
        this.actualCapabilities.remove(capability);
        Simulator.myWriter.write("unbind: "+capability.getType()+" ");
        ArrayList<Constraint> constraint = Simulator.zonesStates.get(this.getZone().getId());
        for(int i=0;i<constraint.size();i++){
           if(constraint.get(i).capability.equals(capability.getType())){
               constraint.get(i).number--;
           } 
        }
    }
    /**
     * creates MutationLog object and insert it in MutationLog table in database
     * @param authorityResponse
     * @param thingDecision
     * @param currentState
     * @param nextState 
     */
    private void saveMutationLog(boolean authorityResponse, boolean thingDecision, ArrayList<Capability> currentState, ArrayList<Capability> nextState, double achievementDegree, int estimatedResourceConsumption, int actualResourceConsumption, int mutationDelay) throws IOException{
        
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String date = format.format(new Date());
        MutationLog log = new MutationLog(this.getId(), date, Simulator.simulationMode, currentState, nextState, thingDecision, authorityResponse, achievementDegree, estimatedResourceConsumption, actualResourceConsumption);
        MutableThing.mutationLogs.add(log);
        String s = "MutationLog: "+date+" thing"+this.getId()+" simulationMode: "+Simulator.simulationMode+" thingDecision: "+thingDecision+" authorityResponse: "+authorityResponse+" currentState:";
        for(int i=0;i<currentState.size();i++){
            s += " "+currentState.get(i).getType();
        }
        s+=" nextState:";
        for(int i=0;i<nextState.size();i++){
            s += " "+nextState.get(i).getType();
        }
        s+=" achievementDegree: "+achievementDegree;
        Simulator.myWriter.write(s+"\n\n");
        if(Simulator.SaveSimulation>0){
            this.addMutationToDb(authorityResponse, thingDecision, currentState, nextState, achievementDegree, actualResourceConsumption, mutationDelay);
        }
    }
    
    /**
     * updates MutableThing trustLevel
     * calls MutableThing's method setTrustLevel
     * @param trustLevel
     */
    public void updateMutableThingTrustLevel(double trustLevel){
        this.setTrustLevel(trustLevel);
        if(Simulator.SaveSimulation>0){
            this.updateMutableThingTrustLevelInDb(trustLevel);
        }
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
                s=20;
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
            case 40:
                s=0.75;
                break;
            case 30:
                s=1.25;
                break;
            case 20:
                s=2;
                break;
            case 10:
                s=3.25;
                break;
            default:
                s=0.1;
                break;
        }
        //ZipfDistribution dist= new ZipfDistribution(2, s);
        RandomDataGenerator generator = new RandomDataGenerator();
        //generator.reSeed(Simulator.deviationSeed);
        //Simulator.deviationSeed++;
        int value = generator.nextZipf(2, s);
        if(this.getCooperationDegree() > 40)
            //return dist.sample()== 1;
             return value==1;
        //return dist.sample()!= 1;
        return value!=1;
    }
    @Override
    public String toString(){
        String s = "id: "+this.getId()+" zone: "+this.getZone().getId()+" totaldelayedMutations: "+this.getZone().getDelayedMutationsNumber()+" trustLevel: "+this.getTrustLevel()+" cooperationdegree: "+this.getCooperationDegree()+ " actualCapabilities: ";
        for(int i=0;i<this.getActualCapabilities().size();i++){
            s += this.getActualCapabilities().get(i).getType()+" ";
        }
        s += "allCapabilities:";
        for(int i=0;i<this.getAllCapabilities().size();i++){
            s += " "+this.getAllCapabilities().get(i).getType();
        }
        s+=" impact: "+this.getImpact()+" need: "+this.getNeed()+"\n";
        return s;
    }
    /**
     * insert a new mutationlog into the database's mutation table
     * @param authorityResponse
     * @param thingDecision
     * @param currentState
     * @param nextState 
     */
    private void addMutationToDb(boolean authorityResponse, boolean thingDecision, ArrayList<Capability> currentState, ArrayList<Capability> nextState, double achievementDegree, int actualResourceConsumption, int mutationDelay){
        String initialState="";
        for(int i=0;i<currentState.size();i++){
            initialState+=currentState.get(i).getType()+",";
        }
        String finalState="";
        for(int i=0;i<nextState.size();i++){
            finalState+=nextState.get(i).getType()+",";
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection("jdbc:mysql://localhost/simulation?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC", "root", "root");
            String query = "insert into Mutation(simulationtime,simulationid,thingid,currentstate,nextstate,thingdecision,authorityresponse,achievementdegree,actualresourceconsumption,delay) values ("+Simulator.cycleCounter+", "+Simulator.simulationId+", "+this.getId()+", '"+initialState+"', '"+finalState+"', '"+thingDecision+"', '"+authorityResponse+"', "+achievementDegree+", "+actualResourceConsumption+", "+mutationDelay+")";
            Statement stm = con.createStatement();
            stm.executeUpdate(query);
            con.close();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(MutableThing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * updates mutablething tustlevel in the database's mutablething table
     * @param trustLevel 
     */
    private void updateMutableThingTrustLevelInDb(double trustLevel){
       try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection("jdbc:mysql://localhost/simulation?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC", "root", "root");
            String query = "update mutablething set trustlevel="+trustLevel+" , impact="+this.getImpact()+" where thingid="+this.getId()+"";
            Statement stm = con.createStatement();
            stm.executeUpdate(query);
            con.close();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(MutableThing.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    /**
     * calculates the expected duration of a mutation
     * @param nextStatus
     * @return duration
     */
    private int calculateMutationDuration(ArrayList<Capability> nextStatus){
        int duration =0;
        ArrayList<Capability> initialStatus = this.getActualCapabilities();
        for(int i=0;i<initialStatus.size();i++){
                if((nextStatus.contains(initialStatus.get(i)))== false){
                    duration++;
                }
            }
            for(int i=0;i<nextStatus.size();i++){
                if((initialStatus.contains(nextStatus.get(i)))== false){
                    duration++;
                }
            }
           return duration; 
    }
    /**
     * returns a combination of capabilities from a thing's potential capabilities not containing a specific capability
     * @param c
     * @return 
     */
    private ArrayList getMutationNotContainingC(String c){
     ArrayList<ArrayList<Capability>> capabilities = this.possibleStatus;
     for(int i=0;i<capabilities.size();i++){
         if(!capabilities.get(i).contains(new Capability("capa",c,0))){
             return capabilities.get(i);
         }
     }
     return null;
    }
}
