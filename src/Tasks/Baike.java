package Tasks;

import java.io.File;




import BasicOps.FileOps;
import Crawler.Client;
import Crawler.Logger;
import Crawler.Task;
import Crawler.TaskSetting;
import Crawler.UserBank;
import Crawler.Worker;

public class Baike extends Task {
	private TaskSetting req;
	public static String lock="lock";
	public static int nxtId=1;
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
			id=nxtId;
			nxtId++;
		}
		worker.curStatus="Downloading "+id;
		Logger.add(worker.wid+"---Downloading "+id);
		File file=new File("baike");
		if (!file.exists()) file.mkdir();
		file=new File("baike/"+id+".htm");
		if (file.exists()) return "";
		String content=client.getContent("http://baike.baidu.com/view/"+id+".htm");
		if (content.contains("Access Denied")) return "Network Error";
		if (content.length()<10240) return "Network Error";
		
		FileOps.SaveFile(file.getAbsolutePath(), content);
		try{
			Thread.sleep(5000);
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
