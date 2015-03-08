package Crawler;

import java.util.LinkedList;

import org.apache.http.params.CoreConnectionPNames;

public class Guard extends Thread{
	LinkedList<String> last=new LinkedList<String>();
	Control core;
	public Guard(Control core) {
		this.core=core;
	}
	@Override
	public void run() {
		super.run();
		Logger.add("Guard Running");
		for (;;){
			try{
				if (last.size()==Config.NWorker)
					for (int i=0;i<Config.NWorker;i++){
						try{
							if (last.get(i).equals(Control.workers.get(i).curStatus)
									&&!last.get(i).startsWith("###")){
								Logger.add("Guard : Restart "+Control.workers.get(i).wid);
								core.stopWorker(i);
								core.startWorker(i);
							}
						}catch (Exception ex){
						}
					}
				last=new LinkedList<String>();
				for (int i=0;i<Config.NWorker;i++){
					if (Control.workers.get(i)!=null)
						last.add(Control.workers.get(i).curStatus);
					else last.add("NULL WORKER");
				}
				Thread.sleep(1000*60*Config.GuardInterval);
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}
}
