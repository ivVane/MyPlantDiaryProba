package com.vane.android.myplantdiaryproba.ui.main

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vane.android.myplantdiaryproba.R
import com.vane.android.myplantdiaryproba.dto.Plant
import com.vane.android.myplantdiaryproba.dto.Specimen
import kotlinx.android.synthetic.main.main_fragment.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment() {

    private val IMAGE_GALLERY_REQUEST_CODE: Int = 2001
    private val SAVE_IMAGE_REQUEST_CODE: Int = 1999
    private val CAMERA_REQUEST_CODE: Int = 1998
    private val CAMERA_PERMISSION_REQUEST_CODE = 1997
    private val LOCATION_PERMISSION_REQUEST_CODE = 2000
    private val AUTH_REQUEST_CODE = 2002

    private lateinit var viewModel: MainViewModel
    private lateinit var locationViewModel: LocationViewModel

    private lateinit var currentPhotoPath: String
    private var _plantId = 0
    private var user : FirebaseUser? = null

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.plants.observe(this, Observer { plants ->
            actPlantName.setAdapter(
                ArrayAdapter(
                    context!!,
                    R.layout.support_simple_spinner_dropdown_item,
                    plants
                )
            )
        })

        viewModel.specimens.observe(this, Observer { specimens ->
            spn_specimens.setAdapter(
                ArrayAdapter(
                    context!!,
                    R.layout.support_simple_spinner_dropdown_item,
                    specimens
                )
            )
        })

        actPlantName.setOnItemClickListener { parent, view, position, id ->
            var selectedPlant = parent.getItemAtPosition(position) as Plant
            _plantId = selectedPlant.planId
        }

        btnTakePhoto.setOnClickListener {
            prepTakePhoto()
        }

        btnLogon.setOnClickListener {
            logon()
        }

        prepRequestLocationUpdates()

        btnSave.setOnClickListener {
            saveSpecimen()
        }
    }

    private fun logon() {
        var providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .build(), AUTH_REQUEST_CODE
        )
    }

    private fun saveSpecimen() {
        var specimen = Specimen().apply {
            latitude = lblLatitudeValue.text.toString()
            longitude = lblLongitudeValue.text.toString()
            plantName = actPlantName.text.toString()
            description = txtDescription.text.toString()
            datePlanted = txtDatePlanted.text.toString()
            plantId = _plantId
        }

        viewModel.save(specimen)
    }

    private fun prepRequestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationUpdates()
        } else {
            val permissionRequest = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions(permissionRequest, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun requestLocationUpdates() {
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        locationViewModel.getLocationLiveData().observe(this, Observer {
            lblLatitudeValue.text = it.latitude
            lblLongitudeValue.text = it.longitude
        })
    }

    private fun prepOpenImageGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            this.type = "image/*"
            startActivityForResult(this, IMAGE_GALLERY_REQUEST_CODE)
        }
    }

    /**
     * See if we have permission or not.
     */
    private fun prepTakePhoto() {
        if (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            takePhoto()
        } else {
            val permissionRequest = arrayOf(Manifest.permission.CAMERA)
            requestPermissions(permissionRequest, CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(context!!.packageManager)
            if (takePictureIntent == null) {
                Toast.makeText(context, "Unable to save photo", Toast.LENGTH_LONG).show()
            } else {
                // If we are here, we have a valid intent.
                val photoFile: File = createImageFile()
                photoFile?.also {
                    val photoURI = FileProvider.getUriForFile(
                        activity!!.applicationContext,
                        "com.vane.android.myplantdiaryproba.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile)
                    startActivityForResult(takePictureIntent, SAVE_IMAGE_REQUEST_CODE)
                }
            }
        }
    }

    private fun createImageFile(): File {
        // Generate a unique filename with date.
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        // Get access to the directory where we can write pictures.
        val storageDir: File? = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PlantDiary${timestamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, let's go stuff.
                    takePhoto()
                } else {
                    Toast.makeText(
                        context,
                        "Unable to take photo without permission",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocationUpdates()
                } else {
                    Toast.makeText(
                        context,
                        "Unable to update location without permission",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                // Now we can get the thumbnail
                val imageBitmap = data!!.extras!!.get("data") as Bitmap
                imgPlant.setImageBitmap(imageBitmap)
            } else if (requestCode == SAVE_IMAGE_REQUEST_CODE) {
                Toast.makeText(context, "ImageSaved", Toast.LENGTH_LONG).show()
            } else if (requestCode == IMAGE_GALLERY_REQUEST_CODE) {
                if (data != null && data.data != null) {
                    val image = data.data
                    val source = ImageDecoder.createSource(activity!!.contentResolver, image!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    imgPlant.setImageBitmap(bitmap)
                }
            } else if (requestCode == AUTH_REQUEST_CODE) {
                user = FirebaseAuth.getInstance().currentUser
            }
        }
    }
}
