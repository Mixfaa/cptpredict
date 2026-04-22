package com.mixfa.cptpredict.ui.misc;

import com.mixfa.cptpredict.model.program.ComplexityModel;
import com.mixfa.cptpredict.model.program.ProgramInfo;

public interface ComplexityModelsToText {
    static String apply(
            ComplexityModel instr,
            ComplexityModel cache,
            ComplexityModel ram,
            ComplexityModel dataRead,
            ComplexityModel time) {
        return String.format(
                "Instructions complexity: %s\nCache misses complexity: %s\nRam usage: %s\nData bytes read complexity: %s\nTime complexity: %s",
                instr.formula(),
                cache.formula(),
                ram.formula(),
                dataRead.formula(),
                time.formula()
        );
    }

    static String apply(ProgramInfo p) {
        return apply(p.instructionModel(), p.cacheMissesModel(), p.ramUsageModel(), p.dataReadModel(), p.timeModel());
    }
}