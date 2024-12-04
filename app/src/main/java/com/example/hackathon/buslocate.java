package com.example.hackathon;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.bson.Document;

import java.util.Objects;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;


// My code
public class buslocate extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap myMap;

    private double lati, longi;
    private String name;
    private Button search;
    private EditText busno;
    private TextView statustext;
    private LinearLayout statusbar, bottom_status;
    private LinearLayout main;
    private float previousZoomLevel = 0f;
    private MarkerOptions busMarkerOptions;

    App app;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> mongoCollection;
    User user;

    // handler created for reloading the marker again and again
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buslocate);
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(BuildConfig.appId).build());
        user = app.currentUser();

        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Intent intent = getIntent();

        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("location");
        mongoCollection = mongoDatabase.getCollection("driverloc");

        busno = findViewById(R.id.busno);
        search = findViewById(R.id.search);
        statustext = findViewById(R.id.statustext);
        statusbar = findViewById(R.id.statusbar);
        main = findViewById(R.id.main);
        bottom_status = findViewById(R.id.bottom_status);

        //automatically search bus
        if(intent.hasExtra("busnofromroute"))
        {
            String bsnfr = intent.getStringExtra("busnofromroute");
            String bsn = bsnfr;
            busno.setText(bsn);
            Document queryFilter = new Document().append("busno", bsn);
            mongoCollection.findOne(queryFilter).getAsync(result -> {
                if (result.isSuccess()) {
                    Document resultData = result.get();
                    if (resultData != null) {
                        String lat = resultData.getString("lat");
                        String lon = resultData.getString("lon");
                        lati = Double.parseDouble(lat);
                        longi = Double.parseDouble(lon);
                        name = bsn;

                        if (myMap != null) {
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
                        statustext.setText("Not Running");
                        statusbar.setBackgroundColor(Color.parseColor("#696B6C"));
                        main.setBackgroundColor(Color.parseColor("#696B6C"));
                        bottom_status.setBackgroundColor(Color.parseColor("#696B6C"));
                        Toast.makeText(buslocate.this, "No Bus Found", Toast.LENGTH_SHORT).show();
                        lati = 0;
                        longi = 0;
                        if (myMap != null) {
                            myMap.clear();
                        }
                    }
                } else {
                    Toast.makeText(buslocate.this, "No Bus Found", Toast.LENGTH_SHORT).show();
                }
            });
        }

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bsn = busno.getText().toString();
                Document queryFilter = new Document().append("busno", bsn);
                mongoCollection.findOne(queryFilter).getAsync(result -> {
                    if (result.isSuccess()) {
                        Document resultData = result.get();
                        if (resultData != null) {
                            String lat = resultData.getString("lat");
                            String lon = resultData.getString("lon");
                            lati = Double.parseDouble(lat);
                            longi = Double.parseDouble(lon);
                            name = bsn;


                            if (myMap != null) {
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
                            statustext.setText("Not Running");
                            statusbar.setBackgroundColor(Color.parseColor("#696B6C"));
                            main.setBackgroundColor(Color.parseColor("#696B6C"));
                            bottom_status.setBackgroundColor(Color.parseColor("#696B6C"));
                            Toast.makeText(buslocate.this, "No Bus Found", Toast.LENGTH_SHORT).show();
                            lati = 0;
                            longi = 0;
                            if (myMap != null) {
                                myMap.clear();
                            }
                        }
                    } else {
                        Toast.makeText(buslocate.this, "No Bus Found", Toast.LENGTH_SHORT).show();
                        Log.d("search_error",result.getError().toString());
                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;


        busMarkerOptions = new MarkerOptions().title(name).icon(bitmapDescriptor(getApplicationContext(), R.drawable.bus_marker));

        if (myMap != null) {
            myMap.clear();
        }

        LatLng clg = new LatLng(28.47755484223689, 79.43644973862979);
        MarkerOptions option = new MarkerOptions().position(clg).title("SRMS CET");
        option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        Objects.requireNonNull(myMap.addMarker(option)).showInfoWindow();
        myMap.moveCamera(CameraUpdateFactory.newLatLng(clg));

        updateMarkerPosition();

        myMap.getUiSettings().setCompassEnabled(true);
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setZoomGesturesEnabled(true);
        myMap.setOnCameraMoveListener(() -> {
            if (myMap != null) {
                previousZoomLevel = myMap.getCameraPosition().zoom;
            }
        });
        handler.postDelayed(updateLocationRunnable, 3000);
    }

    private void updateMarkerPosition() {
        if (myMap != null && lati != 0.0 && longi != 0.0) {
            LatLng busLocation = new LatLng(lati, longi);
            busMarkerOptions.position(busLocation);

            myMap.clear();
            Objects.requireNonNull(myMap.addMarker(busMarkerOptions)).showInfoWindow();


            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(busLocation, previousZoomLevel > 0 ? previousZoomLevel : 15f));
        }
    }


    // dubara se fetch set kera jo pehele zoom level tha
    @Override
    protected void onResume() {
        super.onResume();
        if (myMap != null) {
            previousZoomLevel = myMap.getCameraPosition().zoom;
        }
    }

    private BitmapDescriptor bitmapDescriptor(Context context, int vectorResID) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResID);
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getMinimumWidth(), vectorDrawable.getMinimumHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    Runnable updateLocationRunnable = new Runnable() {
        @Override
        public void run() {
            if (myMap != null) {

                Document queryFilter = new Document().append("busno", name);
                mongoCollection.findOne(queryFilter).getAsync(result -> {
                    if (result.isSuccess()) {
                        Document resultData = result.get();
                        if (resultData != null) {
                            String lat = resultData.getString("lat");
                            String lon = resultData.getString("lon");
                            lati = Double.parseDouble(lat);
                            longi = Double.parseDouble(lon);

                            updateMarkerPosition();
                        }
                    }
                });
            }

            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateLocationRunnable);
    }
}
