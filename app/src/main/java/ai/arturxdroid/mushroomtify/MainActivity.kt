package ai.arturxdroid.mushroomtify

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI"

class MainActivity : AppCompatActivity() {

    private val READ_PERMISSION_REQUEST_CODE = 100
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private val CAMERA_IMAGE_REQUEST_CODE = 101
    private val GALLERY_IMAGE_REQUEST_CODE = 201
    private val SHARED_PREFS = "shared_prefs"
    private val SHARED_FIRST_LAUNCH = "shared_first_launch"
    private var cameraFilePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        showDisclaimer()
        initUI()
    }

    private fun showDisclaimer() {
        val prefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        if(prefs.getBoolean(SHARED_FIRST_LAUNCH,true)){
            prefs.edit().putBoolean(SHARED_FIRST_LAUNCH,false).apply()
            AlertDialog.Builder(this).setTitle(R.string.disclaimer_title)
                .setMessage(R.string.disclaimer_message)
                .setPositiveButton(R.string.ok
                ) { dialog, _ -> dialog?.dismiss() }
                .setCancelable(false)
                .show()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            openGallery()
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            takePicture()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK)
            when (requestCode) {
                CAMERA_IMAGE_REQUEST_CODE -> processCameraImage()
                GALLERY_IMAGE_REQUEST_CODE -> processGalleryImage(data)
            }
        else
            showErrorDialog()
    }

    private fun processCameraImage() {
        val imageUri = Uri.parse(cameraFilePath)
        val intent = Intent(this, RecognitionActivity::class.java)
        intent.putExtra(EXTRA_IMAGE_URI, imageUri.toString())
        startActivity(intent)
    }

    private fun processGalleryImage(data: Intent?) {
        val imageUri = data?.data
        val intent = Intent(this, RecognitionActivity::class.java)
        intent.putExtra(EXTRA_IMAGE_URI, imageUri.toString())
        startActivity(intent)
    }

    private fun initUI() {
        take_picture_button.setOnClickListener { getPermissionAndTakePicture() }
        open_gallery_button.setOnClickListener { getPermissionAndOpenGallery() }
    }


    private fun getPermissionAndTakePicture() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        )
            getCameraPermission()
        else
            takePicture()

    }

    private fun takePicture() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    createImageFile()
                )
            )
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST_CODE)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }

    private fun getPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            getReadPermission()
        } else {
            openGallery()
        }

    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE)
    }

    private fun getReadPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE))
            showRequestExplanationDialog(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        else
            requestPermission(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            )

    }

    private fun getCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            showRequestExplanationDialog(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            )
        else
            requestPermission(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            )
    }

    private fun requestPermission(permissions: Array<String>) {
        val requestCode =
            if (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE) CAMERA_PERMISSION_REQUEST_CODE else READ_PERMISSION_REQUEST_CODE
        requestPermissions(permissions, requestCode)

    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"

        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ), "Camera"
        )
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        cameraFilePath = "file://" + image.absolutePath
        return image
    }

    private fun showRequestExplanationDialog(permissions: Array<String>) {

        val permissionName = when (permissions[0]) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> getString(R.string.write_external_permission_name)
            else -> getString(R.string.read_external_permission_name)
        }

        val dialogMessage = getString(R.string.permission_dialog_message)

        AlertDialog.Builder(this)
            .setTitle(R.string.permission_dialog_title)
            .setMessage(dialogMessage + permissionName)
            .setNeutralButton(
                R.string.ok
            ) { dialog, _ ->
                dialog.dismiss()
                requestPermission(permissions)
            }
            .create()
            .show()
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.error)
            .setMessage(R.string.error_dialog_message)
            .setNeutralButton(
                R.string.ok
            ) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}