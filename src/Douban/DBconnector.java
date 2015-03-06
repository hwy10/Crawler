package Douban;

import java.awt.print.Printable;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DBconnector {
	Connection conn;
	public DBconnector(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn=DriverManager.getConnection(
					"jdbc:mysql://172.16.7.85:3306/douban?characterEncoding=utf8",
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
	
	public void updateWeiboId(String weiboId,int uid){
		try{
			String cmd="update user set weiboid='"+weiboId+"' where uid="+uid;
			Statement stat=conn.createStatement();
			stat.executeUpdate(cmd);
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	public int getNextUser(){
		try{
			String cmd="SELECT uid FROM `user` WHERE `crawltag`=0 limit 1";
			Statement stat=conn.createStatement();
			ResultSet rs=stat.executeQuery(cmd);
			int res=-1;
			for (;rs.next();) res=rs.getInt(1);
			if (res!=-1){
				String cmd2="update `user` set crawltag=-1 WHERE uid="+res;
				stat.executeUpdate(cmd2);
				return res;
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return -1;
	}
	
	public void updateUser(int uid,String username,String displayname,
			String location,String description,String crawltag){
		try {
			PreparedStatement stat=conn.prepareStatement(
					"update `user` set `username`=?, `displayname`=?, "
					+" `location`=?, `description`=?, `crawltag`=? "
					+"where `uid`="+uid);
			stat.setString(1, username);
			stat.setString(2, displayname);
			stat.setString(3, location);
			stat.setString(4, description);
			stat.setString(5, crawltag);
			stat.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public LinkedList<Integer> getAllUser(String query){
		LinkedList<Integer> res=new LinkedList<Integer>();
		try{
			String cmd="SELECT uid FROM `user` WHERE "+query+" order by id";
			Statement stat=conn.createStatement();
			ResultSet rs=stat.executeQuery(cmd);
			for (;rs.next();) res.add(rs.getInt(1));
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return res;
	}
	
	public User getUser(int uid){
		User res=new User();
		try{
			String cmd="SELECT uid,username,displayname,location,"
					+"description,weiboid,crawltag FROM `user` WHERE uid="+uid;
			Statement stat=conn.createStatement();
			ResultSet rs=stat.executeQuery(cmd);
			for (;rs.next();) res=extractUser(rs);
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return res;
	}
	private User extractUser(ResultSet rs) throws SQLException{
		User user=new User();
		user.uid=rs.getInt(1);
		user.username=rs.getString(2);
		user.displayname=rs.getString(3);
		user.location=rs.getString(4);
		user.description=rs.getString(5);
		user.weiboid=rs.getString(6);
		user.crawltag=rs.getString(7);
		return user;
	}
	public void insertUser(int i){
		String cmd="INSERT INTO `user` (`uid`) VALUES ("+i+")";
		try{
			Statement stat=conn.createStatement();
			stat.executeUpdate(cmd);
		}catch (Exception ex){
			if (!ex.getMessage().contains("Duplicate")){
				System.out.println(cmd);
				ex.printStackTrace();
			}
		}
	}
	public void addFriend(int i,int j){
		String cmd="INSERT INTO `friendship` (`uida`,`uidb`) VALUES ("+i+","+j+")";
		try{
			Statement stat=conn.createStatement();
			stat.executeUpdate(cmd);
		}catch (Exception ex){
			if (!ex.getMessage().contains("Duplicate")){
				System.out.println(cmd);
				ex.printStackTrace();
			}
		}
	}
}
