package loginfo;

import java.util.*;

public class IfInfo {
	public String info;
	public int ifnum;
	public int line;
	public String type;

	public String op;
	public String left;
	public String right;

	public Number minDist;
	public String covered;
	
	public IfInfo(int num, int ln, String _info, String _type, String _op)
	{
		info=_info;
		ifnum=num;
		line=ln;
		type=_type;
		op=_op;

		minDist=-1;
		
		covered="Neither";
	}
	
	public IfInfo(List<String> lst,int num,int ln)
	{
		info=lst.get(0);
		ifnum=num;
		line=ln;
		type=lst.get(1);
		op=lst.get(3);

		minDist=-1;
		
		covered="Neither";
	}
	
	public String val2str(boolean v) {
		if (v) return "True";
		else return "False";
	}
	
	public void addLog(List<String> lst)
	{
		if (covered.equals("Both")) return;
		
		String lt=lst.get(2);
		String rt=lst.get(4);
		Predicate p=new Predicate(lt,rt,type,op);
		boolean value=false;
		Number dist=-1;
		
		
		switch (type) {
		
		case "char":
		case "short":
		case "byte":
		case "boolean": dist=p.checkIntBorder(); value=p.checkIntValue(); 

		if (covered.equals(val2str(value)) && minDist.intValue()>dist.intValue()) {
			minDist=dist; left=lt; right=rt;
		}
		break;
		
		case "double":
		case "float": dist=p.checkDoubleBorder(); value=p.checkDoubleValue();

		if (covered.equals(val2str(value)) && minDist.doubleValue()>dist.doubleValue()) {
			minDist=dist; left=lt; right=rt;
		}
		break;
		
		case "int":
		case "long": dist=p.checkLongBorder(); value=p.checkLongValue();

		if (covered.equals(val2str(value)) && minDist.longValue()>dist.longValue()) {
			minDist=dist; left=lt; right=rt;
		}
		break;
		
		case "java.lang.String": dist=p.checkStringBorder(); value=p.checkObjectValue();

		if (covered.equals(val2str(value)) && minDist.intValue()>dist.intValue()) {
			minDist=dist; left=lt; right=rt;
		}
		break;
		
		case "java.lang.Object": value=p.checkObjectValue(); dist=0;
		if (!value) dist=1;

		if (covered.equals(val2str(value)) && minDist.intValue()>dist.intValue()) {
			minDist=dist; left=lt; right=rt;
		}
		break;
		
		default:
			assert(false);
			break;

		}
		if (covered.equals("Neither")) {
			covered=val2str(value); minDist=dist; left=lt; right=rt;
		}
		else if (!covered.equals(val2str(value))) {
			covered="Both"; minDist=0;
		}
	}

}
