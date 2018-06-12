/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runsql_anony;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author sqloper4
 */
public  class Bms_Constants {

       public BmsJdbcString[] jstrings;
       public static int fetchSize = 100 ;
       public static int maxRows = 1000;
       public static float matchScale = (float) 0.72;
       public static String project_icon = "runsql_anony" + File.separator + "db1-icon.png";
       private Vector<Object> jdbclst = new Vector<Object>();
       
       public Vector<String> get_db_names()
       {
           Vector<String> db_list = new Vector<String>();
           for(int i=0; i<jdbclst.size(); i++)
           {
           db_list.addElement(  ((tns_record)jdbclst.get(i)).dbname  );
           }
           return db_list;
       }
       
       public String get_jdbc_str(String db)
       {
           for(int i=0; i<jdbclst.size(); i++)
           {
               if (   ((tns_record)jdbclst.get(i)).dbname.equalsIgnoreCase(db)                             )
               {
                   String service_sid;
                   if(((tns_record)jdbclst.get(i)).sid !=null)
                   {
                       service_sid=":" + ((tns_record)jdbclst.get(i)).sid;
                   }else
                   {
                       service_sid="/" + ((tns_record)jdbclst.get(i)).serviceName;
                   }

                   return "jdbc:oracle:thin:@" + ((tns_record)jdbclst.get(i)).host_name + ":" + ((tns_record)jdbclst.get(i)).port_no + 
                          service_sid;
               }
           }
           return null;
       }
       
    private void readtns() {
        int i = 0;
        BufferedReader br = null;
        String tnsfile = System.getenv("TNS_ADMIN");
        if (tnsfile != null) {
            try {
                String sCurrentLine;
                StringBuffer tns_entry=new StringBuffer();
                StringBuffer  dbname = new StringBuffer();
                StringBuffer  sid = new StringBuffer();
                StringBuffer host_name = new StringBuffer();
                StringBuffer port_no = new StringBuffer();
                br = new BufferedReader(new FileReader(tnsfile + File.separator +"tnsnames.ora"));
                while ((sCurrentLine = br.readLine()) != null) {
                    tns_entry.append(sCurrentLine.replaceAll("\\s+","").toUpperCase());
                       try {
                        if (tns_entry.indexOf("(DESCRIPTION") > 0) {
                            int end = tns_entry.indexOf("(DESCRIPTION") - 1;
                            int beg = Math.max(tns_entry.lastIndexOf(")",end), tns_entry.lastIndexOf("#")) + 1;
                            dbname.append(tns_entry.substring(beg, end));
                            tns_entry.delete(end, end+13);
                        }
                        if ((tns_entry.indexOf("HOST=") > 0)) {
                            int beg = tns_entry.indexOf("HOST=") + 5;
                            int end = tns_entry.indexOf(")", beg);
                            host_name.append(tns_entry.substring(beg, end));
                            tns_entry.delete(beg-5, beg);
                        }
                        if ((tns_entry.indexOf("PORT=") > 0)) {
                            int beg = tns_entry.indexOf("PORT=") + 5;
                            int end = tns_entry.indexOf(")", beg);
                            port_no.append(tns_entry.substring(beg, end));
                            tns_entry.delete(beg -5 , beg);
                        }
                        
                        if (tns_entry.indexOf("SERVICE_NAME=") > 0 || tns_entry.indexOf("SID=") > 0) {
                            int beg = Math.max( tns_entry.indexOf("SID=") +4 ,  tns_entry.indexOf("SERVICE_NAME=") + 13);
                            int end = tns_entry.indexOf(")", beg);
                            sid.append(tns_entry.substring(beg, end));
                            tns_record tns_inst;
                            if ( tns_entry.indexOf("SERVICE_NAME=") > 0) {
                                 tns_inst =new tns_record(dbname.toString(), null, host_name.toString(), port_no.toString(),sid.toString());
                            }else
                            {
                                 tns_inst = new tns_record(dbname.toString(), sid.toString(), host_name.toString(), port_no.toString(),null);
                            }

                            jdbclst.add(tns_inst);
                          //System.out.println(dbname.toString() + " : " + sid.toString() + " : " + host_name.toString());
                            dbname.delete(0, dbname.length());
                            port_no.delete(0, port_no.length());
                            sid.delete(0, sid.length());
                            host_name.delete(0, host_name.length());
                            tns_entry.delete(0, tns_entry.length());
                        }
                        
                    } catch (StringIndexOutOfBoundsException e) {
                            dbname.delete(0, dbname.length());
                            port_no.delete(0, port_no.length());
                            sid.delete(0, sid.length());
                            host_name.delete(0, host_name.length());
                            tns_entry.delete(0, tns_entry.length());
                            tns_entry.append(")");
                    }
                    }
                

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
 
	public  void readfile() {
            int i = 0;
            String dbname;
            String jdbcstr;
            BufferedReader br = null;
            
 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader("src" + File.separator + "runsql_anony" + File.separator + "jdbclist.txt"));
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
        public String serviceName;
        public String host_name;
        public String port_no;
        public tns_record(String dbname, String sid, String host_name, String port_no ,String servieName) {
            this.dbname = dbname;
            this.sid = sid;
            this.host_name = host_name;
            this.port_no = port_no;
            this.serviceName=servieName;
        }      
    }
   

    public Bms_Constants() {
        System.out.println("Begin");
        this.readtns();
        this.jstrings = new BmsJdbcString[20];
         File f = new File("src" + File.separator + "runsql_anony" + File.separator + "jdbclist.txt");
         if(f.exists() && !f.isDirectory()) 
         {
             readfile();
         }
    }
}
