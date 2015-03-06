package Crawler;
import java.util.LinkedList;


public class Config {
	public static int NWorker=1;
	public static int restTime=1000;
	public static LinkedList<String> ProxyIP;
	public static LinkedList<Integer> ProxyPort;
	public static String Task="";
	
	public static String username="hijack2004@126.com";
	public static String password="cat12321";
	
	public static LinkedList<Worker> workers=new LinkedList<Worker>();
}
