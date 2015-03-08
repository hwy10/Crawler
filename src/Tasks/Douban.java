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

import Trash.ClientWrapper;
import Trash.DBconnector;
import BasicOps.FileOps;
import Crawler.Client;
import Crawler.Config;
import Crawler.TaskSetting;
import Crawler.Worker;

public class Douban extends Crawler.Task{
	private TaskSetting req;
	@Override
	public TaskSetting clientRequest() {
		if (req!=null) return req;
		req=new TaskSetting();
		req.proxyType="";
		req.cookiePolicy=CookiePolicy.RFC_2109;
		return req;
	}
	@Override
	public String InitialCheck(Worker worker,Client client) {
		return login(worker,client);
	}

	@Override
	public String run(Worker worker,Client client) {
		DBconnector conn=new DBconnector();
		int nxtId=conn.getNextUser();
		if (nxtId==-1) return "Douban Error #1";
		
		worker.curStatus="user_"+nxtId;
				
		String homepage=client.getContent("http://m.douban.com/people/"+nxtId+"/about");
		FileOps.SaveFile("D:\\cxz\\rawdata\\douban\\userhomepage\\"+nxtId, homepage.length()+homepage);

		LinkedList<Integer> friends=new LinkedList<Integer>();
		
		for (int i=1;;i++){
			worker.curStatus="user_"+nxtId+" friends page "+i;
			String cur=client.getContent(
					"http://m.douban.com/people/"+nxtId+"/contacts?page="+i);
			FileOps.createDir("D:\\cxz\\rawdata\\douban\\userfriends\\"+nxtId);
			FileOps.SaveFile("D:\\cxz\\rawdata\\douban\\userfriends\\"+nxtId+"\\"+i, cur);
			int ncur=0;
			if (cur.contains("你在豆瓣的注册密码")) return "Douban Error #2";
			Matcher matcher=Pattern.compile("<a href=\"/people/(.*?)/").matcher(cur);
			for (;matcher.find();){
				int id=Integer.valueOf(matcher.group(1));
				if (id==nxtId||id==122350491) continue;
				ncur++;
				friends.add(Integer.valueOf(matcher.group(1)));
			}
			if (ncur==0) break;
		}
		
		String username=extractUsername(homepage);
		if (username.equals("")) return "Douban Error #3";
		worker.curStatus="###user_"+nxtId+" #friends "+friends.size()+"   "+username;
		
		String location=extractLocation(homepage);
		String description=extractDescription(homepage);
		String displayName=extractDisplay(homepage);
		
		for (int i=0;i<friends.size();i++){
			conn.addFriend(nxtId,friends.get(i));
			conn.insertUser(friends.get(i));
		}
		conn.updateUser(nxtId,username,displayName,location,description,"1");
		
		conn.close();
		return "";
	}

	@Override
	public String login(Worker worker,Client client) {
		try{
			String content=client.getContent("http://m.douban.com/login");
			Matcher matcher=Pattern.compile("/captcha/(.*?)/").matcher(content);
			matcher.find();
			String capId=matcher.group(1);
			String capsol;
			String imgdir="http://m.douban.com/captcha/"+capId+"/?size=m";
			if (!client.saveImg(imgdir,"temp\\"+worker.wid+"imgcode.jpg")) return "Douban Error #5";
			capsol=com.cqz.dm.CodeReader.getImgCode("temp\\"+worker.wid+"imgcode.jpg", 3008);

			LinkedList<NameValuePair> params=new LinkedList<NameValuePair>();
			params.add(new BasicNameValuePair("form_email", ""));
			params.add(new BasicNameValuePair("form_password", ""));
			params.add(new BasicNameValuePair("captcha-solution", capsol));
			params.add(new BasicNameValuePair("captcha-id",capId));
			params.add(new BasicNameValuePair("user_login", "登录"));
			
			String res=client.sendPost("http://m.douban.com/login",params);
			
			if (res.contains("you should be redirected automatically.")) return "";
		}catch (Exception ex){
		}
		return "Douban Error #7";
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
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean superInit(Worker worker) {
		return true;
	}
}
