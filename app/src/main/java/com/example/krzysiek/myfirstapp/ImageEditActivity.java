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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;

public class ImageEditActivity extends AppCompatActivity implements View.OnClickListener {


    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);


        Button buttonGray = (Button) findViewById(R.id.buttonColorFX);
        buttonGray.setOnClickListener(this); // calling onClick() method
        //Button two = (Button) findViewById(R.id.twoButton);
        //two.setOnClickListener(this);
        //Button three = (Button) findViewById(R.id.threeButton);
        //three.setOnClickListener(this);


        // Get the Intent that started this activity and extract the string
        Uri pictureUri = Uri.parse(getIntent().getStringExtra("pictureUri"));

        imageView = findViewById(R.id.imageView);
        imageView.setImageURI(pictureUri);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.buttonColorFX:
                PopupMenu popup = new PopupMenu(this,v);
                popup.getMenuInflater().inflate(R.menu.colorfx_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_colorfxRed:
                                imageView.setImageTintMode(PorterDuff.Mode.OVERLAY);
                                imageView.setImageTintList(ColorStateList.valueOf(0xFFFF0000));
                                break;
                            case R.id.menu_colorfxIceCold:
                                imageView.setImageTintMode(PorterDuff.Mode.OVERLAY);
                                imageView.setImageTintList(ColorStateList.valueOf(0xFF0000FF));
                                break;
                            case R.id.menu_colorfxGreenery:
                                imageView.setImageTintMode(PorterDuff.Mode.OVERLAY);
                                imageView.setImageTintList(ColorStateList.valueOf(0xFF00FF00));
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popup.show();//showing popup menu
                break;

            default:
                break;
        }

    }
}
