package DBConnector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class WeiboDB {
	Connection conn;
	public WeiboDB(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn=DriverManager.getConnection(
					"jdbc:mysql://172.16.7.85:3306/weibo?characterEncoding=utf8",
					"crawler","crawler");
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	public void close(){
		try{
			conn.close();
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void updateUserTag(String uid,String tag,String ori){
		try{
			synchronized (this) {
				String cmd="update `user` set crawltag='"+tag+"' where userid='"+uid+"' and crawltag='"+ori+"'";
				Statement stat=conn.createStatement();
				stat.executeUpdate(cmd);
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void updateTag(String ori,String tag){
		try{
			synchronized (this) {
				String cmd="update `userqueue` set crawltag='"+tag+"' where name='"+ori+"'";
				Statement stat=conn.createStatement();
				stat.executeUpdate(cmd);
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void insertUser(String userid,String displayname,int weibo,int followee,int follower,String tag){
		try{
			PreparedStatement stat;
			stat=conn.prepareStatement(
					"insert into `user` (`userid`,`displayname`,`cntweibo`,`cntfollowee`,`cntfollower`,`crawltag`)"
					+" VALUES (?,?,?,?,?,?)");
			stat.setString(2, displayname);
			stat.setInt(3, weibo);
			stat.setInt(4, followee);
			stat.setInt(5, follower);
			stat.setString(6, tag);
			stat.setString(1, userid);
			stat.execute();
		}catch (Exception ex){
			if (!ex.toString().contains("Duplicate"))
				ex.printStackTrace();
		}
	}
	
	public void insertWeibo(String userid,String weiboId,String content){
		try{
			PreparedStatement stat=conn.prepareStatement(
					"insert into `weiboraw` (`wid`,`userid`,`content`)"
					+" VALUES (?,?,?)");
			stat.setString(1, weiboId);
			stat.setString(2, userid);
			stat.setString(3, content);
			stat.execute();
		}catch (Exception ex){
			 if (!ex.toString().contains("Duplicate")&&!ex.toString().contains("column \'content\'"))
				ex.printStackTrace();
		}
	}
	
	public String getNextQueue(String tag,String newTag){
		try{
			synchronized (this) {
				String cmd="select name from `userqueue` where crawltag='"+tag+"' order by id limit 1";
				Statement stat=conn.createStatement();
				ResultSet rs=stat.executeQuery(cmd);
				if (rs.next()) {
					String res=rs.getString(1);
					cmd="update `userqueue` set crawltag='"+newTag+"' where `name`='"+res+"'";
					stat.executeUpdate(cmd);
					return res;
				}
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return "";
	}
	public String getNextUser(String tag,String newTag){
		try{
			synchronized (this) {
				String cmd="select userid from `user` where crawltag='"+tag+"' order by id limit 1";
				Statement stat=conn.createStatement();
				ResultSet rs=stat.executeQuery(cmd);
				if (rs.next()) {
					String res=rs.getString(1);
					cmd="update `user` set crawltag='"+newTag+"' where `userid`='"+res+"'";
					stat.executeUpdate(cmd);
					return res;
				}
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return "";
	}
	
	public void addQueue(String name){
		try{
			String cmd="insert into `userqueue` (`name`) VALUES('"+name+"')";
			Statement stat=conn.createStatement();
			stat.executeUpdate(cmd);
		}catch (Exception ex){
		}
	}
}
