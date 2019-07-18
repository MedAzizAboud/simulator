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
public class Constraint {
    public int number;
    public String capability;
    public int zoneId;
    
    public Constraint(int number, String capability, int zoneId){
        this.capability=capability;
        this.number=number;
        this.zoneId=zoneId;
    }
    
}
