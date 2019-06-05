class Test {
  public static void main(String[] args) {
  	int n=Integer.parseInt(args[0]);
  	int m=Integer.parseInt(args[1]);
  	double k=Double.parseDouble(args[2]);
  	int i=0;
  	if (m>k) i++;
  	if (k>m-n) i++;
  	if (k>n) i++;
  	if (i>=3)
  		assert(false);
  }
}
