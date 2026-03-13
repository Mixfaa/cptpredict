package com.mixfa.cptpredict.service.impl;

import com.mixfa.cptpredict.model.estimation.EstimationModel;
import com.mixfa.cptpredict.model.estimation.EstimationModel2;
import com.mixfa.cptpredict.service.EstimationModelManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstimationModelManagerImpl implements EstimationModelManager {
    private final List<EstimationModel<?>> estimationModels = List.of(EstimationModel2.getInstance());

    @Override
    public Optional<EstimationModel<?>> getEstimationModel(String modelName) {
        for (EstimationModel<?> estimationModel : estimationModels)
            if (estimationModel.name().equals(modelName)) return Optional.of(estimationModel);
        return Optional.empty();
    }

    @Override
    public List<EstimationModel<?>> findAll() {
        return estimationModels;
    }
}
