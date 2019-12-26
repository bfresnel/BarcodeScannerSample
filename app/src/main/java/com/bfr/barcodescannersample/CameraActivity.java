package com.bfr.barcodescannersample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.Collections;

public class CameraActivity extends AppCompatActivity {

    public CameraManager cameraManager;
    public int cameraFacing;
    private static final int CAMERA_REQUEST_CODE = 2000;
    private static final String TAG = "CameraActivity";
    private String cameraId;
    private Handler backgroundHandler;
    private CameraDevice.StateCallback stateCallback;
    protected CameraDevice cameraDevice;
    protected TextureView textureView;
    protected TextureView.SurfaceTextureListener surfaceTextureListener;
    private CameraCaptureSession cameraCaptureSession;
    private HandlerThread backgroundThread;
    protected Size previewSize;
    protected CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest captureRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        textureView = (TextureView) findViewById(R.id.textureViewCamera);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraFacing = CameraCharacteristics.LENS_FACING_BACK;

        assert cameraManager != null;
        Log.d(TAG, "cameraManager value : " + cameraManager.toString());
        Log.d(TAG, "cameraFacing value : " + cameraFacing);

        surfaceTextureListener = new TextureView.SurfaceTextureListener(){

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                setUpCamera();
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };

        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {
                CameraActivity.this.cameraDevice = cameraDevice;
                createPreviewSession();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {

            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int i) {

            }
        };
    }

    private void createPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (cameraDevice == null) {
                                return;
                            }

                            try {
                                captureRequest = captureRequestBuilder.build();
                                CameraActivity.this.cameraCaptureSession = cameraCaptureSession;
                                CameraActivity.this.cameraCaptureSession.setRepeatingRequest(captureRequest,
                                        null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        openBackgroundThread();
        if(textureView.isAvailable()){
            setUpCamera();
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            closeCamera();
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeBackgroundThread();
    }

    private void closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }

    private void closeCamera() throws IOException {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void setUpCamera() {
        try {
            for (String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics =
                        cameraManager.getCameraCharacteristics(cameraId);
                Log.d(TAG,cameraCharacteristics.toString());

                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraFacing){
                    StreamConfigurationMap streamConfigurationMap =
                            cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    assert streamConfigurationMap != null;
                    previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                    this.cameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED){
                cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openBackgroundThread(){
        backgroundThread = new HandlerThread("camera_background_thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }
}
