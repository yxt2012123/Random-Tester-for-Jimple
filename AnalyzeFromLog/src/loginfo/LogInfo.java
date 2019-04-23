package loginfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogInfo {
	
	public ArrayList<IfInfo> info;
	public LogInfo() {
		info=new ArrayList<>();
	}
	
	private static ArrayList<String> readIntoLines(String classfilepath) {
		String temp = "";

		try {
			Scanner s = new Scanner(new File(classfilepath));
			while (s.hasNextLine()) {
				temp += s.nextLine() + "\n";
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		temp = temp.replaceAll("\n\n", "\n");

		ArrayList<String> result = new ArrayList<String>(Arrays.asList(temp.trim().split("\n")));

		return result;
	}
	
	private static Map<String,Integer> findIfNumAndLine(String s) {
		Map<String,Integer> res=new HashMap<>();
		Pattern p=Pattern.compile("line(\\d+); ifno\\.(\\d+): if.*;");
		Matcher m=p.matcher(s);
		int n=-1;
		int l=-1;
		while (m.find()) {
			n=Integer.parseInt(m.group(2));
			l=Integer.parseInt(m.group(1));
		}
		res.put("line", l);
		res.put("ifnum", n);
		return res;
	}
	
	public void output(String dest) {
	    try{
	        File file =new File(dest);

	        if(!file.exists()){
	         file.createNewFile();
	        }

	        //rewrite the file, do not append
	        FileWriter fileWriter = new FileWriter(dest);
			for (int i=0;i<info.size();i++) {
				fileWriter.write(info.get(i).ifnum+"\t");
				fileWriter.write(info.get(i).line+"\t");
				
				fileWriter.write(info.get(i).info+"\t");
				fileWriter.write(info.get(i).type+"\t");
				fileWriter.write(info.get(i).op+"\t");
				fileWriter.write(info.get(i).covered);
				if (info.get(i).covered.equals("True")||info.get(i).covered.equals("False")) {
					fileWriter.write("\t");
					switch (info.get(i).type) {
					case "int":
					case "char":
					case "short":
					case "byte":
					case "java.lang.String":
					case "java.lang.Object":
					case "boolean":fileWriter.write(info.get(i).minDist.intValue()+"\t");break;
					case "double":
					case "float":fileWriter.write(info.get(i).minDist.doubleValue()+"\t");break;
					case "long":fileWriter.write(info.get(i).minDist.longValue()+"\t");break;
					default:
						break;
					}
					fileWriter.write(info.get(i).left+"\t"+info.get(i).right);
				}
				fileWriter.write("\n");
			}

	        fileWriter.close();

	        System.out.println("Done");

	    }
	    catch(IOException e){
	         e.printStackTrace();
	    }
	
	}
	
	public void input(String src) {
		try {
			Scanner s = new Scanner(new File(src));
			String[] strlist;
			while (s.hasNextLine()) {
				strlist=s.nextLine().split("\t");
				int num=Integer.parseInt(strlist[0]);
				int ln=Integer.parseInt(strlist[1]);
				String _info=strlist[2];
				String _type=strlist[3];
				String _op=strlist[4];
				
				IfInfo ifinfo= new IfInfo(num,ln,_info,_type,_op);
				ifinfo.covered=strlist[5];
				//Only int considered
				
				if (strlist.length>6) {
					ifinfo.minDist=Integer.parseInt(strlist[6]);
					ifinfo.left=strlist[7];
					ifinfo.right=strlist[8];
				}
				else {
					ifinfo.minDist=0;
					ifinfo.left="0";
					ifinfo.right="0";
				}
				
				info.add(ifinfo);

			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean numInIfInfo(int num) {
		for (int i=0;i<info.size();i++) {
			if (info.get(i).ifnum==num) {
				return true;
			}
		}
		return false;
	}

	public void readFromLog(String src) {
		ArrayList<String> StrList=readIntoLines(src);
		for (int i=0;i<StrList.size();i++) {
			Map<String,Integer> map=findIfNumAndLine(StrList.get(i));
			int num=map.get("ifnum");
			int ln=map.get("line");
			
			if (num>0) {
				IfInfo ifinfo= new IfInfo(StrList.subList(i, i+5),num,ln);
				if (!numInIfInfo(num)) {
					info.add(ifinfo);
				}
				for (int j=0;j<info.size();j++) {
					if (info.get(j).ifnum==num) {
						info.get(j).addLog(StrList.subList(i, i+5));
					}
				}
			}
		}
	}
	
	public void printInfo() {
		for (int i=0;i<info.size();i++) {
			System.out.println(info.get(i).ifnum);
			System.out.println(info.get(i).line);
			System.out.println(info.get(i).info);
			System.out.println(info.get(i).type);
			System.out.println(info.get(i).op);
			System.out.println(info.get(i).covered);
			if (info.get(i).covered.equals("True")||info.get(i).covered.equals("False")) {
				switch (info.get(i).type) {
				case "int":
				case "char":
				case "short":
				case "byte":
				case "java.lang.String":
				case "java.lang.Object":
				case "boolean":System.out.println(info.get(i).minDist.intValue());break;
				case "double":
				case "float":System.out.println(info.get(i).minDist.doubleValue());break;
				case "long":System.out.println(info.get(i).minDist.longValue());break;
				default:
					break;
				}
				System.out.println(info.get(i).left);
				System.out.println(info.get(i).right);
			}
		}
	}
	

	

}
