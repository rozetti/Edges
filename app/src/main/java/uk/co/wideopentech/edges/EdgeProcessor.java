package uk.co.wideopentech.edges;

import android.graphics.Bitmap;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.view.View;
import android.widget.ImageView;

public class EdgeProcessor {

    private static final String TAG = EdgeProcessor.class.getSimpleName();

    private enum RenderScriptFilterType {
        None,
        HighPass,
        LowPass,
        BandPass,
        BandStop
    }

    public enum Type {
        Greyscale,
        Canny,
        Otsu,
        Face,
        Laplacian,
        Box,
        ScharrX,
        ScharrY,
        ScharrQuad,
        ScharrMax,
        SobelX,
        SobelY,
        SobelQuad,
        SobelMax,
        Sobel,
        Chrominance,
        RenderScriptHighPass,
        RenderScriptLowPass,
        RenderScriptBandPass,
        RenderScriptBandStop
    }

    private int mWidth;
    private int mHeight;
    private int mProcessedPixels[] = null;
    private static Handler _viewHandler = null;

    private int mNativeId;
    private EdgeProcessor.Type mType;
    private ImageView mView = null;

    private Bitmap mBitmap = null;
    private boolean mEnabled = true;

    private static native void SetupProcessorsNative(int id, int width, int height);
    private static native void SetFrameDataNative(int id, byte[] data);
    private static native int CreateProcessorNative(int type);
    private static native boolean ProcessNative(int id, int[] pixels);

    public void enable() { mEnabled = true; }
    public void disable() { mEnabled = false; }
    public boolean getEnabled() { return mEnabled; }

    public void setView(ImageView view) { mView = view; }
    public View getView() {
        return mView;
    }

    public int getId() { return mNativeId; }
    public Type getType() {
        return mType;
    }

    public static void setViewHandler(final Handler handler) {
        _viewHandler = handler;
    }

    public EdgeProcessor(EdgeProcessor.Type type)
    {
        mType = type;

        mNativeId = CreateProcessorNative(type.ordinal());
    }

    public void setFrame(int width, int height) {
        mWidth = width;
        mHeight = height;
        mProcessedPixels = new int[width * height];

        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

        SetupProcessorsNative(mNativeId, width, height);
    }

    public void setFrameData(final byte[] data) {
        SetFrameDataNative(mNativeId, data);
    }

    public final Runnable Process = new Runnable()
    {
        public void run()
        {

            if (!mEnabled) return;

            ProcessNative(mNativeId, mProcessedPixels);

            switch(mType) {
                case RenderScriptHighPass:
                    applyRenderScriptFilter(RenderScriptFilterType.HighPass, mProcessedPixels);
                    break;
                case RenderScriptLowPass:
                    applyRenderScriptFilter(RenderScriptFilterType.LowPass, mProcessedPixels);
                    break;
                case RenderScriptBandPass:
                    applyRenderScriptFilter(RenderScriptFilterType.BandPass, mProcessedPixels);
                    break;
                case RenderScriptBandStop:
                    applyRenderScriptFilter(RenderScriptFilterType.BandStop, mProcessedPixels);
                    break;
            }

            mBitmap.setPixels(mProcessedPixels, 0, mWidth, 0, 0, mWidth, mHeight);
            _viewHandler.post(new Runnable() {
                @Override
                public void run() {
                    mView.setImageBitmap(mBitmap);
                }
            });
        }
    };

    private void applyRenderScriptFilter(RenderScriptFilterType filterType, int[] data)
    {
        if (RenderScriptFilterType.None == filterType) return;

        int allocation_size = mWidth * mHeight;

        RenderScript rs = RenderScript.create(MainActivity.getContext());

        Allocation in_alloc = Allocation.createSized(rs, Element.U32(rs), allocation_size);
        in_alloc.copy1DRangeFrom(0, allocation_size, data);

        Allocation out_alloc = Allocation.createTyped(rs, in_alloc.getType());

        switch(filterType) {
            case HighPass:
                ScriptC_high_pass_filter high_pass_filter = new ScriptC_high_pass_filter(rs);
                high_pass_filter.forEach_root(in_alloc, out_alloc);
                break;
            case LowPass:
                ScriptC_low_pass_filter low_pass_filter = new ScriptC_low_pass_filter(rs);
                low_pass_filter.forEach_root(in_alloc, out_alloc);
                break;
            case BandPass:
                ScriptC_band_pass_filter band_pass_filter = new ScriptC_band_pass_filter(rs);
                band_pass_filter.forEach_root(in_alloc, out_alloc);
                break;
            case BandStop:
                ScriptC_band_stop_filter band_stop_filter = new ScriptC_band_stop_filter(rs);
                band_stop_filter.forEach_root(in_alloc, out_alloc);
                break;
        }

        out_alloc.copyTo(data);
    }
}
