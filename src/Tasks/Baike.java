package Tasks;

import java.io.File;





import java.util.LinkedList;

import BasicOps.FileOps;
import Crawler.Client;
import Crawler.Logger;
import Crawler.ProxyBank;
import Crawler.Task;
import Crawler.TaskSetting;
import Crawler.UserBank;
import Crawler.Worker;

public class Baike extends Task {
	private TaskSetting req;
	public static String lock="lock";
	public static int nxtId=1;
	public static LinkedList<Integer> Q=new LinkedList<Integer>();
	@Override
	public TaskSetting clientRequest() {
		return req;
	}

	@Override
	public boolean superInit(Worker worker) {
		req=new TaskSetting();
		req.proxyType="";
		req.proxyLimit=3;
		return true;
	}

	@Override
	public String InitialCheck(Worker worker, Client client) {
		if (client.getContent("http://www.baidu.com/").length()>0) return "";
		return "Network Error";
	}

	@Override
	public String run(Worker worker, Client client) {
		int id;
		synchronized (lock) {
			if (Q.size()>0){
				id=Q.getFirst();
				Q.removeFirst();
			}
			else {
				id=nxtId;
				nxtId++;
			}
		}
		worker.curStatus="###Downloading "+id;
		Logger.add(worker.wid+"---Downloading "+id+" Queue("+Q.size()+")");
		String dirName = "E:/Baidu/"+((id-1)/50000*50000 + 1)+"-"+((id-1)/50000*50000 + 50000);
		File file=new File(dirName);
		if (!file.exists()) file.mkdir();
		file=new File(dirName+"/"+id+".htm");
		if (file.exists()) {
			LinkedList<String> s=FileOps.LoadFilebyLine(file.getAbsolutePath());
			boolean isgood=false;
			if (s.size()>0){
				String content=s.get(0);
				if (content.contains(">收藏</span>") || 
						content.contains("抱歉，您所访问的页面不存在") ||
						content.contains("data-title=\"编辑\"") ||
						content.contains(">多义词</a>")) isgood = true;
			}
			if (isgood) return "";
			Logger.add("Retrying "+id);
		}
		String content=client.getContent("http://baike.baidu.com/view/"+id+".htm");
		boolean flag = false; 
		if (content.contains(">收藏</span>") || 
			content.contains("抱歉，您所访问的页面不存在") ||
			content.contains("data-title=\"编辑\"") ||
			content.contains(">多义词</a>")) flag = true;
		if (flag == false) {
			synchronized(lock){
				Q.add(id);
			}
			FileOps.SaveFile(dirName+"/_"+id+".htm", content);
			return "Network Error";
		}
		ProxyBank.reportGood(client.proxy);
		FileOps.SaveFile(file.getAbsolutePath(), content);
		File badfile=new File(dirName+"/_"+id+".htm");
		if (badfile.exists()) badfile.delete();
		try{
			Thread.sleep(20000);
		}catch (Exception ex){}
		return "";
	}

	@Override
	public String login(Worker worker, Client client) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void releaseResources() {
		// TODO Auto-generated method stub
		
	}
	
}
