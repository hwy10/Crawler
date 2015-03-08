package Trash;

import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BasicOps.FileOps;
import Util.NetworkConnect;

public class Worker extends Thread {
	private ClientWrapper client;
	public String name;
	@Override
	public void run(){
		try{
			Random random=new Random();
			DBconnector conn=new DBconnector();

			client=new ClientWrapper();
			client.init();
			
			for (;;){
				int nxtId=conn.getNextUser();
				if (nxtId==-1) break;
				if (!updateUser(nxtId))break;
				Thread.sleep(random.nextInt(2000)+1000);
			}
			conn.close();
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public boolean updateUser(int nxtId){
		
		System.out.println(name+" updateUser "+nxtId);
		String homepage=client.getContent("http://m.douban.com/people/"+nxtId+"/about");
		FileOps.SaveFile("D:\\cxz\\rawdata\\douban\\userhomepage\\"+nxtId, homepage.length()+homepage);

		LinkedList<Integer> friends=new LinkedList<Integer>();
		
		for (int i=1;;i++){
			String cur=client.getContent(
					"http://m.douban.com/people/"+nxtId+"/contacts?page="+i);
			FileOps.createDir("D:\\cxz\\rawdata\\douban\\userfriends\\"+nxtId);
			FileOps.SaveFile("D:\\cxz\\rawdata\\douban\\userfriends\\"+nxtId+"\\"+i, cur);
			int ncur=0;
			if (cur.contains("你在豆瓣的注册密码")) return false;
			Matcher matcher=Pattern.compile("<a href=\"/people/(.*?)/").matcher(cur);
			for (;matcher.find();){
				int id=Integer.valueOf(matcher.group(1));
				if (id==nxtId||id==122350491) continue;
				ncur++;
				friends.add(Integer.valueOf(matcher.group(1)));
			}
			if (ncur==0) break;
		}
		
		System.out.println(name+" "+"Extracting Infos");
		
		String username=extractUsername(homepage);
		System.out.println(name+"\tusername\t"+username);
		if (username.equals("")) return false;
		String location=extractLocation(homepage);
		String description=extractDescription(homepage);
		String displayName=extractDisplay(homepage);
		
		System.out.println(name+"\tlocation\t"+location);
		System.out.println(name+"\tdescription\t"+description);
		System.out.println(name+"\tdisplay\t"+displayName);
		System.out.println(name+"\t"+"Friend Count\t"+friends.size());
		DBconnector conn=new DBconnector();
		
		for (int i=0;i<friends.size();i++){
			conn.addFriend(nxtId,friends.get(i));
			conn.insertUser(friends.get(i));
		}
		conn.updateUser(nxtId,username,displayName,location,description,"1");
		
		conn.close();
		
		System.out.println(name+" "+"User Finished");
		return true;
	}
	
	private String extractUsername(String homepage){
		Matcher matcher=Pattern.compile("<span>id.*?</span>(.*?)<br />").matcher(homepage);
		if (matcher.find())
			return matcher.group(1).trim();
		return "";
	}
	private String extractDisplay(String homepage){
		Matcher matcher=Pattern.compile("class=\"founder\">(.*?)</a>").matcher(homepage);
		if (matcher.find())
			return matcher.group(1).trim();
		return "";
	}
	private String extractLocation(String homepage){
		Matcher matcher=Pattern.compile("<span>常居地：</span>(.*?)<br />").matcher(homepage);
		if (matcher.find())
			return matcher.group(1).trim();
		return "";
	}
	private String extractDescription(String homepage){
		Matcher matcher=Pattern.compile("<div class=\"intro\">([\\S\\s]*?)</div>").matcher(homepage);
		if (matcher.find())
			return matcher.group(1).trim();
		return "";
	}
}