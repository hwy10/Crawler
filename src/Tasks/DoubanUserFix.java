package Tasks;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.rmi.server.UID;
import java.util.LinkedList;

import org.apache.http.client.params.CookiePolicy;

import com.mysql.jdbc.UpdatableResultSet;

import sun.misc.Lock;
import BasicOps.FileOps;
import Crawler.Client;
import Crawler.Logger;
import Crawler.ProxyBank;
import Crawler.Task;
import Crawler.TaskSetting;
import Crawler.Worker;

public class DoubanUserFix extends Task{
	
	private static LinkedList<String> Q=new LinkedList<String>();
	private static boolean init;

	@Override
	public TaskSetting clientRequest() {
		TaskSetting req=new TaskSetting();
		req.proxyLimit=1;
		req.proxyType="";
		req.cookiePolicy=CookiePolicy.RFC_2109;
		return req;
	}
	
	@Override
	public boolean superInit(Worker worker) {
		synchronized (Q) {
			if (!init){
				System.out.println("Initializing");
				String []s=(new File("D:\\cxz\\rawdata\\douban\\userhomepage\\")).list();
				for (String id:s)
					Q.add(id);
				init=true;
			}
		}
		return true;
	}

	@Override
	public String InitialCheck(Worker worker, Client client) {
		return "";
	}

	private String curid;
	@Override
	public String run(Worker worker, Client client) {
		synchronized (Q) {
			curid=Q.getFirst();
			Q.removeFirst();
		}
		worker.updateStatus("###updating "+curid);
		String dir="D:\\cxz\\rawdata\\douban\\userhomepage\\"+curid;
		String content=FileOps.LoadFile(dir,"UTF-8");
		if (content.contains("返回他/她的豆瓣")) return "";
		content=FileOps.LoadFile(dir,"GBK");
		if (content.contains("返回他/她的豆瓣")) return "";
		worker.updateStatus("###Retrying "+curid);
		Logger.add("Retrying "+curid);
		for (;;){
			content=client.getContent("http://m.douban.com/people/"+curid+"/about");
			if (content.contains("返回他/她的豆瓣")) break;
			client=new Client(clientRequest());
			worker.updateUI();
		}
		FileOps.SaveFile(dir, content);
		
		Logger.add(curid+" Finished");
		worker.updateStatus("");
		
		return "";
	}

	@Override
	public String login(Worker worker, Client client) {
		return null;
	}

	@Override
	public void releaseResources() {
	}

}
