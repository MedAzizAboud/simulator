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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.paukov.combinatorics3.Generator;
/**
 *
 * @author aziza
 */
public class Simulator {

    public static int numberOfThings;
    public static int simulationDuration;
    public static int simulationMode;
    public static int cycleCounter =1;
    public static ArrayList<MutableThing> things;
    public static Authority authority;
    public static FileWriter myWriter;
    public static Map<Integer,Integer> deviant = new HashMap<>();
    public static Map<Integer,Integer> nonDeviant = new HashMap<>();
    /**
     * creates MutableThings used in the simulation from table MutableThing
     * @return list of mutablethings
     */
    private ArrayList<MutableThing> createThing(Connection con) throws SQLException, IOException{
        /*ArrayList<MutableThing> things = new ArrayList<>();
        String query = "select * from MutableThing";
        ArrayList<Capability> allCapabilities=new ArrayList<>(), actualCapabilities=new ArrayList<>();
        Statement stm = con.createStatement();
        Statement stm1 = con.createStatement();
        Statement stm2 = con.createStatement();
        ResultSet rs = stm.executeQuery(query);
        while(rs.next()){
            String query1 = "select * from Actualcapabilities where thingId ="+rs.getInt("id")+"";
            String query2 = "select * from AllCapabilities where thingId ="+rs.getInt("id")+"";
            ResultSet rs1 = stm1.executeQuery(query1);
            ResultSet rs2 = stm2.executeQuery(query2);
            while(rs1.next()){
                actualCapabilities.add(new Capability(rs1.getString("name"),rs1.getString("type")));
            }
            //things.add(new MutableThing(rs.getInt(0),rs.getInt(0),rs.getInt(0),rs.getInt(0)));
        }
        return things;*/
        ArrayList<MutableThing> createdThings = new ArrayList<>();
        ArrayList<Integer> cooperationDegrees = new ArrayList<>();
        cooperationDegrees.add(50);cooperationDegrees.add(60);cooperationDegrees.add(70);
        cooperationDegrees.add(80);cooperationDegrees.add(90);cooperationDegrees.add(100);
        for(int i=0;i<numberOfThings;i++){
            
            createdThings.add(new MutableThing(i,"thing "+i,i,cooperationDegrees.get(new Random().nextInt(cooperationDegrees.size())),Simulator.getPossibleCapabilities().get(new Random().nextInt(Simulator.getPossibleCapabilities().size()))));
        }
        return createdThings;
    }
    /**
     * create the Authority used in the simulation
     * @return authority
     */
    private Authority createAuthority(Connection con)throws SQLException{
        /*String query = "select * from Authority";
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(query);
        while(rs.next()){
            return new Authority(rs.getInt("id"),rs.getString("name"));
        }
        return null;*/
        return new Authority(0, "authority");
    }
    /**
     * checks if there are MutableThings who want to mutate every simulation cycle
     * calls MutableThing's method askForMutation
     */
    private void checkForMutationRequests() throws IOException{
        boolean result;
        for(int i=0;i<things.size();i++){
            MutableThing thing = Simulator.things.get(i);
            result = thing.askForMutation();
            myWriter.write("\n\n");
        }
        for (Map.Entry<MutableThing,ArrayList<Capability>> entry : Authority.mutationRequests.entrySet()){
             //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue()); 
             boolean authorityResponse = authority.handleMutationRequest(entry.getKey(), entry.getValue());
             entry.getKey().executeMutation(authorityResponse, entry.getValue());
        }  
        Authority.mutationRequests.clear();
    }
    private static ArrayList<ArrayList<Capability>> getPossibleCapabilities(){
        ArrayList<ArrayList<Capability>> capabilities = new ArrayList<>();
        ArrayList<Capability> allPossibleCapabilities = new ArrayList<>();
        allPossibleCapabilities.add(new Capability("Sensing","c1"));
        allPossibleCapabilities.add(new Capability("Sensing","c2"));
        allPossibleCapabilities.add(new Capability("Sensing","c3"));
        allPossibleCapabilities.add(new Capability("Actuating","c4"));
        allPossibleCapabilities.add(new Capability("Actuating","c5"));
        allPossibleCapabilities.add(new Capability("Actuating","c6"));
        allPossibleCapabilities.add(new Capability("Communicating","c7"));
        allPossibleCapabilities.add(new Capability("Communicating","c8"));
        allPossibleCapabilities.add(new Capability("Communicating","c9"));
        for(int i=2;i<=allPossibleCapabilities.size();i++){
         Iterator<List<Capability>> iterator = Generator.combination(allPossibleCapabilities)
       .simple(i)
       .stream().iterator();
        while(iterator.hasNext()){
            capabilities.add((ArrayList<Capability>) iterator.next());
        }   
        }
        return capabilities;
    }
/**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        
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
            System.out.println(simulationDuration +" "+simulationMode+" "+numberOfThings);
  
        } catch (Exception ex) {
            System.out.println("Configuration file could not be parsed");
        }
        Simulator sim =new Simulator();
        myWriter = new FileWriter("simulation.txt");
        try {
           
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection("jdbc:mysql://localhost/simulation?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC", "root", "root");
            
            things = sim.createThing(con);
            for(int i=0;i<things.size();i++){
                myWriter.write(things.get(i).toString());
            }
            myWriter.write("things created successfully\n");
            authority = sim.createAuthority(con);
            myWriter.write("authority created successfully\n");
            con.close();
            myWriter.write("start simulation\n\n");
            while(cycleCounter <= simulationDuration){
            myWriter.write("\n*** cycleCounter: "+cycleCounter+" ***\n");
            sim.checkForMutationRequests();
            if(cycleCounter % 10 == 0)
                authority.calculateTrustLevel();
            cycleCounter++;
            
            for(int i=0;i<things.size();i++){
                myWriter.write("thing"+things.get(i).getId()+" numberOfDeviantLogs: "+deviant.get(things.get(i).getId())+" numberOfNonDeviantLogs: "+nonDeviant.get(things.get(i).getId())+"\n");
            }
            
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
