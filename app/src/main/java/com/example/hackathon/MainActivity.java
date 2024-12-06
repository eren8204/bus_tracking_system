package com.example.hackathon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.bson.Document;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class MainActivity extends AppCompatActivity {
    Button loginbtn;
    EditText loginphone,loginpass;
    private String Appid = BuildConfig.appId;

    public static final int time=2000;
    private long backpressed;
    ImageView arrowback;
    App app;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Realm.init(this);
        loginbtn = findViewById(R.id.loginbtn);
        loginphone = findViewById(R.id.loginphone);
        loginpass = findViewById(R.id.loginpass);
        app = new App(new AppConfiguration.Builder(Appid).build());
        User user = app.currentUser();




        arrowback=findViewById(R.id.arrowback);

        arrowback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, splashActivity.class);
                startActivity(intent);
                finish();
            }
        });
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = loginphone.getText().toString();
                String password = loginpass.getText().toString();
                if (id.trim().isEmpty() || password.trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter details please", Toast.LENGTH_SHORT).show();
                } else {
                    Credentials credentials = Credentials.emailPassword(id, password);
                    app.loginAsync(credentials, new App.Callback<User>() {
                        @Override
                        public void onResult(App.Result<User> result) {
                            if (result.isSuccess()) {
                                Toast.makeText(MainActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, driverloc.class);
                                intent.putExtra("driver_id",id);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (backpressed+time>System.currentTimeMillis()) {

            super.onBackPressed();
            return;

        } else {

            Toast.makeText(this, "press again to exit", Toast.LENGTH_SHORT).show();
        }
        backpressed=System.currentTimeMillis();
    }
}