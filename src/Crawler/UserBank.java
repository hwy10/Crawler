package Crawler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import BasicOps.FileOps;

public class UserBank {
	private static HashMap<String, HashMap<String, String> > users=new HashMap<String, HashMap<String,String>>();
	private static HashSet<String> banUsers=new HashSet<String>();
	private static HashMap<String, LinkedList<String> > userlist=new HashMap<String, LinkedList<String>>();
	private static HashMap<String,Integer> assigned=new HashMap<String, Integer>();
	private static HashMap<String, String> assignedProxy=new HashMap<String, String>();
	
	public static void addUser(String site,String username,String pwd){
		if (!users.containsKey(site)){
			users.put(site, new HashMap<String, String>());
			userlist.put(site, new LinkedList<String>());
		}
		if (users.get(site).containsKey(username)) return;
		userlist.get(site).add(username);
		users.get(site).put(username, pwd);
	}
	
	public static void load(){
		LinkedList<String> rows=FileOps.LoadFilebyLine("userbank.txt");
		for (String row:rows){
			String [] sep=row.split("\t");
			if (sep.length==3){
				if (sep[0].equals("proxy"))
					assignedProxy.put(sep[1], sep[2]);
				else addUser(sep[0],sep[1],sep[2]);
			}
			if (sep.length==2&&sep[0].equals("ban"))
				banUsers.add(sep[1]);
			
		}
	}
	
	public static void save(){
		LinkedList<String> rows=new LinkedList<String>();
		for (String site:users.keySet())
			for (String username:users.get(site).keySet())
				rows.add(site+"\t"+username+"\t"+users.get(site).get(username));
		for (String user:banUsers)
			rows.add("ban\t"+user);
		for (String user:assignedProxy.keySet())
			rows.add("proxy\t"+user+"\t"+assignedProxy.get(user));
		FileOps.SaveFile("userbank.txt", rows);
	}
	
	public static Object[][] getUserInfo(){
		int cnt=0;
		for (String site:users.keySet())
			cnt+=users.get(site).size();
		Object[][] content=new Object[cnt][6];
		int cur=0;
		for (String site:users.keySet())
			for (String username:users.get(site).keySet()){
				content[cur][0]=site;
				content[cur][1]=username;
				content[cur][2]=users.get(site).get(username);
				String uid=site+"::"+username;
				if (assigned.containsKey(uid))
					content[cur][3]=assigned.get(uid);
				else content[cur][3]="";
				if (banUsers.contains(uid))
					content[cur][4]=true;
				else content[cur][4]=false;
				if (assignedProxy.containsKey(uid)){
					content[cur][5]=assignedProxy.get(uid);
				}
				else content[cur][5]=""; 
				cur++;
			}
		return content;
	}
	
	public static void reportUser(String site,String username){
		banUsers.add(site+"::"+username);
		save();
	}
	
	public static String getUser(String site,int limit){
		if (!users.containsKey(site)) return "";
		for (String user:users.get(site).keySet()){
			String uid=site+"::"+user;
			if (banUsers.contains(uid)) continue;
			if (assigned.containsKey(uid)&&assigned.get(uid)>=limit) continue;
			if (!assigned.containsKey(uid))
				assigned.put(uid, 1);
			else assigned.put(uid, assigned.get(uid)+1);
			return user+"::"+users.get(site).get(user);
		}
		return "";
	}
	
	public static void releaseUser(String site,String username){
		try{
			String uid=site+"::"+username;
			assigned.put(uid,assigned.get(uid)-1);
		}catch (Exception ex){}
		
	}
	
	public static void changeBan(String site,String username){
		String uid=site+"::"+username;
		if (banUsers.contains(uid))
			banUsers.remove(uid);
		else banUsers.add(uid);
		save();
	}
	public static void assignProxy(String Site,String username,String proxy){
		assignedProxy.put(Site+"::"+username, proxy);
		save();
	}
	public static String getProxy(String site,String username){
		String uid=site+"::"+username;
		if (assignedProxy.containsKey(uid))
			return assignedProxy.get(uid);
		return "";
	}
}
