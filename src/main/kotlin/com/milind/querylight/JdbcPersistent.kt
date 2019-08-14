package com.milind.querylight

//import org.omg.CORBA.Object



import oracle.jdbc.OraclePreparedStatement
import oracle.jdbc.pool.OracleDataSource
import oracle_xls_extract.Oracle_xls_extract
import org.apache.poi.xssf.streaming.SXSSFRow
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFFont
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.*
import java.nio.charset.StandardCharsets
import java.sql.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import java.util.prefs.Preferences
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer


/**

 * @author sqloper4
 */

class JdbcPersistent {


    var conn: Connection? = null
    var stmt: Statement? = null
    var rs: ResultSet?=null
    var uid: String? = null
        private set
    var machine_name: String? = null
        private set
    var dbName: String? = null
    internal var SingleStmt: PreparedStatement? = null
    var pwd: String? = null
        private set
    var jdbcstr: String? = null
        private set

    constructor(jp: JdbcPersistent) {
        try {
            this.uid = jp.uid
            this.pwd = jp.pwd
            this.jdbcstr = jp.jdbcstr
            this.dbName = jp.dbName
            connect_db()
            stmt?.setFetchSize(200)
        } catch (er: Exception) {
            val exption = er.toString()
            print(exption)
        } finally {
            try {
            }//  if (conn != null) conn.close();
            catch (ignored: Exception) {
                val exption = ignored.toString()
                print(exption)
            }

        }
    }

    @Throws(Exception::class)
    constructor(uid: String, pwd: String, jdbcstr: String) {
        try {
            this.uid = uid
            this.pwd = pwd
            this.jdbcstr = jdbcstr
            connect_db()
            stmt?.setFetchSize(200)
        } catch (er: Exception) {
            val exption = er.toString()
            throw er
        } finally {
            try {
            }//  if (conn != null) conn.close();
            catch (ignored: Exception) {
                val exption = ignored.toString()
                print(exption)
            }

        }
    }

    @Throws(Exception::class)
    constructor(uid: String, pwd: String, jdbcstr: String, dbName: String) {
        try {
            this.uid = uid
            this.pwd = pwd
            this.jdbcstr = jdbcstr
            this.dbName = dbName
            connect_db()
            stmt?.setFetchSize(200)
        } catch (er: Exception) {
            val exption = er.toString()
            throw er
        } finally {
            try {
            }//  if (conn != null) conn.close();
            catch (ignored: Exception) {
                val exption = ignored.toString()
                print(exption)
            }

        }
    }

    @Throws(SQLException::class)
    fun fetchNextRows(tbl: JTable) {
        val columnNames = Vector<Any>()
        val data = Vector<Any>()
        var row_count = 0
        try {

            val md: ResultSetMetaData
            try {
                md = rs?.metaData?:return
            } catch (ex: NullPointerException) {
                return
            }

            val columns = md.columnCount
            val model = tbl.model as DefaultTableModel
            while (rs!!.next()) {
                val row = Vector<Any>(columns)

                for (i in 0..columns) {
                    if (i == 0) {
                        row.addElement(rs!!.row)
                    } else {
                        try {
                            if (md.getColumnTypeName(i) == "DATE") {
                                row.addElement(rs!!.getTimestamp(i))
                            } else {
                                row.addElement(rs!!.getString(i))
                            }
                        } catch (e: NullPointerException) {
                            row.addElement(null)
                        }

                    }
                }

                data.addElement(row)
                model.addRow(row)
                row_count++
                if (row_count >= Bms_Constants.fetchSize) {
                    break
                }
            }

        } catch (e: SQLException) {
            throw e
        }

    }

    fun create_standard_table(): JTable {
        return create_standard_tableAct(true, false, false)
    }

    fun create_standard_table(copyEnabled: Boolean, cutEnabled: Boolean, pasteEnabled: Boolean): JTable {
        return create_standard_tableAct(copyEnabled, cutEnabled, pasteEnabled)
    }

    private fun create_standard_tableAct(copyEnabled: Boolean, cutEnabled: Boolean, pasteEnabled: Boolean): JTable {
        val columnNames = arrayOf("-", "-", "-")
        val dataValues = arrayOf(arrayOf("-", "-", "-"))
        val tbl_grid = JTable(dataValues, columnNames)
        val cka = ClipboardKeyAdapter(tbl_grid, copyEnabled, cutEnabled, pasteEnabled)
        tbl_grid.addKeyListener(cka)
        tbl_grid.autoResizeMode = JTable.AUTO_RESIZE_OFF
        tbl_grid.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
        tbl_grid.cellSelectionEnabled = true

        tbl_grid.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if (e!!.button == MouseEvent.BUTTON3) {
                    //Do somethihng on rightclick
                }
                if (e.clickCount == 2) {
                    val target = e.source as JTable
                    val row = target.selectedRow
                    val column = target.selectedColumn
                    val stringSelection = StringSelection(target.getValueAt(row, column).toString())
                    val clpbrd = Toolkit.getDefaultToolkit().systemClipboard
                    clpbrd.setContents(stringSelection, null)
                }
            }
        })
        return tbl_grid
    }

    fun show_alert(c: Component, err_msg: String) {
        JOptionPane.showMessageDialog(c, err_msg, "Error", JOptionPane.ERROR_MESSAGE)
    }

    fun show_msg(c: Component, msg: String) {
        JOptionPane.showMessageDialog(c, msg, "-", JOptionPane.INFORMATION_MESSAGE)
    }

    fun resize_tbl_cols(table: JTable, maxcolsize: Int) {
        resize_tbl_colsAct(table, maxcolsize)
    }

    fun resize_tbl_cols(table: JTable) {
        resize_tbl_colsAct(table, JdbcPersistent.jTableMaxColSize)
    }

    private fun resize_tbl_colsAct(table: JTable, maxcolsize: Int) {
        val columnModel = table.columnModel
        var renderer: TableCellRenderer
        var width: Int // Min width
       val  tcr = tableCellRenderer().tableCellRenderer
        for (column in 0..table.columnCount - 1) {
            val tableColumn = columnModel.getColumn(column)
            //  width = tableColumn.getPreferredWidth();
            val headerRenderer = table.tableHeader.defaultRenderer
            val headerValue = tableColumn.headerValue
            val headerComp = headerRenderer.getTableCellRendererComponent(table, headerValue, false, false, 0, column)
            width = headerComp.preferredSize.width
           table.columnModel.getColumn(column).cellRenderer = tcr
            for (row in 0..table.rowCount) {
                renderer = table.getCellRenderer(row, column)
                var comp: Component? = null
                try {
                    comp = table.prepareRenderer(renderer, row, column)
                    width = Math.min(maxcolsize, Math.max(comp!!.preferredSize.width, width))
                } catch (e: Exception) {
                    // width = Math.min(200,Math.max(comp.getPreferredSize().width, width));
                }
            }
            columnModel.getColumn(column).preferredWidth = width
        }
    }

    fun findWordAtCaret(Text: String?, pos: Int): String {
        if (Text != null && Text.length > 0) {
            val TempString = StringBuffer(Text.replace("\\s+".toRegex(), "`"))
            val startpos = if (TempString.lastIndexOf("`", pos) < 0) 0 else TempString.lastIndexOf("`", pos) + 1
            val endpos = if (TempString.indexOf("`", pos) < 0) TempString.length else TempString.lastIndexOf("`", pos)
            return TempString.substring(startpos, endpos)
        } else {
            return ""
        }
    }

    @Throws(SQLException::class)
    fun TableFromDatabase(tbl: JTable, sql: String) {
        TableFromDatabaseAct(tbl, sql, false, null, null)
    }

    @Throws(SQLException::class)
    fun TableFromDatabase(tbl: JTable, sql: String, fetchall: Boolean) {
        TableFromDatabaseAct(tbl, sql, fetchall, null, null)
    }

    //  public Vector<String> getSingleFieldVector
    @Throws(SQLException::class)
    fun TableFromDatabase(tbl: JTable, sql: String, bindValue: ArrayList<String>, bindName: ArrayList<String>) {
        TableFromDatabaseAct(tbl, sql, false, bindValue, bindName)
    }

    @Throws(SQLException::class)
    fun TableFromDatabase(tbl: JTable, sql: String, fetchall: Boolean, bindValue: ArrayList<String>, bindName: ArrayList<String>) {
        TableFromDatabaseAct(tbl, sql, fetchall, bindValue, bindName)
    }

    @Throws(Exception::class)
    fun genInsFromQuery(sql: String, tblname: String): StringBuffer {
        return genInsFromQueryAct(sql, tblname)
    }

    @Throws(Exception::class)
    fun genInsFromQuery(sql: String): StringBuffer {
        return genInsFromQueryAct(sql, null)
    }

    @Throws(Exception::class)
    private fun genInsFromQueryAct(sql: String, tblname: String?): StringBuffer {
        val row_count = 0
        val inser_script = StringBuffer()
        val STMT: java.sql.PreparedStatement
        try {
            val jp = this//new JdbcPersistent(uid, pwd, jdbcstr);
            val columnNames = Vector<Any>()
            STMT = jp.conn!!.prepareStatement(sql)
            STMT.setFetchSize(200)
            jp.rs = STMT.executeQuery()
            val md = (jp.rs as ResultSet?)?.metaData
            val columns = md?.columnCount?:0
            val tablename = tblname ?: "TABLENAME"
            for (i in 1..columns) {
                columnNames.addElement(md!!.getColumnName(i))
            }

            //  Get row data
            while ((jp.rs as ResultSet?)!!.next()) {
                val ins_stmt = StringBuffer("Insert into $tablename(")
                for (i in 1..columns) {
                    val sep = if (i == columns) " " else ","
                    ins_stmt.append(md?.getColumnName(i) + sep)
                }
                ins_stmt.append(")$lineSeperator values(")
                for (i in 1..columns) {
                    val sep = if (i == columns) " " else ","
                    try {
                        if (md?.getColumnTypeName(i)!!.contains("NUMBER")) {
                            ins_stmt.append(jp.rs!!.getString(i) + sep)
                        } else if (md.getColumnTypeName(i) == "DATE") {
                            val tmpdate = jp.rs?.getString(i)?.substring(0, 19)?:""
                            ins_stmt.append("to_date('$tmpdate' , 'YYYY-MM-DD HH24:MI:SS' )$sep")
                        } else if (md.getColumnTypeName(i) == "LONG")
                             {
                                val Is = jp.rs?.getAsciiStream(i)
                                val buffer = ByteArrayOutputStream()
                                var nRead: Int = 0
                                val data = ByteArray(1024)
                                do {
                                    if (Is != null) {
                                        nRead = Is.read(data, 0, data.size)
                                    }
                                    if(nRead == -1) break;
                                    buffer.write(data, 0, nRead)
                                }while ( nRead != -1)

                                buffer.flush()
                                val byteArray = buffer.toByteArray()
                                val text = String(byteArray, StandardCharsets.UTF_8)
                                 if (text == null) {
                                     ins_stmt.append("null" + sep)
                                 } else {
                                     ins_stmt.append("'" +text.replace("'" , "''") + "'" + sep)
                                 }
                            }
                        else {
                            if (jp.rs!!.getObject(i) == null) {
                                ins_stmt.append("null" + sep)
                            } else {
                                ins_stmt.append("'" + jp.rs!!.getString(i).replace("'","''") + "'" + sep)
                            }
                        }
                    } catch (e: NullPointerException) {
                        ins_stmt.append("null" + sep)
                    }

                }
                ins_stmt.append(");" + lineSeperator)
                inser_script.append(ins_stmt)
            }
            STMT.close()
        } catch (e: SQLException) {
            throw e
        }

        return inser_script
    }

    fun fileNameToOpen(): String {
        return fileNameToOpenAct(null, null, null)!!
    }

    fun fileNameToOpen(filterDesc: String, filterTypeArray: Array<String>, classSetting: Class<*>): String {
        return fileNameToOpenAct(filterDesc, filterTypeArray, classSetting)!!
    }

    private fun fileNameToOpenAct(filterDesc: String?, filterTypeArray: Array<String>?, classSetting: Class<*>?): String? {
        var path: String? = null
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Specify a file to Open"
        if (filterTypeArray != null) {
            val filter1 = ExtensionFileFilter(filterDesc, filterTypeArray)
            fileChooser.fileFilter = filter1
        }
        if (classSetting != null) {
            val prf = Preferences.userNodeForPackage(classSetting)
            path = prf.get("LastDir", "")
        }
        if (path != null) fileChooser.currentDirectory = File(path)

        val userSelection = fileChooser.showOpenDialog(null)
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            val fileToSave = fileChooser.selectedFile
            return fileToSave.absolutePath
        }
        return null
    }


    @Throws(Exception::class)
    fun genTabSepfilefrmQuery(sql: String, fname_path: String) {
        val row_count = 0
        val writer = PrintWriter(fname_path, "UTF-8")
        val STMT: java.sql.PreparedStatement
        try {
            val jp = this //new JdbcPersistent(uid, pwd, jdbcstr);
            val columnNames = Vector<Any>()

            STMT = jp.conn!!.prepareStatement(sql)
            STMT.setFetchSize(200)
            jp.rs = STMT.executeQuery()
            val md = jp.rs!!.metaData
            val columns = md.columnCount
            //  Get column names
            // Added to have row numbers shown
            for (i in 1..columns) {
                columnNames.addElement(md.getColumnName(i))

            }

            for (i in 1..columns) {
                val sep = if (i == columns) "" else ","
                writer.print(md.getColumnName(i) + sep)
            }
            writer.println()
            while (jp.rs!!.next()) {
                val csvline = StringBuffer()
                for (i in 1..columns) {
                    val sep = if (i == columns) "" else "\t"
                    try {
                        if (md.getColumnTypeName(i).contains("NUMBER")) {
                            csvline.append(jp.rs!!.getString(i) + sep)
                        } else if (md.getColumnTypeName(i) == "DATE") {
                            val f = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val tmpdate = f.format(jp.rs!!.getTimestamp(i))
                            csvline.append(tmpdate + sep)
                        } else {
                            if (jp.rs!!.getObject(i) == null) {
                                csvline.append(sep)
                            } else {
                                val tmpstring = if (jp.rs!!.getString(i).contains("\t") || jp.rs!!.getString(i).contains("\n")) "\"" + jp.rs!!.getString(i).replace('"', '\'') + "\"" else jp.rs!!.getString(i)
                                csvline.append(tmpstring + sep)
                            }
                        }
                    } catch (e: NullPointerException) {
                        csvline.append(sep)
                    }

                }
                writer.println(csvline)
            }
            writer.close()

        } catch (e: SQLException) {
            throw e
        }

        // return CSV;
    }

    @Throws(Exception::class)
    fun genXLSfilefrmQuery(sql: String, wworkbook: SXSSFWorkbook, OCX: Oracle_xls_extract, include_header: Boolean) {
        var row_count = OCX.rowcounter % 1000000
        var wsheet: SXSSFSheet
        //   PrintWriter writer = new PrintWriter(fname_path, "UTF-8");
        if (wworkbook.numberOfSheets > 0) {
            wsheet = wworkbook.getSheetAt(wworkbook.activeSheetIndex)
        } else {
            wsheet = wworkbook.createSheet("1")
        }


        val STMT: java.sql.PreparedStatement
        try {
            val jp = JdbcPersistent(uid!!, pwd!!, jdbcstr!!)
            val columnNames = Vector<Any>()

            STMT = jp.conn!!.prepareStatement(sql)
            STMT.setFetchSize(200)
            jp.rs = STMT.executeQuery()
            val md = jp.rs!!.metaData
            val columns = md.columnCount
            val createHelper = wworkbook.creationHelper
            val datecellStyle = wworkbook.createCellStyle()
            datecellStyle.dataFormat = createHelper.createDataFormat().getFormat("dd-MMM-yyyy hh:mm")
            val headerstyle = wworkbook.createCellStyle()
            val font = wworkbook.createFont()
            font.fontName = XSSFFont.DEFAULT_FONT_NAME
            font.fontHeightInPoints = 10.toShort()
            font.bold = true
            headerstyle.setFont(font)

            var hdrrow: SXSSFRow? = null

            for (i in 1..columns) {
                columnNames.addElement(md.getColumnName(i))
            }

            run {
                var i = 1
                while (i <= columns && include_header) {
                    if (i == 1 && include_header) hdrrow = wsheet.createRow(row_count)
                    val cell = hdrrow!!.createCell(i - 1)
                    cell.setCellValue(createHelper.createRichTextString(md.getColumnName(i)))
                    cell.cellStyle = headerstyle
                    i++

                }
            }
            //  writer.println();

            while (jp.rs!!.next()) {
                OCX.rowcounter++
                row_count++
                val datarow = wsheet.createRow(row_count)

                for (i in 1..columns) {
                    val sep = if (i == columns) "" else ","
                    try {
                        if (md.getColumnTypeName(i).contains("NUMBER")) {

                            datarow.createCell(i - 1).setCellValue(jp.rs!!.getDouble(i))

                        } else if (md.getColumnTypeName(i) == "DATE") {


                            //   DateTime dateCell = new DateTime(i-1,  row_count , jp.rs.getTimestamp(i), dateFormat);
                            val cell = datarow.createCell(i - 1)
                            cell.cellStyle = datecellStyle
                            cell.setCellValue(jp.rs!!.getTimestamp(i))

                        } else {
                            if (jp.rs!!.getObject(i) == null) {
                                datarow.createCell(i - 1)

                            } else {

                                val cell = datarow.createCell(i - 1)
                                cell.setCellValue(createHelper.createRichTextString(jp.rs!!.getString(i)))
                            }
                        }
                    } catch (e: NullPointerException) {
                        // Label label = new Label(i-1, row_count , null );
                        // wsheet.addCell(label);
                        datarow.createCell(i - 1)
                    }

                }
                //  writer.println(csvline);

                if (OCX.rowcounter % 1000000 == 0) {
                    //    wsheet = wworkbook.createSheet( String.valueOf( wworkbook.getNumberOfSheets() + 1) , wworkbook.getNumberOfSheets() - 1);
                    wsheet = wworkbook.createSheet((wworkbook.numberOfSheets + 1).toString())
                    row_count = OCX.rowcounter % 1000000
                }
            }

        } catch (e: SQLException) {
            throw e
        }

        // return XLS;
    }

    @Throws(Exception::class)
    fun genXLSfilefrmQuery(sql: String, fname_path: String) {
        val row_count = 0
        //   PrintWriter writer = new PrintWriter(fname_path, "UTF-8");
        val wb = SXSSFWorkbook(100)
        genXLSfilefrmQuery(sql, wb, Oracle_xls_extract(null), true)


        val fileOut = FileOutputStream(fname_path)
        wb.write(fileOut)
        fileOut.close()
        wb.dispose()

        // return CSV;
    }


    @Throws(Exception::class)
    fun genCSVfilefrmQuery(sql: String, fname_path: String) {
        val row_count = 0
        val writer = PrintWriter(fname_path, "UTF-8")
        val STMT: java.sql.PreparedStatement
        try {
            val jp = this// new JdbcPersistent(uid, pwd, jdbcstr);
            val columnNames = Vector<Any>()

            STMT = jp.conn!!.prepareStatement(sql)
            STMT.setFetchSize(200)
            jp.rs = STMT.executeQuery()
            val md = jp.rs!!.metaData
            val columns = md.columnCount
            //  Get column names
            // Added to have row numbers shown
            for (i in 1..columns) {
                columnNames.addElement(md.getColumnName(i))

            }

            for (i in 1..columns) {
                val sep = if (i == columns) "" else ","
                writer.print(md.getColumnName(i) + sep)
            }
            writer.println()
            while (jp.rs!!.next()) {
                val csvline = StringBuffer()
                for (i in 1..columns) {
                    val sep = if (i == columns) "" else ","
                    try {
                        if (md.getColumnTypeName(i).contains("NUMBER")) {
                            csvline.append(jp.rs!!.getString(i) + sep)
                        } else if (md.getColumnTypeName(i) == "DATE") {
                            val f = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val tmpdate = f.format(jp.rs!!.getTimestamp(i))
                            csvline.append(tmpdate + sep)
                        } else {
                            if (jp.rs!!.getObject(i) == null) {
                                csvline.append(sep)
                            } else {
                                val tmpstring = if (jp.rs!!.getString(i).contains(",") || jp.rs!!.getString(i).contains("\n")) "\"" + jp.rs!!.getString(i).replace('"', '\'') + "\"" else jp.rs!!.getString(i)
                                csvline.append(tmpstring + sep)
                            }
                        }
                    } catch (e: NullPointerException) {
                        csvline.append(sep)
                    }

                }
                writer.println(csvline)
            }
            writer.close()

        } catch (e: SQLException) {
            throw e
        }

        // return CSV;
    }

    @Throws(Exception::class)
    fun genCSVfilefrmQuery(sql: String, writer: PrintWriter, include_header: Boolean) {
        val row_count = 0
        //   PrintWriter writer = new PrintWriter(fname_path, "UTF-8");
        val STMT: java.sql.PreparedStatement
        try {
            val jp = JdbcPersistent(uid!!, pwd!!, jdbcstr!!)
            val columnNames = Vector<Any>()

            STMT = jp.conn!!.prepareStatement(sql)
            jp.rs = STMT.executeQuery()
            STMT.setFetchSize(200)
            val md = jp.rs!!.metaData
            val columns = md.columnCount
            //  Get column names
            // Added to have row numbers shown
            run {
                var i = 1
                while (i <= columns) {
                    columnNames.addElement(md.getColumnName(i))
                    i++

                }
            }

            run {
                var i = 1
                while (i <= columns && include_header) {
                    val sep = if (i == columns) "" else ","
                    writer.print(md.getColumnName(i) + sep)
                    i++
                }
            }
            writer.println()
            while (jp.rs!!.next()) {
                val csvline = StringBuffer()
                for (i in 1..columns) {
                    val sep = if (i == columns) "" else ","
                    try {
                        if (md.getColumnTypeName(i).contains("NUMBER")) {
                            csvline.append(jp.rs!!.getString(i) + sep)
                        } else if (md.getColumnTypeName(i) == "DATE") {
                            val f = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val tmpdate = f.format(jp.rs!!.getTimestamp(i))
                            csvline.append(tmpdate + sep)
                        } else {
                            if (jp.rs!!.getObject(i) == null) {
                                csvline.append(sep)
                            } else {
                                val tmpstring = if (jp.rs!!.getString(i).contains(",") || jp.rs!!.getString(i).contains("\n")) "\"" + jp.rs!!.getString(i).replace('"', '\'') + "\"" else jp.rs!!.getString(i)
                                csvline.append(tmpstring + sep)
                            }
                        }
                    } catch (e: NullPointerException) {
                        csvline.append(sep)
                    }

                }
                writer.println(csvline)
            }
            //     writer.close();

        } catch (e: SQLException) {
            throw e
        }

        // return CSV;
    }


    @Throws(Exception::class)
    fun genCSVfrmQuery(sql: String): StringBuffer {
        val row_count = 0
        val CSV = StringBuffer()
        val STMT: java.sql.PreparedStatement
        try {
            val jp = this // new JdbcPersistent(uid, pwd, jdbcstr);
            val columnNames = Vector<Any>()

            STMT = jp.conn!!.prepareStatement(sql)
            STMT.setFetchSize(200)
            jp.rs = STMT.executeQuery()
            val md = jp.rs!!.metaData
            val columns = md.columnCount
            //  Get column names
            // Added to have row numbers shown
            for (i in 1..columns) {
                columnNames.addElement(md.getColumnName(i))
            }

            for (i in 1..columns) {
                val sep = if (i == columns) "" else ","
                CSV.append(md.getColumnName(i) + sep)
            }
            CSV.append(lineSeperator)
            if(jp.rs!=null) {
                while (jp.rs!!.next()) {
                    val csvline = StringBuffer()
                    for (i in 1..columns) {
                        val sep = if (i == columns) "" else ","
                        try {
                            if (md.getColumnTypeName(i).contains("NUMBER")) {
                                csvline.append(jp.rs!!.getString(i) + sep)
                            } else if (md.getColumnTypeName(i) == "DATE") {
                                val f = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                val tmpdate = f.format(jp.rs!!.getTimestamp(i))
                                csvline.append(tmpdate + sep)
                            } else {
                                if (jp.rs!!.getObject(i) == null) {
                                    csvline.append(sep)
                                } else {
                                    val tmpstring = if (jp.rs!!.getString(i).contains(",") || jp.rs!!.getString(i).contains("\n")) "\"" + jp.rs!!.getString(i).replace('"', '\'') + "\"" else jp.rs!!.getString(i)
                                    csvline.append(tmpstring + sep)
                                }
                            }
                        } catch (e: NullPointerException) {
                            csvline.append(sep)
                        }

                    }
                    csvline.append(lineSeperator)
                    CSV.append(csvline)
                }
            }
        } catch (e: SQLException) {
            throw e
        }

        return CSV
    }

    @Throws(SQLException::class)
    fun TableFromDatabaseSingleRecordView(tbl: JTable, sql: String) {
        val columnNames = Vector<Any>()
        val dummy = Vector<Any>()
        val data = Vector<Vector <Any>>()
        val row_count = 0
        val STMT: java.sql.PreparedStatement
        try {
            STMT = this.conn!!.prepareStatement(sql)
            //STMT.setMaxRows(10);
            this.rs = STMT.executeQuery()?:return
            val md = rs?.metaData?:return
            val columns = md.columnCount
            columnNames.addElement("Attribute")
            columnNames.addElement("Value")
            if (rs!!.next()) {
                for (i in 1..columns) {
                    val row = Vector<Any>(2)
                    row.add(md.getColumnName(i))
                    try {
                        if (md.getColumnTypeName(i) == "DATE") {
                            row.addElement(rs!!.getTimestamp(i))
                        } else {
                            row.addElement(rs!!.getString(i))
                        }
                    } catch (e: NullPointerException) {
                        row.addElement(null)
                    }

                    data.add(row)
                }

            }
        } catch (e: SQLException) {
            throw e
        }

        val model: DefaultTableModel
        model = object : DefaultTableModel(data, columnNames) {
            override fun isCellEditable(rowIndex: Int, mColIndex: Int): Boolean {
                return false
            }

            override fun getColumnClass(column: Int): Class<*> {
                for (row in 0..this.rowCount - 1) {
                    val o = getValueAt(row, column)

                    if (o != null) {
                        return o.javaClass
                    }
                }

                return Any::class.java
            }
        }

        tbl.model = model
        this.resize_tbl_cols(tbl)
    }

    @Throws(SQLException::class)
    private fun TableFromDatabaseAct(tbl: JTable, sql: String, fetchall: Boolean, bindValue: ArrayList<String>?, bindName: ArrayList<String>?) {
        val columnNames = Vector<Any>()
        val data = Vector<Vector <Any>>()
        var row_count = 0

        val STMT: OraclePreparedStatement
        val savepoint1 = if(this.conn?.isValid(2)?:false)  this.conn?.setSavepoint() else return
        try {

            if (bindValue != null) {

                STMT = this.conn!!.prepareStatement(sql) as OraclePreparedStatement
                for (i in bindValue.indices) {
                    STMT.setStringAtName(bindName!![i].replace(":", ""), bindValue[i])
                }
            } else {
                STMT = this.conn!!.prepareStatement(sql) as OraclePreparedStatement
            }
            STMT.setFetchSize(200)


            this.rs = STMT.executeQuery()
            val md = rs?.metaData?:return
            val columns = md.columnCount

            //  Get column names
            columnNames.addElement("#") // Added to have row numbers shown
            for (i in 1..columns) {
                columnNames.addElement(md.getColumnName(i))
            }

            //  Get row data
            while (rs!!.next()) {
                val row = Vector<Any>(columns)

                for (i in 0..columns) {
                    if (i == 0) {
                        row.addElement(rs!!.row)
                    } else {
                        try {
                            if (md.getColumnTypeName(i) == "DATE") {
                                row.addElement(rs!!.getTimestamp(i))
                            } else {
                                row.addElement(rs!!.getString(i))
                            }
                        } catch (e: NullPointerException) {
                            row.addElement(null)
                        }

                    }
                }

                data.addElement(row)
                row_count++
                if (row_count >= Bms_Constants.fetchSize && !fetchall) {
                    break
                }
            }

        } catch (e: SQLException) {
            this.conn!!.rollback(savepoint1)
            throw e

        }


        val model: DefaultTableModel
        model = object : DefaultTableModel(data, columnNames) {
            override fun isCellEditable(rowIndex: Int, mColIndex: Int): Boolean {
                return false
            }

            override fun getColumnClass(column: Int): Class<*> {
                for (row in 0..this.rowCount - 1) {
                    val o = getValueAt(row, column)

                    if (o != null) {
                        return o.javaClass
                    }
                }

                return Any::class.java
            }
        }

        tbl.model = model
        this.resize_tbl_cols(tbl)


    }

    @Throws(Exception::class)
    fun execute_stmt(stmt: String) {
        val cstmt = this.conn!!.prepareCall(stmt)
        cstmt.execute()
        cstmt.close()
    }

    fun execute_stmt(stmt: String, DML: Boolean?): String {
        val cstmt: CallableStatement
        var cnt = 0
        try {
            cstmt = this.conn!!.prepareCall(stmt)
            cstmt.executeUpdate()

            if (DML!!) {
                cnt = cstmt.updateCount
                cstmt.close()
                return "Success Rows Affected : " + cnt

            } else {
                cstmt.close()
                return "Success"
            }


        } catch (ex: SQLException) {
            return ex.toString()
        }

    }


    @Throws(Exception::class)
    fun reconnect() {
        try {
            connect_db()
        } catch (er: Exception) {
            er.toString()
            throw er
        } finally {
            try {
            }//  if (conn != null) conn.close();
            catch (ignored: Exception) {
                val exption = ignored.toString()
                print(exption)
            }

        }

    }


    @Throws(SQLException::class, ClassNotFoundException::class)
    private fun connect_db() {
  /*    Class.forName("oracle.jdbc.driver.OracleDriver")
        val props = java.util.Properties()
        props.setProperty("password", pwd)
        props.setProperty("user", uid)
        props.put("v\$session.osuser", uid!!)
        machine_name = jdbcstr!!.substring(jdbcstr!!.indexOf("@") + 1, if (jdbcstr!!.indexOf(".") > 1) jdbcstr!!.indexOf(".") else jdbcstr!!.indexOf(":", jdbcstr!!.indexOf("@")))
        props.put("v\$session.machine", machine_name!!)
        props.put("v\$session.program", "SQL*Plus")
        println("connecting to $jdbcstr")
        this.conn = DriverManager.getConnection(jdbcstr!!, props)
      */

        val ods = OracleDataSource()
        val prop = java.util.Properties()
        prop.setProperty("MinLimit", "2")
        prop.setProperty("MaxLimit", "10")
        //   String url = "jdbc:oracle:oci8:@//xxx.xxx.xxx.xxx:1521/orcl";
        ods.url = jdbcstr
        ods.user = uid
        ods.setPassword(pwd)
        ods.connectionCachingEnabled = true
        ods.connectionCacheProperties = prop

        //ods.connectionCacheName = "RunSQLCache01"

        //   Class.forName("oracle.jdbc.driver.OracleDriver");
        //   java.util.Properties props = new java.util.Properties();
        //   props.setProperty("password", pwd);
        //   props.setProperty("user", uid);
        machine_name = jdbcstr!!.substring(jdbcstr!!.indexOf("@") + 1, jdbcstr!!.indexOf("."))
        prop.setProperty("v\$session.osuser" , uid)
        prop.setProperty("v\$session.machine" , machine_name)
        prop.setProperty("v\$session.program", "SQL*Plus")
        this.conn = ods.connection

        this.conn!!.prepareStatement("alter session set nls_date_format = 'DD-MON-YYYY'").execute()
        this.conn!!.autoCommit = false

        try {
            this.conn!!.prepareStatement("alter session set nls_date_format = 'DD-MON-YYYY'").execute()
        } catch(e: Exception) {
        }
    }

    @Throws(SQLException::class)
    fun runqry(qry: String): ResultSet {
        val query = qry
        //this.rs = stmt.executeQuery(query);
        try {
            if (SingleStmt != null) SingleStmt!!.close()
        } catch (sQLException: SQLException) {
        }

        SingleStmt = this.conn!!.prepareStatement(qry)
        val rs = SingleStmt!!.executeQuery()
        // PS.close();
        return rs
        //return this.rs;
    }

    fun returnSingleField(qry: String): String? {
        var result : String? = null
        try {
            val query = qry
            val PS = this.conn!!.prepareStatement(qry)
            val RS = PS.executeQuery()
            if (RS.next()) {
                result = RS.getString(1)
            } else {
                result = null
            }
            PS.close()
        } catch (ex: SQLException) {
            Logger.getLogger(JdbcPersistent::class.java.name).log(Level.SEVERE, null, ex)

        }
        return result;

    }

    fun returnSingleColumnArray(qry: String): ArrayList<*>? {

        val columnContent:ArrayList<String> = ArrayList()
        try {
            val query = qry
            val result = StringBuffer()
            //this.rs = stmt.executeQuery(query);
            val stmt = this.conn!!.prepareStatement(qry)
            stmt.fetchSize= 2000
            val RS = stmt.executeQuery()

            while (RS.next()) {
                columnContent.add(RS.getString(1))
            }
            return columnContent
        } catch (ex: SQLException) {
            Logger.getLogger(JdbcPersistent::class.java.name).log(Level.SEVERE, null, ex)
          return null
        }

    }

    fun returnSingleField(qry: String, return_all_rows: Boolean, linesep: String): String? {
        if (return_all_rows == false) {
            return returnSingleField(qry)
        } else {
            try {
                val query = qry
                val result = StringBuffer()
                //this.rs = stmt.executeQuery(query);
                val RS = this.conn!!.prepareStatement(qry).executeQuery()
                while (RS.next()) {
                    result.append(RS.getString(1) + linesep)
                }
                return result.toString()
            } catch (ex: SQLException) {
                return null
            }

        }
    }

    /**

     * @param qry
     * *
     * @param JB
     * *
     * @throws SQLException
     */
    @Throws(SQLException::class)
    fun runqry(qry: String, JB: javax.swing.JComboBox<*>) {
        val query = qry
        //this.rs = stmt.executeQuery(query);
        val RS = this.conn!!.prepareStatement(qry).executeQuery()
        JB.removeAllItems()
        var values : Vector<String> = Vector()
        while (RS.next()) {
            values.add(RS.getString(1))
        }
         JB.model=  DefaultComboBoxModel(values)
    }

    fun returnColumnValueMap(qry: String):ArrayList<ArrayList<Map<String,String>>>?  {
        val query = qry
        var rowCount = 0

        var data:ArrayList<ArrayList<Map<String,String>>> = ArrayList()
        try {
            rs = this.conn!!.prepareStatement(qry).executeQuery()

            val rsmd = rs?.metaData ?: return null
            val columnCount = rsmd.columnCount
            while (rs!!.next()) {
                var row:ArrayList<Map<String,String>> = ArrayList()
                for (i in 1..rsmd.columnCount) {
                row.add(mapOf(rsmd.getColumnName(i) to rs!!.getString(i)!!))
                }
                data.add(row)
            }
            }catch(e:Exception)
        {
            Logger.getLogger(JdbcPersistent::class.java.name).log(Level.SEVERE, null, e)
        }
        return data
    }

    @Throws(SQLException::class)
    fun returnHTMLTable(qry: String): StringBuffer {
        val query = qry

        val htmltbl = StringBuffer()
        var rowCount = 0
        try {
            rs = this.conn!!.prepareStatement(qry).executeQuery()
            htmltbl.append(" <div> <table id=\"appdata\">")
            val rsmd = rs?.metaData?:return StringBuffer()
            val columnCount = rsmd.columnCount
            // table header
            htmltbl.append("<thead>")
            htmltbl.append("<tr>")
            for (i in 0..columnCount - 1) {
                htmltbl.append("<th>" + rsmd.getColumnLabel(i + 1) + "</th>")
            }
            htmltbl.append("</tr>")
            htmltbl.append("</thead>")
            htmltbl.append("<tbody>")
            // the data
            while (rs!!.next()) {
                rowCount++
                htmltbl.append("<tr>")
                for (i in 0..columnCount - 1) {
                    htmltbl.append("<td>" + rs!!.getString(i + 1) + "</td>")
                }
                htmltbl.append("</tr>")
            }
            htmltbl.append("</tbody> </table></div>")

        } catch (ex: SQLException) {
            Logger.getLogger(JdbcPersistent::class.java.name).log(Level.SEVERE, null, ex)
            htmltbl.append("")
        }

        return htmltbl
    }

    @Throws(SQLException::class)
    fun returnOptionList(qry: String): StringBuffer {
        val query = qry

        val htmltbl = StringBuffer()
        var rowCount = 0
        try {
            rs = this.conn!!.prepareStatement(qry).executeQuery()
            while (rs!!.next()) {
                rowCount++
                htmltbl.append("<option value=\"")
                htmltbl.append(rs!!.getString(1) + "\">")
                htmltbl.append(rs!!.getString(2) + "</option>")
            }

        } catch (ex: SQLException) {
            Logger.getLogger(JdbcPersistent::class.java.name).log(Level.SEVERE, null, ex)
            htmltbl.append("")
        }

        return htmltbl
    }

    @Throws(SQLException::class)
    fun returnParamsTable(rdfname: String): StringBuffer {
        val query = "select order_seq# seq#, PROMPT_TEXT Prompt, PROMPT_TYPE Type, REQUIRED Req, DEFAULT_VALUE Enter_value, upper(PROMPT_VARIABLE_NAME) PROMPT_VARIABLE_NAME   " +
                "from rr_prompts where report_module='"         + rdfname + "' order by order_seq#"

        val htmltbl = StringBuffer()
        var rowCount = 0
        try {
            rs = this.conn!!.prepareStatement(query).executeQuery()
            htmltbl.append(" <div> <table id=\"appdata\">")
            val rsmd = rs?.metaData?:return StringBuffer()
            val columnCount = rsmd.columnCount
            // table header
            htmltbl.append("<thead>")
            htmltbl.append("<tr>")
            for (i in 0..columnCount - 1) {
                if (rsmd.getColumnLabel(i + 1) != "PROMPT_VARIABLE_NAME") {
                    htmltbl.append("<th>" + rsmd.getColumnLabel(i + 1) + "</th>")
                }
            }
            htmltbl.append("</tr>")
            htmltbl.append("</thead>")
            htmltbl.append("<tbody>")
            // the data
            while (rs!!.next()) {
                rowCount++
                htmltbl.append("<tr>")
                for (i in 0..columnCount - 1 - 1) {
                    if (i == 4) {
                        htmltbl.append("<td>" + "<input type=\"text\" name=" + rs!!.getString(i + 2) + ">" + "</td>")
                    } else {
                        htmltbl.append("<td>" + rs!!.getString(i + 1) + "</td>")
                    }
                }
                htmltbl.append("</tr>")
            }
            htmltbl.append("</tbody> </table></div>")

        } catch (ex: SQLException) {
            Logger.getLogger(JdbcPersistent::class.java.name).log(Level.SEVERE, null, ex)
            htmltbl.append("")
        }

        return htmltbl
    }

    companion object {
        var lineSeperator = System.getProperty("line.separator")
        var jTableMaxColSize = 200

        @Throws(Exception::class)
        fun writeStringToFile(StringToWrite: String, fname_path: String) {
            val newLineCorrected = StringToWrite.replace("\r\n|\n".toRegex(), lineSeperator)
            val writer = PrintWriter(fname_path, "UTF-8")
            writer.print(newLineCorrected)
            writer.close()

        }

        fun fileNameToSave(classSetting: Class<*>): String {
            return fileNameToSaveAct(classSetting)!!
        }


        fun fileNameToSave(): String {
            return fileNameToSaveAct(null)!!
        }


        private fun fileNameToSaveAct(classSetting: Class<*>?): String? {
            var path: String? = null
            val fileChooser = object : JFileChooser() {
                override fun approveSelection() {
                    val f = selectedFile
                    if (f.exists() && dialogType == SAVE_DIALOG) {
                        val result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_OPTION)
                        when (result) {
                            JOptionPane.YES_OPTION -> {
                                super.approveSelection()
                                return
                            }
                            JOptionPane.NO_OPTION -> return
                            JOptionPane.CLOSED_OPTION -> return
                            JOptionPane.CANCEL_OPTION -> {
                                cancelSelection()
                                return
                            }
                        }
                    }
                    super.approveSelection()
                }
            }
            if (classSetting != null) {
                val prf = Preferences.userNodeForPackage(classSetting)
                path = prf.get("LastDir", "")
            }
            fileChooser.dialogTitle = "Specify a file to save"
            if (path != null) fileChooser.currentDirectory = File(path)
            val userSelection = fileChooser.showSaveDialog(null)
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                val fileToSave = fileChooser.selectedFile
                return fileToSave.absolutePath
            }
            return null
        }

        @Throws(Exception::class)
        fun readStringFromFile(fname_path: String): String {
            val br = BufferedReader(java.io.FileReader(fname_path))
            try {
                val sb = StringBuilder()
                var line: String? = br.readLine()

                while (line != null) {
                    sb.append(line)
                    sb.append(lineSeperator)
                    line = br.readLine()
                }
                return sb.toString()
            } finally {
                br.close()
            }
        }
    }

}
