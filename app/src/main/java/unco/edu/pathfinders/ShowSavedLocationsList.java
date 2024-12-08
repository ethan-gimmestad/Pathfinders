package unco.edu.pathfinders;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import android.location.Location;

public class ShowSavedLocationsList extends AppCompatActivity {

    ListView lv_savedLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_saved_locations_list); // Linking to the correct layout

        lv_savedLocations = findViewById(R.id.lv_wayPoints);

        // Fetch saved locations from global application state
        MyApplication myApplication = (MyApplication) getApplicationContext();
        List<Location> savedLocations = myApplication.getMyLocations();

        // Set the adapter to display saved locations
        ArrayAdapter<Location> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, savedLocations);
        lv_savedLocations.setAdapter(adapter);
    }
}
