package com.example.krzysiek.myfirstapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera = null;
    private Activity activity;
    public static final int K_STATE_PREVIEW = 0;
    public static final int K_STATE_FROZEN = 1;
    int mPreviewState = K_STATE_PREVIEW;
    boolean isPreviewRunning = false;

    private Context mContext;
    private int mCameraId;

    public int ROTATION = 0;
    RectF rectF = new RectF();
    public static int mDisplayOrientation;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mContext = context;
        activity = (Activity) context;
    }
    public CameraPreview(Context context, AttributeSet attr) {
        super(context, attr);

        mContext = context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);

                Activity myActivity = (Activity)getContext();
            }
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        //znajodwanie i ustawienie rozmiaru podglądu
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size bestSize = null;

        List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }
        parameters.setPreviewSize(bestSize.width, bestSize.height);

        //znalezienie i ustawienie rozmiaru zdjecia
        List<Camera.Size> supportedSizes = parameters.getSupportedPictureSizes();
        bestSize = supportedSizes.get(0);
        for(int i = 1; i < supportedSizes.size(); i++){
            if((supportedSizes.get(i).width * supportedSizes.get(i).height) >
                    (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }
        parameters.setPictureSize(bestSize.width, bestSize.height);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);
        mCamera.setParameters(parameters);

        setCameraDisplayOrientation(activity,mCameraId,mCamera); //TODO CAMERAID

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void PreviewCamera(){
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d("Camera Magic", "Nie udało się utworzyć poglądu", e);
        }
    }

    public void setCamera(Camera camera){
        if (mCamera == camera) { return; }

        stopPreviewAndFreeCamera();
        mCamera = camera;

        if (mCamera != null) {
            requestLayout();
            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
        }
    }

    public void stopPreviewAndFreeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void onClick (View v) {
        switch (mPreviewState){
            case K_STATE_FROZEN:
                mCamera.startPreview();
                mPreviewState = K_STATE_PREVIEW;
                break;
            default:
                mCamera.takePicture(null,null,null);
                mPreviewState = K_STATE_FROZEN;
        }
    }
    public void switchCamera(Camera camera){
        setCamera(camera);
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            Log.e("Camera Magic","IOEcpetion na setpreviewdisplay", e);
        }
        mCamera.stopPreview();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(getWidth(),getHeight());
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);
        requestLayout();

        camera.setParameters(parameters);
        mCamera.startPreview();
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        mDisplayOrientation = result;
    }

    public void setCameraID(int id) {
        mCameraId = id;
    }


}