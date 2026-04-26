package com.mixfa.cptpredict.misc.datacollection;

import java.util.List;

public interface DataCollector {
    void collectData(CollectableData data);

    List<CollectableData> getResults();
}
