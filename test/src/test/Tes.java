package test;

public class Tes {

	
	public static void main(String args[]) {
		String num = getNum("C000000000001");
		System.out.println(num);
	}
	public static String getNum(String secNumwithzeros) {
		
		String secNumwithoutzeros=null;
		if(secNumwithzeros!=null && secNumwithzeros.trim()!="") {
			String secNumFirstChar=Character.toString(secNumwithzeros.charAt(0));
			if(secNumFirstChar.equalsIgnoreCase("C")) {
				String secNum=secNumwithzeros.substring(1);
				int secnumber=Integer.parseInt(secNum);
				secNumwithoutzeros=((Integer)secnumber).toString();
			}
		}
		return secNumwithoutzeros;
	}
}
