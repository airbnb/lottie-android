package com.airbnb.lottie.samples.testing

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import com.airbnb.lottie.L
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.samples.databinding.FilmStripSnapshotsActivityBinding
import com.airbnb.lottie.samples.utils.viewBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

private const val RC_PERMISSION = 12345

class FilmStripSnapshotActivity : AppCompatActivity() {
    private val binding: FilmStripSnapshotsActivityBinding by viewBinding()

    // TODO: fix this.
    @Suppress("DEPRECATION")
    private val rootDir = "${Environment.getExternalStorageDirectory()}/lottie"
    private val animationsDir = File("$rootDir/animations")
    private val snapshotsDir = File("$rootDir/snapshots")

    private val bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(bitmap)
    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(L.TAG, "Starting Snapshots")

        if (!animationsDir.exists() || !animationsDir.isDirectory) throw IllegalStateException("Animations directory ($animationsDir) does not exist!")
        if (!snapshotsDir.exists()) snapshotsDir.mkdirs()

        binding.filmStripView.doOnNextLayout {
            createSnapshots()
            Log.i(L.TAG, "Finished Snapshots")
            finish()
        }
    }

    private fun hasStoragePermission() = hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)

    private fun hasPermission(permission: String) = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun requirePermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), RC_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RC_PERMISSION -> if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                createSnapshots()
            } else {
                Log.w(L.TAG, "Permission not granted. Finishing.")
                finish()
            }
        }
    }

    private fun createSnapshots() {
        if (!hasStoragePermission()) {
            requirePermissions()
            return
        }

        val files = animationsDir.listFiles() ?: emptyArray()
        Log.d(L.TAG, "Found ${files.size} files.")
        files
                .filter { it.name.endsWith(".json") }
                .forEach { file ->
                    Log.d(L.TAG, "Creating snapshotFilmstrip for ${file.name}")
                    val fis = FileInputStream(file)
                    val result = LottieCompositionFactory.fromJsonInputStreamSync(fis, file.name)
                    val composition = result.value ?: throw IllegalStateException("Unable to parse composition for $file", result.exception)
                    binding.filmStripView.setComposition(composition)
                    canvas.clear()
                    binding.filmStripView.draw(canvas)

                    val outputFileName = file.name.replace(".json", ".png")
                    val outputFilePath = "${Environment.getExternalStorageDirectory()}/lottie/snapshots/$outputFileName"
                    FileOutputStream(outputFilePath).use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }
    }

    private fun Canvas.clear() {
        drawRect(canvas.clipBounds, clearPaint)
    }
}