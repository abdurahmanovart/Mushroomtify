package ai.arturxdroid.mushroomtify

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 200
    private val FILES_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews() {
        takePictureImageButton.setOnClickListener {
            handleCameraPermission()
            takePicture()
        }
        
        openGalleryImageButton.setOnClickListener{
            handleFilesPermission()
            openGallery()
        }

    }

    private fun takePicture() {
        TODO("handle getting image")
    }

    private fun openGallery() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun handleFilesPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionRationaleDialog(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            requestOnePermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }    }

    private fun handleCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showPermissionRationaleDialog(Manifest.permission.CAMERA)
            }
            requestOnePermission(Manifest.permission.CAMERA)
        }
    }

    private fun showPermissionRationaleDialog(permission: String) {

        val permissionName = when (permission) {
            Manifest.permission.CAMERA -> getString(R.string.camera_permission_name)
            else -> getString(R.string.files_permission_name)
        }

        val dialogMessage = getString(R.string.permission_dialog_message)

        AlertDialog.Builder(this)
            .setTitle(R.string.permission_dialog_title)
            .setMessage(dialogMessage + permissionName)
            .setNeutralButton(
                R.string.ok
            ) { dialog, _ ->
                dialog.dismiss()
                requestOnePermission(permission)
            }
            .create()
            .show()
    }

    private fun requestOnePermission(permission: String) {
        val requestCode = if (permission == Manifest.permission.CAMERA) CAMERA_REQUEST_CODE else FILES_REQUEST_CODE
        requestPermissions(arrayOf(permission),requestCode)
    }
}