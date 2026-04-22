package com.mixfa.cptpredict.misc.datacollection;

import java.util.ArrayList;
import java.util.List;

public class SimpleDataCollector implements DataCollector {
    private final List<CollectableData> results = new ArrayList<>();

    @Override
    public void collectData(CollectableData data) {
        results.add(data);
    }

    @Override
    public List<CollectableData> getResults() {
        return results;
    }
}
