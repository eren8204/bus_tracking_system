package com.example.hackathon;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.bson.Document;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class buslocate extends AppCompatActivity implements OnMapReadyCallback{
    private GoogleMap myMap;
    private final int FINE_PERMISSION_CODE = 1;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    String Appid = BuildConfig.appId;
    App app;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> mongoCollection;
    User user;
    double lati, longi;
    String name;
    Button search;
    EditText busno;
    TextView statustext;
    LinearLayout statusbar,bottom_status;
    LinearLayoutCompat main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buslocate);
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(Appid).build());
        user = app.currentUser();
        assert user != null;
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("location");
        mongoCollection = mongoDatabase.getCollection("driverloc");
        busno = findViewById(R.id.busno);
        search = findViewById(R.id.search);
        statustext = findViewById(R.id.statustext);
        statusbar = findViewById(R.id.statusbar);
        main = findViewById(R.id.main);
        bottom_status = findViewById(R.id.bottom_status);
        user = app.currentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bsn = busno.getText().toString();
                Document queryFilter = new Document().append("busno", bsn);
                mongoCollection.findOne(queryFilter).getAsync(result -> {
                    if (result.isSuccess()) {
                        Log.i("resultStatus", String.valueOf(result.isSuccess()));
                        Log.i("result", result.toString());
                        Document resultData = result.get();
                        if (resultData != null) {
                            String lat = resultData.getString("lat");
                            String lon = resultData.getString("lon");
                            lati = Double.parseDouble(lat);
                            longi = Double.parseDouble(lon);
                            name = bsn;
                            if(myMap!=null)
                            {
                                myMap.clear();
                            }
                            Toast.makeText(buslocate.this, "Bus Found", Toast.LENGTH_SHORT).show();
                            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.map);
                            assert mapFragment != null;
                            mapFragment.getMapAsync(buslocate.this);
                            statustext.setText("Running");
                            statusbar.setBackgroundColor(Color.parseColor("#3EC543"));
                            main.setBackgroundColor(Color.parseColor("#3EC543"));
                            bottom_status.setBackgroundColor(Color.parseColor("#3EC543"));
                        } else {
                            Log.i("result1", "error1");
                            statustext.setText("Not Running");
                            statusbar.setBackgroundColor(Color.parseColor("#696B6C"));
                            main.setBackgroundColor(Color.parseColor("#696B6C"));
                            bottom_status.setBackgroundColor(Color.parseColor("#696B6C"));
                            Toast.makeText(buslocate.this, "No Bus Found", Toast.LENGTH_SHORT).show();
                            lati=0;
                            longi=0;
                            if(myMap!=null)
                            {
                                myMap.clear();
                            }
                            handler.removeCallbacks(updateLocationRunnable);
                        }
                    } else {
                        Log.i("result2", "error2: "+result.getError().toString());
                        Toast.makeText(buslocate.this, "No Bus Found", Toast.LENGTH_SHORT).show();
                        lati=0;
                        longi=0;
                        if(myMap!=null)
                        {
                            myMap.clear();
                        }
                        handler.removeCallbacks(updateLocationRunnable);
                    }
                });
            }
        });
    }
    Handler handler = new Handler();
    Runnable updateLocationRunnable = new Runnable() {
        @Override
        public void run() {
            if(myMap!=null)
            {
                myMap.clear();
            }
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            assert mapFragment != null;
            mapFragment.getMapAsync(buslocate.this);
            handler.postDelayed(this, 10000);
        }
    };
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        if(myMap!=null)
        {
            myMap.clear();
        }
        LatLng clg = new LatLng(28.47755484223689, 79.43644973862979);
        MarkerOptions option = new MarkerOptions().position(clg).title("SRMS CET");
        option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        myMap.addMarker(option);
        myMap.moveCamera(CameraUpdateFactory.newLatLng(clg));
        if(lati!=0.0 || longi!=0.0) {
            LatLng bly = new LatLng(lati, longi);
            MarkerOptions options = new MarkerOptions().position(bly).title(name);
            options.icon(bitmapDescriptor(getApplicationContext(),R.drawable.bus_marker));
            myMap.addMarker(options);
            myMap.moveCamera(CameraUpdateFactory.newLatLng(bly));
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lati,longi), 11.7f));
        }
        else{
            Toast.makeText(this,"No Bus Found",Toast.LENGTH_SHORT).show();
        }
        myMap.getUiSettings().setCompassEnabled(true);
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setZoomGesturesEnabled(true);
    }

    private BitmapDescriptor bitmapDescriptor(Context context,int vectorResID)
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
    }
}