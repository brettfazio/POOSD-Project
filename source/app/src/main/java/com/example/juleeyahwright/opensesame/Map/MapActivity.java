package com.example.juleeyahwright.opensesame.Map;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.juleeyahwright.opensesame.CreateReport.CreateReportActivity;
import com.example.juleeyahwright.opensesame.LoginActivity;
import com.example.juleeyahwright.opensesame.R;
import com.example.juleeyahwright.opensesame.SettingsActivity;
import com.example.juleeyahwright.opensesame.SharedPreferencesController;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

/*
MapActivity: the main interface of the app, a google maps that shows the user's location,
reports can be added, and settings page can be accessed
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapController mapController;
    private boolean selectionStateReady;

    // add the map and buttons to the main screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync( this);

        Button addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addReportClicked();
            }
        });

        selectionStateReady = true;
    }

    // changes the state to allow the user to add a marker to the map
    private void addReportClicked() {
        if (!selectionStateReady) {
            System.out.println("changing to add");
            selectionStateReady = true;
            changeAddState();
            return;
        }
        // selectionStateReady is false
        System.out.println("changing to cancel");
        selectionStateReady = false;
        changeAddState();

        Toast.makeText(getApplicationContext(),
                "Select a location on the map to create a report there!",
                Toast.LENGTH_SHORT).show();
    }

    private void resetButtonState() {
        selectionStateReady = true;
        changeAddState();
    }

    // take the user to the report screen to fill out additional material, storing the location selected
    // into the report automatically
    private void intentToReport(LatLng location) {
        Intent intent = new Intent(MapActivity.this, CreateReportActivity.class);
        intent.putExtra("LOCATION", location);
        startActivity(intent);
        resetButtonState();
    }

    private void clicked(LatLng location) {
        if (!selectionStateReady) {
            intentToReport(location);
        }
    }

    // toggles between "add" and "cancel" buttons
    private void changeAddState() {
        Button addButton = (Button) findViewById(R.id.add_button);
        if (selectionStateReady) {
            addButton.setText(getResources().getString(R.string.add_button_ready));
        }else {
            addButton.setText(getResources().getString(R.string.add_button_processing));
        }
    }

    // verifies that the map exists before attempting any other api calls
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapController = new MapController(googleMap, this);
        mapController.checkAndRequestPermissions(MapActivity.this, MapActivity.this);

        mapController.getMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                clicked(latLng);
            }
        });
    }

    // adds a menu to access account
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // depending on the button selected, take the user to the appropriate screen
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.settings_option:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.sign_out_option:
                FirebaseAuth.getInstance().signOut();
                finish();
                SharedPreferencesController.clearSignInData(getApplicationContext());
                intent = new Intent(MapActivity.this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // if permission for location services is granted, the user's location will be displayed on the map
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mapController.setLocationPermission(false);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            mapController.setLocationPermission(true);
        }
        mapController.updateMapLocation(MapActivity.this);
    }
}