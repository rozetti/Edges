package uk.co.wideopentech.edges;

import android.widget.ImageView;

public class ProcessorModel {
    private String mName;
    private EdgeProcessor mProcessor = null;
    private ImageView mView;
    private String mLabel;

    public String getName() { return mName; }
    public ImageView getView() { return mView; }
    public EdgeProcessor getProcessor() { return mProcessor; }
    public String getLabel() { return mLabel; }

    public void setView(ImageView view) {
        mView = view;
        mProcessor.setView(view);
    }

    public ProcessorModel(final String name, EdgeProcessor.Type type, String label) {
        mName = name;
        mProcessor = new EdgeProcessor(type);
        mLabel = label;
    }
}
