package Crawler;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.http.client.params.CookiePolicy;


public class Config {
	public static int NWorker=10;
	public static int GuardInterval=3;
	public static int restTime=1000;
	public static int guiUpdateInterval=5;
	
	public static int ProxyTrial=1;
	
	public static String Task="DoubanMovie";
	
	public static String cookie=CookiePolicy.BROWSER_COMPATIBILITY;
	
}
