/*
 * Copyright (C) 2016 hv655
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package runsql_anony

import java.io.*
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 *
 * @author hv655
 */
class StoredDbDetails : Serializable {

    public var dbRecords = ArrayList<DbRecord>()
    public var basePath = System.getProperty("user.home")
    public var appPath = basePath + File.separator + "QueryLight"
    public var datFile = File(appPath + File.separator + "querylight.dat".toString())
    public var datDir = File(appPath.toString())
    public var datFileFullPathString = appPath + File.separator + "querylight.dat".toString()

    init {

        if (!datDir.exists()) {
            try {
                datDir.mkdir()
                datFile.createNewFile()
            } catch (ex: IOException) {
                Logger.getLogger(newlogin1::class.java.name).log(Level.SEVERE, null, ex)
            }

        } else {
            if (!datFile.exists()) {
                try {
                    datFile.createNewFile()
                } catch (ex: IOException) {
                    Logger.getLogger(newlogin1::class.java.name).log(Level.SEVERE, null, ex)
                }

            } else {
                try {
                    var fin: FileInputStream? = null
                    var ois: ObjectInputStream? = null
                    fin = FileInputStream(this.datFileFullPathString)
                    ois = ObjectInputStream(fin)
                    this.dbRecords = (ois.readObject() as StoredDbDetails).dbRecords
                } catch (ex: FileNotFoundException) {
                    Logger.getLogger(newlogin1::class.java.name).log(Level.SEVERE, null, ex)
                } catch (ex: IOException) {
                    Logger.getLogger(newlogin1::class.java.name).log(Level.SEVERE, null, ex)
                } catch (ex: ClassNotFoundException) {
                    Logger.getLogger(newlogin1::class.java.name).log(Level.SEVERE, null, ex)
                }

            }

        }

    }

    fun get_stored_pwd(db: String, usr: String): String? {
        val index: Int
        for (dbrec in dbRecords) {
            if (dbrec.userName!!.toUpperCase() == usr && dbrec.databaseName.toUpperCase() == db) {
                return dbrec.userPassword
            }
        }
        return null
    }

    fun get_stored_jdbcstr(db: String, usr: String): String? {
        for (dbrec in dbRecords) {
            if (dbrec.userName!!.toUpperCase() == usr && dbrec.databaseName.toUpperCase() == db) {
                return dbrec.jdbcString
            }
        }
        return null
    }

    fun delete_stored_dbRecord(db: String, usr: String) {
        val iterator = dbRecords.iterator()
        while (iterator.hasNext()) {
            val dbrec = iterator.next()
            if (dbrec.userName!!.toUpperCase() == usr.toUpperCase() && dbrec.databaseName.toUpperCase() == db.toUpperCase()) {
                iterator.remove()
            }
        }
        try {
            this.serializeStoredDbDetails(this)
        } catch (ex: IOException) {
            Logger.getLogger(newlogin1::class.java.name).log(Level.SEVERE, null, ex)
        }

    }


    fun store_db_details(db: String, usr: String, pwd: String, jdbcstr: String) {
        var found = false
        for (dbrec in dbRecords) {
            if (dbrec.userName!!.toUpperCase() == usr.toUpperCase() && dbrec.databaseName.toUpperCase() == db.toUpperCase()) {
                dbrec.userPassword = pwd
                dbrec.jdbcString = jdbcstr
                found = true
            }

        }
        if (!found) {
            this.dbRecords.add(DbRecord(usr, pwd, jdbcstr, db))
        }
        try {
            this.serializeStoredDbDetails(this)
        } catch (ex: IOException) {
            Logger.getLogger(newlogin1::class.java.name).log(Level.SEVERE, null, ex)
        }

    }

    val dbRecordsCount: Int
        get() = dbRecords.size

    fun getDbRecordUserNameAt(index: Int): String? {
        return this.dbRecords[index].userName
    }

    fun getDbRecordUserPassWordAt(index: Int): String? {
        return this.dbRecords[index].userPassword
    }

    fun getDbRecordJdbcStringAt(index: Int): String? {
        return this.dbRecords[index].jdbcString
    }

    fun getDbRecordDatabaseNameAt(index: Int): String {
        return this.dbRecords[index].databaseName
    }

    @Throws(IOException::class)
    private fun serializeStoredDbDetails(s: StoredDbDetails) {
        var fout: FileOutputStream? = null
        var oos: ObjectOutputStream? = null
        fout = FileOutputStream(this.datFileFullPathString)
        oos = ObjectOutputStream(fout)
        oos.writeObject(s)
    }

    inner class DbRecord(var userName: String?, var userPassword: String?, var jdbcString: String?, val databaseName: String) : Serializable

}
