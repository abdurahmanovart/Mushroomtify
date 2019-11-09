package ai.arturxdroid.mushroomtify

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception

object Utils {

    public fun assetFilePath(context: Context, asset: String): String {

        val file = File(context.filesDir, asset)

        try {
            val inpStream: InputStream = context.assets.open(asset)

            try {

                val outStream = FileOutputStream(file, false);


                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (true) {
                    read = inpStream.read(buffer)
                    if (read == -1) {
                        break
                    }
                    outStream.write(buffer, 0, read)
                }
                outStream.flush()


            } catch (ex: Exception) {

            }
            return file.absolutePath

        } catch (except: Exception) {

        }
        return ""
    }

}