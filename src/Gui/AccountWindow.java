package Gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JCheckBox;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import Crawler.Control;
import Crawler.UserBank;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AccountWindow extends JFrame {

	private JPanel contentPane;
	private JTable userTable;
	private JTextField siteText;
	private Control core;
	
	/**
	 * Create the frame.
	 */
	public AccountWindow(Control core) {
		this.core=core;
		setTitle("Account Management");
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		userTable = new JTable();
		contentPane.add(new JScrollPane(userTable), BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JLabel lblSite = new JLabel("    Site : ");
		panel.add(lblSite);
		
		siteText = new JTextField();
		panel.add(siteText);
		siteText.setColumns(10);
		
		JLabel lblAccounts = new JLabel("    Accounts : ");
		panel.add(lblAccounts);
		
		JTextArea accountText = new JTextArea();
		accountText.setColumns(30);
		accountText.setLineWrap(true);
		accountText.setRows(1);
		panel.add(new JScrollPane(accountText));
		
		JButton btnAddAccounts = new JButton("Add Accounts");
		btnAddAccounts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String site=siteText.getText();
				String raw=accountText.getText();
				accountText.setText("");
				for (String line:raw.split("\n")){
					String []sep=line.split("\t");
					if (sep.length==2)
						UserBank.addUser(site, sep[0], sep[1]);
				}
				UserBank.save();
				updateGUI();
			}
		});
		panel.add(btnAddAccounts);
		
		updateGUI();
	}
	
	public void updateGUI(){
		userTable.setModel(new DefaultTableModel(
				UserBank.getUserInfo(),
				new String[] {
					"Site","Account","Password","Assigned","Ban","Assigned Proxy"
				}
			));
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		for (int i=0;i<6;i++)
			userTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		userTable.getColumn("Ban").setCellRenderer(new CheckerRender());
		userTable.getColumn("Ban").setCellEditor(new CheckerEditor(new JCheckBox(),this));
	}
	
	public void changeBan(int row){
		String site=(String)userTable.getModel().getValueAt(row, 0);
		String username=(String)userTable.getModel().getValueAt(row, 1);
		UserBank.changeBan(site,username);
		updateGUI();
	}

}
