package loginfo;

import java.util.*;

public class Predicate {
	public String left;
	public String right;
	public String type;
	public String op;
	public Predicate(String l,String r,String t,String _op){
		left=l;
		right=r;
		type=t;
		op=_op;
	}
	public int checkIntBorder()
	{
		/*String[] Int={"int","byte","boolean","char","short"};
		ArrayList<String> IntType= new ArrayList<>(Arrays.asList(Int));
		boolean isint=false;
		for (int j = 0; j < IntType.size(); j++) {
			if (type.equals(IntType.get(j))) {
				isint=true;
			}
		}
		if (!isint) {
		}*/
		int intl;
		int intr;
		if (type.equals("boolean")) {
			if (left.equals("true")) intl=1; else intl=0;
			if (right.equals("true")) intr=1; else intr=0;
		}
		else {
			intl=Integer.parseInt(left);
			intr=Integer.parseInt(right);
		}

		int dist=intl-intr;
		if (dist<0) dist=-dist;
		return dist;
	}
	
	public double checkDoubleBorder()
	{
		/*String[] Double={"double","float"};
		ArrayList<String> DoubleType= new ArrayList<>(Arrays.asList(Double));
		boolean isdouble=false;
		for (int j = 0; j < DoubleType.size(); j++) {
			if (type.equals(DoubleType.get(j))) {
				isdouble=true;
			}
		}
		if (!isdouble) {
		}*/
		double dobl=Double.parseDouble(left);
		double dobr=Double.parseDouble(right);
		double dist=dobl-dobr;
		if (dist<0) dist=-dist;
		return dist;
	}
	
	public long checkLongBorder()
	{
		/*
		if (!type.equals("long")) {
		}*/
		long longl=Long.parseLong(left);
		long longr=Long.parseLong(right);
		long dist=longl-longr;
		if (dist<0) dist=-dist;
		return dist;
	}
	
	public int checkStringBorder()
	{
		int maxLen=right.length();
		if (left.length()>right.length()) {
			maxLen=left.length();
		}
		
		int dist=0;
		for (int i=0;i<maxLen;i++) {
			char c_l=0,c_r=0;
			if (i<left.length()) c_l=left.charAt(i);
			if (i<right.length()) c_r=right.charAt(i);
			if(c_l-c_r>0) {
				dist=(maxLen-i-1)*128+c_l-c_r;
				break;
			}
			else if(c_l-c_r<0) {
				dist=(i-maxLen+1)*128+c_l-c_r;
				break;
			}
		}
		
		return dist;
	}
	
	public boolean checkIntValue() {
		int intl;
		int intr;
		if (type.equals("boolean")) {
			if (left.equals("true")) intl=1; else intl=0;
			if (right.equals("true")) intr=1; else intr=0;
		}
		else {
			intl=Integer.parseInt(left);
			intr=Integer.parseInt(right);
		}
		boolean res=false;
		switch (op) {
		case "<=": res=intl<=intr;break;
		case ">=": res=intl>=intr;break;
		case "==": res=intl==intr;break;
		case "!=": res=intl!=intr;break;
		case "<": res=intl<intr;break;
		case ">": res=intl>intr;break;
		}
		return res;
	}
	
	public boolean checkDoubleValue() {
		Double dobl=Double.parseDouble(left);
		Double dobr=Double.parseDouble(right);
		boolean res=false;
		switch (op) {
		case "<=": res=dobl<=dobr;break;
		case ">=": res=dobl>=dobr;break;
		case "==": res=dobl==dobr;break;
		case "!=": res=dobl!=dobr;break;
		case "<": res=dobl<dobr;break;
		case ">": res=dobl>dobr;break;
		}
		return res;
	}
	
	public boolean checkLongValue() {
		Long longl=Long.parseLong(left);
		Long longr=Long.parseLong(right);
		boolean res=false;
		switch (op) {
		case "<=": res=longl<=longr;break;
		case ">=": res=longl>=longr;break;
		case "==": res=longl==longr;break;
		case "!=": res=longl!=longr;break;
		case "<": res=longl<longr;break;
		case ">": res=longl>longr;break;
		}
		return res;
	}
	
	public boolean checkObjectValue() {
		if (op.equals("==")) return left.equals(right);
		return !left.equals(right);
	}
}
