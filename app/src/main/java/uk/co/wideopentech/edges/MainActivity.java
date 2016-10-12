package uk.co.wideopentech.edges;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.Toast;

import static java.lang.Math.max;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static Context context = null;

    private int mPreviewWidth;
    private int mPreviewHeight;

    private CameraPreview mCameraPreview;
    private boolean mCanUseCamera = true;

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("rsjni");
        System.loadLibrary("RSSupport");
        System.loadLibrary("native-lib");
    }

    public static Context getContext() {
        return context;
    }

    private void onCameraPermitted() {
        setContentView(R.layout.activity_main);

        setupCameraFrame();
        EdgeProcessor.setFrame(mPreviewWidth, mPreviewHeight);
        mCameraPreview = new CameraPreview(mPreviewWidth, mPreviewHeight, createProcessors());
        bindProcessorsToViews();
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
        EdgesConfig.setInstance(new EdgesConfig(this));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
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
        } else {
            mPreviewWidth = 320;
            mPreviewHeight = 240;
        }
    }

    private EdgeProcessor[] createProcessors() {
        return new EdgeProcessor[] {
                new EdgeProcessor(EdgeProcessor.Type.Greyscale, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.Canny, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.Otsu, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.Laplacian, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.Box, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.ScharrX, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.ScharrY, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.ScharrQuad, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.ScharrMax, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.Sobel, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.Chrominance, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.SobelX, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.SobelY, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.SobelQuad, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.SobelMax, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.RenderScriptHighPass, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.RenderScriptLowPass, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.RenderScriptBandPass, new ImageView(this)),
                new EdgeProcessor(EdgeProcessor.Type.RenderScriptBandStop, new ImageView(this))
        };
    }

    private void bindProcessorToView(int id, EdgeProcessor.Type type) {
        final ViewGroup.LayoutParams parms = new ViewGroup.LayoutParams(mPreviewWidth, mPreviewHeight);
        ((FrameLayout)findViewById(id)).addView(mCameraPreview.findProcessorByType(type).getView(), parms);
    }

    // todo crz: automate this
    private void bindProcessorsToViews() {
        bindProcessorToView(R.id.frame_layout_greyscale, EdgeProcessor.Type.Greyscale);
        bindProcessorToView(R.id.frame_layout_canny, EdgeProcessor.Type.Canny);
        bindProcessorToView(R.id.frame_layout_otsu, EdgeProcessor.Type.Otsu);
        bindProcessorToView(R.id.frame_layout_laplacian, EdgeProcessor.Type.Laplacian);
        bindProcessorToView(R.id.frame_layout_box, EdgeProcessor.Type.Box);
        bindProcessorToView(R.id.frame_layout_scharr_x, EdgeProcessor.Type.ScharrX);
        bindProcessorToView(R.id.frame_layout_scharr_y, EdgeProcessor.Type.ScharrY);
        bindProcessorToView(R.id.frame_layout_scharr_quad, EdgeProcessor.Type.ScharrQuad);
        bindProcessorToView(R.id.frame_layout_scharr_max, EdgeProcessor.Type.ScharrMax);
        bindProcessorToView(R.id.frame_layout_sobel, EdgeProcessor.Type.Sobel);
        bindProcessorToView(R.id.frame_layout_chrominance, EdgeProcessor.Type.Chrominance);
        bindProcessorToView(R.id.frame_layout_sobel_x, EdgeProcessor.Type.SobelX);
        bindProcessorToView(R.id.frame_layout_sobel_y, EdgeProcessor.Type.SobelY);
        bindProcessorToView(R.id.frame_layout_sobel_quad, EdgeProcessor.Type.SobelQuad);
        bindProcessorToView(R.id.frame_layout_sobel_max, EdgeProcessor.Type.SobelMax);
        bindProcessorToView(R.id.frame_layout_renderscript_high_pass, EdgeProcessor.Type.RenderScriptHighPass);
        bindProcessorToView(R.id.frame_layout_renderscript_low_pass, EdgeProcessor.Type.RenderScriptLowPass);
        bindProcessorToView(R.id.frame_layout_renderscript_band_pass, EdgeProcessor.Type.RenderScriptBandPass);
        bindProcessorToView(R.id.frame_layout_renderscript_band_stop, EdgeProcessor.Type.RenderScriptBandStop);

        ((FrameLayout)findViewById(R.id.frame_layout_greyscale)).setOnClickListener(new View.OnClickListener() {
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
