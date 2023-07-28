package com.amit07.real_timelanguagetranslation;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        getSupportActionBar().hide();


        Thread thread = new Thread(){
          public void run(){
              try {
                  sleep(1500);
              }
              catch (Exception e) {
                  e.printStackTrace();
              }
              finally {
                  Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                  startActivity(intent);
              }
          }
        }; thread.start();

    }
}