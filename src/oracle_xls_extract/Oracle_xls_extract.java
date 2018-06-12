/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
USAGE :   /usr/java6/jre/bin/java -jar $HOME/oracle_xls_extract.jar -u sqloper4 -p xx  -j jdbc:oracle:thin:@arlioradbs1.arli.cummins.com:1521:prd1  -q "select * from bms_preferences"
   /usr/java6/jre/bin/java -jar $HOME/oracle_xls_extract.jar -u sqloper4 -p xx  -q "select * from bms_preferences"
 */
package oracle_xls_extract;

import org.apache.commons.cli.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hv655
 */
public class Oracle_xls_extract {

    private String[] arguments;
    private String QRY;
    private String fileFormat;
    private String userName;
    private String password;
    private JdbcPersistent jcon;
    private Ora_constants OC;
    public int rowcounter = 0;
    private Options options = new Options();
    private String fname_path;
    private boolean execute_xls_update;
    private String inputfile = null;
    private String col = null;
    private int sheet = 1;
    private String jdbcstr;
    private int SQLTYPE = UpdateFromXlsx.DML;
    private String textFilesPath;

    public Oracle_xls_extract(String[] args) {

        arguments = args;
        parse_args();
        if (this.execute_xls_update) {
            try {
                UpdateFromXlsx ufx = new UpdateFromXlsx(this.userName, this.password, this.jdbcstr, this.inputfile, this.col, this.sheet, SQLTYPE);
            } catch (Exception ex) {
                Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }

        if (fileFormat.equals("XLS")) {
            create_xls(this.userName, this.password);

        } else if (fileFormat.equals("CSV")) {
            try {
                create_csv();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private String readQueryFromFile(String fpath) {
        StringBuffer queryText = new StringBuffer();
        BufferedReader br = null;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(fpath));
            while ((sCurrentLine = br.readLine()) != null) {
                queryText.append(sCurrentLine);
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
        return queryText.toString();
    }

    private void checkAndExecuteReadFlatTextIntoXls(CommandLine cmd) {
        if (cmd.hasOption("read_flat_txt_into_xls")) {
            if (cmd.hasOption("r")) {
                inputfile = cmd.getOptionValue("r");
            } else {
                System.out.println("Input filename not present");
                System.exit(1);
            }
            if (cmd.hasOption("col")) {
                col = cmd.getOptionValue("col");
            } else {
                System.out.println("Column Name to read not present");
                System.exit(1);;
            }
            if (cmd.hasOption("txtfilespath")) {
                this.textFilesPath = cmd.getOptionValue("txtfilespath");
            } else {
                System.out.println("Text files path not present");
                System.exit(1);;
            }

                if (cmd.hasOption("sheet")) {
                    try {
                        String sheetNo = cmd.getOptionValue("sheet");
                        this.sheet = Integer.parseInt(sheetNo);
                    } catch (Exception e) {
                        Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, e.getMessage());
                        System.exit(1);
                    }
                }else
                {
                    this.sheet=1;
                }
                
                ReadFlatTextFromFilesIntoXls rx = new ReadFlatTextFromFilesIntoXls(this.inputfile, this.textFilesPath , this.col, this.sheet);
                System.exit(0);


        }

    }

    private void parse_args() {
        options.addOption("h", "help", false, "show help.");
        options.addOption("u", "username", true, "Database UserName");
        options.addOption("q", "query", true, "SQL Query");
        options.addOption("qfile", "query read from file - Give full path", true, "SQL Query File");
        options.addOption("p", "password", true, "Database Password");
        options.addOption("o", "orausrpass", true, "Database usermname/password with slash in oracle format .. Use either (u and p )  or o");
        options.addOption("j", "jdbcUrl", true, "Database JDCB URL EX jdbc:oracle:thin:@arlioradbs1.arli.cummins.com:1521:prd1");
        options.addOption("f", "jdbcfile", true, "File path containing jdbc urls file format should be \n  bmsd_dev1 jdbc:oracle:thin:@ftdcsoradbs01.ftdc.cummins.com:1521:BMSD1");
        options.addOption("t", "filetype", true, "File Format / Type (CSV ,XLS)  Default : XLS");
        options.addOption("r", "result", true, "File Result output OR File Input name path in case of update or reads based on excel");
        options.addOption("col", "col", true, "if updfrmxl then col option would be the excel column where update stamtemnets are");
        options.addOption("sheet", "sheet", true, "Starts with 1 , if updfrmxl present then sheetnumber that should be read from where update statments are preset");
        options.addOption("SQLTYPE", "SQLTYPE", true, "DML/SQL");
        
             
        options.addOption("updfrmxl", "updfrmxl", false, "Update Oracle table from excel column , Full update stament must be present in column");
        
        
        options.addOption("read_flat_txt_into_xls", "read_flat_txt_into_xls", false, "Read flat text files based on column in excel file ");
        options.addOption("txtfilespath" ,"Path to flat text files", true, "Text Files Path "  );


        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        if (arguments.length == 0) {
            help();
            System.exit(0);
        }

        try {
            cmd = parser.parse(options, this.arguments);
            if (cmd.hasOption("h")) {
                help();
                System.exit(0);
            }

            checkAndExecuteReadFlatTextIntoXls(cmd);
            if (cmd.hasOption("updfrmxl")) {

                if (cmd.hasOption("r")) {
                    inputfile = cmd.getOptionValue("r");
                } else {
                    System.out.println("Input filename not present");
                    return;
                }

                if (cmd.hasOption("col")) {
                    col = cmd.getOptionValue("col");
                } else {
                    System.out.println("Column Name to read not present");
                    return;
                }

                if (cmd.hasOption("sheet")) {
                    try {
                        String sheetNo = cmd.getOptionValue("sheet");
                        this.sheet = Integer.parseInt(sheetNo);
                    } catch (Exception e) {
                        Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, e.getMessage());
                    }
                }
                this.execute_xls_update = true;
                if (cmd.hasOption("j")) {
                    this.jdbcstr = cmd.getOptionValue("j");
                }
                if (cmd.hasOption("SQLTYPE")) {
                    this.SQLTYPE = (cmd.getOptionValue("SQLTYPE").equals("SQL")) ? UpdateFromXlsx.SQL : UpdateFromXlsx.DML;
                }
                // return;        

            }

            if (cmd.hasOption("t")) {
                this.fileFormat = cmd.getOptionValue("t");
                fname_path = new File(".").getCanonicalPath() + File.separator + "output." + cmd.getOptionValue("t");
            } else {
                this.fileFormat = "XLS";
                fname_path = new File(".").getCanonicalPath() + File.separator + "output.xlsx";
            }

            if (cmd.hasOption("r")) {
                this.fname_path = cmd.getOptionValue("r");
            }

            if (cmd.hasOption("q") || cmd.hasOption("qfile")) {
                if (cmd.hasOption("q")) {
                    this.QRY = cmd.getOptionValue("q");
                } else {
                    this.QRY = readQueryFromFile(cmd.getOptionValue("qfile"));
                }

            } else if (!cmd.hasOption("updfrmxl")) {
                Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, " No Query Specified .  Use -q query  ");
                System.exit(0);
            }

            if (cmd.hasOption("u") && cmd.hasOption("p")) {
                this.userName = cmd.getOptionValue("u");
                this.password = cmd.getOptionValue("p");
            } else if (cmd.hasOption("o")) {
                String[] temp = cmd.getOptionValue("o").split("/");
                if (temp.length == 2) {
                    this.userName = temp[0];
                    this.password = temp[1];
                }
            } else {
                Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, "Missing username or password ");
                System.exit(0);
            }

            if (cmd.hasOption("j")) {
                try {
                    OC = new Ora_constants("dummy", cmd.getOptionValue("j"));
                } catch (Exception ex) {
                    Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }

            } else if (cmd.hasOption("f")) {
                try {
                    this.OC = new Ora_constants(cmd.getOptionValue("f"));
                } catch (Exception ex) {
                    Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
            } else {
                try {
                    OC = new Ora_constants();
                } catch (Exception ex) {
                    Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
            }

        } catch (ParseException ex) {
            Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, null, ex);
            help();
        } catch (IOException ex) {
            Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    private static final Logger LOG = Logger.getLogger(Oracle_xls_extract.class.getName());

    private void help() {
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("Main", options);
        System.out.println("USAGE : "
                + "\n  /usr/java6/jre/bin/java -jar $HOME/oracle_xls_extract.jar -u sqloper4 -p xx  -j jdbc:oracle:thin:@arlioradbs1.arli.cummins.com:1521:prd1  -q \"select * from bms_preferences\"\n"
                + "   /usr/java6/jre/bin/java -jar $HOME/oracle_xls_extract.jar -u sqloper4 -p xx  -q \"select * from bms_preferences\"");
        System.exit(0);
    }

    private void create_csv() throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(fname_path, "UTF-8");
        for (int i = 0; i < OC.dbCount; i++) {
            try {
                this.jcon = new JdbcPersistent(this.userName, this.password, OC.jstrings[i].jdbcstring, OC.jstrings[i].jdbcstring);
                jcon.genCSVfilefrmQuery(this.QRY, writer, (i == 0 ? true : false));

            } catch (Exception ex) {
                Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, "Error connecting with " + OC.jstrings[i].jdbcstring + " " + ex.getMessage());
            }
        }
        writer.close();

    }

    private void create_xls(String uid, String pwd) {
        try {

            SXSSFWorkbook wb = new SXSSFWorkbook(100);
            // wworkbook = Workbook.createWorkbook(new File(fname_path));
            //WritableSheet wsheet = wworkbook.createSheet("Data", 0);

            for (int i = 0; i < OC.dbCount; i++) {
                try {
                    this.jcon = new JdbcPersistent(uid, pwd, OC.jstrings[i].jdbcstring, OC.jstrings[i].jdbcstring);
                    jcon.genXLSfilefrmQuery(this.QRY, wb, this, (i == 0 ? true : false));

                } catch (Exception ex) {
                    Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, "Error connecting with " + OC.jstrings[i].jdbcstring + " " + ex.getMessage());
                }
            }
            FileOutputStream fileOut = new FileOutputStream(fname_path);
            wb.write(fileOut);
            fileOut.flush();
            fileOut.close();
            wb.dispose();
            //wb.close();
            System.out.println("File Written : " + fname_path);

        } catch (IOException ex) {
            Logger.getLogger(Oracle_xls_extract.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Oracle_xls_extract OCX = new Oracle_xls_extract(args);
    }

}
