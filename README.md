jclassdump/util package is used for jimple code implanting. Run implant class. All jimple lines with "if *** goto *** " or "*** java.lang.AssertionError" will be implanted. The jimple code can be transfered from class by soot, and the implanted jimple code should also be transfered back to class. The class can output log file when an if condition is being hit or assert(false) statement is being hit. What's more, FindStartAndEndOfFunc.class can return the specific starting/ending lines of a function in jimple file (FOR TEST ONLY).

AnalyzeFromLog/loginfo package is used for log analysis and random testing to cover certain jimple statements. Run CoverStatements class. The seed and config file is written in .ini files. The program will generate arguments for the implanted class file and run it in order to hit more branches. The expected run environment is in windows system, but the version for linux also exists in the source code.
