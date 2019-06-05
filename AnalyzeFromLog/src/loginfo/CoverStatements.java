package loginfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.math.*;
import java.io.File;
import java.io.FileWriter;

public class CoverStatements {
	
	private static String int_p="^-?\\d+$";
	private static String double_p="^-?\\d+\\.\\d*$";
	
	public static String disk_name="F:";
	public static String class_path="F:/mnt/soot-trunk-withscripts/test";
	public static String class_name="Solution";
	public static String folder_name="out";
	public static int max_pace=5;
	public static int max_iter=50;
	public static int sleep_time=5000;
	public static int target_line=68;
	public static int start=11;
	public static int end=72;
	public static int loop=40;
	
	public static ArrayList<ArrayList<String>> hist_seed=new ArrayList<>();
	public static ArrayList<ArrayList<String>> hist_args=new ArrayList<>();
	public static ArrayList<String> seed_args=new ArrayList<>();
	public static ArrayList<String> cur_args=new ArrayList<>();
	public static ArrayList<String> seedArgType=new ArrayList<>();
	
	private static boolean last_closer=false;
	private static int last_arg_chosen=0;
	private static int last_mode=0;
	private static int last_char=0;
	private static boolean last_sign=true;
	
	public static LogInfo combined=new LogInfo();
	public static int reject_num=0;
	public static int reach=-1;
	
	//given seed, generated the next arguments to be tested
	//3 generation plans for int/double/string
	
	public static ArrayList<String> genArgs(ArrayList<String> oriArgs) {
		Random r=new Random();
		int change=last_arg_chosen;
		if (!last_closer) {
		    change=r.nextInt(oriArgs.size());
		    last_arg_chosen=change;
		}
		ArrayList<String> gen=(ArrayList<String>) oriArgs.clone();
		
		String GenArg=oriArgs.get(change);
		if (seedArgType.get(change).equals("int")){			
			int intGenArg=Integer.parseInt(oriArgs.get(change));
			
			int mode=last_mode;
			if (!last_closer) {
				mode=r.nextInt(15);
				last_mode=mode;
			}
			if (mode<12) {
				int diff=r.nextInt(max_pace)+1;
				int maxdigit=GenArg.length();
				if (GenArg.charAt(0)=='-') maxdigit-=1;
				if (maxdigit>8) maxdigit=8;
				int mode_2=r.nextInt(2);
				int digit;
				if (mode_2==0) {//1/2chance
				    digit=r.nextInt(maxdigit);
				}
				else {
					digit=r.nextInt(2);
					if (digit==1) digit=maxdigit-1;
				}
				
				int sign=r.nextInt(2);
				if (last_closer) {
					if (last_sign) sign=1;
					else sign=-1;
				}
				else {
					if (sign==0) {sign=-1;last_sign=false;}
					else last_sign=true;
				}
				intGenArg=intGenArg+sign*diff*(int)Math.round(Math.pow(10, digit));
			}
			else if (mode==12) intGenArg=-intGenArg;
			else if (mode==13) intGenArg=10*intGenArg;
			else intGenArg=intGenArg/10;
			gen.set(change,intGenArg+"");
		}
		
		else if (seedArgType.get(change).equals("double")){
			double doubleGenArg=Double.parseDouble(oriArgs.get(change));
			
			int mode=last_mode;
			if (!last_closer) {
				mode=r.nextInt(15);
				last_mode=mode;
			}
			
			int point=0;
			for (int i=0;i<GenArg.length();i++) {
				if (GenArg.charAt(i)=='.') point=GenArg.length()-1-i;
			}
			
			if (mode<12) {
				int diff=r.nextInt(max_pace)+1;
				int maxdigit=GenArg.length();
				for (int i=0;i<GenArg.length();i++) {
					if (GenArg.charAt(i)=='.') {
						maxdigit+=1;
					}
					if (!".-0".contains(GenArg.charAt(i)+"")) {
						maxdigit-=(i+1);
						break;
					}
				}
				//if (maxdigit>8) maxdigit=8;
				int mode_2=r.nextInt(2);
				int digit;
				if (mode_2==0) {//1/2chance
				    digit=r.nextInt(maxdigit);
				}
				else {
					digit=r.nextInt(3)+point-1;
					if (digit>point) digit=maxdigit-1;
				}

				int sign=r.nextInt(2);
				if (last_closer) {
					if (last_sign) sign=1;
					else sign=-1;
				}
				else {
					if (sign==0) {sign=-1;last_sign=false;}
					else last_sign=true;
				}
				doubleGenArg=doubleGenArg+sign*diff*Math.pow(10, digit-point);
				
			}
			else if (mode==12) doubleGenArg=-doubleGenArg;
			else if (mode==13) doubleGenArg=10*doubleGenArg;
			else doubleGenArg=doubleGenArg/10;
			gen.set(change,String.format("%."+point+"f", doubleGenArg));
			
		}
		else {
			int mode=last_mode;
			if (!last_closer) {
				mode=r.nextInt(10);
				last_mode=mode;
			}
			if (mode<8) {
				char[] GenArgArr=GenArg.toCharArray();
				int change_max=GenArgArr.length;
				int changeAt=last_char;
				if (!last_closer) {
					int mode_2=r.nextInt(3);
					if (mode_2!=0) 
						changeAt=r.nextInt(change_max);
					else changeAt=0; //1/3chance
				    last_char=changeAt;
				}
				int diff=r.nextInt(max_pace)+1;
				int sign=r.nextInt(2);
				if (last_closer) {
					if (last_sign) sign=1;
					else sign=-1;
				}
				else {
					if (sign==0) {sign=-1;last_sign=false;}
					else last_sign=true;
				}
				int newint=GenArgArr[changeAt]+sign*diff;
				if (newint>126) newint-=94;
				if (newint<33) newint+=94;
				GenArgArr[changeAt]=(char)(newint);
				gen.set(change, String.valueOf(GenArgArr));
			}
			else if (mode==8) {
				int newint=r.nextInt(94)+33;
				gen.set(change, GenArg+(char)newint);
			}
			else {
				if (GenArg.length()>1)
					gen.set(change, GenArg.substring(0, GenArg.length()-1));
			}
		}

		return gen;
	}
	
	//to run on windows cmd or linux terminal, we should consider escape chars
	//and fxxk the cmd escape chars
	
	private static String escape(String str) {
		ArrayList<Character> carr=new ArrayList<>();
		char[] cstr=str.toCharArray();
		
		for (int i=0;i<cstr.length;i++) {
			if (cstr[i]=='\"') {
				carr.add('\"');
				carr.add(cstr[i]);
			}
			else if (cstr[i]==' ') {
				if (i>0) {
					carr.add('\"');
				}
				carr.add(cstr[i]);
				carr.add('\"');
			}
			else carr.add(cstr[i]);
		}
		char[] cnarr=new char[carr.size()];
		for (int i=0;i<carr.size();i++) {
			cnarr[i]=carr.get(i);
		}
		
		String nstr=String.valueOf(cnarr);
		//System.out.println(nstr);
		
		return nstr;
	}
	
	private static String escapeInLinux(String str) {
		ArrayList<Character> carr=new ArrayList<>();
		char[] cstr=str.toCharArray();
		for (int i=0;i<cstr.length;i++) {
			if ("\"\'\\&|;".contains(cstr[i]+"")) {
				carr.add('\\');
			}
			carr.add(cstr[i]);
		}
		char[] cnarr=new char[carr.size()];
		for (int i=0;i<carr.size();i++) {
			cnarr[i]=carr.get(i);
		}
		
		String nstr=String.valueOf(cnarr);
		System.out.println(nstr);
		
		return nstr;
	}
	
	//run seed args and current test args on windows or linux
	
	public static void runSeed(int targetline,int sleeptime) {
		
		String arg1="";
		for (int i=0;i<seed_args.size();i++) {
			arg1+=" "+seed_args.get(i);
		}
		
		try {
			Runtime.getRuntime().exec("cmd /c "+disk_name+" && "+
					"cd "+class_path+" && "+"java -ea "+class_name+escape(arg1));

			Thread.sleep(sleeptime);
			OutputFromLogInfo.main_log2out(class_path+"/_DTJVM.log", class_path+"/"
					+ folder_name+"/out1.txt");
			
			Runtime.getRuntime().exec("cmd /c "+disk_name+" && "+
					"cd "+class_path+" && "+"del _DTJVM.log");
			
		}
		catch (IOException e){
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
	
	public static void runCurArgs(int targetline,int sleeptime) {
		
		String arg2="";
		for (int i=0;i<cur_args.size();i++) {
			arg2+=" "+cur_args.get(i);
		}
		
		try {
			Runtime.getRuntime().exec("cmd /c "+disk_name+" && "+
					"cd "+class_path+" && "+"java -ea "+class_name+escape(arg2));
			Thread.sleep(sleeptime);
			OutputFromLogInfo.main_log2out(class_path+"/_DTJVM.log", class_path+"/"
					+ folder_name+"/out2.txt");
			
			Runtime.getRuntime().exec("cmd /c "+disk_name+" && "+
					"cd "+class_path+" && "+"del _DTJVM.log");
			
		}
		catch (IOException e){
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
	
	public static void runSeedInLinux(int targetline,int sleeptime) {
		
		String arg1="";
		for (int i=0;i<seed_args.size();i++) {
			arg1+=" "+seed_args.get(i);
		}
		
		try {
			String cmd1[]={ "/bin/sh", "-c", "cd "+class_path+
					"; java -classpath .:lib/* -noverify -ea "+class_name+escapeInLinux(arg1)};
			String cmd2[]={ "/bin/sh", "-c", "cd "+class_path+"; rm _DTJVM.log"};
			
			Runtime.getRuntime().exec(cmd1);

			Thread.sleep(sleeptime);
			OutputFromLogInfo.main_log2out(class_path+"/_DTJVM.log", class_path+"/"
					+ folder_name+"/out1.txt");
			
			Runtime.getRuntime().exec(cmd2);
			
		}
		catch (IOException e){
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
	
	public static void runCurArgsInLinux(int targetline,int sleeptime) {
		
		String arg2="";
		for (int i=0;i<cur_args.size();i++) {
			arg2+=" "+cur_args.get(i);
		}
		
		//System.out.println(class_path);
		
		try {
			String cmd1[]={ "/bin/sh", "-c", "cd "+class_path+
					"; java -classpath .:lib/* -noverify -ea "+class_name+escapeInLinux(arg2)};
			String cmd2[]={ "/bin/sh", "-c", "cd "+class_path+"; rm _DTJVM.log"};
			
			Runtime.getRuntime().exec(cmd1);

			Thread.sleep(sleeptime);
			OutputFromLogInfo.main_log2out(class_path+"/_DTJVM.log", class_path+"/"
					+ folder_name+"/out2.txt");
			
			Runtime.getRuntime().exec(cmd2);
			
		}
		catch (IOException e){
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
	
	//accept the current test args when hit other branch or be closer to hit
	
	private static void accept(boolean hit) {
		CompareOutputFile.infoList.add(combined);
		combined=CompareOutputFile.combineToLog();
		if (combined.AssertionPoint>0) {
			reach=combined.AssertionPoint;
			CompareOutputFile.msg+="Assertion point at "+reach+" already reached, ";
		}
		File _old=new File(class_path+"/"+folder_name+"/out1.txt");
		_old.delete();
		File _new=new File(class_path+"/"+folder_name+"/out2.txt");
		_new.renameTo(_old);
		
		seed_args=(ArrayList<String>) cur_args.clone();
		if (hit) hist_seed.add(seed_args);
		else last_closer=true;
		CompareOutputFile.msg+="accepted.";
		reject_num=0;
		hist_args=new ArrayList<>();
		
	}
	
	//otherwise
	
	private static void reject() {CompareOutputFile.msg+="rejected."; reject_num+=1;}
	
	//parse setting from config.ini to program
	
	private static void parseConfig() {
		try {
			File config=new File("config.ini");
			if (!config.exists()) {
				config.createNewFile();
				FileWriter fileWriter = new FileWriter("config.ini");
				fileWriter.write("disk_name "+disk_name+"\n");
				fileWriter.write("class_path "+class_path+"\n");
				fileWriter.write("class_name "+class_name+"\n");
				fileWriter.write("folder_name "+folder_name+"\n");
				fileWriter.write("max_pace "+max_pace+"\n");
				fileWriter.write("max_iter "+max_iter+"\n");
				fileWriter.write("sleep_time "+sleep_time+"\n");
				fileWriter.write("target_line "+target_line+"\n");
				fileWriter.write("start "+start+"\n");
				fileWriter.write("end "+end+"\n");
				fileWriter.write("loop "+loop+"\n");
				fileWriter.close();
			}
			else {
				String temp="";
				Scanner s=new Scanner(config);
				while (s.hasNextLine()) {
					temp += s.nextLine() + "\n";
				}
				s.close();
				temp = temp.replaceAll("\n\n", "\n");
				ArrayList<String> configList = new ArrayList<String>(Arrays.asList(temp.trim().split("\n")));
				for (int i=0;i<configList.size();i++) {
					String[] arg=configList.get(i).trim().split(" ");
					if (arg[0].equals("disk_name")) disk_name=arg[1];
					if (arg[0].equals("class_path")) class_path=arg[1];
					if (arg[0].equals("class_name")) class_name=arg[1];
					if (arg[0].equals("folder_name")) folder_name=arg[1];
					if (arg[0].equals("max_pace")) max_pace=Integer.parseInt(arg[1]);
					if (arg[0].equals("max_iter")) max_iter=Integer.parseInt(arg[1]);
					if (arg[0].equals("sleep_time")) sleep_time=Integer.parseInt(arg[1]);
					if (arg[0].equals("target_line")) target_line=Integer.parseInt(arg[1]);
					if (arg[0].equals("start")) start=Integer.parseInt(arg[1]);
					if (arg[0].equals("end")) end=Integer.parseInt(arg[1]);
					if (arg[0].equals("loop")) loop=Integer.parseInt(arg[1]);
				}
			}
			CompareOutputFile.msg+="disk_name:"+disk_name+"\n"+"class_path:"+class_path+"\n"+
			"class_name:"+class_name+"\n"+"folder_name:"+folder_name+"\n"+
					"sleep_time:"+sleep_time+"\n"+"target_line:"+target_line+"\n"+"start:"+
			start+"\n"+"end:"+end+"\n";
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//parse seed arguments from seed.ini to program
	
	private static void parseSeed() {
		try {
			File seed=new File("seed.ini");
			
			if (!seed.exists()) {
				seed.createNewFile();
			}
			else {
				String seedStr="";
				Scanner s=new Scanner(seed);
				if (s.hasNextLine()) {
					seedStr += s.nextLine();
				}
				s.close();
				String[] seedArr=seedStr.split(" ");
				CompareOutputFile.msg+="seed args: ";
				for (int i=0;i<seedArr.length;i++) {
					if (!seedArr[i].equals("")) {
						seed_args.add(seedArr[i]);
						if (Pattern.matches(int_p, seedArr[i])) 
							seedArgType.add("int");
						else if (Pattern.matches(double_p, seedArr[i])) 
							seedArgType.add("double");
						else
							seedArgType.add("string");
						CompareOutputFile.msg+=seedArr[i]+" ";
					}
				}
			}
			CompareOutputFile.msg+="\n";
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		File oldlog=new File("F:/mnt/soot-trunk-withscripts/test/_DTJVM.log");
		if (oldlog.exists()) oldlog.delete();
		File textmsg=new File("F:/mnt/soot-trunk-withscripts/test/testlog.txt");
		if (textmsg.exists()) textmsg.delete();
		CompareOutputFile.init();
		parseConfig();
		parseSeed();
		hist_seed.add(seed_args);
		
		runSeed(target_line,sleep_time);
		combined=OutputFromLogInfo.main_in2info(class_path+"/"+folder_name+"/out1.txt");
		
		reach=combined.AssertionPoint;
		if (reach==target_line) {
			System.out.println("Seed input has already reached target line.");
			return;
		}
		
		for (int time=0;time<loop;time++) {
			if (reject_num>=max_iter) {
				Random r=new Random();
				//int index=hist_seed.size()-2;
				//if (index<0) index=0;
				int index=r.nextInt(hist_seed.size());
				seed_args=(ArrayList<String>) hist_seed.get(index).clone();
				runSeed(target_line,sleep_time);
				combined=OutputFromLogInfo.main_in2info(class_path+"/"+folder_name+"/out1.txt");
				reject_num=0;
				hist_args=new ArrayList<>();
			}
			cur_args=genArgs(seed_args);
			while (hist_args.contains(cur_args))
				cur_args=genArgs(seed_args);
			
			hist_args.add(cur_args);
			
			CompareOutputFile.msg+="Loop: "+time+"\n";
			System.out.println(time);
			CompareOutputFile.msg+="Seed: ";
			for (int i=0;i<seed_args.size();i++) {
				System.out.println(seed_args.get(i));
				CompareOutputFile.msg+=seed_args.get(i)+" ";
			}
			CompareOutputFile.msg+="\nCurrent args: ";
			for (int i=0;i<cur_args.size();i++) {
				System.out.println(cur_args.get(i));
				CompareOutputFile.msg+=cur_args.get(i)+" ";
			}
			CompareOutputFile.msg+="\n";
			runCurArgs(target_line,sleep_time);
			CompareOutputFile.inputFromFolder_OutputFile(class_path+"/"+folder_name);
			int new_index=1;
			for (int i=0;i<CompareOutputFile.infoList.size();i++) {
				if (CompareOutputFile.fileNameList.get(i).equals("out2.txt")) {
					new_index=i;
				}
			}
			
			boolean hit=CompareOutputFile.hitOtherRoute(combined, 
					CompareOutputFile.infoList.get(new_index),start,end);
			
			if (hit) {
				System.out.println("hit");accept(true);last_closer=false;
			}
			else {
				CompareOutputFile.calculateWholeDist("cover",start,end,target_line,combined);
				if (CompareOutputFile.distList.get(1-new_index)>CompareOutputFile.distList.get(new_index)) {
					System.out.println("closer");
					accept(false);
				}
				else {System.out.println("not closer");reject();last_closer=false;}
			}

			CompareOutputFile.outputMsg("F:/mnt/soot-trunk-withscripts/test/testlog.txt");
			CompareOutputFile.init();
			
			if (reach==target_line) {
				System.out.println("Generated input has already reached target line.");
				break;
			}
			
		}
		//combined.printInfo();
		
		//CompareOutputFile.calculateWholeDist(68);
		System.out.println("Done");
		
	}

}
