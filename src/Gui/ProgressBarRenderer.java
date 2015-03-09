package Gui;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {

	  public ProgressBarRenderer() {
		  setOpaque(true);
		  setStringPainted(true);
	  }

	  public Component getTableCellRendererComponent(JTable table, Object value,
	      boolean isSelected, boolean hasFocus, int row, int column) {
	    try{
	    	if (isSelected) {
		      setForeground(table.getSelectionForeground());
		      setBackground(table.getSelectionBackground());
		    } else {
		      setForeground(table.getForeground());
		      setBackground(UIManager.getColor("Button.background"));
		    }
	    	if (value==null||(Integer)value<=0) {
	    		return new JLabel();
	    	}
	    	else {
	    		setVisible(true);
	    		setValue((Integer)value);
	    	}
	    }catch (Exception ex){
	    }
    	return this;
	  }
	}
