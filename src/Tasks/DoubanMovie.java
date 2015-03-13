package Tasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.params.CookiePolicy;

import BasicOps.FileOps;
import Crawler.Client;
import Crawler.Logger;
import Crawler.ProxyBank;
import Crawler.Task;
import Crawler.TaskSetting;
import Crawler.Worker;
import DBConnector.DoubanDB;

public class DoubanMovie extends Task{
	private int curId; 
	TaskSetting req;
	@Override
	public TaskSetting clientRequest() {
		return req;
	}

	@Override
	public boolean superInit(Worker worker) {
		req=new TaskSetting();
		req.proxyLimit=1;
		req.proxyType="local";
		req.cookiePolicy=CookiePolicy.RFC_2109;
		return true;
	}

	@Override
	public String InitialCheck(Worker worker, Client client) {
		if (!client.getContent("http://m.douban.com/").equals(Client.ERROR)) return "";
		return "Network Error";
	}

	@Override
	public String run(Worker worker, Client client) {
		DoubanDB conn=new DoubanDB();
		int uid=conn.getNextUser("2","2X");
		curId=uid;
		conn.close();
		
		if (uid<0) return "Queue Empty";
		
		worker.setProgress(1);
		worker.updateStatus("###user : "+uid);
		
		String dir="D:\\cxz\\rawdata\\douban\\userwatched\\"+uid+"\\";
		if (!FileOps.exist(dir))
			FileOps.createDir(dir);
		
		int tot=1000;
		for (int i=1;i<=tot;i++){
			if (i>1&&FileOps.exist(dir+i+".html")) continue;
			String content=client.getContent("http://m.douban.com/movie/people/"+uid+"/watched?page="+i);
			for (;!content.contains("看过的的电影");){
				System.out.println(content);
//				ProxyBank.releaseProxy(client.proxy);
				client=new Client(clientRequest());
				content=client.getContent("http://m.douban.com/movie/people/"+uid+"/watched?page="+i);
				worker.updateUI();
				try{Thread.sleep(3000);}catch (Exception ex){}
			}
			Logger.add("Page "+i+" Finished");
			if (i==1){
				Matcher matcher=Pattern.compile("<span> 1/(\\d*) </span>").matcher(content);
				if (matcher.find()){
					tot=Integer.valueOf(matcher.group(1));
					worker.updateStatus("###user : "+uid+" watched : "+tot);
				}
				else tot=1;
			}													
			worker.setProgress(Math.max(1,(i*100)/tot));
			
			FileOps.SaveFile(dir+i+".html", content);
//			try{
//				Thread.sleep(1000);
//			}catch (Exception ex){
//				ex.printStackTrace();
//			}
//			parseMovie(uid,content);
		}
		Logger.add("Worker-"+worker.wid+"---Finished "+uid+" ("+tot+")");
		conn=new DoubanDB();
		conn.updateTag(uid,"3","2X");
		return "";
	}
	
	public void parseMovie(int uid,String content){
		Matcher matcher=Pattern.compile("<div class=\"item\">([\\S\\s]*?)</div>").matcher(content);
		for (;matcher.find();){
			String cur=matcher.group(1);
			Matcher m=Pattern.compile("subject/(\\d*)/.*?>(.*?)</a>").matcher(cur);
			if (!m.find()) continue;
			int mid=Integer.valueOf(m.group(1));
			String name=m.group(2);
			m=Pattern.compile("<span>\\((\\d*)星\\)</span>").matcher(cur);
            int rate=-1;
            if (m.find()) rate=Integer.valueOf(m.group(1));
            m=Pattern.compile("标签: (.*)").matcher(cur);
            String tag="";
            if (m.find()) tag=m.group(1);
            m=Pattern.compile("短评: (.*)").matcher(cur);
            String review="";
            if (m.find()) review=m.group(1);
            m=Pattern.compile("<br>(\\d*-\\d*-\\d*)").matcher(cur);
            String date="";
            if (m.find())
            	date=m.group(1);
            
            DoubanDB conn=new DoubanDB();
            conn.insertMovie(mid, name);
            conn.insertUserWatched(uid, mid, date, tag, review, rate);
            conn.close();
		}
	}

	@Override
	public String login(Worker worker, Client client) {
		return null;
	}

	@Override
	public void releaseResources() {
	}
	@Override
	public void taskFail() {
		DoubanDB conn=new DoubanDB();
		conn.updateTag(curId,"1","-2");
		conn.close();
	}
	
}
