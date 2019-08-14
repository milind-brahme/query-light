package com.milind.querylight

import java.io.*
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class StoredTnsEntries  : Serializable {
    var db_list = Vector<String>()
    public var basePath = System.getProperty("user.home")
    public var appPath = basePath + File.separator + "QueryLight"
    public var datFile = File(appPath + File.separator + "querylightStoredTNS.dat".toString())
    public var datDir = File(appPath.toString())
    public var datFileFullPathString = appPath + File.separator + "querylightStoredTNS.dat".toString()

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
                    this.db_list = (ois.readObject() as StoredTnsEntries).db_list
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

    @Throws(IOException::class)
    public fun serializeStoredDbDetails() {
        var fout: FileOutputStream? = null
        var oos: ObjectOutputStream? = null
        fout = FileOutputStream(this.datFileFullPathString)
        oos = ObjectOutputStream(fout)
        oos.writeObject(this)
        System.out.println("The TNS DB LIst written");
    }
}