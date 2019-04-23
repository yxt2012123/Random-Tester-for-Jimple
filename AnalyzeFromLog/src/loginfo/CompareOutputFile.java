package loginfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompareOutputFile {
	public static ArrayList<LogInfo> infoList;
	public static ArrayList<String> fileNameList;
	public static ArrayList<Double> distList;
	public static String msg;
	
	public static void init() {
		infoList=new ArrayList<>();
		fileNameList=new ArrayList<>();
		distList=new ArrayList<>();
		msg="--------------------------------\n";
	}
	
	private static void inputFromOutputFile(String src) {
		LogInfo logInfo = new LogInfo();
		logInfo.input(src);
		infoList.add(logInfo);
	}
	
	private static void inputFromLog(String src) {
		LogInfo logInfo = new LogInfo();
		logInfo.readFromLog(src);
		infoList.add(logInfo);
	}
	
	public static void inputFromFolder_OutputFile(String path) {
		infoList.clear();
		fileNameList.clear();
		File file=new File(path);
		File [] tmpList=file.listFiles();
		for (int i=0;i<tmpList.length;i++) {
			String name=tmpList[i].getName();
			Pattern p=Pattern.compile(".*\\.txt$");
			Matcher m=p.matcher(name);
			while (m.find()){
				//System.out.println(m.group(0));
				fileNameList.add(m.group(0));
				inputFromOutputFile(path+"\\"+m.group(0));
			}
		}
	}

	
	public static void inputFromFolder_Log(String path) {
		infoList.clear();
		fileNameList.clear();
		File file=new File(path);
		File [] tmpList=file.listFiles();
		for (int i=0;i<tmpList.length;i++) {
			String name=tmpList[i].getName();
			Pattern p=Pattern.compile(".*\\.log$");
			Matcher m=p.matcher(name);
			while (m.find()){
				//System.out.println(m.group(0));
				fileNameList.add(m.group(0));
				inputFromLog(path+"\\"+m.group(0));
			}
		}
	}
	
	public static void printInfoList() {
		for (int i=0;i<infoList.size();i++) {
			System.out.println(fileNameList.get(i));
			infoList.get(i).printInfo();
			System.out.println();
		}
	}
	
	private static ArrayList<Double> calculateWeight_simple (ArrayList<Integer> wholeLine, int targetLine){
		int maxLine=-1;
		int minLine=2147483647;
		ArrayList<Double> weight=new ArrayList<>();
		
		for (int i=0;i<wholeLine.size();i++) {
			if (wholeLine.get(i)<minLine) minLine=wholeLine.get(i);
			if (wholeLine.get(i)>maxLine) maxLine=wholeLine.get(i);
		}
		
		for (int i=0;i<wholeLine.size();i++) {
			int curLine=wholeLine.get(i);
			if (curLine<targetLine) {
				double curWeight=(0.8*curLine+0.2*targetLine-minLine)/(targetLine-minLine);
				weight.add(curWeight);
			}
			else if (curLine>targetLine) {
				double curWeight=(0.8*curLine+0.2*targetLine-maxLine)/(targetLine-maxLine);
				weight.add(curWeight);
			}
			else {
				weight.add(1.0);
			}
		}
		return weight;
	}
	
	private static ArrayList<Double> calculateWeight_cover(ArrayList<Integer> wholeIfnum, LogInfo wholeInfo) {
		ArrayList<Double> weight=new ArrayList<>();
		
		for (int i=0;i<wholeIfnum.size();i++) {
			int curIfnum=wholeIfnum.get(i);
			double curWeight=1.0;
			for (int j=0;j<wholeInfo.info.size();j++) {
				if (curIfnum==wholeInfo.info.get(j).ifnum) {
					if (wholeInfo.info.get(j).covered.equals("Both")) {
						curWeight=0.0;
						break;
					}
					else break;
				}
			}
			weight.add(curWeight);
		}
		return weight;
	}
	
	public static void calculateWholeDist(String method, int targetline, LogInfo whole_info) {
		
		if (infoList.size()!=2) {
			System.out.println("There are "+infoList.size()+" loginfos in the list, but 2 request.");
			assert(false);
		}
		
		ArrayList<Integer> whole_ifnum=new ArrayList<>();
		ArrayList<Integer> whole_line=new ArrayList<>();
		ArrayList<ArrayList<Double>> normalizedDist=new ArrayList<>();
		ArrayList<Double> whole_weight=new ArrayList<>();
		
		for (int i=0;i<2;i++) {
			for (int j=0; j<infoList.get(i).info.size();j++) {
				if (!whole_ifnum.contains(infoList.get(i).info.get(j).ifnum)){
					whole_ifnum.add(infoList.get(i).info.get(j).ifnum);
					whole_line.add(infoList.get(i).info.get(j).line);
					//System.out.println(infoList.get(i).info.get(j).ifnum+" added.");
				}
			}
		}

		switch (method) {
		case "simple":whole_weight=calculateWeight_simple(whole_line,targetline);break;
		case "cover":whole_weight=calculateWeight_cover(whole_ifnum,whole_info);break;
		default:
			assert(false);
		}

		
		for (int i=0;i<whole_ifnum.size();i++) {
			int curifnum=whole_ifnum.get(i);
			//for each ifnum
			ArrayList<Double> distList=new ArrayList<>();
			ArrayList<String> coveredList=new ArrayList<>();
			double maxDist=0;
			boolean haveBoth=false;
			boolean haveNeither=false;
			
			for (int j=0;j<2;j++) {
				//for each loginfo
				boolean isNeither=true;
				for (int k=0;k<infoList.get(j).info.size();k++) {
					if (infoList.get(j).info.get(k).ifnum==curifnum) {
						isNeither=false;
						double curDist=infoList.get(j).info.get(k).minDist.doubleValue();
						String curCovered=infoList.get(j).info.get(k).covered;
						distList.add(curDist);
						coveredList.add(curCovered);
						if(curDist>maxDist) maxDist=curDist;
						if(curCovered.equals("Both")) haveBoth=true; 
					}
				}
				if (isNeither) {
					haveNeither=true;
					coveredList.add("Neither");
					distList.add(1.0);
				}
			}
			
			if (haveNeither) {
				for (int j=0;j<2;j++) {
					if (coveredList.get(j).equals("Neither")) distList.set(j,1.0);
					else distList.set(j, 0.0);
				}
			}
			else if (haveBoth) {
				for (int j=0;j<2;j++) {
					if (coveredList.get(j).equals("Both")) distList.set(j, 0.0);
					else distList.set(j,1.0);
				}
			}
			else if (maxDist!=0) {
				for (int j=0;j<2;j++) {
					distList.set(j, distList.get(j)/maxDist);
				}
			}
			normalizedDist.add(distList);
		}
		
		for (int j=0;j<2;j++) {
			double wholedist=0.0;
			for (int i=0;i<whole_ifnum.size();i++) {
				wholedist+=whole_weight.get(i)*normalizedDist.get(i).get(j);
			}
			distList.add(wholedist);
		}
		
		msg+="Distance comparison result:\n";

		for (int j=0;j<2;j++) {
			System.out.println(fileNameList.get(j)+" "+distList.get(j));
			msg+=fileNameList.get(j)+": "+distList.get(j)+"\n";
		}
		
		for (int i=0;i<whole_ifnum.size();i++) {
			System.out.println(whole_ifnum.get(i)+" "+normalizedDist.get(i).get(0)+" "
					+ normalizedDist.get(i).get(1)+" "+whole_line.get(i)+" "
					+ whole_weight.get(i));
			msg+="ifnum"+whole_ifnum.get(i)+": distance: "+normalizedDist.get(i).get(0)+"; "
					+ normalizedDist.get(i).get(1)+" line: "+whole_line.get(i)+" weight: "
					+ whole_weight.get(i)+"\n";
		}
		
		
	}
	
	public static boolean hitOtherRoute(LogInfo main,LogInfo other) {
		boolean res=false;
		for (int i=0;i<main.info.size();i++) {
			for (int j=0;j<other.info.size();j++) {
				if (main.info.get(i).ifnum==other.info.get(j).ifnum) {
					if (main.info.get(i).covered.equals("Both")) {
						continue;
					}
					else {
						if (!main.info.get(i).covered.equals(other.info.get(i).covered) &&
								!other.info.get(i).covered.equals("Neither")) {
							res=true;
							break;
						}
					}
				}
			}
			if (res) break;
		}
		
		for (int j=0;j<other.info.size();j++) {
			boolean contains=false;
			for (int i=0;i<main.info.size();i++) {
				if (other.info.get(j).ifnum==main.info.get(i).ifnum) {
					contains=true;
				}
			}
			if (!contains && !other.info.get(j).covered.equals("Neither")) {
				res=true;
				break;
			}
		}
		
		msg+="Branch test completed, result is "+res+":\n";
		msg+="main:\n";
		for (int i=0;i<main.info.size();i++) {
			msg+="ifnum: "+main.info.get(i).ifnum+": "+main.info.get(i).covered+"\n";
		}
		msg+="other:\n";
		for (int i=0;i<other.info.size();i++) {
			msg+="ifnum: "+other.info.get(i).ifnum+": "+other.info.get(i).covered+"\n";
		}
		
		return res;
	}
	
	private static void combineInfo(IfInfo main,IfInfo next) {
		if (main.covered.equals("Both")) return;
		if (!main.covered.equals(next.covered)) {main.covered="Both"; return;}
		//only consider int
		else if (main.minDist.intValue()>next.minDist.intValue()) {
			main.minDist=next.minDist;
			main.left=next.left;
			main.right=next.right;
		}
	}
	
	public static LogInfo combineToLog() {
		LogInfo logInfo=new LogInfo();
		
		//In this loginfo, the ifnum combine all ifnums existing in infolist,
		//and the info combine all info in infolist and cover them.
		for (int i=0;i<infoList.size();i++) {
			for (int j=0; j<infoList.get(i).info.size();j++) {
				if (!logInfo.numInIfInfo(infoList.get(i).info.get(j).ifnum)){
					logInfo.info.add(infoList.get(i).info.get(j));
				}
			}
		}

		for (int i=0;i<infoList.size();i++) {
			for (int j=0; j<infoList.get(i).info.size();j++) {
				for (int k=0;k<logInfo.info.size();k++) {
					if (infoList.get(i).info.get(j).ifnum==logInfo.info.get(k).ifnum) {
						combineInfo(logInfo.info.get(k),infoList.get(i).info.get(j));
						break;
					}
				}
			}
		}
		return logInfo;

	}
	
	public static void outputMsg(String dest) {
	    try{
	        File file =new File(dest);
	        if(!file.exists()){
	         file.createNewFile();
	        }
	        FileWriter fileWriter = new FileWriter(dest,true);
	        fileWriter.write(msg);
	        fileWriter.write("\n");
	        fileWriter.close();
	    }
	    catch(IOException e){
	         e.printStackTrace();
	    }
	}
	
	public static void runWholeProcess(String[] args,int targetline,int sleeptime) {
		String disk_name="F:";
		String class_path="F:\\mnt\\soot-trunk-withscripts\\test";
		String class_name="Solution";
		String folder_name="out";
		String arg1=" 114 19";
		String arg2=" 3915 1";
		
		if (args.length>0) disk_name=args[0];
		if (args.length>1) class_path=args[1];
		if (args.length>2) class_name=args[2];
		if (args.length>3) folder_name=args[3];
		if (args.length>4) {
			arg1="";
			arg2="";
			boolean b1=true;
			for (int i=4;i<args.length;i++) {
				if (args[i].equals("...")) {
					b1=false;
				}
				else if (b1) {
					arg1=arg1+" "+args[i];
				}
				else {
					arg2=arg2+" "+args[i];
				}
			}
		}
		
		try {
			Runtime.getRuntime().exec("cmd /c "+disk_name+" && "+
					"cd "+class_path+" && "+"java "+class_name+arg1);
			Thread.sleep(sleeptime);
			OutputFromLogInfo.main_log2out(class_path+"\\_DTJVM.log", class_path+"\\"
					+ folder_name+"\\out1.txt");
			Runtime.getRuntime().exec("cmd /c "+disk_name+" && "+
					"cd "+class_path+" && "+"del _DTJVM.log"+" && "+"java "+class_name+arg2);
			
			Thread.sleep(sleeptime);
			OutputFromLogInfo.main_log2out(class_path+"\\_DTJVM.log", class_path+"\\"
					+ folder_name+"\\out2.txt");
			
			Runtime.getRuntime().exec("cmd /c "+disk_name+" && "+
					"cd "+class_path+" && "+"del _DTJVM.log");
			inputFromFolder_OutputFile(class_path+"\\"+folder_name);
			calculateWholeDist("simple",targetline,new LogInfo());
		}
		catch (IOException e){
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
	

	public static void main(String[] args) {
		init();
		//String path="F:\\mnt\\soot-trunk-withscripts\\test\\out";
		//String dest="F:\\mnt\\soot-trunk-withscripts\\test\\outcombined.txt";
		runWholeProcess(args,68,1000);
		//inputFromFolder_Log(path);
		//printInfoList();
		//combineAndOutput(dest);
		//calculateWholeDist(68);

	}

}
