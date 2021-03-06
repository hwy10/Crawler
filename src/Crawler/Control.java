package Crawler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Util.NetworkConnect;

import com.cqz.dm.UUAPI;
import com.sun.java.swing.plaf.windows.resources.windows;

import Functions.AutoStarter;
import Gui.MainWindow;

public class Control {
	
	public MainWindow window;
	
	public static LinkedList<Worker> workers=new LinkedList<Worker>();
	public static HashMap<String, String>captcha=new HashMap<String, String>();
	
	public void assignGUI(MainWindow window){
		this.window=window;
	}
	
	public void init(){
		
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
			if (cmd[0].equals("function")){
				AutoStarter task=(AutoStarter)Class.forName("Functions."+cmd[1]).newInstance();
				task.core=this;
				task.start();
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
//		window.updateGUI();
	}
	
	public void updateNWorker(int num){
		Config.NWorker=Math.max(Config.NWorker, num);
		for (;workers.size()<Config.NWorker;)
			workers.add(new Worker(this, workers.size()));
		for (;workers.size()>Config.NWorker;){
			stopWorker(workers.size()-1);
			workers.removeLast();
		}
	}
	
	public void startWorker(int id){
		startWorker(id,Config.Task);
	}
	
	public void startWorker(int id, String task){
		try{
			if (!workers.get(id).isAlive()||workers.get(id).kill){
				stopWorker(id);
				workers.set(id, new Worker(this, id));
				workers.get(id).setTask(task);
				workers.get(id).start();
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
			
	}
	public void stopWorker(int id){
		workers.get(id).kill=true;
		workers.get(id).interrupt();
		workers.get(id).stop();
		workers.get(id).release();
		workers.get(id).updateUI();
	}
	
}
