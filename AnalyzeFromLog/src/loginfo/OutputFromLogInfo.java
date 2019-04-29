package loginfo;

public class OutputFromLogInfo {
	
	public static void main_log2out(String src,String dest) {
		LogInfo logInfo=new LogInfo();
		logInfo.readFromLog(src);
		//logInfo.printInfo();
		logInfo.output(dest);
	}
	
	public static LogInfo main_in2info(String src) {
		LogInfo logInfo=new LogInfo();
		logInfo.input(src);
		//logInfo.printInfo();
		return logInfo;
	}

	public static void main(String[] args) {
		String src="F:/mnt/soot-trunk-withscripts/test/_DTJVM.log";
		String dest="F:/mnt/soot-trunk-withscripts/test/out.txt";
		
		if (args.length>0) src=args[0];
		if (args.length>1) dest=args[1];
		
		main_log2out(src,dest);

	}

}
