package com.example.hackathon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build; // Added for Build.VERSION_CODES
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat; // Added for notifications
import androidx.core.app.NotificationManagerCompat; // Added for notifications
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
// Other imports remain the same...

import org.bson.Document;
import org.bson.types.ObjectId;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;


//my code
public class driverloc extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myMap;
    private final int FINE_PERMISSION_CODE = 1;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    String Appid = BuildConfig.appId;
    App app;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> mongoCollection, mongoCollection2;
    User user;
    Button startbtn, stopbtn;
    EditText busnostart;
    TextView statustext, curr_bus, bustext;
    String name;
    GoogleMap googleMap;
    LinearLayout statusbar, bottom_status;
    ImageView arrowback;
    LinearLayoutCompat main;
    private static final long UPDATE_INTERVAL = 7000;
    private static final float SMALLEST_DISPLACEMENT = 10f;


    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIFICATION_ID = 1001;

    @SuppressLint({"MissingInflatedId","SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driverloc);
        startbtn = findViewById(R.id.startbtn);
        stopbtn = findViewById(R.id.stopbtn);
        busnostart = findViewById(R.id.busnostart);
        statustext = findViewById(R.id.statustext);
        statusbar = findViewById(R.id.statusbar);
        main = findViewById(R.id.main);
        bottom_status = findViewById(R.id.bottom_status);
        curr_bus = findViewById(R.id.curr_bus);
        bustext = findViewById(R.id.bustext);
        arrowback=findViewById(R.id.arrowback_driver);

        arrowback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(driverloc.this, splashActivity.class);
                startActivity(intent);
                finish();
            }
        });
        createNotificationChannel();

        Intent intent = getIntent();
        String driver_id = intent.getStringExtra("driver_id");
        // location service ko check karaya
        checkLocationServices();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLocation = location;
                }
            }
        }, Looper.getMainLooper());

        app = new App(new AppConfiguration.Builder(Appid).build());
        user = app.currentUser();
        assert user != null;
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("location");
        mongoCollection = mongoDatabase.getCollection("driverloc");
        mongoCollection2 = mongoDatabase.getCollection("parked");

        Document filter = new Document("userid", user.getId());
        mongoCollection.findOne(filter).getAsync(result -> {
            if (result.isSuccess()) {
                if (result.get() != null) {
                    String running_bus = result.get().getString("busno");
                    Toast.makeText(driverloc.this, "Bus Running", Toast.LENGTH_SHORT).show();
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    assert mapFragment != null;
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(driverloc.this);
                    }
                    handler.post(updateLocationRunnable);
                    startbtn.setVisibility(View.GONE);
                    stopbtn.setVisibility(View.VISIBLE);
                    statustext.setText("Running");
                    statusbar.setBackgroundColor(Color.parseColor("#3EC543"));
                    main.setBackgroundColor(Color.parseColor("#3EC543"));
                    bottom_status.setBackgroundColor(Color.parseColor("#3EC543"));
                    curr_bus.setText(running_bus);
                    bustext.setVisibility(View.VISIBLE);
                    curr_bus.setVisibility(View.VISIBLE);

                }
            }
        });

        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String bsn = busnostart.getText().toString();
                if (bsn.isEmpty()) {
                    Toast.makeText(driverloc.this, "Enter Bus No.", Toast.LENGTH_SHORT).show();
                } else {
                    if (currentLocation == null) {
                        Toast.makeText(driverloc.this, "Please Wait", Toast.LENGTH_SHORT).show();
                    } else {
                        Document filter = new Document("userid", user.getId()).append("busno", busnostart.getText().toString());
                        mongoCollection.deleteOne(filter).getAsync(result -> {
                        });
                        Document bus_check = new Document("busno", bsn);
                        mongoCollection.findOne(bus_check).getAsync(result -> {
                            if (result.isSuccess()) {
                                if (result.get() != null) {
                                    Toast.makeText(driverloc.this, "Bus Already Running", Toast.LENGTH_SHORT).show();
                                } else {
                                    String lat = String.valueOf(currentLocation.getLatitude());
                                    String lon = String.valueOf(currentLocation.getLongitude());
                                    Document document = new Document()
                                            .append("_id", new ObjectId())
                                            .append("userid", user.getId())
                                            .append("driver_id", driver_id)
                                            .append("busno", bsn)
                                            .append("lat", lat)
                                            .append("lon", lon);
                                    mongoCollection.insertOne(document).getAsync(res -> {
                                        if (res.isSuccess()) {
                                            name = bsn;
                                            Toast.makeText(driverloc.this, "Bus Running", Toast.LENGTH_SHORT).show();
                                            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                                    .findFragmentById(R.id.map);
                                            assert mapFragment != null;
                                            mapFragment.getMapAsync(driverloc.this);
                                            handler.post(updateLocationRunnable);
                                            startbtn.setVisibility(View.GONE);
                                            stopbtn.setVisibility(View.VISIBLE);
                                            statustext.setText("Running");
                                            statusbar.setBackgroundColor(Color.parseColor("#3EC543"));
                                            main.setBackgroundColor(Color.parseColor("#3EC543"));
                                            bottom_status.setBackgroundColor(Color.parseColor("#3EC543"));
                                            curr_bus.setText(bsn);
                                            bustext.setVisibility(View.VISIBLE);
                                            curr_bus.setVisibility(View.VISIBLE);

                                            showLocationNotification(bsn, "Stop and restart the bus " + bsn);
                                        } else {
                                            Toast.makeText(driverloc.this, "Failed! ", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                Log.d("bus_check", "failed: " + result.getError().toString());
                            }
                        });
                    }
                }
            }
        });
        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bsn = curr_bus.getText().toString();
                if (bsn.isEmpty()) {
                    Toast.makeText(driverloc.this, "Enter Bus No.", Toast.LENGTH_SHORT).show();
                } else {
                    Document filter1 = new Document("busno", busnostart.getText().toString());
                    mongoCollection2.deleteOne(filter1).getAsync(result -> {
                    });
                    handler.removeCallbacks(updateLocationRunnable);
                    String lat = String.valueOf(currentLocation.getLatitude());
                    String lon = String.valueOf(currentLocation.getLongitude());
                    Document document = new Document()
                            .append("_id", new ObjectId())
                            .append("userid", user.getId())
                            .append("busno", busnostart.getText().toString())
                            .append("lat", lat)
                            .append("lon", lon);
                    mongoCollection2.insertOne(document).getAsync(result -> {
                        if (result.isSuccess()) {
                            Toast.makeText(driverloc.this, "Bus Parked!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("bus_stop", result.getError().toString());
                            Toast.makeText(driverloc.this, "Error Parking", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Document filter = new Document("userid", user.getId()).append("busno", bsn);
                    mongoCollection.deleteOne(filter).getAsync(result -> {
                        if (result.isSuccess()) {
                            long deletedCount = result.get().getDeletedCount();
                            if (deletedCount > 0) {
                                startbtn.setVisibility(View.VISIBLE);
                                stopbtn.setVisibility(View.GONE);
                                statustext.setText("Stopped");
                                statusbar.setBackgroundColor(Color.parseColor("#696B6C"));
                                main.setBackgroundColor(Color.parseColor("#696B6C"));
                                bottom_status.setBackgroundColor(Color.parseColor("#696B6C"));
                                Toast.makeText(driverloc.this, "Journey Stopped", Toast.LENGTH_SHORT).show();
                                statustext.setText("Stopped");
                                bustext.setVisibility(View.INVISIBLE);
                                curr_bus.setVisibility(View.INVISIBLE);
                                busnostart.setText("");
                                handler.removeCallbacks(updateLocationRunnable);
                                if (myMap != null) {
                                    Log.d("MapClear", "Clearing the map");
                                    myMap.clear();
                                } else {
                                    Log.d("MapClear", "Map is null, cannot clear");
                                }

                                cancelLocationNotification();
                            } else {
                                Log.d("Stop", "error deleting " + result.getError().toString());
                                Toast.makeText(driverloc.this, "Start the journey first", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("bus_stop", result.getError().toString());
                            Toast.makeText(driverloc.this, "Failed to stop journey", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    Handler handler = new Handler();
    Runnable updateLocationRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentLocation != null) {
                String lat = String.valueOf(currentLocation.getLatitude());
                String lon = String.valueOf(currentLocation.getLongitude());
                Document filter = new Document("userid", user.getId()).append("busno", busnostart.getText().toString());
                Document update = new Document("$set", new Document("lat", lat).append("lon", lon));

                mongoCollection.updateOne(filter, update).getAsync(result -> {
                    if (result.isSuccess()) {
                        long modifiedCount = result.get().getModifiedCount();
                        if (modifiedCount > 0) {
                            Log.d("LocationUpdate", "Location updated successfully: Lat = " + lat + ", Lon = " + lon);

                            updateLocationNotification(lat, lon);

                            LatLng updatedLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            updateBusMarker(updatedLatLng, name);
                        } else {
                            Log.d("LocationUpdate", "No records modified for the update");
                        }
                    } else {
                        Log.e("LocationUpdateError", "Failed to update location: " + result.getError().toString());
                    }
                });
            }


            handler.postDelayed(this, 2000);
        }
    };
    private Marker busMarker;

    private void updateBusMarker(LatLng latLng, String title) {
        if (myMap != null) {
            // Remove the existing marker
            if (busMarker != null) {
                busMarker.remove();
                myMap.clear();
            }
            LatLng clg = new LatLng(28.47755484223689, 79.43644973862979);
            MarkerOptions option = new MarkerOptions()
                    .position(clg)
                    .title("SRMS CET")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            myMap.addMarker(option);
            myMap.moveCamera(CameraUpdateFactory.newLatLng(clg));

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(bitmapDescriptor(getApplicationContext(), R.drawable.bus_marker));
            busMarker = myMap.addMarker(markerOptions);

            //marker ko focus kerne ke liye
            myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        } else {
            Log.e("MapUpdateError", "GoogleMap instance is null");
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if(location != null){
                currentLocation = location;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == FINE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }
            else{
                Toast.makeText(this,"Allow Location",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        if (myMap != null) {
            myMap.clear();

//            LatLng clg = new LatLng(28.47755484223689, 79.43644973862979);
//            MarkerOptions option = new MarkerOptions()
//                    .position(clg)
//                    .title("SRMS CET")
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
//            myMap.addMarker(option);
//            myMap.moveCamera(CameraUpdateFactory.newLatLng(clg));

            if (currentLocation != null) {
                LatLng bly = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                MarkerOptions options = new MarkerOptions()
                        .position(bly)
                        .title(name)
                        .icon(bitmapDescriptor(getApplicationContext(), R.drawable.bus_marker));
                myMap.addMarker(options);
                myMap.moveCamera(CameraUpdateFactory.newLatLng(bly));
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bly, 11.7f));
            } else {
                Toast.makeText(this, "No Bus Found", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(this, MainActivity.class);
                startActivity(intent1);
            }

            myMap.getUiSettings().setCompassEnabled(true);
            myMap.getUiSettings().setZoomControlsEnabled(true);
            myMap.getUiSettings().setZoomGesturesEnabled(true);
        } else {
            Log.e("MapError", "GoogleMap is null");
        }
    }

    private BitmapDescriptor bitmapDescriptor(Context context, int vectorResID)
    {
        Drawable vectorDrawable = ContextCompat.getDrawable(context,vectorResID);
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getMinimumWidth(),vectorDrawable.getMinimumHeight(),Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateLocationRunnable);


        cancelLocationNotification();
    }

    @SuppressLint("MissingPermission")
    private void checkLocationServices() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isLocationEnabled) {
            new AlertDialog.Builder(this)
                    .setTitle("Enable Location Services")
                    .setMessage("OPEN YOUR LOCATION")
                    .setPositiveButton("Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            //
        }
    }

    // Added method to create notification channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Location Channel";
            String description = "Channel for location updates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Added method to show notification
    private void showLocationNotification(String busNo, String contentText) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(driverloc.this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle("Bus Running")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(driverloc.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Added method to update notification with latest location
    private void updateLocationNotification(String lat, String lon) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(driverloc.this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle("Bus Running")
                .setContentText("Current location: Lat = " + lat + ", Lon = " + lon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(driverloc.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Added method to cancel notification
    private void cancelLocationNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(driverloc.this);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
