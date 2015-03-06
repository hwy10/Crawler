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


public class Worker extends Thread{
	private Client client;
	private Task task;
	private String wid;
	private int cnt;
	public String curStatus;
	
	public Worker(String wid,String task) {
		this.wid=wid;
		try{
			this.task=(Task)Class.forName("Tasks."+task).newInstance();
		}catch (Exception ex){
			System.err.println("Invalid Task Name");
		}
	}
	
	public void run(){
		cnt=0;
		for (;;)
		try{
			client=new Client();
			status();
			if (task.InitialCheck(wid,client)){
				System.out.println(wid+"\tInitial Passed");
				for (;task.run(this,client);cnt++)
					Thread.sleep(Config.restTime);
			}
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void status(){
		System.out.println(wid+"\t"+client.proxy+"\t"+curStatus+"\t#output\t"+cnt);
	}
}
