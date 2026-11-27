package org.xide.ui;

import java.util.List;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * Table cell renderer that renders {@link java.util.List} values as a comma-separated
 * string and other values via {@code toString}.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
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