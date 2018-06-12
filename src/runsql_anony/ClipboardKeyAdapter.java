/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runsql_anony;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author hv655
 */
    public class ClipboardKeyAdapter extends KeyAdapter {

        public ClipboardKeyAdapter(JTable table , boolean copyEnabled, boolean cutEnabled, boolean pasteEnabled) {
            this.copyEnabled = copyEnabled;
            this.cutEnabled = cutEnabled;
            this.pasteEnabled = pasteEnabled;
            this.table = table;
        }

        private static final String LINE_BREAK = "\n"; 
        private static final String CELL_BREAK = "\t"; 
        private  final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard(); 
        private boolean copyEnabled ;
        private boolean cutEnabled;
        private boolean pasteEnabled;
        
        private final JTable table; 
        
        /**
         *
         * @param table
         */
        public ClipboardKeyAdapter(JTable table) { 
                this.table = table; 
                this.copyEnabled=true;
                this.pasteEnabled=false;
                this.cutEnabled=false;
        } 
        
        @Override 
        public void keyReleased(KeyEvent event) { 
                if (event.isControlDown()) { 
                        if (event.getKeyCode()==KeyEvent.VK_C && copyEnabled) { // Copy                        
                                cancelEditing(); 
                                copyToClipboard(false); 
                        } else if (event.getKeyCode()==KeyEvent.VK_X  && cutEnabled) { // Cut 
                                cancelEditing(); 
                                copyToClipboard(true); 
                        } else if (event.getKeyCode()==KeyEvent.VK_V  && pasteEnabled) { // Paste 
                                cancelEditing(); 
                                pasteFromClipboard();           
                        } 
                } 
        } 
        
        private void copyToClipboard(boolean isCut) { 
            JTable tbl_grid = this.table;
            StringBuffer txtToCopy = new StringBuffer();
            int colindx = tbl_grid.getSelectedColumn();
            int endcolindx = tbl_grid.getSelectedColumnCount() + colindx - 1;
            int rowindx = tbl_grid.getSelectedRow();
            int endrowindx = tbl_grid.getSelectedRowCount() + rowindx - 1;
            String linesep; 
            if (tbl_grid.getSelectedRowCount()>1) {
                for (int ch = colindx; ch <= endcolindx; ch++) {
                    String sep = (ch == endcolindx) ? "" : "\t";
                    txtToCopy.append(tbl_grid.getColumnName(ch) + sep);
                }
                linesep=System.getProperty("line.separator");
                txtToCopy.append(linesep);
            }else
            {
                linesep= "";
            }
            
            for (int r = rowindx; r <= endrowindx; r++) {
                for (int c = colindx; c <= endcolindx; c++) {
                    String sep = (c == endcolindx) ? "" : "\t";
                    if (tbl_grid.getValueAt(r, c) == null) {
                        txtToCopy.append(sep);
                    } else if (tbl_grid.getValueAt(r,c) instanceof Date) {
                            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String tmpdate = f.format(tbl_grid.getValueAt(r,c));
                            txtToCopy.append(tmpdate + sep);
                    }else
                        {
                        txtToCopy.append(escape(tbl_grid.getValueAt(r, c)) + sep);
                    }
                }
                txtToCopy.append(linesep);
            }
            StringSelection sel  = new StringSelection(txtToCopy.toString()); 
            CLIPBOARD.setContents(sel, sel);
            
            if (isCut) {
                int numCols=table.getSelectedColumnCount(); 
                int numRows=table.getSelectedRowCount(); 
                int[] rowsSelected=table.getSelectedRows(); 
                int[] colsSelected=table.getSelectedColumns(); 
                if (numRows!=rowsSelected[rowsSelected.length-1]-rowsSelected[0]+1 || numRows!=rowsSelected.length || 
                                numCols!=colsSelected[colsSelected.length-1]-colsSelected[0]+1 || numCols!=colsSelected.length) {

                        JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
                        return; 
                } 
                
                StringBuffer excelStr=new StringBuffer(); 
                for (int i=0; i<numRows; i++) { 
                        for (int j=0; j<numCols; j++) { 
                                excelStr.append(escape(table.getValueAt(rowsSelected[i], colsSelected[j]))); 
                                if (isCut) { 
                                        table.setValueAt(null, rowsSelected[i], colsSelected[j]); 
                                } 
                                if (j<numCols-1) { 
                                        excelStr.append(CELL_BREAK); 
                                } 
                        } 
                        excelStr.append(LINE_BREAK); 
                } 
                
                sel  = new StringSelection(excelStr.toString()); 
                CLIPBOARD.setContents(sel, sel); 
            }
        } 
        
        private void pasteFromClipboard() { 
                int startRow=table.getSelectedRows()[0]; 
                int startCol=table.getSelectedColumns()[0];

                String pasteString = ""; 
                try { 
                        pasteString = (String)(CLIPBOARD.getContents(this).getTransferData(DataFlavor.stringFlavor)); 
                } catch (Exception e) { 
                        JOptionPane.showMessageDialog(null, "Invalid Paste Type", "Invalid Paste Type", JOptionPane.ERROR_MESSAGE);
                        return; 
                } 
                
                String[] lines = pasteString.split(LINE_BREAK); 
                for (int i=0 ; i<lines.length; i++) { 
                        String[] cells = lines[i].split(CELL_BREAK); 
                        for (int j=0 ; j<cells.length; j++) { 
                                if (table.getRowCount()>startRow+i && table.getColumnCount()>startCol+j) { 
                                        table.setValueAt(cells[j], startRow+i, startCol+j); 
                                } 
                        } 
                } 
        } 
        
        private void cancelEditing() { 
                if (table.getCellEditor() != null) { 
                        table.getCellEditor().cancelCellEditing(); 
            } 
        } 
        
        private String escape(Object cell) { 
                return cell.toString().replace(LINE_BREAK, " ").replace(CELL_BREAK, " "); 
        } 
    } 
    
