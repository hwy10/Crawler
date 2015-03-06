package Crawler;
import java.io.BufferedReader;
import java.util.Scanner;


public class Console {
	public static void main(String[] args) {
		
		Thread proxyLoader=new ProxyLoader();
		proxyLoader.start();
		Thread guard=new Guard();
		guard.start();
		
		Scanner in=new Scanner(System.in);
		for (;;){
			try{
				String [] cmd=in.nextLine().split(" ");
				if (cmd[0].equals("run")){
					String []sep=cmd[1].split("-");
					if (sep.length==1)
						startWorker(Integer.valueOf(sep[0]));
					else
						for (int i=Integer.valueOf(sep[0]);i<Integer.valueOf(sep[1]);i++)
							startWorker(i);
				}
				if (cmd[0].equals("show")){
					if (cmd[1].equals("proxy")) showProxy();
					if (cmd[1].equals("worker")) showWorker();
				}
				if (cmd[0].equals("set")){
					if (cmd[1].equals("task")) Config.Task=cmd[2];
					if (cmd[1].equals("nworker")) {
						Config.NWorker=Integer.valueOf(cmd[2]);
						for (;Config.workers.size()<Config.NWorker;)
							Config.workers.add(null);
						for (;Config.workers.size()>Config.NWorker;)
							Config.workers.removeLast();
					}
					if (cmd[1].equals("restTime"))
						Config.restTime=Integer.valueOf(cmd[2]);
				}
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	public static void startWorker(int id){
		if (Config.workers.get(id)==null){
			Config.workers.set(id,new Worker("Worker_"+id, Config.Task));
			Config.workers.get(id).start();
		}
	}
	
	public static void showProxy(){
		for (int i=0;i<Config.ProxyIP.size();i++)
			System.out.println(Config.ProxyIP.get(i)+"\t"+Config.ProxyPort.get(i));
	}
	
	public static void showWorker(){
		for (int i=0;i<Config.NWorker;i++)
			if (Config.workers.get(i)==null)
				System.out.println("Worker "+i+"\tidle");
			else Config.workers.get(i).status();
	}
}
