package unco.edu.pathfinders;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private LatLng location1;
    private LatLng location2;
    private FusedLocationProviderClient fusedLocationClient;

    private EditText fromAddress;
    private EditText toAddress;
    private Button plotRouteButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        // Get references to the EditText and Button views
        fromAddress = findViewById(R.id.from_address);
        toAddress = findViewById(R.id.to_address);
        plotRouteButton = findViewById(R.id.btn_plot_route);

        // Set up button click listener to geocode addresses and plot route
        plotRouteButton.setOnClickListener(v -> {
            String fromLocation = fromAddress.getText().toString().trim();
            String toLocation = toAddress.getText().toString().trim();

            if (!fromLocation.isEmpty() && !toLocation.isEmpty()) {
                // Geocode both locations
                geocodeLocation(fromLocation, 1);
                geocodeLocation(toLocation, 2);
            } else {
                Toast.makeText(MainActivity.this, "Please enter both 'From' and 'To' addresses.", Toast.LENGTH_LONG).show();
            }
        });

        // Zoom In and Zoom Out Buttons
        findViewById(R.id.btn_zoom_in).setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        findViewById(R.id.btn_zoom_out).setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Load preference for map type
        SharedPreferences preferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isSatelliteView = preferences.getBoolean("SatelliteView", false);
        mMap.setMapType(isSatelliteView ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);

        // Request location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    // Get the device's current location
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12.0f));
            } else {
                Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);  // Call to super added here
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Geocode the entered location to get LatLng
    private void geocodeLocation(String location, int locationIndex) {
        try {
            // URL encode the location string
            String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8.toString());
            String urlString = "https://maps.googleapis.com/maps/api/geocode/json?address=" + encodedLocation + "&key=AIzaSyBZmGdCsZeGTn42o8lpbGlPk2a1_ukycr0";

            new Thread(() -> {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray results = jsonObject.getJSONArray("results");

                    if (results.length() > 0) {
                        JSONObject locationObject = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                        LatLng latLng = new LatLng(locationObject.getDouble("lat"), locationObject.getDouble("lng"));

                        runOnUiThread(() -> {
                            if (locationIndex == 1) {
                                location1 = latLng;
                            } else {
                                location2 = latLng;
                            }

                            // If both locations are available, plot the route
                            if (location1 != null && location2 != null) {
                                plotRoute(location1, location2);
                            }
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Address not found!", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to plot the route between two locations
    private void plotRoute(LatLng origin, LatLng destination) {
        String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                origin.latitude + "," + origin.longitude + "&destination=" +
                destination.latitude + "," + destination.longitude + "&key=AIzaSyBZmGdCsZeGTn42o8lpbGlPk2a1_ukycr0";

        Log.d("RouteRequest", "Requesting route from " + origin + " to " + destination);  // Debug log

        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                // Parse the API response
                JSONObject jsonObject = new JSONObject(response.toString());
                String status = jsonObject.getString("status");

                Log.d("RouteResponse", "Status: " + status);  // Debug log

                // Check if the API found a route
                if (status.equals("OK")) {
                    JSONArray routes = jsonObject.getJSONArray("routes");
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                    String encodedPolyline = overviewPolyline.getString("points");

                    runOnUiThread(() -> drawPolyline(encodedPolyline));
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "No route found or error: " + status, Toast.LENGTH_LONG).show());
                    Log.e("DirectionsAPI", "Error: " + status);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Decode polyline and draw it on the map
    private void drawPolyline(String encodedPolyline) {
        List<LatLng> decodedPath = PolyUtil.decode(encodedPolyline);
        PolylineOptions polylineOptions = new PolylineOptions().color(0xFF0000FF).width(8);
        for (LatLng latLng : decodedPath) {
            polylineOptions.add(latLng);
        }
        mMap.addPolyline(polylineOptions);

        // Automatically adjust the zoom and camera to show the entire route
        if (location1 != null && location2 != null) {
            LatLngBounds bounds = LatLngBounds.builder()
                    .include(location1)  // Include the first location
                    .include(location2)  // Include the second location
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));  // Padding of 100
        }
    }
}
