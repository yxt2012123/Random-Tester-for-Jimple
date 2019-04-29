package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;

public class Implant{
	public static void main (String args[])
	{
		String src="F:\\mnt\\soot-trunk-withscripts\\test\\Integers.jimple";
		String dest="F:\\mnt\\soot-trunk-withscripts\\test\\Integers.jimple";
		ArrayList<Integer> target = new ArrayList<>();
		
		if (args.length>0)
			src=args[0];
		if (args.length>1)
			dest=args[1];
		
		ArrayList<String> jimplestmts = JClassDumpMemInfo.readIntoLines(src);
		//System.out.println(JClassDumpMemInfo.getClassname(jimplestmts));
		//System.out.println(jimplestmts.toString());

		String func=null;
		String left=null;
		String right=null;
		String op=null;
		//String[] cp={"1","2","3","4","5","6","7","8","9","0","\'","\""};
		//ArrayList<String> constprefix= new ArrayList<>(Arrays.asList(cp)); 
		String[] notobj={"int","byte","long","float","double","boolean","char","short"};
		ArrayList<String> notObject= new ArrayList<>(Arrays.asList(notobj)); 
		Pattern p=Pattern.compile("if (.*?) (>=|<=|!=|==|>|<) (.*?) goto (.*?);");
		Pattern p_assertfalse=Pattern.compile("specialinvoke (.*?)\\.<java\\.lang\\.AssertionError\\:(.*?);");
		int ifno = 0;
		for (int i = 0; i < jimplestmts.size(); i++) {
			//System.out.println(jimplestmts.toString());
			String tmp=jimplestmts.get(i);
			if (JClassDumpMemInfo.isMethodsig(tmp)) {
				//System.out.println();
				//System.out.println(JClassDumpMemInfo.getPreciseMethodsig(jimplestmts.get(i)));

				func=tmp.trim();
				System.out.println(func);
			}
			
			Matcher m_assertfalse=p_assertfalse.matcher(tmp);
			
			while (m_assertfalse.find()) {
				target.add(i+1);
				
				if (tmp.contains("_DTJVM_Print")) {
					System.out.println("assertion error in line"+(i+1)+" is already implanted.");
					break;
				}
				String DTJVM_print="staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>"
						+ "(\"line"+(i+1)+": reached an assertion error\");";
				System.out.println(DTJVM_print);
				
				jimplestmts.set(i, DTJVM_print+tmp);
			}
			
			Matcher m=p.matcher(tmp);
			
			while (m.find()) {
				ifno++;
				System.out.println(m.group(0));
				left=m.group(1);
				op=m.group(2);
				right=m.group(3);
				System.out.println(left+op+right);
				if (tmp.contains("_DTJVM_Print")) {
					System.out.println("ifno."+ifno+" is already implanted.");
					break;
				}
				//give a function, store all visible variable in it
				ArrayList<JTypeVar> allvisible = JClassDumpMemInfo.getVisibleVars(jimplestmts,
						func);
				String _type=null;

				//suppose there are not cases like "if 2 < 3"
				for (int j = 0; j < allvisible.size(); j++) {
					if (allvisible.get(j).var.trim().equals(left))
					{
						System.out.println(allvisible.get(j).type);
						_type=allvisible.get(j).type;
					}
					if (allvisible.get(j).var.trim().equals(right))
					{
						System.out.println(allvisible.get(j).type);
						_type=allvisible.get(j).type;
					}
				}
				//if _type is not in the list, then it's object
				boolean isobj=true;
				for (int j = 0; j < notObject.size(); j++) {
					if (_type.equals(notObject.get(j))) {
						isobj=false;
					}
				}
				if (isobj)
				{
					_type="java.lang.Object";
				}
				
				String DTJVM_print=null;
				if (!right.equals("null") && !left.equals("null")) {
					DTJVM_print="staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>"
							+ "(\"line"+(i+1)+"; ifno."+ifno+": "+m.group(0)+ "\");"
							+ "staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>"+ "(\""+_type+"\");"
							+ "staticinvoke <_DTJVM_Print: boolean print("+_type+")>"+ "("+m.group(1)+");"
							+ "staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>(\""+ m.group(2)+"\");"
							+ "staticinvoke <_DTJVM_Print: boolean print("+_type+")>"+ "("+m.group(3)+");"
							+ "staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>(\"\\n\");";
				}
				else {
					if (right.equals("null"))
						DTJVM_print="staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>"
							+ "(\"line"+(i+1)+"; ifno."+ifno+": "+m.group(0)+ "\");"
							+ "staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>"+ "(\""+_type+"\");"
							+ "staticinvoke <_DTJVM_Print: boolean print("+_type+")>"+ "("+m.group(1)+");"
							+ "staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>(\""+ m.group(2)+"\");"
							+ "staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>"+ "(\""+m.group(3)+"\");"
							+ "staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>(\"\\n\");";
					else
						DTJVM_print="staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>"
							+ "(\"line"+(i+1)+"; ifno."+ifno+": "+m.group(0)+ "\");"
							+ "staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>"+ "(\""+_type+"\");"
							+ "staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>"+ "(\""+m.group(1)+"\");"
							+ "staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>(\""+ m.group(2)+"\");"
							+ "staticinvoke <_DTJVM_Print: boolean print("+_type+")>"+ "("+m.group(3)+");"
							+ "staticinvoke <_DTJVM_Print: boolean print(java.lang.String)>(\"\\n\");";
				}
				
				System.out.println(DTJVM_print);
				
				jimplestmts.set(i, DTJVM_print+tmp);
			}
			
		}
		
	    try{
	        File file =new File(dest);

	        if(!file.exists()){
	         file.createNewFile();
	        }

	        //rewrite the file, do not append
	        FileWriter fileWritter = new FileWriter(dest);
	        for (int i = 0; i < jimplestmts.size(); i++) {
	        	fileWritter.write(jimplestmts.get(i));
	        	fileWritter.write("\n");
	        }
	        fileWritter.close();

	        System.out.println("Done");
	        if (!target.isEmpty()) {
	        	System.out.println("Target lines include:");
	        	for (int i=0;i<target.size();i++) {
	        		System.out.println(target.get(i));
	        	}
	        }

	    }
	    catch(IOException e){
	         e.printStackTrace();
	    }
	    
	}
	
}