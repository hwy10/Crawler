package Gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;

import javafx.scene.control.ScrollPane;

import javax.swing.JFrame;

import Crawler.Client;
import Crawler.Config;
import Crawler.Console;
import Crawler.Control;
import Crawler.Logger;
import Crawler.ProxyBank;
import Crawler.Worker;

import javax.swing.JTextField;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;

import Util.NetworkConnect;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import com.sun.org.apache.bcel.internal.generic.NEW;

import sun.java2d.pipe.SpanShapeRenderer.Simple;

import javax.swing.SwingConstants;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.params.CoreConnectionPNames;

import java.awt.Component;

import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class MainWindow {
	
	private AccountWindow userWindow;
	
	private Control core;
	
	private JFrame frmXuezhiCaosCrawler;
	private JTextField commandText;
	private JTable workerTable;
	private JScrollPane scrollPane;
	
	public DefaultTableModel workerTableModel;
	public DefaultTableModel proxyTableModel;
	
	private String[] tasks=new String[] {"DoubanMovie", "DoubanUser", "WeiboUser", "Baike","WeiboFriend"};
	private String[] workerTableHeader=new String[] {
			"ID","Proxy","Account","Task","Status","Progress","#","Action"
		};
	private String[] proxyTableHeader=new String[] {
			"Address","#Worker","#Report"
		};
	private JList logList;
	private JPanel logPanel;
	private JLabel lblNewLabel;
	private JPanel upperPanel;
	private JLabel headerLabel_2;
	private JLabel headerLabel_3;
	private JTextField guardText;
	private JLabel headerLabel_1;
	private JTextField nworkerText;
	private JButton upperSetBtn;
	private JPanel upperPanel_left;
	private JPanel upperPanel_right;
	private JButton refreshTableBtn;
	private JLabel lblRefreshInterval;
	private JTextField refreshText;
	private JPanel panel;
	private JPanel proxyPanel;
	private JLabel lblProxyBank;
	private JTable proxyTable;
	private JScrollPane scrollPane_1;
	private JPanel proxyActionPanel;
	private JTextArea textArea;
	private JButton addProxy;
	private JButton accountBtn;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmXuezhiCaosCrawler.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmXuezhiCaosCrawler = new JFrame();
		frmXuezhiCaosCrawler.setTitle("Xuezhi Cao's Crawler");
		frmXuezhiCaosCrawler.setBounds(100, 100, 1300, 800);
		frmXuezhiCaosCrawler.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmXuezhiCaosCrawler.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel lowerPanel = new JPanel();
		frmXuezhiCaosCrawler.getContentPane().add(lowerPanel, BorderLayout.SOUTH);
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.X_AXIS));
		
		commandText = new JTextField();
		commandText.setToolTipText("");
		lowerPanel.add(commandText);
		commandText.setColumns(10);
		
		JButton executeBtn = new JButton("Execute");
		executeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String command=commandText.getText();
				core.execute(command);
//				updateGUI();
			}
		});
		lowerPanel.add(executeBtn);
		
		JPanel mainPanel = new JPanel();
		frmXuezhiCaosCrawler.getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		
		workerTable = new JTable();
		scrollPane.setViewportView(workerTable);
		workerTable.setEnabled(true);
		workerTableModel=new DefaultTableModel(
				new Object[][] {
						
				},workerTableHeader);
		workerTable.setModel(workerTableModel);
		
		panel = new JPanel();
		frmXuezhiCaosCrawler.getContentPane().add(panel, BorderLayout.EAST);
		panel.setLayout(new BorderLayout(0, 0));
		
		proxyPanel = new JPanel();
		panel.add(proxyPanel, BorderLayout.NORTH);
		proxyPanel.setLayout(new BorderLayout(0, 0));
		
		lblProxyBank = new JLabel("--- Proxy Bank ---");
		lblProxyBank.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(new Color(130, 130, 130)),
						BorderFactory.createEmptyBorder(4, 120, 4, 120)));
		proxyPanel.add(lblProxyBank, BorderLayout.NORTH);
		
		scrollPane_1 = new JScrollPane();
		
		proxyTable = new JTable();
		proxyTable.setModel(new DefaultTableModel(
			new Object[][] {
			},proxyTableHeader
		));
		scrollPane_1.setViewportView(proxyTable);
		scrollPane_1.setPreferredSize(new Dimension(200,300));
		proxyPanel.add(scrollPane_1, BorderLayout.CENTER);
		
//		proxyPanel.add(new JScrollPane(proxyTable), BorderLayout.CENTER);
		
		proxyActionPanel = new JPanel();
		proxyActionPanel.setLayout(new BoxLayout(proxyActionPanel, BoxLayout.X_AXIS));
		
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setRows(1);
		proxyActionPanel.add(new JScrollPane(textArea));
		proxyPanel.add(proxyActionPanel, BorderLayout.SOUTH);
		
		addProxy = new JButton("Add Proxy");
		addProxy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for (String row:textArea.getText().split("\n")){
					String []sep=row.split("\t");
					ProxyBank.addProxy(sep[1], Integer.valueOf(sep[2]));
				}
				ProxyBank.saveProxies();
				textArea.setText("");
			}
		});
		proxyActionPanel.add(addProxy);
		logPanel = new JPanel();
		panel.add(logPanel, BorderLayout.CENTER);
		logPanel.setLayout(new BorderLayout(0, 0));
		
		lblNewLabel = new JLabel("--- Worker Log ---");
		lblNewLabel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(130, 130, 130)),
				BorderFactory.createEmptyBorder(4, 120, 4, 120)));
		logPanel.add(lblNewLabel, BorderLayout.NORTH);
		
		logList = new JList();
		logPanel.add(new JScrollPane(logList),BorderLayout.CENTER);
		logList.setModel(new AbstractListModel() {
			String[] values = new String[] {};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		
		upperPanel = new JPanel();
		frmXuezhiCaosCrawler.getContentPane().add(upperPanel, BorderLayout.NORTH);
		upperPanel.setLayout(new BorderLayout(0, 0));
		
		upperPanel_left = new JPanel();
		upperPanel.add(upperPanel_left, BorderLayout.CENTER);
		upperPanel_left.setLayout(new BoxLayout(upperPanel_left, BoxLayout.X_AXIS));
		
		headerLabel_1 = new JLabel("#Workers");
		upperPanel_left.add(headerLabel_1);
		headerLabel_1.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(),
				BorderFactory.createEmptyBorder(4, 10, 4, 10)));
		nworkerText = new JTextField();
		upperPanel_left.add(nworkerText);
		nworkerText.setHorizontalAlignment(SwingConstants.CENTER);
		nworkerText.setText(""+Config.NWorker);
		nworkerText.setMaximumSize(new Dimension(30, 100));
		nworkerText.setColumns(3);
		
		headerLabel_2 = new JLabel("Current Task");
		upperPanel_left.add(headerLabel_2);
		headerLabel_2.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(),
				BorderFactory.createEmptyBorder(4, 10, 4, 10)));
		
		taskBox = new JComboBox();
//		taskBox.setPreferredSize(new Dimension(50,20));
		taskBox.setModel(new DefaultComboBoxModel(tasks));
		taskBox.setSelectedItem(Config.Task);
		upperPanel_left.add(taskBox);
		
		headerLabel_3 = new JLabel("Guard Interval(min)");
		upperPanel_left.add(headerLabel_3);
		headerLabel_3.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(),
				BorderFactory.createEmptyBorder(4, 10, 4, 10)));
		
		guardText = new JTextField();
		upperPanel_left.add(guardText);
		guardText.setHorizontalAlignment(SwingConstants.CENTER);
		guardText.setColumns(3);
		guardText.setText(""+Config.GuardInterval);
		guardText.setMaximumSize(new Dimension(30, 100));
		
		lblRefreshInterval = new JLabel("Refresh Interval(s)");
		lblRefreshInterval.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(),
						BorderFactory.createEmptyBorder(4, 10, 4, 10)));
		upperPanel_left.add(lblRefreshInterval);
		
		refreshText = new JTextField();
		refreshText.setText(""+Config.guiUpdateInterval);
		refreshText.setMaximumSize(new Dimension(30, 100));
		refreshText.setHorizontalAlignment(SwingConstants.CENTER);
		refreshText.setColumns(3);
		upperPanel_left.add(refreshText);
		
		lblProxyTrials = new JLabel("# Proxy Trials");
		lblProxyTrials.setBorder(BorderFactory.createCompoundBorder(
								BorderFactory.createCompoundBorder(),
								BorderFactory.createEmptyBorder(4, 10, 4, 10)));
		upperPanel_left.add(lblProxyTrials);
		
		proxyText = new JTextField();
		proxyText.setText(Config.ProxyTrial+"");
		proxyText.setMaximumSize(new Dimension(30, 100));
		proxyText.setHorizontalAlignment(SwingConstants.CENTER);
		proxyText.setColumns(3);
		upperPanel_left.add(proxyText);
		
		upperSetBtn = new JButton("Set Params");
		upperPanel_left.add(upperSetBtn);
		upperSetBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
		
		runallBtn = new JButton("Run All");
		runallBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					@Override
					public void run() {
						core.execute("run 0-"+Config.NWorker);
					}
				}.start();
			}
		});
		upperPanel_left.add(runallBtn);
		upperSetBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				core.updateNWorker(Integer.valueOf(nworkerText.getText()));
				Config.Task=taskBox.getSelectedItem().toString();
				Config.GuardInterval=Integer.valueOf(guardText.getText());
				Config.guiUpdateInterval=Integer.valueOf(refreshText.getText());
				Config.ProxyTrial=Integer.valueOf(proxyText.getText());
			}
		});
		
		upperPanel_right = new JPanel();
		upperPanel.add(upperPanel_right, BorderLayout.EAST);
		upperPanel_right.setLayout(new BoxLayout(upperPanel_right, BoxLayout.X_AXIS));
		
		refreshTableBtn = new JButton("Refresh");
		refreshTableBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateGUI_Full();
			}
		});
		
		accountBtn = new JButton("Accounts Manager");
		accountBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (userWindow==null) userWindow=new AccountWindow(core);
				userWindow.show();
				userWindow.updateGUI();
			}
		});
		upperPanel_right.add(accountBtn);
		upperPanel_right.add(refreshTableBtn);
		
		Logger.window=this;
		
		core=new Control();
		core.assignGUI(this);
		core.init();
		GuiUpdater updater=new GuiUpdater(this);
		updater.start();
		
		setGUI();
	}
	
	private void setGUI(){
		workerTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		workerTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		workerTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		workerTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		workerTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		workerTable.getColumnModel().getColumn(4).setPreferredWidth(300);
		workerTable.getColumnModel().getColumn(5).setPreferredWidth(200);
		workerTable.getColumnModel().getColumn(6).setPreferredWidth(50);
		workerTable.getColumnModel().getColumn(7).setPreferredWidth(150);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		for (int i=0;i<8;i++)
			workerTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		workerTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
		workerTable.getColumn("Progress").setCellRenderer(new ProgressBarRenderer());
		workerTable.getColumn("Action").setCellEditor(
		        new ButtonEditor(new JCheckBox(),core,commandText));
	}
	
	public void updateGUI_Full(){
//		updateWorkerTable();
		updateProxyTable();
		updateGUI();
	}
	
	public void updateGUI(){
		lblProxyBank.setText("--- Proxy Bank ("+ProxyBank.inuse()+"/"+ProxyBank.size()+")---");
		workerTable.updateUI();
		proxyTable.updateUI();
	}
	
	public String workerTableLock="lock";
	private JComboBox taskBox;
	private JButton runallBtn;
	private JLabel lblProxyTrials;
	private JTextField proxyText;
	
	public void updateWorkerTable(){
		Object[][] content=new Object[Config.NWorker][8];
		for (int i=0;i<Config.NWorker;i++){
			Worker worker=Control.workers.get(i);
			if (worker!=null){
				if (!worker.isAlive()){
					Control.workers.set(i, null);
					worker=null;
				}else{
					content[i][0]=worker.wid;
					try{content[i][1]=worker.client.proxy;}catch (Exception ex){}
					try{content[i][2]=worker.task.username;}catch (Exception ex){}
					content[i][3]=worker.taskName;
					content[i][4]=worker.curStatus;
					content[i][5]=worker.progress;
					content[i][6]=worker.cnt;
					content[i][7]=i+"-Stop";
				}
			}
			if (worker==null){
				content[i][0]="#";
				content[i][7]=i+"-Start";
			}
			
		}
		
		workerTableModel=new DefaultTableModel(content,workerTableHeader);
		workerTable.setModel(workerTableModel);
		
	}
	
	public void updateProxyTable(){
		Object[][] content=new Object[ProxyBank.proxy.size()][3];
		int id=0;
		for (String proxy:ProxyBank.proxy){
			content[id][0]=proxy;
			content[id][1]=ProxyBank.assignment.get(proxy);
			content[id][2]=ProxyBank.badProxy.get(proxy);
			id++;
		}
		
		proxyTable.setModel(new DefaultTableModel(content,proxyTableHeader));
		proxyTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		proxyTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		proxyTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		proxyTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		for (int i=0;i<3;i++)
			proxyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		
	}
	
	public void updateLog(String[] logs){
		logList.setModel(new AbstractListModel(){
			String[] values = logs;
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
	}
}



