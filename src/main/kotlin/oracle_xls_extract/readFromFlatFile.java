/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oracle_xls_extract;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author hv655
 */
public class readFromFlatFile {
  
public  static String  readFile(String fileNameWithPath) throws FileNotFoundException, IOException
{
        BufferedReader br = new BufferedReader(new FileReader(fileNameWithPath));
try {
    StringBuilder sb = new StringBuilder();
    String line = br.readLine();

    while (line != null) {
        sb.append(line);
        sb.append(System.getProperty("line.separator"));
        line = br.readLine();
    }       
    return sb.toString();
} finally {
    br.close();
}

}

}
