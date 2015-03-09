package Tasks;

import java.io.File;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.regex.Matcher;


import java.util.regex.Pattern;

import jdk.nashorn.internal.runtime.regexp.joni.MatcherFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import Util.NetworkConnect;
import BasicOps.FileOps;
import Crawler.Client;
import Crawler.Control;
import Crawler.Logger;
import Crawler.TaskSetting;
import Crawler.Config;
import Crawler.Task;
import Crawler.UserBank;
import Crawler.Worker;
import DBConnector.WeiboDB;

public class WeiboUser extends Task{
	private TaskSetting req;
	
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
		if (res.length()==0)
			UserBank.assignProxy("Weibo", username, client.proxy);
		return res;
	}

	@Override
	public String run(Worker worker, Client client) {
		try{
			WeiboDB conn=new WeiboDB();
			String oid=conn.getNextQueue("0","-1");
			conn.close();
			if (oid.length()==0) return "Queue Empty";
			worker.curStatus="ord:"+oid;
			
			String content="";
			for (int i=0;i<5;i++){
				content=client.getContent("http://weibo.cn/"+oid);
				if (content.contains("如果没有自动跳转")){
					Logger.add(worker.wid+"---"+oid+" not exist");
					conn=new WeiboDB();
					conn.updateTag(oid, "-2");
					conn.close();
					return "";
				}
				if (content.contains("加入新浪微博,分享新鲜的事")){
					if (i==4) return "Network Error";
					login(worker, client);
				}
			}

			Matcher matcher=Pattern.compile("/im/chat\\?uid=(\\d*)").matcher(content);
			
			if (!matcher.find()) {
				if (content.contains("为您解答使用问题")){
					UserBank.reportUser("Weibo", username);
					return "Restart Error #1";
				}
				System.out.println(content);
				Thread.sleep(1000000);
				return "Restart Error #2";
			}
			String uid=matcher.group(1);
			
			worker.curStatus=uid+"\t#";
			
			String displayname="";
			matcher=Pattern.compile("<title>(.*?)的微博</title>").matcher(content);
			if (matcher.find()) displayname=matcher.group(1);
			
			int followee=0;
			matcher=Pattern.compile("关注\\[(\\d*)\\]").matcher(content);
			if (matcher.find()) followee=Integer.valueOf(matcher.group(1));
			
			int follower=0;
			matcher=Pattern.compile("粉丝\\[(\\d*)\\]").matcher(content);
			if (matcher.find()) follower=Integer.valueOf(matcher.group(1));
			
			int weibo=0;
			matcher=Pattern.compile("微博\\[(\\d*)\\]").matcher(content);
			if (matcher.find()) weibo=Integer.valueOf(matcher.group(1));
			
			conn=new WeiboDB();
			conn.insertUser(uid, displayname, weibo, followee, follower);
			conn.close();
			
			String path="D:\\cxz\\rawdata\\weibo\\user\\";
			
			File dir=new File(path+uid);
			if (!dir.exists())
				dir.mkdir();
			FileOps.SaveFile(path+uid+"\\1", content);
			
			parseWeibo(uid,content);
			
			for (int i=2;i<20;i++){
				worker.curStatus+="#";
				File f=new File(path+uid+"\\"+i);
				if (f.exists()&&FileOps.LoadFilebyLine(f.getAbsolutePath()).size()>0)
					content=FileOps.LoadFilebyLine(f.getAbsolutePath()).get(0);
				else {
					content=client.getContent("http://weibo.cn/"+uid+"?page="+i);
					FileOps.SaveFile(path+uid+"\\"+i, content);
				}
				parseWeibo(uid,content);
				try{
					Thread.sleep(3000);
				}catch (Exception ex){}
			}
			conn=new WeiboDB();
			conn.updateTag(oid,"1");
			conn.close();
			Logger.add(worker.wid+"---Finished "+uid+"  "+displayname);
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
			String content=client.getContent("http://login.weibo.cn/login/?");
			if (content.length()==0) return "Network Error";
			for (int i=0;i<3;i++){
				worker.curStatus="login trial #"+(i+1);
				Matcher randMatcher=Pattern.compile("rand=(\\d*)").matcher(content);
				if (!randMatcher.find()) return "Weibo_Login Error #1";
				String rand=randMatcher.group(1);
				
				Matcher pidMatcher=Pattern.compile("password_\\d*").matcher(content);
				if (!pidMatcher.find()) return "Weibo_Login Error #2";
				String pid=pidMatcher.group(0);
				
				Matcher vkMatcher=Pattern.compile("name=\"vk\" value=\"(.*?)\"").matcher(content);
				if (!vkMatcher.find()) return "Weibo_Login Error #3";
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
				
				if (content.contains("验证码")){
					Logger.add(worker.wid+"---验证码");
					Matcher macher=Pattern.compile("<img src=\"(.*?)\" alt=\"请打开图片显示\"").matcher(content);
					if (!macher.find()) return "Weibo_Login Error #4";
					String imgURL=macher.group(1);
					macher=Pattern.compile("name=\"capId\" value=\"(.*?)\"").matcher(content);
					if (!macher.find()) return "Weibo_Login Error #5";
					String capId=macher.group(1);
					worker.curStatus="###请输入验证码";
					worker.XXX=imgURL;
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
				
				String url="http://login.weibo.cn/login/?rand="+rand+"&backURL=http%3A%2F%2Fweibo.cn&backTitle=%E6%89%8B%E6%9C%BA%E6%96%B0%E6%B5%AA%E7%BD%91&vt=4";
				content=client.sendPost(url, params);
				System.out.println(content);
				if (!content.contains("登录")) {
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
