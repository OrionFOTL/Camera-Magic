package com.example.krzysiek.myfirstapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.ContactsContract;
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
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ThreadLocalRandom;

public class ImageEditActivity extends AppCompatActivity implements View.OnClickListener {


    public Uri pictureUri;
    public ImageView imageView;
    public Bitmap finalBitmap;
    public Canvas canvas;
    public Paint paint;
    public boolean wasNarutoed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);


        ImageButton buttonColorFX = findViewById(R.id.buttonColorFX);
        buttonColorFX.setOnClickListener(this);
        ImageButton buttonFlip = findViewById(R.id.buttonFlip);
        buttonFlip.setOnClickListener(this);
        ImageButton buttonNaruto = findViewById(R.id.buttonNaruto);
        buttonNaruto.setOnClickListener(this);
        ImageButton buttonReset = findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(this);
        ImageButton buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this);


        // Get the Intent that started this activity and extract the string
        pictureUri = Uri.parse(getIntent().getStringExtra("pictureUri"));

        imageView = findViewById(R.id.imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_START);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pictureUri);
            finalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            finalBitmap = resize(finalBitmap,1200,2000);
            canvas = new Canvas(finalBitmap);
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
                                colorFilter(0xFFFF0000);
                                break;
                            case R.id.menu_colorfxIceCold:
                                colorFilter(0xFF0000FF);
                                break;
                            case R.id.menu_colorfxGreenery:
                                colorFilter(0xFF00FF00);
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

            case R.id.buttonNaruto:
                if (!wasNarutoed) colorFilter(0x00f7a0c0);
                wasNarutoed = true;
                int positionX, positionY;
                Bitmap heart = decodeSampledBitmapFromResource(getResources(),R.drawable.heart,400,400);
                Bitmap heart2 = decodeSampledBitmapFromResource(getResources(),R.drawable.heartm,400,400);
                Bitmap cloud1 = decodeSampledBitmapFromResource(getResources(),R.drawable.cloud1,1200,800);
                Bitmap cloud2 = decodeSampledBitmapFromResource(getResources(),R.drawable.cloud2,1200,800);

                positionX = ThreadLocalRandom.current().nextInt(0, canvas.getWidth()) - 400;
                positionY = ThreadLocalRandom.current().nextInt(canvas.getHeight()/2, canvas.getHeight() - 200);
                canvas.drawBitmap(cloud1, new Rect(0,0,cloud1.getWidth(),cloud1.getHeight()),
                        new Rect(positionX,positionY,(int) (positionX+cloud1.getWidth()*0.5),(int)(positionY+cloud1.getHeight()*0.5)),null);

                for (int i=0; i<4; i++){
                    Matrix matrix = new Matrix();
                    matrix.postRotate((float) Math.random()*360);
                    Bitmap bmp = Bitmap.createBitmap(heart2, 0, 0, heart2.getWidth(), heart2.getHeight(), matrix, true);
                    positionX = ThreadLocalRandom.current().nextInt(0, canvas.getWidth() - 20);
                    positionY = ThreadLocalRandom.current().nextInt(0, canvas.getHeight() - 300);
                    canvas.drawBitmap(bmp, new Rect(0,0,bmp.getWidth(),bmp.getHeight()),
                                              new Rect(positionX,positionY,positionX+400,positionY+400),null);
                    bmp.recycle();
                }
                for (int i=0; i<1; i++){
                    Matrix matrix = new Matrix();
                    matrix.postRotate((float) Math.random()*360);
                    Bitmap bmp = Bitmap.createBitmap(heart, 0, 0, heart.getWidth(), heart.getHeight(), matrix, true);
                    positionX = ThreadLocalRandom.current().nextInt(0, canvas.getWidth() - 200);
                    positionY = ThreadLocalRandom.current().nextInt(0, canvas.getHeight() - 300);
                    canvas.drawBitmap(bmp, new Rect(0,0,bmp.getWidth(),bmp.getHeight()),
                            new Rect(positionX,positionY,positionX+200,positionY+200),null);
                    bmp.recycle();
                }
                positionX = ThreadLocalRandom.current().nextInt(0, canvas.getWidth()) - 400;
                positionY = ThreadLocalRandom.current().nextInt(canvas.getHeight()/2, canvas.getHeight() - 200);
                canvas.drawBitmap(cloud2, new Rect(0,0,cloud2.getWidth(),cloud2.getHeight()),
                        new Rect(positionX,positionY,(int) (positionX+cloud2.getWidth()*0.5),(int)(positionY+cloud2.getHeight()*0.5)),null);
                positionX = ThreadLocalRandom.current().nextInt(0, canvas.getWidth()) - 400;
                positionY = ThreadLocalRandom.current().nextInt(canvas.getHeight()/2, canvas.getHeight() - 200);
                canvas.drawBitmap(cloud2, new Rect(0,0,cloud2.getWidth(),cloud2.getHeight()),
                        new Rect(positionX,positionY,(int) (positionX+cloud2.getWidth()*0.5),(int)(positionY+cloud2.getHeight()*0.5)),null);
                imageView.setImageBitmap(finalBitmap);
                break;

            case R.id.buttonReset:
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pictureUri);
                    finalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    finalBitmap = resize(finalBitmap,1200,2000);
                    canvas = new Canvas(finalBitmap);
                    imageView.setImageBitmap(finalBitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                wasNarutoed = false;
                break;

            case R.id.buttonSave:
                OutputStream outputStream;

                try {
                    outputStream = getContentResolver().openOutputStream(pictureUri);
                    boolean compressed = finalBitmap.compress(Bitmap.CompressFormat.JPEG,90,outputStream);
                    Log.i("Camera Magic","Obraz skompresowany i zapisany w: " + pictureUri + compressed);
                    Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
                    outputStream.close();
                    finish();
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                }
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
    private void colorFilter(@ColorInt int color){
        paint = new Paint();
        ColorFilter colorFilter = new LightingColorFilter(color,0x0);
        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(finalBitmap,new Matrix(),paint);
    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}
