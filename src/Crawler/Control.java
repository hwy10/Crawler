package Crawler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cqz.dm.UUAPI;
import com.sun.java.swing.plaf.windows.resources.windows;

import Gui.MainWindow;

public class Control {
	private MainWindow window;
	
	public static LinkedList<Worker> workers=new LinkedList<Worker>();
	public static HashMap<String, String>captcha=new HashMap<String, String>();
	
	public void assignGUI(MainWindow window){
		this.window=window;
	}
	
	public void init(){
		
//		UUAPI.reportError("463814947");
		
		ProxyBank.loadProxies();
		ProxyBank.proxyLoader();
		UserBank.load();
		
		updateNWorker(1);
		
		Guard guard=new Guard(this);guard.start();
	}
	
	public void execute(String command){
		try{
			Logger.add(command);
			String [] cmd=command.split(" ");
			if (cmd[0].startsWith("http")){
				Matcher matcher=Pattern.compile("cpt=(.*)").matcher(cmd[0]);
				if (!matcher.find()) return;
				captcha.put(matcher.group(1), cmd[1]);
			}
			if (cmd[0].equals("run")){
				String []sep=cmd[1].split("-");
				if (sep.length==1)
					startWorker(Integer.valueOf(sep[0]));
				else
					for (int i=Integer.valueOf(sep[0]);i<Integer.valueOf(sep[1]);i++)
						startWorker(i);
			}
			if (cmd[0].equals("stop")){
				String []sep=cmd[1].split("-");
				if (sep.length==1)
					stopWorker(Integer.valueOf(sep[0]));
				else
					for (int i=Integer.valueOf(sep[0]);i<Integer.valueOf(sep[1]);i++)
						stopWorker(i);
			}
			if (cmd[0].equals("set")){
				if (cmd[1].equals("task")) Config.Task=cmd[2];
				if (cmd[1].equals("nworker"))
					updateNWorker(Integer.valueOf(cmd[2]));
				if (cmd[1].equals("restTime"))
					Config.restTime=Integer.valueOf(cmd[2]);
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
		window.updateGUI();
	}
	
	public void updateNWorker(int num){
		Config.NWorker=num;
		for (;workers.size()<Config.NWorker;)
			workers.add(null);
		for (;workers.size()>Config.NWorker;)
			workers.removeLast();
	}
	
	public void startWorker(int id){
		if (workers.get(id)!=null&&!workers.get(id).isAlive())
			workers.set(id, null);
		if (workers.get(id)==null){
			workers.set(id,new Worker("Worker_"+id, Config.Task));
			workers.get(id).start();
		}
	}
	public void stopWorker(int id){
		if (workers.get(id)!=null){
			workers.get(id).task.releaseResources();
			workers.get(id).stop();
			workers.set(id, null);
		}
	}
	
}
