package uk.co.wideopentech.edges;

import java.util.ArrayList;
import java.util.List;

public class ProcessorModels {
    private ProcessorModel[] mModels;

    public final ProcessorModel[] getModels() { return mModels; }

    public ProcessorModels(ProcessorModel[] models) {
        mModels = models;
    }

    public ProcessorModel findModelByName(final String name) {
        ProcessorModel result = null;

        for(ProcessorModel m : mModels) {
            if (m.getName().equals(name)) {
                result = m;
                break;
            }
        }

        return result;
    }

    public ProcessorModel[] findModelsByType(EdgeProcessor.Type type) {
        List<ProcessorModel> models = new ArrayList<ProcessorModel>();

        for(ProcessorModel m : mModels) {
            if (m.getProcessor().getType() == type) {
                models.add(m);
            }
        }

        ProcessorModel[] result = new ProcessorModel[models.size()];

        return models.toArray(result);
    }
}
