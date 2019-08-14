/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.milind.querylight


import oracle_xls_extract.Oracle_xls_extract
import org.fife.ui.autocomplete.AutoCompletion
import org.fife.ui.autocomplete.CompletionProvider
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.ComponentEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.beans.PropertyVetoException
import java.io.File
import java.io.StringReader
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import java.util.prefs.Preferences
import java.util.regex.Pattern
import javax.swing.*
import javax.swing.event.InternalFrameEvent
import javax.swing.event.InternalFrameListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.JTableHeader
import javax.swing.table.TableCellRenderer
import javax.swing.text.BadLocationException

/*

 */
class StmtRunScreen1(v_usr: String, v_password: String, v_jdbcstr: String, dbname: String) : javax.swing.JFrame() {

    var winList = ArrayList<windowlist>()

    private val mdi_toolbar: javax.swing.JToolBar = javax.swing.JToolBar()
    private val deskTopPane: javax.swing.JDesktopPane
    private val jMenuBar1: javax.swing.JMenuBar
    private val menu_file: javax.swing.JMenu
    private val Exit: javax.swing.JMenuItem
    private val menu_edit: javax.swing.JMenu

    inner class dbSession(val v_usr: String, val v_password: String, val v_jdbcstr: String, val dbname: String) : javax.swing.JInternalFrame() {

        val columnNames = arrayOf("-", "-", "-")
        val dataValues = arrayOf(arrayOf("-", "-", "-"))
        var tabProvider: CompletionProvider? = null
        var colProvider: CompletionProvider? = null
        var ac: AutoCompletion? = null
        var useTabProvider = true
        private var tracker: ChangeTracker? = null
        private var tbl_grid: JTable = JTable(dataValues, columnNames)
        private val header: JTableHeader = JTableHeader()

        private val chkNoPrompts : JCheckBox =   JCheckBox("No Prompts",true)
        private val cancelQuery : javax.swing.JButton = javax.swing.JButton()
        private var btn_commit: javax.swing.JButton = javax.swing.JButton()
        private var btn_execute: javax.swing.JButton = javax.swing.JButton()
        private var btn_lower: javax.swing.JButton = javax.swing.JButton()
        private var btn_reconnect: javax.swing.JButton = javax.swing.JButton()
        private var btn_rollback: javax.swing.JButton = javax.swing.JButton()
        private var btn_upper: javax.swing.JButton = javax.swing.JButton()
        private var grid_pane: javax.swing.JScrollPane = javax.swing.JScrollPane()
        private var internal_db1: javax.swing.JInternalFrame = javax.swing.JInternalFrame()
        private var jLabel1: javax.swing.JLabel = javax.swing.JLabel()

        private var jScrollPane1: javax.swing.JScrollPane = javax.swing.JScrollPane()
        private var jSplitPane1: javax.swing.JSplitPane = javax.swing.JSplitPane()
        private var jTabbedPane2: javax.swing.JTabbedPane = javax.swing.JTabbedPane()
        private var lbl_host: javax.swing.JLabel = javax.swing.JLabel()
        private var lbl_instance: javax.swing.JLabel = javax.swing.JLabel()
        private var lbl_op_status: javax.swing.JTextArea = javax.swing.JTextArea()
        private var menu_export_csv: javax.swing.JMenuItem = javax.swing.JMenuItem()
        private var menu_export_xls: javax.swing.JMenuItem = javax.swing.JMenuItem()
        private var menu_export_ins: javax.swing.JMenuItem = javax.swing.JMenuItem()

        private var menu_get_count: javax.swing.JMenuItem = javax.swing.JMenuItem()
        private var menu_tbl_copy: javax.swing.JMenuItem = javax.swing.JMenuItem()
        private var mnu_grid: javax.swing.JPopupMenu = javax.swing.JPopupMenu()
        private var toggle_qry: javax.swing.JToggleButton = javax.swing.JToggleButton();
        private var toolbar: javax.swing.JToolBar = javax.swing.JToolBar()
        private var v_host: javax.swing.JLabel = javax.swing.JLabel()
        private var v_instance: javax.swing.JLabel = javax.swing.JLabel()
        public var v_stmt_txt: RSyntaxTextArea = RSyntaxTextArea(20, 60)
        private var jScrollPane3: RTextScrollPane = RTextScrollPane(v_stmt_txt)
        private var threadMainQuery : Thread = object : Thread(){
            override fun run() {
                v_stmt_txt.cursor=  Cursor(Cursor.WAIT_CURSOR)
                btn_executeActionPerformed(null)
                v_stmt_txt.cursor = Cursor.getDefaultCursor()
            }
        }


        var jcon: JdbcPersistent? = null
            private set
        private var execute_all_flag = false
        private var lastsql: String? = null
        var currentFile: String? = null
            set(currentFile) {
                field = currentFile
                val temp = File(currentFile)
                val Path = temp.absolutePath
                val prf = Preferences.userNodeForPackage(StmtRunScreen1::class.java)
                prf.put("LastDir", Path)
            }
        private var gridFont: Font? = null
        private var editorFont: Font? = null

        init {
            val jf = this
            val prf = Preferences.userNodeForPackage(StmtRunScreen1::class.java)
            if (prf.get("fontName", null) != null) {
                gridFont = Font(prf.get("fontName", "Calibri"), prf.getInt("fontstyle", Font.PLAIN), prf.getInt("fontsize", 10))
            } else {
                gridFont = Font("Calibri", Font.PLAIN, 13)
            }

            try {
                this.isMaximizable = true
                this.isResizable = true
                val R: ResultSet

                this.jcon = JdbcPersistent(this.v_usr, this.v_password, this.v_jdbcstr, this.dbname)
                if (this.jcon != null) tracker = ChangeTracker(JdbcPersistent(this.v_usr, this.v_password, this.v_jdbcstr, this.dbname))
                Thread({
                    tabProvider = createTabCompletionProvider(JdbcPersistent(this.v_usr, this.v_password, this.v_jdbcstr, this.dbname))
                    ac = AutoCompletion(tabProvider);
                    ac?.install(v_stmt_txt ,  KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK))
                   // ac?.triggerKey =



                }).start()

                initComponents()

                val jb = JButton(this.v_usr + "@" + dbname)
                jb.border = javax.swing.border.SoftBevelBorder(1)
                jb.addActionListener { evt ->
                    for (i in winList.indices) {
                        if (winList[i].jb === evt.source) {
                            try {
                                winList[i].frmInstance.setSelected(true)
                            } catch (ex: PropertyVetoException) {
                                Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, ex)

                            }


                            break
                        }
                    }
                }
                mdi_toolbar.add(jb)
                winList.add(windowlist(this.v_usr + "@" + jcon!!.dbName, this, jb))
                this.setTitle(this.v_usr + "@" + jcon!!.dbName)
                v_stmt_txt.isCodeFoldingEnabled = true
                v_stmt_txt.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_SQL


                //v_stmt_txt.setFont(new Font("Calibri",Font.PLAIN,13));
                val prf2 = Preferences.userNodeForPackage(StmtRunScreen1::class.java)
                if (prf.get("EditorfontName", null) != null) {
                    editorFont = Font(prf2.get("EditorfontName", "Calibri"), prf2.getInt("Editorfontstyle", Font.PLAIN), prf2.getInt("Editorfontsize", 10))
                } else {
                    editorFont = Font("Calibri", Font.PLAIN, 13)
                }
                v_stmt_txt.font = editorFont
                set_icons()
                this.toggle_qry!!.isVisible = false
                this.btn_execute!!.isVisible = false
                R = jcon!!.runqry("select SYS_CONTEXT('USERENV','DB_NAME') instance_name , SYS_CONTEXT('USERENV','SERVER_HOST') server_host from dual")
                if (R.next()) {
                    this.v_instance!!.text = R.getString(1)
                    this.v_host!!.text = R.getString(2)
                }
            } catch (ex: Exception) {
                Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, ex)
            }


            val header = tbl_grid.tableHeader
            tbl_grid.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (e!!.button == MouseEvent.BUTTON3) {
                        mnu_grid!!.show(tbl_grid, e.x, e.y)
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
            tbl_grid.autoResizeMode = JTable.AUTO_RESIZE_OFF
            tbl_grid.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
            tbl_grid.cellSelectionEnabled = true

            header.addMouseListener(TableHeaderMouseListener(tbl_grid))
            this.grid_pane!!.viewport.addChangeListener(javax.swing.event.ChangeListener {
                try {
                    val viewRect = grid_pane!!.viewport.viewRect
                    if (tbl_grid != null) {
                        val first = tbl_grid.rowAtPoint(Point(0, viewRect.y))
                        if (first == -1) {
                            return@ChangeListener  // Table is empty
                        }
                        var last = tbl_grid.rowAtPoint(Point(0, viewRect.y + viewRect.height - 1))
                        if (last == -1) {
                            last = tbl_grid.rowCount - 1 // Handle empty space below last row
                        }
                        if (last == tbl_grid.rowCount - 1) {
                            try {

                                jcon!!.fetchNextRows(tbl_grid)
                            } catch (ex: SQLException) {
                                throw ex
                            }

                        } //... Last row is visible
                    }
                } catch (ex: Exception) {
                    Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, ex);
                } catch (ex: AbstractMethodError) {

                }
            })
            this.addInternalFrameListener(object : InternalFrameListener {
                override fun internalFrameOpened(ife: InternalFrameEvent) {
                    setButtonOnActivation(ife)
                }

                override fun internalFrameClosing(ife: InternalFrameEvent) {
                    jf.dispose()
                }

                override fun internalFrameClosed(ife: InternalFrameEvent) {
                    removeOnDeActivation(ife)
                    jf.dispose()
                    jf.removeAll()
                }

                override fun internalFrameIconified(ife: InternalFrameEvent) {

                }

                override fun internalFrameDeiconified(ife: InternalFrameEvent) {

                }

                override fun internalFrameActivated(ife: InternalFrameEvent) {

                    setButtonOnActivation(ife)
                }

                override fun internalFrameDeactivated(ife: InternalFrameEvent) {

                }
            })
            try {
                this.setSelected(true)
            } catch (ex: PropertyVetoException) {
                // Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
            }

            this.isClosable = true
            this.isMaximizable = true

        }

        private fun removeOnDeActivation(ife: InternalFrameEvent) {
            for (i in winList.indices) {
                if (winList[i].frmInstance === ife.internalFrame) {
                    mdi_toolbar.remove(winList[i].jb)
                    mdi_toolbar.revalidate()
                    mdi_toolbar.repaint()
                    winList.removeAt(i)
                }
            }
        }

        private fun setButtonOnActivation(ife: InternalFrameEvent) {
            for (i in winList.indices) {
                if (winList[i].frmInstance === ife.internalFrame) {
                    winList[i].jb.isOpaque = true
                    winList[i].jb.background = Color.decode("#FFFFE0")
                    winList[i].jb.isEnabled = false
                } else {
                    winList[i].jb.isOpaque = true
                    winList[i].jb.isEnabled = true
                    winList[i].jb.background = Color.GRAY
                }

            }
        }

        inner class TableHeaderMouseListener(private val table: JTable) : MouseAdapter() {

            override fun mouseClicked(event: MouseEvent?) {
                if (event!!.clickCount == 2) {
                    val point = event.point
                    val column_name = table.getColumnName(table.columnAtPoint(point))
                    val stringSelection = StringSelection(column_name)
                    val clpbrd = Toolkit.getDefaultToolkit().systemClipboard
                    clpbrd.setContents(stringSelection, null)
                }
            }
        }

        /**
         * This method is called from within the constructor to initialize the
         * form. WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        private // <editor-fold defaultstate="collapsed" desc="Generated Code">
        fun initComponents() {

            mnu_grid = javax.swing.JPopupMenu()
            menu_tbl_copy = javax.swing.JMenuItem()
            menu_get_count = javax.swing.JMenuItem()
            menu_export_ins = javax.swing.JMenuItem()
            menu_export_csv = javax.swing.JMenuItem()
            menu_export_xls = javax.swing.JMenuItem()

            internal_db1 = this
            lbl_instance = javax.swing.JLabel()
            v_instance = javax.swing.JLabel()
            lbl_host = javax.swing.JLabel()
            v_host = javax.swing.JLabel()
            jSplitPane1 = javax.swing.JSplitPane()
            jTabbedPane2 = javax.swing.JTabbedPane()
            grid_pane = javax.swing.JScrollPane()
            jScrollPane1 = javax.swing.JScrollPane()
            lbl_op_status = javax.swing.JTextArea()
            jLabel1 = javax.swing.JLabel()
            toolbar = javax.swing.JToolBar()
            btn_reconnect = javax.swing.JButton()
            btn_upper = javax.swing.JButton()
            btn_lower = javax.swing.JButton()
            btn_commit = javax.swing.JButton()
            btn_rollback = javax.swing.JButton()
            btn_execute = javax.swing.JButton()
            toggle_qry = javax.swing.JToggleButton()

            menu_tbl_copy.icon = javax.swing.ImageIcon(javaClass.getResource("/Clone-50.png")) // NOI18N
            menu_tbl_copy.text = "Copy"
            menu_tbl_copy.addActionListener { evt -> menu_tbl_copyActionPerformed(evt) }
            mnu_grid.add(menu_tbl_copy)

            menu_get_count!!.icon = javax.swing.ImageIcon(javaClass.getResource("/View Details-50.png")) // NOI18N
            menu_get_count!!.text = "Record Count"
            menu_get_count!!.addActionListener { evt -> menu_get_countActionPerformed(evt) }
            mnu_grid!!.add(menu_get_count)

            menu_export_ins!!.icon = javax.swing.ImageIcon(javaClass.getResource("/SQL-50.png")) // NOI18N
            menu_export_ins!!.text = "Export : Insert Statment"
            menu_export_ins!!.addActionListener { evt -> menu_export_insActionPerformed(evt) }
            mnu_grid!!.add(menu_export_ins)

            menu_export_csv!!.icon = javax.swing.ImageIcon(javaClass.getResource("/CSV-50.png")) // NOI18N
            menu_export_csv!!.text = "Export : CSV"
            menu_export_csv!!.addActionListener { evt -> menu_export_csvActionPerformed(evt) }
            mnu_grid!!.add(menu_export_csv)
            menu_export_xls!!.text = "Export : Excel"
            menu_export_xls!!.addActionListener { evt -> menu_export_xlsActionPerformed(evt) }
            mnu_grid!!.add(menu_export_xls)

            val tblResize = javax.swing.JMenuItem("Resize to fit Text")
            tblResize.addActionListener { jcon!!.resize_tbl_cols(tbl_grid, 3000) }
            mnu_grid!!.add(tblResize)

            val fetchAll = javax.swing.JMenuItem("Fetch All Rows")
            fetchAll.addActionListener {
                try {
                    while (1 == 1) {
                        val rowCount = tbl_grid.rowCount;
                        jcon?.fetchNextRows(tbl_grid);
                        if (rowCount == tbl_grid.rowCount) break; }
                } catch(e: Exception) {
                }
            }
            mnu_grid!!.add(fetchAll)

            val exportToGrid = javax.swing.JMenuItem("Export to data Grid")
            exportToGrid.addActionListener { actionExportToGrid() }
            mnu_grid!!.add(exportToGrid)

            defaultCloseOperation = javax.swing.WindowConstants.EXIT_ON_CLOSE
            addKeyListener(object : java.awt.event.KeyAdapter() {
                override fun keyPressed(evt: java.awt.event.KeyEvent?) {
                    formKeyPressed(evt as KeyEvent)
                }
            })

            this.isVisible = true

            lbl_instance!!.font = java.awt.Font("Tahoma", 1, 11) // NOI18N
            lbl_instance!!.text = "Instance : "

            v_instance!!.text = " "

            lbl_host!!.font = java.awt.Font("Tahoma", 1, 11) // NOI18N
            lbl_host!!.text = "Host :"

            v_host!!.text = " "

            jSplitPane1!!.dividerLocation = 350
            jSplitPane1!!.orientation = javax.swing.JSplitPane.VERTICAL_SPLIT
            jSplitPane1!!.addComponentListener(object : java.awt.event.ComponentAdapter() {
                override fun componentResized(evt: java.awt.event.ComponentEvent?) {
                    jSplitPane1ComponentResized(evt as ComponentEvent)
                }
            })

            v_stmt_txt!!.addKeyListener(object : java.awt.event.KeyAdapter() {
                override fun keyPressed(evt: java.awt.event.KeyEvent?) {
                    v_stmt_txtKeyPressed(evt as KeyEvent)
                }

                override fun keyTyped(e: KeyEvent?) {
                    v_stmt_txtKeyTyped(e as KeyEvent)
                }
            })
            jScrollPane3!!.setViewportView(v_stmt_txt)

            jSplitPane1!!.leftComponent = jScrollPane3

            jTabbedPane2!!.name = "" // NOI18N
            jTabbedPane2!!.addComponentListener(object : java.awt.event.ComponentAdapter() {
                override fun componentResized(evt: java.awt.event.ComponentEvent?) {
                    jTabbedPane2ComponentResized(evt as ComponentEvent)
                }
            })

            grid_pane!!.toolTipText = ""
            grid_pane!!.preferredSize = java.awt.Dimension(1000, 100)
            jTabbedPane2!!.addTab("Grid", grid_pane)

            lbl_op_status!!.isEditable = false
            lbl_op_status!!.background = java.awt.Color(204, 204, 204)
            lbl_op_status!!.columns = 20
            lbl_op_status!!.rows = 5
            jScrollPane1!!.setViewportView(lbl_op_status)

            jTabbedPane2!!.addTab("Script Output ", jScrollPane1)

            jSplitPane1!!.rightComponent = jTabbedPane2

            jLabel1!!.font = java.awt.Font("Tahoma", 1, 11) // NOI18N
            jLabel1!!.foreground = java.awt.Color(0, 102, 204)
            jLabel1!!.text = "Press (Alt+Enter) to run query ,  F5 to run update , delete insert (DML) , Shift + F5 to run all text as script"

            toolbar!!.isFloatable = false
            toolbar!!.isRollover = true

            btn_reconnect!!.icon = javax.swing.ImageIcon(javaClass.getResource("/Connected-50.png")) // NOI18N
            btn_reconnect!!.toolTipText = "Reconnect"
            btn_reconnect!!.isFocusable = false
            btn_reconnect!!.horizontalTextPosition = javax.swing.SwingConstants.CENTER
            btn_reconnect!!.verticalTextPosition = javax.swing.SwingConstants.BOTTOM
            btn_reconnect!!.addActionListener { evt -> btn_reconnectActionPerformed(evt) }
            toolbar!!.add(btn_reconnect)

            btn_upper!!.icon = javax.swing.ImageIcon(javaClass.getResource("/Up Arrow-50.png")) // NOI18N
            btn_upper!!.toolTipText = "UpperCase"
            btn_upper!!.isFocusable = false
            btn_upper!!.horizontalTextPosition = javax.swing.SwingConstants.CENTER
            btn_upper!!.verticalTextPosition = javax.swing.SwingConstants.BOTTOM
            btn_upper!!.addActionListener { evt -> btn_upperActionPerformed(evt) }
            toolbar!!.add(btn_upper)

            btn_lower!!.icon = javax.swing.ImageIcon(javaClass.getResource("/Down Arrow-50.png")) // NOI18N
            btn_lower!!.toolTipText = "LowerCase"
            btn_lower!!.isFocusable = false
            btn_lower!!.horizontalTextPosition = javax.swing.SwingConstants.CENTER
            btn_lower!!.verticalTextPosition = javax.swing.SwingConstants.BOTTOM
            btn_lower!!.addActionListener { evt -> btn_lowerActionPerformed(evt) }
            toolbar!!.add(btn_lower)

            btn_commit!!.icon = javax.swing.ImageIcon(javaClass.getResource("/Accept Database-50.png")) // NOI18N
            btn_commit!!.toolTipText = "Commit"
            btn_commit!!.isFocusable = false
            btn_commit!!.horizontalTextPosition = javax.swing.SwingConstants.CENTER
            btn_commit!!.verticalTextPosition = javax.swing.SwingConstants.BOTTOM
            btn_commit!!.addActionListener { evt -> btn_commitActionPerformed(evt) }
            toolbar!!.add(btn_commit)

            cancelQuery.text="Cancel Query"
            cancelQuery.foreground = Color.RED
            cancelQuery.repaint()
            cancelQuery!!.horizontalTextPosition = javax.swing.SwingConstants.CENTER
            cancelQuery!!.verticalTextPosition = javax.swing.SwingConstants.BOTTOM
            cancelQuery!!.addActionListener { evt -> cancelQueryActionPerformed(evt) }


            btn_rollback!!.icon = javax.swing.ImageIcon(javaClass.getResource("/Delete Database-50.png")) // NOI18N
            btn_rollback!!.toolTipText = "Rollback changes"
            btn_rollback!!.isFocusable = false
            btn_rollback!!.horizontalTextPosition = javax.swing.SwingConstants.CENTER
            btn_rollback!!.verticalTextPosition = javax.swing.SwingConstants.BOTTOM
            btn_rollback!!.addActionListener { evt -> btn_rollbackActionPerformed(evt) }
            toolbar!!.add(btn_rollback)



            chkNoPrompts.toolTipText="Set Variable Prompting On / Off"
            chkNoPrompts.horizontalTextPosition=javax.swing.SwingConstants.RIGHT
            chkNoPrompts.addActionListener {  }
            toolbar?.add(chkNoPrompts)
            toolbar?.add(cancelQuery)

            btn_execute!!.text = "Execute"
            btn_execute!!.addActionListener { evt -> btn_executeActionPerformed(evt) }
            toolbar!!.add(btn_execute)

            toggle_qry!!.text = "Query Mode"
            toggle_qry!!.addActionListener { evt -> toggle_qryActionPerformed(evt) }
            toolbar!!.add(toggle_qry)

            val internal_db1Layout = javax.swing.GroupLayout(this.contentPane)
            this.contentPane.layout = internal_db1Layout
            internal_db1Layout.setHorizontalGroup(
                    internal_db1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(internal_db1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(internal_db1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(toolbar!!, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt())
                                            .addGroup(internal_db1Layout.createSequentialGroup()
                                                    .addComponent(lbl_instance)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(v_instance!!, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(lbl_host)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(v_host!!, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jLabel1)
                                                    .addGap(0, 77, java.lang.Short.MAX_VALUE.toInt()))
                                            .addComponent(jSplitPane1!!, javax.swing.GroupLayout.PREFERRED_SIZE, 0, java.lang.Short.MAX_VALUE.toInt()))
                                    .addContainerGap())
            )
            internal_db1Layout.setVerticalGroup(
                    internal_db1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(internal_db1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(toolbar!!, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(internal_db1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(lbl_instance)
                                            .addComponent(v_instance)
                                            .addComponent(lbl_host)
                                            .addComponent(v_host)
                                            .addComponent(jLabel1))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jSplitPane1!!, javax.swing.GroupLayout.DEFAULT_SIZE, 610, java.lang.Short.MAX_VALUE.toInt())
                                    .addContainerGap())
            )

            val deskTopPaneLayout = javax.swing.GroupLayout(deskTopPane)
            deskTopPane.layout = deskTopPaneLayout
            deskTopPaneLayout.setHorizontalGroup(
                    deskTopPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(deskTopPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(this)
                                    .addContainerGap())
            )
            deskTopPaneLayout.setVerticalGroup(
                    deskTopPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(deskTopPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(this)
                                    .addContainerGap())
            )
            //deskTopPane.setLayer(this, javax.swing.JLayeredPane.DEFAULT_LAYER);

            pack()
        }// </editor-fold>

        private fun btn_executeActionPerformed(evt: java.awt.event.ActionEvent?) {
            val R: ResultSet
            var sql_stmt: String? = null
            val offset_semi_Colon = 0
            var intermediate: String? = null
            var result: String? = null
            var executeSelected = false
            if (v_stmt_txt!!.selectedText != null && v_stmt_txt!!.selectedText.length > 1) {
                executeSelected = true
            }
            var initText: StringBuffer? = null
            try {
                initText = StringBuffer(if (executeSelected) v_stmt_txt.selectedText else v_stmt_txt.document.getText(0, v_stmt_txt.document.length))
            } catch (ex: BadLocationException) {
                Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, ex)
            }

            try {

                val SR = ScriptRunner(this.jcon!!.conn, false, false)
                val bindVars = ArrayList<JLabel>()
                val bindTxtBoxes = ArrayList<JTextField>()
                val comps = ArrayList<JComponent>()
                val bindValues = ArrayList<String>()
                val bindName = ArrayList<String>()
                var bindVarExists = false

                if (tbl_grid != null) {
                    this.grid_pane.viewport.remove(tbl_grid)
                    this.grid_pane.viewport.validate()
                }
                if (!executeSelected) {
                    val caretPos = v_stmt_txt.caretPosition
                    var stmt_pos = initText!!.indexOf("\n", caretPos)
                    if (stmt_pos < 0) {
                        stmt_pos = initText.length - 1
                    }

                    intermediate = initText.insert(stmt_pos, "`~~~`").toString().replace("\n".toRegex(), "~``~")
                    val sqlStatements = intermediate.split("~``~\\s*~``~".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (i in sqlStatements.indices) {
                        if (sqlStatements[i].contains("`~~~`")) {
                            result = (sqlStatements[i] + "~``~").replace("`~~~`", "").replace("--.*?~``~".toRegex(), " ").replace("~``~".toRegex(), " ")
                        }
                    }
                }
                val tmp_sql_stmt = if (executeSelected) initText!!.toString() else result
                val end_pos = if (tmp_sql_stmt?.lastIndexOf(";", tmp_sql_stmt.length - 1)!! > 0) tmp_sql_stmt.lastIndexOf(";", tmp_sql_stmt.length - 1) else tmp_sql_stmt.length
                val istream: StringReader
                istream = if (this.execute_all_flag) StringReader(initText!!.toString()) else StringReader(tmp_sql_stmt)
                if (toggle_qry.text == "Query Mode") {
                    sql_stmt = tmp_sql_stmt.substring(0, end_pos)

                    val patt = Pattern.compile("(:\\S+\\s*(?=([^']*'[^']*')*[^']*$))")
                    val mat = patt.matcher(sql_stmt + " ")
                    var counter = 0
                    OUTER@ while (mat.find()) {
                        bindVarExists = true
                        for (i in bindVars.indices) {
                            if (mat.group(1).trim { it <= ' ' } == bindVars[i].text.trim { it <= ' ' }) {
                                continue@OUTER
                            }
                        }
                        bindVars.add(JLabel(mat.group(1)))
                        val bindname = mat.group(1).trim { it <= ' ' }
                        bindName.add(bindname)
                        val bindvalue: String? = queryPropValue(bindname)
                        bindTxtBoxes.add(JTextField(bindvalue))
                        comps.add(bindVars[counter])
                        comps.add(bindTxtBoxes[counter])
                        counter++
                    }
                    if (bindVarExists &&  !chkNoPrompts.isSelected() ) {
                        val defineoff = javax.swing.JCheckBox("Set Define off")
                        comps.add(defineoff)
                        JOptionPane.showMessageDialog(null, comps.toTypedArray(), "Enter Bind Values", JOptionPane.PLAIN_MESSAGE)
                        for (i in bindVars.indices) {
                            bindValues.add(bindTxtBoxes[i].text)
                            val pv: PropertyValue = PropertyValue(bindName[i], bindTxtBoxes[i].text.trim())
                        }
                        if (defineoff.isSelected) {
                            bindVarExists = false
                            jcon!!.TableFromDatabase(tbl_grid, sql_stmt)
                        } else {
                            jcon!!.TableFromDatabase(tbl_grid, sql_stmt, bindValues, bindName)
                        }
                    } else {
                        jcon!!.TableFromDatabase(tbl_grid, sql_stmt)
                    }
                    if (gridFont != null) {
                        tbl_grid!!.font = gridFont
                    }
                    tbl_grid!!.addKeyListener(ClipboardKeyAdapter(tbl_grid))
                    this.lastsql = sql_stmt

                    this.grid_pane!!.viewport.add(tbl_grid)
                    this.jTabbedPane2!!.selectedIndex = 0
                } else {
                    this.jTabbedPane2!!.selectedIndex = 1
                    if (this.execute_all_flag) {
                        if (initText!!.toString().contains("end;")) {
                            this.lbl_op_status!!.text = SR.runScript(initText.toString(), null, null).toString()
                        } else {
                            val Result = SR.runScript(istream).toString()
                            this.lbl_op_status!!.text = Result
                        }
                    } else {
                        sql_stmt = tmp_sql_stmt.substring(0, end_pos)

                        val patt = Pattern.compile("(:\\S+\\s*(?=([^']*'[^']*')*[^']*$))")
                        val mat = patt.matcher(sql_stmt.toLowerCase() + " ")
                        var counter = 0
                        OUTER@ while (mat.find()) {
                            bindVarExists = true
                            for (i in bindVars.indices) {
                                if (mat.group(1).trim { it <= ' ' } == bindVars[i].text.trim { it <= ' ' }) {
                                    continue@OUTER
                                }
                            }
                            bindVars.add(JLabel(mat.group(1)))
                            val bindname = mat.group(1).trim { it <= ' ' }
                            bindName.add(bindname)
                            val bindvalue: String? = queryPropValue(bindname)
                            bindTxtBoxes.add(JTextField(bindvalue))
                            comps.add(bindVars[counter])
                            comps.add(bindTxtBoxes[counter])
                            counter++
                        }
                        if (bindVarExists && !chkNoPrompts.isSelected()) {
                            val defineoff = javax.swing.JCheckBox("Set Define off")
                            comps.add(defineoff)
                            JOptionPane.showMessageDialog(null, comps.toTypedArray(), "Enter Bind Values", JOptionPane.PLAIN_MESSAGE)
                            for (i in bindVars.indices) {
                                bindValues.add(bindTxtBoxes[i].text)
                                val pv: PropertyValue = PropertyValue(bindName[i], bindTxtBoxes[i].text.trim())
                            }
                            if (defineoff.isSelected) {
                                bindVarExists = false
                                bindValues.clear()
                                bindName.clear()
                            }
                        }
                        val addcolon = if (sql_stmt.toLowerCase().contains("begin") && sql_stmt.toLowerCase().contains("end")) ";" else ""
                        this.lbl_op_status!!.text = SR.runScript(sql_stmt + addcolon, bindValues, bindName).toString()

                    }
                }
            } catch (ex: Exception) {
                this.show_alert(" Error : " + ex.toString()
                        + "\nIf you are trying to run DML or PLSQL please use F5")
                Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, ex.message)
            }

        }

        internal var tableCellRenderer: TableCellRenderer = object : DefaultTableCellRenderer() {
            internal var f = SimpleDateFormat("dd/mmm/yyyy")

            override fun getTableCellRendererComponent(table: JTable?,
                                                       value: Any, isSelected: Boolean, hasFocus: Boolean,
                                                       row: Int, column: Int): Component {
                var value = value
                if (value is Date) {
                    value = f.format(value)
                }
                return super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column)
            }
        }

        private fun resize_tbl_cols(table: JTable) {
            val columnModel = table.columnModel
            var renderer: TableCellRenderer
            var width: Int // Min width
            val tableCellRenderer = object : DefaultTableCellRenderer() {
                internal var f = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")

                override fun getTableCellRendererComponent(table: JTable?,
                                                           value: Any, isSelected: Boolean, hasFocus: Boolean,
                                                           row: Int, column: Int): Component {
                    var value = value
                    if (value is Date) {
                        value = f.format(value)
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column)
                }
            }
            for (column in 0..table.columnCount - 1) {
                val tableColumn = columnModel.getColumn(column)
                //  width = tableColumn.getPreferredWidth();
                val headerRenderer = table.tableHeader.defaultRenderer
                val headerValue = tableColumn.headerValue
                val headerComp = headerRenderer.getTableCellRendererComponent(table, headerValue, false, false, 0, column)
                width = headerComp.preferredSize.width
                table.columnModel.getColumn(column).cellRenderer = tableCellRenderer
                for (row in 0..table.rowCount) {
                    renderer = table.getCellRenderer(row, column)
                    var comp: Component? = null
                    try {
                        comp = table.prepareRenderer(renderer, row, column)
                        width = Math.min(200, Math.max(comp!!.preferredSize.width, width))
                    } catch (e: Exception) {

                    }

                }
                columnModel.getColumn(column).preferredWidth = width
            }
        }

        private fun toggle_qryActionPerformed(evt: java.awt.event.ActionEvent) {
            if (!toggle_qry!!.isSelected) {
                toggle_qry!!.text = "Query Mode"
            } else {
                toggle_qry!!.text = "Script Mode"
            }
        }

        private fun btn_reconnectActionPerformed(evt: java.awt.event.ActionEvent) {
            try {
                jcon!!.reconnect()
            } catch (ex: Exception) {
                show_alert(ex.toString())
            }

        }

        private fun btn_rollbackActionPerformed(evt: java.awt.event.ActionEvent) {
            try {
                jcon!!.conn!!.rollback()
            } catch (ex: SQLException) {
                show_alert(ex.toString())
            }

        }

        private fun menu_get_countActionPerformed(evt: java.awt.event.ActionEvent) {
            val rec_cnt: String
            if (this.lastsql != null) {
                try {
                    val cnt_qry = "select count(1) from (" + this.lastsql + ")"
                    rec_cnt = jcon!!.returnSingleField(cnt_qry)!!
                    show_msg("Record Count : " + rec_cnt)
                } catch (ex: Exception) {
                    show_alert(ex.toString())
                }

            }
        }

        private fun btn_commitActionPerformed(evt: java.awt.event.ActionEvent) {
            try {
                jcon!!.conn!!.commit()
            } catch (ex: SQLException) {
                show_alert(ex.toString())
            }

        }
        private fun cancelQueryActionPerformed(evt: java.awt.event.ActionEvent) {
            try {
                if (this.threadMainQuery.isAlive) {
                    this.threadMainQuery.interrupt()
                    v_stmt_txt.cursor=Cursor.getDefaultCursor()
                    this.jcon?.stmt?.cancel()
                }

            } catch (e: Exception ) {
                this.show_msg("Error occured while cancelling : ${e.message}")
            }
        }

        private fun jTabbedPane2ComponentResized(evt: java.awt.event.ComponentEvent) {

        }

        private fun jSplitPane1ComponentResized(evt: java.awt.event.ComponentEvent) {

        }

        private fun ExitActionPerformed(evt: java.awt.event.ActionEvent) {
            // TODO add your handling code here:
        }

        private fun menu_export_insActionPerformed(evt: java.awt.event.ActionEvent) {
            val patt = Pattern.compile("select(.*)from(\\s+[^\\s]+\\s)")
            val mat = patt.matcher(lastsql!!.toLowerCase() + " ")
            var tablename: String? = null
            if (mat.find()) {
                tablename = mat.group(2)
                JOptionPane.showInputDialog(this, "Enter table_name for the extract", tablename)
            }

            try {
                val ins_script = jcon!!.genInsFromQuery(lastsql!!, tablename!!)
                val stringSelection = StringSelection(ins_script.toString())
                val clpbrd = Toolkit.getDefaultToolkit().systemClipboard
                clpbrd.setContents(stringSelection, null)
            } catch (ex: Exception) {
                Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, ex)
                show_alert(ex.toString())
            }

        }

        private fun menu_export_csvActionPerformed(evt: java.awt.event.ActionEvent) {
            val options = arrayOf<Any>("ClipBoard", "File", "Cancel")
            val n = JOptionPane.showOptionDialog(this, "Export the data to : ", "Clipboard / File ?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2])
            if (n == JOptionPane.CANCEL_OPTION) {
                return
            } else if (n == JOptionPane.YES_OPTION) {
                try {
                    val CSV = jcon!!.genCSVfrmQuery(lastsql!!)
                    val stringSelection = StringSelection(CSV.toString())
                    val clpbrd = Toolkit.getDefaultToolkit().systemClipboard
                    clpbrd.setContents(stringSelection, null)
                } catch (ex: Exception) {
                    this.show_alert(ex.toString())
                }

            } else {
                val tmp = fileNameToSave()
                if (tmp != null) {
                    val fname = if (tmp.toLowerCase().endsWith(".csv")) tmp else tmp + ".csv"
                    try {
                        jcon!!.genCSVfilefrmQuery(lastsql!!, fname)
                    } catch (ex: Exception) {
                        this.show_alert(ex.toString())
                    }

                }
            }

        }

        private fun menu_export_xlsActionPerformed(evt: java.awt.event.ActionEvent) {

            val tmp = fileNameToSave()
            if (tmp != null) {
               Thread {
                   val fname = if (tmp.toLowerCase().endsWith(".xlsx")) tmp else tmp + ".xlsx"
                   try {
                       //jcon.genCSVfilefrmQuery(lastsql, fname);
                       Oracle_xls_extract.main(arrayOf<String>("-u", jcon!!.uid!!, "-p", jcon!!.pwd!!, "-j", jcon!!.jdbcstr!!, "-q", lastsql!!, "-r", fname))
                       if (File(fname).isFile && File(fname).length() > 0) {
                           this.show_msg("File : $fname created")
                       }
                   } catch (ex: Exception) {
                       this.show_alert(ex.toString())
                   }
               }.start()

            }

        }

        private fun btn_upperActionPerformed(evt: java.awt.event.ActionEvent?) {
            try {
                this.v_stmt_txt!!.replaceSelection(this.v_stmt_txt!!.selectedText.toUpperCase())
                this.v_stmt_txt!!.selectedTextColor = Color.yellow
            } catch (e: Exception) {
            }

        }

        private fun btn_lowerActionPerformed(evt: java.awt.event.ActionEvent?) {
            try {
                this.v_stmt_txt!!.replaceSelection(this.v_stmt_txt!!.selectedText.toLowerCase())
            } catch (e: Exception) {
            }

        }

        private fun formKeyPressed(evt: java.awt.event.KeyEvent) {
            // TODO add your handling code here:
        }


        private fun getCurrentSql(): String {
            val R: ResultSet
            var lastWordAtCaret: String
            var sql_stmt: String? = null
            val offset_semi_Colon = 0
            var intermediate: String? = null
            var result: String? = null
            var executeSelected = false
            if (v_stmt_txt!!.selectedText != null && v_stmt_txt!!.selectedText.length > 1) {
                val patt = Pattern.compile("(.+)(\\s\\S.+)$", Pattern.DOTALL)
                val mat = patt.matcher(sql_stmt + " ")
                val strTillCaret = v_stmt_txt!!.selectedText
                lastWordAtCaret = if (mat.find()) mat.group(2).trim().toUpperCase() else ""
            } else {
                try {
                    val patt = Pattern.compile("(.+)(\\s\\S.+)$", Pattern.DOTALL)
                    val mat = patt.matcher(sql_stmt + " ")
                    val strTillCaret = v_stmt_txt!!.document.getText(0, v_stmt_txt!!.document.length)
                    lastWordAtCaret = if (mat.find()) mat.group(2).trim().toUpperCase() else ""
                } catch (ex: BadLocationException) {
                    Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, ex)
                }

            }
            var initText: StringBuffer? = null
            try {
                initText = StringBuffer(if (executeSelected) v_stmt_txt.selectedText else v_stmt_txt.document.getText(0, v_stmt_txt.document.length))
            } catch (ex: BadLocationException) {
                Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, ex)
            }

            try {


                val bindVars = ArrayList<JLabel>()
                val bindTxtBoxes = ArrayList<JTextField>()
                val comps = ArrayList<JComponent>()
                val bindValues = ArrayList<String>()
                val bindName = ArrayList<String>()
                var bindVarExists = false

                if (tbl_grid != null) {
                    this.grid_pane.viewport.remove(tbl_grid)
                    this.grid_pane.viewport.validate()
                }
                if (!executeSelected) {
                    val caretPos = v_stmt_txt.caretPosition
                    var stmt_pos = initText!!.indexOf("\n", caretPos)
                    if (stmt_pos < 0) {
                        stmt_pos = initText.length - 1
                    }

                    intermediate = initText.insert(stmt_pos, "`~~~`").toString().replace("\n".toRegex(), "~``~")
                    val sqlStatements = intermediate.split("~``~\\s*~``~".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (i in sqlStatements.indices) {
                        if (sqlStatements[i].contains("`~~~`")) {
                            result = (sqlStatements[i] + "~``~").replace("`~~~`", "").replace("--.*?~``~".toRegex(), " ").replace("~``~".toRegex(), " ")
                        }
                    }
                }

                val tmp_sql_stmt = if (executeSelected) initText!!.toString() else result
                val end_pos = if (tmp_sql_stmt?.lastIndexOf(";", tmp_sql_stmt.length - 1)!! > 0) tmp_sql_stmt.lastIndexOf(";", tmp_sql_stmt.length - 1) else tmp_sql_stmt.length
                val istream: StringReader
                istream = if (this.execute_all_flag) StringReader(initText!!.toString()) else StringReader(tmp_sql_stmt)
                sql_stmt = tmp_sql_stmt.substring(0, end_pos)
                return sql_stmt;
            } catch (e: Exception) {
                Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, e)
                return ""
            }

        }


        private fun autoGenerateCode() {
            val R: ResultSet
            var lastWordAtCaret: String
            var sql_stmt: String? = null
            val offset_semi_Colon = 0
            var intermediate: String? = null
            var result: String? = null
            var executeSelected = false
            if (v_stmt_txt!!.selectedText == null || v_stmt_txt!!.selectedText.length < 2) {
                if (v_stmt_txt!!.selectedText != null && v_stmt_txt!!.selectedText.length > 1) {
                    val patt = Pattern.compile("(.+)(\\s\\S.+)$", Pattern.DOTALL)
                    val mat = patt.matcher(sql_stmt + " ")
                    val strTillCaret = v_stmt_txt!!.selectedText
                    lastWordAtCaret = if (mat.find()) mat.group(2).trim().toUpperCase() else ""
                } else {
                    try {
                        val patt = Pattern.compile("(.+)(\\s\\S.+)$", Pattern.DOTALL)
                        val mat = patt.matcher(sql_stmt + " ")
                        val strTillCaret = v_stmt_txt!!.document.getText(0, v_stmt_txt!!.document.length)
                        lastWordAtCaret = if (mat.find()) mat.group(2).trim().toUpperCase() else ""
                    } catch (ex: BadLocationException) {
                        Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, ex)
                    }

                }
                var initText: StringBuffer? = null
                try {
                    initText = StringBuffer(if (executeSelected) v_stmt_txt.selectedText else v_stmt_txt.document.getText(0, v_stmt_txt.document.length))
                } catch (ex: BadLocationException) {
                    Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, ex)
                }

                try {

                   // val SR = ScriptRunner(this.jcon!!.conn, false, false)
                    val bindVars = ArrayList<JLabel>()
                    val bindTxtBoxes = ArrayList<JTextField>()
                    val comps = ArrayList<JComponent>()
                    val bindValues = ArrayList<String>()
                    val bindName = ArrayList<String>()
                    var bindVarExists = false

                    if (tbl_grid != null) {
                        this.grid_pane.viewport.remove(tbl_grid)
                        this.grid_pane.viewport.validate()
                    }
                    if (!executeSelected) {
                        val caretPos = v_stmt_txt.caretPosition
                        var stmt_pos = initText!!.indexOf("\n", caretPos)
                        if (stmt_pos < 0) {
                            stmt_pos = initText.length - 1
                        }

                        intermediate = initText.insert(stmt_pos, "`~~~`").toString().replace("\n".toRegex(), "~``~")
                        val sqlStatements = intermediate.split("~``~\\s*~``~".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        for (i in sqlStatements.indices) {
                            if (sqlStatements[i].contains("`~~~`")) {
                                result = (sqlStatements[i] + "~``~").replace("`~~~`", "").replace("--.*?~``~".toRegex(), " ").replace("~``~".toRegex(), " ")
                            }
                        }
                    }

                    val tmp_sql_stmt = if (executeSelected) initText!!.toString() else result
                    val end_pos = if (tmp_sql_stmt?.lastIndexOf(";", tmp_sql_stmt.length - 1)!! > 0) tmp_sql_stmt.lastIndexOf(";", tmp_sql_stmt.length - 1) else tmp_sql_stmt.length
                    val istream: StringReader
                    istream = if (this.execute_all_flag) StringReader(initText!!.toString()) else StringReader(tmp_sql_stmt)
                    if (toggle_qry.text == "Query Mode") {
                        sql_stmt = tmp_sql_stmt.substring(0, end_pos)
                        var whereClause: String = jcon!!.buildWhereClause(sql_stmt)
                        v_stmt_txt.insert(whereClause, v_stmt_txt.caretPosition)
                    }
                } catch (e: Exception) {
                    Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, e)
                }
            } else {
               val  fullText = v_stmt_txt.document.getText(0, v_stmt_txt.document.length)
                val caretPos = v_stmt_txt.caretPosition
                val textAfterCursor = fullText.substring(caretPos,fullText.length)

                val patt = Pattern.compile( "(\\s+)(\\S+)(\\s*(,|where))", Pattern.DOTALL)
                val mat = patt.matcher(textAfterCursor + " ")
                val tabAlias = if (mat.find()) mat.group(2).trim() else null
                val tabName = v_stmt_txt.selectedText
                val columnList =  jcon!!.popColumnList(tabName.toUpperCase(),tabAlias)
                val insertPos : Int = fullText.lastIndexOf("from",caretPos,true) - 1
                v_stmt_txt.selectionStart=insertPos
                v_stmt_txt.selectionEnd=insertPos
                v_stmt_txt.insert(columnList,insertPos)
            }

        }

        private fun v_stmt_txtKeyTyped(evt: java.awt.event.KeyEvent)
        {
            if (evt.keyChar == '.'  ) {


                tracker?.trackChanges(getCurrentSql(),"COL")
                colProvider = tracker?.provider
                val tmp = AutoCompletion(colProvider);
                tmp?.install(v_stmt_txt,KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK))
                tmp.doCompletion()


            }
        }

        private fun v_stmt_txtKeyPressed(evt: java.awt.event.KeyEvent) {
            this.cursor = java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR)



            if (evt.keyCode == KeyEvent.VK_ENTER) {
                if (evt.modifiers and KeyEvent.ALT_MASK != 0 && evt.keyCode == KeyEvent.VK_ENTER) {
                    toggle_qry!!.text = "Query Mode"
                    //  this.btn_executeActionPerformed(null)
                    if (threadMainQuery.state == Thread.State.TERMINATED || threadMainQuery.isInterrupted || threadMainQuery.state == Thread.State.NEW)
                    {
                        threadMainQuery = object : Thread() {
                            override fun run() {
                                v_stmt_txt.cursor = Cursor(Cursor.WAIT_CURSOR)
                                btn_executeActionPerformed(null)
                                v_stmt_txt.cursor = Cursor.getDefaultCursor()
                            }

                        }
                    threadMainQuery.start()
                }
            }
            } else if (evt.keyCode == KeyEvent.VK_TAB && evt.modifiers and KeyEvent.CTRL_MASK != 0 ) {
                autoGenerateCode()
            }   else if (evt.keyCode == KeyEvent.VK_PERIOD )
            {
                ac?.install(v_stmt_txt , KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK))
            } else if (evt.keyCode == KeyEvent.VK_F5 && evt.modifiers and KeyEvent.SHIFT_MASK != 0) {
                toggle_qry!!.text = "Script Mode"
                this.execute_all_flag = true
               // this.btn_executeActionPerformed(null)
                threadMainQuery.start()
                this.execute_all_flag = false
            } else if (evt.keyCode == KeyEvent.VK_F5) {
                toggle_qry!!.text = "Script Mode"
                this.execute_all_flag = false
               // this.btn_executeActionPerformed(null)
                if (threadMainQuery.state == Thread.State.TERMINATED || threadMainQuery.isInterrupted || threadMainQuery.state == Thread.State.NEW ) {
                    threadMainQuery = object : Thread() {
                        override fun run() {
                            v_stmt_txt.cursor = Cursor(Cursor.WAIT_CURSOR)
                            btn_executeActionPerformed(null)
                            v_stmt_txt.cursor = Cursor.getDefaultCursor()
                        }

                    }
                    threadMainQuery.start()
                }
            } else if (evt.modifiers and KeyEvent.CTRL_MASK != 0 && evt.keyCode == KeyEvent.VK_U) {
                this.btn_upperActionPerformed(null)
            } else if (evt.modifiers and KeyEvent.CTRL_MASK != 0 && evt.keyCode == KeyEvent.VK_L) {
                this.btn_lowerActionPerformed(null)
            } else if (evt.keyCode == KeyEvent.VK_F4) {
                var obj: String? = null
                if (v_stmt_txt!!.selectedText != null && v_stmt_txt!!.selectedText.length > 1) {
                    obj = v_stmt_txt!!.selectedText
                } else {
                    try {
                        obj = jcon!!.findWordAtCaret(v_stmt_txt!!.document.getText(0, v_stmt_txt!!.document.length), v_stmt_txt!!.caretPosition)
                    } catch (ex: BadLocationException) {
                        Logger.getLogger(StmtRunScreen1::class.java.name).log(Level.SEVERE, null, ex)
                    }

                }
                descObject.main(jcon, obj)
            }  else {
             /*   if(tabProvider != null) {
                  //  val tmp = AutoCompletion(tabProvider)
                    ac?.install(v_stmt_txt)
                }
                */
            }
            this.cursor = java.awt.Cursor.getDefaultCursor()
        }

        private fun menu_tbl_copyActionPerformed(evt: java.awt.event.ActionEvent) {
            val txtToCopy = StringBuffer()
            val colindx = this.tbl_grid!!.selectedColumn
            val endcolindx = this.tbl_grid.selectedColumnCount + colindx - 1
            val rowindx = this.tbl_grid.selectedRow
            val endrowindx = this.tbl_grid.selectedRowCount + rowindx - 1
            val linesep = System.getProperty("line.separator")
            for (ch in colindx..endcolindx) {
                val sep = if (ch == endcolindx) "" else "\t"
                txtToCopy.append(this.tbl_grid.getColumnName(ch) + sep)
            }
            txtToCopy.append(linesep)
            for (r in rowindx..endrowindx) {
                for (c in colindx..endcolindx) {
                    val sep = if (c == endcolindx) "" else "\t"
                    if (this.tbl_grid.getValueAt(r, c) == null) {
                        txtToCopy.append(sep)
                    } else if (this.tbl_grid.getValueAt(r, c) is Date) {
                        val f = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val tmpdate = f.format(this.tbl_grid.getValueAt(r, c) as Date)
                        txtToCopy.append(tmpdate + sep)
                    } else {
                        txtToCopy.append(this.tbl_grid.getValueAt(r, c).toString() + sep)
                    }
                }
                txtToCopy.append(linesep)
            }

            val stringSelection = StringSelection(txtToCopy.toString())
            val clpbrd = Toolkit.getDefaultToolkit().systemClipboard
            clpbrd.setContents(stringSelection, null)

        }

        private fun fileNameToSave(): String? {
            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = "Specify a file to save"
            val userSelection = fileChooser.showSaveDialog(this)
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                val fileToSave = fileChooser.selectedFile
                return fileToSave.absolutePath
            }
            return null
        }

        private fun set_icons() {
            var btn: JButton
            for (i in 0..this.toolbar!!.componentCount - 1) {
                try {
                    btn = this.toolbar!!.getComponent(i) as JButton
                    var icon = btn.icon as ImageIcon
                    val img = icon.image
                    val newimg = img.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)
                    icon = ImageIcon(newimg)
                    btn.icon = icon
                } catch (e: Exception) {
                }

            }
            var jm: JMenuItem
            for (i in 0..mnu_grid!!.componentCount - 1) {
                try {
                    jm = mnu_grid!!.getComponent(i) as JMenuItem
                    var icon = jm.icon as ImageIcon
                    val img = icon.image
                    val newimg = img.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)
                    icon = ImageIcon(newimg)
                    jm.icon = icon
                } catch (e: Exception) {
                }

            }
        }


        private fun show_msg(msg: String) {
            JOptionPane.showMessageDialog(this, msg, "-", JOptionPane.INFORMATION_MESSAGE)
        }

        private fun show_alert(err_msg: String) {
            JOptionPane.showMessageDialog(this, err_msg, "Error", JOptionPane.ERROR_MESSAGE)
        }

        private fun actionExportToGrid() {
            try {
                while (1 == 1) {
                    val rowCount = tbl_grid.rowCount;
                    jcon?.fetchNextRows(tbl_grid);
                    if (rowCount == tbl_grid.rowCount) break; }
            } catch(e: Exception) {
            }
            val EO: excelOps = excelOps.main(jcon);
            EO.loadGridFromDb(tbl_grid)
        }

    }

    init {
        val parent = this

        mdi_toolbar.isFloatable = false
        mdi_toolbar.isRollover = false
        deskTopPane = javax.swing.JDesktopPane()
        jMenuBar1 = javax.swing.JMenuBar()
        /*  javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(deskTopPane, javax.swing.GroupLayout.Alignment.TRAILING)
         );
         layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(deskTopPane, javax.swing.GroupLayout.Alignment.TRAILING)
         );  */
        this.contentPane.add(mdi_toolbar, BorderLayout.NORTH)
        this.contentPane.add(deskTopPane)

        val mnuOpenConn = javax.swing.JMenuItem("Open New Connection")
        val mnureadfile = javax.swing.JMenuItem("Open File")
        val mnusavefile = javax.swing.JMenuItem("Save")
        val mnusaveAs = javax.swing.JMenuItem("Save As..")
        val mnupref = javax.swing.JMenuItem("Preferences")
        val quickHelp = javax.swing.JMenuItem("Quick Help")
        val about = javax.swing.JMenuItem("About Query Light..")
        menu_file = javax.swing.JMenu()
        val menu_help = javax.swing.JMenu("Help")
        val special_ops = javax.swing.JMenu("Special Ops")
        val cascadeDelete = javax.swing.JMenuItem("Cascade Delete")
        special_ops.add(cascadeDelete)
        cascadeDelete.addActionListener {
            for (i in winList.indices) {
                if (winList[i].frmInstance.isSelected) {
                    val frm = winList[i].frmInstance
                    val cs = cascadeDelete(frm.jcon)
                }
            }
        }
        val menuInsertfromDiffDb = javax.swing.JMenuItem("Insert from diffrent database")
        special_ops.add(menuInsertfromDiffDb)
        menuInsertfromDiffDb.addActionListener {
            for (i in winList.indices) {
                if (winList[i].frmInstance.isSelected) {
                    val frm = winList[i].frmInstance
                    insertFromAnotherDb.openInsertDialog(frm.jcon)
                }
            }
        }
        val jMenuExcelOps = javax.swing.JMenuItem("Excel Operations")
        special_ops.add(jMenuExcelOps)
        jMenuExcelOps.addActionListener {
            for (i in winList.indices) {
                if (winList[i].frmInstance.isSelected) {
                    val frm = winList[i].frmInstance
                    excelOps.main(frm.jcon)
                }
            }
        }


        menu_file.mnemonic = KeyEvent.VK_F
        Exit = javax.swing.JMenuItem()
        menu_edit = javax.swing.JMenu()
        menu_help.add(quickHelp)
        menu_help.mnemonic = KeyEvent.VK_H
        menu_help.add(about)
        about.addActionListener {
            val jif = javax.swing.JInternalFrame("Query Light", false, true)
            jif.add(qryLightAbout())
            deskTopPane.add(jif)
            val x = parent.x + parent.width / 2
            val y = parent.y + parent.height / 2
            jif.isVisible = true
            jif.pack()
            val jInternalFrameSize = jif.size
            jif.setLocation(x - jInternalFrameSize.width / 2, y - jInternalFrameSize.height / 2)
            try {
                jif.isSelected = true
            } catch (ex: PropertyVetoException) {
                //Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        quickHelp.addActionListener {
            val jif = javax.swing.JInternalFrame("Query Light", false, true)
            jif.add(qryLightHelp())
            deskTopPane.add(jif)
            jif.isVisible = true
            jif.pack()
            try {
                jif.isSelected = true
            } catch (ex: PropertyVetoException) {
                //Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        menu_edit.mnemonic = KeyEvent.VK_E
        menu_file.add(mnureadfile)
        mnureadfile.mnemonic = KeyEvent.VK_O
        menu_file.add(mnusavefile)
        menu_file.add(mnusaveAs)
        menu_file.add(mnuOpenConn)
        mnupref.addActionListener(object : java.awt.event.ActionListener {
            override fun actionPerformed(evt: java.awt.event.ActionEvent) {
                val dlg = javax.swing.JDialog()
                val img = ImageIcon(javaClass.getResource("/db1-icon.png"))
                dlg.setIconImage(img.image)
                dlg.add(qryLightPrf())
                dlg.pack()
                dlg.setLocationRelativeTo(null)
                dlg.isVisible = true
            }
        })

        mnusaveAs.addActionListener { saveEditorFile(true) }
        mnusavefile.mnemonic = KeyEvent.VK_S
        mnusavefile.addActionListener { saveEditorFile(false) }
        mnureadfile.addActionListener { loadFileInEditor() }

        mnuOpenConn.addActionListener {
            val jp = newlogin1.showConnDialog(parent)
            addNewConnWin(jp)
        }
        menu_file.add(Exit)
        jMenuBar1.add(menu_file)
        menu_edit.text = "Edit"
        menu_edit.add(mnupref)
        jMenuBar1.add(menu_edit)
        jMenuBar1.add(menu_help)
        jMenuBar1.add(special_ops)
        menu_file.text = "File"
        Exit.text = "Exit"
        Exit.addActionListener { exitApp() }

        jMenuBar = jMenuBar1
        java.awt.EventQueue.invokeLater { val db1 = dbSession(v_usr, v_password, v_jdbcstr, dbname) }

        // final dbSession db1 = new dbSession(v_usr, v_password, v_jdbcstr, Db);
        this.title = "Query Light"
        // ImageIcon img = new ImageIcon(Bms_Constants.project_icon);
        this.iconImage = ImageIcon(javaClass.getResource("/db1-icon.png")).image

        this.pack()
        val TH = object : Thread() {
            override fun run() {
                try {
                    TimeUnit.SECONDS.sleep(2)
                } catch (ex: InterruptedException) {
                    // Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
                }

                /* try {
            db1.setSelected(true);
        } catch (PropertyVetoException ex) {
            //  Logger.getLogger(StmtRunScreen1.class.getName()).log(Level.SEVERE, null, ex);
        } */
            }
        }

        this.addWindowListener(object : java.awt.event.WindowAdapter() {
            override fun windowClosing(e: java.awt.event.WindowEvent?) {
                exitApp()

            }
        })
        this.extendedState = this.extendedState or javax.swing.JFrame.MAXIMIZED_BOTH
        deskTopPane.dragMode = javax.swing.JDesktopPane.OUTLINE_DRAG_MODE
    }

    inner class windowlist(var window_name: String, var frmInstance: dbSession, var jb: JButton)

    private fun saveEditorFile(saveAs: Boolean) {
        val fname: String?
        for (i in winList.indices) {
            if (winList[i].frmInstance.isSelected) {
                val frm = winList[i].frmInstance
                if (frm.v_stmt_txt.getText().length > 0) {
                    try {
                        if (frm.currentFile != null && !saveAs) {
                            fname = frm.currentFile
                        } else {
                            fname = JdbcPersistent.fileNameToSave(this.javaClass)
                        }
                        if (fname != null) {
                            frm.currentFile = fname
                            JdbcPersistent.writeStringToFile(frm.v_stmt_txt.getText(), fname)
                        }
                    } catch (ex: Exception) {
                        JOptionPane.showMessageDialog(null, ex.toString())
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "No Text to Save !")

                }

                break
            }
        }
    }

    private fun loadFileInEditor() {
        for (i in winList.indices) {
            if (winList[i].frmInstance.isSelected) {
                val frm = winList[i].frmInstance
                if (frm.v_stmt_txt.getText().length > 0) {
                    try {
                        val jp = JdbcPersistent(frm.jcon!!)
                        addNewConnWin(jp)
                        val fname = jp.fileNameToOpen("Sql , Text", arrayOf("SQL", "TXT"), this.javaClass)
                        if (fname != null) {
                            val toLoad = JdbcPersistent.readStringFromFile(fname)
                            frm.currentFile = fname
                            winList[winList.size - 1].frmInstance.v_stmt_txt.setText(toLoad)
                        }
                    } catch (ex: NullPointerException) {
                        java.util.logging.Logger.getLogger(StmtRunScreen1::class.java.name).log(java.util.logging.Level.SEVERE, null, ex)
                    } catch (ex: Exception) {
                        JOptionPane.showMessageDialog(null, ex.toString())
                    }

                } else {
                    try {
                        val fname = frm.jcon!!.fileNameToOpen("Sql , Text", arrayOf("SQL", "TXT"), this.javaClass)
                        if (fname != null) {
                            val toLoad = JdbcPersistent.readStringFromFile(fname)
                            frm.currentFile = fname
                            frm.v_stmt_txt.setText(toLoad)
                        }
                    } catch (ex: Exception) {
                        JOptionPane.showMessageDialog(null, ex.toString())
                    }

                }

                break
            }
        }
    }

    private fun addNewConnWin(jp: JdbcPersistent) {
        dbSession(jp.uid!!, jp.pwd!!, jp.jdbcstr!!, jp.dbName!!)
    }

    private fun exitApp() {
        val response = JOptionPane.showConfirmDialog(this, "Close Query Lite", " Quit ? ", JOptionPane.YES_NO_OPTION)
        if (response == JOptionPane.YES_OPTION) {
            System.exit(0)
        }

    }


    companion object {

        fun open_screen(username: String, password: String, jdbcstr: String, Db: String) {
            /* Set the Nimbus look and feel */
            val uid = username
            val pwd = password
            val jdbcurl = jdbcstr
            val tmpDb = Db
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
            try {
                for (info in javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus" == info.name) {
                        javax.swing.UIManager.setLookAndFeel(info.className)
                        break
                    }
                }
            } catch (ex: ClassNotFoundException) {
                java.util.logging.Logger.getLogger(StmtRunScreen1::class.java.name).log(java.util.logging.Level.SEVERE, null, ex)
            } catch (ex: InstantiationException) {
                java.util.logging.Logger.getLogger(StmtRunScreen1::class.java.name).log(java.util.logging.Level.SEVERE, null, ex)
            } catch (ex: IllegalAccessException) {
                java.util.logging.Logger.getLogger(StmtRunScreen1::class.java.name).log(java.util.logging.Level.SEVERE, null, ex)
            } catch (ex: javax.swing.UnsupportedLookAndFeelException) {
                java.util.logging.Logger.getLogger(StmtRunScreen1::class.java.name).log(java.util.logging.Level.SEVERE, null, ex)
            }

            //</editor-fold>

            /* Create and display the form */
            java.awt.EventQueue.invokeLater { StmtRunScreen1(username, password, jdbcstr, Db).isVisible = true }
        }
    }
    // End of variables declaration                   
}
