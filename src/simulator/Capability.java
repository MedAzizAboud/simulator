/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author aziza
 */
public class Capability {
    private String name;
    private String type;
    private int resourceConsumption;
    
    
    public Capability(String name, String type, int resourceConsumption){
        this.name=name;
        this.type=type;
        this.resourceConsumption=resourceConsumption;
    }
    
    public String getName(){
        return name;
    }
    public String getType(){
        return type;
    }
    public int getResourceConsumption(){
        return resourceConsumption;
    }
    
@Override
public boolean equals (Object object) {
    boolean result = false;
    if (object == null || object.getClass() != getClass()) {
        result = false;
    } else {
        Capability capability = (Capability) object;
        if (this.name.equals(capability.getName()) && this.type.equals(capability.getType())) {
            result = true;
        }
    }
    return result;
}

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.name);
        hash = 71 * hash + Objects.hashCode(this.type);
        return hash;
    }
    
    public static Comparator<Capability> capabilityComparator = new Comparator<Capability>() {

	public int compare(Capability c1, Capability c2) {
	   String capabilityType1 = c1.getType().toUpperCase();
	   String capabilityType2 = c2.getType().toUpperCase();

	   //ascending order
	   return capabilityType1.compareTo(capabilityType2);
    }};
}
