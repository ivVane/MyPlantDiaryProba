package com.vane.android.myplantdiaryproba.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.vane.android.myplantdiaryproba.MainActivity
import com.vane.android.myplantdiaryproba.R
import com.vane.android.myplantdiaryproba.dto.Event
import kotlinx.android.synthetic.main.event_fragment.*

class EventFragment : DiaryFragment() {

    companion object {
        fun newInstance() = EventFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.event_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity.let { viewModel = ViewModelProviders.of(it!!).get(MainViewModel::class.java) }

        btnSaveEvent.setOnClickListener {
            saveEvent()
        }

        btnTakeEventPhoto.setOnClickListener {
            prepTakePhoto()
        }

        btnBackToSpecimen.setOnClickListener {
            (activity as MainActivity).onSwipeRight()
        }

        // Wire up our recycler view.
        rcyEvents.hasFixedSize()
        rcyEvents.layoutManager = LinearLayoutManager(context)
        rcyEvents.itemAnimator = DefaultItemAnimator()
        rcyEvents.adapter = EventsAdapter(viewModel.specimen.events, R.layout.row_item)
    }

    private fun saveEvent() {
        var event = Event()
        with(event) {
            type = actEventType.text.toString()
            var quantityString = edtQuantity.text.toString()
            if (quantityString.length > 0) {
                quantity = quantityString.toDouble()
            }
            units = actUnits.text.toString()
            date = edtEventDate.text.toString()
            description = edtDescription.text.toString()
            if (photoURI != null) {
                event.localPhotoUri = photoURI.toString()
            }
        }
        viewModel.specimen.events.add(event)
        clearAll()
        rcyEvents.adapter?.notifyDataSetChanged()
    }

    private fun clearAll() {
        edtEventDate.setText("")
        actEventType.setText("")
        edtQuantity.setText("")
        actUnits.setText("")
        edtDescription.setText("")
        photoURI = null
    }
}