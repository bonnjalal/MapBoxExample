package com.bonnjalal.sowitmapboxtest

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bonnjalal.sowitmapboxtest.adapter.CustomDropDownAdapter
import com.bonnjalal.sowitmapboxtest.application.MapBoxApplication
import com.bonnjalal.sowitmapboxtest.databinding.MainActivityBinding
import com.bonnjalal.sowitmapboxtest.model.PointList
import com.bonnjalal.sowitmapboxtest.model.PolygonModel
import com.bonnjalal.sowitmapboxtest.ui.main.MainFragment
import com.bonnjalal.sowitmapboxtest.ui.main.MainViewModel
import com.bonnjalal.sowitmapboxtest.ui.main.MainViewModelFactory
import com.mapbox.geojson.Point

class MainActivity : AppCompatActivity(){

    //View binding
    private lateinit var binding: MainActivityBinding
    var autoSelection: Boolean = false
    private val polygonViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MapBoxApplication).repository)
    }

    var isSpinnerInitial: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.myToolbar)

        polygonViewModel.allPolygons.observe(this, Observer { polygons ->
            // Update the cached copy of the polygons in the adapter.

            val customDropDownAdapter = CustomDropDownAdapter(this)
            polygons?.let { customDropDownAdapter.setDataList(it as ArrayList<PolygonModel>) }
            binding.dataSpinner.adapter = customDropDownAdapter


            binding.dataSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (isSpinnerInitial){
                        isSpinnerInitial = false
                    }else if(!autoSelection) {
                        //polygonViewModel.selectItem(polygons[position])
                        polygonViewModel.selectSpinnerPosition(position)
                        autoSelection =false
                    }else{
                        //polygonViewModel.selectItem(polygons[position])
                        autoSelection =false
                    }
                }
            }

            polygonViewModel.spinnerSelectedPosition.observe(this, Observer { position ->
                // Perform an action with the latest position
                if (position == -1){
                    autoSelection =true
                    //polygonViewModel.selectSpinnerPosition(polygons.size -1)
                    binding.dataSpinner.setSelection(polygons.size -1)
                }else
                {
                    binding.dataSpinner.setSelection(position)
                }

            })
        })
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
    }
}