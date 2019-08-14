/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oracle_xls_extract;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hv655
 */
public class ReadFlatTextFromFilesIntoXls {

    //private String fileName;
    private String filePath;
    private String columnToRead;
    private int sheetToRead;
    private String xlsToRead;
    private String xlsFilePath;

    /**
     *
     * @param xlsFilePath Excel File Path 
     * @param filePath    Path to text files
     * @param columnToRead Column containing text file names
     * @param sheetToRead  Sheet to read from xlsx
     */
    public  ReadFlatTextFromFilesIntoXls( String xlsFilePath , String filePath, String columnToRead, int sheetToRead ) {
        try {
            this.filePath = filePath;
            this.columnToRead = columnToRead ;
            this.sheetToRead = sheetToRead  - 1 ;
            this.xlsFilePath = xlsFilePath;
            readFiles();
        } catch (InvalidFormatException ex) {
            Logger.getLogger(ReadFlatTextFromFilesIntoXls.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readFiles() throws InvalidFormatException {
       
         OPCPackage fis = OPCPackage.open(new File(this.xlsFilePath) );
        try {
            //fis = new  OPCPackage.new File(this.xlsFilePath)); // FileInputStream(new File(this.xlsFilePath));
            XSSFWorkbook readW = new XSSFWorkbook(fis);
            XSSFSheet sheet = readW.getSheetAt(this.sheetToRead);
            Iterator<Row> ite = sheet.rowIterator();
            while (ite.hasNext()) {
                try {
                    Row row = ite.next();
                    XSSFCell cell = (XSSFCell) row.getCell(CellReference.convertColStringToIndex(this.columnToRead));
                    String txtFileName = cell.getStringCellValue();
                    XSSFCell cellToUpdate = (XSSFCell) row.createCell(row.getLastCellNum() + 1);
                    String fileContent = readFromFlatFile.readFile(this.filePath + File.separator + txtFileName);
                    cellToUpdate.setCellValue(fileContent);  
                }catch (Exception ex)
                {
                     Logger.getLogger(ReadFlatTextFromFilesIntoXls.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                    
            FileOutputStream fos = new FileOutputStream(new File(this.xlsFilePath.replace(".", "new.")));
            readW.write(fos);
            fos.close();
            fis.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReadFlatTextFromFilesIntoXls.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReadFlatTextFromFilesIntoXls.class.getName()).log(Level.SEVERE, null, ex);
        } 

    }//end of readfiles

}
