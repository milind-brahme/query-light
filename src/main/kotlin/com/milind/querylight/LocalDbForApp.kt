package com.milind.querylight

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.table.DatabaseTable
import com.j256.ormlite.table.TableUtils
import com.milind.querylight.LocalDbForApp.Companion.propertyDAO
import com.milind.querylight.LocalDbForApp.Companion.tnsDAO
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


public fun queryPropValue(propName:String) : String?
{  try {
    var DAO: Dao<PropertyValue, String> = DaoManager.createDao(LocalDbForApp.connectionSource, PropertyValue::class.java);
    val pv :PropertyValue? = DAO.queryForId(propName)
    val tmp = pv?.propertyValue?:null
    return tmp
}catch (  e :Exception)
{
    Logger.getLogger(LocalDbForApp::class.java.name).log(Level.SEVERE, null, e)
    return null
}
}

open class LocalDbForApp {
   companion object {
      private val databaseUrl = "jdbc:h2:~/QueryLight"
       val connectionSource = JdbcConnectionSource(databaseUrl)
       val ldb:LocalDbForApp = LocalDbForApp()
       val propertyDAO  = DaoManager.createDao(connectionSource, PropertyValue::class.java);
       val tnsDAO = DaoManager.createDao(connectionSource, TNSTable::class.java);
   }
    constructor()
    {
        TableUtils.createTableIfNotExists(connectionSource,PropertyValue::class.java)
       // var DAO  : Dao<TNSTable,String> = DaoManager.createDao(connectionSource, TNSTable::class.java);
        //TableUtils.dropTable(DAO,true)
        TableUtils.createTableIfNotExists(connectionSource,TNSTable::class.java)

    }

}

@DatabaseTable(tableName = "property_value")
class PropertyValue : LocalDbForApp   {
    @DatabaseField(id=true)
    var propertyName : String?=null
  @DatabaseField(canBeNull = false)
    var propertyValue : String ?=null

    constructor(propName:String , propValue:String?) : super() {
        this.propertyName=propName
        this.propertyValue=propValue
        propertyDAO.createIfNotExists(this)
        propertyDAO.update(this)
    }

    constructor() : super() {

    }

}

public fun getAllTNSDb() :  Vector<String>
{
    val db_list = Vector<String>()
    var d = tnsDAO.iterator()
    for (tnsTable in d) {
        db_list.addElement(tnsTable.tnsConnectString)
    }
    return db_list;
}

public fun queryTNSTable(tnsConnectString: String ) : TNSTable?
{
    try {
        var DAO: Dao<TNSTable, String> = DaoManager.createDao(LocalDbForApp.connectionSource, TNSTable::class.java);
        val pv :TNSTable? =  DAO.queryForId(tnsConnectString)
        val tmp = pv
        return tmp
    }catch (  e :Exception)
    {
        Logger.getLogger(LocalDbForApp::class.java.name).log(Level.SEVERE, null, e)
        return null
    }
}

@DatabaseTable(tableName = "TNSTable")
class TNSTable    {
    @DatabaseField(id=true)
    var tnsConnectString : String?=null

    @DatabaseField()
    var userName : String ?=null

    @DatabaseField
    var password : String ?=null

    @DatabaseField(width = 1000)
    var jdbcString : String ?=null

/*
    constructor() : super() {

    }
    */

    constructor()
    {

    }

    constructor(tnsConnectString: String?, userName: String?, password: String?, jdbcString: String?) : super() {

        this.tnsConnectString = tnsConnectString
        this.userName = userName
        this.password = password
        this.jdbcString = jdbcString
       if (tnsDAO.isTableExists)
       {
           try {
               tnsDAO.createOrUpdate(this)
               tnsDAO.update(this)
           } catch (e: Exception) {
               System.out.println(e.message)
           }

       }
     //  else {
     //      tnsDAO.createIfNotExists(this)
     //   }

    }

}

