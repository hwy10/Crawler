package AboutMe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import Util.NetworkConnect;


public class Crawler {
	private LinkedList<String> queue;
	public LinkedList<String> sites;
	private Set<String> all;
	public int NThread=1;
	private File dir;
	public String authen;
	public String cookie;
	private int cnt;
	public void setSites(String s){
		String cur="";
		s=s+",";
		for (int i=0;i<s.length();i++){
			if (s.charAt(i)==','){
				if (cur.length()>0) sites.add(cur);
				cur="";
			}
			else cur=cur+s.charAt(i);
		}
		System.out.println("Sites: ");
		for (String ss:sites)
			System.out.println(ss);
	}
	public void init(String dirPath){
		cnt=0;
		sites=new LinkedList<String>();
		queue=new LinkedList<String>();
		all=new HashSet<String>();
		dir=new File(dirPath);
		if (!dir.exists()||!dir.isDirectory())
			dir.mkdir();
	}
	public void addUser(String s){
		if (queue.size()>10000000) return;
		if (all.contains(s)) return;
		all.add(s);
		queue.add(s);
	}
	public void setauthen(String s){
		authen=s;
	}
	public void setcookie(String s){
		cookie=s;
	}
	
	private CrawlerThread[] threads;
	public void run(){
		//sites.add("facebook");
		//sites.add("twitter");
		//sites.add("linkedin");
		//sites.add("tumblr");
		//sites.add("flickr");
		//sites.add("foursquare");
		//sites.add("instagram");
		//sites.add("googleplus");
		//sites.add("youtube");
		
		for (String s:sites){
			File site=new File(dir+"/"+s);
			if (!site.exists()) site.mkdir();
		}

		threads=new CrawlerThread[NThread];
		for (int i=0;i<threads.length;i++){
			threads[i]=new CrawlerThread(this,dir.getAbsolutePath(),i);
			threads[i].start();
		}
	}
	public String getNextUser(){
		try{if (queue.size()==0) return null;
		cnt++;
		System.out.println("Count = "+cnt+"\t Queue Size = "+queue.size());
		return queue.pop();
		}catch (Exception ex){
			return null;
		}
	}
}

