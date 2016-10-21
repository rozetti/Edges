package uk.co.wideopentech.edges;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

import static java.lang.Math.max;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static Context context = null;

    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mGridColumnCount;

    private CameraPreview mCameraPreview;
    private boolean mCanUseCamera = true;

    private ProcessorModels mProcessors = null;

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    public static Context getContext() {
        return context;
    }

    private void onCameraPermitted() {
        setContentView(R.layout.activity_main);

        setupCameraFrame();
        mCameraPreview = new CameraPreview(mPreviewWidth, mPreviewHeight, mProcessors);
        createAndBindViews();
        createCameraSurface();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                mCanUseCamera = false;
                Log.e(TAG, "Cant use the camera");
            } else {
                onCameraPermitted();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        mProcessors = createProcessors();
        EdgesConfig.setInstance(new EdgesConfig(this, mProcessors));
        EdgesConfig.getInstance().DefaultConfig();

        InputStream str = getResources().openRawResource(R.raw.lbpcascade_frontalface);
        FaceDetector.init(str);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                onCameraPermitted();
            }
        } else {
            onCameraPermitted();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.config:
                startActivity(new Intent(MainActivity.this, ConfigActivity.class));
                return true;
            case R.id.defauls:
                EdgesConfig.getInstance().DefaultConfig();
                Toast.makeText(this, "Sensible defaults restored", Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupCameraFrame() {

        final DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        // todo crz: handle all frame sizes
        if (max(dm.widthPixels, dm.heightPixels) >= 640 * 3 + 100) {
            mPreviewWidth = 640;
            mPreviewHeight = 480;
            mGridColumnCount = 2;
        } else {
            mPreviewWidth = 320;
            mPreviewHeight = 240;
            if (max(dm.widthPixels, dm.heightPixels) >= 320 * 3 + 100) {
                mGridColumnCount = 3;
            } else {
                mGridColumnCount = 2;
            }
        }
    }

    private ProcessorModels createProcessors() {
        ProcessorModel[] models = new ProcessorModel[] {
                new ProcessorModel("Greyscale", EdgeProcessor.Type.Greyscale, "OpenCV Pre-process"),
                new ProcessorModel("Canny", EdgeProcessor.Type.Canny, "OpenCV Canny"),
                new ProcessorModel("Otsu", EdgeProcessor.Type.Otsu, "OpenCV Otsu"),
                new ProcessorModel("Face", EdgeProcessor.Type.Face, "OpenCV Face Detection"),
                new ProcessorModel("Laplacian", EdgeProcessor.Type.Laplacian, "OpenCV Laplacian"),
                new ProcessorModel("Box", EdgeProcessor.Type.Box, "OpenCV Box Filter"),
                new ProcessorModel("ScharrX", EdgeProcessor.Type.ScharrX, "OpenCV Scharr X"),
                new ProcessorModel("ScharrY", EdgeProcessor.Type.ScharrY, "OpenCV Scharr Y"),
                new ProcessorModel("ScharrQuad", EdgeProcessor.Type.ScharrQuad, "OpenCV Scharr Diff"),
                new ProcessorModel("ScharrMax", EdgeProcessor.Type.ScharrMax, "OpenCV Scharr Max"),
                new ProcessorModel("Sobel", EdgeProcessor.Type.Sobel, "OpenCV Sobel"),
                new ProcessorModel("Chrominance", EdgeProcessor.Type.Chrominance, "Camera Chrominance"),
                new ProcessorModel("SobelX", EdgeProcessor.Type.SobelX, "OpenCV Sobel X"),
                new ProcessorModel("SobelY", EdgeProcessor.Type.SobelY, "OpenCV Sobel Y"),
                new ProcessorModel("SobelQuad", EdgeProcessor.Type.SobelQuad, "OpenCV Sobel Diff"),
                new ProcessorModel("SobelMax", EdgeProcessor.Type.SobelMax, "OpenCV Sobel Max"),
                new ProcessorModel("RenderScriptHighPass", EdgeProcessor.Type.RenderScriptHighPass, "RenderScript High Pass"),
                new ProcessorModel("RenderScriptHighPass", EdgeProcessor.Type.RenderScriptLowPass, "RenderScript Low Pass"),
                new ProcessorModel("RenderScriptBandPass", EdgeProcessor.Type.RenderScriptBandPass, "RenderScript Band Pass"),
                new ProcessorModel("RenderScriptBandStop", EdgeProcessor.Type.RenderScriptBandStop, "RenderScript Band Stop")
        };

        return new ProcessorModels(models);
    }

    private void createAndBindViews() {
        final LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(mPreviewWidth, mPreviewHeight);

        final ImageView greyscaleImageView = (ImageView)findViewById(R.id.greyscale_image_view);
        greyscaleImageView.setLayoutParams(parms);

        final GridLayout grid = (GridLayout)findViewById(R.id.grid_layout);
        grid.setColumnCount(mGridColumnCount);

        for (final ProcessorModel model : mProcessors.getModels()) {
            model.getProcessor().setFrame(mPreviewWidth, mPreviewHeight);

            if (model.getName().equals("Greyscale")) {
                ImageView iv = (ImageView)findViewById(R.id.greyscale_image_view);
                model.setView(iv);
            } else {
                LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.grid_item_layout, grid, false);
                ImageView iv = (ImageView) layout.findViewById(R.id.grid_item_image_view);
                TextView tv = (TextView) layout.findViewById(R.id.grid_item_text_view);

                tv.setText(model.getLabel());
                iv.setLayoutParams(parms);

                model.setView(iv);

                grid.addView(layout);
            }
        }

        mProcessors.findModelByName("Box").getProcessor().disable();

        mProcessors.findModelByName("Greyscale").getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ConfigActivity.class));
            }
        });
    }

    private void createCameraSurface() {
        final SurfaceView cameraSurfaceView = new SurfaceView(this);
        final SurfaceHolder cameraSurfaceViewHolder = cameraSurfaceView.getHolder();
        cameraSurfaceViewHolder.addCallback(mCameraPreview);
        cameraSurfaceViewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        final ViewGroup.LayoutParams parms = new ViewGroup.LayoutParams(mPreviewWidth, mPreviewHeight);
        ((FrameLayout)findViewById(R.id.frame_layout_camera)).addView(cameraSurfaceView, parms);
    }

    @Override
    protected void onPause() {
        super.onPause();

        EdgesConfig.getInstance().SaveConfig();
    }

    @Override
    protected void onResume() {
        super.onResume();

        EdgesConfig.getInstance().RestoreConfig();
    }
}
