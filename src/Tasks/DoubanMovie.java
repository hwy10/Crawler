package Tasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.params.CookiePolicy;

import BasicOps.FileOps;
import Crawler.Client;
import Crawler.Logger;
import Crawler.Task;
import Crawler.TaskSetting;
import Crawler.Worker;
import DBConnector.DoubanDB;

public class DoubanMovie extends Task{

	TaskSetting req;
	@Override
	public TaskSetting clientRequest() {
		return req;
	}

	@Override
	public boolean superInit(Worker worker) {
		req=new TaskSetting();
		req.proxyLimit=1;
		req.proxyType="";
		req.cookiePolicy=CookiePolicy.RFC_2109;
		return true;
	}

	@Override
	public String InitialCheck(Worker worker, Client client) {
		if (client.getContent("http://m.douban.com/").length()>0) return "";
		return "Network Error";
	}

	@Override
	public String run(Worker worker, Client client) {
		DoubanDB conn=new DoubanDB();
		int uid=conn.getNextUser("1","-2");
		conn.close();
		
		if (uid<0) return "Queue Empty";
		
		worker.setProgress(1);
		worker.updateStatus("user : "+uid);
		
		String dir="D:\\cxz\\rawdata\\douban\\userwatched\\"+uid+"\\";
		FileOps.createDir(dir);
		
		int tot=1000;
		for (int i=1;i<=tot;i++){
			if (i>1&&FileOps.exist(dir+i+".html")) continue;
			System.out.println(i);
			String content=client.getContent("http://m.douban.com/movie/people/"+uid+"/watched?page="+i);
			if (!content.contains("看过的的电影")) {
				FileOps.SaveFile("D:\\cxz\\rawdata\\douban\\userwatched\\_"+uid+"_"+i+".html", content);
				return "Network Error";
			}
			if (i==1){
				Matcher matcher=Pattern.compile("<span> 1/(\\d*) </span>").matcher(content);
				if (matcher.find()){
					tot=Integer.valueOf(matcher.group(1));
					worker.updateStatus("user : "+uid+" watched : "+tot);
				}
			}
			worker.setProgress(Math.max(1,(i*100)/tot));
			
			FileOps.SaveFile(dir+i+".html", content);
//			parseMovie(uid,content);
		}
		Logger.add("Worker-"+worker.wid+"---Finished "+uid+" ("+tot+")");
		
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
		// TODO Auto-generated method stub
		
	}
	
}
