package com.vane.android.myplantdiaryproba.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.vane.android.myplantdiaryproba.R
import com.vane.android.myplantdiaryproba.dto.Specimen


/**
 * A simple [Fragment] subclass.
 * Use the [DiaryMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiaryMapFragment : DiaryFragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var specimens: List<Specimen>
    private lateinit var mMap: GoogleMap
    private var mapReady = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_diary_map, container, false)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap
            mapReady = true
            updateMap()
        }

        return rootView
    }

    @Override
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity.let {
            viewModel = ViewModelProviders.of(it!!).get(MainViewModel::class.java)
            viewModel.specimens.observe(this, Observer { specimens ->
                this.specimens = specimens
                updateMap()
            })
        }
    }

    private fun updateMap() {
        if (mapReady && specimens != null) {
            specimens.forEach { specimen ->
                if (!specimen.longitude.isEmpty() && !specimen.latitude.isEmpty()) {
                    val marker = LatLng(specimen.latitude.toDouble(), specimen.longitude.toDouble())
                    mMap.addMarker(MarkerOptions().position(marker).title(specimen.toString()))
                }
            }
        }
    }
}