package uk.co.wideopentech.edges;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class EdgesConfig {
    private static final String TAG = EdgesConfig.class.getSimpleName();

    private static EdgesConfig instance = null;

    public static EdgesConfig getInstance() { return instance; }
    public static void setInstance(EdgesConfig config) { instance = config; }

    private static native void SetParameterNative(int parameter, int value);
    private static native int GetParameterNative(int parameter);

    private SharedPreferences mPreferences = null;

    public enum ParameterType
    {
        INVALID,
        BLUR_KERNEL_SIZE,
        CONTRAST,
        BRIGHTNESS,
        KERNEL_SIZE,
        CANNY_THRESHOLD1,
        CANNY_THRESHOLD2,
        CANNY_KERNEL_SIZE,
        OTSU_THRESHOLD,
        OTSU_MAX,
        PARAMETER_COUNT
    }

    public static int getParameterDefaultValue(ParameterType type) {
        switch(type) {
            case BLUR_KERNEL_SIZE:
                return 8;
            case CONTRAST:
                return 75;
            case BRIGHTNESS:
                return -70;
            case CANNY_THRESHOLD1:
                return 80;
            case CANNY_THRESHOLD2:
                return 100;
            case CANNY_KERNEL_SIZE:
                return 2;
            case OTSU_THRESHOLD:
                return 0;
            case OTSU_MAX:
                return 255;
            case KERNEL_SIZE:
                return 3;
        }

        return 0;
    }

    public EdgesConfig(final Activity activity) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
    }

    public void SaveConfig() {
        SharedPreferences.Editor editor = mPreferences.edit();

        for(ParameterType type : ParameterType.values()) {
            final int value = GetParameterNative(type.ordinal());
            Log.i(TAG, "saving parameter: " + type.name() + " = " + String.valueOf(value));
            editor.putInt(type.name(), value);
        }

        editor.commit();
    }

    protected void RestoreConfig() {

        for(ParameterType type : ParameterType.values()) {
            final int value = mPreferences.getInt(type.name(), getParameterDefaultValue(type));
            Log.i(TAG, "loading parameter: " + type.name() + " = " + String.valueOf(value));
            SetParameterNative(type.ordinal(), value);
        }
    }

    public void DefaultConfig() {

        for(ParameterType type : ParameterType.values()) {
            final int value = getParameterDefaultValue(type);
            SetParameterNative(type.ordinal(), value);
        }
    }

    public void SetParameter(int parameter, int value) {
        SetParameterNative(parameter, value);
    }

    public int GetParameter(int parameter) {
        return GetParameterNative(parameter);
    }


}
