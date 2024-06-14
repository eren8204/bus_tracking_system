package com.example.hackathon;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;

public class splashActivity extends AppCompatActivity {

    Button user,driver,route;
    String Appid = "application-0-raildng";
    App app;
    ImageView bell;


    @SuppressLint("MissingInflatedId")
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

        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog=new Dialog(splashActivity.this);
                dialog.setContentView(R.layout.dialog);
                dialog.show();

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
                Intent intent=new Intent(splashActivity.this, buslocate.class);
                startActivity(intent);
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
}