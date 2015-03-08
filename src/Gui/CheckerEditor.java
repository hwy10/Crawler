package Gui;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextField;

import Crawler.Control;

class CheckerEditor extends DefaultCellEditor {
	  private AccountWindow userWindow;
	  
	  public CheckerEditor(JCheckBox checkBox, AccountWindow userWindow) {
	    super(checkBox);
	    this.userWindow=userWindow;
	  }

	  public Component getTableCellEditorComponent(JTable table, Object value,
	      boolean isSelected, int row, int column) {
		  try{
			  userWindow.changeBan(row);
		  }catch (Exception ex){}
		  return null;
	  }
}