package util;
import java.util.*;

public class JTypeVar {
	String type;
	String var;
	
	public JTypeVar(String type, String var) {
		this.type=type;
		this.var=var;
	}
	
	public String toString() {
		return "{"+type+" "+var+"}";
	}
	
	public boolean equals(JTypeVar element) {
		if(type.equals(element.type) && var.equals(element.var)) {
			return true;
		}
		return false;
	}
	
	public boolean isInArray(ArrayList<JTypeVar> list) {
		boolean result = false;
		
		for(int i=0; i<list.size(); i++) {
			JTypeVar element = list.get(i);
			if(equals(element)) {
				return true;
			}
		}
		return result;
	}
}
