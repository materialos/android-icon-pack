package org.materialos.icons;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;

import org.materialos.icons.activities.MainActivity;

import io.fabric.sdk.android.Fabric;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);

        finish();

    }


}
