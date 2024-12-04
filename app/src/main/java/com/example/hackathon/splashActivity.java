package com.example.hackathon;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    String Appid = BuildConfig.appId;
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
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.show();

                TextView customRoute = dialog.findViewById(R.id.custom_route);
                ListView routesList = dialog.findViewById(R.id.routes_list);
                SearchView searchView = dialog.findViewById(R.id.searchView);
                searchView.clearFocus();

                customRoute.setText("Fetching data...");
                String url = "https://gomap-bus-tracking-system-production.up.railway.app/api/routes";
                RequestQueue requestQueue = Volley.newRequestQueue(splashActivity.this);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        response -> {
                            try {
                                JSONArray dataArray = response.getJSONArray("data");
                                List<Route> routes = new ArrayList<>();

                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject routeObject = dataArray.getJSONObject(i);
                                    int id = routeObject.getInt("id");
                                    String busNo = routeObject.getString("busNo");
                                    String departureTime = routeObject.getString("departureTime");
                                    String arrivalTime = routeObject.getString("arrivalTime");
                                    String route = routeObject.getString("route");
                                    String source = routeObject.getString("start");
                                    String dest = routeObject.getString("end");

                                    routes.add(new Route(id, busNo, departureTime, arrivalTime, route, source, dest));
                                }

                                RouteAdapter adapter = new RouteAdapter(splashActivity.this, routes);
                                routesList.setAdapter(adapter);
                                customRoute.setText("Routes");
                                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                    @Override
                                    public boolean onQueryTextSubmit(String query) {
                                        adapter.getFilter().filter(query);
                                        return false;
                                    }

                                    @Override
                                    public boolean onQueryTextChange(String newText) {
                                        adapter.getFilter().filter(newText);
                                        return false;
                                    }
                                });
                            } catch (JSONException e) {
                                customRoute.setText("Failed to parse data");
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            customRoute.setText("Failed to fetch data");
                            error.printStackTrace();
                        }
                );

                requestQueue.add(jsonObjectRequest);
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