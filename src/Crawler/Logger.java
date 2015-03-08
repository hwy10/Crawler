package Crawler;

import java.io.BufferedWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;



import java.util.concurrent.locks.Lock;

import Gui.MainWindow;

public class Logger {
	private static LinkedList<String> logs=new LinkedList<String>();
	public static MainWindow window=null;
	private static int MAXLOG=200;
	private static BufferedWriter logfile;
	
	public static void add(String log) {
		synchronized (logs) {
			String time=(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
			
			System.out.println(time+"\t"+log);
			if (logfile!=null)
				try{
					logfile.write(time+"\t"+log);
					logfile.flush();
				}catch (Exception ex){}
			
			logs.add(time+"    "+log);
			for (;logs.size()>MAXLOG;)
				logs.removeFirst();
			if (window!=null){
				int n=logs.size();
				String[] res=new String[n];
				for (int i=0;i<n;i++)
					res[i]=logs.get(n-i-1);
				window.updateLog(res);
			}
		}
	}
	public static void addFull(String log){
		String time=(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		System.out.println(time+"    "+log);
	}
}
