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
import org.apache.http.message.BasicNameValuePair;

import BasicOps.FileOps;
import Crawler.Client;
import Crawler.Config;
import Crawler.Worker;
import Douban.ClientWrapper;
import Douban.DBconnector;

public class Douban extends Crawler.Task{

	@Override
	public boolean InitialCheck(String wid,Client client) {
		return login(wid,client);
	}

	@Override
	public boolean run(Worker worker,Client client) {
		DBconnector conn=new DBconnector();
		int nxtId=conn.getNextUser();
		if (nxtId==-1) return false;
		
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
		
		String username=extractUsername(homepage);
		if (username.equals("")) return false;
		worker.curStatus="user_"+nxtId+" #friends "+friends.size()+"\t"+username;
		
		String location=extractLocation(homepage);
		String description=extractDescription(homepage);
		String displayName=extractDisplay(homepage);
		
		for (int i=0;i<friends.size();i++){
			conn.addFriend(nxtId,friends.get(i));
			conn.insertUser(friends.get(i));
		}
		conn.updateUser(nxtId,username,displayName,location,description,"1");
		
		conn.close();
		return true;
	}

	@Override
	public boolean login(String wid,Client client) {
		try{
			String content=client.getContent("http://m.douban.com/login");
			Matcher matcher=Pattern.compile("/captcha/(.*?)/").matcher(content);
			matcher.find();
			String capId=matcher.group(1);
			String capsol;
			String imgdir="http://m.douban.com/captcha/"+capId+"/?size=m";
			if (!client.saveImg(imgdir,"temp\\"+wid+"imgcode.jpg")) return false;
			capsol=com.cqz.dm.CodeReader.getImgCode("temp\\"+wid+"imgcode.jpg", 3008);

			LinkedList<NameValuePair> params=new LinkedList<NameValuePair>();
			params.add(new BasicNameValuePair("form_email", Config.username));
			params.add(new BasicNameValuePair("form_password", Config.password));
			params.add(new BasicNameValuePair("captcha-solution", capsol));
			params.add(new BasicNameValuePair("captcha-id",capId));
			params.add(new BasicNameValuePair("user_login", "登录"));
			
			String res=client.sendPost("http://m.douban.com/login",params);
			
			if (res.contains("you should be redirected automatically.")) return true;
		}catch (Exception ex){
		}
		return false;
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
