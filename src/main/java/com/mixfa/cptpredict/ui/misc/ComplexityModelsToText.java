package com.mixfa.cptpredict.ui.misc;

import com.mixfa.cptpredict.model.program.ComplexityModel;
import com.mixfa.cptpredict.model.program.ProgramInfo;

public interface ComplexityModelsToText {
    static String apply(
            ComplexityModel c1,
            ComplexityModel c2,
            ComplexityModel c3,
            ComplexityModel c4) {
        return String.format(
                "Instructions complexity: %s\nCache misses complexity: %s\nData bytes read complexity: %s\nTime complexity: %s",
                c1.formula(),
                c2.formula(),
                c3.formula(),
                c4.formula()
        );
    }

    static String apply(ProgramInfo p) {
        return apply(p.instructionModel(), p.cacheMissesModel(), p.dataReadModel(), p.timeModel());
    }
}