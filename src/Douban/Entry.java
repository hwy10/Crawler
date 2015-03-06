package Douban;

import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BasicOps.FileOps;
import Util.NetworkConnect;

public class Entry {
	public void run(){
		try{
			int K=40;
			for (int i=0;;i++){
				System.out.println("Iteration "+i);
				Worker[] workers=new Worker[K];
				for (int j=0;j<K;j++){
					workers[j]=new Worker();
					workers[j].name="worker"+j;
					workers[j].start();
					Thread.sleep(1000*5);
				}
				Thread.sleep(1000*60*60);
				for (int j=0;j<K;j++)
					workers[j].stop();
				System.out.println("Iteration Finished");
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
}
