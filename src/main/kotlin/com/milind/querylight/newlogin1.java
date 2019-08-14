package com.milind.querylight;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */




import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;


import static com.milind.querylight.LocalDbForAppKt.queryTNSTable;

/**
 *
 * @author hv655
 */

public class newlogin1 extends javax.swing.JDialog {

    /**
     * Creates new form newlogin1
     */

    StoredDbDetails sdd = new StoredDbDetails();
    private Bms_Constants bc;
    private Vector<String> dbnames = new Vector<String>();
    private String dblist;
    private boolean internal_call = false;
    JdbcPersistent jdbcRetObj;

    public newlogin1(java.awt.Frame parent) {
        super(parent, true);
        this.internal_call = true;
        initComponents();
        this.setTitle("Query Light");
        this.setLocationRelativeTo(null);
        this.setIconImage(new ImageIcon(getClass().getResource("/db1-icon.png")).getImage());

        Thread TH = new Thread() {
            @Override
            public void run() {
                bc = new Bms_Constants();

            }
        };
        TH.start();
        populate_database_list();
        populate_stored_db_details();
        lbl_status.setBackground(new java.awt.Color(0, 0, 0, 0));

        tbl_dblist.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent lse) {
                tbl_dblistProcessChange();
            }
        });
        tbl_dblist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_dblist.setComponentPopupMenu(tbl_popmenu);
    }

    public static JdbcPersistent showConnDialog(java.awt.Frame parent) {
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
            java.util.logging.Logger.getLogger(newlogin1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(newlogin1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(newlogin1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(newlogin1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        newlogin1 dialog = new newlogin1(new javax.swing.JFrame());
        dialog.setVisible(true);
        return dialog.jdbcRetObj;

    }

    public newlogin1(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setTitle("Query Light");
        this.setLocationRelativeTo(null);
       // System.out.println(" try imae class path " + getClass(). );

        this.setIconImage(new ImageIcon(getClass().getResource("/db1-icon.png")).getImage());
        Thread TH = new Thread() {
            @Override
            public void run() {
                bc = new Bms_Constants();

            }
        };
        populate_database_list();
        TH.start();
        populate_stored_db_details();
        lbl_status.setBackground(new java.awt.Color(0, 0, 0, 0));

        tbl_dblist.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent lse) {
                tbl_dblistProcessChange();
            }
        });
        tbl_dblist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_dblist.setComponentPopupMenu(tbl_popmenu);

    }

    private void populate_stored_db_details() {
        /*     Preferences prf = Preferences.userNodeForPackage(this.getClass());
        dblist = prf.get("DBLIST", "");
        this.dbnames.clear(); */

        if (sdd.getDbRecordsCount() != 0) {
            /*     int loc = 0;
            int beg = 0;
            for (;;) {
                loc = dblist.indexOf("^", beg);
                if (loc <= 0) {
                    break;
                }
                String dbdtl = dblist.substring(beg, loc);
                this.dbnames.addElement(dbdtl.toUpperCase());
                beg = loc + 1;
            }

             */
            DefaultTableModel dtm = (DefaultTableModel) tbl_dblist.getModel();

            for (int i = 0; i < sdd.getDbRecordsCount(); i++) {
                Vector<String> row = new Vector<String>();
                 row.add(sdd.getDbRecordDatabaseNameAt(i));
                row.add(sdd.getDbRecordUserNameAt(i));
               dtm.addRow(row);
            }
            RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(dtm);
            this.tbl_dblist.setRowSorter(sorter);
            ArrayList<SortKey> list = new ArrayList<SortKey>();
            list.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
            sorter.setSortKeys(list);
        }
    }

    private void populate_database_list() {

      //  DefaultComboBoxModel dcb = new DefaultComboBoxModel(bc.get_db_names());
        DefaultComboBoxModel dcb = new DefaultComboBoxModel(new StoredTnsEntries().getDb_list());
        this.v_database.setModel(dcb);
        AutoCompleteDecorator.decorate(this.v_database);
        this.v_database.setSelectedIndex(-1);
    }

    private void initComponents() {

        tbl_popmenu = new javax.swing.JPopupMenu();
        Delete = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_dblist = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        v_username = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        v_password = new javax.swing.JPasswordField();
        btn_connect = new javax.swing.JButton();
        btn_close = new javax.swing.JButton();
        v_database = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lbl_status = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txt_jdbc_url = new javax.swing.JTextField();

        Delete.setText("Delete");
        Delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteActionPerformed(evt);
            }
        });
        tbl_popmenu.add(Delete);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tbl_dblist.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Database", "User / Schema"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tbl_dblist);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        v_username.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                v_usernameActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("User Name :");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Password :");

        v_password.setToolTipText("");

        btn_connect.setText("Connect");
        btn_connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_connectActionPerformed(evt);
            }
        });

        btn_close.setText("Close");
        btn_close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_closeActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Database :");

        jScrollPane2.setBorder(null);

        lbl_status.setEditable(false);
        lbl_status.setBackground(javax.swing.UIManager.getDefaults().getColor("Nb.Desktop.background"));
        lbl_status.setColumns(20);
        lbl_status.setForeground(new java.awt.Color(51, 51, 51));
        lbl_status.setLineWrap(true);
        lbl_status.setRows(5);
        lbl_status.setBorder(null);
        lbl_status.setOpaque(false);
        jScrollPane2.setViewportView(lbl_status);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("JDBC URL :");

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("OR");

        txt_jdbc_url.setToolTipText("FORMAT : jdbc:oracle:thin:@SERVER:PORT:SID");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(v_username, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(v_password, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(183, 183, 183)
                                .addComponent(jScrollPane2))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(btn_connect)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(btn_close, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(v_database, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(txt_jdbc_url))
                                .addContainerGap())))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(v_username, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(v_password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(v_database, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txt_jdbc_url, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_connect)
                    .addComponent(btn_close))
                .addGap(33, 33, 33)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 393, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tbl_dblistProcessChange() {

        try {
            int sel_idx = this.tbl_dblist.getSelectedRow();
            String usr = this.tbl_dblist.getValueAt(sel_idx, 1).toString();
            String db = this.tbl_dblist.getValueAt(sel_idx, 0).toString();
            String pwd = sdd.get_stored_pwd(db.toUpperCase(), usr.toUpperCase());
            String jdbcString = sdd.get_stored_jdbcstr(db.toUpperCase(), usr.toUpperCase());
            if (db != null && pwd != null) {
                this.v_username.setText(usr);
                this.v_password.setText(pwd);
                this.v_database.setSelectedItem(db);

                if(db.contains(":"))
                {            
                this.txt_jdbc_url.setText(jdbcString);
                }
            }

        } catch (Exception e) {
        }

    }

    private void v_usernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_v_usernameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_v_usernameActionPerformed

    private void btn_connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_connectActionPerformed
        ResultSet R;
        String connstr = null;
        try {
            if (sdd.get_stored_jdbcstr(v_database.getSelectedItem().toString().toUpperCase(), v_username.getText().toUpperCase()) != null
                    && sdd.get_stored_jdbcstr(v_database.getSelectedItem().toString().toUpperCase(), v_username.getText().toUpperCase()).length() > 3) {
                connstr = sdd.get_stored_jdbcstr(v_database.getSelectedItem().toString().toUpperCase(), v_username.getText().toUpperCase());
            } else {
                connstr = queryTNSTable(v_database.getSelectedItem().toString()).getJdbcString();
                if ( connstr == null ) {
                    connstr = bc.get_jdbc_str(v_database.getSelectedItem().toString());
                }
            }
        } catch (Exception e) {

            lbl_status.setText("Could not get any stored DB details.");
            e.printStackTrace();
        }
        String query = "select SYS_CONTEXT('USERENV','SERVER_HOST') server_host from dual";
        JdbcPersistent jcon = null;
        char[] tmp = v_password.getPassword();
        String pwd = new String(tmp);
        try {
            if (txt_jdbc_url.getText().length() > 3) {
                jcon = new JdbcPersistent(v_username.getText(), pwd, txt_jdbc_url.getText());
                connstr=txt_jdbc_url.getText();
            } else {
                if (connstr != null) {
                    jcon = new JdbcPersistent(v_username.getText(), pwd, connstr);
                }
            }

            R = jcon.runqry(query);
            if (R.next()) {
                {
                    lbl_status.setText("Connected to " + R.getString(1));

                    try {
                        String tmpDbName = (txt_jdbc_url.getText().length() > 2) ? txt_jdbc_url.getText().trim() : v_database.getSelectedItem().toString().toUpperCase();
                        sdd.store_db_details(tmpDbName, v_username.getText().toUpperCase(), pwd, connstr);
                    } catch (NullPointerException e) {
                        try {
                            sdd.store_db_details(connstr, v_username.getText().toUpperCase(), pwd, connstr);
                        } catch (NullPointerException ex) {

                            lbl_status.setText("Connected but cannot store DB details configure TNS");
                        }

                    }
                    //submit_report.call_submit_report(txt_username.getText(), pwd,connstr,txt_database.getSelectedItem().toString());
                    if (this.internal_call) {

                        jdbcRetObj = jcon;
                        jdbcRetObj.setDbName((txt_jdbc_url.getText().length() > 2) ? txt_jdbc_url.getText().trim() : v_database.getSelectedItem().toString().toUpperCase());
                        this.setVisible(false);
                        this.dispose();

                    } else {
                        if (txt_jdbc_url.getText().length() > 3) {
                            StmtRunScreen1.Companion.open_screen(v_username.getText(), pwd, txt_jdbc_url.getText(), txt_jdbc_url.getText());
                            jcon.getConn().close();
                            this.dispose();
                        } else {
                            StmtRunScreen1.Companion.open_screen(v_username.getText(), pwd, connstr, v_database.getSelectedItem().toString());
                            jcon.getConn().close();
                            this.dispose();
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            lbl_status.setText(ex.toString());
            ex.printStackTrace();
        } catch (Exception ex) {
            lbl_status.setText(ex.toString());
            ex.printStackTrace();
        }

    }//GEN-LAST:event_btn_connectActionPerformed

    private void btn_closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_closeActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_btn_closeActionPerformed

    private void DeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteActionPerformed

        int sel_idx = this.tbl_dblist.getSelectedRow();
        String db = this.tbl_dblist.getValueAt(sel_idx, 0).toString();
        String usr = this.tbl_dblist.getValueAt(sel_idx, 1).toString();

        sdd.delete_stored_dbRecord(db,usr);
        tbl_dblist.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Database", "User / Schema"
                }
        ) {
            Class[] types = new Class[]{
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean[]{
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        populate_stored_db_details();
    }//GEN-LAST:event_DeleteActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
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
            java.util.logging.Logger.getLogger(newlogin1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(newlogin1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(newlogin1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(newlogin1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>


        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                newlogin1 dialog = new newlogin1(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        ((newlogin1) e.getSource()).dispose();
                    }
                });
                dialog.setVisible(true);
            }
        });
    }



    private javax.swing.JMenuItem Delete;
    private javax.swing.JButton btn_close;
    private javax.swing.JButton btn_connect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea lbl_status;
    private javax.swing.JTable tbl_dblist;
    private javax.swing.JPopupMenu tbl_popmenu;
    private javax.swing.JTextField txt_jdbc_url;
    private javax.swing.JComboBox v_database;
    private javax.swing.JPasswordField v_password;
    private javax.swing.JTextField v_username;
    // End of variables declaration//GEN-END:variables
}
