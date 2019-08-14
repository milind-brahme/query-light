package com.milind.querylight;



import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.prefs.Preferences;


/**
 *
 * @author hv655
 */
public class descObject extends javax.swing.JFrame {
    private Font gridFont;
    private JdbcPersistent jcon;
    private String objname;
    private String objType;
    private String schemaName;
     private Preferences prf;
     private boolean displayForm=true;

    public String getObjname() {
        return objname;
    }

    public String getObjType() {
        return objType;
    }

    public String getSchemaName() {
        return schemaName;
    }

    descObject(JdbcPersistent jp, String oName, Boolean displayForm )
    {
        this.displayForm=displayForm;
        descObject_act(jp,oName);
    }

    descObject(JdbcPersistent jp, String oName )
    {
        descObject_act(jp,oName);
    }

    private void descObject_act(JdbcPersistent jp, String oName) {
        this.jcon = new JdbcPersistent(jp);
        this.objname = oName.toUpperCase();

       if(this.displayForm) {
           initComponents();
           this.setTitle("Query Light");
           setobjdetails(oName);
       }else
       {
           setobjdetails(oName);
           return;
       }
        prf = Preferences.userNodeForPackage(StmtRunScreen1.class);
          if (prf.get("fontName", null) != null) {
                gridFont = new Font(prf.get("fontName", "Calibri"), prf.getInt("fontstyle", Font.PLAIN), prf.getInt("fontsize", 10));
            } else {
                gridFont = new Font("Calibri", Font.PLAIN, 13);
            }

        if (this.objname != null && this.objType != null && this.schemaName != null) {
            this.v_oname.setText(objname);
            this.v_schema.setText(schemaName);
            this.v_type.setText(objType);
            populate_tabs();
        } else {
            jcon.show_alert(this, "Couldn't find the Object");
        }

           ImageIcon icon =new ImageIcon(getClass().getResource("/db1-icon.png"));
           Image img=icon.getImage();
           Image newimg = img.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
           icon = new ImageIcon(newimg);
           this.setIconImage(newimg);
           this.pack();
         this.setLocationRelativeTo(null);


    }

    private Dimension calcTableSize(JTable jt) {
        int width = 0;
        int height = 440;
        for (int i = 0; i < jt.getColumnCount(); i++) {
            width = width + jt.getColumnModel().getColumn(i).getWidth();
        }
        return (new Dimension(width + 60, height));
    }

    private void populate_tabs() {
        if ("TABLE".equals(this.objType)) {
            try {
                JdbcPersistent jtbl_info = new JdbcPersistent(jcon);
                JTable tbl_info = jtbl_info.create_standard_table();
                String sql = "select  ac.column_name \"Column_name\" ,\n"
                        + "case data_type\n"
                        + "when 'NUMBER' then 'NUMBER' || decode (data_precision,null , null, decode(data_scale, null, '('||data_precision||')' ,  '('||data_precision || ',' || data_scale || ')')  )\n"
                        + "when 'VARCHAR2' then 'VARCHAR2' || decode(char_col_decl_length,null,'' , '('||char_col_decl_length||')') \n"
                        + "else data_type END \"Data Type\" ,nullable \n"
                        + "from  all_tab_columns ac \n"
                        + "where   ac.table_name = '" + this.objname + "' and owner  ='" + this.schemaName + "' order by column_id";
                jtbl_info.TableFromDatabase(tbl_info, sql);
                JPanel jp = new JPanel();
                JdbcPersistent jpk = new JdbcPersistent(jcon);
                JTable pk = jpk.create_standard_table();
                jpk.TableFromDatabase(pk, "select column_name Primary_Key from all_cons_columns where constraint_name = (Select constraint_name from all_constraints"
                        + " where owner = '" + this.schemaName + "' and table_name = '" + this.objname + "' and constraint_type = 'P') order by position");
                jp.setLayout(new java.awt.BorderLayout());
                JScrollPane pk_pane = new JScrollPane(pk);
                JScrollPane tbl_pane = new JScrollPane(tbl_info);
                jp.add(pk_pane, BorderLayout.LINE_START);
                jp.add(tbl_pane, BorderLayout.CENTER);
                main_tab.addTab("Columns", jp);
                pk_pane.setPreferredSize(calcTableSize(pk));
                tbl_pane.setPreferredSize(calcTableSize(tbl_info));
                populate_indexes();
                JTable jstats = jcon.create_standard_table();
                jcon.TableFromDatabaseSingleRecordView(jstats, "select * from all_tables where table_name='" 
                 + this.objname + "' and owner = '" + this.schemaName +"'");
                JScrollPane stats_pane = new JScrollPane(jstats);
                 main_tab.addTab("Stats", stats_pane);
                jp.repaint();
                jp.revalidate();
                this.pack();
                
            } catch (SQLException ex) {
                jcon.show_alert(this, ex.toString());
            }

        }else if ("VIEW".equals(this.objType)) {
            try {
                JdbcPersistent jtbl_info = new JdbcPersistent(jcon);
                JTable tbl_info = jtbl_info.create_standard_table();
                String sql = "select  ac.column_name \"Column_name\" ,\n"
                        + "case data_type\n"
                        + "when 'NUMBER' then 'NUMBER' || decode (data_precision,null , null, decode(data_scale, null, '('||data_precision||')' ,  '('||data_precision || ',' || data_scale || ')')  )\n"
                        + "when 'VARCHAR2' then 'VARCHAR2' || decode(char_col_decl_length,null,'' , '('||char_col_decl_length||')') \n"
                        + "else data_type END \"Data Type\"  \n"
                        + "from  all_tab_columns ac \n"
                        + "where   ac.table_name = '" + this.objname + "' and owner  ='" + this.schemaName + "' order by column_id";
                jtbl_info.TableFromDatabase(tbl_info, sql);
                JScrollPane tbl_pane = new JScrollPane(tbl_info);
                main_tab.addTab("Columns", tbl_pane);
                tbl_pane.setPreferredSize(calcTableSize(tbl_info));
                //populate_indexes();
                JTable jstats = jcon.create_standard_table();
                jcon.TableFromDatabaseSingleRecordView(jstats, "select * from all_views where view_name='" 
                 + this.objname + "' and owner = '" + this.schemaName +"'");
                JScrollPane stats_pane = new JScrollPane(jstats);
                 main_tab.addTab("Stats", stats_pane);
                this.pack();
                
            } catch (SQLException ex) {
                jcon.show_alert(this, ex.toString());
            }

        } else if ("FUNCTION".equals(this.objType) || "PROCEDURE".equals(this.objType) || this.objType.contains("PACKAGE") ||"TRIGGER".equals(this.objType) ) 
        {
                        try {
                JdbcPersistent jtbl_info = new JdbcPersistent(jcon);
               // JTable tbl_info = jtbl_info.create_standard_table();
                javax.swing.JEditorPane tbl_info = new javax.swing.JEditorPane();
                JScrollPane tbl_pane = new JScrollPane(tbl_info);
                
                jsyntaxpane.DefaultSyntaxKit.initKit();
                tbl_info.setContentType("text/sql");
                //v_stmt_txt.setFont(new Font("Calibri",Font.PLAIN,13));
                tbl_info.setFont(this.gridFont);
                String sql = "select text from (\n" +
                "select 'D' id ,  d.* from dba_source d where upper(name) = '" + this.objname + "' and owner='" + this.schemaName + "'" + " \n" +
                "union all\n" +
                "select 'A' id  , a.* from all_source a where upper(name) = '" + this.objname + "' and owner='" + this.schemaName + "'" + "  \n" +
                ") where id = (select max(id) from (select 'D' id ,  d.* from dba_source d where upper(name) = '" + this.objname + "' and owner='" + this.schemaName + "'" + "\n" +
                "union all\n" +
                "select 'A' id  , a.* from all_source a where upper(name) = '" + this.objname + "' and owner='" + this.schemaName + "'" + " ))";
               String trgHeadersql = null;
               String trgbodySql = null ;
                if ("TRIGGER".equals(this.objType))
                       {
                            trgHeadersql = "select id || 'CREATE OR REPLACE TRIGGER ' || OWNER || '.' || TRIGGER_NAME || ' ' ||TRIGGER_TYPE  || ' ' ||  TRIGGERING_EVENT || ' INCOMPLETE ....' "
                                   + " from ( "
                                   + "select 'D' id ,  d.* from dba_triggers d where upper(trigger_name) = '" + this.objname + "' and owner='" + this.schemaName + "'" + " \n"
                                   + "union all\n"
                                   + "select 'A' id  , a.* from all_triggers a where upper(trigger_name) = '" + this.objname + "' and owner='" + this.schemaName + "'" + "  \n"
                                   + ") where id = (select max(id) from (select 'D' id ,  d.* from dba_triggers d where upper(trigger_name) = '" + this.objname + "' and owner='" + this.schemaName + "'" + "\n"
                                   + "union all\n"
                                   + "select 'A' id  , a.* from all_triggers a where upper(trigger_name) = '" + this.objname + "' and owner='" + this.schemaName + "'" + " ))";
                            
                                   
                 
                       }
                
               String codeText = null;
               String trgCodeHead =null;
               String trgCodeBody = null;
                main_tab.addTab("Code", tbl_pane);
                 if ("TRIGGER".equals(this.objType))
                 {
                      trgCodeHead =  jcon.returnSingleField(trgHeadersql , true , " ");
                      trgbodySql = "select trigger_body from " + (trgCodeHead.substring(0, 1).contains("D")?"dba_triggers":"all_triggers" ) + " where upper(trigger_name) = '" + this.objname + "' and owner='" + this.schemaName + "'" ;
                      trgCodeBody =  jcon.returnSingleField(trgbodySql , true , " ");
                      codeText = trgCodeHead.substring(1,trgCodeHead.length() ).concat(trgCodeBody);
                     
                 }else
                 {
                 codeText = jcon.returnSingleField(sql, true , " ");
                 }
                tbl_info.setText(codeText);
                tbl_pane.setPreferredSize(new Dimension(600, 700));
                //populate_indexes();
                JTable jstats = jcon.create_standard_table();
                jcon.TableFromDatabaseSingleRecordView(jstats, "select * from all_objects where object_name='" 
                 + this.objname + "' and owner = '" + this.schemaName +"'");
                JScrollPane stats_pane = new JScrollPane(jstats);
                tbl_info.setEditable(false);
                main_tab.addTab("Stats", stats_pane);
                this.pack();
                
                
            } catch (SQLException ex) {
                jcon.show_alert(this, ex.toString());
            }

        }else if ("CONSTRAINT".equals(this.objType)) {
            try {
                JdbcPersistent jtbl_info = new JdbcPersistent(jcon);
                JTable tbl_info = jtbl_info.create_standard_table();
                String sql = "select   con_col.table_name parent_table , con_col.column_name parent_table_column ,   con_col.position parent_column_position , child_col.table_name child_table ,child_col.column_name child_table_column ,   child_col.position child_column_position \n"
                        + " from all_constraints con , all_cons_columns con_col , all_cons_columns child_col\n"
                        + "where    con.CONSTRAINT_NAME  ='" + this.objname + "'\n"
                        + "and con.R_owner=con_col.owner\n"
                        + "and con.r_CONSTRAINT_NAME = con_col.CONSTRAINT_NAME\n"
                        + "and CHILD_COL.CONSTRAINT_NAME = con.CONSTRAINT_NAME\n"
                        + "and CHILD_COL.owner = con.owner\n"
                        + "and child_col.position = con_col.position";
                jtbl_info.TableFromDatabase(tbl_info, sql);
                JScrollPane tbl_pane = new JScrollPane(tbl_info);
                main_tab.addTab("Constraint Columns", tbl_pane);
                tbl_pane.setPreferredSize(calcTableSize(tbl_info));
                //populate_indexes();
                JTable jstats = jcon.create_standard_table();
                jcon.TableFromDatabaseSingleRecordView(jstats, "select * from all_constraints where constraint_name='" 
                 + this.objname + "'");
                JScrollPane stats_pane = new JScrollPane(jstats);
                 main_tab.addTab("Details", stats_pane);
                this.pack();
                
            } catch (SQLException ex) {
                jcon.show_alert(this, ex.toString());
            }

        } 
        
 }
    

    
    private void populate_indexes() throws SQLException
    {
        
        Vector<String> tblcols = new Vector<String>();
        tblcols.add("Index Name ");
        tblcols.add("Unique ? ");
        tblcols.add("Columns");
        Vector<Object> data = new Vector<Object>();
        ResultSet rs = jcon.runqry("select index_name ,uniqueness from all_indexes where owner = '" + this.schemaName
                + "' and table_name = '" +  this.objname +  "' order by index_name" );
        while(rs.next())
        {   Vector<Object> row = new Vector<Object>(2);
            String indx_name = rs.getString(1); 
            row.add(indx_name);
            row.add(rs.getString(2));
            ResultSet rc = jcon.runqry("select column_name from all_ind_columns where index_owner = '" + this.schemaName
                + "' and table_name = '" +  this.objname +  "' and index_name = '" +  indx_name +  "' order by index_name" );
             
             StringBuffer indxcols = new StringBuffer();
       while (rc.next())
       {        
             indxcols.append( "," + rc.getString(1));
       }
       row.add(indxcols.substring(1));
       data.add(row);
        }
       final DefaultTableModel model = new DefaultTableModel((Vector) data,tblcols );
          JTable jtindx = new JTable (model);
          jcon.resize_tbl_cols(jtindx,300);
          JScrollPane tbl_pane = new JScrollPane(jtindx);
          main_tab.addTab("INDEXES", tbl_pane);
        }
    
    private void setobjdetails(String oName) {
        if (oName != null && oName.indexOf(".") > -1) {
            this.schemaName = oName.substring(0, oName.indexOf(".") );
            this.objname = oName.substring(oName.indexOf(".") + 1, oName.length());
            setObjType(this.objname, this.schemaName);
        } else {
            setObjType(oName);
            setOwner(oName);
        }

    }

    private void setOwner(String oName) {
        if (oName != null && this.objType != null) {
            this.schemaName = jcon.returnSingleField("select owner  from all_objects  where object_name ='" + oName + "'"
                    + " and object_type='" + this.objType + "'");
            if (this.schemaName == null) {
                this.schemaName = jcon.returnSingleField("select owner  from dba_objects  where object_name ='" + oName + "'"
                        + " and object_type='" + this.objType + "'");
                if (this.schemaName == null) {
                    this.schemaName = jcon.returnSingleField("select owner  from all_constraints  where constraint_name ='" + oName + "'");
                }
            }
        }
    }

    private void setObjType(String oName, String Schema) {
        String othobj = jcon.returnSingleField("select object_type  from all_objects  where owner||'.'||object_name = ''" + Schema + "." + oName + "'");
        if (othobj != null) {
            this.objType = othobj;
        }else                  
        {
         String constraintName=  jcon.returnSingleField("select CONSTRAINT_NAME from all_constraints where constraint_name = '" + oName + "'");
       if (constraintName != null) 
       {
           this.objType = "CONSTRAINT";
           return;
       }
       }
            
    }

    private void setObjType(String objName) {
        String usrobj = jcon.returnSingleField("select object_type from all_objects where object_name = '" + objName + "'");
        if (usrobj != null) {
            this.objType = usrobj;
        }else 
        {
        String dbaobj = jcon.returnSingleField("select object_type from dba_objects where object_name = '" + objName + "'");
        if (dbaobj != null) {
            this.objType = dbaobj;
        }
                    else
        {
        String constraintName=  jcon.returnSingleField("select CONSTRAINT_NAME from all_constraints where constraint_name = '" + objName + "'");
       if (constraintName != null) 
       {
           this.objType = "CONSTRAINT";
              return;
       }
     
       }
      }

        String tmp = jcon.returnSingleField("select table_owner||'.'||table_name from all_synonyms where synonym_name = '" + objName + "'");
        String othobj = jcon.returnSingleField("select object_type  from all_objects  where owner||'.'||object_name = '" + tmp + "'");
        if (othobj != null) {
            this.objType = othobj;
        }else
        {
        String dtmp = jcon.returnSingleField("select table_owner||'.'||table_name from dba_synonyms where synonym_name = '" + objName + "'");
        String dothobj = dtmp==null ? jcon.returnSingleField("select object_type  from dba_objects  where object_name = '" + objName + "'") :  jcon.returnSingleField("select object_type  from dba_objects  where owner||'.'||object_name = '" + dtmp + "'");
        if (dothobj != null) {
            this.objType = dothobj;
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

        jToolBar1 = new javax.swing.JToolBar();
        lbl_oname = new javax.swing.JLabel();
        v_oname = new javax.swing.JLabel();
        lbl_type = new javax.swing.JLabel();
        v_type = new javax.swing.JLabel();
        lbl_schema = new javax.swing.JLabel();
        v_schema = new javax.swing.JLabel();
        main_tab = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("descObj"); // NOI18N

        jToolBar1.setRollover(true);

        lbl_oname.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lbl_oname.setForeground(new java.awt.Color(51, 51, 255));
        lbl_oname.setText("Object Name:");
        jToolBar1.add(lbl_oname);

        v_oname.setText(" ");
        v_oname.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jToolBar1.add(v_oname);

        lbl_type.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lbl_type.setForeground(new java.awt.Color(51, 51, 255));
        lbl_type.setText("Type :");
        jToolBar1.add(lbl_type);

        v_type.setText(" ");
        v_type.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jToolBar1.add(v_type);

        lbl_schema.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lbl_schema.setForeground(new java.awt.Color(51, 51, 255));
        lbl_schema.setText("Schema :");
        jToolBar1.add(lbl_schema);

        v_schema.setText(" ");
        v_schema.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jToolBar1.add(v_schema);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.PAGE_START);
        getContentPane().add(main_tab, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    public static void main(final JdbcPersistent jp, final String objName) {
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
            java.util.logging.Logger.getLogger(descObject.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(descObject.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(descObject.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(descObject.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new descObject(jp, objName.toUpperCase()).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lbl_oname;
    private javax.swing.JLabel lbl_schema;
    private javax.swing.JLabel lbl_type;
    private javax.swing.JTabbedPane main_tab;
    private javax.swing.JLabel v_oname;
    private javax.swing.JLabel v_schema;
    private javax.swing.JLabel v_type;
    // End of variables declaration//GEN-END:variables
}
