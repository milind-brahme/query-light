package com.milind.querylight;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import javax.swing.*;
import java.awt.*;
//import java.awt.datatransfer.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
            final StringBuffer txtToCopy = new StringBuffer();
            int colindx = tbl_grid.getSelectedColumn();
            int endcolindx = tbl_grid.getSelectedColumnCount() + colindx - 1;
            int rowindx = tbl_grid.getSelectedRow();
            int endrowindx = tbl_grid.getSelectedRowCount() + rowindx - 1;
            String linesep; 
            final StringBuffer htmlText = new StringBuffer();

           htmlText.append("<table>");
            htmlText.append("<tr>");
            if (tbl_grid.getSelectedRowCount()>1) {
                for (int ch = colindx; ch <= endcolindx; ch++) {
                    String sep = (ch == endcolindx) ? "" : "\t";
                    txtToCopy.append(tbl_grid.getColumnName(ch) + sep);
                   htmlText.append("<td>" + tbl_grid.getColumnName(ch) + "</td>" );
                }
                linesep=System.getProperty("line.separator");
                txtToCopy.append(linesep);
                htmlText.append("<tr>");
            }else
            {
                linesep= "";
            }
            
            for (int r = rowindx; r <= endrowindx; r++) {
                htmlText.append("\n<tr>");
                for (int c = colindx; c <= endcolindx; c++) {
                    String sep = (c == endcolindx) ? "" : "\t";
                    if (tbl_grid.getValueAt(r, c) == null) {
                        txtToCopy.append(sep);
                        htmlText.append("<td></td>");
                    } else if (tbl_grid.getValueAt(r,c) instanceof Date) {
                            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String tmpdate = f.format(tbl_grid.getValueAt(r,c));
                            txtToCopy.append(tmpdate + sep);
                        htmlText.append("<td>" + tmpdate  + "</td>");
                }else
                        {
                        txtToCopy.append(escape(tbl_grid.getValueAt(r, c)) + sep);
                            htmlText.append("<td>" + tbl_grid.getValueAt(r, c)  + "</td>");
                    }
                }
                txtToCopy.append(linesep);
                htmlText.append("</tr>");
            }
            htmlText.append("</table>");
            //StringSelection sel  = new StringSelection(txtToCopy.toString());
            //CLIPBOARD.setContents(sel, sel);
            Transferable t = new Transferable() {

                    private  final ArrayList<DataFlavor> flavors = new ArrayList<DataFlavor>(Arrays.asList( returnHtmlFlavor() , DataFlavor.stringFlavor));

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return ( flavors.equals( returnHtmlFlavor()) || flavors.equals(DataFlavor.stringFlavor));
                    };

                    private DataFlavor returnHtmlFlavor()
                    {
                        try {
                            return new DataFlavor("text/html;class=java.lang.String");
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return flavors.toArray(new DataFlavor[0]);
                    }

                    @Override
                    public Object getTransferData(DataFlavor flavor) throws IOException, UnsupportedFlavorException {
                        if (flavor.equals( returnHtmlFlavor())) {

                            return htmlText.toString();
                        }
                        else  {
                           return txtToCopy.toString();
                        }
                    }

            };
            ClipboardOwner clipBoardOwner  = new ClipboardOwner() {
                @Override
                public void lostOwnership(Clipboard clipboard, Transferable transferable) {

                }
            };

            CLIPBOARD.setContents(t,clipBoardOwner );


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
    
