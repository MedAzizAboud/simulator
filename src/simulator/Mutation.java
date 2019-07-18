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
public class Mutation {
    public MutableThing thing;
    public ArrayList<Capability> capabilities;
    public int mutationDelay;
    public int mutationDuration;
    
    public Mutation(MutableThing thing, ArrayList<Capability> capabilities, int mutationDelay, int mutationDuration){
        this.thing=thing;
        this.capabilities=capabilities;
        this.mutationDelay=mutationDelay;
        this.mutationDuration=mutationDuration;
    }
}
