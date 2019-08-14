package com.milind.querylight


import org.fife.ui.autocomplete.BasicCompletion
import org.fife.ui.autocomplete.CompletionProvider
import org.fife.ui.autocomplete.DefaultCompletionProvider
import java.util.*


var jcon: JdbcPersistent?=null

fun  createTabCompletionProvider( jcon : JdbcPersistent  ) : CompletionProvider
{
    val listTableSql= "select table_name from all_Tables union select view_name from all_views"

    val provider = DefaultCompletionProvider()
    val tableList :ArrayList<String> = jcon.returnSingleColumnArray(listTableSql) as ArrayList<String>
    for (s in tableList) {
        provider.addCompletion( BasicCompletion(provider,s ))
    }
    return provider
}



class ChildTableWithParent ( var childTab:String, val childTabAlias:String , val jcon : JdbcPersistent )
{   var owner:String?=null
    var childIndex:String?=null
    var parentIndex:String?=null
    var parentTab : ChildTableWithParent?=null
    var parentTableName : String?=null
    init{
         owner=if(childTab.contains(".")) childTab.split(".")[0] else jcon?.returnSingleField("select table_owner  from all_synonyms where synonym_name = '$childTab'")
         childTab=if(childTab.contains(".")) childTab.split(".")[0] else childTab

        }

    fun buildWhere(  whereClause : StringBuilder)
    {
       if(whereClause.contains("1=1"))  else whereClause.append(" 1=1 \n")
      if(parentTableName!=null) {
          var chCol: ArrayList<String> = jcon!!.returnSingleColumnArray("select COLUMN_NAME  from ALL_CONS_COLUMNS where constraint_name = '${childIndex}' and owner = '$owner' order by position") as ArrayList<String>
          var parCol: ArrayList<String> = jcon!!.returnSingleColumnArray("select COLUMN_NAME  from ALL_CONS_COLUMNS where constraint_name = '${this.parentIndex}' and owner = '$owner' order by position") as ArrayList<String>

          for (c in chCol) {
              whereClause.append("and ${childTabAlias.toLowerCase()}.${c} = ${this.parentTab!!.childTabAlias.toLowerCase()}.${parCol.get(chCol.indexOf(c))} \n")
          }
      }else{
          var pk_name =jcon!!.returnSingleField("select  ch.CONSTRAINT_NAME CHILD_CON from ALL_CONSTRAINTS ch \n"
                  + "where ch.table_name = '$childTab'" +  " and ch.CONSTRAINT_TYPE = 'P' and ch.owner='$owner'")
          var chCol: ArrayList<String> = jcon!!.returnSingleColumnArray("select COLUMN_NAME  from ALL_CONS_COLUMNS where constraint_name = '${pk_name}' and owner = '$owner' order by position") as ArrayList<String>
          for (c in chCol) {
              whereClause.append("and ${childTabAlias.toLowerCase()}.${c} = :${c} \n")
          }

      }
    }

   fun buildParentDetails(allTablisting : ArrayList<String> , childTableList : ArrayList<ChildTableWithParent> )
    {
        var data: java.util.ArrayList<java.util.ArrayList<Map<String, String>>> =jcon!!.returnColumnValueMap("select  ch.CONSTRAINT_NAME CHILD_CON,  ch.R_CONSTRAINT_NAME PARENT_CON, p.table_name PARENT_TAB from ALL_CONSTRAINTS ch , ALL_CONSTRAINTS p\n"
                + "where ch.table_name = '$childTab' and ch.r_owner = p.owner and ch.r_constraint_name = p.constraint_name\n" +
                "and ch.CONSTRAINT_TYPE = 'R' and ch.owner='$owner'" +
                "and p.table_name in ( ${ allTablisting.joinToString (",") } )")?:return
        for (row in data) {
            for(i in row)
            {
                for((column , value ) in i ) {
                    when (column) {
                        "CHILD_CON" -> childIndex = value
                        "PARENT_CON" -> parentIndex = value
                        "PARENT_TAB" -> parentTableName = value
                    }
                }
            }
            break
        }
        if(parentTableName!=null)
        {
            for (t in childTableList) {
                if(t.childTab.toUpperCase().equals(parentTableName!!.toUpperCase())){
                    this.parentTab=t
                    break
                }
            }
        }


    }
}

fun JdbcPersistent.popColumnList( tabName : String , tabAlias : String?) : String
{
    val jcon=this
    val retString : StringBuffer = StringBuffer("")
    val d:descObject= descObject(jcon,tabName.toUpperCase(),false)
    val sql = ("select  ac.column_name \"Column_name\" \n"
            + "from  all_tab_columns ac \n"
            + "where   ac.table_name = '" + d.objname + "' and owner  ='" + d.schemaName + "' order by column_id")
    val colList : ArrayList<*>? = jcon.returnSingleColumnArray(sql)
    if (tabAlias!=null && colList!=null)
    {
        for (c in colList as ArrayList<String>) {
            retString.append("$tabAlias.$c,")
            if (colList.indexOf(c)%3==0)
                retString.append("\n")
        }
    }else if( colList!=null){
        for (c in colList as ArrayList<String>) {
            retString.append("$c,")
            if (colList.indexOf(c)%3==0)
                retString.append("\n")
        }

    }

    if(retString.length > 1)
    {
        return retString.substring(0,retString.lastIndexOf(","))
    }else
    {
        return retString.toString()
    }

}

fun JdbcPersistent.buildWhereClause( sqlStmt : String) : String
{
    jcon=this
     var whereClause : StringBuilder = StringBuilder()
    val regexTableList  = "from(.+)where".toRegex(options = setOf(RegexOption.DOT_MATCHES_ALL,RegexOption.IGNORE_CASE))
    val  tableListString : String = regexTableList.find(sqlStmt)?.groups?.get(1)?.value?:""
    val tableListTmp = tableListString.split(",")
    var childTableList : ArrayList<ChildTableWithParent> = ArrayList()
    var tableNameList : ArrayList<String> = ArrayList()

    for (str in tableListTmp) {
    var s : String = str.trim()
    var   tableName : String =  if(s.contains(" "))  s.split(" ").get(0).trim().toUpperCase() else s.trim().toUpperCase()
    var   alias : String =  if(s.contains(" "))   s.split(" ")[1].trim().toUpperCase() else "t" + tableListTmp.indexOf(str)
    val ctw:ChildTableWithParent = ChildTableWithParent(tableName,alias,JdbcPersistent(jcon!!))
         childTableList.add(ctw)
         tableNameList.add("'" + ctw.childTab + "'")
       }
    for (t in childTableList) {
        t.buildParentDetails(tableNameList,childTableList)
    }
    for (t in childTableList) {
        t.buildWhere(whereClause)
    }

    return whereClause.toString()
}

fun main(args: Array<String>) {
    val sql="select * Fom " +
            "order_header_table a, " +
            "order_line_items b where =1"
    var jp : JdbcPersistent?=null

}

class autoComplete {

}

class ChangeTracker  (  var j : JdbcPersistent  ) {
    var whereClause: StringBuilder = StringBuilder()
    val regexTableList = "from(.+[\\s\$]+)((where)|\$)".toRegex(options = setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
    var childTableList: ArrayList<ChildTableWithParent> = ArrayList()
    var provider : CompletionProvider = DefaultCompletionProvider()
    var tableListString: String=""

     fun trackChanges(sqlStmt: String , providerType : String) {

         var changeCounter  = 0;
         var tmpTableListString  = regexTableList.find(sqlStmt)?.groups?.get(1)?.value ?: ""
         if (tmpTableListString.equals(tableListString))
         {
             return
         }else
         {
             tableListString=tmpTableListString
         }
        var tableListTmp = tableListString.split(",")
        var tmpList: ArrayList<ChildTableWithParent> = ArrayList()
        for (str in tableListTmp) {
            var s: String = str.trim()
            var tableName: String = if (s.contains(" ")) s.split(" ").get(0).trim().toUpperCase() else s.trim().toUpperCase()
            var alias: String = if (s.contains(" ")) s.split(" ")[1].trim().toUpperCase() else "t" + tableListTmp.indexOf(str)
            val ctw: ChildTableWithParent = ChildTableWithParent(tableName, alias, j)
            tmpList.add(ctw)
            if(childTableList.any()  { it.childTab.toUpperCase().equals(tableName.toUpperCase()) } )  { }
            else {
                childTableList.add(ctw)
                changeCounter++
            }

       }
        for (mainList in childTableList) {
            if(tmpList.any(){it.childTab.equals(mainList.childTab)}){}
            else {
                childTableList.remove(mainList)
                changeCounter++
            }
        }
        if(providerType.equals("COL"))
        {
          this.provider=updateColumnCompletionProvider(  )
        }

    }

   private fun  updateColumnCompletionProvider(  ) : CompletionProvider
    {
        var tmp = childTableList.map{ "'" + it.childTab + "'"}
        val colprovider = DefaultCompletionProvider()
        for (c in childTableList) {
     // '${c.childTabAlias}.'||
            val listTableSql = "select '${c.childTabAlias}.'||column_name from all_Tab_columns where table_name = '" + c.childTab + "' and owner='" + c.owner  + "'"
            val tableList: ArrayList<String> = j.returnSingleColumnArray(listTableSql) as ArrayList<String>
            for (s in tableList) {
                colprovider.addCompletion(BasicCompletion(colprovider, s))
            }
           System.out.println("Adding columns")

        }
        return colprovider
    }

}
