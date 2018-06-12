/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runsql_anony;

import javax.swing.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author hv655
 */
public class cascadeDeleteUI1 extends javax.swing.JDialog {

    JdbcPersistent jcon;
    public boolean okActionPerformed = false;
    cascadeDelete ccd;
    
    public String getTxtOwner() {
        return txtOwner.getText().toUpperCase();
    }

    public String getTxtTable() {
        return txtTable.getText().toUpperCase();
    }

    public String getTxtWhereClause() {
        return txtWhereClause.getText();
    }


    public cascadeDeleteUI1(java.awt.Frame parent) {
        super(parent, true);
       
        initComponents();
        this.setTitle("Query Light");
        this.setLocationRelativeTo(null);
        this.setIconImage(new ImageIcon(getClass().getResource("/runsql_anony/db1-icon.png")).getImage());
    }

    public static cascadeDeleteUI1 showConnDialog(java.awt.Frame parent, JdbcPersistent jcon , cascadeDelete ccd) {
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
            java.util.logging.Logger.getLogger(cascadeDeleteUI1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(cascadeDeleteUI1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(cascadeDeleteUI1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(cascadeDeleteUI1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        
        cascadeDeleteUI1 dialog = new cascadeDeleteUI1(new javax.swing.JFrame(), jcon , ccd);
        dialog.setVisible(true);
        return dialog;
      
    }

    public cascadeDeleteUI1(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setTitle("Query Light");
        this.setLocationRelativeTo(null);
        this.setIconImage(new ImageIcon(getClass().getResource("/runsql_anony/db1-icon.png")).getImage());


    }

        public cascadeDeleteUI1(java.awt.Frame parent, JdbcPersistent jcon , cascadeDelete ccd) {
        super(parent, false);
        this.jcon=jcon;
        this.ccd=ccd;
        initComponents();
        this.setTitle("Query Light");
        this.setLocationRelativeTo(null);
        this.setIconImage(new ImageIcon(getClass().getResource("/runsql_anony/db1-icon.png")).getImage());


    }


    /*  private String get_stored_pwd(String db, String usr) {
        Preferences prf = Preferences.userNodeForPackage(this.getClass());
        String pwd = prf.get((db + ":" + usr + ":" + "PASSWORD").toUpperCase(), "");
        return pwd;
    }

    private String get_stored_jdbcstr(String db, String usr) {
        Preferences prf = Preferences.userNodeForPackage(this.getClass());
        String jdbcstr = prf.get((db + ":" + usr + ":" + "JDBCSTR").toUpperCase(), "");
        return jdbcstr;
    }

    private void store_pwd(String db, String usr, String pwd) {
        Preferences prf = Preferences.userNodeForPackage(this.getClass());
        prf.put((db + ":" + usr + ":" + "PASSWORD").toUpperCase(), pwd);
    }

    private void store_jdbcstr(String db, String usr, String jdbcstr) {
        Preferences prf = Preferences.userNodeForPackage(this.getClass());
        prf.put((db + ":" + usr + ":" + "JDBCSTR").toUpperCase(), jdbcstr);
    }

    private void store_db_details(String db, String usr, String pwd, String jdbcstr) {
        Preferences prf = Preferences.userNodeForPackage(this.getClass());
        if (this.dblist.contains(db + ":" + usr)) {
        } else {
            prf.put("DBLIST", this.dblist + db + ":" + usr + "^");
            this.store_pwd(db, usr, pwd);
            this.store_jdbcstr(db, usr, jdbcstr);
        }
    }
     */
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tbl_popmenu = new javax.swing.JPopupMenu();
        Delete = new javax.swing.JMenuItem();
        jPanel2 = new javax.swing.JPanel();
        txtOwner = new javax.swing.JTextField();
        txtTable = new javax.swing.JTextField();
        txtWhereClause = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtSQL = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtStatus = new javax.swing.JTextArea();

        Delete.setText("Delete");
        Delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteActionPerformed(evt);
            }
        });
        tbl_popmenu.add(Delete);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Owner :");

        jLabel2.setText("Table Name :");

        jLabel3.setText("Where Clause :");

        btnOk.setText("Ok");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.setToolTipText("");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtOwner, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtTable, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(90, 90, 90))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtWhereClause, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 14, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 22, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtOwner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtWhereClause, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancel)))
        );

        txtSQL.setColumns(20);
        txtSQL.setFont(new java.awt.Font("Courier New", 0, 13)); // NOI18N
        txtSQL.setRows(5);
        txtSQL.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                txtSQLCaretUpdate(evt);
            }
        });
        txtSQL.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtSQLKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(txtSQL);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setText("Simple SQL (Delete from only 1 table at a time)");

        txtStatus.setBackground(new java.awt.Color(240, 240, 240));
        txtStatus.setColumns(20);
        txtStatus.setRows(5);
        txtStatus.setBorder(null);
        jScrollPane2.setViewportView(txtStatus);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 747, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
 
    public void setStatusText(String Staus)
    {
        this.txtStatus.append(Staus);
    }
    
    private void DeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteActionPerformed
;
    }//GEN-LAST:event_DeleteActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
       ccd.startMainChurnProcess();
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
  this.dispose();        // TODO add your handling code here:
    }//GEN-LAST:event_btnCancelActionPerformed

    private void txtSQLKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSQLKeyTyped
        // TODO add your handling code here:

    }//GEN-LAST:event_txtSQLKeyTyped

    private void txtSQLCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtSQLCaretUpdate
        // TODO add your handling code here:
                 String sqlQuery = this.txtSQL.getText();
         String tablePattern = "from\\s*(.+?\\s)";
         String wherePattern = "(where.+)";
         Pattern r = Pattern.compile(tablePattern, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE );
         Matcher m = r.matcher(sqlQuery.toUpperCase());
        if (m.find( ))   {
            if (m.group(1).contains("."))
            {
                this.txtOwner.setText(m.group(1).replaceAll(".+\\.", " ").trim().toUpperCase());
                this.txtTable.setText(m.group(1).replaceAll("(.+)\\."  , " ").trim().toUpperCase());
            }
            else
            {
                this.txtTable.setText(m.group(1).trim().toUpperCase());
                this.txtOwner.setText(this.jcon.returnSingleField("select table_owner  from all_synonyms where owner = 'PUBLIC' and SYNONYM_NAME ='" + this.txtTable.getText() + "'"));
            }
        }
         r = Pattern.compile(wherePattern, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE );
         m = r.matcher(sqlQuery);
        if (m.find())
        this.txtWhereClause.setText(m.group(1));
    }//GEN-LAST:event_txtSQLCaretUpdate

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
            java.util.logging.Logger.getLogger(cascadeDeleteUI1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(cascadeDeleteUI1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(cascadeDeleteUI1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(cascadeDeleteUI1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>


        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                cascadeDeleteUI1 dialog = new cascadeDeleteUI1(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        ((cascadeDeleteUI1) e.getSource()).dispose();
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem Delete;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu tbl_popmenu;
    private javax.swing.JTextField txtOwner;
    private javax.swing.JTextArea txtSQL;
    private javax.swing.JTextArea txtStatus;
    private javax.swing.JTextField txtTable;
    private javax.swing.JTextField txtWhereClause;
    // End of variables declaration//GEN-END:variables
}
