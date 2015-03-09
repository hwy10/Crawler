package Crawler;
import java.util.Random;

import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;

import com.sun.org.apache.xerces.internal.impl.xpath.XPath.Step;

import DBConnector.WeiboDB;

public class Worker extends Thread{
	public Client client;
	public Task task;
	public String taskName;
	public String wid;
	public int cnt;
	public String curStatus;
	public String XXX;
	
	public Worker(String wid,String task) {
		this.wid=wid;
		try{
			taskName=task;
			this.task=(Task)Class.forName("Tasks."+task).newInstance();
		}catch (Exception ex){
		}
	}
	
	public void run(){
		cnt=0;
		if (task==null) {
			this.curStatus="Invalid Task Name";
			Logger.add(wid+"---Invalid Task Name");
			return;
		}
		if (!task.superInit(this)){
			Logger.add(wid+"---Super Init Failed");
		}
		
		TaskSetting setting=task.clientRequest();
		if (setting==null) {
			this.curStatus="Invalid Task Setting";
			Logger.add(wid+"---Invalid Task Setting");
			return;
		}
		
		Logger.add(wid+"---start");
		curStatus="";
		
		int qcnt=0;
		client=new Client(setting);
		if (client.proxy.equals("No_Available_Proxy")) return;
		
		for (;;){
			try{
				String message=task.InitialCheck(this,client);
				if (message.length()==0){
					curStatus="Initial Passed";
					Logger.add(wid+"---Initial Passed");
					for (;;cnt++){
						message=task.run(this,client);
						if (message.equals("Queue Empty")){
							curStatus="###Waiting for Jobs";
							Thread.sleep(1000*60);
						}
						if (message.length()>0) break;
						try{
							Thread.sleep(1000);
						}catch (Exception ex){}
					}
				}
				
				Logger.add(wid+"---"+message);
				
				if (message.equals("Network Error")){
					if (qcnt==0) ProxyBank.report(client.proxy);
					curStatus="";
					
					ProxyBank.releaseProxy(client.proxy);
					client=new Client(setting);
					if (client.proxy.equals("No_Available_Proxy")) return;
				} else if (message.startsWith("Restart")){
				} else break;
			}catch (Exception ex) {
				ex.printStackTrace();
//				Logger.add(wid+"---"+ex.toString());
				break;
			}
			try{
				Thread.sleep(Config.restTime);
			}catch (Exception ex){}
		}
		task.releaseResources();
	}
}
