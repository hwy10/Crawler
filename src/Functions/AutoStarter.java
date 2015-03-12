package Functions;

import Crawler.Config;
import Crawler.Control;

public class AutoStarter extends Thread{
	public Control core;
	@Override
	public void run() {
		for (;;){
			try {
				core.execute("run 0-"+Config.NWorker);
			} catch (Exception e) {
			}
			try {
				Thread.sleep(1000*60*5);
			} catch (Exception e) {
			}
		}
	}
}
