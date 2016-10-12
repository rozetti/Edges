package uk.co.wideopentech.edges;

public interface IConfigSliderListener {
    public void onSliderValueChanged(final ConfigSlider slider, int value);
    public void onInteractionStarted(final ConfigSlider slider);
    public void onInteractionStopped(final ConfigSlider slider);
}
