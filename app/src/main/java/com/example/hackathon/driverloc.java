package com.example.hackathon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.bson.Document;
import org.bson.types.ObjectId;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class driverloc extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap myMap;
    private final int FINE_PERMISSION_CODE = 1;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    String Appid = "application-0-raildng";
    App app;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> mongoCollection,mongoCollection2;
    User user;
    Button startbtn, stopbtn;
    EditText busnostart;
    TextView statustext;
    String name;
    GoogleMap googleMap;
//    ImageView alert;
    private static final long UPDATE_INTERVAL = 7000;
    private static final float SMALLEST_DISPLACEMENT = 10f;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driverloc);
        startbtn = findViewById(R.id.startbtn);
        stopbtn = findViewById(R.id.stopbtn);
        busnostart = findViewById(R.id.busnostart);
        statustext = findViewById(R.id.statustext);
//        alert = findViewById(R.id.alert);

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

        startbtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                String bsn = busnostart.getText().toString();
                if(bsn.isEmpty())
                {
                    Toast.makeText(driverloc.this,"Enter Bus No.",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(currentLocation==null){
                        Toast.makeText(driverloc.this,"Please Wait",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Document filter = new Document("userid", user.getId()).append("busno", busnostart.getText().toString());
                        mongoCollection.deleteOne(filter).getAsync(result -> {
                        });
                        String lat = String.valueOf(currentLocation.getLatitude());
                        String lon = String.valueOf(currentLocation.getLongitude());
                        Document document = new Document()
                                .append("_id", new ObjectId())
                                .append("userid", user.getId())
                                .append("busno", bsn)
                                .append("lat", lat)
                                .append("lon", lon);
                        mongoCollection.insertOne(document).getAsync(result -> {
                            if(result.isSuccess())
                            {
                                name=bsn;
                                statustext.setText("Running");
                                Toast.makeText(driverloc.this, "Bus Running", Toast.LENGTH_SHORT).show();
                                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                        .findFragmentById(R.id.map);
                                assert mapFragment != null;
                                mapFragment.getMapAsync(driverloc.this);
                                handler.post(updateLocationRunnable);
                            }
                            else
                            {
                                Toast.makeText(driverloc.this, "Failed! ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
        stopbtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                String bsn = busnostart.getText().toString();
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
                            Toast.makeText(driverloc.this, "Error Parking", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Document filter = new Document("userid", user.getId()).append("busno", busnostart.getText().toString());
                    mongoCollection.deleteOne(filter).getAsync(result -> {
                        if (result.isSuccess()) {
                            long deletedCount = result.get().getDeletedCount();
                            if (deletedCount > 0) {
                                Toast.makeText(driverloc.this, "Journey Stopped", Toast.LENGTH_SHORT).show();
                                statustext.setText("Stopped");
                                Intent intent = new Intent(driverloc.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(driverloc.this, "Start the journey first", Toast.LENGTH_SHORT).show();
                                statustext.setText("Stopped");
                            }
                        } else {
                            Toast.makeText(driverloc.this, "Failed to stop journey", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

//        alert.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onClick(View v) {
//                handler.removeCallbacks(updateLocationRunnable);
//                Document filter = new Document("userid", user.getId()).append("busno", busnostart.getText().toString());
//                mongoCollection.deleteOne(filter).getAsync(result -> {
//                    if (result.isSuccess()) {
//                        long deletedCount = result.get().getDeletedCount();
//                        if (deletedCount > 0) {
//                            Toast.makeText(driverloc.this, "Journey Stopped", Toast.LENGTH_SHORT).show();
//                            statustext.setText("Stopped");
//                        } else {
//                            Toast.makeText(driverloc.this, "Bus Not Running", Toast.LENGTH_SHORT).show();
//                            statustext.setText("Stopped");
//                        }
//                    } else {
//                        Toast.makeText(driverloc.this, "Bus Not Running", Toast.LENGTH_SHORT).show();
//                    }
//                });
//                Toast.makeText(driverloc.this, "Emergency Situation!!!", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(driverloc.this, emergency.class);
//                startActivity(intent);
//            }
//        });
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
                            Toast.makeText(driverloc.this, "Location Updated", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(driverloc.this, "Unable To Update", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(driverloc.this, "Failed to update location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            handler.postDelayed(this, 10000);
        }
    };

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    currentLocation = location;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==FINE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
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
        LatLng clg = new LatLng(28.47755484223689, 79.43644973862979);
        MarkerOptions option = new MarkerOptions().position(clg).title("SRMS CET");
        option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        myMap.addMarker(option);
        myMap.moveCamera(CameraUpdateFactory.newLatLng(clg));
        if(currentLocation!=null) {
            LatLng bly = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            MarkerOptions options = new MarkerOptions().position(bly).title(name);
            options.icon(bitmapDescriptor(getApplicationContext(),R.drawable.bus_marker));
            myMap.addMarker(options);
            myMap.moveCamera(CameraUpdateFactory.newLatLng(bly));
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()), 11.7f));
        }
        else{
            Toast.makeText(this,"No Bus Found",Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(this, MainActivity.class);
            startActivity(intent1);
        }
        myMap.getUiSettings().setCompassEnabled(true);
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setZoomGesturesEnabled(true);
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
}