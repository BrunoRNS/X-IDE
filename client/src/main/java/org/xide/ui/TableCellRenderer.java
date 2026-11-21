package org.xide.ui;

import java.util.List;

import javax.swing.table.DefaultTableCellRenderer;

public class TableCellRenderer extends DefaultTableCellRenderer {

	@Override
	public void setValue(Object value) {
		if (value instanceof List) {
			List<String> values = (List<String>) value;

			setText(String.join(", ", values));
		}
		else {
			setText(value != null ? value.toString() : "");
		}
	}

}
