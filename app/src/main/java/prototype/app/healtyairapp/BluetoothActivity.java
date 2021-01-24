package prototype.app.healtyairapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    public static String urlString = "";
    public static String notificationText = "";
    public static boolean isHarmful = false;
    public static boolean dataFetched = false;
    static final    UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    TextView status;
    Button goMapButton;
    Button backToNameButton,search_blutooth_devices_button;
    int[] bluetoothValues = new int[2];
    BluetoothAdapter btAdapter;
    public static String MACAdress = "00:19:10:08:2A:14";
    FusedLocationProviderClient fusedLocationProviderClient;


    public static String nameSurname = "Mr/Mrs. ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blutooth);
        goMapButton = findViewById(R.id.next_to_map_activity);
        backToNameButton = findViewById(R.id.back_to_name_activity);
        status = findViewById(R.id.status);

        statusCheck();

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null){
            //Means your Device doesn' t support bluetooth
            status.setText("Device doesn't support Bluetooth");
        }

        if (!btAdapter.isEnabled()) {
            //Request to open blutooth from your phone
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        scanDevice();

        getBluetoothData();

        fetchFromService();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        goMapButton.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                if(isHarmful){
                    notifyUser();
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        //When permission granted
                        getLocation();

                    } else {

                        //When permission denied
                        ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                    }

                    Intent intentToMap = new Intent(BluetoothActivity.this,MapActivity.class);
                    startActivity(intentToMap);

                }
                else{
                    notifyUser();

                }

            }
        });

        backToNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToName = new Intent(BluetoothActivity.this, NameActivity.class);
                startActivity(intentToName);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notifyUser(){
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        String CHANNEL_ID="MYCHANNEL";
        NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID,"name", NotificationManager.IMPORTANCE_LOW);
        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),1,intent,0);
        Notification notification=new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentText("Air Quality Info")
                .setContentTitle(notificationText)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.sym_action_chat,"Title",pendingIntent)
                .setChannelId(CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_action_chat)
                .build();

        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(1,notification);

    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void getBluetoothData(){

        BluetoothDevice hc05 = btAdapter.getRemoteDevice(MACAdress);
        BluetoothSocket btSocket= null;
        int counter = 0;
        do{
            try{
                btSocket = hc05.createRfcommSocketToServiceRecord(myUUID);

                btSocket.connect();
                status.setText("Socket conection : " + btSocket.isConnected());
            }catch (IOException e ){
                e.printStackTrace();
            }
            counter++;
        }while(!btSocket.isConnected() && counter < 2);
        try{
            OutputStream outputStream = btSocket.getOutputStream();
            outputStream.write(48);
        }catch (IOException e ){
            e.printStackTrace();
        }
        String result = "";
        try {
            InputStream inputStream = btSocket.getInputStream();
            inputStream.skip(inputStream.available());

            for(int i = 0; i < 2; i++){
                byte b = (byte) inputStream.read();
                result += b +"," ;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            String[] measurementStrings = result.split(",");
            bluetoothValues[0] = Integer.parseInt(measurementStrings[0]);
            bluetoothValues[1] = Integer.parseInt(measurementStrings[1]);

        }catch(Exception e ){
            //  These values are imported to simulate the app in case of no bluetooth connection
            bluetoothValues[0] = 410;
            bluetoothValues[1] = 71;
        }

        try{
            btSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void fetchFromService(){

        urlString = String.format("https://firla-277516.ey.r.appspot.com/?meas1=%d&meas2=%d", bluetoothValues[0],bluetoothValues[1]);

        FetchData process = new FetchData();

        process.execute();

    }

    public void scanDevice(){
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        Boolean hc05Found = false;
        if (pairedDevices.size() > 0) {

            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {

                String deviceName = device.getName();
                MACAdress = device.getAddress(); // MAC address
                if (deviceName.contains("HC-05") || deviceName.contains("hc-05")){
                    hc05Found = true;
                }
            }
        }

        if(!hc05Found){
            //deviceFound.setText("Device found : NO HC05_DEVICE_FOUND" );
        }
    }



    public class FetchData extends AsyncTask<Void,Void,Void> {
        String data = "";

        HashMap<String,Integer> allDataMap = new HashMap<>();

        @Override
        protected Void doInBackground(Void... voids) {
            try{

                URL url = new URL(BluetoothActivity.urlString);
                HttpURLConnection httpUrlConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = httpUrlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line = "";
                while(line != null){
                    line = bufferedReader.readLine();
                    data = data + line;
                }
            }
            catch(MalformedURLException e ){
                e.printStackTrace();
            }catch(IOException e ){
                e.printStackTrace();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            String result = this.data.substring(13,14);

            if(result.equals("1")){
                BluetoothActivity.notificationText = BluetoothActivity.nameSurname + " This place is harmful";
                BluetoothActivity.isHarmful = true;


            }else if (result.equals("0")){

                BluetoothActivity.notificationText = BluetoothActivity.nameSurname + " This place is safe";
                BluetoothActivity.isHarmful = false;

            }
            else{
                BluetoothActivity.notificationText = "Data can' t be retrieved. EXPLORE and GET AIR QUALITY INFO again";

            }

            BluetoothActivity.dataFetched = true;

        }


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
                        Geocoder geocoder = new Geocoder(BluetoothActivity.this,
                                Locale.getDefault());
                        //Initilize address list
                        List<Address> adresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );

                        MapActivity.lat = adresses.get(0).getLatitude();
                        MapActivity.lon = adresses.get(0).getLongitude();


                    } catch (IOException e) {
                        e.printStackTrace();

                    }

                }

            }
        });

    }


}

