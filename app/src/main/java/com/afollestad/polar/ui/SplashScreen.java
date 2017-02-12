package com.afollestad.polar.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * @author Aidan Follestad (afollestad)
 */
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Theme can only be switched (between light and dark) manually from AndroidManifest.xml for this splash screen.

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}