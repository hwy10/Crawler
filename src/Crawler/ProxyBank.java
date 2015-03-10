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
	public static HashSet<String> blacklist=new HashSet<String>();
	public static HashMap<String,Integer> badProxy=new HashMap<String, Integer>();
	public static HashMap<String,Integer> assignment=new HashMap<String, Integer>();
	
	
	public static int size(){
		return proxy.size();
	}
	public static int inuse(){
		int cnt=0;
		for (Integer i:assignment.values())
			cnt+=(i>0)?1:0;
		return cnt;
	}
	
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
				blacklist.add(sep[1]);
			else proxy.add(sep[0]);
		}
	}
	public static void report(String p){
		if (!badProxy.containsKey(p))
			badProxy.put(p,1);
		else badProxy.put(p,badProxy.get(p)+1);
		if (badProxy.get(p)>3) {
			blacklist.add(p);
			removeProxy(p);
			Logger.add("Proxy Removed : "+p);
		}
	}
	public static void saveProxies(){
		LinkedList<String> content=new LinkedList<String>();
		content.addAll(proxy);
		for (String p:blacklist)
			content.add("ban\t"+p);
		FileOps.SaveFile("proxybank.txt", content);
	}
	public static void addProxy(String p){
		if (proxy.size()>10000) return;
		if (blacklist.contains(p)) return;
		proxy.add(p);
	}
	public static void addProxy(String ip,int port){
		addProxy(ip+":"+port);
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
	public static void reportGood(String p){
		try{
			badProxy.remove(p);
		}catch( Exception ex){}
	}
}


class ProxyLoader extends Thread{
	@Override
	public void run() {
		TaskSetting setting=new TaskSetting();
		setting.proxyType="local";
		Client client=new Client(setting);
		for (;;){
			//Source #2
			try{
				int cnt=0;
				String content=client.getContent("http://www.free-proxy-list.net");
				Matcher matcher=Pattern.compile("<td>(\\d*\\.\\d*\\.\\d*\\.\\d*)</td><td>(\\d*)</td>").matcher(content);
				for (;matcher.find();){
					cnt++;
					ProxyBank.addProxy(matcher.group(1), Integer.valueOf(matcher.group(2)));
				}
				Logger.add("Proxy Source #2 : "+cnt);
			}catch (Exception ex){}
			ProxyBank.saveProxies();
			
			//Source #3 & #1
			try{
				for (int i=1;i<100;i++){
					try {
						int cnt=0;
						String content=client.getContent("http://proxy-list.org/english/index.php?p="+i);
						Matcher matcher=Pattern.compile("<li class=\"proxy\">(.*?)</li>").matcher(content);
						for (;matcher.find();){
							ProxyBank.addProxy(matcher.group(1));
							cnt++;
						}
						Logger.add("Proxy Source #3 "+i+" : "+cnt);
					} catch (Exception e) {
					}
					try {
						int cnt=0;
						String content=client.getContent("http://www.proxy.com.ru/list_"+i+".html");
						Matcher matcher=Pattern.compile("<td>(\\d*\\.\\d*\\.\\d*\\.\\d*)</td><td>(\\d*)</td>").matcher(content);
						for (;matcher.find();){
							cnt++;
							ProxyBank.addProxy(matcher.group(1), Integer.valueOf(matcher.group(2)));
						}
						Logger.add("Proxy Source #1 "+i+" : "+cnt);
					} catch (Exception e) {
					}
					try{
						ProxyBank.saveProxies();
						Thread.sleep(3000);
					}catch(Exception ex){}
				}
			}catch (Exception ex){}
			//Source #1
			try{
				for (int i=1;i<100;i++){
					
				}
			}catch (Exception ex){
			}
			
			ProxyBank.saveProxies();
			try {
				Thread.sleep(1000*60);
			} catch (Exception e) {
			}
		}
	}
}