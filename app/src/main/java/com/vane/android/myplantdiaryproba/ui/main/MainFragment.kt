package com.vane.android.myplantdiaryproba.ui.main

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vane.android.myplantdiaryproba.MainActivity
import com.vane.android.myplantdiaryproba.MapsActivity
import com.vane.android.myplantdiaryproba.R
import com.vane.android.myplantdiaryproba.dto.Event
import com.vane.android.myplantdiaryproba.dto.Photo
import com.vane.android.myplantdiaryproba.dto.Plant
import com.vane.android.myplantdiaryproba.dto.Specimen
import kotlinx.android.synthetic.main.main_fragment.*
import kotlin.collections.ArrayList


class MainFragment : DiaryFragment() {

    private val IMAGE_GALLERY_REQUEST_CODE: Int = 2001
    private val LOCATION_PERMISSION_REQUEST_CODE = 2000
    private val AUTH_REQUEST_CODE = 2002

    private lateinit var viewModel: MainViewModel
    private lateinit var applicationViewModel: ApplicationViewModel
    private var _plantId = 0
    private var user: FirebaseUser? = null
    private var photos: ArrayList<Photo> = ArrayList<Photo>()
    private var specimen = Specimen()
    private var _events = ArrayList<Event>()

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
        applicationViewModel = ViewModelProviders.of(this).get(ApplicationViewModel::class.java)
        activity.let { viewModel = ViewModelProviders.of(it!!).get(MainViewModel::class.java) }

        applicationViewModel.plantService.getLocalPlantDAO().getAllPlants().observe(this, Observer { plants ->
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

        btnMap.setOnClickListener {
            val intent = Intent(context, MapsActivity::class.java)
            startActivity(intent)
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

        btnForward.setOnClickListener {
            (activity as MainActivity).onLeftSwipe()
        }

        spn_specimens.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                specimen = parent?.getItemAtPosition(position) as Specimen
                // Use this specimen objects to populate the fields.
                actPlantName.setText(specimen.plantName)
                txtDescription.setText(specimen.description)
                txtDatePlanted.setText(specimen.datePlanted)
                viewModel.specimen = specimen
                // Trigger an update of the events for this specimen.
                viewModel.fetchEvents()
            }
        }

        rcy_eventsForSpecimens.hasFixedSize()
        rcy_eventsForSpecimens.layoutManager = LinearLayoutManager(context)
        rcy_eventsForSpecimens.itemAnimator = DefaultItemAnimator()
        rcy_eventsForSpecimens.adapter = EventsAdapter(_events, R.layout.row_item)

        viewModel.events.observe(this, Observer { events ->
            // Remove everything that is in there.
            _events.removeAll(_events)
            // Update with the new events that we have observed.
            _events.addAll(events)
            // Tell the recyclerview to update.
            rcy_eventsForSpecimens.adapter!!.notifyDataSetChanged()
        })
    }

    /**
     * Helper function that provides Logon into the app using Email and Google providers.
     */
    private fun logon() {
        var providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .build(), AUTH_REQUEST_CODE
        )
    }

    /**
     * Persist our specimen to long term storage.
     */
    internal fun saveSpecimen() {
        if (user == null) {
            logon()
        }
        user ?: return

        storeSpecimen()

        viewModel.save(specimen, photos, user!!)

        // Clearing the local memory so we can create a brand new objects.
        specimen = Specimen()
        photos = ArrayList<Photo>()
    }

    /**
     * Populate a specimen object based on the details entered into the user interface.
     */
    internal fun storeSpecimen() {
        specimen.apply {
            latitude = lblLatitudeValue.text.toString()
            longitude = lblLongitudeValue.text.toString()
            plantName = actPlantName.text.toString()
            description = txtDescription.text.toString()
            datePlanted = txtDatePlanted.text.toString()
            plantId = _plantId
        }
        viewModel.specimen = specimen
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
        applicationViewModel.getLocationLiveData().observe(this, Observer {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
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
            else -> {
                // Make a call to the superclass to handle the anything not handled here.
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                // Now we can get the thumbnail
                val imageBitmap = data!!.extras!!.get("data") as Bitmap
            } else if (requestCode == SAVE_IMAGE_REQUEST_CODE) {
                Toast.makeText(context, "ImageSaved", Toast.LENGTH_LONG).show()
                var photo = Photo(localUri = photoURI.toString())
                photos.add(photo)
            } else if (requestCode == IMAGE_GALLERY_REQUEST_CODE) {
                if (data != null && data.data != null) {
                    val image = data.data
                    val source = ImageDecoder.createSource(activity!!.contentResolver, image!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                }
            } else if (requestCode == AUTH_REQUEST_CODE) {
                user = FirebaseAuth.getInstance().currentUser
            }
        }
    }
}
