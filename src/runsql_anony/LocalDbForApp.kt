package runsql_anony

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.table.DatabaseTable
import com.j256.ormlite.table.TableUtils
import java.util.logging.Level
import java.util.logging.Logger


public fun queryPropValue(propName:String) : String?
{  try {
    var DAO: Dao<PropertyValue, String> = DaoManager.createDao(LocalDbForApp.connectionSource, PropertyValue::class.java);
    val pv :PropertyValue? =  DAO.queryForId(propName)
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
      private val databaseUrl = "jdbc:h2:~/${"QueryLight"}"
       val connectionSource = JdbcConnectionSource(databaseUrl)

       val ldb:LocalDbForApp = LocalDbForApp()
   }
    constructor()
    {
        TableUtils.createTableIfNotExists(connectionSource,PropertyValue::class.java)


    }

}

@DatabaseTable(tableName = "property_value")
class PropertyValue : LocalDbForApp   {
    @DatabaseField(id=true)
    var propertyName : String?=null
  @DatabaseField(canBeNull = false)
    var propertyValue : String ?=null
    var DAO  : Dao<PropertyValue,String> = DaoManager.createDao(connectionSource, PropertyValue::class.java);

    constructor(propName:String , propValue:String?) : super() {
        this.propertyName=propName
        this.propertyValue=propValue
        DAO.createIfNotExists(this)
        DAO.update(this)
    }

    constructor() : super() {

    }

}