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

class ButtonEditor extends DefaultCellEditor {
	  protected JButton button;

	  private String label;

	  private boolean isPushed;
	  private Control core;
	  private JTextField input;

	  public ButtonEditor(JCheckBox checkBox, Control core, JTextField input) {
	    super(checkBox);
	    this.input=input;
	    this.core=core;
	    button = new JButton();
	    button.setOpaque(true);
	    button.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        fireEditingStopped();
	      }
	    });
	  }

	  public Component getTableCellEditorComponent(JTable table, Object value,
	      boolean isSelected, int row, int column) {
	    try{
	    	if (isSelected) {
		      button.setForeground(table.getSelectionForeground());
		      button.setBackground(table.getSelectionBackground());
		    } else {
		      button.setForeground(table.getForeground());
		      button.setBackground(table.getBackground());
		    }
		    label = (value == null) ? "" : value.toString();
		    button.setText(label);
		    isPushed = true;
	    }catch (Exception ex){}
	    return button;
	  }

	  public Object getCellEditorValue() {
	    if (isPushed) {
	    	if (label.startsWith("http")){
	    		input.setText(label);
	    	}else{ 
	    		new Thread(){
	    			public void run() {
	    				try{
	  			    	  String cmd[]=label.split("-");
	  				      if (cmd[1].equals("Start"))
	  				    	  core.execute("run "+cmd[0]);
	  				      else if (cmd[1].equals("Stop"))
	  				    	  core.execute("stop "+cmd[0]);
	  			      }catch (Exception ex){}	
	    			};
	    		}.start();
			      
			    }
			  }
	    isPushed = false;
	    return new String(label);
	  }

	  public boolean stopCellEditing() {
	    isPushed = false;
	    return super.stopCellEditing();
	  }

	  protected void fireEditingStopped() {
	    super.fireEditingStopped();
	  }
	}