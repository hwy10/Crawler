package Crawler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import BasicOps.FileOps;

public class ProxyBank {
	public static LinkedList<String> ProxyIP;
	public static LinkedList<Integer> ProxyPort;
	public static HashMap<String,Integer> badProxy=new HashMap<String, Integer>();
	
	public static String getProxy(int id){
		Random random=new Random();
		int pid=random.nextInt(ProxyIP.size());
		if (id!=-1) pid=id;
		if (pid>=ProxyIP.size()) return "Invalid";
		return ProxyIP.get(pid)+":"+ProxyPort.get(pid);
	}
	
	public static void loadProxies(){
		ProxyIP=new LinkedList<String>();
		ProxyPort=new LinkedList<Integer>();
		for (String proxy:BasicOps.FileOps.LoadFilebyLine("proxybank.txt")){
			String[] sep=proxy.split("\t");
			if (sep.length==2){
				ProxyIP.add(sep[0]);
				ProxyPort.add(Integer.valueOf(sep[1]));
			}
		}
	}
	public static void report(String ip){
		if (!badProxy.containsKey(ip))
			badProxy.put(ip,1);
		badProxy.put(ip,badProxy.get(ip)+1);
		if (badProxy.get(ip)>3) {
			removeProxy(ip);
			Logger.add("Proxy Removed : "+ip);
		}
	}
	public static void saveProxies(){
		LinkedList<String> content=new LinkedList<String>();
		for (int i=0;i<ProxyIP.size();i++)
			content.add(ProxyIP.get(i)+"\t"+ProxyPort.get(i));
		FileOps.SaveFile("proxybank.txt", content);
	}
	public static void addProxy(String ip,int port){
		ProxyIP.add(ip);
		ProxyPort.add(port);
	}
	public static void removeProxy(String ip){
		for (int i=0;i<ProxyIP.size();i++)
			if (ProxyIP.get(i).equals(ip)){
				ProxyIP.remove(i);
				ProxyPort.remove(i);
			}
		saveProxies();
	}
}
