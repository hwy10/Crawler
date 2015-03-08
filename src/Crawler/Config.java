package Crawler;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.http.client.params.CookiePolicy;


public class Config {
	public static int NWorker=1;
	public static int GuardInterval=3;
	public static int restTime=1000;
	public static int guiUpdateInterval=5;
	
	public static String Task="WeiboUser";
	
	public static String cookie=CookiePolicy.BROWSER_COMPATIBILITY;
	
}
