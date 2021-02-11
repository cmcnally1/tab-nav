package android.cmcnall1.tabnavigation

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.sothree.slidinguppanel.ScrollableViewHelper
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), PlaceSelectionListener, MainPresenter.View,
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val presenter: MainPresenter by inject()

    override fun onMarkerClick(p0: Marker?): Boolean = false

    private lateinit var map: GoogleMap

    private lateinit var lastLocation: Location

    private lateinit var locationCallback: LocationCallback

    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    //Buttons
    private lateinit var locationButton: Button

    //View Variables
    private lateinit var buttonView: View
    private lateinit var recyclerView: View
    private lateinit var cardView: View

    private val apiKey = "AIzaSyA2W69fcSdTDZEptQSLI5Q7iKn3E7rljvY"

    //Variables to hold the user's preferred longitude and latitude
    private lateinit var userLongitude: String
    private lateinit var userLatitude: String

    //Divider declaration for recycler view
    private lateinit var divider: DividerItemDecoration

    //Fused location provider client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                //Show sliding panel in the Explore tab including map
                sliding_layout.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                //Hide sliding panel in the My Car tab including map
                sliding_layout.visibility = View.GONE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                //Hide sliding panel in the Settings tab including map
                sliding_layout.visibility = View.GONE
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Hide the Action Bar/Title Bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        setContentView(R.layout.activity_main)

        // Initialize Places.
        Places.initialize(applicationContext, apiKey)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Attach the view
        presenter.attachView(this)

        //Initialize the buttons
        locationButton = findViewById(R.id.location_button)

        //Divider declaration for recycler view
        divider = DividerItemDecoration(applicationContext,
            DividerItemDecoration.VERTICAL)

        //Assigning views
        buttonView = findViewById(R.id.buttonLayout)
        recyclerView = findViewById(R.id.recyclerView_main)
        cardView = findViewById(R.id.cardView)

        //Layout Manager for the recycler view
        recyclerView_main.layoutManager = LinearLayoutManager(this)

        //Create a value to hold the autocomplete fragment
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment?

        //Use Place.Field to choose what information you would like
        //to return from the Places API
        autocompleteFragment?.setPlaceFields(
            Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,
                Place.Field.NAME))

        //Listen to what he user types in the autocomplete fragment
        //If a place is selected sucessfully, go to onPlaceSelected()
        //If an error occurs, got to onError()
        autocompleteFragment?.setOnPlaceSelectedListener(this)

        //Initialise the Fused Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
//                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }

        createLocationRequest()

        //Button Listeners
        locationButton.setOnClickListener {
            getCurrentLocation()
        }

        //Set a variable for the sliding panel
        var slidingLayout: SlidingUpPanelLayout = findViewById(R.id.sliding_layout)

        //Set the anchor point. This is an intermediate stopping point for the sliding panel
        slidingLayout.anchorPoint = 0.22f
        //Make the recyclerView for the list of charge points scrollable within the sliding panel
        slidingLayout.setScrollableView(recyclerView_main)

        //Set a listener for the tab bar at the bottom of the activity
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onPlaceSelected(place: Place) {
        //Latitude/Longitude object containing the places lat/long
        if(place.latLng != null) {
            val latLng: LatLng = place.latLng!!


            //Assign the lat and long of the place to the relevant variables
            userLatitude = latLng.latitude.toString()
            userLongitude = latLng.longitude.toString()

            presenter.usePlaceLocation(userLongitude, userLatitude)

            showPlacePicked(latLng)
        }
    }

    override fun onError(place: Status) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener(this){ location ->

            if(location != null){

                var long = location.longitude.toString()
                var lat = location.latitude.toString()
                //Create a latLng object for the current latitude and longitude
                var currentLatLng = LatLng(location.latitude, location.longitude)
                //Move camera to show the current user position on the map
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))

                presenter.getCurrentLocation(long,lat)
            }
        }
    }

    override fun showChargePoints(chargePoints: Array<PointInfo?>) {

        runOnUiThread{
            //Pass the charge points to the adapter to be displayed
            recyclerView_main.adapter = MainAdapter(chargePoints)

            //Draw a grey divider between elements of the recyclerView
            divider.setDrawable(ShapeDrawable().apply {
                intrinsicHeight = resources.getDimensionPixelOffset(R.dimen.divider_dimen)
                paint.color = Color.GRAY
            })
            //Apply the divider to the view
            recyclerView_main.addItemDecoration(divider)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        setUpMap()    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true

        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            //Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng){

        val markerOptions = MarkerOptions().position(location)

        val titleStr = getAddress(location)
        markerOptions.title(titleStr)

        map.addMarker(markerOptions)
    }

    //function to show the place searched for and picked by the user on the map
    private fun showPlacePicked(location: LatLng){
        val markerOptions = MarkerOptions().position(location)

        val titleStr = getAddress(location)
        markerOptions.title(titleStr)

        //Clear all markers/items from map
        map.clear()
        //Add the new searched for marker on the map
        map.addMarker(markerOptions)
        //Zoom into the new marker
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16f))
    }

    private fun getAddress(latLng: LatLng): String {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try{
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for(i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" +
                            address.getAddressLine(i)
                }
            }
        } catch (e: IOException){
            Log.e("MapsActivity", e.localizedMessage)
        }
        return addressText
    }

    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MainActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    // 1
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }


    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        private const val REQUEST_CHECK_SETTINGS = 2

        private const val PLACE_PICKER_REQUEST = 3
    }


    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}
