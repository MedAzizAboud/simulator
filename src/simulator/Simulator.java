/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.paukov.combinatorics3.Generator;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import javafx.util.Pair;
import static org.apache.commons.math3.util.Precision.round;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 *
 * @author aziza
 */
public class Simulator {

    public static int numberOfThings;
    public static long deviationSeed;
    public static long askForMutationSeed;
    public static long actualCapabilitiesSeed;
    public static int numberOfTrustableThings;
    public static int simulationDuration;
    public static int simulationMode;
    public static int SaveSimulation;
    public static int scenarioId;
    public static String simulationDate;
    public static int simulationId;
    public static int cycleCounter =1;
    public static int resourceConsumptionThreshold;
    public static double resourceWeight;
    public static double mutationResultWeight;
    public static double minimalTrustLevel;
    public static int totalConstraintNumber=0;
    public static ArrayList<MutableThing> things = new ArrayList<>();
    public static Authority authority;
    public static FileWriter myWriter;
    public static Map<Integer,Zone> zones = new HashMap<>();
    public static Map<Integer,ArrayList<Constraint>> zonesRules = new HashMap<>();
    public static Map<Integer,ArrayList<Constraint>> zonesStates = new HashMap<>();
    public static Map<Integer,Queue<Mutation>> zonesWaitingQueues = new HashMap<>();
    public static Map<Integer,Integer> deviant = new HashMap<>();
    public static Map<Integer,Integer> nonDeviant = new HashMap<>();
    public static Map<String,Capability> capabilities = new HashMap();
    /**
     * creates MutableThings used in the simulation
     * @return list of mutablethings
     */
    private ArrayList<MutableThing> createThing(Connection con) throws SQLException, IOException{
    
        ArrayList<MutableThing> createdThings = new ArrayList<>();
        ArrayList<Integer> lowCooperationDegrees = new ArrayList<>();
        ArrayList<Integer> HighCooperationDegrees = new ArrayList<>();
        ArrayList<Capability> NotDesiredCapabilities = new ArrayList<>();
        lowCooperationDegrees.add(50);
        HighCooperationDegrees.add(100);
        int i=0;
        MutableThing thing;
        for (Map.Entry<Integer,ArrayList<Constraint>> entry : zonesStates.entrySet()){
            for(int j=0;j<entry.getValue().size();j++){
                int count=0;
                int number = entry.getValue().get(j).number;
                Capability c =Simulator.capabilities.get(entry.getValue().get(j).capability);
                NotDesiredCapabilities.add(c);
                for(int k=0;k<number;k++){
                   if(i<totalConstraintNumber*numberOfTrustableThings/numberOfThings){
                   thing = new MutableThing(i,"thing "+i+""+simulationId,zones.get(entry.getKey()),HighCooperationDegrees.get(new Random().nextInt(HighCooperationDegrees.size())),Simulator.getPossibleCapabilitiesContainingC(c));
                   }else{
                   thing = new MutableThing(i,"thing "+i+""+simulationId,zones.get(entry.getKey()),lowCooperationDegrees.get(new Random().nextInt(lowCooperationDegrees.size())),Simulator.getPossibleCapabilitiesContainingC(c));
                   }
                   if(thing.getActualCapabilities().contains(c)){
                       count++;
                   }
                   i++;
                   createdThings.add(thing);
                   if(SaveSimulation>0){
                   Simulator.addMutableThingToDb(con,thing);
                   }
                }
            entry.getValue().get(j).number=count;
            }
            
        }  
        for(int j=i,l=0;j<numberOfThings;j++,l++){
           
            if(l<(numberOfThings-totalConstraintNumber)*numberOfTrustableThings/numberOfThings)
                thing = new MutableThing(j,"thing "+j+""+simulationId,zones.get(new Random().nextInt(1)+1),HighCooperationDegrees.get(new Random().nextInt(HighCooperationDegrees.size())),Simulator.getPossibleCapabilitiesNotContainingC(NotDesiredCapabilities.get(0)));
            else
                thing = new MutableThing(j,"thing "+j+""+simulationId,zones.get(new Random().nextInt(1)+1),lowCooperationDegrees.get(new Random().nextInt(lowCooperationDegrees.size())),Simulator.getPossibleCapabilitiesNotContainingC(NotDesiredCapabilities.get(0)));
            createdThings.add(thing);
            if(SaveSimulation>0){
                Simulator.addMutableThingToDb(con,thing);
            }
        }
        return createdThings;
    }
    /**
     * create the Authority used in the simulation
     * @return authority
     */
    private Authority createAuthority(Connection con)throws SQLException{
        
        return new Authority(0, "authority");
    }
    /**
     * checks if there are MutableThings who want to mutate every simulation cycle
     * calls authority's method handleMutationRequest 
     * then calls MutableThing's method askForMutation
     * and finally calls authority's method addMutationRequest
     */
    private void checkForMutationRequests() throws IOException, SQLException, ClassNotFoundException{
        boolean result;
       
        authority.handleMutationRequest();
        
        for(int i=0;i<things.size();i++){
            MutableThing thing = Simulator.things.get(i);
            result = thing.askForMutation();
            myWriter.write("\n\n");
        }
        if(Simulator.minimalTrustLevel>=0){
            authority.addMutationRequest(Simulator.minimalTrustLevel);
        }
        else{
            authority.addMutationRequest();
        }
    }
    /**
     * creates all possible combinations of capabilities from a list of capabilities
     * @return 
     */
    private static ArrayList<ArrayList<Capability>> getPossibleCapabilities(){
        ArrayList<ArrayList<Capability>> capability = new ArrayList<>();
        ArrayList<Capability> allPossibleCapabilities = new ArrayList<>();
        for (Capability capa : Simulator.capabilities.values())  {
            allPossibleCapabilities.add(capa);
        } 
        for(int i=2;i<=allPossibleCapabilities.size();i++){
         Iterator<List<Capability>> iterator = Generator.combination(allPossibleCapabilities)
       .simple(i)
       .stream().iterator();
        while(iterator.hasNext()){
            capability.add((ArrayList<Capability>) iterator.next());
        }   
        }
        return capability;
    }
    /**
     * generates a combination of capabilities containing some specified capabilities
     * @param c
     * @return 
     */
     private static ArrayList<Capability> getPossibleCapabilitiesContainingC(Capability c){
        ArrayList<ArrayList<Capability>> capability = new ArrayList<>();
        ArrayList<Capability> allPossibleCapabilities = new ArrayList<>();
        for (Capability capa : Simulator.capabilities.values())  {
            allPossibleCapabilities.add(capa);
        } 
        
        for(int i=2;i<=allPossibleCapabilities.size();i++){
         Iterator<List<Capability>> iterator = Generator.combination(allPossibleCapabilities)
       .simple(i)
       .stream().iterator();
        while(iterator.hasNext()){
            capability.add((ArrayList<Capability>) iterator.next());
        }   
        }
        ArrayList<Capability> capab = capability.get(new Random().nextInt(capability.size()));
        while(!capab.contains(c)){
         capab = capability.get(new Random().nextInt(capability.size()));   
        }
        return capab;
    }
     /**
     * generates a combination of capabilities not containing some specified capabilities
     * @param c
     * @return 
     */
     private static ArrayList<Capability> getPossibleCapabilitiesNotContainingC(Capability c){
        ArrayList<ArrayList<Capability>> capability = new ArrayList<>();
        ArrayList<Capability> allPossibleCapabilities = new ArrayList<>();
        for (Capability capa : Simulator.capabilities.values())  {
            allPossibleCapabilities.add(capa);
        } 
        
        for(int i=2;i<=allPossibleCapabilities.size();i++){
         Iterator<List<Capability>> iterator = Generator.combination(allPossibleCapabilities)
       .simple(i)
       .stream().iterator();
        while(iterator.hasNext()){
            capability.add((ArrayList<Capability>) iterator.next());
        }   
        }
        ArrayList<Capability> capab = capability.get(new Random().nextInt(capability.size()));
        while(capab.contains(c)){
         capab = capability.get(new Random().nextInt(capability.size()));   
        }
        return capab;
    }
    /**
     * inserts a new simulation into the database
     * @param con
     * @throws SQLException 
     */
    private static void addSimulationToDb(Connection con) throws SQLException{
        Statement stm = con.createStatement();
        String query = "insert into simulation (date,mode,duration) values('"+simulationDate+"',"+simulationMode+","+simulationDuration+")";
        stm.executeUpdate(query);
        query = "select id from simulation";
        ResultSet result = stm.executeQuery(query);
        result.last();
        simulationId = result.getInt("id");
    }
    /**
     * adds capabilities of a mutablething used in a specific simulation to database
     * @param con
     * @param i
     * @throws SQLException 
     */
    private static void addCapabilitiesForMutableThingInSimulationToDb(Connection con, int i) throws SQLException{
        //Statement stm = con.createStatement();
        Statement stm1 = con.createStatement();
        Statement stm2 = con.createStatement();
        /*String query1 = "select thingid from mutablething where name='"+things.get(i).getName()+"'";
        ResultSet rs = stm.executeQuery(query1);
        rs.next();
        things.get(i).setId(rs.getInt("thingid"));*/
            for(int j=0;j<things.get(i).getAllCapabilities().size();j++){
                String query2 = "select id from capability where type='"+things.get(i).getAllCapabilities().get(j).getType()+"'";
                ResultSet rs1 = stm1.executeQuery(query2);
                rs1.next();
                String query3 = "insert into simulationMutablethingCapability (simulationid,thingid,capabilityid) values ("+simulationId+","+things.get(i).getId()+","+rs1.getInt("id")+")";    
                stm2.executeUpdate(query3);
                }
    }
    /**
     * add a new mutablething to the database
     * @param con
     * @param thing
     * @throws SQLException 
     */
    private static int addMutableThingToDb(Connection con, MutableThing thing) throws SQLException{
        Statement stm = con.createStatement();
        String query = "insert into mutablething (name,trustlevel,cooperationdegree,zoneid,impact) values ('"+thing.getName()+"',"+thing.getTrustLevel()+","+thing.getCooperationDegree()+","+thing.getZone().getId()+","+thing.getImpact()+")";
        stm.executeUpdate(query,Statement.RETURN_GENERATED_KEYS);
        ResultSet rs =stm.getGeneratedKeys();
        rs.next();
        thing.setId(rs.getInt(1));
        return rs.getInt(1);
    }
    /**
     * loads the simulation configuration from the database
     * @param con
     * @throws SQLException 
     */
    private static void getSimulationFromDb(Connection con,int id) throws SQLException{
        Statement stm = con.createStatement();
        String query = "select * from simulation where Id="+id+"";
        ResultSet rs = stm.executeQuery(query);
        if(rs.next()){
            scenarioId = rs.getInt("scenarioId");
            simulationDuration = rs.getInt("duration");
            simulationMode = rs.getInt("mode");
            
        }
    }
    /**
     * creates mutablethings from the database acoording to a scenario in the database
     * @param con
     * @throws SQLException 
     */
    private static ArrayList<MutableThing> createThingFromDb(Connection con, int id) throws SQLException{
        Statement stm = con.createStatement();
        Statement stm1 = con.createStatement();
        Statement stm2 = con.createStatement();
        Statement stm3 = con.createStatement();
        ArrayList<MutableThing> createdThings = new ArrayList<>();
        
        String query = "select distinct(thingId) from simulationMutablethingCapability where simulationid="+id+"";
        ResultSet rs = stm.executeQuery(query);
        int i=0;
        while(rs.next()){
            int zoneId=0,cooperationDegree=0;
            ArrayList<Capability> capabilities = new ArrayList<>();
            int thingId = rs.getInt("thingId");
            String query1 = "select * from mutablething where thingId="+thingId+"";
            ResultSet rs1 = stm1.executeQuery(query1);
            if(rs1.next()){
                zoneId = rs1.getInt("zoneId");
                cooperationDegree = rs1.getInt("cooperationDegree");
            }
            String query2 = "select capabilityId from simulationmutablethingcapability where simulationId="+id+" and thingId="+thingId+"";
            ResultSet rs2 =stm2.executeQuery(query2);
            while(rs2.next()){
                String query3 = "select name,type,resourceconsumption from capability where id="+rs2.getInt("capabilityId")+"";
                ResultSet rs3 = stm3.executeQuery(query3);
                if(rs3.next()){
                    capabilities.add(new Capability(rs3.getString("name"),rs3.getString("type"),rs3.getInt("resourceconsumption")));
                }
            }
            MutableThing thing = new MutableThing(i,"thing "+i+""+simulationId,zones.get(zoneId),cooperationDegree,capabilities);
            createdThings.add(thing);
            ArrayList<Constraint> entry= zonesStates.get(zoneId);
            for(int j=0;j<entry.size();j++){
                if(thing.getActualCapabilities().contains(Simulator.capabilities.get(entry.get(j).capability))){
                    entry.get(j).number++;
                }
            }
            if(SaveSimulation>0){
                int newThingId = Simulator.addMutableThingToDb(con,thing);
                //associatedThing.put(newThingId, thingId);
            }
            
            i=i+1;
        }
        return createdThings;
    }
    /**
     * inialises the zone's common resources and initialises the zones constraints
     * also initialises the capabilities that are going to participate in the simulation
     */
    private static void InitializeZonesandCapabilities(){
        for(int i=1;i<=1;i++){
            //on initialise le nombre de resources communes pour la zone
            zones.put(i, new Zone(i,200));
            zonesWaitingQueues.put(i, new LinkedList<>());
            zonesRules.put(i, new ArrayList<>());
            zonesStates.put(i, new ArrayList<>());
        }
        capabilities.put("c1",new Capability("Sensing","c1",new Random().nextInt(10)+1));
        capabilities.put("c2",new Capability("Sensing","c2",new Random().nextInt(10)+1));
        capabilities.put("c3",new Capability("Sensing","c3",new Random().nextInt(10)+1));
        capabilities.put("c4",new Capability("Actuating","c4",new Random().nextInt(10)+1));
        capabilities.put("c5",new Capability("Actuating","c5",new Random().nextInt(10)+1));
        capabilities.put("c6",new Capability("Actuating","c6",new Random().nextInt(10)+1));
        capabilities.put("c7",new Capability("Communicating","c7",new Random().nextInt(10)+1));
        capabilities.put("c7",new Capability("Communicating","c8",new Random().nextInt(10)+1));
        capabilities.put("c8",new Capability("Communicating","c9",new Random().nextInt(10)+1));

    }
    /**
     * inserts for each simulation instant t the for each zone with constraints its workingPercentage
     * @param con
     * @throws SQLException 
     */
    private static void insertZonesWorkingPercentage(Connection con) throws SQLException{
        
         for (Map.Entry<Integer,ArrayList<Constraint>> entry : zonesStates.entrySet()){
             double actualWorkingPercentage=0;
             int zone = entry.getKey();
               ArrayList<Constraint> supposedState = Simulator.zonesRules.get(zone);
                int supposedNumber = 0;
                for(int i=0;i<supposedState.size();i++){
                    supposedNumber += supposedState.get(i).number;
                }
            for(int i=0;i<entry.getValue().size();i++){
                
                actualWorkingPercentage += entry.getValue().get(i).number;
            }
            actualWorkingPercentage = round((actualWorkingPercentage/supposedNumber)*100,0);
            if(actualWorkingPercentage > 100){
            actualWorkingPercentage=100;
            }
            Statement stm = con.createStatement();
            String query = "insert into zoneworkingpercentage (zoneid,workingpercentage,simulationtime,simulationid) values("+zone+","+actualWorkingPercentage+","+Simulator.cycleCounter+","+Simulator.simulationId+")";
            stm.executeUpdate(query);
        }
    }
/**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ParseException {
        // TODO code application logic here
        /*Date d1=new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2019");
        Date d2 =new SimpleDateFormat("dd/MM/yyyy").parse("01/07/2019");
        deviationSeed = (d2.getTime()-d1.getTime())/1000;
        d1=new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2019");
        d2 =new SimpleDateFormat("dd/MM/yyyy").parse("01/06/2019");
        askForMutationSeed = (d2.getTime()-d1.getTime())/1000;
        d1=new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2019");
        d2 =new SimpleDateFormat("dd/MM/yyyy").parse("01/05/2019");
        actualCapabilitiesSeed = (d2.getTime()-d1.getTime())/1000;*/
        
        InitializeZonesandCapabilities();
        Document ConfigurationDoc;
       
        File ConfigFile = new File("configuration");
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder;
        try {
            
            dBuilder = dbFactory.newDocumentBuilder();
            ConfigurationDoc = dBuilder.parse(ConfigFile);
            ConfigurationDoc.getDocumentElement().normalize();
        simulationDuration = Integer.parseInt(ConfigurationDoc.getElementsByTagName("duration").item(0).getTextContent());
        simulationMode = Integer.parseInt(ConfigurationDoc.getElementsByTagName("mode").item(0).getTextContent());
        numberOfThings = Integer.parseInt(ConfigurationDoc.getElementsByTagName("numberOfThings").item(0).getTextContent());
        SaveSimulation = Integer.parseInt(ConfigurationDoc.getElementsByTagName("saveSimulation").item(0).getTextContent());
        simulationId = Integer.parseInt(ConfigurationDoc.getElementsByTagName("simulationId").item(0).getTextContent());
        resourceConsumptionThreshold=Integer.parseInt(ConfigurationDoc.getElementsByTagName("resourceConsumptionThreshold").item(0).getTextContent());
        resourceWeight=Double.parseDouble(ConfigurationDoc.getElementsByTagName("resourceWeight").item(0).getTextContent());
        minimalTrustLevel=Double.parseDouble(ConfigurationDoc.getElementsByTagName("minimalTrustLevel").item(0).getTextContent());
        mutationResultWeight=Double.parseDouble(ConfigurationDoc.getElementsByTagName("mutationResultWeight").item(0).getTextContent());
        numberOfTrustableThings=Integer.parseInt(ConfigurationDoc.getElementsByTagName("percentageOfTrustableThings").item(0).getTextContent());
        numberOfTrustableThings=numberOfTrustableThings*numberOfThings/100;
        Node constraintNode = ConfigurationDoc.getElementsByTagName("constraint").item(0);
        Element constraint = (Element) constraintNode;
        NodeList zoneList= constraint.getElementsByTagName("zone");
        for(int i=0;i<zoneList.getLength();i++){
            Node zoneNode = zoneList.item(i);
            Element zone = (Element) zoneNode;
            int zoneId = Integer.parseInt(zone.getElementsByTagName("zoneid").item(0).getTextContent());
            NodeList zoneRules = zone.getElementsByTagName("rule");
             
            for(int j=0;j<zoneRules.getLength();j++){
                Node ruleNode = zoneRules.item(j);
                Element rule = (Element) ruleNode;
                String capability = rule.getElementsByTagName("capability").item(0).getTextContent();
                int actualNumber = Integer.parseInt(rule.getElementsByTagName("actualNumber").item(0).getTextContent());
                int minimalNumber = Integer.parseInt(rule.getElementsByTagName("minimalNumber").item(0).getTextContent());
                zonesRules.get(zoneId).add(new Constraint (minimalNumber,capability,zoneId));
                Simulator.totalConstraintNumber+=actualNumber;
                if(simulationId==0){
                zonesStates.get(zoneId).add(new Constraint (actualNumber,capability,zoneId));
                }
                else{
                zonesStates.get(zoneId).add(new Constraint (0,capability,zoneId));    
                }
            }
        }
        
            System.out.println(simulationDuration +" "+simulationMode+" "+numberOfThings);
  
        } catch (Exception ex) {
            System.out.println("Configuration file could not be parsed");
            Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Simulator sim =new Simulator();
         //for(int n=0;n<5;n++){
        myWriter = new FileWriter("simulation.txt");
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        simulationDate = format.format(new Date());
        //lastSimulationId=simulationId;
        int id=simulationId;
        try {
           
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection("jdbc:mysql://localhost/simulation?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC", "root", "root");
            if(SaveSimulation>0){
                Simulator.addSimulationToDb(con);
            }
            if(id == 0){
            
            things = sim.createThing(con);
            for(int i=0;i<things.size();i++){
                myWriter.write(things.get(i).toString());
                if(SaveSimulation>0){
                   Simulator.addCapabilitiesForMutableThingInSimulationToDb(con, i);
                }
                Simulator.deviant.put(things.get(i).getId(), 0);
                Simulator.nonDeviant.put(things.get(i).getId(), 0);
                
            }
            }
            else{
                Simulator.getSimulationFromDb(con,id);
                things = Simulator.createThingFromDb(con,id);
                
                for(int i=0;i<things.size();i++){
                myWriter.write(things.get(i).toString());
                if(SaveSimulation>0){
                   Simulator.addCapabilitiesForMutableThingInSimulationToDb(con, i);
                }
                Simulator.deviant.put(things.get(i).getId(), 0);
                Simulator.nonDeviant.put(things.get(i).getId(), 0);
                
            }
            }
            myWriter.write("things created successfully\n");
            authority = sim.createAuthority(con);
            myWriter.write("authority created successfully\n");
            con.close();
            
            myWriter.write("start simulation\n\n");
            while(cycleCounter <= simulationDuration){
            if(SaveSimulation>0){
            con= DriverManager.getConnection("jdbc:mysql://localhost/simulation?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC", "root", "root");
            Simulator.insertZonesWorkingPercentage(con);
            con.close();
            }
            myWriter.write("\n*** cycleCounter: "+cycleCounter+" ***\n");
            sim.checkForMutationRequests();
            // on calcule les nouveaux niveau de trust chaque intervalle de 10 cycles
            if(cycleCounter % 10 == 0)
                authority.calculateTrustLevel();

            cycleCounter++;
            }
            for(int i=0;i<things.size();i++){
                myWriter.write(things.get(i).toString());
            }
            } 
        
        catch (ClassNotFoundException | SQLException ex ) {
            Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        myWriter.close();
    
    }
}
