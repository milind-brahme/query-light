/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import oracle_xls_extract.Oracle_xls_extract;
import runsql_anony.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author hv655
 */
public class StmtRunScreen1 extends javax.swing.JFrame {

    private class dbSession extends javax.swing.JInternalFrame {

        public void setCurrentFile(String currentFile) {
            this.currentFile = currentFile;
            File temp = new File(currentFile);
            String Path = temp.getAbsolutePath();
            Preferences prf = Preferences.userNodeForPackage(StmtRunScreen1.class);
            prf.put("LastDir", Path);
        }

        public String getCurrentFile() {
            return currentFile;
        }

        public JdbcPersistent getJcon() {
            return jcon;
        }

        public String v_usr;
        public String v_password;
        public String v_jdbcstr;
        public String dbname;
        private JdbcPersistent jcon;
        private boolean execute_all_flag = false;
        private String lastsql;
        private String currentFile = null;
        private Font gridFont;
        private Font editorFont;

        public dbSession(String v_usr, String v_password, String v_jdbcstr, String Db) {
            final javax.swing.JInternalFrame jf = this;
            Preferences prf = Preferences.userNodeForPackage(StmtRunScreen1.class);
            if (prf.get("fontName", null) != null) {
                gridFont = new Font(prf.get("fontName", "Calibri"), prf.getInt("fontstyle", Font.PLAIN), prf.getInt("fontsize", 10));
            } else {
                gridFont = new Font("Calibri", Font.PLAIN, 13);
            }

            try {
                this.setMaximizable(true);
                this.setResizable(true);
                ResultSet R;
                this.v_usr = v_usr;
                this.v_password = v_password;
                this.v_jdbcstr = v_jdbcstr;
                this.dbname = Db;
                this.jcon = new JdbcPersistent(this.v_usr, this.v_password, this.v_jdbcstr, this.dbname);
                initComponents();

                JButton jb = new JButton(this.v_usr + "@" + dbname);
                jb.setBorder(new javax.swing.border.SoftBevelBorder(1));
                jb.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        for (int i = 0; i < winList.size(); i++) {
                            if (winList.get(i).jb == evt.getSource()) {
                                try {
                                    winList.get(i).frmInstance.setSelected(true);
                                } catch (PropertyVetoException ex) {
                                    Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                break;
                            }
                        }
                    }
                });
                mdi_toolbar.add(jb);
                winList.add(new windowlist(this.v_usr + "@" + jcon.getDbName(), this, jb));
                this.setTitle(this.v_usr + "@" + jcon.getDbName());
                jsyntaxpane.DefaultSyntaxKit.initKit();
                v_stmt_txt.setContentType("text/sql");
                //v_stmt_txt.setFont(new Font("Calibri",Font.PLAIN,13));
                Preferences prf2 = Preferences.userNodeForPackage(StmtRunScreen1.class);
                if (prf.get("EditorfontName", null) != null) {
                    editorFont = new Font(prf2.get("EditorfontName", "Calibri"), prf2.getInt("Editorfontstyle", Font.PLAIN), prf2.getInt("Editorfontsize", 10));
                } else {
                    editorFont = new Font("Calibri", Font.PLAIN, 13);
                }
                v_stmt_txt.setFont(editorFont);
                set_icons();
                this.toggle_qry.setVisible(false);
                this.btn_execute.setVisible(false);
                R = jcon.runqry("select SYS_CONTEXT('USERENV','DB_NAME') instance_name , SYS_CONTEXT('USERENV','SERVER_HOST') server_host from dual");
                if (R.next()) {
                    this.v_instance.setText(R.getString(1));
                    this.v_host.setText(R.getString(2));
                }
            } catch (Exception ex) {
                Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
            }
            String columnNames[] = {"-", "-", "-"};
            String dataValues[][] = {{"-", "-", "-"}};
            tbl_grid = new JTable(dataValues, columnNames);
            JTableHeader header = tbl_grid.getTableHeader();
            tbl_grid.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        mnu_grid.show(tbl_grid, e.getX(), e.getY());
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
            tbl_grid.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            tbl_grid.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            tbl_grid.setCellSelectionEnabled(true);

            header.addMouseListener(new TableHeaderMouseListener(tbl_grid));
            this.grid_pane.getViewport().addChangeListener(new javax.swing.event.ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    try {
                        Rectangle viewRect = grid_pane.getViewport().getViewRect();
                        if (tbl_grid != null) {
                            int first = tbl_grid.rowAtPoint(new Point(0, viewRect.y));
                            if (first == -1) {
                                return; // Table is empty
                            }
                            int last = tbl_grid.rowAtPoint(new Point(0, viewRect.y + viewRect.height - 1));
                            if (last == -1) {
                                last = tbl_grid.getRowCount() - 1; // Handle empty space below last row
                            }
                            if (last == tbl_grid.getRowCount() - 1) {
                                try {
                                    //JOptionPane.showMessageDialog( jf ,"Eggs are not supposed to be green.", "Inane warning",  JOptionPane.WARNING_MESSAGE);
                                    jcon.fetchNextRows(tbl_grid);
                                } catch (SQLException ex) {
                                    throw ex;
                                }
                            } //... Last row is visible
                        }
                    } catch (Exception ex) {
                        //   Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (AbstractMethodError ex) {

                    }

                }
            });
            this.addInternalFrameListener(new InternalFrameListener() {
                @Override
                public void internalFrameOpened(InternalFrameEvent ife) {
                    setButtonOnActivation(ife);
                }

                @Override
                public void internalFrameClosing(InternalFrameEvent ife) {
                    jf.dispose();
                }

                @Override
                public void internalFrameClosed(InternalFrameEvent ife) {
                    removeOnDeActivation(ife);
                    jf.dispose();
                    jf.removeAll();
                }

                @Override
                public void internalFrameIconified(InternalFrameEvent ife) {

                }

                @Override
                public void internalFrameDeiconified(InternalFrameEvent ife) {

                }

                @Override
                public void internalFrameActivated(InternalFrameEvent ife) {

                    setButtonOnActivation(ife);
                }

                @Override
                public void internalFrameDeactivated(InternalFrameEvent ife) {

                }
            });
            try {
                this.setSelected(true);
            } catch (PropertyVetoException ex) {
                // Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
            }

            this.setClosable(true);
            this.setMaximizable(true);

        }

        public String getV_usr() {
            return v_usr;
        }

        public String getV_password() {
            return v_password;
        }

        public String getV_jdbcstr() {
            return v_jdbcstr;
        }

        public String getDbname() {
            return dbname;
        }

        public JEditorPane getV_stmt_txt() {
            return v_stmt_txt;
        }

        private void removeOnDeActivation(InternalFrameEvent ife) {
            for (int i = 0; i < winList.size(); i++) {
                if (winList.get(i).frmInstance == ife.getInternalFrame()) {
                    mdi_toolbar.remove(winList.get(i).jb);
                    mdi_toolbar.revalidate();
                    mdi_toolbar.repaint();
                    winList.remove(i);
                }

            }
        }

        private void setButtonOnActivation(InternalFrameEvent ife) {
            for (int i = 0; i < winList.size(); i++) {
                if (winList.get(i).frmInstance == ife.getInternalFrame()) {
                    winList.get(i).jb.setOpaque(true);
                    winList.get(i).jb.setBackground(Color.decode("#FFFFE0"));
                    winList.get(i).jb.setEnabled(false);
                } else {
                    winList.get(i).jb.setOpaque(true);
                    winList.get(i).jb.setEnabled(true);
                    winList.get(i).jb.setBackground(Color.GRAY);
                }

            }
        }

        public class TableHeaderMouseListener extends MouseAdapter {

            private JTable table;

            public TableHeaderMouseListener(JTable table) {
                this.table = table;
            }

            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Point point = event.getPoint();
                    String column_name = table.getColumnName(table.columnAtPoint(point));
                    StringSelection stringSelection = new StringSelection(column_name);
                    Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clpbrd.setContents(stringSelection, null);
                }
            }
        }

        /**
         * This method is called from within the constructor to initialize the
         * form. WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
        private void initComponents() {

            mnu_grid = new javax.swing.JPopupMenu();
            menu_tbl_copy = new JMenuItem();
            menu_get_count = new JMenuItem();
            menu_export_ins = new JMenuItem();
            menu_export_csv = new JMenuItem();
            menu_export_xls = new JMenuItem();

            internal_db1 = this;
            lbl_instance = new JLabel();
            v_instance = new JLabel();
            lbl_host = new JLabel();
            v_host = new JLabel();
            jSplitPane1 = new javax.swing.JSplitPane();
            jScrollPane3 = new javax.swing.JScrollPane();
            v_stmt_txt = new JEditorPane();
            jTabbedPane2 = new javax.swing.JTabbedPane();
            grid_pane = new javax.swing.JScrollPane();
            jScrollPane1 = new javax.swing.JScrollPane();
            lbl_op_status = new javax.swing.JTextArea();
            jLabel1 = new JLabel();
            toolbar = new javax.swing.JToolBar();
            btn_reconnect = new JButton();
            btn_upper = new JButton();
            btn_lower = new JButton();
            btn_commit = new JButton();
            btn_rollback = new JButton();
            btn_execute = new JButton();
            toggle_qry = new javax.swing.JToggleButton();

            menu_tbl_copy.setIcon(new ImageIcon(getClass().getResource("/runsql_anony/Clone-50.png"))); // NOI18N
            menu_tbl_copy.setText("Copy");
            menu_tbl_copy.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menu_tbl_copyActionPerformed(evt);
                }
            });
            mnu_grid.add(menu_tbl_copy);

            menu_get_count.setIcon(new ImageIcon(getClass().getResource("/runsql_anony/View Details-50.png"))); // NOI18N
            menu_get_count.setText("Record Count");
            menu_get_count.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menu_get_countActionPerformed(evt);
                }
            });
            mnu_grid.add(menu_get_count);

            menu_export_ins.setIcon(new ImageIcon(getClass().getResource("/runsql_anony/SQL-50.png"))); // NOI18N
            menu_export_ins.setText("Export : Insert Statment");
            menu_export_ins.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menu_export_insActionPerformed(evt);
                }
            });
            mnu_grid.add(menu_export_ins);

            menu_export_csv.setIcon(new ImageIcon(getClass().getResource("/runsql_anony/CSV-50.png"))); // NOI18N
            menu_export_csv.setText("Export : CSV");
            menu_export_csv.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menu_export_csvActionPerformed(evt);
                }
            });
            mnu_grid.add(menu_export_csv);
            menu_export_xls.setText("Export : Excel");
            menu_export_xls.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menu_export_xlsActionPerformed(evt);
                }
            });
            mnu_grid.add(menu_export_xls);

            JMenuItem tblResize = new JMenuItem("Resize to fit Text");
            tblResize.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jcon.resize_tbl_cols(tbl_grid, 3000);
                }
            });
            mnu_grid.add(tblResize);

            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
            addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(KeyEvent evt) {
                    formKeyPressed(evt);
                }
            });

            this.setVisible(true);

            lbl_instance.setFont(new Font("Tahoma", 1, 11)); // NOI18N
            lbl_instance.setText("Instance : ");

            v_instance.setText(" ");

            lbl_host.setFont(new Font("Tahoma", 1, 11)); // NOI18N
            lbl_host.setText("Host :");

            v_host.setText(" ");

            jSplitPane1.setDividerLocation(350);
            jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            jSplitPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
                public void componentResized(java.awt.event.ComponentEvent evt) {
                    jSplitPane1ComponentResized(evt);
                }
            });

            v_stmt_txt.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(KeyEvent evt) {
                    v_stmt_txtKeyPressed(evt);
                }
            });
            jScrollPane3.setViewportView(v_stmt_txt);

            jSplitPane1.setLeftComponent(jScrollPane3);

            jTabbedPane2.setName(""); // NOI18N
            jTabbedPane2.addComponentListener(new java.awt.event.ComponentAdapter() {
                public void componentResized(java.awt.event.ComponentEvent evt) {
                    jTabbedPane2ComponentResized(evt);
                }
            });

            grid_pane.setToolTipText("");
            grid_pane.setPreferredSize(new Dimension(1000, 100));
            jTabbedPane2.addTab("Grid", grid_pane);

            lbl_op_status.setEditable(false);
            lbl_op_status.setBackground(new Color(204, 204, 204));
            lbl_op_status.setColumns(20);
            lbl_op_status.setRows(5);
            jScrollPane1.setViewportView(lbl_op_status);

            jTabbedPane2.addTab("Script Output ", jScrollPane1);

            jSplitPane1.setRightComponent(jTabbedPane2);

            jLabel1.setFont(new Font("Tahoma", 1, 11)); // NOI18N
            jLabel1.setForeground(new Color(0, 102, 204));
            jLabel1.setText("Press (Ctr+Enter) to run query ,  F5 to run update , delete insert (DML) , Shift + F5 to run all text as script");

            toolbar.setFloatable(false);
            toolbar.setRollover(true);

            btn_reconnect.setIcon(new ImageIcon(getClass().getResource("/runsql_anony/Connected-50.png"))); // NOI18N
            btn_reconnect.setToolTipText("Reconnect");
            btn_reconnect.setFocusable(false);
            btn_reconnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            btn_reconnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            btn_reconnect.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btn_reconnectActionPerformed(evt);
                }
            });
            toolbar.add(btn_reconnect);

            btn_upper.setIcon(new ImageIcon(getClass().getResource("/runsql_anony/Up Arrow-50.png"))); // NOI18N
            btn_upper.setToolTipText("UpperCase");
            btn_upper.setFocusable(false);
            btn_upper.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            btn_upper.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            btn_upper.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btn_upperActionPerformed(evt);
                }
            });
            toolbar.add(btn_upper);

            btn_lower.setIcon(new ImageIcon(getClass().getResource("/runsql_anony/Down Arrow-50.png"))); // NOI18N
            btn_lower.setToolTipText("LowerCase");
            btn_lower.setFocusable(false);
            btn_lower.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            btn_lower.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            btn_lower.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btn_lowerActionPerformed(evt);
                }
            });
            toolbar.add(btn_lower);

            btn_commit.setIcon(new ImageIcon(getClass().getResource("/runsql_anony/Accept Database-50.png"))); // NOI18N
            btn_commit.setToolTipText("Commit");
            btn_commit.setFocusable(false);
            btn_commit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            btn_commit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            btn_commit.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btn_commitActionPerformed(evt);
                }
            });
            toolbar.add(btn_commit);

            btn_rollback.setIcon(new ImageIcon(getClass().getResource("/runsql_anony/Delete Database-50.png"))); // NOI18N
            btn_rollback.setToolTipText("Rollback changes");
            btn_rollback.setFocusable(false);
            btn_rollback.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            btn_rollback.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            btn_rollback.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btn_rollbackActionPerformed(evt);
                }
            });
            toolbar.add(btn_rollback);

            btn_execute.setText("Execute");
            btn_execute.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btn_executeActionPerformed(evt);
                }
            });
            toolbar.add(btn_execute);

            toggle_qry.setText("Query Mode");
            toggle_qry.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    toggle_qryActionPerformed(evt);
                }
            });
            toolbar.add(toggle_qry);

            javax.swing.GroupLayout internal_db1Layout = new javax.swing.GroupLayout(this.getContentPane());
            this.getContentPane().setLayout(internal_db1Layout);
            internal_db1Layout.setHorizontalGroup(
                    internal_db1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(internal_db1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(internal_db1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(internal_db1Layout.createSequentialGroup()
                                                    .addComponent(lbl_instance)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(v_instance, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(lbl_host)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(v_host, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jLabel1)
                                                    .addGap(0, 77, Short.MAX_VALUE))
                                            .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                    .addContainerGap())
            );
            internal_db1Layout.setVerticalGroup(
                    internal_db1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(internal_db1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(internal_db1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(lbl_instance)
                                            .addComponent(v_instance)
                                            .addComponent(lbl_host)
                                            .addComponent(v_host)
                                            .addComponent(jLabel1))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                                    .addContainerGap())
            );

            javax.swing.GroupLayout deskTopPaneLayout = new javax.swing.GroupLayout(deskTopPane);
            deskTopPane.setLayout(deskTopPaneLayout);
            deskTopPaneLayout.setHorizontalGroup(
                    deskTopPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(deskTopPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(this)
                                    .addContainerGap())
            );
            deskTopPaneLayout.setVerticalGroup(
                    deskTopPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(deskTopPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(this)
                                    .addContainerGap())
            );
            //deskTopPane.setLayer(this, javax.swing.JLayeredPane.DEFAULT_LAYER);

            pack();
        }// </editor-fold>

        private void btn_executeActionPerformed(java.awt.event.ActionEvent evt) {
            ResultSet R;
            String sql_stmt = null;
            int offset_semi_Colon = 0;
            String intermediate = null;
            String result = null;
            boolean executeSelected = false;
            if (v_stmt_txt.getSelectedText() != null && v_stmt_txt.getSelectedText().length() > 1) {
                executeSelected = true;
            }
            //Modifying for new logic Start 27-08-2015
            StringBuffer initText = null;
            try {
                initText = new StringBuffer((executeSelected) ? v_stmt_txt.getSelectedText() : v_stmt_txt.getDocument().getText(0, v_stmt_txt.getDocument().getLength()));
            } catch (BadLocationException ex) {
                Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {

                ScriptRunner SR = new ScriptRunner(this.jcon.getConn(), false, false);
                ArrayList<JLabel> bindVars = new ArrayList<JLabel>();
                ArrayList<JTextField> bindTxtBoxes = new ArrayList<JTextField>();
                ArrayList<JComponent> comps = new ArrayList<JComponent>();
                ArrayList<String> bindValues = new ArrayList<String>();
                ArrayList<String> bindName = new ArrayList<String>();
                boolean bindVarExists = false;

                if (tbl_grid != null) {
                    this.grid_pane.getViewport().remove(tbl_grid);
                    this.grid_pane.getViewport().validate();
                }
                if (!executeSelected) {
                    int caretPos = v_stmt_txt.getCaretPosition();
                    int stmt_pos = initText.indexOf("\n", caretPos);
                    if (stmt_pos < 0) {
                        stmt_pos = initText.length() - 1;
                    }
                    //show_msg( stmt_pos + ":" );
                    //   else if(stmt_pos-v_stmt_txt.getCaretPosition() <=1) stmt_pos=v_stmt_txt.getText().indexOf("\n", stmt_pos+1);
                    intermediate = initText.insert(stmt_pos, "`~~~`").toString().replaceAll("\n", "~``~").replaceAll("\\s+", " ");
                    String[] sqlStatements = intermediate.split("~``~\\s*~``~");
                    for (int i = 0; i < sqlStatements.length; i++) {
                        if (sqlStatements[i].contains("`~~~`")) {
                            result = (sqlStatements[i] + "~``~").replace("`~~~`", "").replaceAll("--.*?~``~", " ").replaceAll("~``~", " ");
                        }
                    }
                }

                String tmp_sql_stmt = (executeSelected) ? initText.toString() : result;
                int end_pos = tmp_sql_stmt.lastIndexOf(";", tmp_sql_stmt.length() - 1) > 0 ? tmp_sql_stmt.lastIndexOf(";", tmp_sql_stmt.length() - 1) : tmp_sql_stmt.length();
                StringReader istream;
                istream = this.execute_all_flag ? new StringReader(initText.toString()) : new StringReader(tmp_sql_stmt);
                if (toggle_qry.getText().equals("Query Mode")) {
                    sql_stmt = tmp_sql_stmt.substring(0, end_pos);
                    // Pattern patt = Pattern.compile("(:\\w+\\s*(?=([^\"]*\"[^\"]*\")*[^\"]*$))");
                    Pattern patt = Pattern.compile("(:\\w+\\s*(?=([^']*'[^']*')*[^']*$))");
                    Matcher mat = patt.matcher(sql_stmt.toLowerCase() + " ");
                    int counter = 0;
                    OUTER:
                    while (mat.find()) {
                        bindVarExists = true;
                        for (int i = 0; i < bindVars.size(); i++) {
                            if (mat.group(1).trim().equals(bindVars.get(i).getText().trim())) {
                                continue OUTER;
                            }
                        }
                        bindVars.add(new JLabel(mat.group(1)));
                        bindName.add(mat.group(1).trim());
                        bindTxtBoxes.add(new JTextField());
                        comps.add(bindVars.get(counter));
                        comps.add(bindTxtBoxes.get(counter));
                        counter++;
                    }
                    if (bindVarExists) {   //String sqlwithbind = mat.replaceAll(" ? ");
                        javax.swing.JCheckBox defineoff = new javax.swing.JCheckBox("Set Define off");
                        comps.add(defineoff);
                        JOptionPane.showMessageDialog(null, comps.toArray(), "Enter Bind Values", JOptionPane.PLAIN_MESSAGE);
                        for (int i = 0; i < bindVars.size(); i++) {
                            bindValues.add(bindTxtBoxes.get(i).getText());
                        }
                        if (defineoff.isSelected()) {
                            bindVarExists = false;
                            jcon.TableFromDatabase(tbl_grid, sql_stmt);
                        } else {
                            jcon.TableFromDatabase(tbl_grid, sql_stmt, bindValues, bindName);
                        }
                    } else {
                        jcon.TableFromDatabase(tbl_grid, sql_stmt);
                    }
                    if (gridFont != null) {
                        tbl_grid.setFont(gridFont);
                    }
                    tbl_grid.addKeyListener(new ClipboardKeyAdapter(tbl_grid));
                    this.lastsql = sql_stmt;
                    //resize_tbl_cols(tbl_grid);
                    this.grid_pane.getViewport().add(tbl_grid);
                    this.jTabbedPane2.setSelectedIndex(0);
                } else {
                    this.jTabbedPane2.setSelectedIndex(1);
                    if (this.execute_all_flag) {
                        if (initText.toString().contains("end;")) {
                            this.lbl_op_status.setText(SR.runScript(initText.toString(), null, null).toString());
                        } else {
                            String Result = SR.runScript(istream).toString();
                            this.lbl_op_status.setText(Result);
                        }
                    } else {
                        sql_stmt = tmp_sql_stmt.substring(0, end_pos);
                        // Pattern patt = Pattern.compile("(:\\w+\\s*(?=([^\"]*\"[^\"]*\")*[^\"]*$))");
                        Pattern patt = Pattern.compile("(:\\w+\\s*(?=([^']*'[^']*')*[^']*$))");
                        Matcher mat = patt.matcher(sql_stmt.toLowerCase() + " ");
                        int counter = 0;
                        OUTER:
                        while (mat.find()) {
                            bindVarExists = true;
                            for (int i = 0; i < bindVars.size(); i++) {
                                if (mat.group(1).trim().equals(bindVars.get(i).getText().trim())) {
                                    continue OUTER;
                                }
                            }
                            bindVars.add(new JLabel(mat.group(1)));
                            bindName.add(mat.group(1).trim());
                            bindTxtBoxes.add(new JTextField());
                            comps.add(bindVars.get(counter));
                            comps.add(bindTxtBoxes.get(counter));
                            counter++;
                        }
                        if (bindVarExists) {
                            javax.swing.JCheckBox defineoff = new javax.swing.JCheckBox("Set Define off");
                            comps.add(defineoff);
                            JOptionPane.showMessageDialog(null, comps.toArray(), "Enter Bind Values", JOptionPane.PLAIN_MESSAGE);
                            for (int i = 0; i < bindVars.size(); i++) {
                                bindValues.add(bindTxtBoxes.get(i).getText());
                            }
                            if (defineoff.isSelected()) {
                                bindVarExists = false;
                                bindValues.clear();
                                bindName.clear();
                            }
                        }
                        String addcolon = ((sql_stmt.toLowerCase().contains("begin") && sql_stmt.toLowerCase().contains("end"))) ? ";" : "";
                        this.lbl_op_status.setText(SR.runScript(sql_stmt + addcolon, bindValues, bindName).toString());
                        /* String Result = SR.runScript(istream).toString();
                            if (Result.startsWith("\n!Update/Delete Count")) {
                                show_msg(Result.substring(2));
                            }
                            this.lbl_op_status.setText(SR.runScript(istream).toString());*/
                    }
                }
            } catch (Exception ex) {
                this.show_alert(" Error : " + ex.toString()
                        + "\nIf you are trying to run DML or PLSQL please use F5");
                //Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
            SimpleDateFormat f = new SimpleDateFormat("dd/mmm/yyyy");

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

//table.getColumnModel().getColumn(0).setCellRenderer(tableCellRenderer);
        private void resize_tbl_cols(JTable table) {
            TableColumnModel columnModel = table.getColumnModel();
            TableCellRenderer renderer;
            int width; // Min width
            TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
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
            for (int column = 0; column < table.getColumnCount(); column++) {
                TableColumn tableColumn = columnModel.getColumn(column);
                //  width = tableColumn.getPreferredWidth();
                TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
                Object headerValue = tableColumn.getHeaderValue();
                Component headerComp = headerRenderer.getTableCellRendererComponent(table, headerValue, false, false, 0, column);
                width = headerComp.getPreferredSize().width;
                table.getColumnModel().getColumn(column).setCellRenderer(tableCellRenderer);
                for (int row = 0; row <= table.getRowCount(); row++) {
                    renderer = table.getCellRenderer(row, column);
                    Component comp = null;
                    try {
                        comp = table.prepareRenderer(renderer, row, column);
                        width = Math.min(200, Math.max(comp.getPreferredSize().width, width));
                    } catch (Exception e) {
                        // width = Math.min(200,Math.max(comp.getPreferredSize().width, width));
                    }

                }
                columnModel.getColumn(column).setPreferredWidth(width);
            }
        }

        private void toggle_qryActionPerformed(java.awt.event.ActionEvent evt) {
            if (!toggle_qry.isSelected()) {
                toggle_qry.setText("Query Mode");
            } else {
                toggle_qry.setText("Script Mode");
            }
        }

        private void btn_reconnectActionPerformed(java.awt.event.ActionEvent evt) {
            try {
                jcon.reconnect();
            } catch (Exception ex) {
                show_alert(ex.toString());
            }
        }

        private void btn_rollbackActionPerformed(java.awt.event.ActionEvent evt) {
            try {
                jcon.getConn().rollback();
            } catch (SQLException ex) {
                show_alert(ex.toString());
            }
        }

        private void menu_get_countActionPerformed(java.awt.event.ActionEvent evt) {
            String rec_cnt;
            if (this.lastsql != null) {
                try {
                    String cnt_qry = "select count(1) from (" + this.lastsql + ")";
                    rec_cnt = jcon.returnSingleField(cnt_qry);
                    show_msg("Record Count : " + rec_cnt);
                } catch (Exception ex) {
                    show_alert(ex.toString());
                }
            }
        }

        private void btn_commitActionPerformed(java.awt.event.ActionEvent evt) {
            try {
                jcon.getConn().commit();
            } catch (SQLException ex) {
                show_alert(ex.toString());
            }
        }

        private void jTabbedPane2ComponentResized(java.awt.event.ComponentEvent evt) {

        }

        private void jSplitPane1ComponentResized(java.awt.event.ComponentEvent evt) {

        }

        private void ExitActionPerformed(java.awt.event.ActionEvent evt) {
            // TODO add your handling code here:
        }

        private void menu_export_insActionPerformed(java.awt.event.ActionEvent evt) {
            Pattern patt = Pattern.compile("select(.*)from(\\s+[^\\s]+\\s)");
            Matcher mat = patt.matcher(lastsql.toLowerCase() + " ");
            String tablename = null;
            if (mat.find()) {
                tablename = mat.group(2);
                JOptionPane.showInputDialog(this, "Enter table_name for the extract", tablename);
            }

            try {
                StringBuffer ins_script = jcon.genInsFromQuery(lastsql, tablename);
                StringSelection stringSelection = new StringSelection(ins_script.toString());
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
            } catch (Exception ex) {
                show_alert(ex.toString());
            }
        }

        private void menu_export_csvActionPerformed(java.awt.event.ActionEvent evt) {
            Object[] options = {"ClipBoard", "File", "Cancel"};
            int n = JOptionPane.showOptionDialog(this, "Export the data to : ", "Clipboard / File ?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[2]);
            if (n == JOptionPane.CANCEL_OPTION) {
                return;
            } else if (n == JOptionPane.YES_OPTION) {
                try {
                    StringBuffer CSV = jcon.genCSVfrmQuery(lastsql);
                    StringSelection stringSelection = new StringSelection(CSV.toString());
                    Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clpbrd.setContents(stringSelection, null);
                } catch (Exception ex) {
                    this.show_alert(ex.toString());
                }
            } else {
                String tmp = fileNameToSave();
                if (tmp != null) {
                    String fname = (tmp.toLowerCase().endsWith(".csv")) ? tmp : tmp + ".csv";
                    try {
                        jcon.genCSVfilefrmQuery(lastsql, fname);
                    } catch (Exception ex) {
                        this.show_alert(ex.toString());
                    }
                }
            }

        }

        private void menu_export_xlsActionPerformed(java.awt.event.ActionEvent evt) {

            String tmp = fileNameToSave();
            if (tmp != null) {
                String fname = (tmp.toLowerCase().endsWith(".xlsx")) ? tmp : tmp + ".xlsx";
                try {
                    //jcon.genCSVfilefrmQuery(lastsql, fname);
                    Oracle_xls_extract.main(new String[]{"-u", jcon.getUid(), "-p", jcon.getPwd(), "-j", jcon.getJdbcstr(), "-q", lastsql, "-r", fname});
                    if (new File(fname).isFile() && new File(fname).length() > 0) {
                        this.show_msg("File : " + fname + " created");
                    }
                } catch (Exception ex) {
                    this.show_alert(ex.toString());
                }
            }

        }

        private void btn_upperActionPerformed(java.awt.event.ActionEvent evt) {
            try {
                this.v_stmt_txt.replaceSelection(this.v_stmt_txt.getSelectedText().toUpperCase());
                this.v_stmt_txt.setSelectedTextColor(Color.yellow);
            } catch (Exception e) {
            }
        }

        private void btn_lowerActionPerformed(java.awt.event.ActionEvent evt) {
            try {
                this.v_stmt_txt.replaceSelection(this.v_stmt_txt.getSelectedText().toLowerCase());
            } catch (Exception e) {
            }
        }

        private void formKeyPressed(KeyEvent evt) {
            // TODO add your handling code here:
        }

        private void v_stmt_txtKeyPressed(KeyEvent evt) {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                if (((evt.getModifiers() & KeyEvent.CTRL_MASK) != 0) && evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    toggle_qry.setText("Query Mode");
                    this.btn_executeActionPerformed(null);

                }
            } else if (evt.getKeyCode() == KeyEvent.VK_F5 && (evt.getModifiers() & KeyEvent.SHIFT_MASK) != 0) {
                toggle_qry.setText("Script Mode");
                this.execute_all_flag = true;
                this.btn_executeActionPerformed(null);
                this.execute_all_flag = false;
            } else if (evt.getKeyCode() == KeyEvent.VK_F5) {
                toggle_qry.setText("Script Mode");
                this.execute_all_flag = false;
                this.btn_executeActionPerformed(null);
            } else if (((evt.getModifiers() & KeyEvent.CTRL_MASK) != 0) && evt.getKeyCode() == KeyEvent.VK_U) {
                this.btn_upperActionPerformed(null);
            } else if (((evt.getModifiers() & KeyEvent.CTRL_MASK) != 0) && evt.getKeyCode() == KeyEvent.VK_L) {
                this.btn_lowerActionPerformed(null);
            } else if (evt.getKeyCode() == KeyEvent.VK_F4) {
                String obj = null;
                if (v_stmt_txt.getSelectedText() != null && v_stmt_txt.getSelectedText().length() > 1) {
                    obj = v_stmt_txt.getSelectedText();
                } else {
                    try {
                        obj = jcon.findWordAtCaret(v_stmt_txt.getDocument().getText(0, v_stmt_txt.getDocument().getLength()), v_stmt_txt.getCaretPosition());
                    } catch (BadLocationException ex) {
                        Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                descObject.main(jcon, obj);
            }
            this.setCursor(java.awt.Cursor.getDefaultCursor());
        }

        private void menu_tbl_copyActionPerformed(java.awt.event.ActionEvent evt) {
            StringBuffer txtToCopy = new StringBuffer();
            int colindx = this.tbl_grid.getSelectedColumn();
            int endcolindx = this.tbl_grid.getSelectedColumnCount() + colindx - 1;
            int rowindx = this.tbl_grid.getSelectedRow();
            int endrowindx = this.tbl_grid.getSelectedRowCount() + rowindx - 1;
            String linesep = System.getProperty("line.separator");
            for (int ch = colindx; ch <= endcolindx; ch++) {
                String sep = (ch == endcolindx) ? "" : "\t";
                txtToCopy.append(this.tbl_grid.getColumnName(ch) + sep);
            }
            txtToCopy.append(linesep);
            for (int r = rowindx; r <= endrowindx; r++) {
                for (int c = colindx; c <= endcolindx; c++) {
                    String sep = (c == endcolindx) ? "" : "\t";
                    if (this.tbl_grid.getValueAt(r, c) == null) {
                        txtToCopy.append(sep);
                    } else if (this.tbl_grid.getValueAt(r, c) instanceof Date) {
                        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String tmpdate = f.format(this.tbl_grid.getValueAt(r, c));
                        txtToCopy.append(tmpdate + sep);
                    } else {
                        txtToCopy.append(this.tbl_grid.getValueAt(r, c) + sep);
                    }
                }
                txtToCopy.append(linesep);
            }

            StringSelection stringSelection = new StringSelection(txtToCopy.toString());
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);

        }

        private String fileNameToSave() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                return (fileToSave.getAbsolutePath());
            }
            return null;
        }

        private void set_icons() {
            JButton btn;
            for (int i = 0; i < this.toolbar.getComponentCount(); i++) {
                try {
                    btn = (JButton) this.toolbar.getComponent(i);
                    ImageIcon icon = (ImageIcon) btn.getIcon();
                    Image img = icon.getImage();
                    Image newimg = img.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(newimg);
                    btn.setIcon(icon);
                } catch (Exception e) {
                }
            }
            JMenuItem jm;
            for (int i = 0; i < mnu_grid.getComponentCount(); i++) {
                try {
                    jm = (JMenuItem) mnu_grid.getComponent(i);
                    ImageIcon icon = (ImageIcon) jm.getIcon();
                    Image img = icon.getImage();
                    Image newimg = img.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(newimg);
                    jm.setIcon(icon);
                } catch (Exception e) {
                }
            }
        }


        private void show_msg(String msg) {
            JOptionPane.showMessageDialog(this, msg, "-", JOptionPane.INFORMATION_MESSAGE);
        }

        private void show_alert(String err_msg) {
            JOptionPane.showMessageDialog(this, err_msg, "Error", JOptionPane.ERROR_MESSAGE);
        }

        private JTable tbl_grid;
        private JTableHeader header;
        // Variables declaration - do not modify

        private JButton btn_commit;
        private JButton btn_execute;
        private JButton btn_lower;
        private JButton btn_reconnect;
        private JButton btn_rollback;
        private JButton btn_upper;
        private javax.swing.JScrollPane grid_pane;
        private javax.swing.JInternalFrame internal_db1;
        private JLabel jLabel1;

        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JScrollPane jScrollPane3;
        private javax.swing.JSplitPane jSplitPane1;
        private javax.swing.JTabbedPane jTabbedPane2;
        private JLabel lbl_host;
        private JLabel lbl_instance;
        private javax.swing.JTextArea lbl_op_status;
        private JMenuItem menu_export_csv;
        private JMenuItem menu_export_xls;
        private JMenuItem menu_export_ins;

        private JMenuItem menu_get_count;
        private JMenuItem menu_tbl_copy;
        private javax.swing.JPopupMenu mnu_grid;
        private javax.swing.JToggleButton toggle_qry;
        private javax.swing.JToolBar toolbar;
        private JLabel v_host;
        private JLabel v_instance;
        private JEditorPane v_stmt_txt;
    }

    public StmtRunScreen1(final String v_usr, final String v_password, final String v_jdbcstr, final String Db) {
        final javax.swing.JFrame parent = this;
        mdi_toolbar = new javax.swing.JToolBar();
        mdi_toolbar.setFloatable(false);
        mdi_toolbar.setRollover(false);
        deskTopPane = new javax.swing.JDesktopPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        /*  javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(deskTopPane, javax.swing.GroupLayout.Alignment.TRAILING)
         );
         layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(deskTopPane, javax.swing.GroupLayout.Alignment.TRAILING)
         );  */
        this.getContentPane().add(mdi_toolbar, BorderLayout.NORTH);
        this.getContentPane().add(deskTopPane);

        JMenuItem mnuOpenConn = new JMenuItem("Open New Connection");
        JMenuItem mnureadfile = new JMenuItem("Open File");
        JMenuItem mnusavefile = new JMenuItem("Save");
        JMenuItem mnusaveAs = new JMenuItem("Save As..");
        JMenuItem mnupref = new JMenuItem("Preferences");
        JMenuItem quickHelp = new JMenuItem("Quick Help");
        JMenuItem about = new JMenuItem("About Query Light..");
        menu_file = new javax.swing.JMenu();
        javax.swing.JMenu menu_help = new javax.swing.JMenu("Help");
        javax.swing.JMenu special_ops = new javax.swing.JMenu("Special Ops");
        JMenuItem cascadeDelete = new JMenuItem("Cascade Delete");
        special_ops.add(cascadeDelete);
        cascadeDelete.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                for (int i = 0; i < winList.size(); i++) {
                    if (winList.get(i).frmInstance.isSelected()) {
                        dbSession frm = winList.get(i).frmInstance;
                        cascadeDelete cs = new cascadeDelete(frm.getJcon());
                    }
                }

            }
        });
        JMenuItem menuInsertfromDiffDb = new JMenuItem("Insert from diffrent database");
        special_ops.add(menuInsertfromDiffDb);
        menuInsertfromDiffDb.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                for (int i = 0; i < winList.size(); i++) {
                    if (winList.get(i).frmInstance.isSelected()) {
                        dbSession frm = winList.get(i).frmInstance;
                          insertFromAnotherDb.openInsertDialog( frm.getJcon());
                    }
                }

            }
        });
        JMenuItem jMenuExcelOps = new JMenuItem("Excel Operations");
        special_ops.add(jMenuExcelOps);
        jMenuExcelOps.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                for (int i = 0; i < winList.size(); i++) {
                    if (winList.get(i).frmInstance.isSelected()) {
                         dbSession frm = winList.get(i).frmInstance;
                          excelOps.main(frm.getJcon());
                    }
                }

            }
        });


        menu_file.setMnemonic(KeyEvent.VK_F);
        Exit = new JMenuItem();
        menu_edit = new javax.swing.JMenu();
        menu_help.add(quickHelp);
        menu_help.setMnemonic(KeyEvent.VK_H);
        menu_help.add(about);
        about.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javax.swing.JInternalFrame jif = new javax.swing.JInternalFrame("Query Light", false, true);
                jif.add(new qryLightAbout());
                deskTopPane.add(jif);
                int x = parent.getX() + (parent.getWidth() / 2);
                int y = parent.getY() + (parent.getHeight() / 2);
                jif.setVisible(true);
                jif.pack();
                Dimension jInternalFrameSize = jif.getSize();
                jif.setLocation(x - (jInternalFrameSize.width / 2), (y - (jInternalFrameSize.height) / 2));
                try {
                    jif.setSelected(true);
                } catch (PropertyVetoException ex) {
                    //Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        quickHelp.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javax.swing.JInternalFrame jif = new javax.swing.JInternalFrame("Query Light", false, true);
                jif.add(new qryLightHelp());
                deskTopPane.add(jif);
                jif.setVisible(true);
                jif.pack();
                try {
                    jif.setSelected(true);
                } catch (PropertyVetoException ex) {
                    //Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        menu_edit.setMnemonic(KeyEvent.VK_E);
        menu_file.add(mnureadfile);
        mnureadfile.setMnemonic(KeyEvent.VK_O);
        menu_file.add(mnusavefile);
        menu_file.add(mnusaveAs);
        menu_file.add(mnuOpenConn);
        mnupref.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javax.swing.JDialog dlg = new javax.swing.JDialog();
                ImageIcon img = new ImageIcon((getClass().getResource("/runsql_anony/db1-icon.png")));
                dlg.setIconImage(img.getImage());
                dlg.add(new qryLightPrf());
                dlg.pack();
                dlg.setLocationRelativeTo(null);
                dlg.setVisible(true);
            }
        });

        mnusaveAs.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveEditorFile(true);
            }
        });
        mnusavefile.setMnemonic(KeyEvent.VK_S);
        mnusavefile.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveEditorFile(false);
            }
        });
        mnureadfile.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadFileInEditor();
            }
        });

        mnuOpenConn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JdbcPersistent jp = newlogin1.showConnDialog(parent);
                addNewConnWin(jp);
            }
        });
        menu_file.add(Exit);
        jMenuBar1.add(menu_file);
        menu_edit.setText("Edit");
        menu_edit.add(mnupref);
        jMenuBar1.add(menu_edit);
        jMenuBar1.add(menu_help);
        jMenuBar1.add(special_ops);
        menu_file.setText("File");
        Exit.setText("Exit");
        Exit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitApp();
            }
        });

        setJMenuBar(jMenuBar1);
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                final dbSession db1 = new dbSession(v_usr, v_password, v_jdbcstr, Db);
            }
        });

        // final dbSession db1 = new dbSession(v_usr, v_password, v_jdbcstr, Db);
        this.setTitle("Query Light");
        // ImageIcon img = new ImageIcon(Bms_Constants.project_icon);
        this.setIconImage(new ImageIcon(getClass().getResource("/runsql_anony/db1-icon.png")).getImage());

        this.pack();
        Thread TH = new Thread() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {
                    // Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
                }
                /* try {
            db1.setSelected(true);
        } catch (PropertyVetoException ex) {
            //  Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
        } */
            }
        };

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitApp();

            }
        });
        this.setExtendedState(this.getExtendedState() | javax.swing.JFrame.MAXIMIZED_BOTH);
        deskTopPane.setDragMode(javax.swing.JDesktopPane.OUTLINE_DRAG_MODE);
    }

    public static void open_screen(final String username, final String password, final String jdbcstr, final String Db) {
        /* Set the Nimbus look and feel */
        final String uid = username;
        final String pwd = password;
        final String jdbcurl = jdbcstr;
        final String tmpDb = Db;
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
            Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new StmtRunScreen1(username, password, jdbcstr, Db).setVisible(true);
            }
        });
    }

    private class windowlist {

        public windowlist(String window_name, dbSession frmInstance, JButton jb) {
            this.window_name = window_name;
            this.frmInstance = frmInstance;
            this.jb = jb;
        }
        public String window_name;
        public dbSession frmInstance;
        public JButton jb;

    }

    private void saveEditorFile(boolean saveAs) {
        String fname;
        for (int i = 0; i < winList.size(); i++) {
            if (winList.get(i).frmInstance.isSelected()) {
                dbSession frm = winList.get(i).frmInstance;
                if (frm.getV_stmt_txt().getText().length() > 0) {
                    try {
                        if (frm.getCurrentFile() != null && !saveAs) {
                            fname = frm.getCurrentFile();
                        } else {
                            fname = JdbcPersistent.Companion.fileNameToSave(this.getClass());
                        }
                        if (fname != null) {
                            frm.setCurrentFile(fname);
                            JdbcPersistent.Companion.writeStringToFile(frm.getV_stmt_txt().getText(), fname);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.toString());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No Text to Save !");

                }

                break;
            }
        }
    }

    private void loadFileInEditor() {
        for (int i = 0; i < winList.size(); i++) {
            if (winList.get(i).frmInstance.isSelected()) {
                dbSession frm = winList.get(i).frmInstance;
                if (frm.getV_stmt_txt().getText().length() > 0) {
                    try {
                        JdbcPersistent jp = new JdbcPersistent(frm.getJcon());
                        addNewConnWin(jp);
                        String fname = jp.fileNameToOpen("Sql , Text", new String[]{"SQL", "TXT"}, this.getClass());
                        if (fname != null) {
                            String toLoad = jp.Companion.readStringFromFile(fname);
                            frm.setCurrentFile(fname);
                            winList.get(winList.size() - 1).frmInstance.getV_stmt_txt().setText(toLoad);
                        }
                    } catch (NullPointerException ex) {
                        Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.toString());
                    }
                } else {
                    try {
                        String fname = frm.getJcon().fileNameToOpen("Sql , Text", new String[]{"SQL", "TXT"}, this.getClass());
                        if (fname != null) {
                            String toLoad = frm.getJcon().Companion.readStringFromFile(fname);
                            frm.setCurrentFile(fname);
                            frm.getV_stmt_txt().setText(toLoad);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.toString());
                    }
                }

                break;
            }
        }
    }

    private void addNewConnWin(final JdbcPersistent jp) {
        new dbSession(jp.getUid(), jp.getPwd(), jp.getJdbcstr(), jp.getDbName());
    }

    private void exitApp() {
        int response = JOptionPane.showConfirmDialog(this, "Close Query Lite", " Quit ? ", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            System.exit(0);
        }

    }

    ArrayList<windowlist> winList = new ArrayList<windowlist>();

    private javax.swing.JToolBar mdi_toolbar;
    private javax.swing.JDesktopPane deskTopPane;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu menu_file;
    private JMenuItem Exit;
    private javax.swing.JMenu menu_edit;
    // End of variables declaration                   
}
