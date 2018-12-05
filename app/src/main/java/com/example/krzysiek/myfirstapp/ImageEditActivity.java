package com.example.krzysiek.myfirstapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageEditActivity extends AppCompatActivity implements View.OnClickListener {


    public ImageView imageView;
    public Bitmap finalBitmap;
    public Canvas canvas;
    public Paint paint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);


        ImageButton buttonColorFX = findViewById(R.id.buttonColorFX);
        buttonColorFX.setOnClickListener(this); // calling onClick() method
        ImageButton buttonFlip = findViewById(R.id.buttonFlip);
        buttonFlip.setOnClickListener(this);
        //Button three = (Button) findViewById(R.id.threeButton);
        //three.setOnClickListener(this);


        // Get the Intent that started this activity and extract the string
        Uri pictureUri = Uri.parse(getIntent().getStringExtra("pictureUri"));

        imageView = findViewById(R.id.imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_START);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pictureUri);
            finalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            finalBitmap = resize(finalBitmap,1200,2000);
            canvas = new Canvas(finalBitmap);
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.MAGENTA);
            for (int i = 0; i < 30; i++) {
                canvas.drawCircle(50+i*20, 50+i*40, 30, paint);
            }
            imageView.setImageBitmap(finalBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                            case R.id.menu_colorfxInvert:
                                finalBitmap = doInvert(finalBitmap);
                                imageView.setImageBitmap(finalBitmap);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
                break;

            case R.id.buttonFlip:
                PopupMenu popupFlip = new PopupMenu(this,v);
                popupFlip.getMenuInflater().inflate(R.menu.flip_menu, popupFlip.getMenu());

                popupFlip.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_flipLeftToRight:
                                finalBitmap = flipHalf(finalBitmap,true);
                                imageView.setImageBitmap(finalBitmap);
                                break;
                            case R.id.menu_flipRightToLeft:
                                finalBitmap = flipHalf(finalBitmap,false);
                                imageView.setImageBitmap(finalBitmap);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popupFlip.show();
                break;

            default:
                break;
        }

    }
    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
    public static Bitmap doInvert(Bitmap src) {
        // create new bitmap with the same settings as source bitmap
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        // color info
        int A, R, G, B;
        int pixelColor;
        // image size
        int height = src.getHeight();
        int width = src.getWidth();

        // scan through every pixel
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                // get one pixel
                pixelColor = src.getPixel(x, y);
                // saving alpha channel
                A = Color.alpha(pixelColor);
                // inverting byte for each R/G/B channel
                R = 255 - Color.red(pixelColor);
                G = 255 - Color.green(pixelColor);
                B = 255 - Color.blue(pixelColor);
                // set newly-inverted pixel to output image
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        src.recycle();
        // return final bitmap
        return bmOut;
    }

    private static Bitmap flipHalf(Bitmap image, boolean ltr){
        int height = image.getHeight();
        int width = image.getWidth();
        if (ltr) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width / 2; x++) {
                    // Calculate how far to the right the mirrored pixel is
                    int mirrorOffset = (width - (x * 2)) - 1;
                    image.setPixel(x + mirrorOffset, y, image.getPixel(x, y));
                }
            }
        } else {
            for (int y = 0; y < height; y++) {
                for (int x = width/2; x < width; x++) { // divide by 2 to only loop through the left half of the image.
                    // Calculate how far to the right the mirrored pixel is
                    int mirrorOffset = width - x - 1;
                    image.setPixel(mirrorOffset, y, image.getPixel(x, y));
                }
            }
        }
        return image;
    }
}
