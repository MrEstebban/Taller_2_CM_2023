package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.taller2.databinding.ActivityGoogleMapsBinding;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class GoogleMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityGoogleMapsBinding binding;
    private Geocoder mGeocoder;

    SensorManager sensorManager;
    Sensor lightSensor;
    SensorEventListener lightSensorListener;

    // Variables de UI
    EditText mAddress;
    private static final String TAG = GoogleMapsActivity.class.getName();
    private Logger logger = Logger.getLogger(TAG);
    // Constantes de permisos
    private final int LOCATION_PERMISSION_ID = 103;
    String locationPerm = Manifest.permission.ACCESS_FINE_LOCATION;
    // Variables de localización
    private FusedLocationProviderClient mFusedLocationClient;

    //Ubicaciones
    public static final double lowerLeftLatitude = 4.555039;
    public static final double lowerLeftLongitude = -74.268697;
    public static final double upperRightLatitude = 4.800039;
    public static final double upperRightLongitude = -73.862203;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGoogleMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Variables de UI
        mAddress = binding.address;

        // Initialize the sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Initialize th listener
        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (mMap != null) {
                    if (event.values[0] < 5000) {
                        Log.i("MAPS", "DARK MAP " + event.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(GoogleMapsActivity.this, R.raw.style_night));
                    } else {
                        Log.i("MAPS", "LIGHT MAP " + event.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(GoogleMapsActivity.this, R.raw.style_day));
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };

        // Initialize the geocoder
        mGeocoder = new Geocoder(getBaseContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(lightSensorListener,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(lightSensorListener);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Inicializar el FusedLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // LongClick Listener
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                try {
                    List<Address> foundLocations = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 2);
                    if (foundLocations != null && !foundLocations.isEmpty()) {
                        String firstLocationName = foundLocations.get(0).getAddressLine(0);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title(firstLocationName));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }});

        requestPermission(this, locationPerm, "Permiso para utilizar la localización", LOCATION_PERMISSION_ID);
        setCurrentLocationOnMap();

    }

    private void requestPermission(Activity context, String permission, String justification, int id) {
        // Verificar si no hay permisos
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                Toast.makeText(context, justification, Toast.LENGTH_SHORT).show();
            }
            // Request the permission
            ActivityCompat.requestPermissions(context, new String[]{permission}, id);
        }
    }

    private void setCurrentLocationOnMap () {
        if (ContextCompat.checkSelfPermission(this, locationPerm) != PackageManager.PERMISSION_GRANTED) {
            logger.warning("Failed to getting the location permission :(");
        } else {
            logger.info("Success getting the location permission :)");
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                logger.info("onSuccessLocation");
                if (location != null) {


                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(loc).title("Current location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                    // Zoom
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    // Gestures
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                    // Zoom Buttons
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                }

            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_ID) {
            setCurrentLocationOnMap();
        }
    }

    public void refresh (View view) {
        setCurrentLocationOnMap();
    }

    public void search(View view) {
        mMap.clear();
        findAddress();
    }

    private void findAddress() {
        String addressString = mAddress.getText().toString();
        if (!addressString.isEmpty()) {
            try {
                List<Address> addresses = mGeocoder.getFromLocationName(addressString,2,
                        lowerLeftLatitude, lowerLeftLongitude,
                        upperRightLatitude, upperRightLongitude);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addressResult = addresses.get(0);
                    LatLng position = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
                    if (mMap != null) {
                        mMap.addMarker(new MarkerOptions().position(position)
                                .title(addressResult.getFeatureName())
                                .snippet(addressResult.getAddressLine(0))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                    }
                } else {
                    Toast.makeText(GoogleMapsActivity.this, "Dirección no encontrada", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(GoogleMapsActivity.this, "La dirección está vacía", Toast.LENGTH_SHORT).show();
        }
    }
}