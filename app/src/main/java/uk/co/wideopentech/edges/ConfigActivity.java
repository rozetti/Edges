package uk.co.wideopentech.edges;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

public class ConfigActivity extends Activity implements IConfigSliderListener {

    private static final String TAG = ConfigActivity.class.getSimpleName();

    ConfigSlider mBlurKernelSizeSlider = null;
    ConfigSlider mContrastSlider = null;
    ConfigSlider mBrightnessSlider = null;
    ConfigSlider mSobelKernelSize = null;
    ConfigSlider mCannyThreshold1 = null;
    ConfigSlider mCannyThreshold2 = null;
    ConfigSlider mCannyKernelSize = null;
    ConfigSlider mOtsuThreshold = null;
    ConfigSlider mOtsuMax = null;
    Button mDefaultsButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);

        initWindow();
        initControls();
    }

    private static void setDefault(ConfigSlider cs) {
        int v = EdgesConfig.getParameterDefaultValue(EdgesConfig.ParameterType.values()[cs.getIdTag()]);
        cs.setValue(v);
    }

    private void setDefaults() {
        setDefault(mBlurKernelSizeSlider);
        setDefault(mContrastSlider);
        setDefault(mBrightnessSlider);
        setDefault(mSobelKernelSize);
        setDefault(mCannyThreshold1);
        setDefault(mCannyThreshold2);
        setDefault(mCannyKernelSize);
        setDefault(mOtsuThreshold);
        setDefault(mOtsuMax);
    }

    private void initControls() {
        mBlurKernelSizeSlider = configureSlider(R.id.csBlurKernelSize, EdgesConfig.ParameterType.BLUR_KERNEL_SIZE);
        mContrastSlider = configureSlider(R.id.csContrast, EdgesConfig.ParameterType.CONTRAST);
        mBrightnessSlider = configureSlider(R.id.csBrightness, EdgesConfig.ParameterType.BRIGHTNESS);
        mSobelKernelSize = configureSlider(R.id.csKernelSize, EdgesConfig.ParameterType.KERNEL_SIZE);
        mCannyThreshold1 = configureSlider(R.id.csCannyThreshold1, EdgesConfig.ParameterType.CANNY_THRESHOLD1);
        mCannyThreshold2 = configureSlider(R.id.csCannyThreshold2, EdgesConfig.ParameterType.CANNY_THRESHOLD2);
        mCannyKernelSize = configureSlider(R.id.csCannyKernelSize, EdgesConfig.ParameterType.CANNY_KERNEL_SIZE);
        mOtsuThreshold = configureSlider(R.id.csOtsuThreshold, EdgesConfig.ParameterType.OTSU_THRESHOLD);
        mOtsuMax = configureSlider(R.id.csOtsuMas, EdgesConfig.ParameterType.OTSU_MAX);

        mDefaultsButton = (Button) findViewById(R.id.defaultsButton);
        mDefaultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               setDefaults();
            }
        });
    }

    private void initWindow() {
        final DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        final int width = (int) (0.7 * dm.widthPixels);
        final int height = (int) (0.7 * dm.heightPixels);
        getWindow().setLayout(width, height);
    }

    private ConfigSlider configureSlider(int id, EdgesConfig.ParameterType type) {
        final int id_tag = type.ordinal();
        final ConfigSlider slider = (ConfigSlider) findViewById(id);
        slider.setIdTag(id_tag);
        slider.setValue(EdgesConfig.getInstance().GetParameter(id_tag));
        slider.setListener(this);
        return slider;
    }

    @Override
    public void onSliderValueChanged(ConfigSlider slider, int value) {
        EdgesConfig.getInstance().SetParameter(slider.getIdTag(), value);
    }

    @Override
    public void onInteractionStarted(ConfigSlider slider) {
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onInteractionStopped(ConfigSlider slider) {
        getWindow().setBackgroundDrawableResource(R.color.configBackground);
    }

    @Override
    protected void onPause() {
        super.onPause();

        EdgesConfig.getInstance().SaveConfig();
    }
}
