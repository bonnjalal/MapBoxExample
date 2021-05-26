package com.bonnjalal.sowitmapboxtest.ui.main

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bonnjalal.sowitmapboxtest.MainActivity
import com.bonnjalal.sowitmapboxtest.R
import com.bonnjalal.sowitmapboxtest.databinding.MainFragmentBinding
import com.bonnjalal.sowitmapboxtest.model.PointList
import com.bonnjalal.sowitmapboxtest.ui.fragments.PolygonDialog
import com.google.android.material.snackbar.Snackbar
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.core.exceptions.ServicesException
import com.mapbox.geojson.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfConstants.UNIT_KILOMETERS
import com.mapbox.turf.TurfMeasurement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.absoluteValue


class MainFragment : Fragment(), OnMapReadyCallback {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()

    //Cashing size
    val DESIRED_AMBIENT_CACHE_SIZE = 5000000L

    //View binding
    private var _binding: MainFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //private lateinit var viewModel: MainViewModel

    //private var mapView: MapView? = null
    private var activity: Activity? = null
    private var mapboxMap: MapboxMap? = null

    private val DISTANCE_UNITS = UNIT_KILOMETERS
    private val CIRCLE_SOURCE_ID = "circle-source-id"
    private val FILL_SOURCE_ID = "fill-source-id"
    private val LINE_SOURCE_ID = "line-source-id"
    private val CIRCLE_LAYER_ID = "circle-layer-id"
    private val FILL_LAYER_ID = "fill-layer-polygon-id"
    private val LINE_LAYER_ID = "line-layer-id"
    private var fillLayerPointList: ArrayList<Point> = ArrayList()
    private var lineLayerPointList: ArrayList<Point> = ArrayList()
    private var circleLayerFeatureList: ArrayList<Feature> = ArrayList()
    private var listOfList: ArrayList<List<Point>>? = null
    private var circleSource: GeoJsonSource? = null
    private var fillSource: GeoJsonSource? = null
    private var lineSource: GeoJsonSource? = null
    private var firstPointOfPolygon: Point? = null
    private var firstLatLngOfPolygon: LatLng? = null
    private var firstPositionTouchPoint: android.graphics.PointF? = null
    private var positionTouchPoint: android.graphics.PointF? = null
    private var polygonArea: Double = 0.0

    private var mapPointList: ArrayList<Point> = ArrayList()

    private var isPolygonSelected: Boolean = false
    var location = "location"

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        activity = getActivity() as MainActivity?

        activity?.let { Mapbox.getInstance(it, getString(R.string.mapbox_access_token)) }
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        val view = binding.root


        //Managing MapBox caching
        val fileSource = activity?.let { OfflineManager.getInstance(it) }

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        fileSource?.setMaximumAmbientCacheSize(
                DESIRED_AMBIENT_CACHE_SIZE,
                object : OfflineManager.FileSourceCallback {
                    override fun onSuccess() {

                    }

                    override fun onError(message: String) {

                    }
                })

        return view
        //return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(
                Style.MAPBOX_STREETS
        ) { style ->
            // Add sources to the map
            circleSource = initCircleSource(style)
            fillSource = initFillSource(style)
            lineSource = initLineSource(style)

            // Add layers to the map
            initCircleLayer(style)
            initLineLayer(style)
            initFillLayer(style)

            viewModel.allPolygons.observe(viewLifecycleOwner, Observer { polygons ->
                // Perform an action with the latest item data

                viewModel.spinnerSelectedPosition.observe(viewLifecycleOwner, Observer { position ->
                    // Perform an action with the latest item data
                    if (position < polygons.size){
                        if (position == -1) {
                            //polygons[polygons.size - 1].polygonObjectList?.let { drawPolygon(it, false) }
                        } else {
                            polygons[position].polygonObjectList?.let { drawPolygon(it) }
                        }
                    }


                    binding.deleteButton.setOnClickListener {
                        if (position < polygons.size){
                            if (position == -1){
                                viewModel.delete(polygons[polygons.size - 1])
                            }else{
                                viewModel.delete(polygons[position])
                            }
                        }

                        clearEntireMap()
                        //viewModel.selectSpinnerPosition(0)
                        binding.deleteButton.visibility = View.GONE
                    }
                })

            })

        }
        //clear map
        binding.clearButton.setOnClickListener {
            clearEntireMap()
        }

        //get clicked position coordination.
        mapboxMap.addOnMapClickListener { point ->

            binding.clearButton.visibility = View.VISIBLE

            if (isPolygonSelected) {
                clearEntireMap()
            }
            setupPolygonSelection(point)

            true
        }
        mapboxMap.addOnMapLongClickListener {
            if (circleLayerFeatureList.size >= 3 && !isPolygonSelected) {
                firstLatLngOfPolygon?.let { it1 -> setupPolygonSelection(it1) }
            }
            true
        }

        val snackBarMessage = "Click one click on the map to draw a polygon, and long click to close polygon"
        showSnackBar(snackBarMessage, activity)
    }

    fun showSnackBar(message: String?, activity: Activity?) {
        if (null != activity && null != message) {
            val snackbar = Snackbar.make(
                    activity.findViewById(android.R.id.content),
                    message, Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setAction("Ok") { snackbar.dismiss() }.show()
        }
    }

    private fun setupPolygonSelection(point: LatLng) {

        // Use the map click location to create a Point object
        var mapTargetPoint = Point.fromLngLat(
                point.longitude,
                point.latitude
        )


        // Make note of the first map click location so that it can be used to create a closed polygon later on
        if (circleLayerFeatureList.isEmpty()) {
            firstPointOfPolygon = mapTargetPoint
            firstLatLngOfPolygon = point
            firstPositionTouchPoint = mapboxMap!!.projection.toScreenLocation(point)

        } else {
            positionTouchPoint = mapboxMap!!.projection.toScreenLocation(point)
        }
        /*else if (((mapTargetPoint.latitude().absoluteValue - firstPointOfPolygon!!.latitude().absoluteValue).absoluteValue <= 0.1)
            && ((mapTargetPoint.longitude().absoluteValue - firstPointOfPolygon!!.longitude().absoluteValue).absoluteValue <= 0.1)){
            mapTargetPoint = firstPointOfPolygon as Point
        }
         */

        if (circleLayerFeatureList.size >= 3) {
            val subtractionX = firstPositionTouchPoint!!.x.toInt().pixelsToDp() - positionTouchPoint!!.x.toInt().pixelsToDp()
            val subtractionY = firstPositionTouchPoint!!.y.toInt().pixelsToDp() - positionTouchPoint!!.y.toInt().pixelsToDp()
            if ((subtractionX.absoluteValue <= 10) && (subtractionY.absoluteValue <= 10)) {
                firstPositionTouchPoint = null
                mapTargetPoint = firstPointOfPolygon as Point
            }
        }


        mapPointList.add(mapTargetPoint)
        // Add the click point to the circle layer and update the display of the circle layer data

        circleLayerFeatureList.add(Feature.fromGeometry(mapTargetPoint))
        circleSource?.setGeoJson(FeatureCollection.fromFeatures(circleLayerFeatureList))

        // Add the click point to the line layer and update the display of the line layer data
        lineLayerPointList.add(mapTargetPoint)
        lineSource?.setGeoJson(
                FeatureCollection.fromFeatures(
                        arrayOf(
                                Feature.fromGeometry(
                                        LineString.fromLngLats(lineLayerPointList)
                                )
                        )
                )
        )

        // Add the click point to the fill layer and update the display of the fill layer data
        fillLayerPointList.add(mapTargetPoint)

        listOfList = ArrayList()
        listOfList!!.add(fillLayerPointList)

        if ((mapTargetPoint == firstPointOfPolygon) && circleLayerFeatureList.size > 1) {
            isPolygonSelected = true

            val finalFeatureList: MutableList<Feature> = ArrayList()
            finalFeatureList.add(Feature.fromGeometry(Polygon.fromLngLats(listOfList!!)))
            val newFeatureCollection = FeatureCollection.fromFeatures(finalFeatureList)
            fillSource?.setGeoJson(newFeatureCollection)

            // get polygon Area in meters
            polygonArea = TurfMeasurement.area(newFeatureCollection)
            //Calculate rounded Area in Hectare
            val roundedArea = (Math.round(polygonArea * 100) / 100) / 10000
            /*
            Toast.makeText(activity,
                    "$roundedArea ha",
                    Toast.LENGTH_LONG).show()

             */

            reverseGeocode(mapPointList, roundedArea, Point.fromLngLat(point.longitude, point.latitude))
            //val location: String =

            binding.deleteButton.visibility = View.VISIBLE
            binding.clearButton.visibility = View.VISIBLE

            //showPolygonDialog(mapPointList, roundedArea, location)

            mapPointList = ArrayList()

        }

    }

    /**
     * Get Location name and show Polygon dialog
     */
    private fun reverseGeocode(mapPointList: ArrayList<Point>, roundedArea: Long, point: Point) {

        try {
            /*
            Toast.makeText(
                    activity,
                    "getting location, please wait...", Toast.LENGTH_LONG
            ).show()
             */

            // show retrieving location text
            binding.retrieveLocationBar.visibility = View.VISIBLE
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            val client: MapboxGeocoding = MapboxGeocoding.builder()
                    .accessToken(getString(R.string.mapbox_access_token))
                    .query(point)
                    .geocodingTypes()
                    .build()

            client.enqueueCall(object : Callback<GeocodingResponse?> {
                override fun onResponse(
                        call: Call<GeocodingResponse?>?,
                        response: Response<GeocodingResponse?>
                ) {

                    if (response.body() != null) {
                        val results: List<CarmenFeature> = response.body()!!.features()
                        if (results.isNotEmpty()) {
                            val feature: CarmenFeature = results[0]

                            /*
                            Toast.makeText(
                                    activity,
                                    "place: " + feature.placeName().toString(), Toast.LENGTH_LONG
                            ).show()

                             */


                            location = feature.placeName().toString()
                            saveDataAndHideProgressBar(mapPointList, roundedArea, location)


                        } else {

                            /*
                            Toast.makeText(
                                    activity,
                                    "No location name",
                                    Toast.LENGTH_SHORT
                            ).show()

                             */

                            location = "location"
                            saveDataAndHideProgressBar(mapPointList, roundedArea, location)
                        }
                    } else {

                        Toast.makeText(
                                activity,
                                "GeoCoding can't get location, make sure you're connected to internet",
                                Toast.LENGTH_LONG
                        ).show()
                        saveDataAndHideProgressBar(mapPointList, roundedArea, location)
                    }

                }

                override fun onFailure(call: Call<GeocodingResponse?>?, throwable: Throwable) {
                    //Log.e("GeoCoding Failure", throwable.message, throwable)

                    Toast.makeText(
                            activity,
                            "GeoCoding can't get location, make sure you're connected to internet",
                            Toast.LENGTH_LONG
                    ).show()
                    saveDataAndHideProgressBar(mapPointList, roundedArea, location)
                }
            })
        } catch (servicesException: ServicesException) {
            //Log.e("Error GeoCoding: %s", servicesException.toString())
            servicesException.printStackTrace()


            Toast.makeText(
                    activity,
                    "GeoCoding can't get location, make sure you're connected to internet",
                    Toast.LENGTH_LONG
            ).show()
            saveDataAndHideProgressBar(mapPointList, roundedArea, location)
        }

    }

    private fun saveDataAndHideProgressBar(_mapPointList: ArrayList<Point>, _polygonArea: Long, _polygonLocation: String){
        binding.retrieveLocationBar.visibility = View.GONE
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        showPolygonDialog(_mapPointList, _polygonArea, _polygonLocation)
    }

    /**
     * Show polygon dialog fragment to polygon data
     */

    private fun drawPolygon(pointList: PointList) {

        clearEntireMap()
        val mapPointList: ArrayList<Point>? = pointList.mapPoints

        if (mapPointList != null) {
            //var latitude: Double = 0.0
            //var longitude: Double = 0.0

            val latLngList: ArrayList<LatLng> = ArrayList()

            for (mapTargetPoint in mapPointList) {
                circleLayerFeatureList.add(Feature.fromGeometry(mapTargetPoint))
                circleSource?.setGeoJson(FeatureCollection.fromFeatures(circleLayerFeatureList))

                lineLayerPointList.add(mapTargetPoint)
                lineSource?.setGeoJson(
                        FeatureCollection.fromFeatures(
                                arrayOf(
                                        Feature.fromGeometry(
                                                LineString.fromLngLats(lineLayerPointList)
                                        )
                                )
                        )
                )

                // Add the click point to the fill layer and update the display of the fill layer data
                fillLayerPointList.add(mapTargetPoint)

                listOfList = ArrayList()
                listOfList!!.add(fillLayerPointList)

                //latitude += mapTargetPoint.latitude()
                //longitude += mapTargetPoint.longitude()

                latLngList.add(LatLng(mapTargetPoint.latitude(), mapTargetPoint.longitude()))

            }


            val latLngBounds = LatLngBounds.Builder().includes(latLngList)
            val latLngBounds2: LatLngBounds = latLngBounds.build()

            //latitude /= mapPointList.size
            //longitude /= mapPointList.size

            isPolygonSelected = true

            val finalFeatureList: MutableList<Feature> = ArrayList()
            finalFeatureList.add(Feature.fromGeometry(Polygon.fromLngLats(listOfList!!)))
            val newFeatureCollection = FeatureCollection.fromFeatures(finalFeatureList)
            fillSource?.setGeoJson(newFeatureCollection)

            /*
            //mapboxMap.projection.getMetersPerPixelAtLatitude()
            val position = CameraPosition.Builder()
                    .target(LatLng(latitude, longitude)) // Sets the new camera position
                    .zoom((200000000/roundedArea*2).toDouble()) // Sets the zoom
                    .bearing(360.0) // Rotate the camera
                    .tilt(30.0) // Set the camera tilt
                    .build() // Creates a CameraPosition from the builder

             */
            //mapboxMap!!.animateCamera(CameraUpdateFactory
            //      .newCameraPosition(position), 5000)

            mapboxMap!!.animateCamera(CameraUpdateFactory
                    .newLatLngBounds(latLngBounds2, 80), 5000)


            binding.clearButton.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.VISIBLE
        }
    }

    private fun showPolygonDialog(_mapPointList: ArrayList<Point>, _polygonArea: Long, _polygonLocation: String) {

        viewModel.setMapListPoint(_mapPointList)

        val fragment: DialogFragment = PolygonDialog()
        val args = Bundle()
        args.putLong("polygonArea", _polygonArea)
        args.putString("polygonLocation", _polygonLocation)
        //args.putParcelable("_mapPointList", PointList(_mapPointList))
        fragment.arguments = args
        fragment.show(childFragmentManager, "PolygonFragment")

        //PolygonDialog().show(childFragmentManager, "PolygonDialogFragment")
    }

    /**
     * Remove the drawn area from the map by resetting the FeatureCollections used by the layers' sources
     */
    private fun clearEntireMap() {
        fillLayerPointList = ArrayList()
        circleLayerFeatureList = ArrayList()
        lineLayerPointList = ArrayList()
        circleSource?.setGeoJson(FeatureCollection.fromFeatures(arrayOf()))
        lineSource?.setGeoJson(FeatureCollection.fromFeatures(arrayOf()))
        fillSource?.setGeoJson(FeatureCollection.fromFeatures(arrayOf()))

        mapPointList = ArrayList()
        isPolygonSelected = false

        binding.clearButton.visibility = View.GONE
        binding.deleteButton.visibility = View.GONE
    }

    /**
     * Set up the CircleLayer source for showing map click points
     */
    private fun initCircleSource(loadedMapStyle: Style): GeoJsonSource? {
        val circleFeatureCollection = FeatureCollection.fromFeatures(arrayOf())
        val circleGeoJsonSource = GeoJsonSource(CIRCLE_SOURCE_ID, circleFeatureCollection)
        loadedMapStyle.addSource(circleGeoJsonSource)
        return circleGeoJsonSource
    }

    /**
     * Set up the CircleLayer for showing polygon click points
     */
    private fun initCircleLayer(loadedMapStyle: Style) {
        val circleLayer = CircleLayer(
                CIRCLE_LAYER_ID,
                CIRCLE_SOURCE_ID
        )
        circleLayer.setProperties(
                circleRadius(5f),
                circleColor(Color.parseColor("#d004d3"))
        )
        loadedMapStyle.addLayer(circleLayer)
    }

    /**
     * Set up the FillLayer source for showing map click points
     */
    private fun initFillSource(loadedMapStyle: Style): GeoJsonSource? {
        val fillFeatureCollection = FeatureCollection.fromFeatures(arrayOf())
        val fillGeoJsonSource = GeoJsonSource(FILL_SOURCE_ID, fillFeatureCollection)
        loadedMapStyle.addSource(fillGeoJsonSource)
        return fillGeoJsonSource
    }

    /**
     * Set up the FillLayer for showing the set boundaries' polygons
     */
    private fun initFillLayer(loadedMapStyle: Style) {
        val fillLayer = FillLayer(
                FILL_LAYER_ID,
                FILL_SOURCE_ID
        )
        fillLayer.setProperties(
                fillOpacity(.4f),
                fillColor(Color.parseColor("#f74e4e"))
        )
        loadedMapStyle.addLayerBelow(fillLayer, LINE_LAYER_ID)
    }

    /**
     * Set up the LineLayer source for showing map click points
     */
    private fun initLineSource(loadedMapStyle: Style): GeoJsonSource? {
        val lineFeatureCollection = FeatureCollection.fromFeatures(arrayOf())
        val lineGeoJsonSource = GeoJsonSource(LINE_SOURCE_ID, lineFeatureCollection)
        loadedMapStyle.addSource(lineGeoJsonSource)
        return lineGeoJsonSource
    }

    /**
     * Set up the LineLayer for showing the set boundaries' polygons
     */
    private fun initLineLayer(loadedMapStyle: Style) {
        val lineLayer = LineLayer(
                LINE_LAYER_ID,
                LINE_SOURCE_ID
        )
        lineLayer.setProperties(
                lineColor(Color.WHITE),
                lineWidth(2f)
        )
        loadedMapStyle.addLayerBelow(lineLayer, CIRCLE_LAYER_ID)
    }

    fun Int.pixelsToDp(): Float {
        return this / (requireActivity().resources
                .displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT).toFloat()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
//        binding.mapView.onDestroy()

    }
}