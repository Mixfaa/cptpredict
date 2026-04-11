package com.mixfa.cptpredict.model.program;

public record ProgramStructureDataRecord(
        double dataAmount,
        double instructions,
        double cacheMisses,
        double dataBytesRead,
        double timeInMs
) {
}
