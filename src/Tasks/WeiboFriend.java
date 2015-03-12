package Tasks;

import java.io.File;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.regex.Matcher;


import java.util.regex.Pattern;

import javax.print.DocFlavor.STRING;

import jdk.nashorn.internal.runtime.regexp.joni.MatcherFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import Util.NetworkConnect;
import BasicOps.FileOps;
import Crawler.Client;
import Crawler.Control;
import Crawler.Logger;
import Crawler.ProxyBank;
import Crawler.TaskSetting;
import Crawler.Config;
import Crawler.Task;
import Crawler.UserBank;
import Crawler.Worker;
import DBConnector.WeiboDB;

public class WeiboFriend extends Task{
	private TaskSetting req;
	private static String lauid="2072423117";
	
	@Override
	public TaskSetting clientRequest() {
		return req;
	}
	@Override
	public boolean superInit(Worker worker) {
		String user=UserBank.getUser("Weibo", 1);
		if (user.split("::").length!=2) return false;
		username=user.split("::")[0];
		password=user.split("::")[1];
		
		req=new TaskSetting();
		req.proxyType=UserBank.getProxy("Weibo", username);
		
		return true;
	}
	
	@Override
	public String InitialCheck(Worker worker, Client client) {
		String res=login(worker, client);
		if (res.length()==0){
			UserBank.assignProxy("Weibo", username, client.proxy);
			ProxyBank.reportForce(client.proxy);
		}
		return res;
	}

	@Override
	public String run(Worker worker, Client client) {
		try{
			worker.setProgress(1);
			
			WeiboDB conn=new WeiboDB();
			String uid=conn.getNextUser("1","1X");
			if (uid.length()>0) lauid=uid;
			conn.close();
			if (uid.length()==0) return "Queue Empty";
			worker.updateStatus("ID : "+uid);
			
			String dir="D:\\cxz\\rawdata\\weibo\\friends\\"+uid+"\\";
			if (!FileOps.exist(dir))
				FileOps.createDir(dir);
			
			int tot=1000;
			for (int i=1;i<=tot;i++){
				if (i>1&&FileOps.exist(dir+i)) continue;
				String content="";
				
				for (int k=3;k>0;k--){
					content=client.getContent("http://weibo.cn/"+uid+"/follow?page="+i);
					if (i==1){
						Matcher matcher=Pattern.compile("1/(\\d*)页").matcher(content);
						if (matcher.find())
							tot=Integer.valueOf(matcher.group(1));
					}
					worker.setProgress(Math.max(1, i*100/tot));
					worker.updateStatus("ID : "+uid +", Page : "+tot);
					
					int cnt=0;
					
					conn=new WeiboDB();
					Matcher matcher=Pattern.compile
							("<td valign=\"top\"><a href=\"http://weibo.cn/(.*?)\">(.*?)</a>(.*?)粉丝(\\d*)人<br/><a href=\"http://weibo.cn/attention/add\\?uid=(\\d*)")
							.matcher(content);
					for (;matcher.find();cnt++){
						String displayname=matcher.group(2);
						int follower=Integer.valueOf(matcher.group(4));
						String id=matcher.group(5);
						conn.insertUser(id, displayname, 0, 0, follower, "0");
					}
					conn.close();

					if (cnt>0) break;
					if (k==1){
						System.out.println(content);
						UserBank.reportUser("Weibo", username);
						return "Restart";
					}
					Thread.sleep(5000);
				}
				FileOps.SaveFile(dir+i, content);
				
				try{
					Thread.sleep(5000);
				}catch (Exception ex){}
			}
			
			Logger.add("Worker-"+worker.wid+"---Finished "+uid);
			conn=new WeiboDB();
			conn.updateUserTag(uid,"2","1X");
			conn.close();
			return "";
		}catch (Exception ex){
			return ex.toString();
		}
	}
	
	private void parseWeibo(String uid,String content){
		WeiboDB conn=new WeiboDB();
		try{
			Matcher matcher=Pattern.compile("<div class=\"c\" id=\"(.*?)\">([\\S\\s]*?)<div class=\"s\"></div>").matcher(content);
			for (;matcher.find();){
				conn.insertWeibo(uid, matcher.group(1), matcher.group(2));
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
		conn.close();
	}
	
	@Override
	public String login(Worker worker, Client client) {
		try{
			String content=client.getContent("https://login.weibo.cn/login/?");
			boolean release=false;
			System.out.println(content);
			if (content.equals(Client.ERROR)) {
				if (release)UserBank.releaseProxy("Weibo",username);
				req.proxyType="";
				return "Network Error";
			}
			for (int i=0;i<3;i++){
				worker.updateStatus("login trial #"+(i+1));
				Matcher randMatcher=Pattern.compile("rand=(\\d*)").matcher(content);
				if (!randMatcher.find()) {
					if (release)UserBank.releaseProxy("Weibo",username);
					req.proxyType="";
					return "Network Error";
				}
				String rand=randMatcher.group(1);
				
				Matcher pidMatcher=Pattern.compile("password_\\d*").matcher(content);
				if (!pidMatcher.find()) {
					if (release)UserBank.releaseProxy("Weibo",username);
					req.proxyType="";
					return "Network Error";
				}
				String pid=pidMatcher.group(0);
				
				Matcher vkMatcher=Pattern.compile("name=\"vk\" value=\"(.*?)\"").matcher(content);
				if (!vkMatcher.find()) {
					UserBank.releaseProxy("Weibo",username);
					req.proxyType="";
					return "Network Error";
				}
				String vk=vkMatcher.group(1);
				
				LinkedList<NameValuePair> params=new LinkedList<NameValuePair>();
				params.add(new BasicNameValuePair("mobile", username));
				params.add(new BasicNameValuePair(pid, password));
				params.add(new BasicNameValuePair("remember", "on"));
				params.add(new BasicNameValuePair("backURL", "http://weibo.cn"));
				params.add(new BasicNameValuePair("backTitle", "手机新浪网"));
				params.add(new BasicNameValuePair("tryCount", ""));
				params.add(new BasicNameValuePair("vk", vk));
				params.add(new BasicNameValuePair("submit", "登陆"));
//				params.add(new BasicNameValuePair("revalid", "2"));
//				params.add(new BasicNameValuePair("ns", "1"));
				
				if (content.contains("验证码")){
					Logger.add(worker.wid+"---验证码");
//					Logger.add(worker.wid+"---Report User "+username);
//					UserBank.reportUser("Weibo", username);
//					if (superInit(worker)) return "Restart";
//					else return "No Available Account";
					Matcher macher=Pattern.compile("<img src=\"(.*?)\" alt=\"请打开图片显示\"").matcher(content);
					if (!macher.find()) return "Weibo_Login Error #4";
					String imgURL=macher.group(1);
					macher=Pattern.compile("name=\"capId\" value=\"(.*?)\"").matcher(content);
					if (!macher.find()) return "Weibo_Login Error #5";
					String capId=macher.group(1);
					worker.updateStatus(imgURL);
					for (;;){
						try{
							Thread.sleep(3000);
						}catch (Exception ex){}
						if (Control.captcha.containsKey(capId)){
							params.add(new BasicNameValuePair("capId", capId));
							System.out.println(capId);
							System.out.println(URLEncoder.encode(Control.captcha.get(capId)));
							params.add(new BasicNameValuePair("code", URLEncoder.encode(Control.captcha.get(capId))));
							worker.XXX="";
							break;
						}
					}
				}
				
				String url="https://login.weibo.cn/login/?rand="+rand+"&backURL=http%3A%2F%2Fweibo.cn&backTitle=手机新浪网&vt=4";
				content=client.sendPost(url, params);
				System.out.println("Login Return : "+content);
				String test=client.getContent("http://weibo.cn/"+lauid+"/follow?page=1");
				if (!test.contains("登录")&&!test.equals(Client.ERROR)) {
					return "";
				}
			}
		}catch (Exception ex){
			return ex.toString();
		}
		return "WTF";
	}

	@Override
	public void releaseResources() {
		if (username!=null) UserBank.releaseUser("Weibo", username);
	}
}
