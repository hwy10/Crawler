package Crawler;


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

public class Client {
	HttpClient client;
	HttpClientContext context;
	public String proxy;
	
	public Client() {
		client=new DefaultHttpClient();
		context=HttpClientContext.create();
		client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
		CookieStore cookie=new BasicCookieStore();
		context.setCookieStore(cookie);
		
		Random random=new Random();
		int pid=random.nextInt(Config.ProxyIP.size());
		this.proxy=Config.ProxyIP.get(pid)+":"+Config.ProxyPort.get(pid);
		HttpHost proxy = new HttpHost(Config.ProxyIP.get(pid),Config.ProxyPort.get(pid));  
		client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy); 
	}
	
	public boolean saveImg(String url,String path){
		try{
			HttpGet get=new HttpGet(url);
			
			HttpResponse res=client.execute(get,context);
			HttpEntity entity=res.getEntity();
			if (entity != null) { 
			    InputStream instream = entity.getContent();
			    ImageIO.write(ImageIO.read(instream),"jpg",new File(path));
			    return true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return false;
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
		}
		return "";
	}
	
	public String sendPost(String url,LinkedList<NameValuePair> params){
		try{
			HttpPost login=new HttpPost(url);
			login.setHeader("User-Agent",
		             "Mozilla/5.0 (Windows NT 5.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
			login.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			
			login.setEntity(new UrlEncodedFormEntity(params));
			
			HttpResponse res=client.execute(login,context);
	
			BufferedReader rd = new BufferedReader(
				new InputStreamReader(res.getEntity().getContent()));
		 
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			return result.toString();
		}catch (Exception ex){
		}
		return "";
	}
}
