package com.autocloset.mobile;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.content.Context.WINDOW_SERVICE;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    private boolean isPreviewRunning;
    private Context c;

    private static Camera.Size size;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        c = context;

        // install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // the Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

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

        // start preview with new settings

        if (isPreviewRunning) {
            mCamera.stopPreview();
        }

        Camera.Parameters parameters = mCamera.getParameters();
        Display display = ((WindowManager) c.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // rotate camera preview to be upright

        if(display.getRotation() == Surface.ROTATION_0) {
            setPreviewSize(parameters, height, width);
            mCamera.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_90) {
            setPreviewSize(parameters, width, height);
        }

        if(display.getRotation() == Surface.ROTATION_180) {
            setPreviewSize(parameters, height, width);
        }

        if(display.getRotation() == Surface.ROTATION_270) {
            setPreviewSize(parameters, width, height);
            mCamera.setDisplayOrientation(180);
        }

        mCamera.setParameters(parameters);

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            isPreviewRunning = true;

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    // updates the preview's dimensions
    private void setPreviewSize(Camera.Parameters param, int w, int h) {

        List<Camera.Size> allSizes = param.getSupportedPreviewSizes();
        Camera.Size size = allSizes.get(0); // get top size
        for (int i = 0; i < allSizes.size(); i++) {

            if (allSizes.get(i).width > w)
                size = allSizes.get(i);
            this.size = size;
        }

        param.setPreviewSize(size.width, size.height);
    }

    public Camera.Size getSize() {

        return size;
    }
}
