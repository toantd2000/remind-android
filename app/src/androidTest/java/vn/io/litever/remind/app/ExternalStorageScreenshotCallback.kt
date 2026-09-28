package vn.io.litever.remind.app

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import tools.fastlane.screengrab.ScreenshotCallback
import tools.fastlane.screengrab.Screengrab
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Custom [ScreenshotCallback] for Screengrab that writes screenshots to the app's external files directory:
 *   /sdcard/Android/data/<package>/files/screengrab/<locale>/images/screenshots/
 *
 * Why this is necessary:
 * On Android 10+ (API 29+), Screengrab's built-in FileWritingScreenshotCallback defaults to internal storage
 * (/data/user/0/<package>/app_screengrab/...), which triggers 'Permission denied' on adb pull.
 * Fastlane then falls back to a Unix pipe (`run-as ... tar -cC ... | tar -xv ...`) which fails/hangs on Windows.
 * By saving to external files dir (which is directly accessible to adb pull without root or run-as),
 * Fastlane pulls screenshots quickly and cleanly on any OS.
 */
class ExternalStorageScreenshotCallback(
    private val appContext: Context
) : ScreenshotCallback {

    override fun screenshotCaptured(screenshotName: String, bitmap: Bitmap) {
        try {
            val locale = Screengrab.getLocale() ?: "vi"
            val externalDir = appContext.getExternalFilesDir("screengrab")
                ?: appContext.filesDir
            val targetDir = File(File(externalDir, locale), "images/screenshots")
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                throw IOException("Unable to create screenshot directory: ${targetDir.absolutePath}")
            }

            val shouldAppendTimestamp = InstrumentationRegistry.getArguments()
                .getString("appendTimestamp")
                ?.toBoolean() ?: false

            val filename = if (shouldAppendTimestamp) {
                "${screenshotName}_${System.currentTimeMillis()}.png"
            } else {
                "${screenshotName}.png"
            }

            val screenshotFile = File(targetDir, filename)
            Log.d(TAG, "Saving screenshot to: ${screenshotFile.absolutePath}")

            BufferedOutputStream(FileOutputStream(screenshotFile)).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Log.d(TAG, "Successfully saved screenshot: ${screenshotFile.absolutePath} (${screenshotFile.length()} bytes)")
        } finally {
            bitmap.recycle()
        }
    }

    companion object {
        private const val TAG = "ScreengrabExt"
    }
}
