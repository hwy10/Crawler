package Gui;

import Crawler.Config;

public class GuiUpdater extends Thread{
	private MainWindow window;
	public GuiUpdater(MainWindow window) {
		this.window=window;
	}
	@Override
	public void run() {
		super.run();
		for (;;){
			try{
				window.updateGUI();
				Thread.sleep(Config.guiUpdateInterval*1000);
			}catch (Exception ex){}
			
		}
	}
}
