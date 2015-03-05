
public class Main {
	public static void main(String args[]) { 
		Douban.Entry entry=new Douban.Entry();
		entry.run();
    }
	
	public static void AboutMe(String args[]){
		AboutMe.Crawler aboutme=new AboutMe.Crawler();
		aboutme.init("AboutMe");
		aboutme.NThread=Integer.valueOf(args[0]);
		if (args.length>=2) aboutme.setSites(args[1]);
		aboutme.addUser("jodifritch");
		aboutme.addUser("juliaroy");
		aboutme.addUser("julian_allom");
		aboutme.setauthen("123912033005615354415897974966926407187");
		//aboutme.setcookie("pumpkinhead=8ef4c54f4983d02e02e1549ade0ab5971bfadf2931f96d863afb4750baded914068c28fe; optimizelyEndUserId=oeu1410843511704r0.5451995488256216; km_lv=x; authtkt=d0922078ee2f21d26516d2b7a7a85840541934f8xuezhicao!; optimizelyCustomEvents=%7B%22oeu1410843511704r0.5451995488256216%22%3A%5B%22Step%201%20complete%22%2C%22Signup%20complete%22%5D%7D; ki_t=1410940631341%3B1410940631341%3B1410941429220%3B1%3B6; ki_r=; km_ai=a9108g%40gmail.com; km_ni=a9108g%40gmail.com; optimizelySegments=%7B%22177238534%22%3A%22gc%22%2C%22177247141%22%3A%22false%22%2C%22177274055%22%3A%22referral%22%7D; optimizelyBuckets=%7B%7D; optimizelyPendingLogEvents=%5B%5D; __utma=61449596.1222638113.1410843512.1410952585.1410965840.5; __utmb=61449596.3.10.1410965840; __utmc=61449596; __utmz=61449596.1410939308.3.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); show_signup=false; kvcd=1410966007352; km_vs=1; km_uq=1410966007%20%2Fs%3FPageComplete%2520-%2520Username%3Djodifritch%26PageComplete%2520-%2520Bio%252FHeader%3Dtrue%26PageComplete%2520-%2520Background%3Dcustom%26PageComplete%2520-%2520Services%3D11%26ProfileView%2520-%2520LoggedIn%3Dtrue%26_k%3D02c12958c27452f207e74e259da5adcd66b80beb%26_p%3Da9108g%2540gmail.com%26_t%3D1410966007");
		aboutme.setcookie("pumpkinhead=8ef4c54f4983d02e02e1549ade0ab5971bfadf2931f96d863afb4750baded914068c28fe; optimizelyEndUserId=oeu1410843511704r0.5451995488256216; km_lv=x; optimizelyCustomEvents=%7B%22oeu1410843511704r0.5451995488256216%22%3A%5B%22Step%201%20complete%22%2C%22Signup%20complete%22%5D%7D; km_ai=a9108g%40gmail.com; km_ni=a9108g%40gmail.com; ki_t=1410940631341%3B1411021484088%3B1411030656126%3B2%3B8; ki_r=; authtkt=\"c5ef8558b9351ca6a082e95e81db9fe3541a9fb2xuezhicao!userid_type:unicode\"; authtkt=\"c5ef8558b9351ca6a082e95e81db9fe3541a9fb2xuezhicao!userid_type:unicode\"; optimizelySegments=%7B%22177238534%22%3A%22gc%22%2C%22177247141%22%3A%22false%22%2C%22177274055%22%3A%22referral%22%7D; optimizelyBuckets=%7B%7D; optimizelyPendingLogEvents=%5B%5D; __utma=61449596.1222638113.1410843512.1411019741.1411030648.9; __utmb=61449596.6.10.1411030648; __utmc=61449596; __utmz=61449596.1410939308.3.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); show_signup=false; kvcd=1411030975654; km_vs=1; km_uq=1411030976%20%2Fs%3FPageComplete%2520-%2520Username%3Dxuezhicao%26PageComplete%2520-%2520Bio%252FHeader%3Dfalse%26PageComplete%2520-%2520Background%3Dcustom%26PageComplete%2520-%2520Services%3D0%26ProfileView%2520-%2520LoggedIn%3Dtrue%26_k%3D02c12958c27452f207e74e259da5adcd66b80beb%26_p%3Da9108g%2540gmail.com%26_t%3D1411030976");
		
		if (args.length>3&&args[args.length-3].equals("proxy")){
			System.setProperty("http.maxRedirects", "50");  
	        System.getProperties().setProperty("proxySet", "true");
	        System.getProperties().setProperty("http.proxyHost", args[args.length-2]);  
	        System.getProperties().setProperty("http.proxyPort", args[args.length-1]);
		}
        
        aboutme.run();
	}
}
