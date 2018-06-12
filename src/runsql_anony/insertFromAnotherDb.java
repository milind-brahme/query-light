/*
 * Copyright (C) 2017 Milind Brahme
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

import oracle.jdbc.OraclePreparedStatement;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author hv655
 */
public class insertFromAnotherDb extends javax.swing.JDialog {

    private int recCounter = 0;
    private int errorCounter = 0;
    private JdbcPersistent srcJcon;
    private JdbcPersistent destJcon;
    private ArrayList<defaultField> dfltList = new ArrayList<defaultField>();
    private ArrayList<destField> destFieldsList = new ArrayList<destField>();
    private StringBuffer insertStmt = new StringBuffer();
    private StringBuffer mergeStmt = new StringBuffer();
    private StringBuffer erroredInserts;
    private Thread thMain;
    private excelOps EO;

    class defaultField {

        public String dfltFieldName;
        public String dfltFieldVorC;
        public boolean constantFlag;

        public defaultField(String dfltFieldName, String dfltFieldVorC) {
            this.dfltFieldName = dfltFieldName;
            this.dfltFieldVorC = dfltFieldVorC;
            this.constantFlag = false;
        }
    }

    class destField {

        public String destFieldName;
        public String destFieldValue;
        public int dataType;
        public boolean isConstant = false;

        public destField(String destFieldName, String destFieldValue, int dataType) {
            this.destFieldName = destFieldName;
            this.destFieldValue = destFieldValue;
            this.dataType = dataType;
            if( chkSelectAs.isSelected() ||  destFieldValue.toUpperCase().trim().equals("NULL") ||destFieldValue.toUpperCase().trim().equals("SYSDATE")  ||
                 destFieldValue.toUpperCase().equals("ROWNUM") || destFieldValue.toUpperCase().equals("ROWID") ||  destFieldValue.toUpperCase().contains("'")
                    || destFieldValue.toUpperCase().contains(".NEXTVAL") )
            {
            this.isConstant = true;
            }else if (StringUtils.isNumeric(destFieldValue)) {
                this.isConstant = true;
            }else             {
                 this.isConstant = false;
            }
        }

        public destField(String destFieldName, int dataType) {
            this.destFieldName = destFieldName;
            this.dataType = dataType;
            this.isConstant = false;
        }
    }

    public insertFromAnotherDb(java.awt.Frame parent, boolean modal) {
        super(parent, false);
        initComponents();
    }

    public insertFromAnotherDb(java.awt.Frame parent, boolean modal, excelOps EO) {
        super(parent, false);
        initComponents();
        this.EO = EO;
        this.destTable.setText(EO.getTxtTableName().trim());
    }

    private void buildDefaultsList() {
        if (EO == null) {

            for (int row = 0; row < tblColumnsAndValues.getRowCount(); row++) {
                if (tblColumnsAndValues.getModel().getValueAt(row, 0) != null && tblColumnsAndValues.getModel().getValueAt(row, 1) != null) {
                    dfltList.add(new defaultField((String) tblColumnsAndValues.getModel().getValueAt(row, 0), (String) tblColumnsAndValues.getModel().getValueAt(row, 1)));
                }
            }
        } else {
            for (int col = 0; col < this.EO.getTabMappings().getColumnModel().getColumnCount(); col++) {
                if (this.EO.getTabMappings().getValueAt(0, col) != null) {
                    dfltList.add(new defaultField(this.EO.getTabMappings().getColumnName(col), (String) this.EO.getTabMappings().getValueAt(0, col)));
                }
            }
        }
    }

    private String populateColumnSimilars(String fieldName) {
        if (chkUseLikeness.isSelected()) {

            try {
                uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanOrderedNameCompoundSimilarity algorithm = new uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanOrderedNameCompoundSimilarity();
                //double s =  algorithm.getSimilarity(word1, word2);
                StringBuilder tmp = new StringBuilder();
                ArrayList<Float> likeIndex = new ArrayList<>();
                java.sql.PreparedStatement srcSTMT = srcJcon.getConn().prepareStatement(this.srcSelect.getText());
                ResultSet srcRs = srcSTMT.executeQuery();
                ResultSetMetaData srcMetaData = srcRs.getMetaData();
                int srcColumns = srcMetaData.getColumnCount();
                for (int i = 1; i <= srcColumns; i++) {
                    likeIndex.add(algorithm.getSimilarity(fieldName.toUpperCase(), srcMetaData.getColumnName(i)        ));
                    float f = algorithm.getSimilarity(fieldName.toUpperCase(), srcMetaData.getColumnName(i)  );
                    if (f >= Collections.max(likeIndex) && f> Bms_Constants.matchScale) {
                        tmp = new StringBuilder(srcMetaData.getColumnName(i) );
                    }
                }
                srcSTMT.close();
              
                return tmp.toString();
            } catch (SQLException ex) {
               lblError.setText(" Source Sql is blank or incorrect " + ex.getMessage());
            }
        }
        return null;
    }

    private String findDefaultValue(String fieldName) {

        for (defaultField d : dfltList) {
            if (fieldName.toUpperCase().equals(d.dfltFieldName.toUpperCase())) {
                return d.dfltFieldVorC;
            }
        }

        return null;
    }

    private void updateDestFieldValue(String fieldName, String fieldValue) {
        for (destField d : destFieldsList) {
            if (fieldName.toUpperCase().equals(d.destFieldName.toUpperCase()) && d.isConstant == false) {
                d.destFieldValue = fieldValue;
            }
        }
    }

    private void removeAbsentDestValue() {
        destField del;
        for ( Iterator<destField> iterator = destFieldsList.iterator(); iterator.hasNext(); ) {
           destField d = iterator.next();
            if (d.destFieldValue == null) {
               iterator.remove();
            }else if (d.destFieldValue.length()<1)
            {
               iterator.remove();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        SingleTableInsert = new javax.swing.JPanel();
        destTable = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtTnsString = new javax.swing.JTextArea();
        btnRollback = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblColumnsAndValues = new javax.swing.JTable();
        startInsert = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        srcSelect = new javax.swing.JTextArea();
        btnCommit = new javax.swing.JButton();
        btnStopIns = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        btnConvertToJDBC = new javax.swing.JButton();
        chkUseLikeness = new javax.swing.JCheckBox();
        btnGetColumnDetails = new javax.swing.JButton();
        chkDisplayOnly = new javax.swing.JCheckBox();
        startMerge = new javax.swing.JButton();
        chkSelectAs = new javax.swing.JCheckBox();
        lblError = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        srcJDBC = new javax.swing.JTextField();
        srcPass = new javax.swing.JPasswordField();
        srcUser = new javax.swing.JTextField();
        destUser = new javax.swing.JTextField();
        destPass = new javax.swing.JPasswordField();
        destJDBC = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        destTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                destTableKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                destTableKeyReleased(evt);
            }
        });

        jLabel7.setText("Select Statement");

        txtTnsString.setColumns(20);
        txtTnsString.setRows(5);
        jScrollPane2.setViewportView(txtTnsString);

        btnRollback.setText("Rollback");
        btnRollback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRollbackActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Column Mapping (only exceptions)"));

        tblColumnsAndValues.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Column Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblColumnsAndValues.setColumnSelectionAllowed(true);
        tblColumnsAndValues.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(tblColumnsAndValues);
        tblColumnsAndValues.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                .addContainerGap())
        );

        startInsert.setText("Insert");
        startInsert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startInsertActionPerformed(evt);
            }
        });

        jLabel8.setText("Destination Table Name");

        srcSelect.setColumns(20);
        srcSelect.setRows(5);
        jScrollPane1.setViewportView(srcSelect);

        btnCommit.setText("Commit");
        btnCommit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCommitActionPerformed(evt);
            }
        });

        btnStopIns.setText("Stop Insert Job");
        btnStopIns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopInsActionPerformed(evt);
            }
        });

        jLabel11.setText("Convert TNS to JDBC (Trial )");

        btnConvertToJDBC.setText("Convet to JDBC URL");
        btnConvertToJDBC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConvertToJDBCActionPerformed(evt);
            }
        });

        chkUseLikeness.setText("Use Likeness (Tables with slightly diff column names)");
        chkUseLikeness.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkUseLikenessActionPerformed(evt);
            }
        });

        btnGetColumnDetails.setText("Get Column Name");
        btnGetColumnDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGetColumnDetailsActionPerformed(evt);
            }
        });

        chkDisplayOnly.setSelected(true);
        chkDisplayOnly.setText("Display Stmts only (Do not execute)");

        startMerge.setText("Merge");
        startMerge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startMergeActionPerformed(evt);
            }
        });

        chkSelectAs.setText("Make Select As (Need to be executed in editor with mods)");
        chkSelectAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSelectAsActionPerformed(evt);
            }
        });

        lblError.setForeground(new java.awt.Color(204, 102, 0));
        lblError.setText("Message : ");

        javax.swing.GroupLayout SingleTableInsertLayout = new javax.swing.GroupLayout(SingleTableInsert);
        SingleTableInsert.setLayout(SingleTableInsertLayout);
        SingleTableInsertLayout.setHorizontalGroup(
            SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SingleTableInsertLayout.createSequentialGroup()
                .addGroup(SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SingleTableInsertLayout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addGroup(SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SingleTableInsertLayout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addGap(630, 641, Short.MAX_VALUE))
                            .addGroup(SingleTableInsertLayout.createSequentialGroup()
                                .addGroup(SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1)
                                    .addGroup(SingleTableInsertLayout.createSequentialGroup()
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 583, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnConvertToJDBC, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(SingleTableInsertLayout.createSequentialGroup()
                                        .addGroup(SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel7)
                                            .addGroup(SingleTableInsertLayout.createSequentialGroup()
                                                .addComponent(startInsert, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(startMerge, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnCommit, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnRollback, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnStopIns, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(SingleTableInsertLayout.createSequentialGroup()
                                                .addGroup(SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(destTable, javax.swing.GroupLayout.PREFERRED_SIZE, 635, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jLabel8))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnGetColumnDetails))
                                            .addComponent(chkDisplayOnly))
                                        .addGap(0, 9, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))))
                    .addGroup(SingleTableInsertLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, 806, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkSelectAs, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(chkUseLikeness, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        SingleTableInsertLayout.setVerticalGroup(
            SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SingleTableInsertLayout.createSequentialGroup()
                .addGroup(SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SingleTableInsertLayout.createSequentialGroup()
                        .addContainerGap(19, Short.MAX_VALUE)
                        .addComponent(chkUseLikeness))
                    .addGroup(SingleTableInsertLayout.createSequentialGroup()
                        .addComponent(lblError)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SingleTableInsertLayout.createSequentialGroup()
                        .addComponent(chkSelectAs)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SingleTableInsertLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(destTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnGetColumnDetails))
                        .addGap(1, 1, 1)))
                .addGroup(SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SingleTableInsertLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(startInsert)
                            .addComponent(btnCommit)
                            .addComponent(btnRollback)
                            .addComponent(btnStopIns)
                            .addComponent(startMerge))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkDisplayOnly)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(SingleTableInsertLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(SingleTableInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnConvertToJDBC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane2))
                        .addGap(49, 49, 49))))
        );

        jTabbedPane1.addTab("Single Table Insert", SingleTableInsert);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1137, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 620, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Multi Table Insert", jPanel2);

        jLabel1.setText("Source Username");

        jLabel2.setText("Source Password");

        jLabel3.setText("Source JDBCString");

        jLabel6.setText("Dest JDBCString");

        jLabel5.setText("Dest Password");

        jLabel4.setText("Dest Username");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(destUser, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(destPass, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(destJDBC))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(srcUser, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(jLabel5)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(srcPass, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel6))
                                .addGap(501, 501, 501))
                            .addComponent(srcJDBC))))
                .addGap(269, 269, 269))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 11, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srcUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(srcJDBC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(srcPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(destUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(destJDBC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(destPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(73, 73, 73))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 648, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startMergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startMergeActionPerformed
        if (thMain == null) {
            thMain = new Thread() {
                @Override
                public void run() {
                    if (EO != null && EO.peformBatchInsert()) {
                        runMergeProcess();

                    }else if (EO != null)
                    {
                        runMergeProcessForExcel();
                    }
                    else {
                        runMergeProcess();
                    }
                }
            };
            thMain.start();
        } else if (thMain.isAlive()) {
            lblError.setText("Process already running");
        } else {
            thMain = null;
            thMain = new Thread() {
                @Override
                public void run() {
                    if (EO != null && EO.peformBatchInsert()) {
                        runMergeProcess();

                    }else if (EO != null)
                    {
                        runMergeProcessForExcel();
                    }
                    else {
                        runMergeProcess();
                    }
                }
            };
            thMain.start();
        }
    }//GEN-LAST:event_startMergeActionPerformed

    private void btnGetColumnDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetColumnDetailsActionPerformed
        try {
            if (srcJcon == null || destJcon == null) {
                try {
                    this.srcJcon = new JdbcPersistent(srcUser.getText(), new String(srcPass.getPassword()), srcJDBC.getText());
                    this.destJcon = new JdbcPersistent(destUser.getText(), new String(destPass.getPassword()), destJDBC.getText());
                } catch (Exception ex) {
                    lblError.setText(ex.getMessage());
                    return;
                }
            }
            final DefaultTableModel model = (DefaultTableModel) tblColumnsAndValues.getModel();
            model.setRowCount(0);
            java.sql.PreparedStatement destSTMT = destJcon.getConn().prepareStatement("select * from " + this.destTable.getText());
            destJcon.setRs(destSTMT.executeQuery());

            ResultSetMetaData destMetaData = destJcon.getRs().getMetaData();

            int destColumns = destMetaData.getColumnCount();
            for (int i = 1; i <= destColumns; i++) {
                model.addRow(new String[]{destMetaData.getColumnName(i), null});
            }
        } catch (Exception ex) {
            lblError.setText(ex.getMessage());
        }
    }//GEN-LAST:event_btnGetColumnDetailsActionPerformed

    private void chkUseLikenessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkUseLikenessActionPerformed
        if (chkUseLikeness.isSelected()) {
            final DefaultTableModel model = (DefaultTableModel) tblColumnsAndValues.getModel();

            for(int row=0; row < tblColumnsAndValues.getRowCount(); row++)
            {
                model.setValueAt(populateColumnSimilars( (String) model.getValueAt(row, 0) ) , row, 1) ;
            }
        }
    }//GEN-LAST:event_chkUseLikenessActionPerformed

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

    private void btnStopInsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopInsActionPerformed
        this.thMain.interrupt();
    }//GEN-LAST:event_btnStopInsActionPerformed

    private void btnCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommitActionPerformed
        try {
            // TODO add your handling code here:
            this.destJcon.getConn().commit();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            lblError.setText("Commit Complete @" + dateFormat.format(date));
        } catch (SQLException ex) {
            Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, null, ex);
            lblError.setText(ex.getMessage());
        }
    }//GEN-LAST:event_btnCommitActionPerformed

    private void startInsertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startInsertActionPerformed
        if (thMain == null) {
            thMain = new Thread() {
                @Override
                public void run() {
                    if (EO != null && EO.peformBatchInsert()) {
                        runInsProcess();

                    }else if(EO != null)
                    {
                        runInsProcessForExcel();
                    }
                    else {
                        runInsProcess();
                    }
                }
            };
            thMain.start();
        } else if (thMain.isAlive()) {
            lblError.setText("Process already running");
        } else {
            thMain = null;
            thMain = new Thread() {
                @Override
                public void run() {
                    if (EO != null && EO.peformBatchInsert()) {
                        runInsProcess();

                    }else if(EO != null)
                    {
                        runInsProcessForExcel();
                    }
                    else {
                        runInsProcess();
                    }
                }
            };
            thMain.start();
        }
    }//GEN-LAST:event_startInsertActionPerformed

    private void btnRollbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRollbackActionPerformed
        // TODO add your handling code here:
        try {
            //TODO add your handling code here:
            this.destJcon.getConn().rollback();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            lblError.setText("Rollback Complete @" + dateFormat.format(date));
        } catch (SQLException ex) {
            Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, null, ex);
            lblError.setText(ex.getMessage());
        }
    }//GEN-LAST:event_btnRollbackActionPerformed

    private void destTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_destTableKeyReleased
        // TODO add your handling code here:
        String destTable=this.destTable.getText();

        this.srcSelect.setText("select * from " + destTable ) ;
    }//GEN-LAST:event_destTableKeyReleased

    private void destTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_destTableKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_destTableKeyPressed

    private void chkSelectAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSelectAsActionPerformed
        // TODO add your handling code here:
        if(chkSelectAs.isSelected())
        {
            this.srcSelect.append(this.srcSelect.getText().toUpperCase().contains("WHERE")?"and rownum <= 1" : " where rownum <= 1" );
        }
    }//GEN-LAST:event_chkSelectAsActionPerformed

    public void displayRuntimeRecordCount() {
        while (true) {
            try {
                lblError.setText(" Records inserted  : " + Integer.toString(this.recCounter) + "   Records Errored :" + Integer.toString(this.errorCounter) + "   Total : " + Integer.toString(this.recCounter + this.errorCounter));
                Thread.sleep(100);
                if (!thMain.isAlive()) {
                    break;
                }
            } catch (InterruptedException ex) {
               
            }
        }
    }

    private boolean IsMappedtoConstantValue(String colName) {
        for (int col = 0; col < EO.getTabMappings().getColumnCount(); col++) {
            if (EO.getTabMappings().getValueAt(0, col).equals(colName)) {
                return false;
            }
        }
        return true;
    }

    
    private void runMergeProcessForExcel() {
        lblError.setText("    ");
        erroredInserts = new StringBuffer();
        this.recCounter = 0;
        this.errorCounter = 0;
        try {
            // TODO add your handling code here:
            dfltList = new ArrayList<defaultField>();
            destFieldsList = new ArrayList<destField>();
            this.srcJcon = null;
            this.destJcon = new JdbcPersistent(destUser.getText(), new String(destPass.getPassword()), destJDBC.getText());
            buildDefaultsList();
            java.sql.PreparedStatement destSTMT = destJcon.getConn().prepareStatement("select * from " + this.destTable.getText());
            destJcon.setRs(destSTMT.executeQuery());

            ResultSetMetaData destMetaData = destJcon.getRs().getMetaData();

            int destColumns = destMetaData.getColumnCount();
            for (int i = 1; i <= destColumns; i++) {

                if (IsMappedtoConstantValue(destMetaData.getColumnName(i))) {
                    destFieldsList.add(new destField(destMetaData.getColumnName(i), (String) EO.getTabMappings().getValueAt(0, i - 1), destMetaData.getColumnType(i)));
                } else {
                    destFieldsList.add(new destField(destMetaData.getColumnName(i), destMetaData.getColumnType(i)));
                    updateDestFieldValue(destMetaData.getColumnName(i), destMetaData.getColumnName(i));
                }
            }

            for (int i = 0; i < EO.getTblExcel().getColumnCount(); i++) {
                updateDestFieldValue(EO.getTblExcel().getColumnName(i), EO.getTblExcel().getColumnName(i));
            }

            recCounter = 0;
            errorCounter = 0;
             String destSchema =null;
           String destTableName = null;
            if ((this.destTable.getText().contains(".")))
            {
                 String[] tmp = this.destTable.getText().toUpperCase().split(".");
                 destSchema=tmp[0];
                 destTableName = tmp[1];
            }
            else
            {
                destSchema=destJcon.returnSingleField("select table_owner from all_synonyms where synonym_name='" +  this.destTable.getText().toUpperCase() + "'" )  ;
                destTableName=this.destTable.getText().toUpperCase().trim();
            }
            
           
            ArrayList<String> pkColumns = ( ArrayList<String> ) destJcon.returnSingleColumnArray( "select con_col.column_name column_name    \n" +
"from all_constraints con , all_cons_columns con_col \n" +
"where  con.owner  = '" + destSchema +  "' \n" +
"and con.table_name = '" + destTableName + "' \n" +
"and con.CONSTRAINT_TYPE = 'P'\n" +
"and con.owner=con_col.owner\n" +
"and con.CONSTRAINT_NAME = con_col.CONSTRAINT_NAME\n" +
" order by  con_col.position")   ;
            

            Thread TH = new Thread() {
                @Override
                public void run() {
                    displayRuntimeRecordCount();
                }
            };
            TH.start();
            
            JTable tabsrc = EO.getTblExcel();
            this.removeAbsentDestValue();
            for (int row = 0; row < tabsrc.getRowCount(); row++) {
                if (this.thMain.isInterrupted()) {
                    break;
                }
                
                mergeStmt = new StringBuffer("merge into " + destTable.getText() + " destTab \n" + "using (select "  );             
                for (destField d : destFieldsList) {
                    if (d.destFieldValue != null) {
                        if (d.isConstant) {
                            mergeStmt.append(d.destFieldValue + " " + d.destFieldName  + ",");
                        } else {
                            if (d.dataType == java.sql.Types.DATE || d.dataType == java.sql.Types.TIME || d.dataType == java.sql.Types.TIMESTAMP) {
                                try {
                                         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date date = (Date) tabsrc.getValueAt(row, tabsrc.getColumnModel().getColumnIndex(d.destFieldName));
                                    mergeStmt.append("to_date('" + dateFormat.format(date) + "' , 'YYYY-MM-DD HH24:MI:SS')"  + " " +  d.destFieldName + ",");
                                } catch (Exception ex) {
                                    mergeStmt.append("null" + " " +  d.destFieldName + ",");
                                }
                            } else {
                              if (tabsrc.getValueAt(row, tabsrc.getColumnModel().getColumnIndex(d.destFieldName)) == null) {
                                
                                } else if (tabsrc.getValueAt(row, tabsrc.getColumnModel().getColumnIndex(d.destFieldName)).equals("null")) {
                                    mergeStmt.append("null"+ " " +  d.destFieldName + ",");
                                } else {
                                    mergeStmt.append("'" + tabsrc.getValueAt(row, tabsrc.getColumnModel().getColumnIndex(d.destFieldName)).toString().replace("'","''") + "'" + " " +  d.destFieldName+ ",");
                            }
                            }
                        }
                    }
                }
                
                mergeStmt.deleteCharAt(mergeStmt.lastIndexOf(","));
                mergeStmt.append( " from dual ) sourceTab \n ") ;
                mergeStmt.append(" on ( ");
                int pk=0;
                for(String pkcol : pkColumns )
                {   

                    for(destField d : destFieldsList) { 
                        if(d.destFieldName.toUpperCase().equals(pkcol.toUpperCase())) 
                    {  
                            String sep = null;
                        if (pk==0) sep =" ";
                        else sep = ","; 
                        mergeStmt.append ( sep + "sourceTab." + pkcol + "=" + "destTab." + d.destFieldName  ); 
                        pk++;
                    }  
                }
                }
                mergeStmt.append(")\n");
                mergeStmt.append("when matched then \n");
                mergeStmt.append("update set "  );
                {    int t=0;
                for(destField d : destFieldsList)
                {   
                   
                    ArrayList<String> tmp = new ArrayList();
                    tmp.add(d.destFieldName.toUpperCase());
                     if (d.destFieldValue != null) {
                    if(Collections.disjoint(tmp, pkColumns))
                    {   
                        
                    String sep = null;
                        if (t==0) sep =" "; 
                        else sep = ","; 
                      if(tabsrc.getValueAt(row, tabsrc.getColumnModel().getColumnIndex(d.destFieldName)) != null)
                      {    mergeStmt.append( sep + "destTab." + d.destFieldName +"=" + "sourceTab." +  d.destFieldName );
                        t++; 
                      }
                    }
                }
                } }
                
                mergeStmt.append("\n when not matched then \n");
                mergeStmt.append("insert (");
                {int t=0;
                for(destField d : destFieldsList)
                {   
                     
                     if (d.destFieldValue != null) {
                                       String sep = null;
                        if (t==0) sep =" "; 
                        else sep = ","; 
                        if(tabsrc.getValueAt(row, tabsrc.getColumnModel().getColumnIndex(d.destFieldName)) != null)
                        { mergeStmt.append(sep  +  d.destFieldName  );
                        t++;
                        }
                    }
                
                } }
                mergeStmt.append(")\n values (");
                {int t=0; for(destField d : destFieldsList)
                {   
                     
                         String sep = null;
                        if (t==0) sep =" "; 
                        else sep = ","; 
                     if (d.destFieldValue != null) {
                     mergeStmt.append(sep  + "sourceTab." + d.destFieldName  );
                        t++;
                    }
                
                } }
                mergeStmt.append(")\n");
                
                //destJcon.execute_stmt("dbms_session.set_nls('nls_date_format','''YYYY-MM-DD HH24:MI:SS''')");
if (!chkDisplayOnly.isSelected()) {
                    OraclePreparedStatement mSTMT = (OraclePreparedStatement) destJcon.getConn().prepareStatement(mergeStmt.toString());
                    try {
                        mSTMT.executeUpdate();
                        this.recCounter++;
                    } catch (Exception ex) {
                        Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, mSTMT.toString(), ex);
                        erroredInserts.append(mergeStmt.toString() + "\n " + ex.getMessage());
                        this.errorCounter++;
                    }
                    mSTMT.close();
                } else {
                  erroredInserts.append(mergeStmt.toString() + "\n\n");
                }
            }
            TH.interrupt();
            lblError.setText(" Records Merged  : " + Integer.toString(this.recCounter) + "   Records Errored :" + Integer.toString(this.errorCounter) + "   Total : " + Integer.toString(this.recCounter + this.errorCounter));
            longMsg1.main(erroredInserts);

    
            
        } catch (Exception ex) {
            Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, null, ex);
            lblError.setText(ex.getMessage());
        }
    }
    private void runInsProcessForExcel() {
        lblError.setText("    ");
        erroredInserts = new StringBuffer();
        this.recCounter = 0;
        this.errorCounter = 0;
        try {
            // TODO add your handling code here:
            dfltList = new ArrayList<defaultField>();
            destFieldsList = new ArrayList<destField>();
            this.srcJcon = null;
            this.destJcon = new JdbcPersistent(destUser.getText(), new String(destPass.getPassword()), destJDBC.getText());
            buildDefaultsList();
            java.sql.PreparedStatement destSTMT = destJcon.getConn().prepareStatement("select * from " + this.destTable.getText());
            destJcon.setRs(destSTMT.executeQuery());

            ResultSetMetaData destMetaData = destJcon.getRs().getMetaData();

            int destColumns = destMetaData.getColumnCount();
            for (int i = 1; i <= destColumns; i++) {

                if (IsMappedtoConstantValue(destMetaData.getColumnName(i))) {
                    destFieldsList.add(new destField(destMetaData.getColumnName(i), (String) EO.getTabMappings().getValueAt(0, i - 1), destMetaData.getColumnType(i)));
                } else {
                    destFieldsList.add(new destField(destMetaData.getColumnName(i), destMetaData.getColumnType(i)));
                    updateDestFieldValue(destMetaData.getColumnName(i), destMetaData.getColumnName(i));
                }
            }

            /* java.sql.PreparedStatement srcSTMT = srcJcon.conn.prepareStatement(this.srcSelect.getText());
            ResultSet srcRs = srcSTMT.executeQuery();
            ResultSetMetaData srcMetaData = srcRs.getMetaData();
            int srcColumns = srcMetaData.getColumnCount();
            for (int i = 1; i <= srcColumns; i++) {
                updateDestFieldValue(srcMetaData.getColumnName(i), srcMetaData.getColumnName(i));
            } */
            for (int i = 0; i < EO.getTblExcel().getColumnCount(); i++) {
                updateDestFieldValue(EO.getTblExcel().getColumnName(i), EO.getTblExcel().getColumnName(i));
            }

            recCounter = 0;
            errorCounter = 0;
            Thread TH = new Thread() {
                @Override
                public void run() {
                    displayRuntimeRecordCount();
                }
            };
            TH.start();
             this.removeAbsentDestValue();
            JTable tabsrc = EO.getTblExcel();
            for (int row = 0; row < tabsrc.getRowCount(); row++) {

                try {
                    if (this.thMain.isInterrupted()) {
                        break;
                    }

                    insertStmt = new StringBuffer("insert into " + destTable.getText() + "\n" + "(");
                    for (destField d : destFieldsList) {
                        if (d.destFieldValue != null) {
                            insertStmt.append(d.destFieldName + ",");
                        }
                    }
                    insertStmt.deleteCharAt(insertStmt.lastIndexOf(","));
                    insertStmt.append(")\n values \n" + "(");
                    for (destField d : destFieldsList) {
                        if (d.destFieldValue != null) {
                            if (d.isConstant) {
                                insertStmt.append(d.destFieldValue + ",");
                            } else {
                                if (d.dataType == Types.DATE || d.dataType == Types.TIME || d.dataType == Types.TIMESTAMP) {
                                    try {
                                        //int dotIndx = srcRs.getString(d.destFieldName).lastIndexOf(".");
                                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        Date date = (Date) tabsrc.getValueAt(row, tabsrc.getColumnModel().getColumnIndex(d.destFieldName));
                                        insertStmt.append("to_date('" + dateFormat.format(date) + "' , 'YYYY-MM-DD HH24:MI:SS')" + ",");
                                    } catch (Exception ex) {
                                        insertStmt = new StringBuffer(insertStmt.toString().replaceFirst(d.destFieldName + ",?", " "));
                                    }
                                } else {
                                    if (tabsrc.getValueAt(row, tabsrc.getColumnModel().getColumnIndex(d.destFieldName)) == null) {
                                        // insertStmt.append(null + ",");
                                        insertStmt = new StringBuffer(insertStmt.toString().replaceFirst(d.destFieldName + ",?", " "));
                                    } else if (tabsrc.getValueAt(row, tabsrc.getColumnModel().getColumnIndex(d.destFieldName)).equals("null")) {
                                        insertStmt.append(null + ",");
                                    } else {
                                        insertStmt.append("'" + tabsrc.getValueAt(row, tabsrc.getColumnModel().getColumnIndex(d.destFieldName)).toString().replace("'","''") + "'" + ",");
                                    }
                                }
                            }
                        }
                    }
                     if(insertStmt.lastIndexOf(",")>0)
                     { insertStmt.deleteCharAt(insertStmt.lastIndexOf(",")); }
                    insertStmt.append(")\n");
                    //destJcon.execute_stmt("dbms_session.set_nls('nls_date_format','''YYYY-MM-DD HH24:MI:SS''')");
                    if (!chkDisplayOnly.isSelected()) {
                                        OraclePreparedStatement insSTMT = (OraclePreparedStatement) destJcon.getConn().prepareStatement(insertStmt.toString());
                                        try {
                                            insSTMT.executeUpdate();
                                            this.recCounter++;
                                        } catch (Exception ex) {
                                            Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, insSTMT.toString(), ex);
                                            erroredInserts.append(insertStmt.toString() + "\n " + ex.getMessage());
                                            this.errorCounter++;
                                        }
                                        insSTMT.close();
                                    }else { erroredInserts.append(insertStmt.toString() + "\n\n");  }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            TH.interrupt();
            lblError.setText(" Records processed  : " + Integer.toString(this.recCounter) + "   Records Errored :" + Integer.toString(this.errorCounter) + "   Total : " + Integer.toString(this.recCounter + this.errorCounter));
            longMsg1.main(erroredInserts);

        } catch (Exception ex) {
            Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, null, ex);
            lblError.setText(ex.getMessage());
        }
    }
    private void runMergeProcess()
    {
        int tblCount = 1;
        JTable tblExcel=null;
        if (EO!=null && EO.peformBatchInsert())
        {
            tblCount = EO.getTblExcel().getRowCount();
            tblExcel = EO.getTblExcel();
            EO.getColumnNames().add("Result");
        }
        erroredInserts = new StringBuffer();
        for(int tbl=0 ; tbl < tblCount ; tbl++ ) {
            lblError.setText("    ");
            this.recCounter = 0;
            this.errorCounter = 0;
            try {
                if (EO!=null && EO.peformBatchInsert())
                {
                    this.destTable.setText( tblExcel.getValueAt(tbl, EO.batchInsTableColIndex() - 1).toString());
                    this.srcSelect.setText( tblExcel.getValueAt(tbl, EO.batchInsStmtColIndex() - 1).toString());

                }

                dfltList = new ArrayList<defaultField>();
                destFieldsList = new ArrayList<destField>();
                try {
                    if (srcJcon == null || destJcon == null) {
                        this.srcJcon = new JdbcPersistent(srcUser.getText(), new String(srcPass.getPassword()), srcJDBC.getText());
                        this.destJcon = new JdbcPersistent(destUser.getText(), new String(destPass.getPassword()), destJDBC.getText());
                    }
                } catch (Exception exception) {
                    Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, null, exception);
                    lblError.setText("Could not connect : " + exception.getMessage());
                }
                buildDefaultsList();
                java.sql.PreparedStatement destSTMT = destJcon.getConn().prepareStatement("select * from " + this.destTable.getText());
                destJcon.setRs(destSTMT.executeQuery());


                ResultSetMetaData destMetaData = destJcon.getRs().getMetaData();
                String destSchema = null;
                String destTableName = null;
                if ((this.destTable.getText().contains("."))) {
                    String[] tmp = this.destTable.getText().toUpperCase().split(".");
                    destSchema = tmp[0];
                    destTableName = tmp[1];
                } else {
                    destSchema = destJcon.returnSingleField("select table_owner from all_synonyms where synonym_name='" + this.destTable.getText().toUpperCase() + "'");
                    destTableName = this.destTable.getText().toUpperCase().trim();
                }


                ArrayList<String> pkColumns = (ArrayList<String>) destJcon.returnSingleColumnArray("select con_col.column_name column_name    \n" +
                        "from all_constraints con , all_cons_columns con_col \n" +
                        "where  con.owner  = '" + destSchema + "' \n" +
                        "and con.table_name = '" + destTableName + "' \n" +
                        "and con.CONSTRAINT_TYPE = 'P'\n" +
                        "and con.owner=con_col.owner\n" +
                        "and con.CONSTRAINT_NAME = con_col.CONSTRAINT_NAME\n" +
                        " order by  con_col.position");


                int destColumns = destMetaData.getColumnCount();
                for (int i = 1; i <= destColumns; i++) {
                    String dfltValue = findDefaultValue(destMetaData.getColumnName(i));
                    if (dfltValue != null) {
                        destFieldsList.add(new destField(destMetaData.getColumnName(i), dfltValue, destMetaData.getColumnType(i)));
                    } else {
                        destFieldsList.add(new destField(destMetaData.getColumnName(i), destMetaData.getColumnType(i)));
                    }
                }

                java.sql.PreparedStatement srcSTMT = srcJcon.getConn().prepareStatement(this.srcSelect.getText());
                ResultSet srcRs = srcSTMT.executeQuery();
                ResultSetMetaData srcMetaData = srcRs.getMetaData();
                int srcColumns = srcMetaData.getColumnCount();
                for (int i = 1; i <= srcColumns; i++) {
                    updateDestFieldValue(srcMetaData.getColumnName(i), srcMetaData.getColumnName(i));
                }
                recCounter = 0;
                errorCounter = 0;
                Thread TH = new Thread() {
                    @Override
                    public void run() {
                        displayRuntimeRecordCount();
                    }
                };
                TH.start();
                this.removeAbsentDestValue();
                while (srcRs.next()) {
                    if (this.thMain.isInterrupted()) {
                        break;
                    }


                    mergeStmt = new StringBuffer("merge into " + destTable.getText() + " destTab \n" + "using (select ");
                    for (destField d : destFieldsList) {
                        if (d.destFieldValue != null) {
                            if (d.isConstant) {
                                mergeStmt.append(d.destFieldValue + " " + d.destFieldName + ",");
                            } else {
                                if (d.dataType == java.sql.Types.DATE || d.dataType == java.sql.Types.TIME || d.dataType == java.sql.Types.TIMESTAMP) {
                                    try {
                                        int dotIndx = srcRs.getString(d.destFieldValue).lastIndexOf(".");
                                        mergeStmt.append("to_date('" + srcRs.getString(d.destFieldValue).substring(0, dotIndx) + "' , 'YYYY-MM-DD HH24:MI:SS')" + " " + d.destFieldName + ",");
                                    } catch (Exception ex) {
                                        Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, null, ex);
                                        mergeStmt.append("null" + " " + d.destFieldName + ",");
                                    }
                                } else if(d.dataType == java.sql.Types.LONGNVARCHAR || d.dataType == Types.LONGVARBINARY || d.dataType == Types.LONGVARCHAR )
                                {
                                    String longVal = srcRs.getString(d.destFieldValue);
                                    if(longVal!=null)
                                    {
                                        mergeStmt.append("'" + longVal.replace("'", "''") + "'" + " " + d.destFieldName + ",");
                                    }
                                }
                                else {
                                    if (srcRs.getString(d.destFieldValue) == null) {
                                        mergeStmt.append("null" + " " + d.destFieldName + ",");
                                    } else if (srcRs.getString(d.destFieldValue).equals("null")) {
                                        mergeStmt.append("null" + " " + d.destFieldName + ",");
                                    } else {
                                        mergeStmt.append("'" + srcRs.getString(d.destFieldValue).replace("'", "''") + "'" + " " + d.destFieldName + ",");
                                    }
                                }
                            }
                        }
                    }

                    mergeStmt.deleteCharAt(mergeStmt.lastIndexOf(","));
                    mergeStmt.append(" from dual ) sourceTab \n ");
                    mergeStmt.append(" on ( ");
                    int pk = 0;
                    for (String pkcol : pkColumns) {
                        for (destField d : destFieldsList) {
                            if (d.destFieldName.toUpperCase().equals(pkcol.toUpperCase())) {
                                String sep = null;
                                if (pk == 0)
                                {
                                    sep = " ";
                                }
                                else
                                {
                                    sep = " and ";
                                }
                                mergeStmt.append(sep + "sourceTab." + pkcol + "=" + "destTab." + d.destFieldName);
                                pk++;
                            }
                        }
                    }
                    mergeStmt.append(")\n");
                    mergeStmt.append("when matched then \n");
                    mergeStmt.append("update set ");
                    {
                        int t = 0;
                        for (destField d : destFieldsList) {

                            ArrayList<String> tmp = new ArrayList();
                            tmp.add(d.destFieldName.toUpperCase());
                            if (d.destFieldValue != null) {
                                if (Collections.disjoint(tmp, pkColumns)) {

                                    String sep = null;
                                    if (t == 0) sep = " ";
                                    else sep = ",";
                                    mergeStmt.append(sep + "destTab." + d.destFieldName + "=" + "sourceTab." + d.destFieldName);
                                    t++;
                                }
                            }
                        }
                    }

                    mergeStmt.append("\n when not matched then \n");
                    mergeStmt.append("insert (");
                    {
                        int t = 0;
                        for (destField d : destFieldsList) {

                            if (d.destFieldValue != null) {
                                String sep = null;
                                if (t == 0) sep = " ";
                                else sep = ",";
                                mergeStmt.append(sep + d.destFieldName);
                                t++;
                            }

                        }
                    }
                    mergeStmt.append(")\n values (");
                    {
                        int t = 0;
                        for (destField d : destFieldsList) {

                            String sep = null;
                            if (t == 0) sep = " ";
                            else sep = ",";
                            if (d.destFieldValue != null) {
                                mergeStmt.append(sep + "sourceTab." + d.destFieldName);
                                t++;
                            }

                        }
                    }
                    mergeStmt.append(")\n");

                    //destJcon.execute_stmt("dbms_session.set_nls('nls_date_format','''YYYY-MM-DD HH24:MI:SS''')");
                    if (!chkDisplayOnly.isSelected()) {
                        OraclePreparedStatement mSTMT = (OraclePreparedStatement) destJcon.getConn().prepareStatement(mergeStmt.toString());
                        try {
                            mSTMT.executeUpdate();
                            this.recCounter++;
                        } catch (Exception ex) {
                            Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, mSTMT.toString(), ex);
                            erroredInserts.append(mergeStmt.toString() + "\n " + ex.getMessage());
                            this.errorCounter++;
                        }
                        mSTMT.close();
                    } else {
                        erroredInserts.append(mergeStmt.toString() + "\n\n");
                    }
                }
                TH.interrupt();
                lblError.setText(" Records Merged  : " + Integer.toString(this.recCounter) + "   Records Errored :" + Integer.toString(this.errorCounter) + "   Total : " + Integer.toString(this.recCounter + this.errorCounter));

                if (EO!=null && EO.peformBatchInsert())
                {
                    Vector<Object> tmp = (Vector<Object>) EO.getData().get(tbl);
                    tmp.add(this.recCounter);
       }

            } catch (Exception ex) {
                Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, null, ex);
                lblError.setText(ex.getMessage());
            }
        }
        longMsg1.main(erroredInserts);
        if (EO!=null && EO.peformBatchInsert())
        {
            EO.refreshTableModel();
        }

    }
    private void runInsProcess() {
        int tblCount = 1;
        JTable tblExcel=null;
        if (EO!=null && EO.peformBatchInsert())
        {
            tblCount = EO.getTblExcel().getRowCount();
            tblExcel = EO.getTblExcel();
            EO.getColumnNames().add("Result");
        }
        erroredInserts = new StringBuffer();
        for(int t=0 ; t < tblCount ; t++ ) {
        lblError.setText("    ");

        this.recCounter = 0;
        this.errorCounter = 0;

           try {
               if (EO!=null && EO.peformBatchInsert())
               {
                   this.destTable.setText( tblExcel.getValueAt(t, EO.batchInsTableColIndex() - 1).toString());
                   this.srcSelect.setText( tblExcel.getValueAt(t, EO.batchInsStmtColIndex() - 1).toString());

               }
               // TODO add your handling code here:
               dfltList = new ArrayList<defaultField>();
               destFieldsList = new ArrayList<destField>();
               if (srcJcon == null || destJcon == null) {
                   this.srcJcon = new JdbcPersistent(srcUser.getText(), new String(srcPass.getPassword()), srcJDBC.getText());
                   this.destJcon = new JdbcPersistent(destUser.getText(), new String(destPass.getPassword()), destJDBC.getText());
               }
               buildDefaultsList();
               java.sql.PreparedStatement destSTMT = destJcon.getConn().prepareStatement("select * from " + this.destTable.getText());
               destJcon.setRs(destSTMT.executeQuery());

               ResultSetMetaData destMetaData = destJcon.getRs().getMetaData();

               int destColumns = destMetaData.getColumnCount();
               for (int i = 1; i <= destColumns; i++) {
                   String dfltValue = findDefaultValue(destMetaData.getColumnName(i));
                   if (dfltValue != null) {
                       destFieldsList.add(new destField(destMetaData.getColumnName(i), dfltValue, destMetaData.getColumnType(i)));
                   } else {
                       destFieldsList.add(new destField(destMetaData.getColumnName(i), destMetaData.getColumnType(i)));
                   }
               }

               java.sql.PreparedStatement srcSTMT = srcJcon.getConn().prepareStatement(this.srcSelect.getText());
               ResultSet srcRs = srcSTMT.executeQuery();
               ResultSetMetaData srcMetaData = srcRs.getMetaData();
               int srcColumns = srcMetaData.getColumnCount();
               for (int i = 1; i <= srcColumns; i++) {
                   updateDestFieldValue(srcMetaData.getColumnName(i), srcMetaData.getColumnName(i));
               }
               recCounter = 0;
               errorCounter = 0;
               Thread TH = new Thread() {
                   @Override
                   public void run() {
                       displayRuntimeRecordCount();
                   }
               };
               TH.start();
               while (srcRs.next()) {
                   if (this.thMain.isInterrupted()) {
                       break;
                   }
                   this.removeAbsentDestValue();
                   insertStmt = new StringBuffer("insert into " + destTable.getText() + "\n" + "(");
                   for (destField d : destFieldsList) {
                       if (d.destFieldValue != null) {
                           insertStmt.append(d.destFieldName + ",");
                       }
                   }
                   insertStmt.deleteCharAt(insertStmt.lastIndexOf(","));
                   insertStmt.append(")\n values \n" + "(");
                   for (destField d : destFieldsList) {
                       if (d.destFieldValue != null) {
                           if (d.isConstant) {
                               insertStmt.append(d.destFieldValue + ",");
                           } else {
                               if (d.dataType == java.sql.Types.DATE || d.dataType == java.sql.Types.TIME || d.dataType == java.sql.Types.TIMESTAMP) {
                                   try {
                                       int dotIndx = srcRs.getString(d.destFieldValue).lastIndexOf(".");
                                       insertStmt.append("to_date('" + srcRs.getString(d.destFieldValue).substring(0, dotIndx) + "' , 'YYYY-MM-DD HH24:MI:SS')" + ",");
                                   } catch (Exception ex) {
                                       //Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, null, ex);
                                       insertStmt.append(null + ",");
                                   }
                               } else {
                                   if (srcRs.getString(d.destFieldValue) == null) {
                                       insertStmt.append(null + ",");
                                   } else if (srcRs.getString(d.destFieldValue).equals("null")) {
                                       insertStmt.append(null + ",");
                                   } else {
                                       insertStmt.append("'" + srcRs.getString(d.destFieldValue).replace("'", "''") + "'" + ",");
                                   }
                               }
                           }
                       }
                   }
                   insertStmt.deleteCharAt(insertStmt.lastIndexOf(","));
                   insertStmt.append(")\n");
                   //destJcon.execute_stmt("dbms_session.set_nls('nls_date_format','''YYYY-MM-DD HH24:MI:SS''')");
                   if (!chkDisplayOnly.isSelected()) {
                       OraclePreparedStatement insSTMT = (OraclePreparedStatement) destJcon.getConn().prepareStatement(insertStmt.toString());
                       try {
                           insSTMT.executeUpdate();
                           this.recCounter++;
                       } catch (Exception ex) {
                           Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, insSTMT.toString(), ex);
                           erroredInserts.append(insertStmt.toString() + "\n " + ex.getMessage());
                           this.errorCounter++;
                       }
                       insSTMT.close();
                   } else {
                       erroredInserts.append(insertStmt.toString() + "\n\n");
                     //  this.recCounter++;
                   }
               }
               TH.interrupt();
               lblError.setText(" Records inserted  : " + Integer.toString(this.recCounter) + "   Records Errored :" + Integer.toString(this.errorCounter) + "   Total : " + Integer.toString(this.recCounter + this.errorCounter));

               if (EO!=null && EO.peformBatchInsert())
               {
                   Vector<Object> tmp = (Vector<Object>) EO.getData().get(t);
                   tmp.add(this.recCounter);

               }

           } catch (Exception ex) {
               Logger.getLogger(insertFromAnotherDb.class.getName()).log(Level.SEVERE, null, ex);
               lblError.setText(ex.getMessage());
           }
       }
        if (EO!=null && EO.peformBatchInsert())
        {
         EO.refreshTableModel();
        }
        longMsg1.main(erroredInserts);
    }

    /**
     */

    public static void main(String[] args) {
        JdbcPersistent jp= null;
        try {
            jp = new JdbcPersistent("HV655HD", "nnn", "jdbc:oracle:thin:@ftdcsls435-scan.ftiz.cummins.com:1525/bmsnadu_ADHOC");
        } catch (Exception e) {
            e.printStackTrace();
        }
        openInsertDialog(jp);
    }

    public static void openInsertDialog(final JdbcPersistent jsource) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(insertFromAnotherDb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(insertFromAnotherDb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(insertFromAnotherDb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(insertFromAnotherDb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                final insertFromAnotherDb dialog = new insertFromAnotherDb(new javax.swing.JFrame(), true);
                dialog.srcUser.setText(jsource.getUid());
                dialog.srcPass.setText(jsource.getPwd());
                dialog.srcJDBC.setText(jsource.getJdbcstr());
                //dialog.srcJDBC.setText(jsource.getJdbcstr());
                dialog.destUser.setText(jsource.getUid());
                dialog.destPass.setText(jsource.getPwd());
                dialog.destJDBC.setText( jsource.getJdbcstr());
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        dialog.dispose();
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    public static void openInsertDialog(final JdbcPersistent jsource, final excelOps EO) {
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
            java.util.logging.Logger.getLogger(insertFromAnotherDb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(insertFromAnotherDb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(insertFromAnotherDb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(insertFromAnotherDb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                final insertFromAnotherDb dialog = new insertFromAnotherDb(new javax.swing.JFrame(), true, EO);
                dialog.srcUser.setText(jsource.getUid());
                dialog.srcPass.setText(jsource.getPwd());
                dialog.srcJDBC.setText(jsource.getJdbcstr());
                dialog.destUser.setText(jsource.getUid());
                dialog.destPass.setText(jsource.getPwd());
                dialog.destJDBC.setText(jsource.getJdbcstr());
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        dialog.dispose();
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel SingleTableInsert;
    private javax.swing.JButton btnCommit;
    private javax.swing.JButton btnConvertToJDBC;
    private javax.swing.JButton btnGetColumnDetails;
    private javax.swing.JButton btnRollback;
    private javax.swing.JButton btnStopIns;
    private javax.swing.JCheckBox chkDisplayOnly;
    private javax.swing.JCheckBox chkSelectAs;
    private javax.swing.JCheckBox chkUseLikeness;
    private javax.swing.JTextField destJDBC;
    private javax.swing.JPasswordField destPass;
    private javax.swing.JTextField destTable;
    private javax.swing.JTextField destUser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblError;
    private javax.swing.JTextField srcJDBC;
    private javax.swing.JPasswordField srcPass;
    private javax.swing.JTextArea srcSelect;
    private javax.swing.JTextField srcUser;
    private javax.swing.JButton startInsert;
    private javax.swing.JButton startMerge;
    private javax.swing.JTable tblColumnsAndValues;
    private javax.swing.JTextArea txtTnsString;
    // End of variables declaration//GEN-END:variables
}
