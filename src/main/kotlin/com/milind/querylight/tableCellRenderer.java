package com.milind.querylight;


import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class tableCellRenderer extends DefaultTableCellRenderer {
    SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    @Override
    public Component getTableCellRendererComponent(JTable jTable, Object o, boolean isSelected, boolean hasFocus, int row, int column) {

        Object value = o;
        if (value instanceof Date) {
            value = f.format(value);
            String tmp = f.format(value);
        }
        return super.getTableCellRendererComponent(jTable, o, isSelected, hasFocus , row, column);
    }

    private TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
        SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (value instanceof Date) {
                value = f.format(value);
            }
            return super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
        }
    };

    public TableCellRenderer getTableCellRenderer() {
        return tableCellRenderer;
    }
}
