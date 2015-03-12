package Tasks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.message.BasicNameValuePair;

import BasicOps.FileOps;
import Crawler.Client;
import Crawler.Config;
import Crawler.Logger;
import Crawler.ProxyBank;
import Crawler.TaskSetting;
import Crawler.Worker;
import DBConnector.DoubanDB;
import DBConnector.WeiboDB;

public class DoubanUser extends Crawler.Task{
	private TaskSetting req;
	@Override
	public TaskSetting clientRequest() {
		return req;
	}
	@Override
	public String InitialCheck(Worker worker,Client client) {
		return login(worker,client);
	}

	@Override
	public String run(Worker worker,Client client) {
		worker.setProgress(1);

		DoubanDB conn=new DoubanDB();
		int nxtId=conn.getNextUser("0","-1");
		conn.close();
		if (nxtId==-1) return "Queue Empty";
		
		worker.updateStatus("user_"+nxtId);
				
		String homepage=client.getContent("http://m.douban.com/people/"+nxtId+"/about");
		FileOps.SaveFile("D:\\cxz\\rawdata\\douban\\userhomepage\\"+nxtId, homepage.length()+homepage);
		
		String username=extractUsername(homepage);
		if (username.equals("")) {
			Logger.add("Worker-"+worker.wid+"---Unknown Username");
			return "";
		}

		LinkedList<Integer> friends=new LinkedList<Integer>();
		
		int tot=1000;
		for (int i=1;;i++){
			worker.updateStatus("user_"+nxtId+" friends page "+i);
			String cur=client.getContent(
					"http://m.douban.com/people/"+nxtId+"/contacts?page="+i);
			if (i==1){
				Matcher matcher=Pattern.compile("<span> 1/(\\d*) </span>").matcher(cur);
				if (matcher.find())
					tot=Integer.valueOf(matcher.group(1));
				FileOps.createDir("D:\\cxz\\rawdata\\douban\\userfriends\\"+nxtId);
			}
			worker.setProgress(Math.max(1, (i*100)/tot));
			FileOps.SaveFile("D:\\cxz\\rawdata\\douban\\userfriends\\"+nxtId+"\\"+i, cur);
			int ncur=0;
			if (cur.contains("你在豆瓣的注册密码")) return "Network Error";
			Matcher matcher=Pattern.compile("<a href=\"/people/(.*?)/").matcher(cur);
			for (;matcher.find();){
				int id=Integer.valueOf(matcher.group(1));
				if (id==nxtId||id==122350491) continue;
				ncur++;
				friends.add(Integer.valueOf(matcher.group(1)));
			}
			if (ncur==0) break;
		}
		
		worker.updateStatus("###user_"+nxtId+" #friends "+friends.size()+"   "+username);
		
		String location=extractLocation(homepage);
		String description=extractDescription(homepage);
		String displayName=extractDisplay(homepage);
		
		addWeiboLink(nxtId,description);
		
		conn=new DoubanDB();
		for (int i=0;i<friends.size();i++){
			conn.addFriend(nxtId,friends.get(i));
			conn.insertUser(friends.get(i));
		}
		conn.updateUser(nxtId,username,displayName,location,description,"1");
		
		conn.close();
		Logger.add("Worker-"+worker.wid+"---Finished: "+username+" "+displayName);
		return "";
	}

	@Override
	public String login(Worker worker,Client client) {
		try{
			String content=client.getContent("http://m.douban.com/login");
			if (content.equals(Client.ERROR)) return "Network Error";
			Matcher matcher=Pattern.compile("/captcha/(.*?)/").matcher(content);
			String capId="";
			if (matcher.find())
				capId=matcher.group(1);
			else {
				System.out.println(content);
				return "Network Error";
			}
			String capsol[];
			String imgdir="http://m.douban.com/captcha/"+capId+"/?size=m";
			if (!client.saveImg(imgdir,"temp\\"+worker.wid+"imgcode.jpg")) return "Network Error";
			capsol=com.cqz.dm.CodeReader.getImgCode("temp\\"+worker.wid+"imgcode.jpg", 3008);

			LinkedList<NameValuePair> params=new LinkedList<NameValuePair>();
			params.add(new BasicNameValuePair("form_email", "hijack2004@126.com"));
			params.add(new BasicNameValuePair("form_password", "cat12321"));
			params.add(new BasicNameValuePair("captcha-solution", capsol[1]));
			params.add(new BasicNameValuePair("captcha-id",capId));
			params.add(new BasicNameValuePair("user_login", "登录"));
			
			String res=client.sendPost("http://m.douban.com/login",params);
			
			if (res.contains("you should be redirected automatically.")) return "";
			else {
				Logger.tofile(capsol[0]);
				return "Network Error";
			}
		}catch (Exception ex){
			ex.printStackTrace();
			return ex.toString();
		}
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
	@Override
	public void releaseResources() {
	}
	@Override
	public boolean superInit(Worker worker) {
		req=new TaskSetting();
		req.proxyType="";
		req.cookiePolicy=CookiePolicy.RFC_2109;
		req.proxyLimit=3;
		return true;
	}
	
	public void addWeiboLink(int uid,String des){
		String weiboId=null;
		Matcher wlink=Pattern.compile("weibo.com/(\\w*)").matcher(des);
		if (wlink.find()) weiboId=wlink.group(1);
		Matcher wulink=Pattern.compile("weibo.com/./(\\w*)").matcher(des);
		if (wulink.find()) weiboId=wulink.group(1);
		Matcher tlink=Pattern.compile(".sina.com.cn/(\\w*)").matcher(des);
		if (tlink.find()) weiboId=tlink.group(1);
		Matcher blink=Pattern.compile(".sina.com.cn/./(\\w*)").matcher(des);
		if (blink.find()) weiboId=blink.group(1);
		wlink=Pattern.compile("sina.com/(\\w*)").matcher(des);
		if (wlink.find()) weiboId=wlink.group(1);
		if (weiboId!=null){
			WeiboDB weiboconn=new WeiboDB();
			weiboconn.addQueue(weiboId);
			weiboconn.close();
			DoubanDB conn=new DoubanDB();
			conn.updateWeiboId(weiboId,uid);
			conn.close();
		}
	}
}
