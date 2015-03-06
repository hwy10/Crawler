package Crawler;
import java.util.LinkedList;


public class ProxyLoader extends Thread{
	@Override
	public void run() {
		super.run();
		try{
			for (;;){
				updateProxy();
				Thread.sleep(1000*60);
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void updateProxy(){
		Config.ProxyIP=new LinkedList<String>();
		Config.ProxyPort=new LinkedList<Integer>();
		for (String proxy:BasicOps.FileOps.LoadFilebyLine("proxy.txt")){
			String[] sep=proxy.split("\t");
			Config.ProxyIP.add(sep[1]);
			Config.ProxyPort.add(Integer.valueOf(sep[2]));
		}
	}
	
	public void test(){
		System.out.println("123");
	}
}
