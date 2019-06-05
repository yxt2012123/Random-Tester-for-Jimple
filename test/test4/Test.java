class Test {
	public static void terminate(){
		return;
	}
	
  public static void main(String[] args) {
  	int a=Integer.parseInt(args[0]);
  	if (a>2008){terminate();}
  	else if (a>2007){terminate();}
  	else if (a>=2006){terminate();}
  	else if (a==2005){terminate();}
  	else if (a>2003){assert(false);}
  	else{terminate();}    
  }
}
