package com.example.hackathon;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;


import java.sql.Date;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

@SuppressLint("CustomSplashScreen")
public class splashActivity extends AppCompatActivity {

    View user;
    Button driver;
    Button route;
    String Appid = "application-0-raildng";
    App app;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> mongoCollection,mongoCollection2;
    ImageView bell;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(Appid).build());
        user=findViewById(R.id.user);
        driver=findViewById(R.id.Driver);
        route=findViewById(R.id.routes);
        bell=findViewById(R.id.bell);

        checkNetworkConnectivity();

        route.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(splashActivity.this);
                dialog.setContentView(R.layout.dialog);
                dialog.show();

                TextView custom_route = dialog.findViewById(R.id.custom_route);
                TextView routes_data = dialog.findViewById(R.id.routes_data);
                custom_route.setText("Fetching data...");
                Log.d("MongoDB", "Dialog and TextView initialized");

                User userID = app.currentUser();

                if (userID != null) {
                    MongoClient mongoClient = userID.getMongoClient("mongodb-atlas");
                    MongoDatabase mongoDatabase = mongoClient.getDatabase("location");
                    MongoCollection<Document> routeCollection = mongoDatabase.getCollection("routes");
                    String routeId = "1234";
                    Document filter = new Document("route_id", routeId);

                    Log.d("MongoDB", "Filter created: " + filter.toJson());

                    routeCollection.findOne(filter).getAsync(task -> {
                        if (task.isSuccess()) {
                            Document doc = task.get();
                            if (doc != null) {
                                String routeName = doc.getString("route");
                                runOnUiThread(() -> {
                                    custom_route.setText("Routes");
                                    routes_data.setText("Route Name: " + routeName);
                                    Log.d("MongoDB", "Data fetched successfully: " + routeName);
                                });
                            } else {
                                runOnUiThread(() -> {
                                    routes_data.setText("No data found for Route ID: " + routeId);
                                    Log.d("MongoDB", "No document found for Route ID: " + routeId);
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                String errorMessage = "Failed to fetch data: " + task.getError().toString();
                                routes_data.setText(errorMessage);
                                Log.e("MongoDB", errorMessage);
                            });
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        custom_route.setText("User not logged in");
                        Log.e("MongoDB", "User not logged in");
                    });
                }
            }
        });


        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Credentials credentials = Credentials.anonymous();
                app.loginAsync(credentials, new App.Callback<User>() {
                    @Override
                    public void onResult(App.Result<User> result) {
                        if(result.isSuccess()){
                            Intent intent=new Intent(splashActivity.this, buslocate.class);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(splashActivity.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(splashActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(splashActivity.this, notices.class);
                startActivity(intent);
            }
        });
    }
    private void checkNetworkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        boolean isWiFi = activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

        if (!isConnected) {
            new AlertDialog.Builder(this)
                    .setTitle("No Internet Connection")
                    .setMessage("You are not connected to the internet. Please check your network settings.")
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            //
        }
    }
}