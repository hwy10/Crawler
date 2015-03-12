package Tasks;

import java.util.LinkedList;

import org.apache.http.client.params.CookiePolicy;

import Crawler.Client;
import Crawler.Task;
import Crawler.TaskSetting;
import Crawler.Worker;

public class DoubanUserFix extends Task{
	
	private static LinkedList<String> Q=new LinkedList<String>();

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
		return true;
	}

	@Override
	public String InitialCheck(Worker worker, Client client) {
		return "";
	}

	@Override
	public String run(Worker worker, Client client) {
		// TODO Auto-generated method stub
		return null;
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
