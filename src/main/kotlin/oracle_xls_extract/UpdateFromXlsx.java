/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oracle_xls_extract;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Boolean.TRUE;

/**
 *
 * @author hv655
 */
public class UpdateFromXlsx {

    private String fileFormat;
    private String userName;
    private String password;
    private JdbcPersistent jcon;
    private Ora_constants OC;
    private String jdbcstr;
    private String fname;

    public static final int SQL = 1;
    public static final int DML = 2;

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setJdbcstr(String jdbcstr) {
        this.jdbcstr = jdbcstr;
    }

    public UpdateFromXlsx(String userName, String password, String jdbcstr, String fname, String colToRead, int sheetNoToRead, int typeOfOperation)  {
        try {
            this.userName = userName;
            this.password = password;
            this.jdbcstr = jdbcstr;
            this.fname = fname;
            int sheeetno = sheetNoToRead - 1;
            
            this.jcon = new JdbcPersistent(this.userName, this.password, this.jdbcstr, this.jdbcstr);
            FileInputStream fis = new FileInputStream(new File(fname));
            XSSFWorkbook readW = new XSSFWorkbook(fis);
            XSSFSheet sheet = readW.getSheetAt(sheeetno);
            Iterator<Row> ite = sheet.rowIterator();
            while (ite.hasNext()) {
                
                try {
                    if (typeOfOperation == DML) {
                        Row row = ite.next();
                        XSSFCell cell = (XSSFCell) row.getCell(CellReference.convertColStringToIndex(colToRead));
                        XSSFCell cellToUpdate = (XSSFCell) row.createCell(row.getLastCellNum() + 1);
                        String qry = cell.getStringCellValue();
                        String result = jcon.execute_stmt(qry, TRUE);
                        cellToUpdate.setCellValue(result);
                    } else {
                        try{
                            Row row = ite.next();
                            XSSFCell cell = (XSSFCell) row.getCell(CellReference.convertColStringToIndex(colToRead));
                            
                            String qry = cell.getStringCellValue();
                            ResultSet rs = jcon.runqry(qry);
                            if (rs.next()) {
                                ResultSetMetaData md = rs.getMetaData();
                                int columns = md.getColumnCount();
                                for (int i = 1; i <= columns; i++) {
                                    XSSFCell cellToUpdate = (XSSFCell) row.createCell(row.getLastCellNum() + i);
                                    try {
                                        if (md.getColumnTypeName(i).equals("DATE")) {
                                            cellToUpdate.setCellValue(rs.getTimestamp(i));
                                        } else {
                                            cellToUpdate.setCellValue(rs.getString(i));
                                        }
                                    } catch (NullPointerException e) {
                                        cellToUpdate.setCellValue("");
                                    }
                                    
                                }

                            }
                        }catch(SQLException e)
                        {
                           Logger.getLogger(UpdateFromXlsx.class.getName()).log(Level.SEVERE, null, e); 
                        }
                        
                    }

                } catch (NullPointerException e) {
                    
                }

            }
            jcon.conn.commit();
            fis.close();
            FileOutputStream fos = new FileOutputStream(new File(fname.replace(".", "new.")));
            readW.write(fos);
            fos.close();
        } catch (SQLException ex) {
            Logger.getLogger(UpdateFromXlsx.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UpdateFromXlsx.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(UpdateFromXlsx.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
