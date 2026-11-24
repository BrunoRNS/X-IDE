package org.xide.ui;

import java.util.List;

import javax.swing.table.DefaultTableCellRenderer;

public class TableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public void setValue(Object value) {
		if (value instanceof List<?> values) {
			setText(String.join(", ", values.stream().map(String::valueOf).toList()));
		}
		else {
			setText(value != null ? value.toString() : "");
		}
	}

}