package com.example.krzysiek.myfirstapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;

public class ImageEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String hi = intent.getStringExtra("hello");
        Uri pictureUri = Uri.parse(getIntent().getStringExtra("pictureUri"));

        TextView text = findViewById(R.id.textView2);
        text.setText(pictureUri.toString());
    }
}
