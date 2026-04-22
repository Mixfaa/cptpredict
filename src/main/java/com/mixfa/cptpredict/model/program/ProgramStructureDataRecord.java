package com.mixfa.cptpredict.model.program;

public record ProgramStructureDataRecord(
        double dataAmount,
        double instructions,
        double cacheMisses,
        double ramUsedKb,
        double dataBytesRead,
        double timeInMs
) {
}
