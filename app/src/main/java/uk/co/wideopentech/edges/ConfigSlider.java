package uk.co.wideopentech.edges;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class ConfigSlider extends LinearLayout {
    private static final String TAG = ConfigSlider.class.getSimpleName();

    private static final String STATE_VALUE = "Value";
    private static final String STATE_SUPER_CLASS = "SuperClass";

    private IConfigSliderListener mListener = null;

    private String mTitle = "";
    private int mSliderMax = 100;
    private int mSliderMin = 0;
    private int mSliderValue = 0;
    private int mSliderDefaultValue = 0;

    private TextView mTitleTextView = null;
    private TextView mSliderTextView = null;
    private SeekBar mSlider = null;
    private int mIdTag = 0;

    public ConfigSlider(Context context) {
        super(context);

        initialiseAttrs(getContext(), null);
        initialiseLayout(context);
    }

    public ConfigSlider(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialiseAttrs(getContext(), attrs);
        initialiseLayout(context);
    }

    public ConfigSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initialiseAttrs(getContext(), attrs);
        initialiseLayout(context);
    }

    public void setListener(final IConfigSliderListener listener) {
        mListener = listener;
    }

    public void setValue(int value) {
        setValue(value, false);
    }

    public void setIdTag(int tag) {
        mIdTag = tag;
    }

    public int getIdTag() {
        return mIdTag;
    }

    public void setValue(int value, Boolean shouldRaiseEvents) {
        if (value != mSliderValue) {
            mSliderValue = value;

            if (shouldRaiseEvents) {
                raiseOnValueChanged();
            }
        }

        updateUI();
    }

    private void initialiseLayout(final Context context) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.config_slider, this);
    }

    private void initialiseAttrs(final Context context, final AttributeSet attrs) {

        if (null != attrs) {
            final TypedArray aa = context.obtainStyledAttributes(attrs, R.styleable.ConfigSlider);

            mTitle = aa.getString(R.styleable.ConfigSlider_label);
            mSliderMin = aa.getInt(R.styleable.ConfigSlider_min, 0);
            mSliderMax = aa.getInt(R.styleable.ConfigSlider_max, 100);
            mSliderDefaultValue = aa.getInt(R.styleable.ConfigSlider_value, 50);
        }

        assert mSliderMin < mSliderMax;
        assert mSliderValue >= mSliderMin;
        assert mSliderValue <= mSliderMax;
    }

    private void initialiseViews() {

        mTitleTextView = (TextView)findViewById(R.id.titleTextView);
        mSliderTextView = (TextView)findViewById(R.id.sliderTextView);
        mSlider = (SeekBar)findViewById(R.id.slider);

        mTitleTextView.setText(mTitle);
        mSlider.setMax(mSliderMax - mSliderMin);
        mSlider.setProgress(mSliderValue - mSliderMin);

        updateUI();

        mSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, int progress, boolean fromUser) {
                mSliderValue = progress + mSliderMin;

                updateUI();

                raiseOnValueChanged();
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
                raiseOnInteractionStarted();
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                raiseOnInteractionStopped();
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        initialiseViews();
    }

    protected void updateUI()
    {
        mSliderTextView.setText(String.valueOf(mSliderValue));
        mSlider.setProgress(mSliderValue - mSliderMin);
    }

    protected void raiseOnValueChanged()
    {
        IConfigSliderListener listener = mListener;

        if (null == listener) return;

        listener.onSliderValueChanged(this, mSliderValue);
    }

    protected void raiseOnInteractionStarted()
    {
        IConfigSliderListener listener = mListener;

        if (null == listener) return;

        listener.onInteractionStarted(this);
    }

    protected void raiseOnInteractionStopped()
    {
        IConfigSliderListener listener = mListener;

        if (null == listener) return;

        listener.onInteractionStopped(this);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable(STATE_SUPER_CLASS,
                super.onSaveInstanceState());
        bundle.putInt(STATE_VALUE, mSliderValue);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle)state;

            super.onRestoreInstanceState(bundle
                    .getParcelable(STATE_SUPER_CLASS));
            setValue(bundle.getInt(STATE_VALUE));
        }
        else {
            super.onRestoreInstanceState(state);
            setValue(mSliderDefaultValue);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        super.dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        super.dispatchThawSelfOnly(container);
    }
}
