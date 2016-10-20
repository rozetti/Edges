package uk.co.wideopentech.edges;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
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

    private static int _width;
    private static int _height;
    private static int _processedPixels[] = null;
    private static Handler _viewHandler = null;

    private EdgeProcessor.Type mType;
    private ImageView mView = null;

    private Bitmap mBitmap = null;
    private boolean mEnabled = true;

    private static native void SetupProcessorsNative(int width, int height);
    private static native void SetFrameDataNative(byte[] data);
    private static native boolean ProcessNative(int type, int[] pixels);

    public static void setFrame(int width, int height) {
        _width = width;
        _height = height;
        _processedPixels = new int[_width * _height];

        SetupProcessorsNative(width, height);
    }

    public boolean getEnabled() { return mEnabled; }
    public void enable() { mEnabled = true; }
    public void disable() { mEnabled = false; }

    public static void setViewHandler(final Handler handler) {
        _viewHandler = handler;
    }
    public static void setFrameData(final byte[] data) {
        SetFrameDataNative(data);
    }

    public EdgeProcessor(EdgeProcessor.Type type)
    {
        mType = type;

        mBitmap = Bitmap.createBitmap(_width, _height, Bitmap.Config.ARGB_8888);
    }

    public void setView(ImageView view) { mView = view; }
    public View getView() {
        return mView;
    }

    public Type getType() {
        return mType;
    }

    public final Runnable Process = new Runnable()
    {
        public void run()
        {

            if (!mEnabled) return;

            ProcessNative(mType.ordinal(), _processedPixels);

            switch(mType) {
                case RenderScriptHighPass:
                    applyRenderScriptFilter(RenderScriptFilterType.HighPass, _processedPixels);
                    break;
                case RenderScriptLowPass:
                    applyRenderScriptFilter(RenderScriptFilterType.LowPass, _processedPixels);
                    break;
                case RenderScriptBandPass:
                    applyRenderScriptFilter(RenderScriptFilterType.BandPass, _processedPixels);
                    break;
                case RenderScriptBandStop:
                    applyRenderScriptFilter(RenderScriptFilterType.BandStop, _processedPixels);
                    break;
            }

            mBitmap.setPixels(_processedPixels, 0, _width, 0, 0, _width, _height);
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

        int allocation_size = _width * _height;

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

        out_alloc.copy1DRangeTo(0, allocation_size, data);
    }
}
