package com.bonnjalal.sowitmapboxtest.ui.main

import androidx.lifecycle.*
import com.bonnjalal.sowitmapboxtest.model.PolygonModel
import com.bonnjalal.sowitmapboxtest.repository.PolygonRepository
import com.mapbox.geojson.Point
import kotlinx.coroutines.launch

class MainViewModel(private val repository: PolygonRepository) : ViewModel() {

    /**
     * Get All stored polygons
     */
    val allPolygons: LiveData<List<PolygonModel>> = repository.allPolygons.asLiveData()

    /**
     * insert new polygon data
     */
    fun insert(polygonModel: PolygonModel) = viewModelScope.launch {
        repository.insert(polygonModel)
        selectSpinnerPosition(-1)
    }

    /**
     * delete a polygon item
     */
    fun delete(polygonModel: PolygonModel) = viewModelScope.launch {
        repository.delete(polygonModel)
        selectSpinnerPosition(0)
    }


    private val mutableSelectedItem = MutableLiveData<PolygonModel>()

    val selectedItem: LiveData<PolygonModel> get() = mutableSelectedItem

    fun selectItem(item: PolygonModel?) {
        mutableSelectedItem.value = item!!
    }



    private val mutableAllItem = MutableLiveData<ArrayList<Point>>()
    val mapListPoint: LiveData<ArrayList<Point>> get() = mutableAllItem

    fun setMapListPoint(mapList: ArrayList<Point>) {
        mutableAllItem.value = mapList
    }

    private val mutableSpinnerSelectedPosition = MutableLiveData<Int>()

    val spinnerSelectedPosition: LiveData<Int> get() = mutableSpinnerSelectedPosition

    fun selectSpinnerPosition(position: Int?) {
        mutableSpinnerSelectedPosition.value = position!!
    }

}

class MainViewModelFactory(private val repository: PolygonRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
