package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class JClassDumpMemInfo {
	// Read a file into an array of lines
	public static ArrayList<String> readIntoLines(String classfilepath) {
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

		for (int i = 0; i < result.size();) {
			if (result.get(i).trim().length() == 0) {
				result.remove(i);
			} 
			else
			{
				//System.out.println(result.get(i));
				i++;
			}
		}
		return result;
	}

	public static boolean isVarDeclarations(String tmp) {
		if (tmp.contains("=") || tmp.contains("(") || //
				tmp.contains("}") || tmp.contains("{") || //
				tmp.contains("DTJVM_") || tmp.contains(":"))
			return false;
		ArrayList<String> tmps = Util.fromArraytoArrayList(tmp.split(" "));
		if (tmps.contains("class") || tmps.contains("interface") || //
				tmps.contains("staticinvoke") || tmps.contains("specialinvoke") || //
				tmps.contains("virtualinvoke") || tmps.contains("goto") || //
				tmps.contains("return") || tmps.contains("catch") || //
				tmps.contains("throw") || tmps.contains("entermonitor") || tmps.contains("exitmonitor"))
			return false;

		return true;
	}

	public static boolean isMethodsig(String tmp) {
		if (tmp.contains("(") && !tmp.contains("<") && !tmp.contains("=") &&
				!tmp.contains("lookupswitch(") && !tmp.contains("tableswitch("))
			return true;
		if (tmp.trim().equals("public void <init>()") || tmp.trim().equals("static void <clinit>()")) 
			return true;
		return false;
	}

	public static String getPreciseMethodsig(String tmp) {
		if (isMethodsig(tmp)) {

			int start = tmp.indexOf("(");
			int end = tmp.indexOf(")");

			String tmp2 = tmp.substring(0, start);
			String[] tmp2s = tmp2.trim().replaceAll("  ", " ").split(" ");
			String name = tmp2s[tmp2s.length - 1];

			return name + tmp.substring(start, end + 1);
		}
		return "";
	}

	public static String getClassname(ArrayList<String> jimpleStmts) {
		for (int i = 0; i < jimpleStmts.size(); i++) {
			String tmp = jimpleStmts.get(i);

			tmp = tmp.replaceAll(",", " ").replaceAll(";", " ").replaceAll("  ", " ").trim();
			String[] tmps = tmp.split(" ");
			ArrayList<String> tmparray = Util.fromArraytoArrayList(tmps);
			if (tmparray.contains("class") || tmparray.contains("interface")) {
				int index = 0;
				if (tmparray.contains("class")) {
					index = tmparray.indexOf("class");
				} else if (tmparray.contains("interface")) {
					index = tmparray.indexOf("interface");
				}
				return tmparray.get(index + 1);
			}
		}
		return "";
	}

	public static ArrayList<JTypeVar> createTypeVars(String tmp) {
		ArrayList<JTypeVar> result = new ArrayList<JTypeVar>();
		tmp = tmp.replaceAll(",", " ").replaceAll(";", " ").replaceAll("  ", " ").trim();
		String[] tmps = tmp.split(" ");
		ArrayList<String> tmparray = Util.fromArraytoArrayList(tmps);
		while (tmparray.remove("static") || tmparray.remove("final") || //
				tmparray.remove("public") || tmparray.remove("private") || //
				tmparray.remove("protected"))
			;

		for (int k = 1; k < tmparray.size(); k++) {
			result.add(new JTypeVar(tmparray.get(0), tmparray.get(k)));
		}
		return result;
	}

	public static ArrayList<JTypeVar> getGlobalVars(ArrayList<String> jimpleStmts) {
		String classname = getClassname(jimpleStmts);
		ArrayList<JTypeVar> result = new ArrayList<JTypeVar>();

		int i = 0;
		for (; i < jimpleStmts.size(); i++) {
			String[] tmps = jimpleStmts.get(i).trim().split(" ");
			ArrayList<String> tmparray = Util.fromArraytoArrayList(tmps);
			if (tmparray.contains("class") || tmparray.contains("interface"))
				break;
		}
		i++;
		int start = i;

		for (; i < jimpleStmts.size(); i++) {
			if (jimpleStmts.get(i).contains("(") || jimpleStmts.get(i).contains("}"))
				break;
		}
		int end = i;
		// System.out.println(start + " " + end);

		List<String> globalBody = jimpleStmts.subList(start, end);

		for (i = 0; i < globalBody.size(); i++) {
			// System.out.println(globalBody.get(i));
			String tmp = globalBody.get(i).trim();
			if (isVarDeclarations(tmp)) {
				result.addAll(createTypeVars(tmp));
			}
		}

		for (i = 0; i < result.size(); i++) {
			JTypeVar global = result.get(i);
			JTypeVar globalwithclassname = new JTypeVar(global.type, classname + ":" + global.var);
			result.set(i, globalwithclassname);
		}

		return result;
	}

	private static int getRightAngleBracket(String str, int position) {
		int start = str.indexOf("<", position);
		int end = start;
		int count = 1;
		while (count != 0) {
			end++;
			int end1 = str.indexOf("<", end);
			int end2 = str.indexOf(">", end);
			if (end1 > 0 && end2 > 0 && end1 < end2) {
				end = end1;
				count++;
			} else if (end1 > 0 && end2 > 0 && end1 > end2) {
				end = end2;
				count--;
			} else if (end1 > 0 && end2 < 0) {
				end = end1;
				count++;
			} else if (end1 < 0 && end2 > 0) {
				end = end2;
				count--;
			} else {
				break;
			}
		}

		return end;
	}

	public static ArrayList<JTypeVar> getVisibleVars(ArrayList<String> jimpleStmts, String methodsig) {
		ArrayList<JTypeVar> result = new ArrayList<JTypeVar>();
		result.addAll(getGlobalVars(jimpleStmts));

		result.add(new JTypeVar("", ""));

		// <classA: int a>: (1) such global vars can occur several times; (2)
		// <abc<def>ghi>
		for (int i = 0; i < jimpleStmts.size(); i++) {
			String stmt = jimpleStmts.get(i);
			if (!stmt.contains("<"))
				continue;

			int start = stmt.indexOf("<", 0);
			while (start >= 0) {
				int end = getRightAngleBracket(stmt, start);

				if (start < end) {
					String tmp = stmt.substring(start + 1, end).trim().replaceAll(":", " ").replaceAll("  ", " ");
					if (!tmp.contains("<") && !tmp.contains(">") && !tmp.contains("(") && !tmp.contains(")")) {
						String[] tmps = tmp.split(" ");
						if (tmps.length == 3) {
							// System.out.println(tmp);
							JTypeVar var = new JTypeVar(tmps[1], tmps[0] + ":" + tmps[2]);
							if (!var.isInArray(result))
								result.add(var);
						}
					}
				}
				start = stmt.indexOf("<", start + 1);
			}
		}

		result.add(new JTypeVar("", ""));
		result.addAll(getLocalVars(jimpleStmts, methodsig));
		return result;
	}

	public static ArrayList<JTypeVar> getLocalVars(ArrayList<String> jimpleStmts, String methodsig) {
		ArrayList<JTypeVar> result = new ArrayList<JTypeVar>();
		if (!isMethodsig(methodsig))
			return result;

		int i = 0;
		for (; i < jimpleStmts.size(); i++) {
			
			if (jimpleStmts.get(i).replaceAll(" ", "").contains(methodsig.replaceAll(" ", "")))
				break;
		}
		//System.out.println(jimpleStmts.get(i).replaceAll(" ", ""));
		if (jimpleStmts.get(i).trim().endsWith(";"))
			return result;

		while (!jimpleStmts.get(i).trim().equals("{")) {
			i++;
		}
		int count = 1;
		int start = i + 1;
		int end = start - 1;
		while (count != 0) {
			end++;
			if (jimpleStmts.get(end).trim().equals("{"))
				count++;
			if (jimpleStmts.get(end).trim().equals("}") || jimpleStmts.get(end).trim().equals("};"))
				count--;
		}

		List<String> methodBody = jimpleStmts.subList(start, end);
		for (i = 0; i < methodBody.size();) {
			String tmp = methodBody.get(i).trim();
			if (isVarDeclarations(tmp)) {
				result.addAll(createTypeVars(tmp));
				i++;
			} else {
				//methodBody.remove(i);
				i++;
			}
		}
		return result;
	}

	public static String getType(ArrayList<String> jimpleStmts, String methodsig, String variable) {
		if (Util.findOccurences(variable, "<") != 1 && Util.findOccurences(variable, ">") != 1) {
			// <classA: int x>

			String tmp = variable.substring(variable.indexOf("<") + 1, variable.indexOf(">"));
			String[] tmps = tmp.split(" ");
			return tmps[tmps.length - 2];
		}

		ArrayList<JTypeVar> locals = getLocalVars(jimpleStmts, methodsig);
		for (int i = 0; i < locals.size(); i++) {
			JTypeVar tmp = locals.get(i);
			if (tmp.var.trim().equals(variable.trim()))
				return tmp.type.trim();
		}
		assert (false); // We should not reach here
		return "-1";
	}

	public static void main(String args[]) {
		ArrayList<String> jimplestmts = JClassDumpMemInfo.readIntoLines(
				"F:\\mnt\\soot-trunk-withscripts\\test\\Solution.jimple");

		System.out.println(getClassname(jimplestmts));
		//System.out.println(jimplestmts.toString());

		//give a function, store all visible variable in it
		ArrayList<JTypeVar> allvisible = getVisibleVars(jimplestmts,
				"public static void printArray(int[])");
		//String var = "org.eclipse.core.runtime.adaptor.EclipseStarter:adaptor";
		String var = "i2";
		//System.out.println(allvisible.toString());
		
		//give a name of a variable, print its type
		for (int i = 0; i < allvisible.size(); i++) {
			if (allvisible.get(i).var.trim().equals(var))
				System.out.println(allvisible.get(i).type);
		}

		//find and print all global variables
		ArrayList<JTypeVar> globals = getGlobalVars(jimplestmts);
		for (int i = 0; i < globals.size(); i++)
			System.out.println(globals.get(i));

		//find and print all local variables in all functions
		for (int i = 0; i < jimplestmts.size(); i++) {
			//System.out.println(jimplestmts.get(i));
			if (isMethodsig(jimplestmts.get(i))) {
				System.out.println();
				System.out.println(getPreciseMethodsig(jimplestmts.get(i)));
				ArrayList<JTypeVar> locals = getLocalVars(jimplestmts, jimplestmts.get(i));
				for (int j = 0; j < locals.size(); j++)
					System.out.println(locals.get(j));
			}
			
		}

	/*	System.out.println(getType(jimplestmts,
				"public static org.osgi.framework.BundleContext startup(java.lang.String[], java.lang.Runnable) throws java.lang.Exception",
				"r0"));
  */
	}
}
