package Crawler;

import java.util.LinkedList;

public class Guard extends Thread{
	LinkedList<String> last=new LinkedList<String>();
	@Override
	public void run() {
		super.run();
		for (;;){
			try{
				if (last.size()==Config.NWorker)
					for (int i=0;i<Config.NWorker;i++){
						if (last.get(i).equals(Config.workers.get(i).curStatus)){
							Config.workers.get(i).stop();
							Config.workers.get(i).start();
						}
					}
				last=new LinkedList<String>();
				for (int i=0;i<Config.NWorker;i++)
					last.add(Config.workers.get(i).curStatus);
				Thread.sleep(1000*60*3);
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}
}
