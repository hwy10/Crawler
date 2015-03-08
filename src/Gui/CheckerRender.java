package Gui;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

class CheckerRender extends JCheckBox implements TableCellRenderer {

	  public CheckerRender() {
		  setOpaque(true);
		  setHorizontalAlignment( JLabel.CENTER );
	  }

	  public Component getTableCellRendererComponent(JTable table, Object value,
	      boolean isSelected, boolean hasFocus, int row, int column) {
	    try{
	    	if (isSelected) {
		      setForeground(table.getSelectionForeground());
		      setBackground(table.getSelectionBackground());
		    } else {
		      setForeground(table.getForeground());
		      setBackground(UIManager.getColor("CheckBox.background"));
		    }
	    	if (value.toString().equals("true")) this.setSelected(true);
	    	else this.setSelected(false);
	    }catch (Exception ex){
	    }
    	return this;
	  }
	}
