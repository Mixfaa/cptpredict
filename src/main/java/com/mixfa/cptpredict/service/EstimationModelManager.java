package com.mixfa.cptpredict.service;

import com.mixfa.cptpredict.model.estimation.EstimationModel;

import java.util.List;
import java.util.Optional;

public interface EstimationModelManager {
    Optional<EstimationModel<?>> getEstimationModel(String modelName);

    List<EstimationModel<?>> findAll();
}
