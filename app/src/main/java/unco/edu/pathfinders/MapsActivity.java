package unco.edu.pathfinders;

import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    List<Location> savedLocations;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps); // Linking to the correct layout

        // Obtain the SupportMapFragment and get notified when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getMyLocations();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng lastLocationPlaced = null;

        // Add markers for saved locations
        for (Location location : savedLocations) {
            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latlng)
                    .title("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
            mMap.addMarker(markerOptions);
            lastLocationPlaced = latlng;
        }

        if (lastLocationPlaced != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocationPlaced, 12.0f));
        }

        // Handle marker click events
        mMap.setOnMarkerClickListener(marker -> {
            Integer clicks = (Integer) marker.getTag();
            clicks = clicks == null ? 1 : clicks + 1;
            marker.setTag(clicks);
            Toast.makeText(MapsActivity.this, marker.getTitle() + " clicked " + clicks + " times", Toast.LENGTH_SHORT).show();
            return false;
        });
    }
}
