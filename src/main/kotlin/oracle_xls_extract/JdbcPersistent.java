package oracle_xls_extract;

import oracle.jdbc.OraclePreparedStatement;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;


/**
 *
 * @author sqloper4
 */
public class JdbcPersistent {

    
    public Connection conn = null;
    public Statement stmt = null;
    public ResultSet rs;
    private String uid;
    private String machine_name;
    private String dbName;
    public static String lineSeperator = System.getProperty("line.separator");

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }

    public String getMachine_name() {
        return machine_name;
    }

    public String getUid() {
        return uid;
    }

    public String getPwd() {
        return pwd;
    }

    public String getJdbcstr() {
        return jdbcstr;
    }
    private String pwd;
    private String jdbcstr;
    public static int jTableMaxColSize = 200;

    public JdbcPersistent(JdbcPersistent jp) {
        try {
            this.uid = jp.uid;
            this.pwd = jp.pwd;
            this.jdbcstr = jp.jdbcstr;
            this.dbName = jp.dbName;
            connect_db();
        } catch (Exception er) {
            String exption = er.toString();
            System.out.print(exption);
        } finally {
            try {
                ;//  if (conn != null) conn.close();
            } catch (Exception ignored) {
                String exption = ignored.toString();
                System.out.print(exption);
            }
        }
    }

    public JdbcPersistent(String uid, String pwd, String jdbcstr) throws Exception {
        try {
            this.uid = uid;
            this.pwd = pwd;
            this.jdbcstr = jdbcstr;
            connect_db();
        } catch (Exception er) {
            String exption = er.toString();
            throw er;
        } finally {
            try {
                ;//  if (conn != null) conn.close();
            } catch (Exception ignored) {
                String exption = ignored.toString();
                System.out.print(exption);
            }
        }
    }

    public JdbcPersistent(String uid, String pwd, String jdbcstr, String dbName) throws Exception {
        try {
            this.uid = uid;
            this.pwd = pwd;
            this.jdbcstr = jdbcstr;
            this.dbName = dbName;
            connect_db();
        } catch (Exception er) {
            String exption = er.toString();
            throw er;
        } finally {
            try {
                ;//  if (conn != null) conn.close();
            } catch (Exception ignored) {
                String exption = ignored.toString();
                System.out.print(exption);
            }
        }
    }

    public void fetchNextRows(JTable tbl) throws SQLException {
        Vector<Object> columnNames = new Vector<Object>();
        Vector<Object> data = new Vector<Object>();
        int row_count = 0;
        try {

            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            //TableModel model = tbl.getModel();
            DefaultTableModel model = (DefaultTableModel) tbl.getModel();
            while (rs.next()) {
                Vector<Object> row = new Vector<Object>(columns);

                for (int i = 0; i <= columns; i++) {
                    if (i == 0) {
                        row.addElement(rs.getRow());
                    } else {
                        try {
                            if (md.getColumnTypeName(i).equals("DATE")) {
                                row.addElement(rs.getTimestamp(i));
                            } else {
                                row.addElement(rs.getString(i));
                            }
                        } catch (NullPointerException e) {
                            row.addElement(null);
                        }
                    }
                }

                data.addElement(row);
                model.addRow(row);
                row_count++;
                if (row_count >= Ora_constants.fetchSize) {
                    break;
                }
            }

        } catch (SQLException e) {
            throw e;
        }
    }

    public   JTable create_standard_table() {
        return create_standard_tableAct(true , false , false );
    }
    
    public   JTable create_standard_table(boolean copyEnabled, boolean cutEnabled , boolean pasteEnabled) {
        return create_standard_tableAct(copyEnabled, cutEnabled, pasteEnabled);
    }
    
    private  JTable create_standard_tableAct(boolean copyEnabled, boolean cutEnabled , boolean pasteEnabled) {
        String columnNames[] = {"-", "-", "-"};
        String dataValues[][] = {{"-", "-", "-"}};
        JTable tbl_grid = new JTable(dataValues, columnNames);
     //   ClipboardKeyAdapter cka = new ClipboardKeyAdapter(tbl_grid , copyEnabled, cutEnabled, pasteEnabled);
      //  tbl_grid.addKeyListener(cka);      
        tbl_grid.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbl_grid.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        tbl_grid.setCellSelectionEnabled(true);

        tbl_grid.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    //Do somethihng on rightclick
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
        return tbl_grid;
    } 

    public void show_alert(Component c, String err_msg) {
        JOptionPane.showMessageDialog(c, err_msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void show_msg(Component c, String msg) {
        JOptionPane.showMessageDialog(c, msg, "-", JOptionPane.INFORMATION_MESSAGE);
    }

    public void resize_tbl_cols(JTable table, int maxcolsize) {
        resize_tbl_colsAct(table, maxcolsize);
    }

    public void resize_tbl_cols(JTable table) {
        resize_tbl_colsAct(table, JdbcPersistent.jTableMaxColSize);
    }

    private void resize_tbl_colsAct(JTable table, int maxcolsize) {
        TableColumnModel columnModel = table.getColumnModel();
        TableCellRenderer renderer;
        int width;
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
                    width = Math.min(maxcolsize, Math.max(comp.getPreferredSize().width, width));
                } catch (Exception e) {
                    // width = Math.min(200,Math.max(comp.getPreferredSize().width, width));
                }

            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    public String findWordAtCaret(String Text, int pos) {
        if (Text != null && Text.length() > 0) {
            StringBuffer TempString = new StringBuffer(Text.replaceAll("\\s+", "`"));
            int startpos = (TempString.lastIndexOf("`", pos) < 0) ? 0 : TempString.lastIndexOf("`", pos) + 1;
            int endpos = (TempString.indexOf("`", pos) < 0) ? TempString.length() : TempString.lastIndexOf("`", pos);
            return TempString.substring(startpos, endpos);
        } else {
            return "";
        }
    }

   public void TableFromDatabase(JTable tbl, String sql) throws SQLException {
        TableFromDatabaseAct(tbl, sql, false,null , null);
    }

 public void TableFromDatabase(JTable tbl, String sql, boolean fetchall) throws SQLException {
        TableFromDatabaseAct(tbl, sql, fetchall,null,null);
    }
    
    //  public Vector<String> getSingleFieldVector
    public void TableFromDatabase(JTable tbl, String sql,ArrayList<String> bindValue , ArrayList<String> bindName) throws SQLException {
        TableFromDatabaseAct(tbl, sql, false,bindValue , bindName);
    }

    public void TableFromDatabase(JTable tbl, String sql, boolean fetchall ,ArrayList<String> bindValue , ArrayList<String> bindName) throws SQLException {
        TableFromDatabaseAct(tbl, sql, fetchall, bindValue ,  bindName);
    }

    public StringBuffer genInsFromQuery(String sql, String tblname) throws Exception {
        return genInsFromQueryAct(sql, tblname);
    }

    public StringBuffer genInsFromQuery(String sql) throws Exception {
        return genInsFromQueryAct(sql, null);
    }

    private StringBuffer genInsFromQueryAct(String sql, String tblname) throws Exception {
        int row_count = 0;
        StringBuffer inser_script = new StringBuffer();
        java.sql.PreparedStatement STMT;
        try {
            JdbcPersistent jp = new JdbcPersistent(uid, pwd, jdbcstr);
            Vector<Object> columnNames = new Vector<Object>();
            STMT = jp.conn.prepareStatement(sql);
            jp.rs = STMT.executeQuery();
            ResultSetMetaData md = jp.rs.getMetaData();
            int columns = md.getColumnCount();
            String tablename = (tblname != null) ? tblname : "TABLENAME";
            for (int i = 1; i <= columns; i++) {
                columnNames.addElement(md.getColumnName(i));
            }

            //  Get row data
            while (jp.rs.next()) {
                StringBuffer ins_stmt = new StringBuffer("Insert into " + tablename + "(");
                for (int i = 1; i <= columns; i++) {
                    String sep = (i == columns) ? " " : ",";
                    ins_stmt.append(md.getColumnName(i) + sep);
                }
                ins_stmt.append(")" + lineSeperator   + " values(");
                for (int i = 1; i <= columns; i++) {
                    String sep = (i == columns) ? " " : ",";
                    try {
                        if (md.getColumnTypeName(i).contains("NUMBER")) {
                            ins_stmt.append(jp.rs.getString(i) + sep);
                        } else if (md.getColumnTypeName(i).equals("DATE")) {
                            String tmpdate = jp.rs.getString(i).substring(0, 19);
                            ins_stmt.append("to_date('" + tmpdate + "' , 'YYYY-MM-DD HH24:MI:SS' )" + sep);
                        }
                        else if (md.getColumnTypeName(i).equals("LONG"))
                        {

                            InputStream is =  jp.rs.getAsciiStream(i);
                            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                            int nRead;
                            byte[] data = new byte[1024];
                            while ((nRead = is.read(data, 0, data.length)) != -1) {
                                buffer.write(data, 0, nRead);
                            }

                            buffer.flush();
                            byte[] byteArray = buffer.toByteArray();

                            String text = new String(byteArray, StandardCharsets.UTF_8);

                            if (text == null) {
                                ins_stmt.append("null" + sep);
                            } else {
                                ins_stmt.append("'" +text.replace("'" , "''") + "'" + sep);
                            }
                        }
                        else {
                            if (jp.rs.getObject(i) == null) {
                                ins_stmt.append("null" + sep);
                            } else {
                                ins_stmt.append("'" + jp.rs.getString(i).replace("'" , "''") + "'" + sep);
                            }
                        }
                    } catch (NullPointerException e) {
                        ins_stmt.append("null" + sep);
                    }
                }
                ins_stmt.append(");" + lineSeperator);
                inser_script.append(ins_stmt);
            }

        } catch (SQLException e) {
            throw e;
        }
        return inser_script;
    }

    public static void writeStringToFile(String StringToWrite, String fname_path) throws Exception {
        String newLineCorrected = StringToWrite.replaceAll("\r\n|\n", lineSeperator);
        PrintWriter writer = new PrintWriter(fname_path, "UTF-8");
        writer.print(newLineCorrected);
        writer.close();

    }

    class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {

        String description;

        String extensions[];

        public ExtensionFileFilter(String description, String extension) {
            this(description, new String[]{extension});
        }

        public ExtensionFileFilter(String description, String extensions[]) {
            if (description == null) {
                this.description = extensions[0];
            } else {
                this.description = description;
            }
            this.extensions = (String[]) extensions.clone();
            toLower(this.extensions);
        }

        private void toLower(String array[]) {
            for (int i = 0, n = array.length; i < n; i++) {
                array[i] = array[i].toLowerCase();
            }
        }

        public String getDescription() {
            return description;
        }

        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            } else {
                String path = file.getAbsolutePath().toLowerCase();
                for (int i = 0, n = extensions.length; i < n; i++) {
                    String extension = extensions[i];
                    if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    
    public String fileNameToOpen() {
        return fileNameToOpenAct(null,null,null);
    }
    
    public String fileNameToOpen(String filterDesc, String[] filterTypeArray, Class<?> classSetting) {
        return fileNameToOpenAct(filterDesc , filterTypeArray , classSetting);
    }
    
    private String fileNameToOpenAct(String filterDesc, String[] filterTypeArray , Class<?> classSetting) {
        String path=null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to Open");
        if (filterTypeArray != null ){
        javax.swing.filechooser.FileFilter filter1 = new ExtensionFileFilter(filterDesc, filterTypeArray);
        fileChooser.setFileFilter(filter1);
        }
        if ( classSetting != null)
        {
        Preferences prf = Preferences.userNodeForPackage(classSetting);
         path = prf.get("LastDir", "");
        }
        if(path !=null) fileChooser.setCurrentDirectory(new File(path));
        
        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            return (fileToSave.getAbsolutePath());
        }
        return null;
    }
    
    public static String fileNameToSave(Class<?> classSetting)
    {
        return  fileNameToSaveAct(classSetting);
    }

        
    public static String fileNameToSave()
    {
        return  fileNameToSaveAct(null);
    }

    
    private static String fileNameToSaveAct(Class<?> classSetting) {
       String path=null;
        JFileChooser fileChooser = new JFileChooser() {
            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        if ( classSetting != null)
        {
        Preferences prf = Preferences.userNodeForPackage(classSetting);
         path = prf.get("LastDir", "");
        }
        fileChooser.setDialogTitle("Specify a file to save");
        if(path !=null) fileChooser.setCurrentDirectory(new File(path));
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            return (fileToSave.getAbsolutePath());
        }
        return null;
    }

    public static String readStringFromFile(String fname_path) throws Exception {
        BufferedReader br = new BufferedReader(new java.io.FileReader(fname_path));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(lineSeperator);
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }


       public void genTabSepfilefrmQuery(String sql, String fname_path) throws Exception {
        int row_count = 0;
        PrintWriter writer = new PrintWriter(fname_path, "UTF-8");
        java.sql.PreparedStatement STMT;
        try {
            JdbcPersistent jp = new JdbcPersistent(uid, pwd, jdbcstr);
            Vector<Object> columnNames = new Vector<Object>();

            STMT = jp.conn.prepareStatement(sql);
            jp.rs = STMT.executeQuery();
            ResultSetMetaData md = jp.rs.getMetaData();
            int columns = md.getColumnCount();
            //  Get column names
            // Added to have row numbers shown
            for (int i = 1; i <= columns; i++) {
                columnNames.addElement(md.getColumnName(i));

            }

            for (int i = 1; i <= columns; i++) {
                String sep = (i == columns) ? "" : ",";
                writer.print(md.getColumnName(i) + sep);
            }
            writer.println();
            while (jp.rs.next()) {
                StringBuffer csvline = new StringBuffer();
                for (int i = 1; i <= columns; i++) {
                    String sep = (i == columns) ? "" : "\t";
                    try {
                        if (md.getColumnTypeName(i).contains("NUMBER")) {
                            csvline.append(jp.rs.getString(i) + sep);
                        } else if (md.getColumnTypeName(i).equals("DATE")) {
                            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String tmpdate = f.format(jp.rs.getTimestamp(i));
                            csvline.append(tmpdate + sep);
                        } else {
                            if (jp.rs.getObject(i) == null) {
                                csvline.append(sep);
                            } else {
                                String tmpstring = (jp.rs.getString(i).contains("\t") || jp.rs.getString(i).contains("\n")) ? "\"" + jp.rs.getString(i).replace('"', '\'') + "\"" : jp.rs.getString(i);
                                csvline.append(tmpstring + sep);
                            }
                        }
                    } catch (NullPointerException e) {
                        csvline.append(sep);
                    }
                }
                writer.println(csvline);
            }
            writer.close();

        } catch (SQLException e) {
            throw e;
        }
        // return CSV;
    }

   public void genXLSfilefrmQuery(String sql,  SXSSFWorkbook wworkbook   , Oracle_xls_extract OCX , boolean include_header) throws Exception {
        int row_count = OCX.rowcounter % 1000000;
       SXSSFSheet wsheet ;
     //   PrintWriter writer = new PrintWriter(fname_path, "UTF-8");
        if(wworkbook.getNumberOfSheets()> 0)
        {
              wsheet=wworkbook.getSheetAt(wworkbook.getActiveSheetIndex());
        }      
        else 
        {
             wsheet = wworkbook.createSheet( "1");
        }
        

        java.sql.PreparedStatement STMT;
        try {
            JdbcPersistent jp = new JdbcPersistent(uid, pwd, jdbcstr);
            Vector<Object> columnNames = new Vector<Object>();

            STMT = jp.conn.prepareStatement(sql);
            jp.rs = STMT.executeQuery();
            ResultSetMetaData md = jp.rs.getMetaData();
            int columns = md.getColumnCount();
           CreationHelper createHelper = wworkbook.getCreationHelper();
             CellStyle datecellStyle = wworkbook.createCellStyle();
             datecellStyle.setDataFormat( createHelper.createDataFormat().getFormat("dd-MMM-yyyy hh:mm"));
           CellStyle headerstyle = wworkbook.createCellStyle();
Font font = wworkbook.createFont();
font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
font.setFontHeightInPoints((short)10);
font.setBold(true);
headerstyle.setFont(font);
             
           //  DateFormat customDateFormat = new DateFormat ("dd-MMM-yyyy hh:mm:ss"); 
              
           //  WritableCellFormat dateFormat = new WritableCellFormat (customDateFormat); 
            //  Get column names
            // Added to have row numbers shown
             
              SXSSFRow hdrrow =null ;
              
            for (int i = 1; i <= columns; i++) {
                columnNames.addElement(md.getColumnName(i));
            }

            for (int i = 1; (i <= columns && include_header)  ; i++) {
                if(i==1 && include_header) hdrrow  = wsheet.createRow(row_count);
                SXSSFCell cell = hdrrow.createCell(i-1);
               cell.setCellValue(createHelper.createRichTextString (md.getColumnName(i)));
                cell.setCellStyle(headerstyle);
                //Label label = new Label(i-1, 0 ,md.getColumnName(i) );
                //label.setCellFormat(  new WritableCellFormat( new WritableFont(WritableFont.ARIAL, 10 ,  WritableFont.BOLD ) ));
                // wsheet.addCell(label);
                // CellView cell= wsheet.getColumnView(i-1);
           //     if (md.getColumnTypeName(i).contains("NUMBER")) {/* cell.setSize(15 * 256*) */   ;                
            //    }
             //   else if ( md.getColumnTypeName(i).equals("DATE")) { 
             //       cell.setSize(21 * 256 );
              //  }
              //  else cell.setSize(25 * 256); 
             //   {
             //    wsheet.setColumnView(i-1, cell);
             //   }
            }
         //  writer.println();
            
            while (jp.rs.next()) {
                 OCX.rowcounter++;
                 row_count++;
                 SXSSFRow datarow = wsheet.createRow(row_count);
                
                for (int i = 1; i <= columns; i++) {
                    String sep = (i == columns) ? "" : ",";
                    try {
                        if (md.getColumnTypeName(i).contains("NUMBER")) {
                            //Number number = new Number(i-1 ,row_count, jp.rs.getDouble(i));
                            //wsheet.addCell(number);
                            datarow.createCell(i-1).setCellValue(jp.rs.getDouble(i));
                            
                        } else if (md.getColumnTypeName(i).equals("DATE")) {
                         

                         //   DateTime dateCell = new DateTime(i-1,  row_count , jp.rs.getTimestamp(i), dateFormat);
                             SXSSFCell cell = datarow.createCell(i-1);
                             cell.setCellStyle(datecellStyle);
                             cell.setCellValue(jp.rs.getTimestamp(i));
                            
                        }else if (md.getColumnTypeName(i).equals("LONG"))
                        {
                            SXSSFCell cell = datarow.createCell(i-1);
                            InputStream is =  jp.rs.getAsciiStream(i);
                            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                            int nRead;
                            byte[] data = new byte[1024];
                            while ((nRead = is.read(data, 0, data.length)) != -1) {
                                buffer.write(data, 0, nRead);
                            }

                            buffer.flush();
                            byte[] byteArray = buffer.toByteArray();

                            String text = new String(byteArray, StandardCharsets.UTF_8);

                            cell.setCellValue(createHelper.createRichTextString (text));
                        }
                        else {
                            if (jp.rs.getObject(i) == null) {
                            datarow.createCell(i-1);
                             //Label label = new Label(i-1,row_count , null );
                             //wsheet.addCell(label);
                            } else {
                              //  String tmpstring = (jp.rs.getString(i).contains(",") || jp.rs.getString(i).contains("\n")) ? "\"" + jp.rs.getString(i).replace('"', '\'') + "\"" : jp.rs.getString(i);
                               // csvline.append(tmpstring + sep);
                           //  Label label = new Label(i-1,row_count , jp.rs.getString(i) );
                            // wsheet.addCell(label);
                                 SXSSFCell cell = datarow.createCell(i-1);
                                 cell.setCellValue(createHelper.createRichTextString (jp.rs.getString(i)));
                            }
                        }
                    } catch (NullPointerException e) {
                       // Label label = new Label(i-1, row_count , null );
                        // wsheet.addCell(label);
                        datarow.createCell(i-1);
                        }
                }
              //  writer.println(csvline);
               
                if (OCX.rowcounter % 1000000 ==0 )
                {
              //    wsheet = wworkbook.createSheet( String.valueOf( wworkbook.getNumberOfSheets() + 1) , wworkbook.getNumberOfSheets() - 1);
                    wsheet =  wworkbook.createSheet( String.valueOf( wworkbook.getNumberOfSheets() + 1));
                    row_count = OCX.rowcounter % 1000000; 
                }
            }
      
        } catch (SQLException e) {
            throw e;
        }
        // return XLS;
    }

        public void genXLSfilefrmQuery(String sql, String fname_path  ) throws Exception {
        int row_count = 0;
     //   PrintWriter writer = new PrintWriter(fname_path, "UTF-8");
        SXSSFWorkbook wb = new SXSSFWorkbook(100);
        genXLSfilefrmQuery( sql,   wb   , new Oracle_xls_extract(null) , true);

       
        FileOutputStream fileOut = new FileOutputStream(fname_path);
        wb.write(fileOut);
        fileOut.close();
        wb.dispose();

        // return CSV;
    }

    
    public void genCSVfilefrmQuery(String sql, String fname_path) throws Exception {
        int row_count = 0;
        PrintWriter writer = new PrintWriter(fname_path, "UTF-8");
        java.sql.PreparedStatement STMT;
        try {
            JdbcPersistent jp = new JdbcPersistent(uid, pwd, jdbcstr);
            Vector<Object> columnNames = new Vector<Object>();

            STMT = jp.conn.prepareStatement(sql);
            jp.rs = STMT.executeQuery();
            ResultSetMetaData md = jp.rs.getMetaData();
            int columns = md.getColumnCount();
            //  Get column names
            // Added to have row numbers shown
            for (int i = 1; i <= columns; i++) {
                columnNames.addElement(md.getColumnName(i));

            }

            for (int i = 1; i <= columns; i++) {
                String sep = (i == columns) ? "" : ",";
                writer.print(md.getColumnName(i) + sep);
            }
            writer.println();
            while (jp.rs.next()) {
                StringBuffer csvline = new StringBuffer();
                for (int i = 1; i <= columns; i++) {
                    String sep = (i == columns) ? "" : ",";
                    try {
                        if (md.getColumnTypeName(i).contains("NUMBER")) {
                            csvline.append(jp.rs.getString(i) + sep);
                        } else if (md.getColumnTypeName(i).equals("DATE")) {
                            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String tmpdate = f.format(jp.rs.getTimestamp(i));
                            csvline.append(tmpdate + sep);
                        } else {
                            if (jp.rs.getObject(i) == null) {
                                csvline.append(sep);
                            } else {
                                String tmpstring = (jp.rs.getString(i).contains(",") || jp.rs.getString(i).contains("\n")) ? "\"" + jp.rs.getString(i).replace('"', '\'') + "\"" : jp.rs.getString(i);
                                csvline.append(tmpstring + sep);
                            }
                        }
                    } catch (NullPointerException e) {
                        csvline.append(sep);
                    }
                }
                writer.println(csvline);
            }
            writer.close();

        } catch (SQLException e) {
            throw e;
        }
        // return CSV;
    }

        public void genCSVfilefrmQuery(String sql,  PrintWriter writer , boolean include_header) throws Exception {
        int row_count = 0;
     //   PrintWriter writer = new PrintWriter(fname_path, "UTF-8");
        java.sql.PreparedStatement STMT;
        try {
            JdbcPersistent jp = new JdbcPersistent(uid, pwd, jdbcstr);
            Vector<Object> columnNames = new Vector<Object>();

            STMT = jp.conn.prepareStatement(sql);
            jp.rs = STMT.executeQuery();
            ResultSetMetaData md = jp.rs.getMetaData();
            int columns = md.getColumnCount();
            //  Get column names
            // Added to have row numbers shown
            for (int i = 1; (i <= columns ) ; i++) {
                columnNames.addElement(md.getColumnName(i));

            }

            for (int i = 1; ( i <= columns && include_header ); i++) {
                String sep = (i == columns) ? "" : ",";
                writer.print(md.getColumnName(i) + sep);
            }
            writer.println();
            while (jp.rs.next()) {
                StringBuffer csvline = new StringBuffer();
                for (int i = 1; i <= columns; i++) {
                    String sep = (i == columns) ? "" : ",";
                    try {
                        if (md.getColumnTypeName(i).contains("NUMBER")) {
                            csvline.append(jp.rs.getString(i) + sep);
                        } else if (md.getColumnTypeName(i).equals("DATE")) {
                            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String tmpdate = f.format(jp.rs.getTimestamp(i));
                            csvline.append(tmpdate + sep);
                        } else {
                            if (jp.rs.getObject(i) == null) {
                                csvline.append(sep);
                            } else {
                                String tmpstring = (jp.rs.getString(i).contains(",") || jp.rs.getString(i).contains("\n")) ? "\"" + jp.rs.getString(i).replace('"', '\'') + "\"" : jp.rs.getString(i);
                                csvline.append(tmpstring + sep);
                            }
                        }
                    } catch (NullPointerException e) {
                        csvline.append(sep);
                    }
                }
                writer.println(csvline);
            }
       //     writer.close();

        } catch (SQLException e) {
            throw e;
        }
        // return CSV;
    }

    
    public StringBuffer genCSVfrmQuery(String sql) throws Exception {
        int row_count = 0;
        StringBuffer CSV = new StringBuffer();
        java.sql.PreparedStatement STMT;
        try {
            JdbcPersistent jp = new JdbcPersistent(uid, pwd, jdbcstr);
            Vector<Object> columnNames = new Vector<Object>();

            STMT = jp.conn.prepareStatement(sql);
            jp.rs = STMT.executeQuery();
            ResultSetMetaData md = jp.rs.getMetaData();
            int columns = md.getColumnCount();
            //  Get column names
            // Added to have row numbers shown
            for (int i = 1; i <= columns; i++) {
                columnNames.addElement(md.getColumnName(i));
            }

            for (int i = 1; i <= columns; i++) {
                String sep = (i == columns) ? "" : ",";
                CSV.append(md.getColumnName(i) + sep);
            }
            CSV.append(lineSeperator);
            while (jp.rs.next()) {
                StringBuffer csvline = new StringBuffer();
                for (int i = 1; i <= columns; i++) {
                    String sep = (i == columns) ? "" : ",";
                    try {
                        if (md.getColumnTypeName(i).contains("NUMBER")) {
                            csvline.append(jp.rs.getString(i) + sep);
                        } else if (md.getColumnTypeName(i).equals("DATE")) {
                            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String tmpdate = f.format(jp.rs.getTimestamp(i));
                            csvline.append(tmpdate + sep);
                        } else {
                            if (jp.rs.getObject(i) == null) {
                                csvline.append(sep);
                            } else {
                                String tmpstring = (jp.rs.getString(i).contains(",") || jp.rs.getString(i).contains("\n")) ? "\"" + jp.rs.getString(i).replace('"', '\'') + "\"" : jp.rs.getString(i);
                                csvline.append(tmpstring + sep);
                            }
                        }
                    } catch (NullPointerException e) {
                        csvline.append(sep);
                    }
                }
                csvline.append(lineSeperator);
                CSV.append(csvline);
            }

        } catch (SQLException e) {
            throw e;
        }
        return CSV;
    }

    public void TableFromDatabaseSingleRecordView(JTable tbl, String sql) throws SQLException {
        Vector<Object> columnNames = new Vector<Object>();
        Vector<Object> data = new Vector<Object>();
        int row_count = 0;
        java.sql.PreparedStatement STMT;
        try {
            STMT = this.conn.prepareStatement(sql);
            //STMT.setMaxRows(10);
            this.rs = STMT.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            columnNames.addElement("Attribute");
            columnNames.addElement("Value");
            if (rs.next()) {
                for (int i = 1; i <= columns; i++) {
                    Vector<Object> row = new Vector<Object>(2);
                    row.add(md.getColumnName(i));
                    try {
                        if (md.getColumnTypeName(i).equals("DATE")) {
                            row.addElement(rs.getTimestamp(i));
                        } else {
                            row.addElement(rs.getString(i));
                        }
                    } catch (NullPointerException e) {
                        row.addElement(null);
                    }
                    data.add(row);
                }

            }
        } catch (SQLException e) {
            throw e;
        }

        final DefaultTableModel model;
        model = new DefaultTableModel((Vector) data, columnNames) {
            @Override
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
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

        tbl.setModel(model);
        this.resize_tbl_cols(tbl);
    }

    private void TableFromDatabaseAct(JTable tbl, String sql, boolean fetchall ,ArrayList<String> bindValue ,ArrayList<String> bindName ) throws SQLException {
        Vector<Object> columnNames = new Vector<Object>();
        Vector<Object> data = new Vector<Object>();
        int row_count = 0;
        //java.sql.PreparedStatement STMT;
       OraclePreparedStatement STMT;
        try {
            
            if (bindValue !=null)
            {   
                
                STMT = (OraclePreparedStatement) this.conn.prepareStatement(sql); 
                for (int i=0; i < bindValue.size(); i++)
                {   
                    STMT.setStringAtName( bindName.get(i).replace(":", ""),bindValue.get(i));
                 }
            }else
            {
                STMT = (OraclePreparedStatement) this.conn.prepareStatement(sql); 
            }
            
            this.rs = STMT.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();

            //  Get column names
            columnNames.addElement("#"); // Added to have row numbers shown
            for (int i = 1; i <= columns; i++) {
                columnNames.addElement(md.getColumnName(i));
            }

            //  Get row data
            while (rs.next()) {
                Vector<Object> row = new Vector<Object>(columns);

                for (int i = 0; i <= columns; i++) {
                    if (i == 0) {
                        row.addElement(rs.getRow());
                    } else {
                        try {
                            if (md.getColumnTypeName(i).equals("DATE")) {
                                row.addElement(rs.getTimestamp(i));
                            } else {
                                row.addElement(rs.getString(i));
                            }
                        } catch (NullPointerException e) {
                            row.addElement(null);
                        }
                    }
                }

                data.addElement(row);
                row_count++;
                if (row_count >= Ora_constants.fetchSize && !fetchall) {
                    break;
                }
            }

        } catch (SQLException e) {
            throw e;
        }

        final DefaultTableModel model;
        model = new DefaultTableModel((Vector)data, columnNames) {
            @Override
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
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

        tbl.setModel(model);
        this.resize_tbl_cols(tbl);
    }

    public void execute_stmt(String stmt) throws Exception {
        CallableStatement cstmt = this.conn.prepareCall(stmt);
        cstmt.execute();
        cstmt.close();
    }
    
      public String execute_stmt(String stmt , Boolean DML)  {
         CallableStatement cstmt;
         int cnt = 0;
          try {
           cstmt = this.conn.prepareCall(stmt);
            cstmt.executeUpdate();
           
           if (DML)
           {
               cnt =  cstmt.getUpdateCount();
                cstmt.close();  
               return "Success Rows Affected : " +  cnt;
                 
           }
           else
           {
                cstmt.close();
               return "Success";
           }
           
           
        } catch (SQLException ex) {
           return ex.toString();
        }
       
    }


    public void reconnect() throws Exception {
        try {
            connect_db();
        } catch (Exception er) {
            String exption = er.toString();
            throw er;
        } finally {
            try {
                ;//  if (conn != null) conn.close();
            } catch (Exception ignored) {
                String exption = ignored.toString();
                System.out.print(exption);
            }
        }

    }

    /**
     *
     * @param uid
     * @param pwd
     * @param jdbcstr
     * @throws Exception
     */
    private void connect_db() throws SQLException, ClassNotFoundException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("password", pwd);
        props.setProperty("user", uid);
        props.put("v$session.osuser", uid);
        machine_name = jdbcstr.substring(jdbcstr.indexOf("@") + 1, jdbcstr.indexOf("."));
        props.put("v$session.machine", machine_name);
        props.put("v$session.program", "SQL*Plus");
        this.conn = DriverManager.getConnection(jdbcstr, props);
        this.conn.setAutoCommit(false);
        this.conn.prepareStatement("alter session set nls_date_format = 'DD-MON-YYYY'").execute();
    }

    public ResultSet runqry(String qry) throws SQLException {
        String query = qry;
        //this.rs = stmt.executeQuery(query);
        return this.conn.prepareStatement(qry).executeQuery();
        //return this.rs;
    }

    public String returnSingleField(String qry) {
        try {
            String query = qry;
            //this.rs = stmt.executeQuery(query);
            ResultSet RS = this.conn.prepareStatement(qry).executeQuery();
            if (RS.next()) {
                return RS.getString(1);
            } else {
                return null;
            }
        } catch (SQLException ex) {
            return null;
        }
    }

        public String returnSingleField(String qry, boolean return_all_rows , String linesep ) {
            if (return_all_rows==false)
            {
                return returnSingleField(qry);
            }
            else
            {
        try {
            String query = qry;
            StringBuffer result = new StringBuffer();
            //this.rs = stmt.executeQuery(query);
            ResultSet RS = this.conn.prepareStatement(qry).executeQuery();
            while (RS.next()) {
                 result.append(RS.getString(1) + linesep );
            } 
            return result.toString();
        } catch (SQLException ex) {
            return null;
        }
            }
    }
    
    /**
     *
     * @param qry
     * @param JB
     * @throws SQLException
     */
    public void runqry(String qry, javax.swing.JComboBox JB) throws SQLException {
        String query = qry;
        //this.rs = stmt.executeQuery(query);
        ResultSet RS = this.conn.prepareStatement(qry).executeQuery();
        JB.removeAllItems();
        while (RS.next()) {
            JB.addItem(RS.getString(1));
        }
    }

    public StringBuffer returnHTMLTable(String qry) throws SQLException {
        String query = qry;

        StringBuffer htmltbl = new StringBuffer();
        int rowCount = 0;
        try {
            rs = this.conn.prepareStatement(qry).executeQuery();
            htmltbl.append(" <div> <table id=\"appdata\">");
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            // table header
            htmltbl.append("<thead>");
            htmltbl.append("<tr>");
            for (int i = 0; i < columnCount; i++) {
                htmltbl.append("<th>" + rsmd.getColumnLabel(i + 1) + "</th>");
            }
            htmltbl.append("</tr>");
            htmltbl.append("</thead>");
            htmltbl.append("<tbody>");
            // the data
            while (rs.next()) {
                rowCount++;
                htmltbl.append("<tr>");
                for (int i = 0; i < columnCount; i++) {
                    htmltbl.append("<td>" + rs.getString(i + 1) + "</td>");
                }
                htmltbl.append("</tr>");
            }
            htmltbl.append("</tbody> </table></div>");

        } catch (SQLException ex) {
            Logger.getLogger(JdbcPersistent.class.getName()).log(Level.SEVERE, null, ex);
            htmltbl.append("");
        }
        return htmltbl;
    }

    public StringBuffer returnOptionList(String qry) throws SQLException {
        String query = qry;

        StringBuffer htmltbl = new StringBuffer();
        int rowCount = 0;
        try {
            rs = this.conn.prepareStatement(qry).executeQuery();
            while (rs.next()) {
                rowCount++;
                htmltbl.append("<option value=\"");
                htmltbl.append(rs.getString(1) + "\">");
                htmltbl.append(rs.getString(2) + "</option>");
            }

        } catch (SQLException ex) {
            Logger.getLogger(JdbcPersistent.class.getName()).log(Level.SEVERE, null, ex);
            htmltbl.append("");
        }
        return htmltbl;
    }

    public StringBuffer returnParamsTable(String rdfname) throws SQLException {
        String query = "select order_seq# seq#, PROMPT_TEXT Prompt, PROMPT_TYPE Type, REQUIRED Req, DEFAULT_VALUE Enter_value, upper(PROMPT_VARIABLE_NAME) PROMPT_VARIABLE_NAME   from rr_prompts where report_module='"
                + rdfname + "' order by order_seq#";

        StringBuffer htmltbl = new StringBuffer();
        int rowCount = 0;
        try {
            rs = this.conn.prepareStatement(query).executeQuery();
            htmltbl.append(" <div> <table id=\"appdata\">");
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            // table header
            htmltbl.append("<thead>");
            htmltbl.append("<tr>");
            for (int i = 0; i < columnCount; i++) {
                if (!rsmd.getColumnLabel(i + 1).equals("PROMPT_VARIABLE_NAME")) {
                    htmltbl.append("<th>" + rsmd.getColumnLabel(i + 1) + "</th>");
                }
            }
            htmltbl.append("</tr>");
            htmltbl.append("</thead>");
            htmltbl.append("<tbody>");
            // the data
            while (rs.next()) {
                rowCount++;
                htmltbl.append("<tr>");
                for (int i = 0; i < columnCount - 1; i++) {
                    if (i == 4) {
                        htmltbl.append("<td>" + "<input type=\"text\" name=" + rs.getString(i + 2) + ">" + "</td>");
                    } else {
                        htmltbl.append("<td>" + rs.getString(i + 1) + "</td>");
                    }
                }
                htmltbl.append("</tr>");
            }
            htmltbl.append("</tbody> </table></div>");

        } catch (SQLException ex) {
            Logger.getLogger(JdbcPersistent.class.getName()).log(Level.SEVERE, null, ex);
            htmltbl.append("");
        }
        return htmltbl;
    }

}
