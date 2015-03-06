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
			for (int i=0;;i++){
				System.out.println("Worker "+i);
				Worker worker=new Worker();
				worker.start();
				Thread.sleep(1000*60*60*2);
				System.out.println("Worker Finished");
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
}
