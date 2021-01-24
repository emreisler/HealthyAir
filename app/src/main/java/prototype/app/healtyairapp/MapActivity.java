package prototype.app.healtyairapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "1";
    Button backToBlutoothButton, suggest;
    public static double lat, lon;
    FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        backToBlutoothButton = findViewById(R.id.back_to_blutooth_activity);
        suggest = findViewById(R.id.suggest);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        backToBlutoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBackToBlutooth = new Intent(MapActivity.this, BluetoothActivity.class);
                startActivity(intentBackToBlutooth);
            }
        });

        suggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(MapActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //When permission granted
                    getLocation();

                } else {

                    //When permission denied
                    ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }
        });

        //Initilize fragment
        Fragment fragment = new MapsFragment();

        //Open fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();
    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //Initilize location
                Location location = task.getResult();
                if (location != null) {

                    try {
                        //Initilize geoCoder
                        Geocoder geocoder = new Geocoder(MapActivity.this,
                                Locale.getDefault());
                        //Initilize address list
                        List<Address> adresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );

                        lat = adresses.get(0).getLatitude();
                        lon = adresses.get(0).getLongitude();

                        MapsFragment.addMarker("I am here", lat + 0.1, lon + 0.1);
                        MapsFragment.addMarker("I am here", lat - 0.1, lon - 0.1);
                        MapsFragment.addMarker("I am here", lat + 0.1, lon - 0.1);
                        MapsFragment.addMarker("I am here", lat - 0.1, lon + 0.1);
                    } catch (IOException e) {
                        e.printStackTrace();

                    }

                }

            }
        });

    }

}