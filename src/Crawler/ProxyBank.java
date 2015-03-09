package Crawler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Util.NetworkConnect;
import BasicOps.FileOps;

public class ProxyBank {
	public static HashSet<String> proxy=new HashSet<String>();
	public static HashMap<String,Integer> badProxy=new HashMap<String, Integer>();
	public static HashMap<String,Integer> assignment=new HashMap<String, Integer>();
	
	public static void proxyLoader(){
		Thread loader=new ProxyLoader();
		loader.start();
	}
	
	public static String getProxy(int limit){
		String res="";
		synchronized (proxy) {
			int cnt=limit;
			for (String s:proxy){
				if (assignment.containsKey(s)&&assignment.get(s)>=limit)
					continue;
				int cur=0;
				if (assignment.containsKey(s)) cur=assignment.get(s);
				if (cur<cnt){
					cnt=cur;
					res=s;
				}
			}
			if (cnt==limit) return "No_Available_Proxy";
			if (assignment.containsKey(res))
				assignment.put(res, assignment.get(res)+1);
			else assignment.put(res, 1);
		}
		return res;
	}
	public static void loadProxies(){
		proxy=new HashSet<String>();
		for (String row:BasicOps.FileOps.LoadFilebyLine("proxybank.txt")){
			String[] sep=row.split("\t");
			if (sep.length==2)
				proxy.add(sep[0]+":"+sep[1]);
			else proxy.add(sep[0]);
		}
	}
	public static void report(String p){
		if (!badProxy.containsKey(p))
			badProxy.put(p,1);
		else badProxy.put(p,badProxy.get(p)+1);
		if (badProxy.get(p)>1) {
			removeProxy(p);
			Logger.add("Proxy Removed : "+p);
		}
	}
	public static void saveProxies(){
		LinkedList<String> content=new LinkedList<String>();
		content.addAll(proxy);
		FileOps.SaveFile("proxybank.txt", content);
	}
	public static void addProxy(String ip,int port){
		if (proxy.size()>10000) return;
		proxy.add(ip+":"+port);
	}
	public static void removeProxy(String p){
		proxy.remove(p);
		saveProxies();
	}
	public static void releaseProxy(String p){
		try{
			assignment.put(p, assignment.get(p)-1);
		}catch (Exception ex){}
	}
}


class ProxyLoader extends Thread{
	@Override
	public void run() {
		for (;;){
			try{
				for (int i=1;i<12;i++){
					try {
						String content=NetworkConnect.send2Get("http://www.proxy.com.ru/list_"+i+".html","","");
						Matcher matcher=Pattern.compile("<td>(\\d*\\.\\d*\\.\\d*\\.\\d*)</td><td>(\\d*)</td>").matcher(content);
						for (;matcher.find();){
							ProxyBank.addProxy(matcher.group(1), Integer.valueOf(matcher.group(2)));
						}
							
					} catch (Exception e) {
					}
				}
			}catch (Exception ex){
			}
			try {
				Thread.sleep(1000*60);
			} catch (Exception e) {
			}
		}
	}
}