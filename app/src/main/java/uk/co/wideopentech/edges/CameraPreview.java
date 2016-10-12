package uk.co.wideopentech.edges;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback
{
    private static final String TAG = CameraPreview.class.getSimpleName();

    private int mPreviewWidth;
    private int mPreviewHeight;
    private EdgeProcessor[] mProcessors;

    private Camera mCamera = null;
    private Handler mProcessHandler = null;
    private boolean mIsIdle = true;

    public CameraPreview(int width, int height, final EdgeProcessor processors[])
    {
        mPreviewWidth = width;
        mPreviewHeight = height;
        mProcessors = processors;

        EdgeProcessor.setViewHandler(new Handler(Looper.getMainLooper()));

        new Thread() {
            public void run() {
                Looper.prepare();

                mProcessHandler = new Handler(Looper.myLooper());

                Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                    @Override
                    public boolean queueIdle() {
                        mIsIdle = true;
                        return true;
                    }
                });

                Looper.loop();
            }
        }.start();
    }

    public EdgeProcessor findProcessorByType(final EdgeProcessor.Type type) {

        for (EdgeProcessor p : mProcessors) {
            if (p.getType() == type) {
                return p;
            }
        }

        return null;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera arg1)
    {
        if (null == mProcessHandler) return;

        if (!mIsIdle) return;
        mIsIdle = false;

        EdgeProcessor.setFrameData(data);

        for (final EdgeProcessor p : mProcessors) {
            mProcessHandler.post(p.Process);
        }
    }

    public void onPause()
    {
        mCamera.stopPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
    {
        final Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);

        if (parameters.getPreviewFormat() == ImageFormat.NV21) {
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0)
    {
        try {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            mCamera = Camera.open(0);
        }
        catch (Exception ex) {
            Log.e(TAG, "failed to open camera (probably permissions..): " + ex.getMessage());
            return;
        }

        try
        {
            mCamera.setPreviewDisplay(arg0);
            mCamera.setPreviewCallback(this);
        }
        catch (IOException e)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0)
    {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}