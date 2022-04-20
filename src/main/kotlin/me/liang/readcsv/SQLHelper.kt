package me.liang.readcsv

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

object SQLHelper {
    private var conn: Connection? = null
    fun connect(url: String, user: String, password: String, cb: ((String)->Unit)?) {
        println("db connecting...")
        var result = "Timeout!"
        try {
            conn = DriverManager.getConnection(
                "$url?user=$user&password=$password"
            )
            result = "DB connected"
        } catch (e: SQLException) {
            e.printStackTrace()
            result = e.localizedMessage
        }
        println("db connected")
        cb?.invoke(result)
    }

    fun createSchema(name: String) {
        executeSQLCommand(
            "CREATE SCHEMA `$name` DEFAULT CHARACTER SET utf8 ;"
        )
    }

    fun createTableWithColumns(schema: String, tableName: String, fields: Collection<SQLColumn>?) {
        val columnsBuffer: StringBuffer = StringBuffer()
        fields?.forEach { col ->
            columnsBuffer.append("`${col.name}` ${col.type}")
            if(fields.size-1 > fields.indexOf(col)) {
                columnsBuffer.append(",\n")
            }
        }
        val cmd = "CREATE TABLE `$schema`.`$tableName` (\n" +
                columnsBuffer.toString() +
                ");"
        executeSQLCommand(cmd)
    }
    fun importCsvFileToTable(path:String, tableName: String) {
        executeSQLCommand(
                "LOAD DATA INFILE '$path' INTO TABLE $tableName\n" +
                        "FIELDS TERMINATED BY ','\n" +
                        "ENCLOSED BY '\"'\n" +
                        "LINES TERMINATED BY '\\n'\n" +
                        "IGNORE 1 LINES;"
            )
    }

    private fun executeSQLCommand(cmd: String) : Boolean {
        conn ?: throw SQLException("DB is not connected! Try to call connect() first!")
        val stmt = conn!!.createStatement()
        var result = false
        try {
            result = stmt.execute(cmd)
        } catch (e: Exception) {
            throw e
        } finally {
            stmt.close()
        }
        return result
    }
}