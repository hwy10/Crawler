package Crawler;
import java.util.Random;

import javax.swing.text.AbstractDocument.Content;

import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;

import sun.misc.Lock;

import com.sun.org.apache.bcel.internal.generic.PUTSTATIC;
import com.sun.org.apache.xerces.internal.impl.xpath.XPath.Step;

import DBConnector.WeiboDB;

public class Worker extends Thread{
	public Client client;
	public Task task;
	public String taskName;
	public int wid;
	public int cnt;
	public String curStatus;
	public String XXX;
	public int progress=-1;
	public Control core;
	public boolean kill=false;
	
	public static String UILock="lock";
	
	public Worker(Control core,int wid) {
		this.core=core;
		this.wid=wid;
		updateUI();
	}
	
	public void setTask(String task){
		try{
			taskName=task;
			this.task=(Task)Class.forName("Tasks."+task).newInstance();
			updateUI();
		}catch (Exception ex){
		}		
	}
	
	public void updateUI(){
		synchronized (UILock) {
			for (int i=0;i<core.window.workerTableModel.getRowCount();i++)
				if ((Integer)core.window.workerTableModel.getValueAt(i, 0)==wid){
					if (isAlive()&&!kill){
						if (client!=null)
							core.window.workerTableModel.setValueAt(client.proxy, i, 1);
						else core.window.workerTableModel.setValueAt("", i, 1);
						if (task!=null)
							core.window.workerTableModel.setValueAt(task.username, i, 2);
						else core.window.workerTableModel.setValueAt("", i, 2);
						core.window.workerTableModel.setValueAt(taskName, i, 3);
						core.window.workerTableModel.setValueAt(curStatus, i, 4);
						core.window.workerTableModel.setValueAt(progress, i, 5);
						core.window.workerTableModel.setValueAt(cnt, i, 6);
						if (isAlive())
							core.window.workerTableModel.setValueAt(i+"-Stop", i, 7);
						else core.window.workerTableModel.setValueAt(i+"-Start",i, 7);
					}
					else {
						for (int j=1;j<7;j++)
							core.window.workerTableModel.setValueAt("", i, j);
						core.window.workerTableModel.setValueAt(-1, i, 5);
						core.window.workerTableModel.setValueAt(i+"-Start", i, 7);
						core.window.updateGUI();
					}
					return;
				}
			Object[] content=new Object[8];
			content[0]=wid;
			core.window.workerTableModel.addRow(content);
		}
		updateUI();
	}
	
	public void updateStatus(String s){
		curStatus=s;
		updateUI();
	}
	public void setProgress(int p){
		progress=p;
		updateUI();
	}
	
	public void run(){
		cnt=0;
		if (task==null) {
			updateStatus("Invalid Task Name");
			Logger.add("Worker-"+wid+"---Invalid Task Name");
			return;
		}
		if (!task.superInit(this)){
			Logger.add("Worker-"+wid+"---Super Init Failed");
		}
		
		TaskSetting setting=task.clientRequest();
		if (setting==null) {
			updateStatus("Invalid Task Setting");
			Logger.add("Worker-"+wid+"---Invalid Task Setting");
			return;
		}
		
		Logger.add("Worker-"+wid+"---start");
		updateStatus("");
		
		int qcnt=0;
		client=new Client(setting);
		if (client.proxy.equals("No_Available_Proxy")) return;
		updateUI();
		for (;;){
			if (kill){Logger.add("Kill : "+wid);release();return;}
			try{
				setProgress(-1);
				String message=task.InitialCheck(this,client);
				if (message.length()==0){
					updateStatus("Initial Passed");
					Logger.add("Worker-"+wid+"---Initial Passed");
					for (;;){
						if (kill){Logger.add("Kill : "+wid);release();return;}
						message=task.run(this,client);
						if (message.equals("Queue Empty")){
							updateStatus("###Waiting for Jobs");
							Thread.sleep(1000*60);
							message="";
						}
						if (message.length()>0) break;
						cnt++;
						updateUI();
						try{
							Thread.sleep(1000);
						}catch (Exception ex){
							ex.printStackTrace();
						}
					}
				}
				
				Logger.add("Worker-"+wid+"---"+message);
				
				if (message.equals("Network Error")){
					task.taskFail();
					if (qcnt==0) ProxyBank.report(client.proxy);
					updateStatus("");
					
					ProxyBank.releaseProxy(client.proxy);
					client=new Client(setting);
					if (client.proxy.equals("No_Available_Proxy")) return;
					updateUI();
				} else if (message.startsWith("Restart")){
				} else break;
			}catch (Exception ex) {
				ex.printStackTrace();
//				Logger.add(wid+"---"+ex.toString());
				break;
			}
			try{
				Thread.sleep(Config.restTime);
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
		release();
		updateUI();
	}
	
	public void release(){
		if (task!=null){
			task.taskFail();
			task.releaseResources();
		}
		if (client!=null) ProxyBank.releaseProxy(client.proxy);
		client=null;
		progress=-1;
		curStatus="";
	}
}
