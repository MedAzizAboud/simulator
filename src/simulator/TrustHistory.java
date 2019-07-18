/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aziza
 */
public class TrustHistory {
    
    private int id;
    private double trustLevel;
    private int impact;
    /**
     * creates new TrustHistory object
     * @param id
     * @param trustLevel 
     */
    public TrustHistory(int id, double trustLevel, int impact){
        this.id=id;
        this.trustLevel=trustLevel;
        this.impact=impact;
    }
     /**
     * adds new line with thingId and corresponding new trustLevel in table TrustHistory
     * @param MutableThingId
     */
    public void addTrustLevelHistory(int MutableThingId){
        if(Simulator.SaveSimulation>0)
        {
        try {
            //insert new trustHistory
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection("jdbc:mysql://localhost/simulation?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC", "root", "root");
            Statement stm = con.createStatement();
            String query = "insert into TrustHistory (trustlevel,thingid,simulationid,impact,simulationtime) values('"+this.getTrustLevel()+"',"+MutableThingId+","+Simulator.simulationId+","+this.getImpact()+","+Simulator.cycleCounter+")";
            stm.executeUpdate(query);
            con.close();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TrustHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
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
    public int getImpact(){
        return impact;
    }
}
