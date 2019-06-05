class Test {
  public static void main(String[] args) {
  	int n=Integer.parseInt(args[0]);
  	int m=Integer.parseInt(args[1]);
  	if (n+m==25 && n*m>=154){
  		System.out.println(n);
  		System.out.println(m);
  		assert(false);
  	}
  }
}
