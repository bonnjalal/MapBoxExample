package com.bonnjalal.sowitmapboxtest.ui.fragments


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bonnjalal.sowitmapboxtest.R
import com.bonnjalal.sowitmapboxtest.databinding.CustomPolygonDialogBinding
import com.bonnjalal.sowitmapboxtest.model.PointList
import com.bonnjalal.sowitmapboxtest.model.PolygonModel
import com.bonnjalal.sowitmapboxtest.ui.main.MainViewModel
import com.mapbox.geojson.Point


class PolygonDialog: DialogFragment() {

    private var _binding: CustomPolygonDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    var position = 0

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog!!.getWindow()?.setBackgroundDrawableResource(R.drawable.back_spinner)
         //inflater.inflate(R.layout.custom_dialog_fragment.xml, container, false)
        _binding = CustomPolygonDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel.mapListPoint.observe(viewLifecycleOwner, Observer { mapList ->
            // Perform an action with the latest item data

            val mapPointList: ArrayList<Point> = mapList
            val  polygonArea = arguments?.getLong("polygonArea", 0).toString() + " ha"
            binding.tvPolygonArea.text = polygonArea

            binding.etPolygonLocation.setText(arguments?.getString("polygonLocation"))

            // save polygon data to database
            binding.btnSavePolygon.setOnClickListener(View.OnClickListener {
                val polygonName = binding.etPlygonName.text.toString()
                val polygonLocation = binding.etPolygonLocation.text.toString()
                val polygonModel = PolygonModel(polygonName, polygonLocation, polygonArea, PointList(mapPointList))

                viewModel.insert(polygonModel)
                position = -1

                dialog!!.dismiss()
            })


        })
        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.selectSpinnerPosition(position)
    }
    override fun onStart() {
        super.onStart()

        // set width and height of dialog on fragment start
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}