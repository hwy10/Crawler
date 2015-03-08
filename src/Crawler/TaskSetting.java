package Crawler;

import org.apache.http.client.params.CookiePolicy;

public class TaskSetting {
	public String cookiePolicy=CookiePolicy.BROWSER_COMPATIBILITY;
	public String proxyType="localhost";
	public boolean reportProxy=true;
	public int proxyLimit=1;
}
