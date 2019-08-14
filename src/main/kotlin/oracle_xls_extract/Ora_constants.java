/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oracle_xls_extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 *
 * @author hv655
 */


public  class Ora_constants {
 
       public BmsJdbcString[] jstrings;
       public int dbCount;
       public static int fetchSize = 100 ;
       public static int maxRows = 1000;
       private String fpath;
       
     
       
	public  void readfile() {
            int i = 0;
            String dbname;
            String jdbcstr;
            BufferedReader br = null;
            
 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(fpath));
 			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println(sCurrentLine);
                            dbname = sCurrentLine.substring(0, sCurrentLine.indexOf(" "));
                            jdbcstr = sCurrentLine.substring(sCurrentLine.indexOf(" ")+1);
                            this.jstrings[i] = new BmsJdbcString(dbname,jdbcstr);
                        i++;
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
          dbCount=i;
	}

    public class BmsJdbcString{
        public String jdbcstring;
        public String dbname;
        public BmsJdbcString(String dbname , String jdbcstring)
        {
            this.jdbcstring=jdbcstring;
            this.dbname=dbname;
        }
    }
    
    public class tns_record{
        public String dbname;
        public String sid;
        public String host_name;
        public String port_no;
        public tns_record(String dbname, String sid, String host_name, String port_no) {
            this.dbname = dbname;
            this.sid = sid;
            this.host_name = host_name;
            this.port_no = port_no;
        }      
    }
   
    

          public Ora_constants( String dbname , String jdbcurl)  {
           
               System.out.println("Begin");
               this.jstrings = new BmsJdbcString[200];
               jstrings[0] = new BmsJdbcString(dbname , jdbcurl);
               this.dbCount=1;

          }
      
    public Ora_constants()  {
           try {
               System.out.println("Begin");
               this.jstrings = new BmsJdbcString[200];
               
               this.fpath=  new File(".").getCanonicalPath() + File.separator  + "jdbclist.txt" ;
               File f = new File(fpath);
               if(f.exists() && !f.isDirectory())
               {
                   readfile();
               }
           } catch (IOException ex) {
              ;
           }
    }
    
        public Ora_constants(String fpath) throws Exception {
        System.out.println("Begin");
        this.jstrings = new BmsJdbcString[200];
         File f = new File(fpath);
         
         if(f.exists() && !f.isDirectory()) 
         {
             this.fpath=fpath;
             readfile();
         }else throw new Exception("File " + fpath + " not found");
    }
    
}
