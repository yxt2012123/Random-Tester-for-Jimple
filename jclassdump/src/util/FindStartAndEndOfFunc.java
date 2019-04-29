package util;

import java.util.ArrayList;

public class FindStartAndEndOfFunc {
	
	public static void main(String[] args) {
		String func_name="readJavaFormatString";
		String src="F:\\mnt\\soot-trunk-withscripts\\test\\Tmath.FloatingDecimal.jimple";
		ArrayList<Integer> start=new ArrayList<>();
		ArrayList<Integer> end=new ArrayList<>();
		ArrayList<String> func=new ArrayList<>();
		if (args.length>0) func_name=args[0];
		if (args.length>1) func_name=args[1];
		ArrayList<String> jimplestmts = JClassDumpMemInfo.readIntoLines(src);
		boolean OnFlag=false;
		for (int i = 0; i < jimplestmts.size(); i++) {
			String tmp=jimplestmts.get(i);
			if (OnFlag) {
				if (tmp.trim().equals("}")) {
					end.add(i+1);
					OnFlag=false;
				}
			}
			else if (JClassDumpMemInfo.isMethodsig(tmp)) {
				int left=tmp.indexOf('(');
				String part=tmp.substring(0, left);
				
				if (part.contains(func_name)) {
					start.add(i+1);
					func.add(tmp.trim());
					OnFlag=true;
				}
			}
		}
		for (int i=0;i<start.size();i++) {
			System.out.println(func.get(i));
			System.out.println(start.get(i));
			System.out.println(end.get(i));
			System.out.println();
		}
	}

}
