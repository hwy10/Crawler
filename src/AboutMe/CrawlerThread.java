package AboutMe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.locks.Lock;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import Util.NetworkConnect;

public class CrawlerThread extends Thread{
	Crawler control;
	String dir;
	int Tid;
	public CrawlerThread(Crawler c,String Path,int id){
		control=c;
		dir=Path;
		Tid=id;
	}
	public void run(){
		try {
			for (;;){
				String username=null;
				synchronized(control){
					username=control.getNextUser();
				}
				if (username==null) sleep(1000);
				else {
					runSingle(username);
					System.out.println("Thread #"+Tid+" Running User "+username);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void runSingle(String s){
		File cur=new File(dir+"/"+s);
		if (!cur.exists()||cur.length()==0) {
			//downloadData(s,cur);
			return;
		}
		for (String site:control.sites)
			downloadSite(site,s);
		ParseData(cur);
	}
	private void downloadSite(String type,String s){
		File file=new File(dir+"/"+type+"/"+s);
		if (file.exists()&&file.length()>0) return;
		String res;
		if (type.equals("aboutmedes"))
			res=NetworkConnect.sendGet("http://about.me/"+s,"","");
		else res=NetworkConnect.sendGet("http://about.me/content/"+s+"/"+type,"","");
		try{
			BufferedWriter fout=new BufferedWriter(new FileWriter(file));
			fout.write(res);
			fout.close();
		}catch (Exception ex){
			System.out.println("Error Writing to File" + ex);
		}
	}
	private void downloadData(String s,File file){
		System.out.println("Thread #"+Tid+" --- download data : "+s);
		String res=NetworkConnect.sendGet("http://about.me/ajax/load_groups"
				, "owner="+s+"&_authentication_token="+control.authen
				, control.cookie);
		if (res.length()==0){
			try {
				System.out.println("Thread #"+Tid +" Sleeps");
				sleep(10000*(10+Tid));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try{
			BufferedWriter fout=new BufferedWriter(new FileWriter(file));
			fout.write(res);
			fout.close();
		}catch (Exception ex){
			System.out.println("Error Writing to File" + ex);
		}
	}
	private void ParseData(File file){
		if (file.length()==0) return;
		String data="";
		try{
//			System.out.println("Thread #"+Tid+" --- parsing data : "+file.getName());
			BufferedReader fin=new BufferedReader(new FileReader(file));
			data=fin.readLine();
			fin.close();
			JSONObject json=JSONObject.fromObject(data);
			//System.out.println(json.get("groups"));
			if (json.get("groups")==null) return;
			JSONArray group=JSONArray.fromObject(json.get("groups"));
			for (int i=0;i<group.size();i++){
				JSONObject cur=JSONObject.fromObject(group.get(i));
				String user=cur.getString("users");
				if (user==null||user.length()==0) continue;
				JSONArray users=JSONArray.fromObject(user);
				for (int z=0;z<users.size();z++){
					JSONObject curUser=JSONObject.fromObject(users.get(z));
					String username=curUser.getString("name");
					synchronized(control){
						control.addUser(username);
					}
				}
			}
		}catch (Exception ex){
			System.out.println("Thread #"+Tid+" Error Parsing Data"+ex);
			System.out.println(file.getName()+"\t"+data+"\t"+file.length());
			ex.printStackTrace();
		}
	}
}
