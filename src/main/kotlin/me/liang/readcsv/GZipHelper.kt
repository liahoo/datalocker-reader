package me.liang.readcsv

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.zip.GZIPInputStream

object GZipHelper {
    fun unzip(source: File, target: File, removeSource: Boolean=false) {
        if(!source.exists() || !source.isFile) throw NoSuchFileException(source)
        if(!target.exists()) target.createNewFile()
        lateinit var gis: GZIPInputStream
        lateinit var fos: FileOutputStream
        try {
            gis = GZIPInputStream(FileInputStream(source))
            fos = FileOutputStream(target)
            val buffer = ByteArray(1024)
            var len = gis.read(buffer)
            while(len > 0) {
                fos.write(buffer, 0, len)
                len=gis.read(buffer)
            }
        } catch (e: Exception) {
            throw e
        } finally {
            gis.close()
            fos.close()
        }
        if(removeSource) source.delete()
    }
}