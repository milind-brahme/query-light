package com.milind.querylight;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import oracle.jdbc.OraclePreparedStatement;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tool to run database scripts
 */
public class ScriptRunner {

    private static final String DEFAULT_DELIMITER = ";";
    private Connection connection;
    private boolean stopOnError;
    private boolean autoCommit;
    private PrintWriter logWriter = new PrintWriter(System.out);
    private PrintWriter errorLogWriter = new PrintWriter(System.err);
    private String delimiter = DEFAULT_DELIMITER;
    private boolean fullLineDelimiter = false;
    private static final String DELIMITER_LINE_REGEX = "(?i)DELIMITER.+";
    private static final String DELIMITER_LINE_SPLIT_REGEX = "(?i)DELIMITER";
    private StringBuffer log_output;

    /**
     * Default constructor
     */
    public ScriptRunner(Connection connection, boolean autoCommit,
            boolean stopOnError) {
        this.connection = connection;
        this.stopOnError = stopOnError;
        log_output = new StringBuffer();
    }

    public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
        this.delimiter = delimiter;
        this.fullLineDelimiter = fullLineDelimiter;
    }

    /**
     * Setter for logWriter property
     *
     * @param logWriter - the new value of the logWriter property
     */
    public void setLogWriter(PrintWriter logWriter) {
        this.logWriter = logWriter;
    }

    /**
     * Setter for errorLogWriter property
     *
     * @param errorLogWriter - the new value of the errorLogWriter property
     */
    public void setErrorLogWriter(PrintWriter errorLogWriter) {
        this.errorLogWriter = errorLogWriter;
    }

    /**
     * Runs an SQL script (read in using the Reader parameter)
     *
     * @param reader - the source of the script
     */
    /**
     * Runs an SQL script
     *
     * @param cmd
     * @param bindValue
     * @param bindName
     * @return
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public StringBuffer runScript(String cmd, ArrayList<String> bindValue, ArrayList<String> bindName) throws IOException, SQLException {
        //Statement statement;
        CallableStatement enable_stmt;
        CallableStatement show_stmt = connection.prepareCall(
                "declare "
                + "    l_line varchar2(32000); "
                + "    l_done number; "
                + "    l_buffer long; "
                + "begin "
                + "  loop "
                + "    exit when length(l_buffer)+255 > :maxbytes OR l_done = 1; "
                + "    dbms_output.get_line( l_line, l_done ); "
                + "    l_buffer := l_buffer || l_line || chr(10); "
                + "  end loop; "
                + " :done := l_done; "
                + " :buffer := l_buffer; "
                + "end;");
        try {
            OraclePreparedStatement statement;
            enable_stmt = connection.prepareCall("begin dbms_output.enable(1000000 ); end;");
            enable_stmt.executeUpdate();
            // statement = connection.createStatement();
            if (bindValue != null) {

                statement = (OraclePreparedStatement) connection.prepareStatement(cmd);
                for (int i = 0; i < bindValue.size(); i++) {
                    statement.setStringAtName(bindName.get(i).replace(":", ""), bindValue.get(i));

                }
            } else {
                statement = (OraclePreparedStatement) connection.prepareStatement(cmd);
            }

            statement.execute();
            int done = 0;
            try {
                int updcnt = statement.getUpdateCount();
                if (updcnt != -1) {
                    log_output.append("!Update/Delete Count : " + updcnt);
                }
            } catch (Exception e) {
            }
            show_stmt.registerOutParameter(2, java.sql.Types.INTEGER);
            show_stmt.registerOutParameter(3, java.sql.Types.VARCHAR);

            for (;;) {
                show_stmt.setInt(1, 32000);
                show_stmt.executeUpdate();
                log_output.append(show_stmt.getString(3));
                if ((done = show_stmt.getInt(2)) == 1) {
                    break;
                }
            }

            enable_stmt = connection.prepareCall("begin dbms_output.disable; end;");
            enable_stmt.executeUpdate();

        } catch (SQLException e) {
            log_output.append("Error running script.  Cause: " + e);
        } catch (Exception e) {
            log_output.append("Error running script.  Cause: " + e);
        }
        return log_output;
    }

    public StringBuffer runScript(Reader reader) throws IOException, SQLException {
        try {
            runScript(connection, reader);
        } catch (IOException e) {
            log_output.append("Error running script.  Cause: " + e);
        } catch (SQLException e) {
            log_output.append("Error running script.  Cause: " + e);
        } catch (Exception e) {
            log_output.append("Error running script.  Cause: " + e);
        }
        return log_output;
    }

    /**
     * Runs an SQL script (read in using the Reader parameter) using the
     * connection passed in
     *
     * @param conn - the connection to use for the script
     * @param reader - the source of the script
     * @throws SQLException if any SQL errors occur
     * @throws IOException if there is an error reading from the Reader
     */
    private void runScript(Connection conn, Reader reader) throws IOException,
            SQLException {
        StringBuffer command = null;
        try {
            LineNumberReader lineReader = new LineNumberReader(reader);
            String line = null;
            while ((line = lineReader.readLine()) != null) {
                if (command == null) {
                    command = new StringBuffer();
                }
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--")) {
                    println(trimmedLine);
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("//")) {
                    // Do nothing
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("--")) {
                    // Do nothing
                } else if (!fullLineDelimiter
                        && trimmedLine.endsWith(getDelimiter())
                        || fullLineDelimiter
                        && trimmedLine.equals(getDelimiter())) {

                    Pattern pattern = Pattern.compile(DELIMITER_LINE_REGEX);
                    Matcher matcher = pattern.matcher(trimmedLine);
                    if (matcher.matches()) {
                        setDelimiter(trimmedLine.split(DELIMITER_LINE_SPLIT_REGEX)[1].trim(), fullLineDelimiter);
                        line = lineReader.readLine();
                        if (line == null) {
                            break;
                        }
                        trimmedLine = line.trim();
                    }

                    command.append(line.substring(0, line.lastIndexOf(getDelimiter())));
                    command.append(" ");
                    Statement statement = conn.createStatement();

                    //println(command);
                    String cmd = command.toString();
                    boolean hasResults = false;
                    if (stopOnError) {
                        hasResults = statement.execute(command.toString());
                    } else {
                        try {
                            hasResults = statement.execute(command.toString());
                            try {
                                int updcnt = statement.getUpdateCount();
                                if (updcnt != -1) {
                                    println("!Update/Delete Count : " + updcnt);
                                }
                            } catch (Exception e) {
                            }
                            try {
                                int delcnt = statement.getUpdateCount();
                                if (delcnt != -1) {
                                    println("!Delete Count : " + delcnt);
                                }
                            } catch (Exception e) {
                            }

                        } catch (SQLException e) {
                            e.fillInStackTrace();
                            printlnError("Error executing: " + command);
                            printlnError(e);
                        }
                    }

                    ResultSet rs = statement.getResultSet();
                    if (hasResults && rs != null) {
                        ResultSetMetaData md = rs.getMetaData();
                        int cols = md.getColumnCount();
                        for (int i = 1; i < cols; i++) {
                            String name = md.getColumnLabel(i);
                            print(name + "\t");
                        }
                        println("");
                        while (rs.next()) {
                            for (int i = 1; i < cols; i++) {
                                String value = rs.getString(i);
                                print(value + "\t");
                            }
                            println("");
                        }
                    }

                    command = null;
                    try {
                        statement.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Ignore to workaround a bug in Jakarta DBCP
                    }
                    Thread.yield();
                } else {
                    Pattern pattern = Pattern.compile(DELIMITER_LINE_REGEX);
                    Matcher matcher = pattern.matcher(trimmedLine);
                    if (matcher.matches()) {
                        setDelimiter(trimmedLine.split(DELIMITER_LINE_SPLIT_REGEX)[1].trim(), fullLineDelimiter);
                        line = lineReader.readLine();
                        if (line == null) {
                            break;
                        }
                        trimmedLine = line.trim();
                    }
                    command.append(line);
                    command.append(" ");
                }
            }
        } catch (SQLException e) {
            e.fillInStackTrace();
            printlnError("Error executing: " + command);
            printlnError(e);
            throw e;
        } catch (IOException e) {
            e.fillInStackTrace();
            printlnError("Error executing: " + command);
            printlnError(e);
            throw e;
        } finally {

        }

    }

    private String getDelimiter() {
        return delimiter;
    }

    private void print(Object o) {
        log_output.append(o);
        if (logWriter != null) {
            System.out.print(o);
        }
    }

    private void println(Object o) {
        log_output.append("\n" + o);
        if (logWriter != null) {
            logWriter.println(o);
        }
    }

    private void printlnError(Object o) {
        log_output.append(o);
        if (errorLogWriter != null) {
            errorLogWriter.println(o);
        }
    }

    private void flush() {
        if (logWriter != null) {
            logWriter.flush();
        }
        if (errorLogWriter != null) {
            errorLogWriter.flush();
        }
    }
}
