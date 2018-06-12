/*
 * Copyright (C) 2017 hv655
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package runsql_anony;


import oracle_xls_extract.Oracle_xls_extract;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.TRUE;

/**
 * @author hv655
 */


public class excelOps extends javax.swing.JFrame {

    // Variables declaration - do not modify//GEN-BEGIN:variables

    private javax.swing.JButton btnCommit;
    private javax.swing.JButton btnConvertToJDBC;
    private javax.swing.JButton btnReadNow;
    private javax.swing.JButton btnRollback;
    private javax.swing.JButton btnStartOp;
    private javax.swing.JButton btnfileDlg;
    private javax.swing.JCheckBox chkCreateXls;
    private javax.swing.JCheckBox chkDataHasHeader;
    private javax.swing.ButtonGroup grpOperationChoice;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JMenuItem menuItemExportToExcel;
    private javax.swing.JMenuItem menuItemInsertIntoTable;
    private javax.swing.JPopupMenu mnu_grid;
    private javax.swing.JRadioButton radDML;
    private javax.swing.JRadioButton radSQL;
    private javax.swing.JTextField srcJDBC;
    private javax.swing.JPasswordField srcPass;
    private javax.swing.JTextField srcUser;
    private javax.swing.JTable tabMappings;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JPanel tabPanel0;
    private javax.swing.JPanel tabPanel1;
    private javax.swing.JTable tblExcel;
    private javax.swing.JTextField txtColXlsFileName;
    private javax.swing.JTextField txtColumnForOperation;
    private javax.swing.JTextField txtFileName;
    private javax.swing.JTextField txtSheetNumber;
    private javax.swing.JTextField txtTableName;
    private javax.swing.JTextArea txtTnsString;
    private javax.swing.JTextField  txtBatchInsTableCol = new JTextField();
    private javax.swing.JTextField  txtBatchInsertStmtCol = new  JTextField() ;
    private JCheckBox chkbatchInsert = new JCheckBox("Batch Insert using Excel columns" , false);
    // End of variables declaration//GEN-END:variables

    public class TableHeaderMouseListener extends MouseAdapter {

        private JTable table;

        public TableHeaderMouseListener(JTable table) {
            this.table = table;
        }

        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                Point point = event.getPoint();
                int columnNum = (table.columnAtPoint(point));
                StringSelection stringSelection = new StringSelection(Integer.toString(columnNum));
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
                txtColumnForOperation.setText(Integer.toString(columnNum));
            }
        }
    }

    /**
     * Creates new form excelOps
     */
    private JdbcPersistent jcon;
    private Vector<Object> columnNames = new Vector<Object>();
    private Vector<Object> data = new Vector<Object>();

    public Vector<Object> getColumnNames() {
        return columnNames;
    }

    public Vector<Object> getData() {
        return data;
    }

    public excelOps(JdbcPersistent jc) {
        initComponents();
        tblExcel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    mnu_grid.show(tblExcel, e.getX(), e.getY());
                }
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();
                    StringSelection stringSelection = new StringSelection(target.getValueAt(row, column).toString());
                    Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clpbrd.setContents(stringSelection, null);
                }
            }
        });
        JTableHeader header = this.tblExcel.getTableHeader();
        header.addMouseListener(new TableHeaderMouseListener(tblExcel));

        this.srcUser.setText(jc.getUid());
        this.srcPass.setText(jc.getPwd());
        this.srcJDBC.setText(jc.getJdbcstr());
    }

    public JTable getTabMappings() {
        return tabMappings;
    }

    public JTable getTblExcel() {
        return tblExcel;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpOperationChoice = new javax.swing.ButtonGroup();
        mnu_grid = new javax.swing.JPopupMenu();
        menuItemExportToExcel = new javax.swing.JMenuItem();
        menuItemInsertIntoTable = new javax.swing.JMenuItem();
        tabPane = new javax.swing.JTabbedPane();
        tabPanel0 = new javax.swing.JPanel();
        txtFileName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btnfileDlg = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        srcUser = new javax.swing.JTextField();
        srcPass = new javax.swing.JPasswordField();
        srcJDBC = new javax.swing.JTextField();
        radSQL = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        radDML = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        txtColumnForOperation = new javax.swing.JTextField();
        btnConvertToJDBC = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtTnsString = new javax.swing.JTextArea();
        btnReadNow = new javax.swing.JButton();
        btnStartOp = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        txtSheetNumber = new javax.swing.JTextField();
        btnCommit = new javax.swing.JButton();
        btnRollback = new javax.swing.JButton();
        chkDataHasHeader = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblExcel = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        txtColXlsFileName = new javax.swing.JTextField();
        chkCreateXls = new javax.swing.JCheckBox();
        tabPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        txtTableName = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabMappings = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();

        menuItemExportToExcel.setText("Export to Excel");
        menuItemExportToExcel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        menuItemExportToExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/runsql_anony/CSV-50.png"))); // NOI18N
        menuItemExportToExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemExportToExcelActionPerformed(evt);
            }
        });
        mnu_grid.add(menuItemExportToExcel);

        menuItemInsertIntoTable.setText("Insert into Table");
        menuItemInsertIntoTable.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        mnu_grid.add(menuItemInsertIntoTable);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("File Name :");

        btnfileDlg.setText("...");
        btnfileDlg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnfileDlgActionPerformed(evt);
            }
        });

        jLabel2.setText("Username");

        jLabel3.setText("Password");

        jLabel4.setText("JDBCString");

        radSQL.setSelected(true);
        radSQL.setText("SQL");
        this.grpOperationChoice.add(radSQL);
        radSQL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radSQLActionPerformed(evt);
            }
        });
        radSQL.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                radSQLPropertyChange(evt);
            }
        });

        jLabel5.setText("Perform :");

        radDML.setText("DML");
        this.grpOperationChoice.add(radDML);
        radDML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radDMLActionPerformed(evt);
            }
        });
        radDML.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                radDMLPropertyChange(evt);
            }
        });

        jLabel6.setText("On Column");

        txtColumnForOperation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtColumnForOperationActionPerformed(evt);
            }
        });

        btnConvertToJDBC.setText("Convet to JDBC URL");
        btnConvertToJDBC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConvertToJDBCActionPerformed(evt);
            }
        });

        txtTnsString.setColumns(20);
        txtTnsString.setRows(5);
        jScrollPane2.setViewportView(txtTnsString);

        btnReadNow.setText("Read File Now");
        btnReadNow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReadNowActionPerformed(evt);
            }
        });

        btnStartOp.setText("Start ");
        btnStartOp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartOpActionPerformed(evt);
            }
        });

        jLabel7.setText("From Sheet : ");

        txtSheetNumber.setText("1");

        btnCommit.setText("Commit");
        btnCommit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCommitActionPerformed(evt);
            }
        });

        btnRollback.setText("Rollback");
        btnRollback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRollbackActionPerformed(evt);
            }
        });

        chkDataHasHeader.setSelected(true);
        chkDataHasHeader.setText("Data has Header");
        chkDataHasHeader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDataHasHeaderActionPerformed(evt);
            }
        });

        tblExcel.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "Column 1", "Column 2", "Column 3", "Column 4"
                }
        ));
        tblExcel.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(tblExcel);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Batch Operation"));

        jLabel9.setText("Column having file Names");

        chkCreateXls.setText("Create Excel files based on SQL");

        //javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout (jPanel1);
       BorderLayout jPanel1Layout = new BorderLayout();
       jPanel1.setLayout(jPanel1Layout);
        jPanel1.setPreferredSize(new Dimension(250,105  ));

        JPanel jp1 = new JPanel();
        jPanel1.add(jp1,BorderLayout.PAGE_START);

        jp1.add(chkCreateXls );
        txtColXlsFileName.setPreferredSize(new Dimension(25,25));
        jp1.add(txtColXlsFileName   );
        jp1.add(jLabel9);

        JPanel jp2 = new JPanel();
        jPanel1.add(jp2,BorderLayout.PAGE_END);


        jp2.add(chkbatchInsert);
        txtBatchInsTableCol.setPreferredSize(new Dimension(25,25));
        jp2.add(txtBatchInsTableCol);
        jp2.add(new JLabel("Dest Table ColIndx"));
        txtBatchInsertStmtCol.setPreferredSize(new Dimension(25,25));
        jp2.add(txtBatchInsertStmtCol);
        jp2.add(new JLabel("Select stmt ColIndx"));



        /*
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(chkCreateXls)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtColXlsFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(108, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(chkCreateXls)
                                        .addComponent(jLabel9)
                                        .addComponent(txtColXlsFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 38, Short.MAX_VALUE))
        );
        */

        javax.swing.GroupLayout tabPanel0Layout = new javax.swing.GroupLayout(tabPanel0);
        tabPanel0.setLayout(tabPanel0Layout);
        tabPanel0Layout.setHorizontalGroup(
                tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1)
                                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(jLabel2)
                                                                                        .addComponent(srcUser, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                        .addComponent(srcPass, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                .addGap(18, 18, 18)
                                                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(jLabel4)
                                                                                        .addComponent(srcJDBC, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                                                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                        .addComponent(jLabel5))
                                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                        .addComponent(txtFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                                                                                                .addComponent(radSQL)
                                                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                .addComponent(radDML)
                                                                                                                .addGap(12, 12, 12)
                                                                                                                .addComponent(jLabel6)
                                                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                .addComponent(txtColumnForOperation, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                .addComponent(jLabel7)))
                                                                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                                                                                                .addGap(19, 19, 19)
                                                                                                                .addComponent(btnfileDlg))
                                                                                                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                                                                                                .addGap(6, 6, 6)
                                                                                                                .addComponent(txtSheetNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                .addComponent(chkDataHasHeader)))
                                                                                                .addGap(0, 7, Short.MAX_VALUE))
                                                                                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                                        .addComponent(btnReadNow, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                                                                                        .addComponent(btnCommit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                        .addComponent(btnRollback, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                                                .addGap(51, 51, 51)
                                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(btnConvertToJDBC)
                                                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 583, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                        .addComponent(btnStartOp, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 208, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        tabPanel0Layout.setVerticalGroup(
                tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnConvertToJDBC)
                                                .addGap(49, 49, 49))
                                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(srcUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(srcJDBC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(srcPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(txtFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel1)
                                                        .addComponent(btnfileDlg)
                                                        .addComponent(btnReadNow))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(btnCommit, javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(radSQL)
                                                                .addComponent(jLabel5)
                                                                .addComponent(radDML)
                                                                .addComponent(jLabel6)
                                                                .addComponent(txtColumnForOperation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jLabel7)
                                                                .addComponent(txtSheetNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(chkDataHasHeader)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(tabPanel0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(tabPanel0Layout.createSequentialGroup()
                                                                .addComponent(btnRollback)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnStartOp, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                                .addGap(98, 98, 98))
        );

        tabPane.addTab("Perform DML / SQL on Excel column", tabPanel0);

        jLabel8.setText("Table Name");

        txtTableName.setText("Table Name");
        txtTableName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtTableNameFocusLost(evt);
            }
        });
        txtTableName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTableNameActionPerformed(evt);
            }
        });

        tabMappings.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null}
                },
                new String[]{
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }
        ));
        tabMappings.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane3.setViewportView(tabMappings);

        jButton1.setText("Proceed to Next Step >");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tabPanel1Layout = new javax.swing.GroupLayout(tabPanel1);
        tabPanel1.setLayout(tabPanel1Layout);
        tabPanel1Layout.setHorizontalGroup(
                tabPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(tabPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(tabPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane3)
                                        .addGroup(tabPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel8)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtTableName, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(jButton1)
                                                .addGap(0, 918, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        tabPanel1Layout.setVerticalGroup(
                tabPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(tabPanel1Layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addGroup(tabPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel8)
                                        .addComponent(txtTableName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton1))
                                .addGap(49, 49, 49)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(321, Short.MAX_VALUE))
        );

        tabPane.addTab("Insert into Table ", tabPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(tabPane)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(tabPane)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private String fileNameToRead() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            return (fileToSave.getAbsolutePath());
        }
        return null;
    }

    private void btnConvertToJDBCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConvertToJDBCActionPerformed
        String TNSentry = this.txtTnsString.getText().toLowerCase();
        String hostPattern = "host\\s*=\\s*(.+?)\\)";
        String portPattern = "port\\s*=\\s*(.+?)\\)";
        String sidPattern = "sid\\s*=\\s*(.+?)\\)";
        String service_namePattern = "service_name\\s*=\\s*(.+?)\\)";
        String HostName;
        String PortName;
        String serviceName;
        String SID;
        Pattern r = Pattern.compile(hostPattern, Pattern.MULTILINE);
        Matcher m = r.matcher(TNSentry.toLowerCase());
        if (m.find()) {
            HostName = m.group(1);
        } else {
            HostName = null;
        }

        r = Pattern.compile(portPattern, Pattern.MULTILINE);
        m = r.matcher(TNSentry.toLowerCase());
        if (m.find()) {
            PortName = m.group(1);
        } else {
            PortName = null;
        }
        r = Pattern.compile(sidPattern, Pattern.MULTILINE);
        m = r.matcher(TNSentry.toLowerCase());
        if (m.find()) {
            SID = m.group(1);
        } else {
            SID = null;
        }
        r = Pattern.compile(service_namePattern, Pattern.MULTILINE);
        m = r.matcher(TNSentry.toLowerCase());
        if (m.find()) {
            serviceName = m.group(1);
        } else {
            serviceName = null;
        }

        if (HostName != null && PortName != null) {
            if (SID != null) {
                this.txtTnsString.setText("jdbc:oracle:thin:@" + HostName.trim() + ":" + PortName.trim() + ":" + SID.trim());
            } else if (serviceName != null) {
                this.txtTnsString.setText("jdbc:oracle:thin:@" + HostName.trim() + ":" + PortName.trim() + ":" + serviceName.trim());
            }
        }
    }//GEN-LAST:event_btnConvertToJDBCActionPerformed

    private void txtColumnForOperationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtColumnForOperationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtColumnForOperationActionPerformed

    private void radDMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDMLActionPerformed
        // TODO add your handling code here:
        if (radDML.isSelected()) {
            btnStartOp.setText("Start DML on Column " + this.txtColumnForOperation.getText());
        }
    }//GEN-LAST:event_radDMLActionPerformed

    private void btnfileDlgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnfileDlgActionPerformed
        // TODO add your handling code here:
        txtFileName.setText(fileNameToRead());
    }//GEN-LAST:event_btnfileDlgActionPerformed

    private void preLoad()
    {
        this.data.clear();
        this.columnNames.clear();
        columnNames.add("#");
    }
    public void loadGridFromDb(JTable src)
    {
        preLoad();
        TableModel srcTM = src.getModel();
        for (int i=1 ; i < src.getColumnCount() ; i++ )
        {        this.columnNames.add(src.getColumnName(i));  }

        for(int row=0; row< src.getRowCount(); row++) {
            Vector<Object> tblrow = new Vector<Object>();
            for (int col = 0; col < src.getColumnCount(); col++) {
                tblrow.add(srcTM.getValueAt(row,col));
            }
            data.add(tblrow);
        }
        refreshTableModel();

    }
    private void btnReadNowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReadNowActionPerformed
        FileInputStream fis = null;
        preLoad();
        int recordCount = 1;
        int sheeetno = Integer.parseInt(txtSheetNumber.getText()) - 1;
        try {
            String fname = txtFileName.getText();
            fis = new FileInputStream(new File(fname));
            XSSFWorkbook readW = new XSSFWorkbook(fis);
            XSSFSheet sheet = readW.getSheetAt(sheeetno);
            Iterator<Row> ite = sheet.rowIterator();
            while (ite.hasNext()) {
                Row row = ite.next();
                if(row.getLastCellNum()>=0) {
                    Vector<Object> tblrow = new Vector<Object>(row.getLastCellNum());
                    tblrow.add(recordCount);
                    for (int i = 0; i <= row.getLastCellNum(); i++) {
                        XSSFCell cell = (XSSFCell) row.getCell(i);

                        Object cellValue = null;
                        if (i + 1 > columnNames.size() - 1) {
                            if (chkDataHasHeader.isSelected() && recordCount == 1) {
                                try {
                                    columnNames.add(cell.getStringCellValue());
                                } catch (Exception e) {
                                    columnNames.add("column " + Integer.toString(i + 1));
                                }
                                continue;
                            } else {
                                columnNames.add("Column " + Integer.toString(i + 1));
                            }
                        }
                        try {
                            cellValue = cell.getStringCellValue();
                        } catch (Exception e) {
                            try {
                                if (cell.getCellTypeEnum() == CellType.NUMERIC && !DateUtil.isCellDateFormatted(cell)) {
                                    cellValue = cell.getNumericCellValue();
                                } else if (DateUtil.isCellDateFormatted(cell)) {
                                    cellValue = cell.getDateCellValue();
                                } else {
                                    cellValue = null;
                                }

                            } catch (Exception e1) {

                            }

                        }

                        tblrow.add(cellValue);
                    }
                    data.add(tblrow);
                    recordCount++;
                }
            }
            refreshTableModel();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(excelOps.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(excelOps.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(excelOps.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }//GEN-LAST:event_btnReadNowActionPerformed

    public void refreshTableModel() {
        final DefaultTableModel model;
        model = new DefaultTableModel((Vector) data, columnNames) {
            @Override
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return true;
            }

            @Override
            public Class getColumnClass(int column) {
                for (int row = 0; row < this.getRowCount(); row++) {
                    Object o = getValueAt(row, column);

                    if (o != null) {
                        return o.getClass();
                    }
                }

                return Object.class;
            }

        };
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent tme) {
                int row = tme.getFirstRow();
                int col = tme.getColumn();
                Object o = tme.getSource();
                setCellValue(o, row, col);
            }
        });

        tblExcel.setModel(model);
        //    RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        //    this.tblExcel.setRowSorter(sorter);
        //     ArrayList<RowSorter.SortKey> list = new ArrayList<RowSorter.SortKey>();
        //     list.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        //   sorter.setSortKeys(list);

    }

    private void radSQLPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_radSQLPropertyChange
        // TODO add your handling code here:

    }//GEN-LAST:event_radSQLPropertyChange

    private void radDMLPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_radDMLPropertyChange
        // TODO add your handling code here:

    }//GEN-LAST:event_radDMLPropertyChange

    private void setCellValue(Object o, int row, int col) {
        Vector<Object> tblrow = (Vector<Object>) data.get(row);
        if (col < tblrow.size()) {
            Object tmp = tblrow.get(col);
            tmp = o;

        } else {
            tblrow.add(col, o);
        }

    }

    private void callBatchInsertProcess()
    {
        if (jcon==null) try {
            jcon = new JdbcPersistent(this.srcUser.getText(), new String(this.srcPass.getPassword()), this.srcJDBC.getText());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        insertFromAnotherDb.openInsertDialog(jcon, this);

    }

    public boolean peformBatchInsert()
    {
        return this.chkbatchInsert.isSelected();
    }

    public int batchInsTableColIndex()
    {
        return ( Integer.parseInt(this.txtBatchInsTableCol.getText()) + 1 ) ;
    }

    public int batchInsStmtColIndex()
    {
        return (Integer.parseInt(this.txtBatchInsertStmtCol.getText()) +  1)  ;
    }



    private void btnStartOpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartOpActionPerformed
        // TODO add your handling code here:
        if (chkbatchInsert.isSelected())  { callBatchInsertProcess() ; return;}

        int columnNumber = Integer.parseInt(this.txtColumnForOperation.getText()) + 1;
        int columnCount = tblExcel.getColumnCount();
        try {
            if (!this.jcon.getConn().isValid(java.sql.Connection.TRANSACTION_READ_COMMITTED) || !jcon.getJdbcstr().toLowerCase().equals(this.srcJDBC.getText().toLowerCase())) {
                this.jcon = new JdbcPersistent(this.srcUser.getText(), new String(this.srcPass.getPassword()), this.srcJDBC.getText());
            }
        } catch (java.lang.NullPointerException ex) {
            try {
                this.jcon = new JdbcPersistent(this.srcUser.getText(), new String(this.srcPass.getPassword()), this.srcJDBC.getText());
            } catch (Exception ex1) {
                Logger.getLogger(excelOps.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (Exception ex) {
            Logger.getLogger(excelOps.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (radSQL.isSelected()) {
            try {

                for (int rowNumber = 0; rowNumber < tblExcel.getRowCount(); rowNumber++) {
                    String qry = null;
                    try {
                        qry = tblExcel.getValueAt(rowNumber, columnNumber - 1).toString();
                    } catch (Exception e) {
                    }
                    if (qry != null) {

                        if (!chkCreateXls.isSelected()) {
                            ResultSet rs = null;
                            try {
                                rs = jcon.runqry(qry);
                            } catch (SQLException sQLException) {
                                Logger.getLogger(excelOps.class.getName()).log(Level.SEVERE, null, sQLException);
                                continue;
                            }
                            if (rs != null && rs.next()) {
                                ResultSetMetaData md = rs.getMetaData();
                                int columns = md.getColumnCount();

                                for (int i = 1; i <= columns; i++) {
                                    if (this.columnNames.size() < columnCount + i) {
                                        this.columnNames.add(md.getColumnName(i));
                                    }
                                    try {
                                        if (md.getColumnTypeName(i).equals("DATE")) {
                                            setCellValue(rs.getTimestamp(qry), rowNumber, columnCount + i - 1);
                                        } else {
                                            setCellValue(rs.getString(i), rowNumber, columnCount + i - 1);
                                        }
                                    } catch (NullPointerException e) {
                                        setCellValue("", rowNumber, columnCount + i - 1);
                                    }
                                }
                            }
                        } else if (txtColXlsFileName.getText() != null && !txtColXlsFileName.getText().isEmpty()) {
                            String fname = tblExcel.getValueAt(rowNumber, Integer.parseInt(txtColXlsFileName.getText())).toString().trim();
                            System.out.println("File Name about to process is :" + new File(fname).getAbsolutePath()  + " from field : " + txtColXlsFileName.getText());
                            Oracle_xls_extract.main(new String[]{"-u", jcon.getUid(), "-p", jcon.getPwd(), "-j", jcon.getJdbcstr(), "-q", qry, "-r", fname});
                            if (new File(fname).isFile() && new File(fname).length() > 0) {
                                setCellValue("File" + fname + "created", rowNumber, columnCount);
                            }
                        }
                    }

                }
            } catch (Exception ex) {
                Logger.getLogger(excelOps.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {

                this.columnNames.add("Result");
                for (int rowNumber = 0; rowNumber < tblExcel.getRowCount(); rowNumber++) {
                    String qry = null;
                    try {
                        qry = tblExcel.getValueAt(rowNumber, columnNumber - 1).toString();
                    } catch (Exception e) {

                    }
                    if (qry != null) {
                        String result = null;
                        try {
                            result = jcon.execute_stmt(qry, TRUE);
                        } catch (Exception e) {
                            setCellValue("", rowNumber, columnCount);
                        }
                        setCellValue(result, rowNumber, columnCount);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(excelOps.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        refreshTableModel();
    }//GEN-LAST:event_btnStartOpActionPerformed

    private void btnCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommitActionPerformed
        try {
            // TODO add your handling code here:
            this.jcon.getConn().commit();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            JOptionPane.showMessageDialog(this, "Commit complete at " + dateFormat.format(date));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.toString());

        }
    }//GEN-LAST:event_btnCommitActionPerformed

    private void btnRollbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRollbackActionPerformed
        try {
            // TODO add your handling code here:
            this.jcon.getConn().rollback();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            JOptionPane.showMessageDialog(this, "Rollback complete at " + dateFormat.format(date));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error " + ex.getMessage());
        }
    }//GEN-LAST:event_btnRollbackActionPerformed

    private void menuItemExportToExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExportToExcelActionPerformed
        String tmp = fileNameToRead();
        String fname = null;
        if (tmp != null) {
            fname = (tmp.toLowerCase().endsWith(".xlsx")) ? tmp : tmp + ".xlsx";
        } else {
            return;
        }
        SXSSFWorkbook wworkbook = new SXSSFWorkbook(100);
        SXSSFSheet wsheet;
        boolean include_header = this.chkDataHasHeader.isSelected();
        //   PrintWriter writer = new PrintWriter(fname_path, "UTF-8");
        if (wworkbook.getNumberOfSheets() > 0) {
            wsheet = wworkbook.getSheetAt(wworkbook.getActiveSheetIndex());
        } else {
            wsheet = wworkbook.createSheet("1");
        }

        try {
            Vector<Object> columnNames = new Vector<Object>();

            int columns = this.columnNames.size();
            CreationHelper createHelper = wworkbook.getCreationHelper();
            CellStyle datecellStyle = wworkbook.createCellStyle();
            datecellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MMM-yyyy hh:mm"));
            CellStyle headerstyle = wworkbook.createCellStyle();
            Font font = wworkbook.createFont();
            font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
            font.setFontHeightInPoints((short) 10);
            font.setBold(true);
            headerstyle.setFont(font);
            SXSSFRow hdrrow = null;
            hdrrow = wsheet.createRow(0);

            for (int i = 0; (i < columns && include_header); i++) {
                Vector<Object> tblrow = (Vector<Object>) data.get(0);
                if (!include_header) {

                    SXSSFCell cell = hdrrow.createCell(i);
                    try {
                        if (tblrow.get(i) instanceof Date) {
                            cell.setCellValue((Date) tblrow.get(i));
                        } else if (tblrow.get(i) instanceof String) {
                            cell.setCellValue((String) tblrow.get(i));
                        } else if (tblrow.get(i) instanceof Double) {
                            cell.setCellValue((Double) tblrow.get(i));
                        }

                    } catch (Exception e) {
                        cell.setCellValue(createHelper.createRichTextString(null));
                    }
                } else {

                    SXSSFCell cell = hdrrow.createCell(i);
                    cell.setCellValue((String) this.columnNames.get(i));
                    cell.setCellStyle(headerstyle);
                }

            }

            for (int row_count = 1; row_count < data.size(); row_count++) {
                Vector<Object> tblrow = (Vector<Object>) data.get(row_count);
                SXSSFRow datarow = wsheet.createRow(row_count);

                for (int col = 0; col < columns; col++) {
                    String sep = (col == columns) ? "" : ",";
                    SXSSFCell cell = datarow.createCell(col);
                    try {
                        if (tblrow.get(col) instanceof Date) {
                            cell.setCellValue((Date) tblrow.get(col));
                            cell.setCellStyle(datecellStyle);
                        } else if (tblrow.get(col) instanceof String) {
                            cell.setCellValue((String) tblrow.get(col));
                        } else if (tblrow.get(col) instanceof Double || tblrow.get(col) instanceof Integer) {
                            cell.setCellValue((Double) tblrow.get(col));
                        }

                    } catch (Exception e) {
                        cell.setCellValue(createHelper.createRichTextString(null));
                    }
                }
                //  writer.println(csvline);

            }
            FileOutputStream fileOut = new FileOutputStream(new File(fname));
            wworkbook.write(fileOut);
            fileOut.flush();
            fileOut.close();
            wworkbook.dispose();
            System.out.println("File Written : " + fname);
            JOptionPane.showMessageDialog(this, "File Written : " + fname);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            Logger.getLogger(excelOps.class.getName()).log(Level.SEVERE, null, e);
        }
        // return XLS;

    }//GEN-LAST:event_menuItemExportToExcelActionPerformed

    private void chkDataHasHeaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDataHasHeaderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkDataHasHeaderActionPerformed

    private void txtTableNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTableNameFocusLost
        try {
            // TODO add your handling code here:
            try {
                if (!this.jcon.getConn().isValid(java.sql.Connection.TRANSACTION_READ_COMMITTED) || !jcon.getJdbcstr().toLowerCase().equals(this.srcJDBC.getText().toLowerCase())) {
                    this.jcon = new JdbcPersistent(this.srcUser.getText(), new String(this.srcPass.getPassword()), this.srcJDBC.getText());
                }
            } catch (java.lang.NullPointerException ex) {
                try {
                    this.jcon = new JdbcPersistent(this.srcUser.getText(), new String(this.srcPass.getPassword()), this.srcJDBC.getText());
                } catch (Exception ex1) {
                    Logger.getLogger(excelOps.class.getName()).log(Level.SEVERE, null, ex1);
                }
            } catch (Exception ex) {
                Logger.getLogger(excelOps.class.getName()).log(Level.SEVERE, null, ex);
            }

            java.sql.PreparedStatement destSTMT = jcon.getConn().prepareStatement("select * from " + this.txtTableName.getText() + " where 1=0");
            jcon.setRs(destSTMT.executeQuery());
            ResultSetMetaData destMetaData = jcon.getRs().getMetaData();
            int destColumns = destMetaData.getColumnCount();
            ArrayList<String> tableColumnNames = new ArrayList();
            ArrayList<JComboBox> excelCols = new ArrayList();
            ArrayList<String> MappedCols = new ArrayList();
            final DefaultTableModel model;


            for (int i = 1; i <= destColumns; i++) {
                tableColumnNames.add(destMetaData.getColumnName(i));
                JComboBox j = new JComboBox(this.columnNames.toArray());
                if (this.columnNames.contains(destMetaData.getColumnName(i))) {
                    MappedCols.add(destMetaData.getColumnName(i));
                    j.setSelectedItem(destMetaData.getColumnName(i));

                } else {
                    MappedCols.add("");
                }
                j.setEditable(true);
                excelCols.add(j);

            }

            model = new DefaultTableModel(null, tableColumnNames.toArray()) {
                @Override
                public boolean isCellEditable(int rowIndex, int mColIndex) {
                    return rowIndex == 0;
                }

            };
            model.addRow(MappedCols.toArray());
            tabMappings.setModel(model);
            for (int i = 0; i < model.getColumnCount(); i++) {
                tabMappings.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(excelCols.get(i)));
            }
            jcon.resize_tbl_cols(tabMappings);

        } catch (SQLException ex) {
            Logger.getLogger(excelOps.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_txtTableNameFocusLost

    public String getTxtTableName() {
        return txtTableName.getText();
    }


    private void txtTableNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTableNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTableNameActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        insertFromAnotherDb.openInsertDialog(jcon, this);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void radSQLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radSQLActionPerformed
        // TODO add your handling code here:
        if (radSQL.isSelected()) {
            btnStartOp.setText("Start SQL on Column " + this.txtColumnForOperation.getText());
        }
    }//GEN-LAST:event_radSQLActionPerformed


    public static excelOps main(final JdbcPersistent jcon) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(excelOps.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(excelOps.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(excelOps.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(excelOps.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
         final excelOps EO = new excelOps(jcon);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                EO.setVisible(true);
            }
        });
        return EO;
    }


}
