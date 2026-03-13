package com.mixfa.cptpredict.model.estimation;

import com.mixfa.cptpredict.model.VMConfig;

sealed public interface EstimationModel<EstimationModelParametersType> permits EstimationModel2 {
    String name();

    EstimationResult estimate(VMConfig vmConfig, EstimationModelParametersType parameters);

    Class<EstimationModelParametersType> parametersType();
}
