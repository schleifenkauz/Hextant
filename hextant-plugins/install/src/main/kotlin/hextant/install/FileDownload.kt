package hextant.install

import java.io.*
import java.net.*
import java.nio.channels.Channels

object FileDownload {
    private fun estimateSize(url: URL): Long {
        if (url.protocol == "file") return File(url.toURI()).length()
        var con: URLConnection? = null
        try {
            con = url.openConnection()
            if (con !is HttpURLConnection) {
                System.err.println("Unrecognized URL - cannot estimate size of download: $url")
                return 0
            }
            con.requestMethod = "HEAD"
            return con.contentLengthLong
        } catch (ex: IOException) {
            ex.printStackTrace()
            System.err.println("Could not estimate size of download: $url")
            return 0
        } finally {
            if (con is HttpURLConnection) con.disconnect()
        }
    }

    fun download(url: URL, dest: File) {
        val size = estimateSize(url)
        if (size != 0L) println("Attempting to download ${size / 1000}kb")
        val rbc = Channels.newChannel(url.openStream())
        val fos = FileOutputStream(dest)
        fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
    }
}