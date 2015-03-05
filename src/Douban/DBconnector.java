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
	
	public int getNextUser(){
		try{
			String cmd="SELECT uid FROM `user` WHERE `crawltag`=0 limit 1";
			Statement stat=conn.createStatement();
			ResultSet rs=stat.executeQuery(cmd);
			for (;rs.next();) return rs.getInt(1);
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return -1;
	}
}
