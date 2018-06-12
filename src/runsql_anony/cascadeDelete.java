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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hv655
 */
public class cascadeDelete {

    private final JdbcPersistent jcon;
    private String vTableName;
    private String vOwner;
    private String vWhereClause;
    private int levelCounter = 0;
    private nodeStructure rootTable;
    private StringBuffer buffer = new StringBuffer();
    private StringBuffer insert_stmts = new StringBuffer();
    private int sequenceNum=0;
    private  cascadeDeleteUI1 cu;

    private class nodeStructure {

        int level;
        String owner;
        String tableName;
        String whereClause;
        private ArrayList<nodeStructure> child = new ArrayList();
        private ArrayList<queryRecordStructure> queryChildRecStructure = new ArrayList();
        

        public nodeStructure(int level, String owner, String tableName, String whereClause) {
            this.level = level;
            this.owner = owner;
            this.tableName = tableName;
            this.whereClause = whereClause;
        }

    }
    
    private class queryRecordStructure 
    {
        public String childtable;
        public String childOwner;
        public String childWhereClause;
        public String predicatesql;

        public queryRecordStructure(String childtable, String childOwner, String childWhereClause, String predicatesql) {
            this.childtable = childtable;
            this.childOwner = childOwner;
            this.childWhereClause = childWhereClause;
            this.predicatesql = predicatesql;
        }
        
    }
    

    public cascadeDelete(JdbcPersistent jcon) {
        this.jcon = jcon;
        this.showdialog();
    }

    private void buildTree(nodeStructure currentNode) {
    JdbcPersistent temp=new JdbcPersistent(this.jcon);
    boolean haschildren = false;
        try {
           
            String sql = "SELECT  child_owner,  child_tab , 'select  ' ||  LISTAGG(child_column , ',') WITHIN GROUP (ORDER BY 1  )    || '  from '  ||   child_tab || '   where ' ||  LISTAGG(child_column , ',') WITHIN GROUP (ORDER BY 1  )    ||'  in  ( '  || ' )' ,   LISTAGG( 'chr(39)||' ||  Parent_column ||  '||CHR(39)||'   , ''',''||' ) WITHIN GROUP (ORDER BY 1  )  parent_columns  ,LISTAGG(child_column , ',') WITHIN GROUP (ORDER BY 1  )  child_columns \n"
                    + " FROM (\n"
                    + "select      con.owner , con_col.table_name parent_tab , con_col.column_name Parent_column  , con.table_name child_tab  ,child_col.column_name  child_column  , con.owner child_owner \n"
                    + "from all_constraints con , all_cons_columns con_col , all_cons_columns child_col\n"
                    + "where  con_col.table_name ='" + currentNode.tableName + "'\n"
                    + "and con_col.owner = '" + currentNode.owner + "'\n"
                    + "and con.CONSTRAINT_TYPE = 'R'\n"
                    + "and con.R_owner=con_col.owner\n"
                    + "and con.r_CONSTRAINT_NAME = con_col.CONSTRAINT_NAME\n"
                    + "and CHILD_COL.CONSTRAINT_NAME = con.CONSTRAINT_NAME\n"
                    + "and CHILD_COL.owner = con.owner\n"
                    + "and child_col.position = con_col.position\n"
                    + ")\n"
                    + "GROUP BY child_tab,child_owner";
            java.sql.PreparedStatement STMT;
            STMT = temp.getConn().prepareStatement(sql);
     
            java.sql.ResultSet rs = STMT.executeQuery();
            
            while (rs.next()) {
                String predicatesql = "select " + rs.getString(4).substring(0 , rs.getString(4).length() - 2 ) + " from " + currentNode.owner + "." + currentNode.tableName + "  " + currentNode.whereClause;
                java.sql.PreparedStatement STMT2;
                STMT2 = temp.getConn().prepareStatement(predicatesql);
                java.sql.ResultSet rs2 = STMT2.executeQuery();
                while( rs2.next())
                {
                String predicate =  rs2.getString(1);
                String childWhereClause = " where (" + rs.getString(5) + ")  "  +  " =  (( " + predicate + ")) " ;
                 if (predicate!=null) {
                 String childSelect = "select count(1) from " + rs.getString(1) + "." + rs.getString(2)+  " " + childWhereClause;    
                 if( Integer.parseInt((jcon.returnSingleField(childSelect)==null )? "0" : jcon.returnSingleField(childSelect) ) > 0) {
                 currentNode.queryChildRecStructure.add(new queryRecordStructure(rs.getString(2) , rs.getNString(1), childWhereClause, predicatesql));
                 this.cu.setStatusText("Visiting "  + rs.getString(2) + "\n" );
                 haschildren=true;
                 }
                 }
                }
            }
            if (haschildren) this.levelCounter++;
            STMT.close();
            temp.getConn().close();
            for ( queryRecordStructure q :  currentNode.queryChildRecStructure ) {    
                 String childtable =  q.childtable;
                 String childOwner = q.childOwner;
                 nodeStructure childNode = new nodeStructure(this.levelCounter + 1, childOwner, childtable, q.childWhereClause);
                 currentNode.child.add(childNode);
                 buildTree(childNode);
                 
                 }
            if (haschildren) this.levelCounter++;
         /*   JdbcPersistent deleteTemp=new JdbcPersistent(this.jcon);
            ScriptRunner SR = new ScriptRunner(deleteTemp.conn, false, false);
            //StringReader istream = new StringReader("delete from " + currentNode.owner + "." + currentNode.tableName + " " + currentNode.whereClause);
            StringBuffer Result = SR.runScript("delete from " + currentNode.owner + "." + currentNode.tableName + " " + currentNode.whereClause, null , null );
            buffer.append( "\n sql : Result "+ Result + "delete from " + currentNode.owner + "." + currentNode.tableName + " " + currentNode.whereClause   );
            deleteTemp.conn.commit(); 
            deleteTemp.conn.close(); */
           buffer.append("\nprompt deleting from " + currentNode.owner + "." + currentNode.tableName + " Sequence no - " +  Integer.toString(this.sequenceNum++) + "\n" +
                   "delete from " + currentNode.owner + "." + currentNode.tableName + " " + currentNode.whereClause  + ";" );
         
        } catch (SQLException ex) {
            Logger.getLogger(cascadeDelete.class.getName()).log(Level.SEVERE, "select * from " +  currentNode.owner + "." + currentNode.tableName + " " + currentNode.whereClause  ,ex );
      /*  } catch (IOException ex) {
            Logger.getLogger(cascadeDelete.class.getName()).log(Level.SEVERE, null, ex); */
        } catch (Exception ex) {
            Logger.getLogger(cascadeDelete.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void processDeleteOperation() {
        this.rootTable = new nodeStructure(this.levelCounter, this.vOwner, this.vTableName, this.vWhereClause);
        buildTree(this.rootTable);
    }

    public void showdialog() {
         this.cu = cascadeDeleteUI1.showConnDialog(null,jcon , this);
    }

    public void startMainChurnProcess()
       {
            try {
                vTableName = cu.getTxtTable();
                vOwner = cu.getTxtOwner();
                vWhereClause = cu.getTxtWhereClause();
                processDeleteOperation();
                traverseTree(this.rootTable);
                longMsg1.main(buffer);
                longMsg1.main(this.insert_stmts);
            } catch (Exception ex) {
                Logger.getLogger(cascadeDelete.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
    private void traverseTree(nodeStructure node) throws Exception
    {
        this.insert_stmts.append( jcon.genInsFromQuery("select * from " +  node.owner + "." + node.tableName + " " + node.whereClause, node.tableName)+ "\n" );
        for (nodeStructure n : node.child)
        {
            traverseTree(n);
        }
       
    }
    
}
