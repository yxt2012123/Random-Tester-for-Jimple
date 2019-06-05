class Test {
  public static void main(String[] args) {
  	int n=Integer.parseInt(args[0]);
    if (n==4096){
    	System.out.println(n);
    	assert(false);
    }
  }
}
