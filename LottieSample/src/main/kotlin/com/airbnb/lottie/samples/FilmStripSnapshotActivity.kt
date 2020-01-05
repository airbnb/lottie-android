package com.airbnb.lottie.samples

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.doOnNextLayout
import com.airbnb.lottie.L
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import kotlinx.android.synthetic.main.activity_film_strip_snapshots.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.IllegalStateException

private const val RC_PERMISSION = 12345

class FilmStripSnapshotActivity : AppCompatActivity() {

    // TODO: fix this.
    @Suppress("DEPRECATION")
    private val rootDir = "${Environment.getExternalStorageDirectory()}/lottie"
    private val animationsDir = File("$rootDir/animations")
    private val snapshotsDir = File("$rootDir/snapshots")
    private val dummyBitmap by lazy { BitmapFactory.decodeResource(resources, R.drawable.airbnb) }

    private val bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(bitmap)
    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_film_strip_snapshots)
        Log.i(L.TAG, "Starting Snapshots")

        if (!animationsDir.exists() || !animationsDir.isDirectory) throw IllegalStateException("Animations directory ($animationsDir) does not exist!")
        if (!snapshotsDir.exists()) snapshotsDir.mkdirs()

        filmStripView.doOnNextLayout {
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

        Log.d(L.TAG, "Found ${animationsDir.listFiles().size} files.")
        animationsDir.listFiles()
                .filter { it.name.endsWith(".json") }
                .forEach { file ->
                    Log.d(L.TAG, "Creating snapshotFilmstrip for ${file.name}")
                    val fis = FileInputStream(file)
                    val result = LottieCompositionFactory.fromJsonInputStreamSync(fis, file.name)
                    val composition = result.value ?: throw IllegalStateException("Unable to parse composition for $file", result.exception)
                    filmStripView.setComposition(composition)
                    canvas.clear()
                    filmStripView.draw(canvas)

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