package Crawler;
import java.io.BufferedReader;
import java.util.Scanner;

import Gui.MainWindow;


public class Console {
	public static void main(String[] args) {
		
		Control core=new Control();
		core.init();
		
		Scanner in=new Scanner(System.in);
		for (;;)
			core.execute(in.nextLine());
	}
}
