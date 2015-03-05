package Util;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.cookie.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;

public class NetworkConnect {
	
	public static String send2Get(String url,String param,String cookie){
		String result="";
		HttpClient client = new HttpClient();
		try{
			HttpMethod method=new GetMethod(url+"?"+param);  
			client.executeMethod(method);  
			if (cookie!=null&&cookie.length()>0){
				//client.getParams().setParameter("http.protocol.single-cookie-header", true);
				method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
				//client.getState().addCookie(new Cookie("N/A", "pumpkinhead", "8ef4c54f4983d02e02e1549ade0ab5971bfadf2931f96d863afb4750baded914068c28fe"));
				method.setRequestHeader("Cookie",cookie);
			}
			method.setRequestHeader("Accept","*/*");
			method.setRequestHeader("Connection", "Keep-Alive"); 
			method.setRequestHeader("Cache-Control", "no-cache");
			method.setRequestHeader("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			method.setRequestHeader("Content-Type", "application/json; charset=utf-8");
			
			client.executeMethod(method);
		    //System.out.println(method.getStatusLine());  
		    //System.out.println(method.getResponseBodyAsString());  
			result=method.getResponseBodyAsString();
		    method.releaseConnection();
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return result;
	}
	
	public static String sendGet(String url, String param, String cookie) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 鎵撳紑鍜孶RL涔嬮棿鐨勮繛鎺�
            URLConnection connection = realUrl.openConnection();
            // 璁剧疆閫氱敤鐨勮姹傚睘鎬�
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            if (cookie.length()>0) 
            	connection.addRequestProperty("Cookie", cookie);
            connection.addRequestProperty("Content-Type", "application/json; charset=utf-8");
            // 寤虹珛瀹為檯鐨勮繛鎺�
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();
            // 瀹氫箟 BufferedReader杈撳叆娴佹潵璇诲彇URL鐨勫搷搴�
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
        	System.out.println(url+"\t"+param);
            System.out.println("鍙戦�丟ET璇锋眰鍑虹幇寮傚父锛�" + e);
            //e.printStackTrace();
            
        }
        // 浣跨敤finally鍧楁潵鍏抽棴杈撳叆娴�
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
}
