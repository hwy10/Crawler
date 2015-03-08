package Trash;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.management.Query;

import org.apache.commons.codec.binary.Base32;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;

import com.sun.jndi.url.corbaname.corbanameURLContextFactory;

public class ClientWrapper {
	HttpClient client;
	HttpClientContext context;
	String name;
	public void init(){
		LinkedList<String> ip=new LinkedList<String>();
		LinkedList<Integer> port=new LinkedList<Integer>();
		for (String proxy:BasicOps.FileOps.LoadFilebyLine("proxy.txt")){
			String[] sep=proxy.split("\t");
			ip.add(sep[1]);
			port.add(Integer.valueOf(sep[2]));
		}
		Random random=new Random();
		for (;;){
			int pid=random.nextInt(ip.size());
			String proxyip=ip.get(pid);
			int proxyport=port.get(pid);
//			proxyip="local";proxyport=0;
			System.out.println(proxyip+"\t"+proxyport);
			name=proxyip+"_"+proxyport;
			client=new DefaultHttpClient();
			context=HttpClientContext.create();
			client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
			CookieStore cookie=new BasicCookieStore();
			context.setCookieStore(cookie);
			if (proxyport!=0) {
				HttpHost proxy = new HttpHost(proxyip,proxyport);  
				client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy); 
			}
			if (trylogin()) break;
		}
	}
	
	private boolean trylogin(){
		try{
			String content=getContent("http://m.douban.com/login");
//			System.out.println(content);
				
				String email="hijack2004@126.com";
				String pwd="cat12321";
				Matcher matcher=Pattern.compile("/captcha/(.*?)/").matcher(content);
				matcher.find();
				String capId=matcher.group(1);
//						"gVbrtXYR3IDAFduurqZ4mHbx";
				String capsol;
				Scanner input=new Scanner(System.in);
				String imgdir="http://m.douban.com/captcha/"+capId+"/?size=m";
				saveImg(imgdir,name+"imgcode.jpg");
//				Network.Images.saveJPG(imgdir, "imgcode.jpg");
//				System.out.println("输入验证码:\n"+"http://m.douban.com/captcha/"+capId+"/?size=m");
//				capsol=input.nextLine();
				capsol=com.cqz.dm.CodeReader.getImgCode(name+"imgcode.jpg", 3008);

				HttpPost login=new HttpPost("http://m.douban.com/login");
				login.setHeader("User-Agent",
			             "Mozilla/5.0 (Windows NT 5.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
				login.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
				
				LinkedList<NameValuePair> params=new LinkedList<NameValuePair>();
				params.add(new BasicNameValuePair("form_email", email));
				params.add(new BasicNameValuePair("form_password", pwd));
				params.add(new BasicNameValuePair("captcha-solution", capsol));
				params.add(new BasicNameValuePair("captcha-id",capId));
				params.add(new BasicNameValuePair("user_login", "登录"));
				login.setEntity(new UrlEncodedFormEntity(params));
				
				HttpResponse res=client.execute(login,context);

				BufferedReader rd = new BufferedReader(
					new InputStreamReader(res.getEntity().getContent()));
			 
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
				content=result.toString();
//				System.out.println(content);
				if (content.contains("you should be redirected automatically.")) return true;
	
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return false;
	}
	
	public void saveImg(String url,String path){
		try{
			HttpGet get=new HttpGet(url);
			
			HttpResponse res=client.execute(get,context);
			HttpEntity entity=res.getEntity();
			if (entity != null) { 
			    InputStream instream = entity.getContent();
			    ImageIO.write(ImageIO.read(instream),"jpg",new File(path));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public String getContent(String url){
		try{
			HttpGet get=new HttpGet(url);
			
			HttpResponse res=client.execute(get,context);
			BufferedReader rd = new BufferedReader(
				new InputStreamReader(res.getEntity().getContent(),"UTF-8"));
		 
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			return result.toString();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return "";
	}
	
}
