package me.liang.readcsv

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream

object GZipHelper {
    fun unzip(source: File, target: File, removeSource: Boolean=false) {
        println("unzip ${source.canonicalPath}")
        if(!source.exists() || !source.isFile) throw NoSuchFileException(source)
        if(!target.exists()) target.createNewFile()
        try {
            val gis: GZIPInputStream = GZIPInputStream(FileInputStream(source))
            val fos: FileOutputStream = FileOutputStream(target)
            val buffer: ByteArray = ByteArray(1024)
            var len = gis.read(buffer)
            while(len > 0) {
                fos.write(buffer, 0, len)
                len=gis.read(buffer)
            }
        } catch (e: Exception) {
            throw e
        }
        if(removeSource) source.delete()
    }
}