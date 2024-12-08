package unco.edu.pathfinders;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        Switch mapTypeSwitch = findViewById(R.id.switch_map_type);

        // Load saved preference
        boolean isSatelliteView = preferences.getBoolean("SatelliteView", false);
        mapTypeSwitch.setChecked(isSatelliteView);

        // Toggle listener to save user choice
        mapTypeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("SatelliteView", isChecked);
            editor.apply();
        });

        Button exitSettingsButton = findViewById(R.id.btn_exit_settings);
        exitSettingsButton.setOnClickListener(v -> {
            // Close the current SettingsActivity and return to MainActivity
            finish();
        });

    }
}
