package com.example.krzysiek.myfirstapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class ImageEditActivity extends AppCompatActivity implements View.OnClickListener {


    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);


        Button buttonGray = (Button) findViewById(R.id.buttonGray);
        buttonGray.setOnClickListener(this); // calling onClick() method
        //Button two = (Button) findViewById(R.id.twoButton);
        //two.setOnClickListener(this);
        //Button three = (Button) findViewById(R.id.threeButton);
        //three.setOnClickListener(this);


        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        Uri pictureUri = Uri.parse(getIntent().getStringExtra("pictureUri"));

        imageView = findViewById(R.id.imageView);
        imageView.setImageURI(pictureUri);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.buttonGray:
                imageView.setImageTintMode(PorterDuff.Mode.OVERLAY);
                imageView.setImageTintList(ColorStateList.valueOf(0xFFFF0000));
                break;

            default:
                break;
        }

    }
}
