class Test {
  public static void main(String[] args) {
  	char[] arr=args[0].toCharArray();
    if (arr.length!=6) return;
    if (arr[5]=='g' && arr[0]=='s' && arr[2]=='r' && arr[1]=='t' && arr[4]=='n' && arr[3]=='i'){
    	System.out.println(args[0]);
    	assert(false);
    }
  }
}
