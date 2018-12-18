package com.example.krzysiek.myfirstapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.icu.util.Output;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity implements MediaScannerConnection.MediaScannerConnectionClient {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public static final int PERMISSIONS_REQUEST_CAMERA = 1;
    public static final int PERMISSIONS_REQ_WRITESTORAGE = 5;
    private MediaScannerConnection scanner;
    private Camera mCamera;
    private CameraPreview mPreview;
    private int mCameraId = 0;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            Bitmap pictureTaken = BitmapFactory.decodeByteArray(data,0,data.length);

            //obroc zdjecie
            Matrix matrix = new Matrix();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId,cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) matrix.postRotate(CameraPreview.mDisplayOrientation);
            else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) matrix.postRotate(-CameraPreview.mDisplayOrientation);

            //tworzenie bitmapy na podstawie obrazu, uzywajac matrix do obrocenia
            pictureTaken = Bitmap.createBitmap(pictureTaken,0,0,pictureTaken.getWidth(),
                                                pictureTaken.getHeight(),matrix,true);
            Uri contentUri = Uri.fromFile(pictureFile);
            OutputStream outputStream;

            try {
                outputStream = getContentResolver().openOutputStream(contentUri);
                boolean compressed = pictureTaken.compress(Bitmap.CompressFormat.JPEG,90,outputStream);
                Log.i("Camera Magic","Obraz skompresowany i zapisany w: " + pictureFile + compressed);
                outputStream.close();
            } catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }

            if (pictureFile == null){
                displayModal("Błąd przy tworzeniu pliku","Błąd zapisu zdjęcia, sprawdź uprawnienia dostępu do pamięci");
                return;
            }

            //wyslij zadanie przeskanowania pamieci
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(contentUri);
            getApplicationContext().sendBroadcast(mediaScanIntent);
            Log.i("Camera Magic","Zdjecie zrobione " + pictureFile.getPath());


            Intent intent = new Intent(MainActivity.this, ImageEditActivity.class);
            intent.putExtra("pictureUri",contentUri.toString());
            startActivity(intent);

            //mCamera.startPreview();

            /*
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                displayModal("Nie znaleziono pliku",e.getMessage());
            } catch (IOException e) {
                displayModal("Błąd dsotępu do pliku",e.getMessage());
            }*/
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanner = new MediaScannerConnection(getApplicationContext(),this);

        //jeśli nie ma pozwolenia na kamerę
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("Camera Magic","Asking for camera permissionns now");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},PERMISSIONS_REQUEST_CAMERA); //poproś o pozwolenie
        }
        else{ //jeśli zgoda już była udzielona
            Log.i("Camera Magic","opening back camera now");
            openFrontCamera();
            mPreview = new CameraPreview(this,mCamera);
            mPreview.setCameraID(mCameraId);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }
        final ImageButton captureButton = (ImageButton) findViewById(R.id.captureButton);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSIONS_REQ_WRITESTORAGE);
                        }
                        else {
                            if (mCamera.getParameters().getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                        mCamera.takePicture(null, null, mPicture);
                                    }
                                });
                            } else mCamera.takePicture(null, null, mPicture);
                            Log.e("Camera Magic","Wcisnieto przycisk migawki");
                        }

                    }
                }
        );
        final ImageButton flipCameraButton = (ImageButton) findViewById(R.id.flipCameraButton);
        flipCameraButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                        Camera.getCameraInfo(mCameraId,cameraInfo);
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            openFrontCamera();
                            mPreview = new CameraPreview(MainActivity.this,mCamera);
                            mPreview.setCameraID(mCameraId);
                            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                            preview.removeAllViews();
                            preview.addView(mPreview);
                        }
                        else {
                            openBackCamera();
                            mPreview = new CameraPreview(MainActivity.this,mCamera);
                            mPreview.setCameraID(mCameraId);
                            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                            preview.removeAllViews();
                            preview.addView(mPreview);
                        }
                    }
                }
        );
    }
    private boolean safeCameraOpen(int id){
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            //CameraPreview.setCameraDisplayOrientation((Activity) this, id, mCamera);
            qOpened = (mCamera != null);
        } catch (Exception e){
            Log.e("Camera Magic","nie udało się otworzyć kamery");
            e.printStackTrace();
        }
        return qOpened;
    }
    private void releaseCameraAndPreview() {
        if (mPreview != null) mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraId == 0) openBackCamera();
        else openFrontCamera();
        if (mPreview != null) mPreview.setCamera(mCamera);
    }

    private void openBackCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int liczbaAparatow = Camera.getNumberOfCameras();

        for (int id=0; id<liczbaAparatow; id++){
            Camera.getCameraInfo(id,cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraId = id;
                safeCameraOpen(id);
            }
        }
    }

    private void openFrontCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int liczbaAparatow = Camera.getNumberOfCameras();

        for (int id=0; id<liczbaAparatow; id++){
            Camera.getCameraInfo(id,cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCameraId = id;
                safeCameraOpen(id);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onMediaScannerConnected() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Camera Magic");
        scanner.scanFile(mediaStorageDir.getAbsolutePath(),null);
        Log.i("Scanner","Zeskanowana sciezka " + mediaStorageDir.getAbsolutePath());
    }

    @Override
    public void onScanCompleted(String path, Uri uri){
        scanner.disconnect();
    }

    //////////////////////Stare
    public void initCamera(){
        checkCameraHardware(this); //sprawdz czy jest kamera

        //create an instance of Camera
        mCamera = getCameraInstance();

        //Create our Preview View and set it as content of our activity
        mPreview = new CameraPreview(this,mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

    }
    public Camera getCameraInstance() {
        Camera cam = null;
        try {
            cam = Camera.open();

        }
        catch (Exception e){
            displayErrorModal("Błąd kamery",e.toString());
        }
        return cam;
    }
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            displayErrorModal("Brak kamery","Twój telefon nie ma aparatu");
            return false;
        }
    } //true jeśli urządzenie ma kamerę

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openBackCamera();
                mPreview = new CameraPreview(this,mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);
                return;
            } else displayErrorModal("Błąd uprawnień", "Nie pozwoliłeś na dostęp do kamery");
        } else if (requestCode == PERMISSIONS_REQ_WRITESTORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                safeCameraOpen(mCameraId);
                mPreview = new CameraPreview(MainActivity.this,mCamera);
                mPreview.setCameraID(mCameraId);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.removeAllViews();
                preview.addView(mPreview);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCamera.getParameters().getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                @Override
                                public void onAutoFocus(boolean success, Camera camera) {
                                    mCamera.takePicture(null, null, mPicture);
                                }
                            });
                        } else mCamera.takePicture(null, null, mPicture);
                        Log.e("Camera Magic","Wcisnieto przycisk migawki");
                    }
                }, 500);
                return;
            }
            else displayErrorModal("Błąd uprawnień", "Nie pozwoliłeś na dostęp do pamięci");
        }
        //inne case dla requestCode'ów innych przyznanych uprawnień
    }
    public void displayErrorModal(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                System.exit(0);
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    public void displayModal(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Camera Magic");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            String filename = "IMG_"+timeStamp;
            try{
                mediaFile = File.createTempFile(filename,".jpg",mediaStorageDir);
            } catch (IOException e){
                mediaFile=null;
                Log.d("CameraMagic","nie udało się otworzyć pliku tymczasowego");
            }
        } else {
            return null;
        }

        return mediaFile;
    }


    /* Called when the user taps the Send button */
    /*
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
     */
}

